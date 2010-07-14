package edu.jhu.thrax.util;

/**
 * This exception is thrown whenever the grammar type provided by the user
 * expects a certain type of input (for example, alignments for a parallel
 * corpus) but the source for that input has not been provided by the
 * configuration.
 */
@SuppressWarnings("serial")
public class MissingGrammarInputTypeException extends InvalidConfigurationException {

    private String type;

    public MissingGrammarInputTypeException(String t)
    {
        type = t;
    }

    public String getMessage()
    {
        return String.format("Missing grammar input type: %s", type);
    }
}
