package edu.jhu.thrax.util;

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
