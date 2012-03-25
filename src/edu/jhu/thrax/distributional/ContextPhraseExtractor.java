package edu.jhu.thrax.distributional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.util.FormatUtils;
import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.util.exceptions.MalformedInputException;
import edu.jhu.thrax.util.exceptions.NotEnoughFieldsException;
import edu.jhu.thrax.util.io.LineReader;

@SuppressWarnings("unchecked")
public class ContextPhraseExtractor {

	private final String G = "_";

	private final String L = "l" + G;
	private final String C = "c" + G;
	private final String R = "r" + G;

	private final String LEX = "lex" + G;
	private final String POS = "pos" + G;
	private final String LEM = "lem" + G;
	private final String SYN = "syn" + G;
	private final String DEP = "dep" + G;
	private final String GOV = "gov" + G;

	private int MAX_PHRASE_LENGTH;

	private int MAX_LEX_CONTEXT;
	private int MAX_POS_CONTEXT;
	private int MAX_LEM_CONTEXT;

	private int MAX_LEX_GRAM;
	private int MAX_POS_GRAM;
	private int MAX_LEM_GRAM;
	
	private final boolean USE_LEX;
	private final boolean USE_POS;
	private final boolean USE_LEM;
	
	private final boolean USE_LEX_DEP;
	private final boolean USE_POS_DEP;
	private final boolean USE_LEM_DEP;
	
	private String[][] lex_features;
	private String[][] pos_features;
	private String[][] lem_features;
	
	private int size;

	private LatticeArray parse;
	private String[] lemma;
	private ArrayList<Dependency>[] govern;
	private Dependency[] depend;

	public ContextPhraseExtractor(Configuration conf) {
		MAX_PHRASE_LENGTH = conf.getInt("thrax.max-phrase-length", 4);

		MAX_LEX_CONTEXT = conf.getInt("thrax.max-lex-context", 4);
		MAX_POS_CONTEXT = conf.getInt("thrax.max-pos-context", 4);
		MAX_LEM_CONTEXT = conf.getInt("thrax.max-lem-context", 4);

		MAX_LEX_GRAM = conf.getInt("thrax.max-lex-gram", 2);
		MAX_POS_GRAM = conf.getInt("thrax.max-pos-gram", 2);
		MAX_LEM_GRAM = conf.getInt("thrax.max-lem-gram", 2);
		
		USE_LEX = conf.getBoolean("thrax.use-lex-ngrams", false);
		USE_POS = conf.getBoolean("thrax.use-pos-ngrams", false);
		USE_LEM = conf.getBoolean("thrax.use-lem-ngrams", false);
		
		USE_LEX_DEP = conf.getBoolean("thrax.use-lex-dep", false);
		USE_POS_DEP = conf.getBoolean("thrax.use-pos-dep", false);
		USE_LEM_DEP = conf.getBoolean("thrax.use-lem-dep", false);
	}

