package edu.jhu.thrax.util.getopt;

/**
 * This exception is thrown when <code>GetOpt</code> expects a command-line
 * option to provide an argument, but the given command line does not provide
 * that argument.
 */
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
