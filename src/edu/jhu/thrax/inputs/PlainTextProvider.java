package edu.jhu.thrax.inputs;

import edu.jhu.thrax.util.Vocabulary;

import java.io.IOException;

/**
 * This class reads plain text (presumably the source or target side of a
 * parallel corpus) and provides it line-by-line after each line has been
 * converted from a String to an array of int.
 */
public class PlainTextProvider extends AbstractInputProvider<int []> {

	public PlainTextProvider(String filename) throws IOException {
		super(filename);
	}

	public int [] next()
	{
		return Vocabulary.getWordIDs(scanner.nextLine().split("\\s+"));
	}

}
