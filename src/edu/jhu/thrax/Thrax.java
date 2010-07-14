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
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

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
            final RuleExtractor extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);

            final Scorer scorer = new Scorer();

            if (!"".equals(ThraxConfig.FEATURES)) {
                String [] feats = ThraxConfig.FEATURES.split("\\s+");
                for (Feature f : FeatureFactory.createAll(feats))
                    scorer.addFeature(f);
            }
            else {
                System.err.println("WARNING: no feature functions provided");
            }

            final List<Rule> allRules = new Vector<Rule>(1000, 1000);

            final InputProvider inp = new InputProvider(extractor.requiredInputs());
            if (ThraxConfig.verbosity > 0)
                System.err.println("Processing sentences.");

            BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(1000);
            ThreadPoolExecutor exec = new ThreadPoolExecutor(ThraxConfig.THREADS, ThraxConfig.THREADS, 1, TimeUnit.SECONDS, q);

            while (inp.hasNext()) {
                exec.execute(new Runnable(){
                    public void run() {
                        for (Rule r : extractor.extract(inp.next())) {
                            scorer.noteExtraction(r);
                            allRules.add(r);
                        }
                    }
                });
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
