package edu.jhu.thrax.inputs;

import edu.jhu.thrax.ThraxConfig;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class InputProvider {

    private Scanner [] inputs;
    private Scanner unifiedInput;
    private boolean unified;
    private int lineNumber;


    public InputProvider(String [] reqs) throws IOException
    {
        lineNumber = 0;
        unified = ThraxConfig.UNIFIED_INPUT;
        if (unified)
            unifiedInput = new Scanner(new File(ThraxConfig.INPUT_FILE));
        else {
            inputs = new Scanner[reqs.length];
            for (int i = 0; i < reqs.length; i++) {
                inputs[i] = getScannerByName(reqs[i]);
            }
        }
    }

    public synchronized boolean hasNext()
    {
        if (unified)
            return unifiedInput.hasNextLine();
        else {
            for (Scanner s : inputs) {
                if (!s.hasNextLine())
                    return false;
            }
            return true;
        }
    }

    public synchronized String [] next()
    {
        if (!hasNext())
            return null;
        lineNumber++;
        if (ThraxConfig.verbosity > 0 && lineNumber % 20000 == 0)
            System.err.println(String.format("[line %d]", lineNumber));
        if (unified) {
            String line = unifiedInput.nextLine();
            String [] toks = line.split(ThraxConfig.DELIMITER_REGEX);
            for (String t : toks)
                t = t.trim();
            return toks;
        }
        else {
            String [] result = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++)
                result[i] = inputs[i].nextLine().trim();
            return result;
        }
    }

    private static Scanner getScannerByName(String name) throws IOException
    {
        if ("source".equals(name))
            return new Scanner(new File(ThraxConfig.SOURCE));
        else if ("target".equals(name))
            return new Scanner(new File(ThraxConfig.TARGET));
        else if ("parse".equals(name))
            return new Scanner(new File(ThraxConfig.PARSE));
        else if ("alignment".equals(name))
            return new Scanner(new File(ThraxConfig.ALIGNMENT));
        else
            return null;
    }
}
