package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.HashSet;
import java.util.ArrayList;

import edu.jhu.thrax.ThraxConfig;

public class CreateGlueGrammar
{
    private static HashSet<String> nts;
    private static ArrayList<String> features;

    private static String phrasePenalty;

    private static final String RULE_ONE = "[%1$s] ||| [%2$s,1] ||| [%2$s,1] ||| ";
    private static final String RULE_TWO = "[%1$s] ||| [%1$s,1] [%2$s,2] ||| [%1$s,1] [%2$s,2] ||| ";

    private static void getFeatureOrder(String rule)
    {
        features = new ArrayList<String>();
        String [] parts = rule.split(ThraxConfig.DELIMITER_REGEX);
        String [] feats = parts[3].trim().split("\\s+");
        for (String f : feats) {
            String name = f.substring(0, f.indexOf("="));
            features.add(name);
            if ("PhrasePenalty".equals(name))
                phrasePenalty = f.substring(f.indexOf("=") + 1);
        }
        return;
    }

    public static void main(String [] argv)
    {
        if (argv.length < 1) {
            System.err.println("usage: CreateGlueGrammar <goal symbol>");
            return;
        }

        Scanner scanner = new Scanner(System.in, "UTF-8");
        nts = new HashSet<String>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (features == null)
                getFeatureOrder(line);
            String lhs = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
            nts.add(lhs);
        }
        for (String n : nts) {
            StringBuilder r1 = new StringBuilder();
            r1.append(String.format(RULE_ONE, argv[0], n));
            StringBuilder r2 = new StringBuilder();
            r2.append(String.format(RULE_TWO, argv[0], n));

            for (String f : features) {
                r1.append(String.format("%s=%s ", f, getValueOne(f, n)));
                r2.append(String.format("%s=%s ", f, getValueTwo(f, n)));
            }
            System.out.println(r1.toString());
            System.out.println(r2.toString());
        }
    }

    private static String getValueOne(String feat, String nt)
    {
        if ("SourceTerminalsButNoTarget".equals(feat)) {
            return "0";
        }
        else if ("SourcePhraseGivenTarget".equals(feat)) {
            return "0.0";
        }
        else if ("Monotonic".equals(feat)) {
            return "0";
        }
        else if ("PhrasePenalty".equals(feat)) {
            return phrasePenalty;
        }
        else if ("TargetTerminalsButNoSource".equals(feat)) {
            return "0";
        }
        else if ("LexprobTargetGivenSource".equals(feat)) {
            return "0.0";
        }
        else if ("ContainsX".equals(feat)) {
            return "0";
        }
        else if ("Adjacent".equals(feat)) {
            return "0";
        }
        else if ("Abstract".equals(feat)) {
            return "1";
        }
        else if ("Lexical".equals(feat)) {
            return "0";
        }
        else if ("LexprobSourceGivenTarget".equals(feat)) {
            return "0.0";
        }
        else if ("TargetPhraseGivenSource".equals(feat)) {
            return "0.0";
        }
        return "0";
    }

    private static String getValueTwo(String feat, String nt)
    {
        if ("SourceTerminalsButNoTarget".equals(feat)) {
            return "0";
        }
        else if ("SourcePhraseGivenTarget".equals(feat)) {
            return "0.0";
        }
        else if ("Monotonic".equals(feat)) {
            return "0";
        }
        else if ("PhrasePenalty".equals(feat)) {
            return phrasePenalty;
        }
        else if ("TargetTerminalsButNoSource".equals(feat)) {
            return "0";
        }
        else if ("LexprobTargetGivenSource".equals(feat)) {
            return "0.0";
        }
        else if ("ContainsX".equals(feat)) {
            return "0";
        }
        else if ("Adjacent".equals(feat)) {
            return "1";
        }
        else if ("Abstract".equals(feat)) {
            return "1";
        }
        else if ("Lexical".equals(feat)) {
            return "0";
        }
        else if ("LexprobSourceGivenTarget".equals(feat)) {
            return "0.0";
        }
        else if ("TargetPhraseGivenSource".equals(feat)) {
            return "0.0";
        }
        return "0";
    }
}

