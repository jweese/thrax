package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public class PivotedLexicalSourceGivenTargetFeature extends
		PivotedNegLogProbFeature {

	private static final Text LABEL = new Text("Lex(f|e)");

	public String getName() {
		return "lexprob_sgt";
	}

	public Text getFeatureLabel() {
		return LABEL;
	}

	public Set<String> getPrerequisites() {
		Set<String> prereqs = new HashSet<String>();
		prereqs.add("f_given_e_lex");
        prereqs.add("e_given_f_lex");
		return prereqs;
	}

	public FloatWritable pivot(FeatureMap src, FeatureMap tgt) {
		float egf = ((FloatWritable) tgt.get(new Text("Lex(e|f)"))).get();
		float fge = ((FloatWritable) src.get(new Text("Lex(f|e)"))).get();

		return new FloatWritable(egf + fge);
	}

	@Override
	public Set<Text> getLowerBoundLabels() {
		Set<Text> lower_bound_labels = new HashSet<Text>();
		lower_bound_labels.add(new Text("Lex(e|f)"));
		lower_bound_labels.add(new Text("Lex(f|e)"));
		return lower_bound_labels;
	}

	@Override
	public Set<Text> getUpperBoundLabels() {
		return null;
	}
}
