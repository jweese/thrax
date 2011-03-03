package edu.jhu.thrax.util.io;

import java.util.ArrayList;
import edu.jhu.thrax.util.exceptions.*;

/**
 * Methods for validating user input. These should be used everywher user
 * input is received.
 */
public class InputUtilities
{
    /**
     * Returns an array of the leaves of a parse tree, reading left to right.
     *
     * @param parse a representation of a parse tree (Penn treebank style)
     * @return an array of String giving the labels of the tree's leaves
     * @throws MalformedParseException if the parse tree is not well-formed
     */
    public static String [] parseYield(String parse) throws MalformedParseException
    {
        if (!parse.startsWith("("))
            throw new MalformedParseException(parse);
        int level = 0;
        boolean expectNT = false;

        ArrayList<String> result = new ArrayList<String>();
        String [] tokens = parse.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim().split("\\s+");
        for (String t : tokens) {
            if ("(".equals(t)) {
                level++;
                expectNT = true;
                continue;
            }
            if (")".equals(t)) {
                if (level == 0)
                    throw new MalformedParseException(parse);
                level--;
            }
            else if (!expectNT)
                result.add(t);
            expectNT = false;
        }
        if (level != 0)
            throw new MalformedParseException(parse);
        return result.toArray(new String[result.size()]);
    }
}

