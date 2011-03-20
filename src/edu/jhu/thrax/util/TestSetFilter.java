package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.HashSet;

import java.io.IOException;
import java.io.File;

import edu.jhu.thrax.ThraxConfig;

public class TestSetFilter
{
    private static HashSet<String> testPhrases;

    private static final String NT_REGEX = "\\[[^\\]]+?\\]";

    private static void getTestPhrases(int phraseLength, String filename) throws IOException
    {
        if (testPhrases == null) {
            testPhrases = new HashSet<String>();
        }
        Scanner scanner = new Scanner(new File(filename));
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()) {
            String [] words = scanner.nextLine().trim().split("\\s+");
            for (int len = 1; len <= phraseLength; len++) {
                for (int start = 0; start <= words.length - len; start++) {
                    sb.setLength(0);
                    for (int i = 0; i < len; i++) {
                        sb.append(words[start + i]);
                        if (i < len - 1)
                            sb.append(" ");
                    }
                    testPhrases.add(sb.toString());
                }
            }
        }
    }

    private static boolean inTestSet(String rule)
    {
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        if (parts.length != 4) {
            System.err.printf("malformed rule: %s\n", rule);
            return false;
        }
        String source = parts[1];
        String [] phrases = source.split(NT_REGEX);
        for (String p : phrases) {
            if ("".equals(p.trim())) continue;
            if (!testPhrases.contains(p.trim())) {
                return false;
            }
        }
        return true;
    }

    public static void main(String [] argv) throws IOException
    {
        // do some setup
        if (argv.length < 2) {
            System.err.println("usage: TestSetFilter [-p] <phrase length> <test set1> [test set2 ...]");
            return;
        }
        int phraseLength = Integer.parseInt(argv[0]);
		int testStartIndex = 1;
		if (argv[1].equals("-p"))
			testStartIndex = 2;

		boolean doFlush = (testStartIndex == 2) ? true : false;

        for (int i = testStartIndex; i < argv.length; i++) {
            getTestPhrases(phraseLength, argv[i]);
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");
        int rulesIn = 0;
        int rulesOut = 0;
        while (scanner.hasNextLine()) {
            rulesIn++;
            String rule = scanner.nextLine();
            if (inTestSet(rule)) {
                System.out.println(rule);
				if (doFlush)
					System.out.flush();
                rulesOut++;
            } else if (doFlush) {
				System.out.println();
				System.out.flush();
			}
        }
        System.err.println("[INFO] Total rules read: " + rulesIn);
        System.err.println("[INFO] Rules kept: " + rulesOut);
        System.err.println("[INFO] Rules dropped: " + (rulesIn - rulesOut));
        return;
    }
}
