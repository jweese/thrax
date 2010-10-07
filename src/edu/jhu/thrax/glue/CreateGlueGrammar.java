package edu.jhu.thrax.glue;

public class CreateGlueGrammar
{
    private static String GOAL;
    private static String grammarFile;
    private static String [] features;

    public static void main(String [] argv)
    {
        GOAL = argv[1];
        features = new String[argv.length - 2];
        System.arraycopy(argv, 2, features, 0, features.length);
    }

    private static String singleRule(String NT)
    {
        return String.format("[%s] ||| [%s,1] ||| [%s,1] |||", GOAL, NT, NT);
    }

    private static String glueRule(String NT)
    {
        return String.format("[%s] ||| [%s,1] [%s,2] ||| [%s,1] [%s,2] |||", GOAL, GOAL, NT, GOAL, NT);
    }
}

