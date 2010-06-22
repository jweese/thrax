package edu.jhu.thrax.extraction;

import java.util.Collection;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.features.Feature;
import edu.jhu.thrax.ThraxConfig;

/**
 * This class extracts Hiero-style SCFG rules. The inputs that are needed
 * are "source" "target" and "alignment", which are the source and target
 * sides of a parallel corpus, and an alignment between each of the sentences.
 */
public class HieroRuleExtractor implements RuleExtractor {

	public static String name = "hiero";
	public static String [] requiredInputs = { ThraxConfig.SOURCE,
	                                           ThraxConfig.TARGET,
						   ThraxConfig.ALIGNMENT };

	public int INIT_LENGTH_LIMIT = 10;
	public int SOURCE_LENGTH_LIMIT = 5;
	public int NT_LIMIT = 2;
        public int LEXICAL_MINIMUM = 1;
        public boolean ALLOW_ADJACENT_NTS = false;
        public boolean ALLOW_LOOSE_BOUNDS = false;

        public static final String X = "X";
        public static final int X_ID = Vocabulary.getId(X);

        private ArrayList<Feature> features;
        private int featureLength;

        /**
         * Default constructor. All it does is to initialize the list of
         * features to an empty list.
         */
        public HieroRuleExtractor()
        {
            features = new ArrayList<Feature>();
            featureLength = 0;
            ALLOW_ADJACENT_NTS = ThraxConfig.opts.containsKey(ThraxConfig.ADJACENT);
            ALLOW_LOOSE_BOUNDS = ThraxConfig.opts.containsKey(ThraxConfig.LOOSE);
        }

	public Set<Rule> extract(Object [] inputs)
	{
		if (inputs.length < 3) {
			return null;
		}

		int [] source = (int []) inputs[0];
		int [] target = (int []) inputs[1];
		Alignment alignment = (Alignment) inputs[2];

                ArrayList<PhrasePair> [] phrasesByStart = initialPhrasePairs(source, target, alignment);

                Queue<Rule> q = new LinkedList<Rule>();
                for (int i = 0; i < source.length - 1; i++)
                    q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

                return processQueue(q, phrasesByStart);
	}

        public void addFeature(Feature f)
        {
            features.add(f);
            featureLength += f.length;
        }

        public void score(Rule r)
        {
            r.scores = new double[featureLength];
            int idx = 0;
            for (Feature f : features) {
                System.arraycopy(f.score(r), 0, r.scores, idx, f.length);
                idx += f.length;
            }
        }
	
        protected Set<Rule> processQueue(Queue<Rule> q, ArrayList<PhrasePair> [] phrasesByStart)
        {
            Set<Rule> rules = new HashSet<Rule>();
            while (q.peek() != null) {
                Rule r = q.poll();

                if (isWellFormed(r)) {
                    for (Rule s : getLabelVariants(r)) {
                        for (Feature feat : features)
                            feat.noteExtraction(s);
                        rules.add(s);
                    }
                }
                if (r.appendPoint > phrasesByStart.length - 1)
                    continue;

                for (PhrasePair pp : phrasesByStart[r.appendPoint]) {
                    if (pp.sourceEnd - r.rhs.sourceStart > INIT_LENGTH_LIMIT ||
                        (r.rhs.targetStart >= 0 &&
                         pp.targetEnd - r.rhs.targetStart > INIT_LENGTH_LIMIT))
                        continue;
                    if (r.numNTs < NT_LIMIT &&
                        r.numNTs + r.numTerminals < SOURCE_LENGTH_LIMIT &&
                        (!r.sourceEndsWithNT || ALLOW_ADJACENT_NTS)) {
                        Rule s = r.copy();
                        s.extendWithNonterminal(pp);
                        q.offer(s);
                    }
                    if (r.numNTs + r.numTerminals + pp.sourceEnd - pp.sourceStart <= SOURCE_LENGTH_LIMIT) {
                        Rule s = r.copy();
                        s.extendWithTerminals(pp);
                        q.offer(s);
                    }
                }

            }
            return rules;
        }

        protected boolean isWellFormed(Rule r)
        {
            if (r.rhs.targetStart < 0)
                return false;
            for (int i = r.rhs.targetStart; i < r.rhs.targetEnd; i++) {
                if (r.targetLex[i] < 0)
                    return false;
            }
            return (r.alignedWords >= LEXICAL_MINIMUM);
        }

        private Collection<Rule> variantSet = new HashSet<Rule>(1);
        protected Collection<Rule> getLabelVariants(Rule r)
        {
            variantSet.clear();
            r.lhs = X_ID;
            Arrays.fill(r.nts, X_ID);
            variantSet.add(r);
            return variantSet;
        }

        private ArrayList<PhrasePair> [] initialPhrasePairs(int [] f, int [] e, Alignment a)
        {
            
            ArrayList<ArrayList<PhrasePair>> result = new ArrayList<ArrayList<PhrasePair>>();
            for (int j = 0; j < f.length; j++)
                result.add(new ArrayList<PhrasePair>());

            int maxlen = f.length < INIT_LENGTH_LIMIT ? f.length : INIT_LENGTH_LIMIT;
            for (int len = 1; len < maxlen; len++) {
                for (int i = 0; i < f.length - len + 1; i++) {
                    if (!ALLOW_LOOSE_BOUNDS && !a.sourceIsAligned(i) || !a.sourceIsAligned(i+len-1))
                        continue;
                    PhrasePair pp = a.getPairFromSource(i, i+len);
                    if (pp != null && pp.targetEnd - pp.targetStart <= INIT_LENGTH_LIMIT)
                        result.get(i).add(pp);
                }
            }
            return (ArrayList<PhrasePair> []) result.toArray();
        }

}
