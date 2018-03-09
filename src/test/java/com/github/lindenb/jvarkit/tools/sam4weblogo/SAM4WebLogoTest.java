package com.github.lindenb.jvarkit.tools.sam4weblogo;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.lindenb.jvarkit.tools.tests.TestUtils;

import htsjdk.samtools.util.Interval;


public class SAM4WebLogoTest extends TestUtils
	{
	@DataProvider(name = "src1")
	public Object[][] createData1() {
		return new ParamCombiner().
			initList(collectIndexedBams()).
			build();
		}
	private void basetest(final String inBam,String params) throws IOException {
		final File out = createTmpFile(".txt");
		final Interval interval = super.randomIntervalsFromDict(new File(inBam), 1).get(0);
		Assert.assertEquals(new SAM4WebLogo().instanceMain(newCmd().add(
				"-o",out.getPath(),
				"-r",interval.getContig()+":"+interval.getStart()+"-"+interval.getEnd()).
				split(params).
				add(inBam).
				make()),0);
		assertIsNotEmpty(out);
		}
	
	@Test(dataProvider="src1")
	public void testNoClip(final String inBam) throws IOException {
		basetest(inBam,"");
		}
	@Test(dataProvider="src1")
	public void testClip(final String inBam) throws IOException {
		basetest(inBam,"-c");
		}
	}
