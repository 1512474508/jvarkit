/*
The MIT License (MIT)

Copyright (c) 2019 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
package com.github.lindenb.jvarkit.tools.structvar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.lang.JvarkitException;
import com.github.lindenb.jvarkit.lang.StringUtils;
import com.github.lindenb.jvarkit.samtools.Decoy;
import com.github.lindenb.jvarkit.samtools.util.IntervalListProvider;
import com.github.lindenb.jvarkit.util.JVarkitVersion;
import com.github.lindenb.jvarkit.util.bio.SequenceDictionaryUtils;
import com.github.lindenb.jvarkit.util.iterator.MergingIterator;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.jcommander.NoSplitter;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.log.ProgressFactory;
import com.github.lindenb.jvarkit.util.samtools.SAMRecordPartition;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;

import htsjdk.samtools.AlignmentBlock;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordComparator;
import htsjdk.samtools.SAMRecordCoordinateComparator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.SequenceUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFStandardHeaderLines;
/**
BEGIN_DOC

### Example

```
$ java -jar dist/samfindclippedregions.jar --min-depth 10 --min-ratio 0.2 src/test/resources/S*.bam
##fileformat=VCFv4.2
##FORMAT=<ID=AD,Number=R,Type=Integer,Description="Allelic depths for the ref and alt alleles in the order listed">
##FORMAT=<ID=CL,Number=1,Type=Integer,Description="Left Clip">
##FORMAT=<ID=DP,Number=1,Type=Integer,Description="Approximate read depth (reads with MQ=255 or with bad mates are filtered)">
##FORMAT=<ID=GT,Number=1,Type=String,Description="Genotype">
##FORMAT=<ID=RL,Number=1,Type=Integer,Description="Right Clip">
##FORMAT=<ID=TL,Number=1,Type=Integer,Description="Total Clip">
##INFO=<ID=AC,Number=A,Type=Integer,Description="Allele count in genotypes, for each ALT allele, in the same order as listed">
##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
##INFO=<ID=DP,Number=1,Type=Integer,Description="Approximate read depth; some reads may have been filtered">
(...)
##samfindclippedregions.meta=compilation:20191009163450 githash:b8d60cab htsjdk:2.20.1 date:20191009163608 cmd:--min-depth 10 --min-ratio 0.2 src/test/resources/S1.bam src/test/resources/S2.bam src/test/resources/S3.bam src/test/resources/S4.bam src/test/resources/S5.bam
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	S1	S2	S3	S4	S5
RF01	996	.	N	<CLIP>	.	.	AC=1;AF=0.1;AN=10;DP=30	GT:AD:CL:DP:RL:TL	0/0:2,0:0:2:0:0	0/0:4,0:0:4:0:00/0:4,0:0:4:0:0	0/0:15,0:0:15:0:0	0/1:4,1:0:5:1:1
(...)
```

END_DOC

 */
@Program(name="samfindclippedregions",
	description="Fins clipped position in one or more bam. Output is a VCF file",
	keywords= {"sam","bam","clip","vcf"},
	modificationDate="20191009"
	)
