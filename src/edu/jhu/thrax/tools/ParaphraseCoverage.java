package edu.jhu.thrax.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
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

    for (int i = 0; i < args.length; i++) {
      if ("-g".equals(args[i]) && (i < args.length - 1)) {
        grammar_file = args[++i];
      } else if ("-r".equals(args[i]) && (i < args.length - 1)) {
        reference_file = args[++i];
      } else if ("-v".equals(args[i]) && (i < args.length - 1)) {
        relevant_file = args[++i];
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

    int unknown_source = 0;

    HashMap<String, List<Integer>> reference_pairs = new HashMap<String, List<Integer>>();
    HashMap<Integer, Integer> coverage_counts = new HashMap<Integer, Integer>();
    HashSet<String> items = new HashSet<String>();

    HashMap<String, Double> weights = new HashMap<String, Double>();

    try {
      LineReader reference_reader = new LineReader(reference_file);
      while (reference_reader.hasNext()) {
        String line = reference_reader.next().trim();
        String[] fields = line.split(DELIM);

        int id = Integer.parseInt(fields[0]);
        String item = fields[1] + " ||| " + fields[2];

        items.add(item);
        if (!reference_pairs.containsKey(item))
          reference_pairs.put(item, new ArrayList<Integer>());
        reference_pairs.get(item).add(id);
        coverage_counts.put(id, 0);
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

      HashMap<String, Double> candidates = new HashMap<String, Double>();

      BufferedWriter rel_writer = null;
      if (relevant_file != null) rel_writer = FileManager.getWriter(relevant_file);

      LineReader reader = new LineReader(grammar_file);
      System.err.print("[");
      int count = 0;
      while (reader.hasNext()) {
        String rule_line = reader.next().trim();

        String[] fields = rule_line.split(DELIM);

        String lhs = fields[0];
        String source = fields[1];
        String cand = lhs + " ||| " + source;

        if (!items.contains(cand)) {
          unknown_source++;
          continue;
        }
        if (rel_writer != null) rel_writer.write(rule_line + "\n");

        double score = 0;
        String[] features = fields[3].split("\\s+");
        for (String f : features) {
          String[] parts = f.split("=");
          if (weights.containsKey(parts[0]))
            score += weights.get(parts[0]) * Double.parseDouble(parts[1]);
        }

        if (++count % 10000 == 0) System.err.print("-");

        if (!candidates.containsKey(cand)) {
          candidates.put(cand, score);
        } else {
          double previous = candidates.get(cand);
          candidates.put(cand, Math.max(score, previous));
        }
      }
      System.err.println("]");
      reader.close();
      if (rel_writer != null) rel_writer.close();

      int total = 0;
      int found = 0;
      int correct = coverage_counts.keySet().size();
      
      PriorityQueue<ScoredEntry> entries = new PriorityQueue<ScoredEntry>();
      for (String cand : candidates.keySet()) {
        if (reference_pairs.containsKey(cand)) {
          for (int item : reference_pairs.get(cand)) {
            int coverage = coverage_counts.get(item);
            if (coverage == 0) found++;
            coverage_counts.put(item, coverage + 1);
          }
        }
        total++;
        entries.add(new ScoredEntry(cand, candidates.get(cand)));
      }

      int paraphrases = 0;
      for (int c : coverage_counts.keySet()) {
        paraphrases += coverage_counts.get(c);
        System.err.println(coverage_counts.get(c));
      }
      System.err.println(paraphrases);
      
      System.err.println("Total:      " + total);
      System.err.println("Found:      " + found);
      System.err.println("Correct:    " + correct);
      System.err.println("Irrelevant: " + unknown_source);

      BufferedWriter score_writer = FileManager.getWriter(output_file);
      while (!entries.isEmpty()) {
        ScoredEntry e = entries.poll();
        if (reference_pairs.containsKey(e.pair)) {
          score_writer.write(e.score + "\t" + (found / (double) correct) + "\t"
              + (paraphrases / (double) found) + "\n");
          for (int item : reference_pairs.get(e.pair)) {
            int coverage = coverage_counts.get(item);
            coverage--;
            coverage_counts.put(item, coverage);
            if (coverage == 0) found--;
            paraphrases--;
          }
        }
        total--;
      }

      score_writer.close();
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }
}
