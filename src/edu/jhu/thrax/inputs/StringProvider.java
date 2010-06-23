package edu.jhu.thrax.inputs;

import edu.jhu.thrax.util.Vocabulary;

import java.io.IOException;

/**
 * This class simply reads its input one line at a time and returns the
 * resulting String each time.
 */
public class StringProvider extends AbstractInputProvider<String> {
	public static String name = "string";

	public StringProvider(String filename) throws IOException {
		super(filename);
	}

	public String convert(String line)
	{
		return line;
	}

}
