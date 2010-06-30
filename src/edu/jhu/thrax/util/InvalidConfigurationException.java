package edu.jhu.thrax.util;

/**
 * This exception is thrown whenever the configuration provided by the user
 * cannot be used to extract a grammar.
 */
public abstract class InvalidConfigurationException extends Exception {

    public abstract String getMessage();

}
