package edu.jhu.thrax.util;

public class UnknownGrammarTypeException extends InvalidConfigurationException {

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
