package edu.jhu.thrax.syntax;

import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.Iterator;

public class ParseTreeTest
{
	@Test
	public void numLeaves_Leaf_isOne()
	{
		ParseTree pt = ParseTree.fromPennFormat("a");
		Assert.assertEquals(pt.numLeaves(), 1);
	}

	@Test
	public void numNodes_Leaf_isOne()
	{
		ParseTree pt = ParseTree.fromPennFormat("a");
		Assert.assertEquals(pt.numNodes(), 1);
	}

	@Test
	public void numLeaves_Tree()
	{
		ParseTree pt = ParseTree.fromPennFormat("(A (B c d))");
		Assert.assertEquals(pt.numLeaves(), 2);
	}

	@Test
	public void numNodes_Tree()
	{
		ParseTree pt = ParseTree.fromPennFormat("(A (B c d))");
		Assert.assertEquals(pt.numNodes(), 4);
	}
}

