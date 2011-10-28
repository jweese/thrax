package edu.jhu.thrax.hadoop.features;

import java.util.ArrayList;
import java.util.List;

public class SimpleFeatureFactory {

	public static SimpleFeature get(String name) {
		if (name.equals("abstract"))
			return new AbstractnessFeature();
		else if (name.equals("adjacent"))
			return new AdjacentNonTerminalsFeature();
		else if (name.equals("lexical"))
			return new LexicalityFeature();
		else if (name.equals("x-rule"))
			return new XRuleFeature();
		else if (name.equals("monotonic"))
			return new MonotonicFeature();
		else if (name.equals("phrase-penalty"))
			return new PhrasePenaltyFeature();
		else if (name.equals("target-word-count"))
			return new TargetWordCounterFeature();
		else if (name.equals("unaligned-count"))
			return new UnalignedWordCounterFeature();
		else if (name.equals("source-terminals-without-target"))
			return new ConsumeSourceTerminalsFeature();
		else if (name.equals("target-terminals-without-source"))
			return new ProduceTargetTerminalsFeature();
		else if (name.equals("glue-rule"))
			return new GlueRuleFeature();

		return null;
	}

	public static List<SimpleFeature> getAll(String names) {
		String[] feature_names = names.split("\\s+");
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();

		for (String feature_name : feature_names) {
			SimpleFeature feature = get(feature_name);
			if (feature != null)
				features.add(feature);
		}
		return features;
	}
}
