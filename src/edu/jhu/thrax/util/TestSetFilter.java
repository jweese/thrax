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

import java.io.FileNotFoundException;
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
        return false;
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
		System.err.println("Processing rules...");
        while (scanner.hasNextLine()) {
			if (verbose) {
				if ((rulesIn+1) % 2000 == 0) {
					System.err.print(".");
					System.err.flush();
				}
				if ((rulesIn+1) % 100000 == 0) {
					System.err.println(" [" + rulesIn + "]");
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
