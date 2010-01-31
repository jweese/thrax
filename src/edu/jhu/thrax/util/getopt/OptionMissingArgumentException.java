package edu.jhu.thrax.util.getopt;

public class OptionMissingArgumentException extends Exception {
	private String option;

	public OptionMissingArgumentException(String opt)
	{
		super();
		this.option = opt;
	}

	public String getMessage()
	{
		return String.format("command-line option \'%s\' requires an argument", option);
	}
}
