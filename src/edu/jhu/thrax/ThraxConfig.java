package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.GetOpt;
import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;

import edu.jhu.thrax.extraction.*;
import edu.jhu.thrax.inputs.*;

import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
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
	public static final String VERSION_STRING = "0.5/alpha";

        /**
         * Used to separate values in multi-value options.
         */
        public static final String SEPARATOR = ":";

	/**
	 * Determines the verbosity level. 0 is the normal level, which is
	 * quiet. 1 means verbose, and 2 includes debugging information.
	 */
	public static int verbosity = 0;

	// the names possible in opts.keySet()
	// they are defined here to avoid spelling errors
	// everything else like the factories can just refer to these
	// names instead! only one place for things to be defined.
	public static String GRAMMAR = "hiero";
	public static String ALIGNMENT = "";
	public static String ALIGNMENT_FORMAT = "berkeley";
	public static String PARSE = "";
	public static String PARSE_FORMAT = "ptb";
	public static String SOURCE = "";
	public static String TARGET = "";
	public static String OUTPUT_FORMAT = "joshua";

        public static boolean ADJACENT = false;
        public static boolean LOOSE = false;
        public static int INITIAL_PHRASE_LIMIT = 10;
        public static int SOURCE_LENGTH_LIMIT = 5;
        public static int ARITY = 2;
        public static int LEXICALITY = 1;

        public static String FEATURES = "";

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
                GetOpt.registerOption("f", "features", true);

		// if you want to add more command line options, register
		// them here. Then don't forget to check for them in
		// configure()!

                // allow adjacent terminals
                GetOpt.registerOption("j", "adjacent-nts", false);
                // allow loose bounds for phrase extraction
                GetOpt.registerOption("L", "loose-phrases", false);
                // initial phrase length limit
                GetOpt.registerOption("i", "initial-phrase-length", true);
                // source side length limit
                GetOpt.registerOption("S", "rule-source-length", true);
                // arity limit
                GetOpt.registerOption("r", "arity", true);
                // lexical limit
                GetOpt.registerOption("l", "lexicality", true);

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

		if (GetOpt.isSet("c")) {
			getConfigurationFromFile(GetOpt.valueOf("c"));
		}
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
			ALIGNMENT = GetOpt.valueOf("a");
                        if (verbosity > 1)
                            System.err.println("alignment: " + ALIGNMENT);
		}
		if (GetOpt.isSet("A")) {
			ALIGNMENT_FORMAT = GetOpt.valueOf("A");
                        if (verbosity > 1)
                            System.err.println("alignment format: " + ALIGNMENT_FORMAT);
		}
		if (GetOpt.isSet("p")) {
			PARSE = GetOpt.valueOf("p");
                        if (verbosity > 1)
                            System.err.println("parse: " + PARSE);
		}
		if (GetOpt.isSet("P")) {
			PARSE_FORMAT = GetOpt.valueOf("P");
                        if (verbosity > 1)
                            System.err.println("parse format: " + PARSE_FORMAT);
		}
		if (GetOpt.isSet("o")) {
			OUTPUT_FORMAT = GetOpt.valueOf("o");
                        if (verbosity > 1)
                            System.err.println("output format: " + OUTPUT_FORMAT);

		}
		if (GetOpt.isSet("s")) {
			SOURCE = GetOpt.valueOf("s");
                        if (verbosity > 1)
                            System.err.println("source: " + SOURCE);
		}
		if (GetOpt.isSet("t")) {
			TARGET = GetOpt.valueOf("t");
                        if (verbosity > 1)
                            System.err.println("target: " + TARGET);
		}
		if (GetOpt.isSet("g")) {
			GRAMMAR = GetOpt.valueOf("g");
                        if (verbosity > 1)
                            System.err.println("grammar: " + GRAMMAR);
		}
                if (GetOpt.isSet("f")) {
                        FEATURES = GetOpt.valueOf("f");
                        if (verbosity > 1)
                            System.err.println("features: " + FEATURES);
                }
                if (GetOpt.isSet("j")) {
                    if ("".equals(GetOpt.valueOf("j")))
                        ADJACENT = true;
                    else
                        ADJACENT = Boolean.parseBoolean(GetOpt.valueOf("j"));
                    if (verbosity > 1)
                        System.err.println("adjacent NTs: " + ADJACENT);

                }
                if (GetOpt.isSet("L")) {
                    if ("".equals(GetOpt.valueOf("L")))
                        LOOSE = true;
                    else
                        LOOSE = Boolean.parseBoolean(GetOpt.valueOf("L"));
                    if (verbosity > 1)
                        System.err.println("loose phrase bounds: " + LOOSE);
                }
                if (GetOpt.isSet("i")) {
                    INITIAL_PHRASE_LIMIT = Integer.parseInt(GetOpt.valueOf("i"));
                    if (verbosity > 1)
                        System.err.println("initial phrase limit: " + INITIAL_PHRASE_LIMIT);
                }
                if (GetOpt.isSet("S")) {
                    SOURCE_LENGTH_LIMIT = Integer.parseInt(GetOpt.valueOf("S"));
                    if (verbosity > 1)
                        System.err.println("source length limit: " + SOURCE_LENGTH_LIMIT);
                }
                if (GetOpt.isSet("r")) {
                    ARITY = Integer.parseInt(GetOpt.valueOf("r"));
                    if (verbosity > 1)
                        System.err.println("arity: " + ARITY);
                }
                if (GetOpt.isSet("l")) {
                    LEXICALITY = Integer.parseInt(GetOpt.valueOf("l"));
                    if (verbosity > 1)
                        System.err.println("lexicality: " + LEXICALITY);
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
	private static void getConfigurationFromFile(String filename) throws OptionMissingArgumentException, IOException {
		Scanner scanner = new Scanner(new File(filename));
                ArrayList<String> al = new ArrayList<String>();

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
                        al.add(String.format("--%s=%s", keyVal[0], keyVal[1]));
                        String [] theArgv = new String[al.size()];
                        for (int i = 0; i < theArgv.length; i++)
                            theArgv[i] = al.get(i);
                        GetOpt.parse(theArgv);

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
