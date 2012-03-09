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
}

