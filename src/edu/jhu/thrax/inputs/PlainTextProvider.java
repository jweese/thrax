package edu.jhu.thrax.inputs;

import edu.jhu.thrax.util.Vocabulary;

import java.io.IOException;

public class PlainTextProvider extends AbstractInputProvider<int []> {

	public PlainTextProvider(String filename) throws IOException {
		super(filename);
	}

	public int [] next()
	{
		return Vocabulary.getWordIDs(scanner.nextLine().split("\\s+"));
	}

}
