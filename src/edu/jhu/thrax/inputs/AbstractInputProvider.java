package edu.jhu.thrax.inputs;

import java.util.Scanner;

import java.io.File;
import java.io.IOException;

public abstract class AbstractInputProvider<T> implements InputProvider<T> {

	protected Scanner scanner;

	protected AbstractInputProvider(String filename) throws IOException
	{
		scanner = new Scanner(new File(filename));
	}

	public boolean hasNext() {
		return scanner.hasNextLine();
	}

	abstract public T next();
}
