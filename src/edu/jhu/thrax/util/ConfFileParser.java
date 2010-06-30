package edu.jhu.thrax.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import java.io.File;
import java.io.IOException;

/**
 * This class parses conf files of a standard format. The '#' character is used
 * to indicate comments, and non-comment lines have a key and a value separated
 * by whitespace.
 */
public class ConfFileParser {

    public static Map<String,String> parse(String filename) throws IOException
    {
        Map<String,String> opts = new HashMap<String,String>();
        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // strip comments
            if (line.indexOf("#") != -1) {
                line = line.substring(0, line.indexOf("#")).trim();
            }
            if ("".equals(line))
                continue;

            String [] keyVal = line.split("\\s+", 2);
            if (keyVal.length > 1)
                opts.put(keyVal[0], keyVal[1]);
        }
        return opts;
    }
}

