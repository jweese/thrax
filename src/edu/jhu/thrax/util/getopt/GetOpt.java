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

	private static boolean requiresArgument(String s)
	{
		if (opts.containsKey(s)) {
			return opts.get(s).requiresArgument();
		}
		else {
			return false;
		}
	}

	public static ArrayList<String> parse(String [] argv) throws OptionMissingArgumentException
	{
		ArrayList<String> nonOptionArgs = new ArrayList<String>(argv.length);
		for (int i = 0; i < argv.length; i++) {
			String curr = argv[i];
			if (curr.startsWith("--")) {
				// long-form GNU-style option
				System.err.println(curr.substring(2));
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
