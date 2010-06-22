package edu.jhu.thrax.extraction;

import java.util.Set;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.features.Feature;

/**
 * This is the common interface for classes that can extract <code>Rule</code>
 * objects from certain inputs. 
 */
public interface RuleExtractor {

	/**
	 * An array of names of input sources. These names correspond
	 * to configuration options that must be provided by the user (either
	 * on the command line or in a configuration file).
	 */
	public String [] requiredInputs();

	/**
	 * Extracts synchronous context-free production rules given some
	 * inputs. The inputs in the array are provided in the same order that
	 * <code>requiredInputs</code> gave their names.
	 *
	 * @param inputs an array of Objects; the required input to this
	 * extractor
	 * @return a list of <code>Rule</code> extracted from these inputs
	 */
	public Set<Rule> extract(Object [] inputs);

        /**
         * Adds a feature function to this extractor. When the rules are
         * extracted, they will be scored using all of the features that were
         * added.
         *
         * @param f the feature function to add to this extractor
         */
        public void addFeature(Feature f);

        /**
         * Compute feature function scores for a rule using the features
         * that were added to this extractor. This should be called on all
         * rules in the corpus after all rules have been extracted (since
         * some features may depend on corpus-wide statistics).
         *
         * @param r the rule to score
         */
        public void score(Rule r);
}
