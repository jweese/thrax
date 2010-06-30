package edu.jhu.thrax;

import edu.jhu.thrax.util.ConfFileParser;

import java.util.Map;
import java.io.IOException;

/**
 * This class reads configuration options from several sources and aggregates
 * them into a single <code>HashMap</code> that can be read from other parts
 * of Thrax. Specifically, it uses the <code>GetOpt</code> class to read
 * command-line options, and also can read options from a configuration file.
 */
public class ThraxConfig {

    /**
     * Determines the verbosity level. 0 is the normal level, which is
     * quiet. 1 means verbose, and 2 includes debugging information.
     */
    public static int verbosity = 0;

    public static String GRAMMAR = "hiero";
    public static String ALIGNMENT = "";
    public static String ALIGNMENT_FORMAT = "berkeley";
    public static String PARSE = "";
    public static String PARSE_FORMAT = "ptb";
    public static String SOURCE = "";
    public static String TARGET = "";
    public static String OUTPUT_FORMAT = "joshua";

    public static boolean ADJACENT = false;
    public static boolean LOOSE = false;
    public static int INITIAL_PHRASE_LIMIT = 10;
    public static int SOURCE_LENGTH_LIMIT = 5;
    public static int ARITY = 2;
    public static int LEXICALITY = 1;

    public static String FEATURES = "";

    public static int MAX_CONSTITUENT_LABELS = -1;
    public static int MAX_CCG_LABELS = -1;
    public static int MAX_CAT_LABELS = -1;

    public static void configure(String filename) throws IOException
    {
        Map<String,String> configMap = ConfFileParser.parse(filename);
        for (String key : configMap.keySet()) {
            String value = configMap.get(key);

            if ("grammar".equals(key)) {
                GRAMMAR = value;
            }
            else if ("alignment".equals(key)) {
                ALIGNMENT = value;
            }
            else if ("alignment-format".equals(key)) {
                ALIGNMENT_FORMAT = value;
            }
            else if ("parse".equals(key)) {
                PARSE = value;
            }
            else if ("parse-format".equals(key)) {
                PARSE_FORMAT = value;
            }
            else if ("source".equals(key)) {
                SOURCE = value;
            }
            else if ("target".equals(key)) {
                TARGET = value;
            }
            else if ("output-format".equals(key)) {
                OUTPUT_FORMAT = value;
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
                FEATURES = value;
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
        }
    }

}
