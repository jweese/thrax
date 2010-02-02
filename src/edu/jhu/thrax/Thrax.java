package edu.jhu.thrax;

import edu.jhu.thrax.util.getopt.OptionMissingArgumentException;
import edu.jhu.thrax.util.InvalidConfigurationException;

import edu.jhu.thrax.inputs.InputProvider;
import edu.jhu.thrax.inputs.InputProviderFactory;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;

import edu.jhu.thrax.datatypes.Rule;

import java.io.IOException;
import java.util.ArrayList;

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
			Object [] currInputs = new Object[inputs.length];
			while (allHaveNext(inputs)) {
				for (int i = 0; i < inputs.length; i++)
					currInputs[i] = inputs[i].next();
				ArrayList<Rule> rules = extractor.extract(currInputs);
			}

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
			System.exit(1);
		}
		return;
	}

	private static InputProvider [] getInputs(String [] inps) throws InvalidConfigurationException, IOException
	{
		InputProvider [] ret = new InputProvider[inps.length];
		for (int i = 0; i < inps.length; i++) {
			ret[i] = InputProviderFactory.create(inps[i]);
		}
		return ret;
	}

	private static boolean allHaveNext(InputProvider [] inps)
	{
		for (InputProvider i : inps) {
			if (!i.hasNext()) {
				return false;
			}
		}
		return true;
	}
}
