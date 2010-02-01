package edu.jhu.thrax.inputs;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.InvalidConfigurationException;
import edu.jhu.thrax.util.MissingGrammarInputTypeException;
import edu.jhu.thrax.util.UnknownAlignmentFormatException;

import java.io.IOException;

public class InputProviderFactory {

	public static InputProvider create(String inputType) throws InvalidConfigurationException, IOException {

		String inp = inputType.toLowerCase();
		if (!ThraxConfig.opts.containsKey(inp)) {
			throw new MissingGrammarInputTypeException(inp);
		}

		if (inp.equals("source")) {
			return new PlainTextProvider(ThraxConfig.opts.get(inp));

		}
		// if you add a new input type, be sure to allow it to be
		// created in this if/elseif block.
		else if (inp.equals("target")) {
			return new PlainTextProvider(ThraxConfig.opts.get(inp));

		}
		else if (inp.equals("alignment")) {
			String fmt = ThraxConfig.opts.containsKey("alignment-format") ? ThraxConfig.opts.get("alignment-format") : ThraxConfig.DEFAULT_ALIGNMENT_FORMAT;
			fmt = fmt.toLowerCase();
			if (fmt.equals("berkeley")) {
				return new BerkeleyAlignmentProvider(ThraxConfig.opts.get(inp));
			}
			// if you add a new alignment format, put it here.
			else {
				throw new UnknownAlignmentFormatException(fmt);
			}
		}
		return null;
	}
}
