package edu.jhu.thrax.extraction;

import java.util.ArrayList;
import edu.jhu.thrax.datatypes.Rule;

/**
 * This is the common interface for classes that can extract <code>Rule</code>
 * objects from certain inputs. 
 */
public interface RuleExtractor {

	/**
	 * Returns an array of names of input sources. These names correspond
	 * to configuration options that must be provided by the user (either
	 * on the command line or in a configuration file).
	 *
	 * @return an array of names of required input sources
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
	public ArrayList<Rule> extract(Object [] inputs);

}
