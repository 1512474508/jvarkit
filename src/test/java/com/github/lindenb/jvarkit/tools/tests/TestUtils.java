package com.github.lindenb.jvarkit.tools.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.helpers.DefaultHandler;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.bio.bed.BedLineCodec;
import com.github.lindenb.jvarkit.util.ncbi.NcbiApiKey;

import htsjdk.samtools.BAMIndex;
import htsjdk.samtools.DefaultSAMRecordFactory;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordFactory;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceDictionaryCodec;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamFiles;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.variant.utils.SAMSequenceDictionaryExtractor;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;


public class TestUtils {
	protected final String SRC_TEST_RESOURCE="./src/test/resources";
	private final List<Path> deletePathsAtExit = new Vector<>();
	private final List<File> deleteFilesAtExit = new Vector<>();
	protected final Random random = new Random();
	protected final NcbiApiKey ncbiApiKey = new NcbiApiKey();
	
	
private static class FastaCreator
	{
	private final Random random = new Random();
	
	private int nContigs = 3;
	private int minLen = 100;
	private int maxLen = 10000;
	private boolean chrPrefix=false;
	public File build() throws IOException {
		File outFile = File.createTempFile("tmp.", ".fa");
		PrintWriter pw= new PrintWriter(outFile);
		SAMSequenceDictionary dict=new SAMSequenceDictionary();
		for(int i=0;i< this.nContigs;i++)
			{
			int len = minLen + random.nextInt(maxLen-minLen);
			final String name=(chrPrefix?"chr":"")+(i+1);
			final SAMSequenceRecord ssr = new SAMSequenceRecord(name, len);
			dict.addSequence(ssr);
			pw.write(">"+name);
			for(int x=0;x<len;++x) 
				{
				if(x%100==0) pw.write('\n');
				final char base;
				switch(random.nextInt(4)) {
					case 0: base='A';break;
					case 1: base='C';break;
					case 2: base='G';break;
					case 3: base='T';break;
					default: base='N';break;
					}
				pw.write(base);
				}
			pw.write('\n');
			}
		pw.flush();
		pw.close();
		FastaSequenceIndexCreator.buildFromFasta(outFile.toPath());
		ReferenceSequenceFileFactory.getFastaIndexFileName(outFile.toPath()).toFile().deleteOnExit();
		File dictFile = new File(outFile.getParentFile(),outFile.getName()+".dict");
		dictFile.deleteOnExit();
		BufferedWriter bw = new BufferedWriter(new PrintWriter(dictFile));
		SAMSequenceDictionaryCodec codec=  new SAMSequenceDictionaryCodec(bw);
		codec.encode(dict);
		bw.flush();
		bw.close();
		return outFile;
		}
	}	
	
private static class BamCreator
	{
	private final Random random = new Random();
	private File ref= null;
	private SortOrder sortOrder = SortOrder.coordinate;
	private int nRecords=10000;
	
	public File build() throws IOException {
		

		SAMSequenceDictionary dict=new SAMSequenceDictionary();
		SAMFileWriterFactory swf = new SAMFileWriterFactory();
		File outFile = File.createTempFile("tmp.", ".bam");
		outFile.deleteOnExit();
		SAMFileHeader header = new SAMFileHeader(dict);
		header.setSortOrder(this.sortOrder);
		
		SAMFileWriter sfw = swf.makeBAMWriter(header, false, outFile);
		for(int i=0;i< nRecords;i++)
			{
			
			}
		sfw.close();
		
		return outFile;
		}
	}
	

protected static class CommandBuilder
	{
	private final List<String> args = new ArrayList<>();
	
	
	public CommandBuilder addIf(boolean b,final Object...ss) {
		if(b) add(ss);
		return this;
		}
	
	public CommandBuilder add(final Object...ss) {
		for(final Object s :ss) this.args.add(String.valueOf(s));
		return this;
		}
	public CommandBuilder addAll(final Collection<?> ss) {
		for(final Object s :ss) this.args.add(String.valueOf(s));
		return this;
		}
	public CommandBuilder split(final String ss) {
		for(final String s: ss.split("[ \t]+")) {
			if(s.isEmpty()) continue;
			this.args.add(s);
			}
		return this;
		}
	public CommandBuilder dump() {
		for(int i=0;i< args.size();++i)
		{
			System.err.println("["+(i+1)+"] \""+args.get(i)+"\"");
		}
		return this;
	}
	public String[] make() {
		return args.toArray(new String[args.size()]);
		}
	}

/** create a new CommandBuilder */
static protected CommandBuilder newCmd() {
	return new CommandBuilder();
}

protected static class ParamCombiner
	{
	private List<List<Object>> data = new ArrayList<>();
	
	
	public ParamCombiner() {
		
	}
	
