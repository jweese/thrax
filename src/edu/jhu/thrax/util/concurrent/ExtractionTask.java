package edu.jhu.thrax.util.concurrent;

import java.util.List;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.features.Scorer;
import edu.jhu.thrax.extraction.RuleExtractor;

public class ExtractionTask implements Runnable {

    private RuleExtractor extractor;
    private Scorer scorer;
    private List<Rule> allRules;
    private String [] inputs;

    public int lineNumber;

    public ExtractionTask(int line, RuleExtractor e, Scorer s, List<Rule> rules, String [] inp)
    {
        lineNumber = line;
        extractor = e;
        scorer = s;
        allRules = rules;
        inputs = inp;
    }

    public void run()
    {
        for (Rule r : extractor.extract(inputs)) {
            scorer.noteExtraction(r);
            allRules.add(r);
        }
    }
}