public class SamFindClippedRegions extends Launcher
	{
	private static final Logger LOG=Logger.build(SamFindClippedRegions.class).make();
	
	@Parameter(names={"-o","--output"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private Path outputFile = null;
	@Parameter(names= {"-R","--reference"},description="For reading CRAM. "+INDEXED_FASTA_REFERENCE_DESCRIPTION)
	private Path faidx = null;
	@Parameter(names= {"--region","--bed","-B"},description=IntervalListProvider.OPT_DESC,converter=IntervalListProvider.StringConverter.class,splitter=NoSplitter.class)
	private IntervalListProvider intervalListProvider = null;
	@Parameter(names="-c",description="consider only clip having length >= 'x'")
	private int min_clip_operator_length = 2;

	@Parameter(names={"--groupby","--partition"},description="Group Reads by. "+SAMRecordPartition.OPT_DESC)
	private SAMRecordPartition partition=SAMRecordPartition.sample;
	@Parameter(names={"--min-depth"},description="Ignore Depth lower than 'x'")
	private int min_depth=10;
	@Parameter(names={"--min-clip-depth"},description="Ignore number of clipped bases lower than 'x'")
	private int min_clip_depth =10;
	@Parameter(names={"--min-ratio"},description="Ignore genotypes where count(clip)/(count(clip)+DP) < x")
	private double fraction = 0.1;

	
	private final int max_clip_length=1000;
	
	private static class Gt
		{
		int noClip=0;
		int leftClip = 0;
		int rightClip = 0;
		int clip() { return leftClip+rightClip;}
		int dp() { return noClip+clip();}
		double ratio() {return clip()/(double)dp();}
		}

	private static class Base
		{
		final int pos;
		final Map<String,Gt> sample2gt = new HashMap<>();
		Base(final int pos) {
			this.pos = pos;
			}
		Gt getGt(final String sn) {
			Gt gt = this.sample2gt.get(sn);
			if(gt==null) {
				gt=new Gt();
				this.sample2gt.put(sn,gt);
				}
			return gt;
			}
		}
	
	
	@Override
	public int doWork(final List<String> args) {
		if(this.min_clip_depth >this.min_depth) {
			LOG.error("this.min_clip_depth>this.min_depth");
			return -1;
		}
		if(this.fraction<0 || this.fraction>1.0) {
			LOG.error("bad ratio: "+fraction);
			return -1;
		}
		
		
		final List<SamReader> samReaders =new ArrayList<>();
		final List<CloseableIterator<SAMRecord>> samIterators =new ArrayList<>();
		
		VariantContextWriter w=null;
		//SAMFileWriter w=null;
		try
			{
			final List<Path> bamPaths = IOUtils.unrollPaths(args);
			if(bamPaths.isEmpty())
				{
				LOG.error("No Bam was provided");
				return -1;
				}
			final SamReaderFactory srf = super.createSamReaderFactory();
			SAMSequenceDictionary dict=null;
			if(this.faidx!=null) {
				srf.referenceSequence(this.faidx);
				dict = SequenceDictionaryUtils.extractRequired(this.faidx);
				}
			
			final Set<String> samples = new TreeSet<>();
			
			QueryInterval[] intervalList = null;
			
			/* create input, collect sample names */
			for(final Path filename:bamPaths)
				{
				final SamReader sr = srf.open(filename);
				//input.index=inputs.size();
				samReaders.add(sr);
				
				final SAMFileHeader header = sr.getFileHeader();
				
				samples.addAll(header.getReadGroups().
						stream().
						map(this.partition).
						filter(S->!StringUtils.isBlank(S)).
						collect(Collectors.toSet()));
				
				final SAMSequenceDictionary d2 = SequenceDictionaryUtils.extractRequired(header);
				
				if(dict==null)
					{
					dict=d2;
					}
				else if(!SequenceUtil.areSequenceDictionariesEqual(dict, d2))
					{
					throw new JvarkitException.DictionariesAreNotTheSame(dict, d2);
					}
				final CloseableIterator<SAMRecord> iter;
				if(this.intervalListProvider!=null) {
					if(intervalList==null) {
						intervalList = this.intervalListProvider.
								dictionary(d2).
								optimizedQueryIntervals();
						}
					iter = sr.queryOverlapping(intervalList);
					}
				else
					{
					iter = sr.iterator();
					}
				samIterators.add(iter);
				}
			
			
			/* create merged iterator */
			final SAMRecordComparator samRecordComparator = new SAMRecordCoordinateComparator();
			
			final MergingIterator<SAMRecord> iter = new MergingIterator<>(
					(A,B)->samRecordComparator.fileOrderCompare(A,B), 
					samIterators
					);
			
			/* build VCF header */
			final Allele reference_allele= Allele.create("N",true);			
			final Allele alt_allele = Allele.create("<CLIP>",false);			
		
			
			final Set<VCFHeaderLine> vcfHeaderLines=new HashSet<>();
			
			VCFStandardHeaderLines.addStandardFormatLines(vcfHeaderLines, true, 
					VCFConstants.GENOTYPE_KEY,
					VCFConstants.DEPTH_KEY,
					VCFConstants.GENOTYPE_ALLELE_DEPTHS
					);
			
			VCFStandardHeaderLines.addStandardInfoLines(vcfHeaderLines, true, 
					VCFConstants.DEPTH_KEY,
					VCFConstants.ALLELE_COUNT_KEY,
					VCFConstants.ALLELE_NUMBER_KEY,
					VCFConstants.ALLELE_FREQUENCY_KEY
					)
					;

			final VCFFormatHeaderLine leftClip = new VCFFormatHeaderLine("CL", 1,VCFHeaderLineType.Integer,"Left Clip");
			vcfHeaderLines.add(leftClip);
			final VCFFormatHeaderLine rightClip = new VCFFormatHeaderLine("RL", 1,VCFHeaderLineType.Integer,"Right Clip");
			vcfHeaderLines.add(rightClip);
			final VCFFormatHeaderLine totalCip = new VCFFormatHeaderLine("TL", 1,VCFHeaderLineType.Integer,"Total Clip");
			vcfHeaderLines.add(totalCip);
			
			final VCFHeader vcfHeader=new VCFHeader(vcfHeaderLines,samples);
			vcfHeader.setSequenceDictionary(dict);
			
			JVarkitVersion.getInstance().addMetaData(this, vcfHeader);
			
			if(this.outputFile!=null) {
				w=VCFUtils.createVariantContextWriterToPath(this.outputFile);
				}
			else
				{
				w=VCFUtils.createVariantContextWriterToOutputStream(stdout());
				}
		
			w.writeHeader(vcfHeader);
			
			final VariantContextWriter finalVariantContextWriter = w;
			
			/** dump a BASe into the VCF */
			final BiConsumer<String,Base> baseConsumer = (CTG,B)->{
				if(B.pos<1) return;
			
				//no clip
				if(B.sample2gt.values().stream().mapToInt(G->G.clip()).sum()==0) return;
				
				if(B.sample2gt.values().stream().allMatch(G->G.clip() < min_clip_depth)) return;
				if(B.sample2gt.values().stream().allMatch(G->G.dp() < min_depth)) return;
				
				
				if(B.sample2gt.values().stream().allMatch(G->G.ratio() < fraction)) return;
				final VariantContextBuilder vcb=new VariantContextBuilder();
				vcb.chr(CTG);
				vcb.start(B.pos);
				vcb.stop(B.pos);
				vcb.alleles(Arrays.asList(reference_allele,alt_allele));
				vcb.attribute(VCFConstants.DEPTH_KEY,B.sample2gt.values().stream().mapToInt(G->G.dp()).sum());
				final List<Genotype> genotypes = new ArrayList<>(B.sample2gt.size());
				int AC=0;
				int AN=0;
				int max_clip=1;
				for(final String sn:B.sample2gt.keySet()) {
					final Gt gt = B.sample2gt.get(sn);
					final GenotypeBuilder gb = new GenotypeBuilder(sn);
					if(gt.noClip==0) {
						gb.alleles(Arrays.asList(alt_allele,alt_allele));
						AC+=2;
						}
					else if(gt.leftClip+gt.rightClip==0) {
						gb.alleles(Arrays.asList(reference_allele,reference_allele));
						}
					else{
						gb.alleles(Arrays.asList(reference_allele,alt_allele));
						AC++;
						}
					
					gb.DP(gt.dp());
					gb.attribute(leftClip.getID(), gt.leftClip);
					gb.attribute(rightClip.getID(), gt.rightClip);
					gb.attribute(totalCip.getID(), gt.clip());
					gb.AD(new int[] {gt.noClip, gt.clip()});
					
					genotypes.add(gb.make());
					AN+=2;
					max_clip = Math.max(max_clip, gt.clip());
					}
				vcb.log10PError(max_clip/-10.0);
				vcb.attribute(VCFConstants.ALLELE_COUNT_KEY, AC);
				vcb.attribute(VCFConstants.ALLELE_NUMBER_KEY, AN);
				if(AN>0) vcb.attribute(VCFConstants.ALLELE_FREQUENCY_KEY, AC/(float)AN);
				vcb.genotypes(genotypes);
				finalVariantContextWriter.add(vcb.make());
				};
				
				
			final ProgressFactory.Watcher<SAMRecord> progress = ProgressFactory.
					newInstance().
					dictionary(dict).
					logger(LOG).
					build();
			
			String prevContig = null;
			final SortedMap<Integer,Base> pos2base = new TreeMap<>();
			
			/* get base in pos2base, create it if needed */
			final Function<Integer, Base> baseAt = POS->{
				Base b = pos2base.get(POS);
				if(b==null) {
					b = new Base(POS);
					pos2base.put(POS, b);
					}
				return b;
				};
			
			final Decoy decoy= Decoy.getDefaultInstance();
			for(;;) {
				final SAMRecord rec=(iter.hasNext()?progress.apply(iter.next()):null);
				if(rec!=null) {
					if(rec.getReadUnmappedFlag()) continue;
					if(rec.isSecondaryOrSupplementary()) continue;
					if(rec.getDuplicateReadFlag()) continue;
					if(rec.getReadFailsVendorQualityCheckFlag()) continue;
					if(decoy.isDecoy(rec.getContig())) continue;
					}
				
				if(rec==null || !rec.getContig().equals(prevContig))
					{
					for(final Integer pos: pos2base.keySet()) {
						baseConsumer.accept(prevContig,pos2base.get(pos));
						}
					if(rec==null) break;
					pos2base.clear();
					prevContig = rec.getContig();
					}
				
				for(Iterator<Integer> rpos = pos2base.keySet().iterator();
						rpos.hasNext();
					) {
			    	final Integer pos = rpos.next();
			    	if(pos.intValue() + this.max_clip_length >= rec.getUnclippedStart()) break;
			    	baseConsumer.accept(prevContig,pos2base.get(pos));
			    	rpos.remove();
			    	}

				
				
				final String rg = this.partition.getPartion(rec);
				if(StringUtils.isBlank(rg)) continue;
				
				for(final AlignmentBlock ab:rec.getAlignmentBlocks()) {
					for(int n=0;n< ab.getLength();++n) {
						baseAt.apply(ab.getReferenceStart()+n).getGt(rg).noClip++;
						}
					}
				
				final Cigar cigar = rec.getCigar();
				CigarElement ce = cigar.getFirstCigarElement();
				if(ce!=null && ce.getOperator().isClipping() && ce.getLength()>=this.min_clip_operator_length) {
					baseAt.apply(rec.getStart()-1).getGt(rg).leftClip++;
					}
				ce = cigar.getLastCigarElement();
				if(ce!=null && ce.getOperator().isClipping() && ce.getLength()>=this.min_clip_operator_length) {
					baseAt.apply(rec.getEnd()+1).getGt(rg).rightClip++;
					}
				
				}
			
			iter.close();
			samIterators.forEach(R->R.close());
			samReaders.forEach(R->CloserUtil.close(R));
			progress.close();
			
			w.close();
			w=null;
			return 0;
			}

		catch(final Throwable err)
			{
			LOG.error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(w);
			}
		}
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		new SamFindClippedRegions().instanceMainWithExit(args);
		}
	}
