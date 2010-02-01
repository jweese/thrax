package edu.jhu.thrax.inputs;

public interface InputProvider<T> {

	public boolean hasNext();
	public T next();
}

