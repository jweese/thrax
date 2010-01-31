package edu.jhu.thrax.util.getopt;

import java.util.HashMap;
import java.util.ArrayList;

public class GetOpt {
/*
	private static HashMap<Char,Option> shortFormHash = new HashMap<Char,Option>();
	private static HashMap<String,Option> longFormHash = new HashMap<String,Option>();

	public static void registerOption(char shortForm, String longForm, boolean requiresArgument)
	{
		if (shortFormHash.hasKey(shortForm) || longFormHash.hasKey(longForm)) {
			return;
		}
		Option o = new Option(requiresArgument);
		shortFormHash.put(shortForm, o);
		longFormHash.put(longForm, o);
	}

	public static boolean isSet(char c)
	{
		if (shortFormHash.hasKey(c)) {
			return shortFormHash.get(c).isSet();
		}
		else {
			return false;
		}
	}

	public static boolean isSet(String s)
	{
		if (longFormHash.hasKey(s)) {
			return longFormHash.get(s).isSet();
		}
		else {
			return false;
		}
	}

	public static String valueOf(char c)
	{
		if (shortFormHash.hasKey(c)) {
			return shortFormHash.get(c).value();
		}
		else {
			// think of default return value
			return "";
		}
	}

	public static String valueOf(String s)
	{
		if (longFormHash.hasKey(s)) {
			return longFormHash.get(s).value();
		}
		else {
			return "";
		}
	}

	private static void set(char c)
	{
		if (shortFormHash.hasKey(c)) {
			shortFormHash.get(c).set();
		}
		return;
	}

	private static void set(String s)
	{
		if (longFormHash.hasKey(s)) {
			longFormHash.get(s).set();
		}
		return;
	}

	private static void set(char c, String val)
	{
		if (shortFormHash.hasKey(c)) {
			shortFormHash.get(c).set(val);
		}
		return;
	}

	private static void set(String s, String val)
	{
		if (longFormHash.hasKey(s)) {
			longFormHash.get(s).set(val);
		}
		return;
	}

	public static String [] parse(String [] argv)
	{
		ArrayList<String> nonOptionArgs = new ArrayList<String>(argv.length);
		for (int i = 0; i < argv.length; i++) {
			String curr = argv[i];
			if (curr.startsWith("--")) {
				// long-form GNU-style option
				int eq = curr.indexOf("=");
				if (eq == -1) {
					if (argv[i+1].startsWith("-")) {
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
*/
}
