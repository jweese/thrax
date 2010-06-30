package edu.jhu.thrax;

import edu.jhu.thrax.util.InvalidConfigurationException;

import edu.jhu.thrax.inputs.InputProvider;
import edu.jhu.thrax.inputs.InputProviderFactory;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.features.Feature;
import edu.jhu.thrax.features.FeatureFactory;
import edu.jhu.thrax.features.Scorer;

import edu.jhu.thrax.datatypes.Rule;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;

public class Thrax {

    public static void main(String [] argv)
    {
        try {
            ThraxConfig.configure(argv[0]);
            RuleExtractor extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);

            InputProvider [] inputs = InputProviderFactory.createAll(extractor.requiredInputs());
            Scorer scorer = new Scorer();

            if (!"".equals(ThraxConfig.FEATURES)) {
                String [] feats = ThraxConfig.FEATURES.split("\\s+");
                for (Feature f : FeatureFactory.createAll(feats))
                    scorer.addFeature(f);
            }
            else {
                System.err.println("WARNING: no feature functions provided");
            }

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
                        scorer.noteExtraction(r);
                    }
                }
            }
            for (Rule r : scorer.rules()) {
                scorer.score(r);
            }

            for (Rule r : scorer.rules()) {
                System.out.println(scorer.ruleScoreString(r));
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

}
