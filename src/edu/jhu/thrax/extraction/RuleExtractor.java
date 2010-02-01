package edu.jhu.thrax.extraction;

import java.util.ArrayList;
import edu.jhu.thrax.datatypes.Rule;

public interface RuleExtractor {

	public String [] requiredInputs();
	public ArrayList<Rule> extract(Object [] inputs);

}
