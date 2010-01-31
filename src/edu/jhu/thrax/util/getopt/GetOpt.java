package edu.jhu.thrax.util.getopt;

import java.util.HashMap;
import java.util.ArrayList;

public class GetOpt {
	private static HashMap<String,Option> opts = new HashMap<String,Option>();

	public static void registerOption(String shortForm, String longForm, boolean requiresArgument)
	{
		if (opts.containsKey(shortForm) || opts.containsKey(longForm)) {
			return;
		}
		Option o = new Option(requiresArgument);
		opts.put(shortForm, o);
		opts.put(longForm, o);
	}

	public static boolean isSet(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).isSet();
		}
		else {
			return false;
		}
	}

	public static String valueOf(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).value();
		}
		else {
			return "";
		}
	}

	private static void set(String s)
	{
		if (opts.containsKey(s)) {
			opts.get(s).set();
		}
		return;
	}

	private static void set(String s, String val)
	{
		if (opts.containsKey(s)) {
			opts.get(s).set(val);
		}
		return;
	}

	public static String [] parse(String [] argv) throws OptionMissingArgumentException
	{
		ArrayList<String> nonOptionArgs = new ArrayList<String>(argv.length);
		for (int i = 0; i < argv.length; i++) {
			String curr = argv[i];
			if (curr.startsWith("--")) {
				// long-form GNU-style option
				int eq = curr.indexOf("=");
				if ((eq == -1) && (opts.get(curr.substring(2)).requiresArgument())) {
					if ((i == argv.length - 1) || argv[i+1].startsWith("-")) {
						throw new OptionMissingArgumentException(curr);
					}
					set(curr.substring(2), argv[i+1]);
					i++;
				}
				else {
					set(curr.substring(2, eq-1), curr.substring(eq+1));
				}
			}
			else if (curr.startsWith("-")) {
				// short form
			}
			else {
				nonOptionArgs.add(curr);
			}
		}
		return (String []) nonOptionArgs.toArray();
	}
}
