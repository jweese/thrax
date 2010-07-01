package edu.jhu.thrax.inputs;

public class InputProvider {

    private String [] requiredInputs;

    public InputProvider(String [] reqs)
    {
        requiredInputs = reqs;
    }

    public boolean hasNext()
    {
        return false;
    }

    public String [] next()
    {
        return new String[0];
    }
}
