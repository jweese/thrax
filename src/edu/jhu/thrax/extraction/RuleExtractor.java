package edu.jhu.thrax.extraction;

import java.util.List;
import edu.jhu.thrax.datatypes.Rule;
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
     * @param inputs an array of String; the required input to this
     * extractor
     * @return a list of <code>Rule</code> extracted from these inputs
     */
    public List<Rule> extract(String [] inputs);

}
