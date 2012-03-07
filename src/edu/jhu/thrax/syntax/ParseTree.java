package edu.jhu.thrax.syntax;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.EmptyStackException;
import java.util.Arrays;
import java.util.Collections;

import java.io.IOException;
import java.util.Scanner;

public class ParseTree
{
	private final String [] labels;
	private final int [] numChildren;
	private final int [] start;
	private final int [] end;

	private ParseTree(String [] ls, int [] cs, int [] ss, int [] es)
	{
		labels = ls;
		numChildren = cs;
		start = ss;
		end = es;
	}

	public static ParseTree fromPennFormat(String s)
	{
		List<String> ls = new ArrayList<String>();
		List<Integer> cs = new ArrayList<Integer>();
		List<Integer> ss = new ArrayList<Integer>();
		List<Integer> es = new ArrayList<Integer>();
		try {
			buildLists(ls, cs, ss, es, s);
		}
		catch (EmptyStackException e) {
			return null;
		}
		int size = ls.size();
		String [] labels = new String[size];
		ls.toArray(labels);
		int [] numChildren = toIntArray(cs);
		int [] start = toIntArray(ss);
		int [] end = toIntArray(es);
		return new ParseTree(labels, numChildren, start, end);
	}

	private static int [] toIntArray(List<Integer> list)
	{
		int [] result = new int[list.size()];
		for (int i = 0; i < result.length; i++)
			result[i] = list.get(i);
		return result;
	}

	private static void addToken(List<String> ls, List<Integer> cs, List<Integer> ss, List<Integer> es, String label, int start, int end)
	{
		ls.add(label);
		cs.add(0);
		ss.add(start);
		es.add(end);
	}

	private static void buildLists(List<String> ls, List<Integer> cs, List<Integer> ss, List<Integer> es, String line)
	{
		int start = 0;
		Stack<Integer> ancestors = new Stack<Integer>();
        String[] tokens = line.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim().split("\\s+");
		boolean nextNT = false;
		for (String t : tokens) {
			if (t.equals("(")) {
				nextNT = true;
				continue;
			}
			if (t.equals(")")) {
				int x = ancestors.pop();
				continue;
			}
			if (nextNT) {
				nextNT = false;
				addToken(ls, cs, ss, es, t, start, start);
				if (!ancestors.empty())
					increment(cs, ancestors.peek());
				ancestors.push(ls.size() - 1);
			}
			else {
				addToken(ls, cs, ss, es, t, start, start + 1);
				for (int i : ancestors)
					increment(es, i);
				if (!ancestors.empty())
					increment(cs, ancestors.peek());
			}
		}
	}

	private static void increment(List<Integer> list, int index)
	{
		list.set(index, list.get(index) + 1);
	}

	public Node root()
	{
		return new Node(0);
	}

	public List<Node> nodesWithSpan(int from, int to)
	{
		int index = firstIndexOf(start, from);
		if (index < 0)
			return Collections.<Node>emptyList();
		List<Node> result = new ArrayList<Node>();
		while (start[index] == from && end[index] <= to) {
			if (end[index] == to)
				result.add(new Node(index));
		}
		return result;
	}

	private static int firstIndexOf(int [] array, int key)
	{
		int result = Arrays.binarySearch(array, key);
		if (result < 0)
			return result;
		while (result >= 0 && array[result] == key)
			result--;
		return result + 1;
	}

	public String toString()
	{
		return root().toString();
	}

	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof ParseTree))
			return false;
		ParseTree other = (ParseTree) o;
		return Arrays.equals(labels, other.labels)
			&& Arrays.equals(numChildren, other.numChildren)
			&& Arrays.equals(start, other.start)
			&& Arrays.equals(end, other.end);
	}

	public int hashCode()
	{
		int result = 163;
		result = result * 37 + Arrays.hashCode(labels);
		result = result * 37 + Arrays.hashCode(numChildren);
		result = result * 37 + Arrays.hashCode(start);
		result = result * 37 + Arrays.hashCode(end);
		return result;
	}

	private class Node
	{
		private final int index;

		public Node(int i)
		{
			index = i;
		}

		public String label()
		{
			return labels[index];
		}

		public int numChildren()
		{
			return numChildren[index];
		}

		public int spanStart()
		{
			return start[index];
		}

		public int spanEnd()
		{
			return end[index];
		}

		public Iterator<Node> children()
		{
			return new ChildIterator(index);
		}

		public String toString()
		{
			if (isLeaf())
				return label();
			String result = String.format("(%s", label());
			Iterator<Node> children = children();
			while (children.hasNext())
				result += " " + children.next().toString();
			result += ")";
			return result;
		}

		public boolean isLeaf()
		{
			return numChildren() == 0;
		}
	}

	private class ChildIterator implements Iterator<Node>
	{
		private final int index;
		private final int totalChildren;
		private int childrenSeen;
		private int childIndex;

		public ChildIterator(int i)
		{
			index = i;
			totalChildren = numChildren[index];
			childrenSeen = 0;
			childIndex = index + 1;
		}

		public boolean hasNext()
		{
			return childrenSeen < totalChildren;
		}

		public Node next()
		{
			Node result = new Node(childIndex);
			childIndex = nextSiblingIndex(childIndex);
			childrenSeen++;
			return result;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private int nextSiblingIndex(int i)
		{
			int result = i + 1;
			int children = numChildren[i];
			for (int j = 0; j < children; j++)
				result = nextSiblingIndex(result);
			return result;
		}
	}

	public static void main(String [] argv) throws IOException
	{
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			ParseTree tree = ParseTree.fromPennFormat(scanner.nextLine());
			System.out.printf("%s\t%d\n", tree, tree.hashCode());
		}
	}
}

