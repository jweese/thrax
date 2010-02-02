package edu.jhu.thrax.util.getopt;

/**
 * This class represents a command-line option.
 */
public class Option {
	/**
	 * Does this option require an argument?
	 */
	private boolean requiresArgument;

	/**
	 * The argument provided to this option.
	 */
	private String value;

	/**
	 * Whether this option was set by the command line or not.
	 */
	private boolean isSet;

	public Option(boolean requiresArgument)
	// not political commentary, I promise.
	{
		this.requiresArgument = requiresArgument;
		this.value = "";
		this.isSet = false;
	}

	/**
	 * Reports whether this <code>Option</code> requires an argument on the
	 * command line.
	 *
	 * @return <code>true</code> if an argument is required, <code>false
	 * </code> otherwise.
	 */
	public boolean requiresArgument()
	{
		return requiresArgument;
	}

	/**
	 * Returns the argument provided to this option on the command line.
	 *
	 * @return the argument provided on the command line
	 */
	public String value()
	{
		return value;
	}

	/**
	 * Reports whether this option was set on the command line.
	 *
	 * @return true if this option is set, false otherwise
	 */
	public boolean isSet()
	{
		return isSet;
	}

	/**
	 * Stores the fact that this option was set on the command line.
	 */
	public void set()
	{
		isSet = true;
		return;
	}

	/**
	 * Stores the fact that this option was set on the command line, and
	 * also sets its command-line argument to the given string.
	 *
	 * @param val the argument provided to this option
	 */
	public void set(String val)
	{
		isSet = true;
		value = val;
		return;
	}
}
