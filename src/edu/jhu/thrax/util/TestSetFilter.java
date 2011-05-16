package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.FileNotFoundException;
import java.io.File;

import edu.jhu.thrax.ThraxConfig;

public class TestSetFilter
{
    private static HashSet<String> testSentences;

    private static final String NT_REGEX = "\\[[^\\]]+?\\]";

    private static void getTestSentences(String filename)
    {
        try {
            Scanner scanner = new Scanner(new File(filename), "UTF-8");
            while (scanner.hasNextLine()) {
                testSentences.add(scanner.nextLine());
            }
        }
        catch (FileNotFoundException e) {
            System.err.printf("Could not open %s\n", e.getMessage());
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
        if (pattern == null) {
            System.err.printf("malformed rule: %s\n", rule);
            return false;
        }
//        System.err.println("pattern is " + pattern);
        for (String s : testSentences) {
            if (pattern.matcher(s).find()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String [] argv)
    {
        // do some setup
        if (argv.length < 1) {
            System.err.println("usage: TestSetFilter <test set1> [test set2 ...]");
            return;
        }
        testSentences = new HashSet<String>();
        for (int i = 0; i < argv.length; i++) {
            getTestSentences(argv[i]);
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");
//        int rulesIn = 0;
//        int rulesOut = 0;
        while (scanner.hasNextLine()) {
//            rulesIn++;
            String rule = scanner.nextLine();
            if (inTestSet(rule)) {
                System.out.println(rule);
//                rulesOut++;
            }
        }
//        System.err.println("[INFO] Total rules read: " + rulesIn);
//        System.err.println("[INFO] Rules kept: " + rulesOut);
//        System.err.println("[INFO] Rules dropped: " + (rulesIn - rulesOut));
        return;
    }
}
