package edu.jhu.thrax.util.getopt;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class reads a command line (an array of String) and stores a description
 * of the flags given on that command-line. This description can be queried to
 * determine which flags the user passed and whether those options were 
 * accompanied by arguments or not.
 */
public class GetOpt {
	/**
	 * Holds the <code>Option</code> objects.
	 */
	private static HashMap<String,Option> opts = new HashMap<String,Option>();

	/**
	 * Tells GetOpt about a command-line option that it should be looking
	 * for. This method should be called for each option of interest before
	 * calling <code>parse</code> to read the command line.
	 *
	 * @param shortForm a one-character String short name for this flag
	 * @param longForm a longer name for the flag
	 * @param requiresArgument if the option requires an argument to be
	 * given
	 */
	public static void registerOption(String shortForm, String longForm, boolean requiresArgument)
	{
		if (opts.containsKey(shortForm) || opts.containsKey(longForm)) {
			return;
		}
		Option o = new Option(requiresArgument);
		opts.put(shortForm, o);
		opts.put(longForm, o);
	}

	/**
	 * Determines if a given option was set on the command line. This method
	 * should only be called after calling <code>parse</code>
	 *
	 * @param s the short or long form of the option name
	 */
	public static boolean isSet(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).isSet();
		}
		else {
			return false;
		}
	}

	/**
	 * Looks up the argument provided to a given option on the command line.
	 * This method should only be called after parse.
	 *
	 * @param s the short or long form of the option name
	 * @return the argument provided to that option
	 */
	public static String valueOf(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).value();
		}
		else {
			return "";
		}
	}

	/**
	 * Save the fact that an option was seen on the command line.
	 *
	 * @param s the short or long form of the option name
	 */
	private static void set(String s)
	{
		if (opts.containsKey(s)) {
			opts.get(s).set();
		}
		return;
	}

	/**
	 * Save the fact that an option was seen on the command line, and save
	 * the argument provided with it.
	 *
	 * @param s the short or long form of the option name
	 * @param val the argument provided to the option
	 */
	private static void set(String s, String val)
	{
		if (opts.containsKey(s)) {
			opts.get(s).set(val);
		}
		return;
	}

	/**
	 * Determine if a particular option requires an argument to be
	 * provided on the command line.
	 *
	 * @param s the short or long name of the option
	 * @return true if the option requires an argument, false otherwise
	 */
	private static boolean requiresArgument(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).requiresArgument();
		}
		else {
			return false;
		}
	}

	/**
	 * Read a command line and determine all option-related information
	 * contained in it. Determines which registered options were set on the
	 * command line, and stores their arguments if arguments were provided.
	 * After calling this function, the option information may be read by
	 * using the <code>isSet</code> and <code>valueOf</code> methods with
	 * option names of interest.
	 *
	 * @param argv the command line
	 * @return an <code>ArrayList</code> holding all Strings in argv that
	 * are not option names or option arguments
	 */
	public static ArrayList<String> parse(String [] argv) throws OptionMissingArgumentException
	{
		ArrayList<String> nonOptionArgs = new ArrayList<String>(argv.length);
		for (int i = 0; i < argv.length; i++) {
			String curr = argv[i];
			if (curr.startsWith("--")) {
				// long-form GNU-style option
				i = handleLongFormOption(argv, i);
			}
			else if (curr.startsWith("-")) {
				// short form
				i = handleShortFormOption(argv, i);
			}
			else {
				nonOptionArgs.add(curr);
			}
		}
		return nonOptionArgs;
	}

	/**
	 * Extracts information from GNU-style long form options. These options
	 * start with "--" followed by some word. Arguments are attached to them
	 * either by seperating the argument from the option with "=" or letting
	 * the argument be the next String on the command line.
	 *
	 * @param argv the command line
	 * @param i the index into <code>argv</code> where the long form option
	 * is
	 * @return the index into <code>argv</code> of the most-recently-read
	 * String
	 * @throws OptionMissingArgumentException if the option requires an
	 * argument but it was not provided
	 */
	private static int handleLongFormOption(String [] argv, int i) throws OptionMissingArgumentException
	{
		int eq = argv[i].indexOf("=");
		if (eq == -1) {
			String name = argv[i].substring(2);
			if (requiresArgument(name)) {
				if ((i == argv.length - 1) || argv[i+1].startsWith("-")) {
					throw new OptionMissingArgumentException(name);
				}
				set(name, argv[i+1]);
				return i + 1;
			}
			else {
				set(name);
				return i;
			}
		}
		else {
			String name = argv[i].substring(2, eq);
			String val = argv[i].substring(eq+1);
			set(name, val);
			return i;
		}
	}

	/**
	 * Extracts information from short-form options (that start with "-").
	 * Several short form arguments that do not require arguments may be 
	 * grouped together. For example, "-abc" sets "a" "b" and "c" as long
	 * as none of them require an argument. If an option does require an
	 * argument, it may be given either as a continuation of the same flag
	 * (for example, "-xfoo" assigns "foo" to "x" as long as "x" requires
	 * an argument) or as the next String on the command line.
	 *
	 * @param argv the command line
	 * @param i the index into the command line where the short form option
	 * is
	 * @return the index into the command line of the most-recently-read
	 * option
	 * @throws OptionMissingArgumentException if an option that requires an
	 * argument was not provided with one
	 */
	private static int handleShortFormOption(String [] argv, int i) throws OptionMissingArgumentException
	{
		String curr = argv[i];
		String flag = curr.substring(1, 2);
		if (curr.length() > 2) {
			if (requiresArgument(flag)) {
				set(flag, curr.substring(2));
				return i;
			}
			set(flag);
			for (int j = 2; j < curr.length(); j++) {
				String flag2 = curr.substring(j, j+1);
				if (requiresArgument(flag2)) {
					throw new OptionMissingArgumentException(flag2);
				}
				else {
					set(flag2);
				}
			}
			return i;

		}
		else {
			if (requiresArgument(flag)) {
				if ((i == argv.length - 1) || argv[i+1].startsWith("-")) {
					throw new OptionMissingArgumentException(flag);
				}
				set(flag, argv[i+1]);
				return i+1;
			}
			set(flag);
			return i;
		}
	}
}
