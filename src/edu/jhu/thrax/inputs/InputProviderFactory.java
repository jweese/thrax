package edu.jhu.thrax.inputs;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.InvalidConfigurationException;
import edu.jhu.thrax.util.MissingGrammarInputTypeException;
import edu.jhu.thrax.util.UnknownAlignmentFormatException;

import java.io.IOException;

/**
 * This class instantiates various types of <code>InputProvider</code> depending
 * on which name is provided. When you create new input providers (whether they
 * provide new types of data, or parse that data from different formats, they
 * should be added to this factory.
 */
public class InputProviderFactory {

	/**
	 * Given the name of a type of input, returns an InputProvider that will
	 * provide that particular type. When you create a new type of
	 * InputProvider, you should be sure that it is checked for in this
	 * method.
	 *
	 * @param inputType the name of the input source
	 * @return an InputProvider that will return input from that source
	 * @throws InvalidConfigurationException if the InputProvider could not
	 * be created because the Thrax configuration was invalid
	 * @throws IOException if an input or output excdeption occurred
	 */
	public static InputProvider create(String inputType) throws InvalidConfigurationException, IOException {

		String inp = inputType.toLowerCase();
		if (!ThraxConfig.opts.containsKey(inp)) {
			throw new MissingGrammarInputTypeException(inp);
		}

		if (inp.equals(ThraxConfig.SOURCE)) {
			return new PlainTextProvider(ThraxConfig.opts.get(inp));

		}
		// if you add a new input type, be sure to allow it to be
		// created in this if/elseif block.
		else if (inp.equals(ThraxConfig.TARGET)) {
			return new PlainTextProvider(ThraxConfig.opts.get(inp));

		}
		else if (inp.equals(ThraxConfig.ALIGNMENT)) {
			String fmt = ThraxConfig.opts.containsKey(ThraxConfig.ALIGNMENT_FORMAT) ? ThraxConfig.opts.get(ThraxConfig.ALIGNMENT_FORMAT) : ThraxConfig.DEFAULT_ALIGNMENT_FORMAT;
			fmt = fmt.toLowerCase();
			if (fmt.equals(BerkeleyAlignmentProvider.name)) {
				return new BerkeleyAlignmentProvider(ThraxConfig.opts.get(inp));
			}
			// if you add a new alignment format, put it here.
			else {
				throw new UnknownAlignmentFormatException(fmt);
			}
		}
		return null;
	}

	/**
	 * Creates appropriate <code>InputProvider</code> objects for all named
	 * inputs in a given array of String. This is provided as a convenience
	 * method when dealing with the requiredInputs of a RuleExtractor.
	 *
	 * @param inps an array of names of inputs
	 * @return an array of InputProviders in the same order as the names
	 * @throws InvalidConfigurationException if an InputProvider could not
	 * be created because the configuration was inappropriate
	 * @throws IOException if an input or output exception occurred
	 */
	public static InputProvider [] createAll(String [] inps) throws InvalidConfigurationException, IOException {
		InputProvider [] ret = new InputProvider[inps.length];
		for (int i = 0; i < inps.length; i++) {
			ret[i] = create(inps[i]);
		}
		return ret;
	}
}
