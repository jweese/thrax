package edu.jhu.thrax.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import java.net.URI;

/**
 * This class parses conf files of a standard format. The '#' character is used
 * to indicate comments, and non-comment lines have a key and a value separated
 * by whitespace.
 */
public class ConfFileParser {

    public static Map<String,String> parse(String confName)
    {
        Map<String,String> opts = new HashMap<String,String>();
        Scanner scanner;
        
        try {
        	scanner = new Scanner(ConfigFileLoader.getConfigStream(new URI(confName)));
        } catch (Exception e) {
        	throw new IllegalArgumentException(e.toString());
        }
        
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
                opts.put(keyVal[0].trim(), keyVal[1].trim());
        }
        return opts;
    }
}

