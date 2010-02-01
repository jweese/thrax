package edu.jhu.thrax.inputs;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;

import edu.jhu.thrax.datatypes.Alignment;

public class BerkeleyAlignmentProvider implements InputProvider<Alignment> {

	private Scanner scanner;

	public BerkeleyAlignmentProvider(String filename) throws IOException
	{
		scanner = new Scanner(new File(filename));
	}

	public Alignment next()
	{
		return null;
	}

	public boolean hasNext()
	{
		return false;
	}
}
