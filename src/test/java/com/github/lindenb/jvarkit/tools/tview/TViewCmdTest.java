package com.github.lindenb.jvarkit.tools.tview;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.lindenb.jvarkit.tools.tests.TestUtils;

public class TViewCmdTest  extends TestUtils {
	@DataProvider(name="rf_regions")
	public Object[][] getDataRegions() throws IOException  {
		return randomIntervalsFromDict(new File(SRC_TEST_RESOURCE+"/rotavirus_rf.dict"),10).
				stream().
				map(I->I.getContig()+":"+I.getStart()+"-"+I.getEnd()).
				map(S->new Object[]{S}).
				toArray(x->new Object[x][])
				;
		}
		
	@Test(dataProvider="rf_regions")
	public void test01(final String rgn) throws IOException {
		final File imgOut = super.createTmpFile(".txt");
		Assert.assertEquals(new  TViewCmd().instanceMain(newCmd().add(
				"-V",SRC_TEST_RESOURCE+"/rotavirus_rf.vcf.gz",
				"-R",SRC_TEST_RESOURCE+"/rotavirus_rf.fa",
				"-r",rgn,
				"-o",imgOut).
				add(Arrays.asList("1","2","3","4","5").stream().
					map(S->SRC_TEST_RESOURCE+"/S"+S+".bam").
					toArray()).
				make()
				),0);
		assertIsNotEmpty(imgOut);
		}

}
