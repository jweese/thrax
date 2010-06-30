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

        if (inp.equals("source")) {
            return new PlainTextProvider(ThraxConfig.SOURCE);

        }
        // if you add a new input type, be sure to allow it to be
        // created in this if/elseif block.
        else if (inp.equals("target")) {
            return new PlainTextProvider(ThraxConfig.TARGET);

        }
        else if (inp.equals("alignment")) {
            String fmt = ThraxConfig.ALIGNMENT_FORMAT.toLowerCase();
            if (fmt.equals(BerkeleyAlignmentProvider.name)) {
                return new BerkeleyAlignmentProvider(ThraxConfig.ALIGNMENT);
            }
            // if you add a new alignment format, put it here.
            else {
                throw new UnknownAlignmentFormatException(fmt);
            }
        }

        else if (inp.equals("parse")) {
            // looks lame, but we can just give the string to Juri's
            // lattice code once we're at the SAMT extractor
            return new StringProvider(ThraxConfig.PARSE);
        }
        System.err.println("WARNING: unknown input type: " + inp);
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
