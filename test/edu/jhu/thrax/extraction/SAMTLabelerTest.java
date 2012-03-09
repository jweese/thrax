package edu.jhu.thrax.extraction;

import org.testng.annotations.Test;
import org.testng.Assert;

public class SAMTLabelerTest
{
	@Test
	public void getLabel_MalformedTree_isDefault()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A b))", true, true, true, true, "top", "X");
		Assert.assertEquals(labeler.getLabel(0, 1), "X");
	}

	@Test
	public void getLabel_SpanOutOfBounds_isDefault()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A b)", true, true, true, true, "top", "X");
		Assert.assertEquals(labeler.getLabel(0, 3), "X");
		Assert.assertEquals(labeler.getLabel(-2, 1), "X");
	}

	@Test
	public void getLabel_UnaryChain_Top()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c))", true, true, true, true, "top", "X");
		Assert.assertEquals(labeler.getLabel(0, 1), "A");
	}

	@Test
	public void getLabel_UnaryChain_Bottom()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c))", true, true, true, true, "bottom", "X");
		Assert.assertEquals(labeler.getLabel(0, 1), "B");
	}

	@Test
	public void getLabel_UnaryChain_All()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c))", true, true, true, true, "all", "X");
		Assert.assertEquals(labeler.getLabel(0, 1), "A:B");
	}

	@Test
	public void getLabel_NoConst_returnCat()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c) (D e))", false, true, true, true, "all", "X");
		Assert.assertEquals(labeler.getLabel(0, 2), "B+D");
	}

	@Test
	public void getLabel_NoConstCat_noCCG_returnDefault()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c) (D e))", false, true, false, true, "all", "X");
		Assert.assertEquals(labeler.getLabel(0, 2), "X");
	}

	@Test
	public void getLabel_NoConstCat_returnCCG()
	{
		SAMTLabeler labeler = new SAMTLabeler("(A (B c) (D e))", false, true, false, true, "all", "X");
		Assert.assertEquals(labeler.getLabel(0, 1), "A/D");
	}
}

