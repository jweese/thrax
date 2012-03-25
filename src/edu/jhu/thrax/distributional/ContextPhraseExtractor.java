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

	private final String L = "l";
	private final String C = "c";
	private final String R = "r";

	private final String LEX = "lex";
	private final String POS = "pos";
	private final String LEM = "lem";
	private final String SYN = "syn";
	private final String DEP = "dep";
	private final String GOV = "gov";

	private int MAX_PHRASE_LENGTH;

	private int MAX_LEX_CONTEXT;
	private int MAX_POS_CONTEXT;
	private int MAX_LEM_CONTEXT;

	private int MAX_LEX_GRAM;
	private int MAX_POS_GRAM;
	private int MAX_LEM_GRAM;

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
			if (size != parse.size()) {
				System.err.println("Parse: " + parse.size() + "\t Lemma: " + size);
				throw new MalformedInputException();
			}

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

			for (int i = 0; i < size; i++) {
				for (int j = i + 1; j <= Math.min(i + MAX_PHRASE_LENGTH, size); j++) {
					ContextPhrase cp = new ContextPhrase(parse.getTerminalPhrase(i, j));
					addLexicalFeatures(i, j, cp);
					addPOSFeatures(i, j, cp);
					addLemmaFeatures(i, j, cp);
					addSyntaxFeatures(i, j, cp);
					addDependencyFeatures(i, j, cp);
					output.add(cp);
				}
			}
		} catch (Exception e) {
			throw new MalformedInputException();
		}
		return output;
	}

	private void addLexicalFeatures(int from, int to, ContextPhrase cp) {
		extractGramFeatures(parse.getTerminals(), cp, from, to, LEX, MAX_LEX_CONTEXT, MAX_LEX_GRAM);
	}

	private void addPOSFeatures(int from, int to, ContextPhrase cp) {
		extractGramFeatures(parse.getPOS(), cp, from, to, POS, MAX_POS_CONTEXT, MAX_POS_GRAM);
	}

	private void addLemmaFeatures(int from, int to, ContextPhrase cp) {
		extractGramFeatures(lemma, cp, from, to, LEM, MAX_LEM_CONTEXT, MAX_LEM_GRAM);
	}

	private void extractGramFeatures(String[] sentence, ContextPhrase cp, int from, int to,
			String tag, int max_window, int max_length) {
		StringBuilder sb = new StringBuilder();
		String left_prefix = L + G + tag;
		for (int cf = Math.max(0, from - max_window); cf < from; cf++) {
			sb.delete(0, sb.length()).append(left_prefix);
			for (int l = 0; l < Math.min(max_length, from - cf); l++) {
				sb.append(G).append(sentence[cf + l]);
				cp.addFeature(sb.toString() + G + (from - cf));
			}
		}
		String right_prefix = R + G + tag;
		final int right_boundary = Math.min(size, to + max_window);
		for (int cf = to; cf < right_boundary; cf++) {
			sb.delete(0, sb.length()).append(right_prefix);
			for (int l = 0; l < Math.min(max_length, right_boundary - cf); l++) {
				sb.append(G).append(sentence[cf + l]);
				cp.addFeature(sb.toString() + G + (cf - to + 1));
			}
		}
	}

	private void addSyntaxFeatures(int from, int to, ContextPhrase cp) {
		Collection<Integer> constituents = parse.getConstituentLabels(from, to);
		for (int c : constituents)
			cp.addFeature(C + G + SYN + G + "span" + G + Vocabulary.getWord(c));

		Collection<Integer> ccg = parse.getCcgLabels(from, to);
		for (int c : ccg) {
			String label = Vocabulary.getWord(c);
			if (label.contains("/")) {
				String[] parts = label.split("/");
				cp.addFeature(R + G + SYN + G + "pref" + G + parts[0]);
				cp.addFeature(R + G + SYN + G + "miss" + G + parts[1]);
			} else {
				String[] parts = label.split("\\\\");
				cp.addFeature(L + G + SYN + G + "suff" + G + parts[0]);
				cp.addFeature(L + G + SYN + G + "miss" + G + parts[1]);
			}
		}
	}

	private void addDependencyFeatures(int from, int to, ContextPhrase cp) {
		int head = from;
		boolean seen_outlink = false;
		boolean valid = true;
		for (int p = from; p < to; p++) {
			if (depend[p] != null) {
				if (depend[p].gov < from) {
					addLinkFeatures(cp, depend[p].gov, depend[p].type, L, DEP);
					valid = valid && !seen_outlink;
					if (valid)
						head = p;
					seen_outlink = true;
				} else if (depend[p].gov >= to) {
					addLinkFeatures(cp, depend[p].gov, depend[p].type, R, DEP);
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
				if (d.dep < from) {
					addLinkFeatures(cp, d.dep, d.type, L, GOV);
					valid = false;
				} else if (d.dep >= to) {
					addLinkFeatures(cp, d.dep, d.type, R, GOV);
					valid = false;
				}
			}
		}
		if (valid) {
			cp.addFeature(C + G + "head" + G + LEX + G + parse.getTerminal(head));
			cp.addFeature(C + G + "head" + G + LEM + G + lemma[head]);
			cp.addFeature(C + G + "head" + G + POS + G + parse.getPOS(head));
		}
	}

	private final void addLinkFeatures(ContextPhrase cp, int t, String type, String side,
			String direction) {
		cp.addFeature(side + G + direction + G + type + G + LEX + G + parse.getTerminal(t));
		cp.addFeature(side + G + direction + G + type + G + LEM + G + lemma[t]);
		cp.addFeature(side + G + direction + G + type + G + POS + G + parse.getPOS(t));
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
		String type;
		int gov;
		int dep;

		public Dependency(String entry) {
			String[] fields = entry.split("-");
			type = fields[0];
			gov = Integer.parseInt(fields[1]) - 1;
			dep = Integer.parseInt(fields[2]) - 1;
		}
	}
}
