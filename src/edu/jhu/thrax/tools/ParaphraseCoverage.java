package edu.jhu.thrax.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Logger;

import edu.jhu.jerboa.util.FileManager;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.io.LineReader;

public class ParaphraseCoverage {

  private static final Logger logger = Logger.getLogger(ParaphraseCoverage.class.getName());

  private static final String DELIM = String.format(" %s ", ThraxConfig.DELIMITER_REGEX);

  public static void main(String[] args) {

    String grammar_file = null;
    String reference_file = null;
    String weight_file = null;
    String output_file = null;
    String relevant_file = null;
    String judgment_prefix = null;

    for (int i = 0; i < args.length; i++) {
      if ("-g".equals(args[i]) && (i < args.length - 1)) {
        grammar_file = args[++i];
      } else if ("-r".equals(args[i]) && (i < args.length - 1)) {
        reference_file = args[++i];
      } else if ("-v".equals(args[i]) && (i < args.length - 1)) {
        relevant_file = args[++i];
      } else if ("-j".equals(args[i]) && (i < args.length - 1)) {
        judgment_prefix = args[++i];
      } else if ("-w".equals(args[i]) && (i < args.length - 1)) {
        weight_file = args[++i];
      } else if ("-o".equals(args[i]) && (i < args.length - 1)) {
        output_file = args[++i];
      }
    }

    if (grammar_file == null) {
      logger.severe("No grammar specified.");
      return;
    }
    if (reference_file == null) {
      logger.severe("No reference file specified.");
      return;
    }
    if (weight_file == null) {
      logger.severe("No weight file specified.");
      return;
    }
    if (output_file == null) {
      logger.severe("No output file specified.");
      return;
    }

    HashMap<String, List<Integer>> phrase_to_item = new HashMap<String, List<Integer>>();
    HashMap<Integer, Integer> item_counts = new HashMap<Integer, Integer>();
    HashSet<String> phrases = new HashSet<String>();

    HashMap<String, Double> weights = new HashMap<String, Double>();

    try {
      LineReader reference_reader = new LineReader(reference_file);
      while (reference_reader.hasNext()) {
        String line = reference_reader.next().trim();
        String[] fields = line.split(DELIM);

        int item = Integer.parseInt(fields[0]);
        String phrase = fields[1] + " ||| " + fields[2];

        phrases.add(phrase);
        if (!phrase_to_item.containsKey(phrase))
          phrase_to_item.put(phrase, new ArrayList<Integer>());
        phrase_to_item.get(phrase).add(item);
        item_counts.put(item, 0);
      }
      reference_reader.close();

      LineReader weights_reader = new LineReader(weight_file);
      while (weights_reader.hasNext()) {
        String line = weights_reader.next().trim();
        if (line.isEmpty()) continue;
        String[] fields = line.split("\\s+");
        weights.put(fields[0], Double.parseDouble(fields[1]));
      }
      weights_reader.close();

      PriorityQueue<ScoredParaphrase> paraphrases = new PriorityQueue<ScoredParaphrase>();

      BufferedWriter rel_writer = null;
      if (relevant_file != null) rel_writer = FileManager.getWriter(relevant_file);

      LineReader reader = new LineReader(grammar_file);
      System.err.print("[");
      int rule_count = 0;
      while (reader.hasNext()) {
        String rule_line = reader.next().trim();

        String[] fields = rule_line.split(DELIM);
        String candidate_phrase = fields[0] + " ||| " + fields[1];

        if (!phrases.contains(candidate_phrase)) continue;
        if (rel_writer != null) rel_writer.write(rule_line + "\n");

        double score = 0;
        String[] features = fields[3].split("\\s+");
        for (String f : features) {
          String[] parts = f.split("=");
          if (weights.containsKey(parts[0]))
            score += weights.get(parts[0]) * Double.parseDouble(parts[1]);
        }

        if (++rule_count % 10000 == 0) System.err.print("-");

        paraphrases.add(new ScoredParaphrase(candidate_phrase, fields[2], score));
      }
      System.err.println("]");
      reader.close();
      if (rel_writer != null) rel_writer.close();

      int num_items = item_counts.keySet().size();
      int num_covered = 0;
      int num_paraphrases = 0;

      for (ScoredParaphrase sp : paraphrases) {
        for (int item : phrase_to_item.get(sp.key)) {
          int count = item_counts.get(item);
          if (count == 0) num_covered++;
          item_counts.put(item, count + 1);
          num_paraphrases++;
        }
      }

      System.err.println("Items:       " + num_items);
      System.err.println("Covered:     " + num_covered);
      System.err.println("Paraphrases: " + num_paraphrases);

      boolean judge = (judgment_prefix != null);
      BufferedWriter cand_writer = null;
      int bin_id = 0;
      if (judge) cand_writer = FileManager.getWriter(judgment_prefix + ".cand");
      double[] bins = {-24.0, -18.0, -10.0, -8.0, -5.0};
      double last_score = Double.NEGATIVE_INFINITY;
      Random rand = new Random();

      BufferedWriter score_writer = FileManager.getWriter(output_file);
      while (!paraphrases.isEmpty()) {
        ScoredParaphrase sp = paraphrases.poll();
        boolean print = false;
        for (int item : phrase_to_item.get(sp.key)) {
          int count = item_counts.get(item);
          count--;
          item_counts.put(item, count);
          if (count == 0) {
            num_covered--;
            print = true;
          }
        }
        if (judge && bin_id < bins.length && last_score < bins[bin_id] && sp.score >= bins[bin_id]) {
          bin_id++;
          logger.info("Sampling bin :" + bin_id);
          
          Object[] pps = paraphrases.toArray();
          for (int i = 0; i < 200; i++) {
            int cand = rand.nextInt(paraphrases.size());
            cand_writer.write(((ScoredParaphrase) pps[cand]).key + " ||| "
                + ((ScoredParaphrase) pps[cand]).paraphrase + "\n");
          }
        }
        last_score = sp.score;
        if (print)
          score_writer.write(sp.score + "\t" + (num_covered / (double) num_items) + "\t"
              + (num_paraphrases / (double) num_covered) + "\n");
        num_paraphrases -= phrase_to_item.get(sp.key).size();
      }
      if (judge) cand_writer.close();
      score_writer.close();
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }
}


class ScoredParaphrase implements Comparable<ScoredParaphrase> {
  String key;
  String paraphrase;
  double score;

  public ScoredParaphrase(String k, String p, double s) {
    key = k;
    paraphrase = p;
    score = s;
  }

  @Override
  public int compareTo(ScoredParaphrase that) {
    return Double.compare(this.score, that.score);
  }
}
