package edu.jhu.thrax.hadoop.features;

public class FeatureFactory
{
    public static Feature get(String name)
    {
        if (name.equals("lex"))
            return new LexicalProbabilityFeature();
        else 
            return null;
    }

    public static Feature [] getAll(String [] names)
    {
        Feature [] result = new Feature[names.length];
        for (int i = 0; i < result.length; i++)
            result[i] = get(names[i]);
        return result;
    }
}
