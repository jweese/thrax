package edu.jhu.thrax.util;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.util.Scanner;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import java.io.IOException;

import edu.jhu.thrax.hadoop.jobs.FeatureJobFactory;
import edu.jhu.thrax.hadoop.features.SimpleFeature;
import edu.jhu.thrax.hadoop.features.SimpleFeatureFactory;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;

public class CreateGlueGrammar
{
    private static HashSet<String> nts;

    private static TreeMap<Text,Writable> unaryFeatures;
    private static TreeMap<Text,Writable> binaryFeatures;

    // [GOAL] ||| <s> ||| <s> ||| 0
    // [GOAL] ||| [GOAL,1] [X,2] ||| [GOAL,1] [X,2] ||| -1
    // [GOAL] ||| [GOAL,1] </s> ||| [GOAL,1] </s> ||| 0
    private static final String RULE_START = "[%1$s] ||| <s> ||| <s> ||| 0";
    private static final String RULE_TWO = "[%1$s] ||| [%1$s,1] [%2$s,2] ||| [%1$s,1] [%2$s,2] ||| -1";
    private static final String RULE_END = "[%1$s] ||| [%1$s,1] </s> ||| [%1$s,1] </s> ||| 0";
    private static String GOAL = "GOAL";
    private static boolean LABEL = false;
    private static String [] FEATURES;

    public static void main(String [] argv) throws IOException
    {
        if (argv.length < 1) {
            System.err.println("usage: CreateGlueGrammar <conf file>");
            return;
        }

        Map<String,String> opts = ConfFileParser.parse(argv[0]);
        if (opts.containsKey("goal-symbol"))
            GOAL = opts.get("goal-symbol");

        Scanner scanner = new Scanner(System.in, "UTF-8");
        nts = new HashSet<String>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int lhsStart = line.indexOf("[") + 1;
            int lhsEnd = line.indexOf("]");
            if (lhsStart < 1 || lhsEnd < 0) {
                System.err.printf("malformed rule: %s\n", line);
                continue;
            }
            String lhs = line.substring(lhsStart, lhsEnd);
            nts.add(lhs);
        }
        System.out.println(String.format(RULE_START, GOAL));
        for (String nt : nts) {
            Text n = new Text(nt);
            System.out.println(String.format(RULE_TWO, GOAL, n));
        }
        System.out.println(String.format(RULE_END, GOAL));
    }

}