	public List<ContextPhrase> extract(String input) throws MalformedInputException {
		List<ContextPhrase> output = new ArrayList<ContextPhrase>();
		try {
			input = StringEscapeUtils.unescapeXml(input);

			String[] inputs = input.split(ThraxConfig.DELIMITER_REGEX);
			if (inputs.length < 3)
				throw new NotEnoughFieldsException();

			parse = new LatticeArray(inputs[0].trim(), true);
			lemma = inputs[1].trim().toLowerCase().split("\\s+");

			size = lemma.length;
			if (size != parse.size())
				throw new MalformedInputException();

			govern = new ArrayList[size];
			for (int i = 0; i < size; i++)
				govern[i] = new ArrayList<Dependency>();
			depend = new Dependency[size];

			String[] entries = inputs[2].trim().split("\\s+");
			for (String entry : entries) {
				Dependency d = new Dependency(entry);
				govern[d.gov].add(d);
				depend[d.dep] = d;
			}

			generateAllGramFeatures();
			
			for (int i = 0; i < size; i++) {
				for (int j = i + 1; j <= Math.min(i + MAX_PHRASE_LENGTH, size); j++) {
					ContextPhrase cp = new ContextPhrase(parse.getTerminalPhrase(i, j));
					if (USE_LEX)
						addGramFeatures(cp, i, j, MAX_LEX_CONTEXT, MAX_LEX_GRAM, LEX, lex_features);
					if (USE_POS)
						addGramFeatures(cp, i, j, MAX_POS_CONTEXT, MAX_POS_GRAM, POS, pos_features);
					if (USE_LEM)
						addGramFeatures(cp, i, j, MAX_LEM_CONTEXT, MAX_LEM_GRAM, LEM, lem_features);
					
					addSyntaxFeatures(i, j, cp);
					addDependencyFeatures(i, j, cp);
					cp.addFeature("count");
					output.add(cp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MalformedInputException();
		}
		return output;
	}
	
	private void generateAllGramFeatures() {
		if (USE_LEX)
			lex_features = buildGramFeatures(parse.getTerminals(), MAX_LEX_GRAM);
		if (USE_POS)
			pos_features = buildGramFeatures(parse.getPOS(), MAX_POS_GRAM);
		if (USE_LEM)
			lem_features = buildGramFeatures(lemma, MAX_LEM_GRAM);
	}

	private String[][] buildGramFeatures(String[] sentence, int N) {
		String[][] cache = new String[size][];
		for (int i = 0; i <= size - N; i++)
			cache[i] = new String[N];
		for (int i = 1; i < N; i++)
			cache[size - N + i] = new String[N - i];
		
		StringBuilder sb = new StringBuilder();
		for (int cf = 0; cf < size; cf++) {
			sb.delete(0, sb.length());
			for (int l = 0; l < Math.min(N, size - cf); l++) {
				sb.append(sentence[cf + l]).append(G);
				cache[cf][l] = sb.toString();
			}
		}
		return cache;
	}

	private void addGramFeatures(ContextPhrase cp, int from, int to,
			int max_window, int N, String tag, String[][] cache) {
		String left_prefix = L + tag;
		for (int cf = Math.max(0, from - max_window); cf < from; cf++)
			for (int l = 0; l < Math.min(N, from - cf); l++)
				cp.addFeature(left_prefix + cache[cf][l] + (from - cf));
		
		String right_prefix = R + tag;
		final int right_boundary = Math.min(size, to + max_window);
		for (int cf = to; cf < right_boundary; cf++)
			for (int l = 0; l < Math.min(N, right_boundary - cf); l++)
				cp.addFeature(right_prefix + cache[cf][l] + (cf - to + 1));
	}
	
	private void addSyntaxFeatures(int from, int to, ContextPhrase cp) {
		Collection<Integer> constituents = parse.getConstituentLabels(from, to);
		for (int c : constituents)
			cp.addFeature(C + SYN + "span" + G + Vocabulary.getWord(c));

		Collection<Integer> ccg = parse.getCcgLabels(from, to);
		for (int c : ccg) {
			String label = Vocabulary.getWord(c);
			if (label.contains("/")) {
				String[] parts = label.split("/");
				cp.addFeature(R + SYN + "pref" + G + parts[0]);
				cp.addFeature(R + SYN + "miss" + G + parts[1]);
			} else {
				String[] parts = label.split("\\\\");
				cp.addFeature(L + SYN + "suff" + G + parts[0]);
				cp.addFeature(L + SYN + "miss" + G + parts[1]);
			}
		}
	}

	private void addDependencyFeatures(int from, int to, ContextPhrase cp) {
		int head = from;
		boolean seen_outlink = false;
		boolean valid = true;
		for (int p = from; p < to; p++) {
			if (depend[p] != null) {
				if (depend[p].gov < from || depend[p].gov >= to) {
					depend[p].addDependingFeatures(cp);
					valid = valid && !seen_outlink;
					if (valid)
						head = p;
					seen_outlink = true;
				} else if (valid && p == head) {
					head = depend[p].gov;
				}
			} else if (govern[p].isEmpty()) {
				valid = false;
			}
			for (Dependency d : govern[p]) {
				if (d.dep < from || d.dep >= to) {
					d.addGoverningFeatures(cp);
					valid = false;
				}
			}
		}
		if (valid) {
			cp.addFeature(C + "head" + G + LEX + parse.getTerminal(head));
			cp.addFeature(C + "head" + G + LEM + lemma[head]);
			cp.addFeature(C + "head" + G + POS + parse.getPOS(head));
		}
	}

	public static void main(String[] args) throws Exception {
		LineReader reader = new LineReader(args[0]);

		ContextPhraseExtractor cpe = new ContextPhraseExtractor(new Configuration());

		while (reader.hasNext()) {
			String line = reader.next().trim();
			List<ContextPhrase> cps = cpe.extract(line);
			for (ContextPhrase cp : cps) {
				TreeMap<Text, Integer> feature_map = new TreeMap<Text, Integer>();
				for (Writable fn : cp.getFeatures().keySet())
					feature_map.put((Text) fn, ((IntWritable) cp.getFeatures().get(fn)).get());
				System.out.println(FormatUtils.contextPhraseToText(cp.getPhrase(), feature_map));
			}
		}
	}

	class Dependency {
		final String type;
		final int gov;
		final int dep;
		
		final String dep_lex;
		final String gov_lex;
		final String dep_lem;
		final String gov_lem;
		final String dep_pos;
		final String gov_pos;		

		public Dependency(String entry) {
			String[] fields = entry.split("-");
			type = fields[0];
			gov = Integer.parseInt(fields[1]) - 1;
			dep = Integer.parseInt(fields[2]) - 1;
			
			String dep_side = (gov > dep ? R : L);
			String gov_side = (gov > dep ? L : R);
			
			if (USE_LEX_DEP) {
				dep_lex = dep_side + DEP + type + G + LEX + parse.getTerminal(gov);
				gov_lex = gov_side + GOV + type + G + LEX + parse.getTerminal(dep);
			} else {
				dep_lex = null;
				gov_lex = null;
			}
			if (USE_POS_DEP) {
				dep_pos = dep_side + DEP + type + G + POS + parse.getPOS(gov);
				gov_pos = gov_side + GOV + type + G + POS + parse.getPOS(dep);
			} else {
				dep_pos = null;
				gov_pos = null;
			}
			if (USE_LEM_DEP) {
				dep_lem = dep_side + DEP + type + G + LEM + lemma[gov];
				gov_lem = gov_side + GOV + type + G + LEM + lemma[dep];
			} else {
				dep_lem = null;
				gov_lem = null;
			}
		}
		
		final void addGoverningFeatures(ContextPhrase cp) {
			if (USE_LEX_DEP) cp.addFeature(gov_lex);
			if (USE_POS_DEP) cp.addFeature(gov_pos);
			if (USE_LEM_DEP) cp.addFeature(gov_lem);
		}
		
		final void addDependingFeatures(ContextPhrase cp) {
			if (USE_LEX_DEP) cp.addFeature(dep_lex);
			if (USE_POS_DEP) cp.addFeature(dep_pos);
			if (USE_LEM_DEP) cp.addFeature(dep_lem);
		}
	}
}
