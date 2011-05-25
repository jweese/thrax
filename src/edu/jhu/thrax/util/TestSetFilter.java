package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;

import edu.jhu.thrax.ThraxConfig;

public class TestSetFilter
{
    private static List<String> testSentences;
    private static Map<String,Set<Integer>> sentencesByWord;

    private static final String NT_REGEX = "\\[[^\\]]+?\\]";

	private static boolean verbose = false;

    private static void getTestSentences(String filename)
    {
        try {
            Scanner scanner = new Scanner(new File(filename), "UTF-8");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                addSentenceToWordHash(sentencesByWord, line, testSentences.size());
                testSentences.add(line);
            }
        }
        catch (FileNotFoundException e) {
            System.err.printf("Could not open %s\n", e.getMessage());
        }

		if (verbose) 
			System.err.println("Added " + testSentences.size() + " sentences.\n");
    }

	/** setSentence()
	 *
	 * Sets a single sentence against which the grammar is filtered.
	 * Used in filtering the grammar on the fly at runtime.
	 */
	public static void setSentence(String sentence) {
		if (testSentences == null)
			testSentences = new ArrayList<String>();

		if (sentencesByWord == null)
			sentencesByWord = new HashMap<String,Set<Integer>>();

		// reset the list of sentences and the hash mapping words to
		// sets of sentences they appear in
		testSentences.clear();
		sentencesByWord.clear();
		// fill in the hash with the current sentence
		addSentenceToWordHash(sentencesByWord, sentence, 0);
		// and add the sentence
		testSentences.add(sentence);
	}

	/** filterGrammarToFile
	 *
	 * Filters a large grammar against a single sentence, and writes
	 * the resulting grammar to a file.  The input grammar is assumed
	 * to be compressed, and the output file is also compressed.
	 */
	public static void filterGrammarToFile(String fullGrammarFile,
										   String sentence,
										   String filteredGrammarFile) {
		
		setSentence(sentence);

		try {
			Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(fullGrammarFile)), "UTF-8");
			int rulesIn = 0;
			int rulesOut = 0;
			boolean verbose = false;
			if (verbose)
				System.err.println("Processing rules...");

			GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(filteredGrammarFile));
			byte newline[] = "\n".getBytes("UTF-8");

			while (scanner.hasNextLine()) {
				if (verbose) {
					if ((rulesIn+1) % 2000 == 0) {
						System.err.print(".");
						System.err.flush();
					}
					if ((rulesIn+1) % 100000 == 0) {
						System.err.println(" [" + (rulesIn+1) + "]");
						System.err.flush();
					}
				}
				rulesIn++;
				String rule = scanner.nextLine();
				if (inTestSet(rule)) {
					byte[] bytes = rule.getBytes("UTF-8");
					int len = bytes.length;
					out.write(bytes, 0, len);
					out.write(newline, 0, 1);
					rulesOut++;
				}
			}

			out.close();

			if (verbose) {
				System.err.println("[INFO] Total rules read: " + rulesIn);
				System.err.println("[INFO] Rules kept: " + rulesOut);
				System.err.println("[INFO] Rules dropped: " + (rulesIn - rulesOut));
			}
		} catch (FileNotFoundException e) {
            System.err.printf("* FATAL: could not open %s\n", e.getMessage());
		} catch (IOException e) {
            System.err.printf("* FATAL: could not write to %s\n", e.getMessage());
        }
	}

    public static Pattern getPattern(String rule)
    {
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        if (parts.length != 4) {
            return null;
        }
        String source = parts[1].trim();
        String pattern = Pattern.quote(source);
        pattern = pattern.replaceAll(NT_REGEX, "\\\\E.+\\\\Q");
        pattern = pattern.replaceAll("\\\\Q\\\\E", "");
        pattern = "(?:^|\\s)" + pattern + "(?:$|\\s)";
        return Pattern.compile(pattern);
    }

    private static boolean inTestSet(String rule)
    {
        Pattern pattern = getPattern(rule);
        for (int i : getSentencesForRule(sentencesByWord, rule)) {
            if (pattern.matcher(testSentences.get(i)).find()) {
                return true;
            }
        }
        return hasAbstractSource(rule);
    }

    private static void addSentenceToWordHash(Map<String,Set<Integer>> sentencesByWord, String sentence, int index)
    {
        String [] tokens = sentence.split("\\s+");
        for (String t : tokens) {
            if (sentencesByWord.containsKey(t))
                sentencesByWord.get(t).add(index);
            else {
                Set<Integer> set = new HashSet<Integer>();
                set.add(index);
                sentencesByWord.put(t, set);
            }
        }
    }

    private static Set<Integer> getSentencesForRule(Map<String,Set<Integer>> sentencesByWord, String rule)
    {
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        if (parts.length != 4)
            return Collections.emptySet();
        String source = parts[1].trim();
        List<Set<Integer>> list = new ArrayList<Set<Integer>>();
        for (String t : source.split("\\s+")) {
            if (t.matches(NT_REGEX))
                continue;
            if (sentencesByWord.containsKey(t))
                list.add(sentencesByWord.get(t));
            else
                return Collections.emptySet();
        }
        return intersect(list);
    }

    private static boolean hasAbstractSource(String rule)
    {
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        if (parts.length != 4)
            return false;
        String source = parts[1].trim();
        for (String t : source.split("\\s+")) {
            if (!t.matches(NT_REGEX))
                return false;
        }
        return true;
    }

    private static <T> Set<T> intersect(List<Set<T>> list)
    {
        if (list.isEmpty())
            return Collections.emptySet();
        Set<T> result = new HashSet<T>(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            result.retainAll(list.get(i));
            if (result.isEmpty())
                return Collections.emptySet();
        }
        if (result.isEmpty())
            return Collections.emptySet();
        return result;
    }

    public static void main(String [] argv)
    {
        // do some setup
        if (argv.length < 1) {
            System.err.println("usage: TestSetFilter [-v] <test set1> [test set2 ...]");
            return;
        }
        testSentences = new ArrayList<String>();
        sentencesByWord = new HashMap<String,Set<Integer>>();
        for (int i = 0; i < argv.length; i++) {
			if (argv[i].equals("-v")) {
				verbose = true;
				continue;
			}
            getTestSentences(argv[i]);
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");
        int rulesIn = 0;
        int rulesOut = 0;
		if (verbose)
			System.err.println("Processing rules...");
        while (scanner.hasNextLine()) {
			if (verbose) {
				if ((rulesIn+1) % 2000 == 0) {
					System.err.print(".");
					System.err.flush();
				}
				if ((rulesIn+1) % 100000 == 0) {
					System.err.println(" [" + (rulesIn+1) + "]");
					System.err.flush();
				}
			}
            rulesIn++;
            String rule = scanner.nextLine();
            if (inTestSet(rule)) {
                System.out.println(rule);
                rulesOut++;
			}
        }
		if (verbose) {
			System.err.println("[INFO] Total rules read: " + rulesIn);
			System.err.println("[INFO] Rules kept: " + rulesOut);
			System.err.println("[INFO] Rules dropped: " + (rulesIn - rulesOut));
		}

        return;
    }
}
