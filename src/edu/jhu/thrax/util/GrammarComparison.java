package edu.jhu.thrax.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;


public class GrammarComparison {

    private static final String SEPARATOR = "|||";
    private static final String USAGE = "usage: GrammarComparison <grammar> <grammar> <prefix for output files>";

    public static void main(String [] argv)
    {
        if (argv.length < 3) {
            System.err.println(USAGE);
            return;
        }

        String file1 = argv[0];
        String file2 = argv[1];
        String outputBase = argv[2];

        try {
            HashSet<String> grammar1 = getRulesFromFile(file1);
            HashSet<String> grammar2 = getRulesFromFile(file2);

            Set<String> smaller = grammar1.size() < grammar2.size()
                ? grammar1
                : grammar2;
            Set<String> larger = smaller == grammar1 ? grammar2 : grammar1;

            Set<String> intersection = new HashSet<String>();
            for (String s : smaller) {
                if (larger.contains(s))
                    intersection.add(s);
            }
            Set<String> only1 = (HashSet<String>) grammar1.clone();
            only1.removeAll(grammar2);
            Set<String> only2 = (HashSet<String>) grammar2.clone();
            only2.removeAll(grammar1);

            printRules(only1, outputBase + ".1");
            printRules(only2, outputBase + ".2");
            printRules(intersection, outputBase + ".both");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    private static void printRules(Set<String> rules, String filename) throws FileNotFoundException, SecurityException {
        PrintStream ps = new PrintStream(new FileOutputStream(filename));
        for (String s : rules)
            ps.println(s);
        ps.close();
        return;
    }

    private static HashSet<String> getRulesFromFile(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));

        HashSet<String> ret = new HashSet<String>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String rule = line.substring(0, line.lastIndexOf(SEPARATOR));
            ret.add(rule);
        }
        return ret;
    }
}
