package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.util.Map;

public class IdentityFeature implements SimpleFeature {

	private static final Text LABEL = new Text("Identity");
	private static final IntWritable ZERO = new IntWritable(0);
	private static final IntWritable ONE = new IntWritable(1);

	public void score(RuleWritable r, Map<Text, Writable> map) {
		if (r.target.equals(r.source))
			map.put(LABEL, ONE);
		else
			map.put(LABEL, ZERO);
		return;
	}

	public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
		map.put(LABEL, ZERO);
	}

	public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
		map.put(LABEL, ZERO);
	}
}
