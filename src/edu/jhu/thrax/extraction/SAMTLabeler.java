package edu.jhu.thrax.extraction;

import java.util.List;

import edu.jhu.thrax.syntax.ParseTree;

import org.apache.hadoop.conf.Configuration;

public class SAMTLabeler implements SpanLabeler {

    private boolean TARGET_IS_SAMT_SYNTAX = true;
    private boolean ALLOW_CONSTITUENT_LABEL = true;
    private boolean ALLOW_CCG_LABEL = true;
    private boolean ALLOW_CONCAT_LABEL = true;
    private boolean ALLOW_DOUBLE_CONCAT = true;
    private UnaryCategoryHandler unaryCategoryHandler = UnaryCategoryHandler.ALL;

	private ParseTree tree;
	private String defaultLabel;

    public SAMTLabeler(Configuration conf, String parse)
    {
        TARGET_IS_SAMT_SYNTAX = conf.getBoolean("thrax.target-is-samt-syntax", true);
        ALLOW_CONSTITUENT_LABEL = conf.getBoolean("thrax.allow-constituent-label", true);
        ALLOW_CCG_LABEL = conf.getBoolean("thrax.allow-ccg-label", true);
        ALLOW_CONCAT_LABEL = conf.getBoolean("thrax.allow-concat-label", true);
        ALLOW_DOUBLE_CONCAT = conf.getBoolean("thrax.allow-double-plus", true);
		defaultLabel = conf.get("thrax.default-nt", "X");
		tree = ParseTree.fromPennFormat(parse);
		if (tree == null)
			System.err.printf("WARNING: SAMT labeler: %s is not a parse tree\n", parse);
    }

	public String getLabel(int from, int to)
	{
		if (tree == null)
			return defaultLabel;
		String label;
		if (ALLOW_CONSTITUENT_LABEL) {
			label = constituentLabel(from, to);
			if (label != null)
				return label;
		}
		if (ALLOW_CONCAT_LABEL) {
			label = concatenatedLabel(from, to);
			if (label != null)
				return label;
		}
		if (ALLOW_CCG_LABEL) {
			label = forwardSlashLabel(from, to);
			if (label != null)
				return label;
			label = backwardSlashLabel(from, to);
			if (label != null)
				return label;
		}
		if (ALLOW_DOUBLE_CONCAT) {
			label = doubleConcatenatedLabel(from, to);
			if (label != null)
				return label;
		}
		return null;
	}

	private String constituentLabel(int from, int to)
	{
		List<ParseTree.Node> nodes = tree.internalNodesWithSpan(from, to);
		if (nodes.isEmpty())
			return null;
		switch (unaryCategoryHandler) {
		case TOP:
			return nodes.get(0).label();
		case BOTTOM:
			return nodes.get(nodes.size() - 1).label();
		case ALL:
			String result = nodes.get(0).label();
			for (int i = 1; i < nodes.size(); i++)
				result += ":" + nodes.get(i).label();
			return result;
		}
		return null;
	}

	private String concatenatedLabel(int from, int to)
	{
		for (int mid = from + 1; mid < to; mid++) {
			String a = constituentLabel(from, mid);
			String b = constituentLabel(mid, to);
			if (a != null && b != null)
				return a + "+" + b;
		}
		return null;
	}

	private String forwardSlashLabel(int from, int to)
	{
		for (int end = to + 1; end <= tree.numLeaves(); end++) {
			String a = constituentLabel(from, end);
			String b = constituentLabel(to, end);
			if (a != null && b != null)
				return a + "/" + b;
		}
		return null;
	}

	private String backwardSlashLabel(int from, int to)
	{
		for (int start = from - 1; start >= 0; start--) {
			String a = constituentLabel(start, to);
			String b = constituentLabel(start, from);
			if (a != null && b != null)
				return a + "\\" + b;
		}
		return null;
	}

	private String doubleConcatenatedLabel(int from, int to)
	{
		for (int mid1 = from + 1; mid1 < to - 1; mid1++) {
			for (int mid2 = mid1 + 1; mid2 < to; mid2++) {
				String a = constituentLabel(from, mid1);
				String b = constituentLabel(mid1, mid2);
				String c = constituentLabel(mid2, to);
				if (a != null && b != null && c != null)
					return a + "+" + b + "+" + c;
			}
		}
		return null;
	}

	public enum UnaryCategoryHandler
	{
		TOP, BOTTOM, ALL;

		public static UnaryCategoryHandler fromString(String s)
		{
			if (s.equalsIgnoreCase("top"))
				return TOP;
			else if (s.equalsIgnoreCase("bottom"))
				return BOTTOM;
			else
				return ALL;
		}
	}
}

