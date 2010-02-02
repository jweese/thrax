package edu.jhu.thrax.inputs;

/**
 * The common interface for classes that provide input for different types
 * of grammar extractors. The input provider is imagined mainly as a wrapper
 * around a Scanner reading its input from its source, wherever that may be.
 */
public interface InputProvider<T> {

	/**
	 * Determines if this <code>InputProvider</code> has more input to
	 * provide.
	 *
	 * @return true if more input is waiting, false otherwise
	 */
	public boolean hasNext();

	/**
	 * Gets the next piece of input.
	 *
	 * @return the next piece of input
	 */
	public T next();
}

