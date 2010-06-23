package edu.jhu.thrax.extraction;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;

public class SAMTExtractor extends HieroRuleExtractor {

    public static String name = "samt";

    public String [] requiredInputs()
    {
        return new String [] { ThraxConfig.SOURCE,
                               ThraxConfig.PARSE,
                               ThraxConfig.ALIGNMENT };
    }

    private LatticeArray lattice;

    public SAMTExtractor()
    {
        super();
    }

    public Set<Rule> extract(Object [] inputs)
    {
		if (inputs.length < 3) {
			return null;
		}

		int [] source = (int []) inputs[0];
		String parse = (String) inputs[1];
		Alignment alignment = (Alignment) inputs[2];

                lattice = new LatticeArray(parse);
                int [] target = yield(parse);

                PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);

                Queue<Rule> q = new LinkedList<Rule>();
                for (int i = 0; i < source.length; i++)
                    q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

                return processQueue(q, phrasesByStart);
	}

    protected Collection<Rule> getLabelVariants(Rule r)
    {
        Collection<Rule> result = new HashSet<Rule>();

        return result;
    }

    private int [] yield(String parse)
    {
        return null;
    }

}
