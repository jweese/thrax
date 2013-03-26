package edu.jhu.thrax.hadoop.features.pivot;

import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public interface PivotedFeature {

	public String getName();
	
	public Text getFeatureLabel();

	public Set<String> getPrerequisites();

	public Writable pivot(FeatureMap src, FeatureMap tgt);

	public void initializeAggregation();
	
	public void aggregate(FeatureMap a);

	public Writable finalizeAggregation();
	
	public Set<Text> getLowerBoundLabels();
	
	public Set<Text> getUpperBoundLabels();
	
	public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map);

	public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map);

}
