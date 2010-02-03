package edu.jhu.thrax.extraction;

import java.util.Set;
import edu.jhu.thrax.datatypes.Rule;

/**
 * This is the common interface for classes that can extract <code>Rule</code>
 * objects from certain inputs. 
 */
public interface RuleExtractor {
	/**
	 * The unique name of the extractor. This corresponds to a value that
	 * can be passed to the ThraxConfig.opts.get(ThraxConfig.GRAMMAR)
	 * key.
	 */
	public static String name = "";

	/**
	 * An array of names of input sources. These names correspond
	 * to configuration options that must be provided by the user (either
	 * on the command line or in a configuration file).
	 */
	public static String [] requiredInputs = {};

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

}
