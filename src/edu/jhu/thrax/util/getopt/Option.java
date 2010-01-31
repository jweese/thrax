package edu.jhu.thrax.util.getopt;

public class Option {
	private boolean requiresArgument;
	private String value;
	private boolean isSet;

	// not political commentary, I promise.
	public Option(boolean requiresArgument)
	{
		this.requiresArgument = requiresArgument;
		this.value = "";
		this.isSet = false;
	}

	public boolean requiresArgument()
	{
		return requiresArgument;
	}

	public String value()
	{
		return value;
	}

	public boolean isSet()
	{
		return isSet;
	}

	public void set()
	{
		isSet = true;
		return;
	}

	public void set(String val)
	{
		isSet = true;
		value = val;
		return;
	}
}
