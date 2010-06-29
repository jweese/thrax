package edu.jhu.thrax.features;

import java.util.HashMap;
import edu.jhu.thrax.datatypes.Rule;

public class RelativeFrequencyFeature implements Feature {

        public static final String name = "rf";

        public int length()
        {
            return 1;
        }

	private HashMap<Integer,Integer> lhsCounts;
	private HashMap<Rule,Integer> ruleCounts;

	public RelativeFrequencyFeature() {
		lhsCounts = new HashMap<Integer,Integer>();
		ruleCounts = new HashMap<Rule,Integer>();
	}

	public void noteExtraction(Rule r)
	{
		int currLhsCount = lhsCounts.containsKey(r.getLhs())
		                 ? lhsCounts.get(r.getLhs()) : 0;
		int currRuleCount = ruleCounts.containsKey(r)
		                  ? ruleCounts.get(r) : 0;

		lhsCounts.put(r.getLhs(), currLhsCount + 1);
		ruleCounts.put(r, currRuleCount + 1);
		return;
	}

	public double [] score(Rule r)
	{
            double [] ret = new double[1];
            ret[0] = -Math.log((double) ruleCounts.get(r) / lhsCounts.get(r.getLhs()));
            return ret;
	}
}
