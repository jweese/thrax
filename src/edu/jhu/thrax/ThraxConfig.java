package edu.jhu.thrax;

import edu.jhu.thrax.util.ConfFileParser;

import java.util.Map;
import java.io.IOException;

/**
 */
public class ThraxConfig {

    /**
     * Field delimiter.
     */
    public static final String DELIMITER = "|||";

    /**
     * Determines the verbosity level. 0 is the normal level, which is
     * quiet. 1 means verbose, and 2 includes debugging information.
     * A number less than zero means silent.
     */
    public static int verbosity = 0;

    /**
     * Grammar type for extraction.
     */
    public static String GRAMMAR = "hiero";
    /**
     * Path to alignment file.
     */
    public static String ALIGNMENT = "";
    /**
     * Alignment format. This doesn't do anything right now. The only format
     * is Berkeley.
     */
    public static String ALIGNMENT_FORMAT = "berkeley";
    /**
     * Path to file with parses of target-side sentences.
     */
    public static String PARSE = "";
    /**
     * Parse format. This doesn't do anything. The only format is Penn
     * treebank.
     */
    public static String PARSE_FORMAT = "ptb";
    /**
     * Path to file with source-side sentences.
     */
    public static String SOURCE = "";
    /**
     * Path to file with plaintext target-side sentences.
     */
    public static String TARGET = "";
    /**
     * Output format. This doesn't do anything. The only format is Joshua.
     */
    public static String OUTPUT_FORMAT = "joshua";

    /**
     * Whether or not to allow adjacent NTs in extracted rules.
     */
    public static boolean ADJACENT = false;
    /**
     * Whether or not to allow unaligned words on the boundary of initial
     * phrases during rule extraction.
     */
    public static boolean LOOSE = false;
    /**
     * Maximum length of initial phrase pairs.
     */
    public static int INITIAL_PHRASE_LIMIT = 10;
    /**
     * Maximum number of symbols on the source side of extracted rules.
     */
    public static int SOURCE_LENGTH_LIMIT = 5;
    /**
     * Maximum number of nonterminal symbols in extracted rules.
     */
    public static int ARITY = 2;
    /**
     * Minimum number of aligned terminal symbols required in extracted rules.
     */
    public static int LEXICALITY = 1;

    /**
     * Contains whitespace-separated names of features for rule extraction.
     */
    public static String FEATURES = "";

    /**
     * Maximum number of constituent labels to extract for each span in SAMT.
     * Constituent labels are labels that consist of one syntactic symbol.
     */
    public static int MAX_CONSTITUENT_LABELS = -1;
    /**
     * Maximum number of CCG-style labels to extract for each span in SAMT.
     * CCG-style labels do not correspond to a syntactic constituent, but
     * rather encode symbols that are missing particular parts on either left
     * or right.
     */
    public static int MAX_CCG_LABELS = -1;
    /**
     * Maximum number of concatenated labels to extract for each span in SAMT.
     */
    public static int MAX_CAT_LABELS = -1;

    /**
     * Number of threads of execution.
     */
    public static int THREADS = 1;

    /**
     * Default nonterminal symbol. This is the only NT in Hiero grammars, and
     * it is used when no other symbol is possible in an SAMT grammar.
     */
    public static String DEFAULT_NT = "X";

    /**
     * Whether or not to use hadoop (open-source implementation of MapReduce).
     */
    public static boolean HADOOP = false;
    // put hadoop options here

    /**
     * Whether or not the parallel corpus is in one file.
     */
    public static boolean UNIFIED_INPUT = false;
    /**
     * The path to the unified input file.
     */
    public static String INPUT_FILE = "";

    /**
     * Sets the various static configuration variables by reading them from
     * a configuration file.
     *
     * @param filename path to the config file
     * @throws IOException if the given config file cannot be read
     */
    public static void configure(String filename) throws IOException
    {
        Map<String,String> configMap = ConfFileParser.parse(filename);
        for (String key : configMap.keySet()) {
            String value = configMap.get(key);

            if ("grammar".equals(key)) {
                GRAMMAR = value.toLowerCase();
            }
            else if ("alignment".equals(key)) {
                ALIGNMENT = value;
            }
            else if ("alignment-format".equals(key)) {
                ALIGNMENT_FORMAT = value.toLowerCase();
            }
            else if ("parse".equals(key)) {
                PARSE = value;
            }
            else if ("parse-format".equals(key)) {
                PARSE_FORMAT = value.toLowerCase();
            }
            else if ("source".equals(key)) {
                SOURCE = value;
            }
            else if ("target".equals(key)) {
                TARGET = value;
            }
            else if ("output-format".equals(key)) {
                OUTPUT_FORMAT = value.toLowerCase();
            }
            else if ("adjacent".equals(key)) {
                ADJACENT = Boolean.parseBoolean(value);
            }
            else if ("loose".equals(key)) {
                LOOSE = Boolean.parseBoolean(value);
            }
            else if ("initial-phrase-length".equals(key)) {
                INITIAL_PHRASE_LIMIT = Integer.parseInt(value);
            }
            else if ("rule-source-length".equals(key)) {
                SOURCE_LENGTH_LIMIT = Integer.parseInt(value);
            }
            else if ("arity".equals(key)) {
                ARITY = Integer.parseInt(value);
            }
            else if ("lexicality".equals(key)) {
                LEXICALITY = Integer.parseInt(value);
            }
            else if ("adjacent-nts".equals(key)) {
                ADJACENT = Boolean.parseBoolean(value);
            }
            else if ("loose".equals(key)) {
                LOOSE = Boolean.parseBoolean(value);
            }
            else if ("features".equals(key)) {
                FEATURES = value.toLowerCase();
            }
            else if ("max-constituent-labels".equals(key)) {
                MAX_CONSTITUENT_LABELS = Integer.parseInt(value);
            }
            else if ("max-ccg-labels".equals(key)) {
                MAX_CCG_LABELS = Integer.parseInt(value);
            }
            else if ("max-cat-labels".equals(key)) {
                MAX_CAT_LABELS = Integer.parseInt(value);
            }
            else if ("default-nt".equals(key)) {
                DEFAULT_NT = value;
            }
            else if ("threads".equals(key)) {
                THREADS = Integer.parseInt(value);
            }
            else if ("hadoop".equals(key)) {
                HADOOP = Boolean.parseBoolean(value);
            }
            else if ("unified-input".equals(key)) {
                UNIFIED_INPUT = Boolean.parseBoolean(value);
            }
            else if ("input-file".equals(key)) {
                INPUT_FILE = value;
            }
        }
    }

    /**
     * Sets the verbosity level.
     *
     * @param level a String describing the verbosity level
     */
    public static void setVerbosity(String level)
    {
        if ("verbose".equals(level)) {
            verbosity = 1;
        }
        else if ("debug".equals(level)) {
            verbosity = 2;
        }
        else if ("quiet".equals(level)) {
            verbosity = -1;
        }
    }
}
