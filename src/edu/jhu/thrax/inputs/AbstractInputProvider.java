package edu.jhu.thrax.inputs;

import java.util.Scanner;

import java.io.File;
import java.io.IOException;

import edu.jhu.thrax.ThraxConfig;

/**
 * A common implementation of <code>InputProvider</code> based on wrapping
 * a Scanner that can read the input line by line from some source.
 */
public abstract class AbstractInputProvider<T> implements InputProvider<T> {

	protected Scanner scanner;

	/**
	 * Constructor meant to read input from a file.
	 *
	 * @param filename the name of the file to get input from
	 * @throws IOException if an input or output exception occurs
	 */
	protected AbstractInputProvider(String filename) throws IOException
	{
		scanner = new Scanner(new File(filename));
	}

	public boolean hasNext() {
		return scanner.hasNextLine();
	}

	public T next() {
            String line = scanner.nextLine();
            if (ThraxConfig.verbosity > 1)
                System.err.println(String.format("%s: %s", name, line));
            return convert(line);
        }

        abstract public T convert(String line);
}
