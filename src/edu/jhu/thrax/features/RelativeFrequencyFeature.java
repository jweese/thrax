package edu.jhu.thrax.features;

import java.util.HashMap;
import edu.jhu.thrax.datatypes.Rule;

public class RelativeFrequencyFeature implements Feature {

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
		int currLhsCount = lhsCounts.containsKey(r.lhs)
		                 ? lhsCounts.get(r.lhs) : 0;
		int currRuleCount = ruleCounts.containsKey(r)
		                  ? ruleCounts.get(r) : 0;

		lhsCounts.put(r.lhs, currLhsCount + 1);
		ruleCounts.put(r, currRuleCount + 1);
		return;
	}

	public double [] score(Rule r)
	{
            double [] ret = new double[1];
            ret[0] = (double) ruleCounts.get(r) / lhsCounts.get(r.lhs);
            return ret;
	}
}
