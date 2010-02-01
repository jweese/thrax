package edu.jhu.thrax.util;

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
