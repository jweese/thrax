package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;
import edu.jhu.thrax.util.InvalidConfigurationException;

import edu.jhu.thrax.inputs.InputProvider;
import edu.jhu.thrax.inputs.InputProviderFactory;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;

import java.io.IOException;

public class Thrax {


	public static void main(String [] argv)
	{
		try {
			ThraxConfig.configure(argv);
			String grammar = ThraxConfig.opts.containsKey("grammar")
			               ? ThraxConfig.opts.get("grammar")
				       : ThraxConfig.DEFAULT_GRAMMAR;
			RuleExtractor extractor = RuleExtractorFactory.create(grammar);

			InputProvider [] inputs = getInputs(extractor.requiredInputs());

		}
		catch (OptionMissingArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (InvalidConfigurationException e) {
			System.err.println(e.getMessage());
		}
		return;
	}

	private static InputProvider [] getInputs(String [] inps) throws InvalidConfigurationException
	{
		InputProvider [] ret = new InputProvider[inps.length];
		for (int i = 0; i < inps.length; i++) {
			ret[i] = InputProviderFactory.create(inps[i]);
		}
		return ret;
	}
}
