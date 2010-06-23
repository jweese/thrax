package edu.jhu.thrax.util;

/**
 * This exception is thrown when a feature provided by the user is not known
 * by Thrax.
 */
public class UnknownFeatureException extends InvalidConfigurationException {

	private String type;

	public UnknownFeatureException(String t)
	{
		type = t;
	}

	public String getMessage()
	{
		return String.format("Unknown feature function provided: %s", type);
	}
}
