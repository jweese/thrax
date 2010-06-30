package edu.jhu.thrax;

import edu.jhu.thrax.util.InvalidConfigurationException;

import edu.jhu.thrax.inputs.InputProvider;
import edu.jhu.thrax.inputs.InputProviderFactory;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.features.Feature;
import edu.jhu.thrax.features.FeatureFactory;

import edu.jhu.thrax.datatypes.Rule;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class Thrax {

	public static void main(String [] argv)
	{
		try {
			ThraxConfig.configure(argv[0]);
			RuleExtractor extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);

			InputProvider [] inputs = InputProviderFactory.createAll(extractor.requiredInputs());
                        Feature [] features = new Feature[0];
                        int featureLength = 0;

                        if (!"".equals(ThraxConfig.FEATURES)) {
                            String [] feats = ThraxConfig.FEATURES.split("\\s+");
                            features = FeatureFactory.createAll(feats);
                            for (Feature f : features)
                                featureLength += f.length();
                        }
                        else {
                            System.err.println("WARNING: no feature functions provided");
                        }

                        Set<Rule> rules = new HashSet<Rule>();
			Object [] currInputs = new Object[inputs.length];
			boolean haveInput = true;
                        int lineNumber = 0;
			while (haveInput) {
                                if (ThraxConfig.verbosity > 0) {
                                    lineNumber++;
                                    if (lineNumber % 10000 == 0)
                                        System.err.println(String.format("[line %d]", lineNumber));
                                }
				for (int i = 0; i < inputs.length; i++) {
					if (!inputs[i].hasNext()) {
						haveInput = false;
						break;
					}
					currInputs[i] = inputs[i].next();
				}
				if (haveInput) {
                                    for (Rule r : extractor.extract(currInputs)) {
                                        rules.add(r);
                                        for (Feature f : features)
                                            f.noteExtraction(r);
                                    }
				}
			}
                        
                        for (Rule r : rules) {
                            score(features, featureLength, r);
                            System.out.println(r);
                        }

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

        private static void score(Feature [] features, int len, Rule r)
        {
            r.scores = new double[len];
            int idx = 0;
            for (Feature f : features) {
                System.arraycopy(f.score(r), 0, r.scores, idx, f.length());
                idx += f.length();
            }
        }

}
