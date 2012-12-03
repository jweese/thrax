package edu.jhu.thrax.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import edu.jhu.jerboa.util.FileManager;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.io.LineReader;

public class SplitAndFilter {

  private static final Logger logger = Logger.getLogger(SplitAndFilter.class.getName());

  private static final String DELIM = String.format(" %s ", ThraxConfig.DELIMITER_REGEX);

  public static void main(String[] args) {

    boolean labeled = false;
    boolean sparse = false;

    String grammar_file = null;
    String filter_file = null;
    String output_prefix = null;

    for (int i = 0; i < args.length; i++) {
      if ("-g".equals(args[i]) && (i < args.length - 1)) {
        grammar_file = args[++i];
      } else if ("-f".equals(args[i]) && (i < args.length - 1)) {
        filter_file = args[++i];
      } else if ("-o".equals(args[i]) && (i < args.length - 1)) {
        output_prefix = args[++i];
      } else if ("-l".equals(args[i])) {
        labeled = true;
      } else if ("-s".equals(args[i])) {
        sparse = true;
      }
    }

    if (grammar_file == null) {
      logger.severe("No grammar specified.");
      return;
    }
    if (filter_file == null) {
      logger.severe("No filter file specified.");
      return;
    }
    if (output_prefix == null) {
      logger.severe("No output prefix specified.");
      return;
    }

    int lex_count = 0, phr_count = 0, syn_count = 0, drop_count = 0;

    HashSet<String> filter = new HashSet<String>();
    HashMap<String, Integer> stop_count = new HashMap<String, Integer>();
    try {
      LineReader filter_reader = new LineReader(filter_file);
      while (filter_reader.hasNext()) {
        String word = filter_reader.next().trim();
        filter.add(word);
        stop_count.put(word, 0);
      }
      filter_reader.close();
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }

    try {
      LineReader reader = new LineReader(grammar_file);
      BufferedWriter lex_writer = FileManager.getWriter(output_prefix + ".lexical.gz");
      BufferedWriter phr_writer = FileManager.getWriter(output_prefix + ".phrasal.gz");
      BufferedWriter syn_writer = FileManager.getWriter(output_prefix + ".syntax.gz");
      BufferedWriter stop_writer = FileManager.getWriter(output_prefix + ".stop.gz");
      BufferedWriter stats_writer = FileManager.getWriter(output_prefix + ".stats.txt");

      HashSet<String> source_words = new HashSet<String>();
      HashSet<String> target_words = new HashSet<String>();

      while (reader.hasNext()) {
        String rule_line = reader.next().trim();
        boolean phrasal = true;
        boolean drop = true;

        String[] fields = rule_line.split(DELIM);
        String[] source = fields[1].split("\\s+");
        String[] target = fields[2].split("\\s+");

        source_words.clear();
        target_words.clear();
        for (String word : source) {
          if (word.startsWith("["))
            phrasal = false;
          else
            source_words.add(word);
        }
        for (String word : target)
          if (!word.startsWith("[")) target_words.add(word);

        HashSet<String> source_added = (HashSet<String>) source_words.clone();
        HashSet<String> target_added = (HashSet<String>) target_words.clone();

        source_added.removeAll(target_words);
        target_added.removeAll(source_words);

        for (String word : source_added)
          if (!filter.contains(word))
            drop = false;
          else
            stop_count.put(word, stop_count.get(word) + 1);
        for (String word : target_added)
          if (!filter.contains(word))
            drop = false;
          else
            stop_count.put(word, stop_count.get(word) + 1);

        // Dropped rule.
        if (drop) {
          stop_writer.write(rule_line);
          stop_writer.newLine();
          drop_count++;
          continue;
        }

        // Lexical rules;
        if (source.length == 1 && target.length == 1 && !source[0].startsWith("[")) {
          lex_writer.write(rule_line);
          lex_writer.newLine();
          lex_count++;
          continue;
        }

        // Syntactic or phrasal rule.
        (phrasal ? phr_writer : syn_writer).write(rule_line);
        (phrasal ? phr_writer : syn_writer).newLine();
        if (phrasal)
          phr_count++;
        else
          syn_count++;
      }
      reader.close();

      for (String word : stop_count.keySet())
        stats_writer.write(word + "\t" + stop_count.get(word) + "\n");
      
      System.err.println("Total:  \t" + (lex_count + phr_count + syn_count + drop_count));
      System.out.println("Dropped:\t" + drop_count);
      System.out.println("Lexical:\t" + lex_count);
      System.out.println("Phrasal:\t" + phr_count);
      System.out.println("Syntactic:\t" + syn_count);

      lex_writer.close();
      phr_writer.close();
      syn_writer.close();
      stop_writer.close();
      stats_writer.close();
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }

}
