package edu.jhu.thrax.inputs;

import java.io.IOException;

import edu.jhu.thrax.datatypes.Alignment;

/**
 * This class is an InputProvider for the Alignment type. It reads in an
 * alignment map that the same format as output from the Berkeley Aligner.
 * It converts this alignment map to an n-by-2 array of int and instantiates
 * that array as an Alignment object, then provides the Alignment as the input
 * for a rule extractor.
 */
public class BerkeleyAlignmentProvider extends AbstractInputProvider<Alignment>
{
	public static String name = "berkeley";

	public BerkeleyAlignmentProvider(String filename) throws IOException
	{
		super(filename);
	}

	public Alignment next()
	{
		return new Alignment(scanner.nextLine());
	}

}
