package edu.jhu.thrax;

import edu.jhu.thrax.util.InvalidConfigurationException;

import edu.jhu.thrax.inputs.InputProvider;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.features.Feature;
import edu.jhu.thrax.features.FeatureFactory;
import edu.jhu.thrax.features.Scorer;

import edu.jhu.thrax.datatypes.Rule;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import edu.jhu.thrax.util.concurrent.ExtractionTask;
import edu.jhu.thrax.util.concurrent.ExtractionThreadPoolExecutor;

/**
 * The main class for the Thrax extractor.
 */
public class Thrax {

    /**
     * The main function of the exctractor.
     *
     * @param argv the command line passed to the JVM
     */
    public static void main(String [] argv) throws InterruptedException
    {
        String confFile = argv.length > 0 ? argv[0] : "NO_THRAX_CONF_FILE";
        if (argv.length > 1)
            ThraxConfig.setVerbosity(argv[1]);
        try {
            ThraxConfig.configure(confFile);

            final Scorer scorer = new Scorer();

            if (!"".equals(ThraxConfig.FEATURES)) {
                String [] feats = ThraxConfig.FEATURES.split("\\s+");
                for (Feature f : FeatureFactory.createAll(feats))
                    scorer.addFeature(f);
            }
            else {
                System.err.println("WARNING: no feature functions provided");
            }

            final List<Rule> allRules = Collections.synchronizedList(new LinkedList<Rule>());

	    final RuleExtractor extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);
            final InputProvider inp = new InputProvider(extractor.requiredInputs());
            if (ThraxConfig.verbosity > 0)
                System.err.println("Processing sentences.");

            ExecutorService exec = new ExtractionThreadPoolExecutor();

            int numLines = 0;

            while (inp.hasNext()) {
                numLines++;
                exec.execute(new ExtractionTask(numLines, extractor, scorer, allRules, inp.next())); // Runnable(){
//                    public void run() {
//                        String [] inputs = inp.next();
//                        if (inputs == null)
//                            return;
//                        for (Rule r : extractor.extract(inputs)) {
//                            scorer.noteExtraction(r);
//                            allRules.add(r);
//                        }
//                    }
//                });
            }
            exec.shutdown();
            exec.awaitTermination(60, TimeUnit.SECONDS);
            if (!exec.isTerminated()) {
                // do something bad
            }
            if (ThraxConfig.verbosity > 0)
                System.err.println("Done processing sentences.\nScoring rules.");
            while (!allRules.isEmpty()) {
                scorer.score(allRules.remove(0));
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
