package edu.jhu.thrax.util;

/**
 * This exception is thrown when the "alignment-format" option provided by the
 * user is not known by Thrax.
 */
@SuppressWarnings("serial")
public class UnknownAlignmentFormatException extends InvalidConfigurationException {
    private String format;

    public UnknownAlignmentFormatException(String fmt)
    {
        format = fmt;
    }

    public String getMessage()
    {
        return String.format("Unknown alignment format: %s", format);
    }
}
