package edu.jhu.thrax.util.exceptions;

/**
 * This exception is thrown when the "grammar" option provided by the user is
 * not known by Thrax.
 */
@SuppressWarnings("serial")
public class UnknownGrammarTypeException extends Exception {

    private String type;

    public UnknownGrammarTypeException(String t)
    {
        type = t;
    }

    public String getMessage()
    {
        return String.format("Unknown grammar type provided: %s", type);
    }
}
