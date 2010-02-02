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
			String grammar = ThraxConfig.opts.containsKey(ThraxConfig.GRAMMAR) ? ThraxConfig.opts.get(ThraxConfig.GRAMMAR) : ThraxConfig.DEFAULT_GRAMMAR;
			RuleExtractor extractor = RuleExtractorFactory.create(grammar);

			InputProvider [] inputs = InputProviderFactory.createAll(extractor.requiredInputs);
			Object [] currInputs = new Object[inputs.length];
			boolean haveInput = true;
			while (haveInput) {
				for (int i = 0; i < inputs.length; i++) {
					if (!inputs[i].hasNext()) {
						haveInput = false;
						break;
					}
					currInputs[i] = inputs[i].next();
				}
				if (haveInput) {
					ArrayList<Rule> rules = extractor.extract(currInputs);
				}
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

}