	public ParamCombiner initList(Object list[]) {
		this.data.clear();
		for(final Object o:list)
			{
			List<Object> row = new ArrayList<>(1);
			row.add(o);
			this.data.add(row);
			}
		return this;
		}
	
	ParamCombiner append(Object right)
		{
		for(int i=0;i< this.data.size();i++)
			{
			this.data.get(i).add(right);
			}
		return this;
		}
	
	public ParamCombiner when(Function<List<Object>, Object> fun)
		{
		for(int i=0;i< this.data.size();i++)
			{
			this.data.get(i).add(fun.apply(this.data.get(i)));
			}
		return this;
		}
	
	public ParamCombiner filter(Predicate<List<Object>> pred)
		{
		this.data.removeIf(pred);
		return this;
		}
	
	public ParamCombiner product(Object...right)
		{
		final List<List<Object>> data2 = new ArrayList<>();
		
		for(int i=0;i< this.data.size();i++)
			{
			final List<Object> datarow = this.data.get(i);
			for(int j=0;j< right.length;j++)
				{
				final List<Object> row2 = new ArrayList<>(datarow);
				row2.add(right[j]);
				data2.add(row2);
				}
			}
		this.data.clear();
		this.data.addAll(data2);
		return this;
		}
	
	public Object[][] build() {
		final Object[][] table = new Object[this.data.size()][];
		for(int i=0;i< this.data.size();i++)
			{
			table[i] = this.data.get(i).toArray(new Object[this.data.get(i).size()]);
			}
		return table;
		}
	}
	
protected List<File> _collectFiles(final File dir,final FilenameFilter filter) {
	List<File> list = new ArrayList<>();
	if(dir==null || !dir.isDirectory()) return list;
	File child[] = dir.listFiles(filter);
	if(child==null || child.length==0) return list;
	for(File c : child) {
		if(c==null) continue;
		if(c.isDirectory())
			{
			list.addAll(_collectFiles(c,filter));
			continue;
			}
		if(!c.canRead() ) continue;
		if(!c.isFile()) continue;
		
		if(filter.accept(dir, c.getName())) {
			list.add(c);
			}
		}
	return list;
	}


@DataProvider(name = "genomic-segments")
public Object[][] createSomeGenomicSegments() {
	return new Object[][] {
		{"1",43_996_511,"CGCGAGCGCGAGGGGAGCGCGCGGCTGGAGCTGGCGCGGGAGCGGCGGGAGCGGTGGCGGCGGCAGAGGCGGCGGCTCCAGCTTCGGCTCC"},
		{"22",41_756_126,"CTAGAAAATAAACTTGTGCACTTTGACCTCTGTCCCCGAGATGT"},
		{"MT",5_881,"AGCCATTTTACCTCACCCCCACTGATGTTC"}
	};
	}

@DataProvider(name = "all-vcf-files")
public Object[][] createAllVcfsData() {
	return new ParamCombiner().
		initList(collectAllVcfs()).
		build();
		}
@DataProvider(name = "all-indexed-vcf-files")
public Object[][] createAllIndexedVcfsData() {
	return new ParamCombiner().
		initList(collectIndexedVcf()).
		build();
		}


@DataProvider(name = "all-sam-or-bam-files")
public Object[][] createAllSamOrBamData() {
	return new ParamCombiner().
		initList(collectAllSamOrBam()).
		build();
		}

@DataProvider(name = "all-one-bam-and-ref")
public Object[][] createOneBamAndRefData() {
	return new Object[][] {
		{SRC_TEST_RESOURCE +"/toy.bam",SRC_TEST_RESOURCE+"/toy.fa"},
		{SRC_TEST_RESOURCE +"/S1.bam",SRC_TEST_RESOURCE+"/rotavirus_rf.fa"},
		{SRC_TEST_RESOURCE +"/S2.bam",SRC_TEST_RESOURCE+"/rotavirus_rf.fa"},
		{SRC_TEST_RESOURCE +"/S3.bam",SRC_TEST_RESOURCE+"/rotavirus_rf.fa"},
		{SRC_TEST_RESOURCE +"/S4.bam",SRC_TEST_RESOURCE+"/rotavirus_rf.fa"},
		{SRC_TEST_RESOURCE +"/S5.bam",SRC_TEST_RESOURCE+"/rotavirus_rf.fa"}
	};
}

@DataProvider(name = "all-dictionaries")
public Object[][] createAllDictData() {
	return new ParamCombiner().
		initList(_collectAllFiles((D,F)->F.endsWith(".dict"))).
		build();
		}


protected Object[] _collectAllFiles( final FilenameFilter filter) {
	return _collectFiles(new File(SRC_TEST_RESOURCE),filter).
			stream().
			map(F->(Object)F.getPath()).
			toArray(i->new Object[i]);
		}

protected Object[] collectAllVcfs() {
	return _collectAllFiles((D,N)->N.endsWith(".vcf") || N.endsWith(".vcf.gz"));
	}

protected Object[] collectAllSamOrBam() {
	return _collectAllFiles((D,N)->N.endsWith(".sam") || N.endsWith(".bam"));
	}

protected Object[] collectIndexedBams() {
	return _collectAllFiles((D,N)->{
		if(!N.endsWith(".bam")) return false; 
		if(SamFiles.findIndex(Paths.get(D.getPath(),N))==null)
			{
			return false;
			}
		return true;
		});
	}

protected Object[] collectIndexedVcf() {
	return _collectAllFiles((D,N)->{
		if(N.endsWith(".vcf"))
			{
			return Files.exists(Paths.get(D.getPath(),N+".idx"));
			}
		if(N.endsWith(".vcf.gz"))
			{
			return Files.exists(Paths.get(D.getPath(),N+".tbi"));
			}
		return false;
		});
	}



protected Object[] collectAllFasta() {
	return _collectAllFiles((D,N)->N.endsWith(".fa") || N.endsWith(".fasta"));
	}
protected Object[] collectAllFastq() {
	return _collectAllFiles((D,N)->N.endsWith(".fq") || N.endsWith(".fq.gz") || N.endsWith(".fastq") || N.endsWith(".fastq.gz"));
	}
protected synchronized File createTmpFile(final String suffix) throws IOException {
	return deleteOnExit(File.createTempFile("tmp.", suffix));
	}


protected synchronized File deleteOnExit(final File f) {
	if(f!=null) this.deleteFilesAtExit.add(f);
	return f;
	}

protected synchronized Path deleteOnExit(final Path p) {
	if(p!=null) this.deletePathsAtExit.add(p);
	return p;
	}

@BeforeClass
public synchronized void startup() {
	System.err.println("[TEST] starting "+this.getClass().getSimpleName());
}

@AfterClass
public synchronized void removeTmpFiles() {
	for(final File f:this.deleteFilesAtExit) f.delete();
	for(final Path f:this.deletePathsAtExit) try {
		Files.delete(f);
		} catch(IOException err) {}
	System.err.println("[TEST] end "+this.getClass().getSimpleName());

	}

protected void assertIsValidBam(final File bamFile) throws IOException {
	final SamReader sr= SamReaderFactory.makeDefault().
			validationStringency(ValidationStringency.SILENT).
			open(bamFile);
	sr.iterator().stream().count();
	sr.close();
	}

protected void assertIsXml(final File f) {
	Exception err=null; 
	try {
		Assert.assertTrue(f.exists(), "file "+f+" should exist");
		final SAXParser sax=SAXParserFactory.newInstance().newSAXParser();
		sax.parse(f, new DefaultHandler());
		
	}catch (final Exception e) {
		err= e;
		}
	Assert.assertNull(err,"file "+f+" should be xml : "+err);
	}

protected void assertIsVcf(final File f) {
	Exception err=null; 
	try {
		variantStream(f).
			flatMap(V->V.getGenotypes().stream()).
			flatMap(G->G.getAlleles().stream());
		
	}catch (final Exception e) {
		err= e;
	}
	Assert.assertNull(err,"file "+f+" should be vcf : "+err);
}
protected void assertIsFastq(final File f) {
	Exception err=null; 
	FastqReader fqr =null;
	try {
		fqr = new FastqReader(f);
		Iterator<FastqRecord> iter =fqr.iterator();
		while(iter.hasNext()) iter.next();
	}catch (final Exception e) {
		err= e;
	} finally {
		CloserUtil.close(fqr);
	}
	Assert.assertNull(err,"file "+f+" should be fastq : "+err);
}


protected long assertIsBed(final File f) {
	Exception err=null;
	BufferedReader r = null;
	try {
		r = IOUtils.openFileForBufferedReading(f);
		final BedLineCodec codec = new BedLineCodec();
		return r.lines().map(L->codec.decode(L)).filter(B->B!=null).count();
	}catch (final Exception e) {
		err= e;
	} finally {
		CloserUtil.close(r);
	}
	Assert.assertNull(err,"file "+f+" should be fastq : "+err);
	return 0L;
}


protected void assertIsNotEmpty(final File f) throws IOException {
	Assert.assertNotNull(f,"File is null");
	Assert.assertFalse(f.length()==0L,"file "+f+" is empty");
	}

protected void assertIsImage(final File f) throws IOException {
	Assert.assertNotNull(f,"File is null");
	try {
		ImageIO.read(f);
		}
	catch(Throwable err)
		{
		Assert.fail(f.getPath()+" is not an image");
		}
	}


protected Stream<VariantContext> variantStream(final File vcfFile ) {
	final VCFFileReader r = new VCFFileReader(vcfFile,false);
	final CloseableIterator<VariantContext> iter = r.iterator();
	return iter.stream().onClose(()->{CloserUtil.close(iter);CloserUtil.close(r);});
	}
protected File createRandomPedigreeFromFile(final String vcfFile) throws IOException {
	class TmpSample {
		String fam="F1";
		String name;
		String father="0";
		String mother="0";
		int sex=0;
		int status=0;
		}
	final File vcfIn = new File(vcfFile);
	final VCFFileReader r= new VCFFileReader(vcfIn,false);
	final VCFHeader header=r.getFileHeader();
	r.close();
	final List<String> samples= new ArrayList<>(header.getSampleNamesInOrder());
	final List<TmpSample> ped= new ArrayList<>();
	if(samples.isEmpty()) return null;
	while(!samples.isEmpty())
		{
		final TmpSample indi = new TmpSample();
		indi.name = samples.remove(0);
		indi.fam = ped.stream().
				filter(P->P.father.equals(indi.name) || P.mother.equals(indi.name)).
				map(P->P.fam).findFirst().orElse("F"+this.random.nextInt()
				);
		if(ped.stream().anyMatch(P->P.father.equals(indi.name))) {
			indi.sex=1;
			}
		else if(ped.stream().anyMatch(P->P.mother.equals(indi.name))) {
			indi.sex=2;
			}
		else
			{
			indi.sex = this.random.nextBoolean()?1:2;
			}
		if(random.nextBoolean())
			{
			final List<String> remain=new ArrayList<>(samples);
			if(!remain.isEmpty())
				{
				indi.father = remain.remove(0);
				}
			else
				{
				indi.father = "0";
				}
			if(!remain.isEmpty())
				{
				indi.mother = remain.remove(0);
				}
			else
				{
				indi.mother = "0";
				}
			}
		else
			{
			indi.father = "0";
			indi.mother = "0";
			}
		
		indi.status = this.random.nextInt(2);
		ped.add(indi);
		}
	final File pedFile = createTmpFile(".ped");
	final PrintWriter pw  = new PrintWriter(pedFile);
	for(TmpSample ts:ped)
		{
		pw.print(ts.fam);
		pw.print('\t');
		pw.print(ts.name);
		pw.print('\t');
		pw.print(ts.father);
		pw.print('\t');
		pw.print(ts.mother);
		pw.print('\t');
		pw.print(ts.sex);
		pw.print('\t');
		pw.print(ts.status);
		pw.println();
		}
	pw.flush();
	pw.close();
	return pedFile;
	}

protected File sortBamOnQueryName(final Path bamFile,final Predicate<SAMRecord> pred) throws IOException {
	File sortedBam = this.createTmpFile(".bam");
	SamReader sr = SamReaderFactory.makeDefault().open(bamFile);
	SAMFileHeader outHeader= sr.getFileHeader().clone();
	outHeader.setSortOrder(SortOrder.queryname);
	SAMFileWriter w=new SAMFileWriterFactory().makeBAMWriter(outHeader, false, sortedBam);
	sr.iterator().stream().filter(R->pred==null?true:pred.test(R)).forEach(R->w.addAlignment(R));
	w.close();
	sr.close();
	return sortedBam;
	}
protected List<Interval> randomIntervalsFromDict(final File dictFile,int n) throws IOException{
	final SAMSequenceDictionary dict = SAMSequenceDictionaryExtractor.extractDictionary(dictFile);
	final List<Interval> rgns = new ArrayList<>(n);
	if(dict==null) return rgns;
	while(n>0)
		{
		final SAMSequenceRecord ssr = dict.getSequence(random.nextInt(dict.size()));
		final int L = 1 + random.nextInt( ssr.getSequenceLength()-1);
		final int start = 1 + random.nextInt(ssr.getSequenceLength() -L);
		rgns.add(new Interval(ssr.getSequenceName(), start, start+L));
		n--;
		}
	return rgns;
	}
protected boolean assertZip(final File f) {
	FileInputStream is = null;
	try {
		is=new FileInputStream(f);
		ZipInputStream zin = new ZipInputStream(is);
		zin.close();
		return true;
	} catch(IOException err) {
		return false;
	}
	finally {
		CloserUtil.close(is);
	}
	
}
protected void assertTsvTableIsConsitent(final File f,Predicate<String> ignoreLine) {
	final Pattern tab=Pattern.compile("[\t]");
	BufferedReader r=null;
	try {
		Assert.assertTrue(f.exists(), "file "+f+" should exist");
		r= IOUtils.openFileForBufferedReading(f);
		int nCols=-1;
		int nRow=0;
		String line;
		while((line=r.readLine())!=null)
			{
			if(ignoreLine!=null && ignoreLine.test(line)) continue;
			nRow++;
			final String tokens[]=tab.split(line);
			final int c= tokens.length;
			if(nRow==1)
				{
				nCols = c;
				}
			else 
				{
				if(nCols!=c)
					{
					for(int i=0;i< tokens.length;i++)
						{
						System.err.println("$"+(i+1)+":"+tokens[i]);
						}
					}
				Assert.assertEquals(nCols,c,"Line "+line+" expected "+nCols+" tokens but got "+c);
				}
			}
		}
	catch (final Exception e) {
		e.printStackTrace();
		Assert.fail();
		}
	finally
		{
		CloserUtil.close(r);
		}
	}

protected long wc(final File f) throws IOException
	{
	if(f.getName().endsWith(".bam") || f.getName().endsWith(".sam")) {
		final SamReader sr = SamReaderFactory.makeDefault().
				validationStringency(ValidationStringency.SILENT).
				open(f);
		final SAMRecordIterator iter =sr.iterator();
		final long n= iter.stream().count();
		iter.close();
		sr.close();
		return n;
		}
	
	final BufferedReader br = IOUtils.openFileForBufferedReading(f);
	final long n=br.lines().count();
	br.close();
	return n;
	}

protected File addClippingToBam(final File bamFile) throws IOException {
	final String bases = "ATGC";
	File clippedBam = this.createTmpFile(".bam");
	SamReader sr = SamReaderFactory.makeDefault().open(bamFile);
	SAMFileHeader inHeader = sr.getFileHeader();
	boolean createIndex = sr.hasIndex() && inHeader.getSortOrder().equals(SortOrder.coordinate);
	if(createIndex) {
		this.deleteFilesAtExit.add(new File(bamFile.getParentFile(),IOUtil.basename(clippedBam) + BAMIndex.BAMIndexSuffix));
		}
	final SAMFileWriter w=new SAMFileWriterFactory().
			setCreateIndex(createIndex).
			makeBAMWriter(inHeader, true, clippedBam);
	sr.iterator().stream().map(R->{
		if(R.getReadUnmappedFlag() || R.getCigar()==null) return R;
		if(R.getCigar().isClipped()) return R;
		if(R.getBaseQualities().equals(SAMRecord.NULL_QUALS)) return R;
		if(R.getBaseQualityString().equals(SAMRecord.NULL_QUALS_STRING)) return R;
		for(int side=0;side<2;side++) {
			
			final String cigar;
			boolean hard = this.random.nextBoolean();
			final int clipLen = 1+this.random.nextInt(100);
			final StringBuilder seq = new StringBuilder();
			final StringBuilder qual = new StringBuilder();
			
			if(hard) {
				cigar = String.valueOf(clipLen)+"H";
				} 
			else
				{
				cigar = String.valueOf(clipLen)+"S";
				
				
				for(int x=0;x<clipLen;++x) {
					seq.append(bases.charAt(this.random.nextInt(bases.length())));
					qual.append("#");
					}
				}
			if(side==0) {
				R.setReadString(seq.toString()+R.getReadString());
				R.setBaseQualityString(qual.toString()+R.getBaseQualityString());
				R.setCigarString(cigar+R.getCigarString());
				}
			else
				{
				R.setCigarString(R.getCigarString()+cigar);
				R.setReadString(R.getReadString()+seq.toString());
				R.setBaseQualityString(R.getBaseQualityString()+qual.toString());
				}
			}
		return R;
		}).forEach(R->w.addAlignment(R));
	w.close();
	sr.close();
	assertIsValidBam(bamFile);
	return bamFile;
	}

}
