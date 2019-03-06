package com.github.lindenb.jvarkit.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.lindenb.jvarkit.tools.tests.AlsoTest;
import com.github.lindenb.jvarkit.tools.tests.TestSupport;
import com.github.lindenb.jvarkit.util.iterator.LineIteratorTest;

@AlsoTest(LineIteratorTest.class)
public class IOUtilsTest {
	private final TestSupport support = new TestSupport();
	
	private void write(OutputStream os) throws IOException {
		for(int i=0;i<10;i++) os.write((byte)'A');
	}
	
	private void read(InputStream is) throws IOException {
		for(int i=0;i<10;i++) Assert.assertEquals((int)'A', is.read());
		Assert.assertEquals(-1, is.read());
	}
	
@Test
void testWrite() throws IOException {
	try {
		Path p = support.createTmpPath(".vcf.gz");
		OutputStream os = IOUtils.openPathForWriting(p);
		write(os);
		os.close();
		
		InputStream in = new GZIPInputStream(Files.newInputStream(p));
		read(in);
		in.close();
		
		in = IOUtils.openPathForReading(p);
		in.close();
		
		p = support.createTmpPath(".txt.gz");
		os = IOUtils.openPathForWriting(p);
		write(os);
		os.close();
		
		in = new GZIPInputStream(Files.newInputStream(p));
		in.close();
		in = IOUtils.openPathForReading(p);
		read(in);
		in.close();
		
		p = support.createTmpPath(".txt");
		os = IOUtils.openPathForWriting(p);
		write(os);
		os.close();
		
		in = Files.newInputStream(p);
		read(in);
		in.close();
		in = IOUtils.openPathForReading(p);
		read(in);
		in.close();
		os.close();
		
		p = support.createTmpPath(".bz2");
		os = IOUtils.openPathForWriting(p);
		write(os);
		os.close();
		
		in =  new BZip2CompressorInputStream(Files.newInputStream(p));
		read(in);
		in.close();
		in = IOUtils.openPathForReading(p);
		read(in);
		in.close();
		
		
	} finally {
		support.removeTmpFiles();
	}
	}
}
