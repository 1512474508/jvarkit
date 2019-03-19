package com.github.lindenb.jvarkit.util.jcommander;

import com.github.lindenb.jvarkit.io.IOUtilsTest;
import com.github.lindenb.jvarkit.lang.SmartComparatorTest;
import com.github.lindenb.jvarkit.lang.StringUtilsTest;
import com.github.lindenb.jvarkit.tools.tests.AlsoTest;
import com.github.lindenb.jvarkit.util.CounterTest;
import com.github.lindenb.jvarkit.util.bio.IntervalParserTest;
import com.github.lindenb.jvarkit.util.bio.bed.BedLineCodecTest;
import com.github.lindenb.jvarkit.util.log.ProgressFactoryTest;
import com.github.lindenb.jvarkit.util.samtools.ContigDictComparatorTest;
import com.github.lindenb.jvarkit.util.vcf.VCFUtilsTest;

@AlsoTest({IOUtilsTest.class,VCFUtilsTest.class,StringUtilsTest.class,IntervalParserTest.class,CounterTest.class,BedLineCodecTest.class,
	ProgressFactoryTest.class,ContigDictComparatorTest.class,SmartComparatorTest.class})
public class LauncherTest {

}
