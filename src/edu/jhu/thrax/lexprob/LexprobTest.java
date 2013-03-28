package edu.jhu.thrax.lexprob;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class LexprobTest extends Configured implements Tool {
  public int run(String[] argv) throws Exception {
    if (argv.length < 1) {
      System.err.println("usage: LexprobTest <file>");
      return 1;
    }

    Configuration conf = getConf();
    HashMapLexprobTable t = new HashMapLexprobTable(conf, argv[0]);
    System.err.println("HashMap populated: " + t.toString());
    TrieLexprobTable trie = new TrieLexprobTable(conf, argv[0]);
    System.err.println("Trie populated: " + trie.toString());
    return 0;
  }

  public static void main(String[] argv) throws Exception {
    ToolRunner.run(null, new LexprobTest(), argv);
    return;
  }
}
