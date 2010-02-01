package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.GetOpt;
import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class ThraxConfig {
	public static final String VERSION_STRING = "0.1/alpha";

	// the options themselves
	public static String alignment;
	public static String alignment_format;
	public static String parse;
	public static String parse_format;
	public static String output_format;
	public static String source;
	public static String target;
	public static String grammar;

	public static int verbosity = 0;

	private static void printConfiguration()
	{
		System.err.println("--- THRAX CONFIGURATION ---");
		System.err.println(String.format("grammar style: %s", grammar));
		System.err.println(String.format("source-side file: %s", source));
		System.err.println(String.format("target-side file: %s", target));
		System.err.println(String.format("alignment file: %s", alignment));
		System.err.println(String.format("alignment format: %s", alignment_format));
		System.err.println(String.format("parse file: %s", parse));
		System.err.println(String.format("parse format: %s", parse_format));
		System.err.println(String.format("output format: %s", output_format));
		return;
	}

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
			alignment = GetOpt.valueOf("a");
		}
		if (GetOpt.isSet("A")) {
			alignment_format = GetOpt.valueOf("A");
		}
		if (GetOpt.isSet("p")) {
			parse = GetOpt.valueOf("p");
		}
		if (GetOpt.isSet("P")) {
			parse_format = GetOpt.valueOf("P");
		}
		if (GetOpt.isSet("o")) {
			output_format = GetOpt.valueOf("o");
		}
		if (GetOpt.isSet("s")) {
			source = GetOpt.valueOf("s");
		}
		if (GetOpt.isSet("t")) {
			target = GetOpt.valueOf("t");
		}
		if (GetOpt.isSet("g")) {
			grammar = GetOpt.valueOf("g");
		}

		if (GetOpt.isSet("c")) {
			getConfigurationFromFile(GetOpt.valueOf("c"));

		}
		if (verbosity > 0) {
			printConfiguration();
		}
		return;
	}

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
			String [] opt = content.split("\\s+", 2);
		}

		if (verbosity > 0) {
			System.err.println("done.");
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
