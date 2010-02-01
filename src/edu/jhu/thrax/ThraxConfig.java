package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.GetOpt;
import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;

public class ThraxConfig {
	public static final String VERSION_STRING = "0.1/alpha";

	private static void registerAllOptions()
	{
		// necessary GNU-type switches
		GetOpt.registerOption("V", "version", false);
		GetOpt.registerOption("h", "help", false);

		// program operation switches

		GetOpt.registerOption("a", "alignment", true);
		GetOpt.registerOption("A", "alignment-format", true);
		GetOpt.registerOption("c", "config", true);
		GetOpt.registerOption("d", "debug", false);
		GetOpt.registerOption("p", "parse", true);
		GetOpt.registerOption("P", "parse-format", true);
		GetOpt.registerOption("f", "output-format", true);
		GetOpt.registerOption("s", "source", true);
		GetOpt.registerOption("t", "target", true);
		GetOpt.registerOption("v", "verbose", false);
		GetOpt.registerOption("g", "grammar", true);

		// if you want to add more command line options, register
		// them here. Then don't forget to check for them in
		// configure()!

		return;
	}

	public static void configure(String[] argv) throws OptionMissingArgumentException {
		registerAllOptions();
		GetOpt.parse(argv);

		if (GetOpt.isSet("V")) {
			printVersionInfo();
			System.exit(0);
		}
		if (GetOpt.isSet("h")) {
			printHelpMessage();
			System.exit(0);
		}
		return;
	}

	private static void printVersionInfo()
	{
		System.out.println(String.format("Thrax grammar extractor %s", VERSION_STRING));
		System.out.println("Copyright (C) 2010 Jonny Weese <jonny@cs.jhu.edu>");
		System.out.println("MIT License: <http://www.opensource.org/licenses/mit-license.php>");
		System.out.println("This is free software: you are free to change and redistribute it.");
		System.out.println("There is NO WARRANTY, to the extent permitted by law.");
		return;
	}

	private static void printHelpMessage() {
		System.out.println("help message goes here.");
		System.exit(0);
	}

}
