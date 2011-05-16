package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.HashSet;

import java.io.IOException;
import java.io.File;

import edu.jhu.thrax.ThraxConfig;

public class TestSetFilter
{
    private static HashSet<String> testSentences;

    private static final String NT_REGEX = "\\[[^\\]]+?\\]";

    private static void getTestSentences(String filename) throws IOException
    {
        if (testSentences == null) {
            testSentences = new HashSet<String>();
        }
        Scanner scanner = new Scanner(new File(filename), "UTF-8");
        while (scanner.hasNextLine()) {
            testSentences.add(scanner.nextLine());
        }
    }

    public static String getPattern(String rule)
    {
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        if (parts.length != 4) {
            return "NOT-A-RULE";
        }
        String source = parts[1].trim();
        String pattern = source.replaceAll(NT_REGEX, "\\\\E.+\\\\Q");
        pattern = "(.+ )?\\Q" + pattern + "\\E( .+)?";
        return pattern;
    }

    private static boolean inTestSet(String rule)
    {
        String pattern = getPattern(rule);
        if (pattern.equals("NOT-A-RULE")) {
            System.err.printf("malformed rule: %s\n", rule);
            return false;
        }
//        System.err.println("pattern is " + pattern);
        for (String s : testSentences) {
            if (s.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String [] argv) throws IOException
    {
        // do some setup
        if (argv.length < 1) {
            System.err.println("usage: TestSetFilter <test set1> [test set2 ...]");
            return;
        }
        for (int i = 0; i < argv.length; i++) {
            getTestSentences(argv[i]);
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");
        int rulesIn = 0;
        int rulesOut = 0;
        while (scanner.hasNextLine()) {
            rulesIn++;
            String rule = scanner.nextLine();
            if (inTestSet(rule)) {
                System.out.println(rule);
                rulesOut++;
            }
        }
        System.err.println("[INFO] Total rules read: " + rulesIn);
        System.err.println("[INFO] Rules kept: " + rulesOut);
        System.err.println("[INFO] Rules dropped: " + (rulesIn - rulesOut));
        return;
    }
}
