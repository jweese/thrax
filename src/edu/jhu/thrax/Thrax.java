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
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Executors;

/**
 * The main class for the Thrax extractor.
 */
public class Thrax {

    /**
     * The main function of the exctractor.
     *
     * @param argv the command line passed to the JVM
     */
    public static void main(String [] argv)
    {
        String confFile = argv.length > 0 ? argv[0] : "NO_THRAX_CONF_FILE";
        if (argv.length > 1)
            ThraxConfig.setVerbosity(argv[1]);
        try {
            ThraxConfig.configure(confFile);
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

            Queue<Rule> allRules = new LinkedList<Rule>();

            Object [] currInputs = new Object[inputs.length];
            boolean haveInput = true;
            int lineNumber = 0;
            if (ThraxConfig.verbosity > 0)
                System.err.println("Processing sentences.");
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
                        allRules.offer(r);
                    }
                }
            }
            if (ThraxConfig.verbosity > 0)
                System.err.println("Done processing sentences.\nScoring rules.");
            while (allRules.peek() != null) {
                scorer.score(allRules.poll());
            }
            if (ThraxConfig.verbosity > 0)
                System.err.println("Done scoring rules.\nPrinting rules.");

            for (Rule r : scorer.rules()) {
                System.out.println(scorer.ruleScoreString(r));
            }
            if (ThraxConfig.verbosity > 0)
                System.err.println("Done!");

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
