package edu.jhu.thrax.hadoop.features;

public abstract class Feature
{
    private String name;

    public Feature(String theName)
    {
        name = theName;
    }

    public String getName()
    {
        return name;
    }
}

