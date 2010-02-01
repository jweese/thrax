package edu.jhu.thrax.inputs;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class PlainTextProvider implements InputProvider<int []> {
	private Scanner scanner;

	public PlainTextProvider(String filename) throws IOException {
		scanner = new Scanner(new File(filename));
	}

	public int [] next()
	{
		return null;
	}

	public boolean hasNext()
	{
		return false;
	}
}
