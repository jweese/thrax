package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.GetOpt;
import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

/**
 * This class reads configuration options from several sources and aggregates
 * them into a single <code>HashMap</code> that can be read from other parts
 * of Thrax. Specifically, it uses the <code>GetOpt</code> class to read
 * command-line options, and also can read options from a configuration file.
 */
public class ThraxConfig {
	/**
	 * Describes the version number of this copy of Thrax.
	 */
	public static final String VERSION_STRING = "0.1/alpha";

	// the options themselves
	/**
	 * Holds all key-value pairs describing the configuration.
	 */
	public static final HashMap<String,String> opts = new HashMap<String,String>();

	/**
	 * Determines the verbosity level. 0 is the normal level, which is
	 * quiet. 1 means verbose, and 2 includes debugging information.
	 */
	public static int verbosity = 0;

	// some defaults
	public static final String DEFAULT_GRAMMAR = "hiero";
	public static final String DEFAULT_ALIGNMENT_FORMAT = "berkeley";
	public static final String DEFAULT_PARSE_FORMAT = "stanford";
	public static final String DEFAULT_OUTPUT_FORMAT = "joshua";

	/**
	 * Prints the current key-value pairs in <code>opts</code> to stderr.
	 * Generally only included in debug output.
	 */
	private static void printConfiguration()
	{
		System.err.println("--- THRAX CONFIGURATION ---");
		for (String k : opts.keySet()) {
			System.err.println(String.format("%s: %s", k, opts.get(k)));
		}
		return;
	}

	/**
	 * Tells <code>GetOpt</code> which command-line flags to look for.
	 * If you add a new command line option that needs to be checked,
	 * it should be included here.
	 */
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
		GetOpt.registerOption("o", "output-format", true);
		GetOpt.registerOption("s", "source", true);
		GetOpt.registerOption("t", "target", true);
		GetOpt.registerOption("v", "verbose", false);
		GetOpt.registerOption("g", "grammar", true);

		// if you want to add more command line options, register
		// them here. Then don't forget to check for them in
		// configure()!

		return;
	}

	/**
	 * Uses <code>GetOpt</code> to parse the command line and populate
	 * the <code>opts</code> HashMap.
	 *
	 * @param argv	the command line as passed to the main function.
	 */
	public static void configure(String[] argv) throws OptionMissingArgumentException, IOException {
		registerAllOptions();
		GetOpt.parse(argv);

		if (GetOpt.isSet("v")) { // verbose
			verbosity = 1;
		}
		if (GetOpt.isSet("d")) { //debug
			verbosity = 2;
		}
		if (GetOpt.isSet("V")) { // version
			printVersionInfo();
			System.exit(0);
		}
		if (GetOpt.isSet("h")) { // help
			printHelpMessage();
			System.exit(0);
		}
		if (GetOpt.isSet("a")) {
			opts.put("alignment", GetOpt.valueOf("a"));
		}
		if (GetOpt.isSet("A")) {
			opts.put("alignment_format", GetOpt.valueOf("A"));
		}
		if (GetOpt.isSet("p")) {
			opts.put("parse", GetOpt.valueOf("p"));
		}
		if (GetOpt.isSet("P")) {
			opts.put("parse_format", GetOpt.valueOf("P"));
		}
		if (GetOpt.isSet("o")) {
			opts.put("output_format", GetOpt.valueOf("o"));
		}
		if (GetOpt.isSet("s")) {
			opts.put("source", GetOpt.valueOf("s"));
		}
		if (GetOpt.isSet("t")) {
			opts.put("target", GetOpt.valueOf("t"));
		}
		if (GetOpt.isSet("g")) {
			opts.put("grammar", GetOpt.valueOf("g"));
		}

		if (GetOpt.isSet("c")) {
			getConfigurationFromFile(GetOpt.valueOf("c"));

		}
		if (verbosity > 0) {
			printConfiguration();
		}
		return;
	}

	/**
	 * Reads a configuration file and uses the key-value pairs there to
	 * populate the <code>opts</code> HashMap.
	 *
	 * @param filename	the name of the configuration file to read
	 * @throws IOException	if an input or output exception occurs
	 */
	private static void getConfigurationFromFile(String filename) throws IOException {
		Scanner scanner = new Scanner(new File(filename));

		if (verbosity > 0) {
			System.err.print(String.format("Reading configuration from %s ... ", filename));
		}

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			// strip comments
			String content = line;
			if (line.indexOf("#") != -1) {
				content = line.substring(0, line.indexOf("#"));
			}
			content = content.trim();
			if (content.equals("")) {
				continue;
			}
			String [] keyVal = content.split("\\s+", 2);
			if (!opts.containsKey(keyVal[0])) {
				opts.put(keyVal[0], keyVal[1]);
			}
		}

		if (verbosity > 0) {
			System.err.println("done.");
		}

		return;
	}

	/**
	 * Prints the version information to stdout. The format of the version
	 * message follows the GNU style guide.
	 */
	private static void printVersionInfo()
	{
		System.out.println(String.format("Thrax grammar extractor %s", VERSION_STRING));
		System.out.println("Copyright (C) 2010 Jonny Weese <jonny@cs.jhu.edu>");
		System.out.println("MIT License: <http://www.opensource.org/licenses/mit-license.php>");
		System.out.println("This is free software: you are free to change and redistribute it.");
		System.out.println("There is NO WARRANTY, to the extent permitted by law.");
		return;
	}

	/**
	 * Prints a help message to stdout.
	 */
	private static void printHelpMessage() {
		System.out.println("help message goes here.");
		return;
	}

}
