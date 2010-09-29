package edu.jhu.thrax.util;

import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;

public class Intersect
{
    private static HashMap<String,String> rules;
    public static void main(String [] argv) throws Exception
    {
        System.err.print("[INF] Reading smaller grammar ... ");
        getRulesFromFile(argv[0]);
        System.err.println("done.");
        System.err.println("Iterating over larger grammar; printing matches.");

        Scanner scanner;
        if (argv[1].endsWith(".gz"))
            scanner = new Scanner(new File(argv[1]), "UTF-8");
        else
            scanner = new Scanner(new GZIPInputStream(new FileInputStream(new File(argv[1]))), "UTF-8");
        PrintStream firstGrammar = new PrintStream(new FileOutputStream(argv[2] + ".1"));
        PrintStream secondGrammar = new PrintStream(new FileOutputStream(argv[2] + ".2"));
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            String r = repr(s);
            if (rules.containsKey(r)) {
                secondGrammar.println(s);
                firstGrammar.println(rules.get(r));
                rules.remove(r);
            }
        }
        return;
    }

    private static String repr(String s)
    {
        return s.substring(0, s.lastIndexOf("|||"));
    }

    private static void getRulesFromFile(String filename) throws IOException
    {
        rules = new HashMap<String,String>();
        Scanner scanner;
        if (filename.endsWith(".gz")) {
            scanner = new Scanner(new GZIPInputStream(new FileInputStream(new File(filename))), "UTF-8");
        }
        else {
            scanner = new Scanner(new File(filename), "UTF-8");
        }
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            rules.put(repr(s), s);
        }
        return;
    }
}

