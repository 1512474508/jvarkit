/*
The MIT License (MIT)

Copyright (c) 2018 Pierre Lindenbaum

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
package com.github.lindenb.jvarkit.tools.vcfbed;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlException;

import com.github.lindenb.jvarkit.util.bio.bed.IndexedBedReader;
import com.github.lindenb.jvarkit.util.bio.fasta.ContigNameConverter;
import com.github.lindenb.jvarkit.util.bio.fasta.ContigNameConverter.OnNotFound;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.lindenb.jvarkit.util.bio.bed.BedLine;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.VariantContextUtils;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.vcf.DelegateVariantContextWriter;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VariantContextWriterFactory;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


/**
 * VCFBed
 * 
BEGIN_DOC

## Example

Map the NCBI biosystems to a BED file using the following script:     https://gist.github.com/6024788 

```
$ gunzip -c ~/ncbibiosystem.bed.gz | head
1	69091	70008	79501	106356	30	Signaling_by_GPCR
1	69091	70008	79501	106383	50	Olfactory_Signaling_Pathway
1	69091	70008	79501	119548	40	GPCR_downstream_signaling
1	69091	70008	79501	477114	30	Signal_Transduction
1	69091	70008	79501	498	40	Olfactory_transduction
1	69091	70008	79501	83087	60	Olfactory_transduction
1	367640	368634	26683	106356	30	Signaling_by_GPCR
1	367640	368634	26683	106383	50	Olfactory_Signaling_Pathway
1	367640	368634	26683	119548	40	GPCR_downstream_signaling
1	367640	368634	26683	477114	30	Signal_Transduction
```

Now, annotate a remote VCF with the data of NCBI biosystems.

```
curl "https://raw.github.com/arq5x/gemini/master/test/test1.snpeff.vcf" |\
 sed 's/^chr//' |\
 java -jar  dist/vcfbed.jar -B ~/ncbibiosystem.bed.gz -T NCBIBIOSYS  -f '($4|$5|$6|$7)' |\
 grep -E '(^#CHR|NCBI)'

##INFO=<ID=NCBIBIOSYS,Number=.,Type=String,Description="metadata added from /home/lindenb/ncbibiosystem.bed.gz . Format was ($4|$5|$6|$7)">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	1094PC0005	1094PC0009	1094PC0012	1094PC0013
1	69270	.	A	G	2694.18	.	AC=40;AF=1.000;AN=40;DP=83;Dels=0.00;EFF=SYNONYMOUS_CODING(LOW|SILENT|tcA/tcG|S60|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=0.000;HRun=0;HaplotypeScore=0.0000;InbreedingCoeff=-0.0598;MQ=31.06;MQ0=0;NCBIBIOSYS=(79501|119548|40|GPCR_downstream_signaling),(79501|106356|30|Signaling_by_GPCR),(79501|498|40|Olfactory_transduction),(79501|83087|60|Olfactory_transduction),(79501|477114|30|Signal_Transduction),(79501|106383|50|Olfactory_Signaling_Pathway);QD=32.86	GT:AD:DP:GQ:PL	./.	./.	1/1:0,3:3:9.03:106,9,0	1/1:0,6:6:18.05:203,18,0
1	69511	.	A	G	77777.27	.	AC=49;AF=0.875;AN=56;BaseQRankSum=0.150;DP=2816;DS;Dels=0.00;EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|Aca/Gca|T141A|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=21.286;HRun=0;HaplotypeScore=3.8956;InbreedingCoeff=0.0604;MQ=32.32;MQ0=0;MQRankSum=1.653;NCBIBIOSYS=(79501|119548|40|GPCR_downstream_signaling),(79501|106356|30|Signaling_by_GPCR),(79501|498|40|Olfactory_transduction),(79501|83087|60|Olfactory_transduction),(79501|477114|30|Signal_Transduction),(79501|106383|50|Olfactory_Signaling_Pathway);QD=27.68;ReadPosRankSum=2.261	GT:AD:DP:GQ:PL	./.	./.	0/1:2,4:6:15.70:16,0,40	0/1:2,2:4:21.59:22,0,40</h:pre>
```

Another example:

```
$ tabix -h dbsnp138_00-All.vcf.gz "19:58864565-58865165" | sed '/^[^#]/s/^/chr/' |\
java -jar dist/vcfbed.jar -m your.bed -e 'bed.get(0)+"|"+bed.get(1)+"|"+bed.get(2)+"|"+bed.get(3)+"&"+bed.get(4)'

##INFO=<ID=VCFBED,Number=.,Type=String,Description="metadata added from your.bed . Format was ${1}|${2}|${3}|${4}&${5}">
(...)
chr19   58864911    rs113760967 T   C   .   .   GNO;OTHERKG;R5;RS=113760967;RSPOS=58864911;SAO=0;SLO;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VP=0x050100020001000102000100;WGT=1;dbSNPBuildID=132
chr19   58865054    rs893183    T   C   .   .   CAF=[0.1299,0.8701];COMMON=1;G5;GNO;HD;KGPROD;KGPhase1;KGPilot123;OTHERKG;PH3;R5;RS=893183;RSPOS=58865054;RV;SAO=0;SLO;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VLD;VP=0x05010002000115051f000100;WGT=1;dbSNPBuildID=86
chr19   58865068    rs893182    T   C   .   .   CAF=[0.1299,0.8701];COMMON=1;G5;GNO;HD;KGPROD;KGPhase1;KGPilot123;OTHERKG;PH3;R5;RS=893182;RSPOS=58865068;RV;SAO=0;SLO;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VLD;VP=0x05010002000115051f000100;WGT=1;dbSNPBuildID=86
chr19   58865082    rs893181    A   T   .   .   CAF=[0.1295,0.8705];COMMON=1;G5;GNO;HD;KGPROD;KGPhase1;KGPilot123;OTHERKG;PH3;R5;RS=893181;RSPOS=58865082;RV;SAO=0;SLO;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VLD;VP=0x05010002000115051f000100;WGT=1;dbSNPBuildID=86
chr19   58865091    rs893180    A   G   .   .   CAF=[0.1299,0.8701];COMMON=1;G5;GNO;HD;KGPROD;KGPhase1;KGPilot123;OTHERKG;R5;RS=893180;RSPOS=58865091;RV;SAO=0;SLO;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VLD;VP=0x05010002000115051e000100;WGT=1;dbSNPBuildID=86
chr19   58865112    rs188818621 C   T   .   .   CAF=[0.9954,0.004591];COMMON=1;KGPROD;KGPhase1;R5;RS=188818621;RSPOS=58865112;SAO=0;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VP=0x050000020001000014000100;WGT=1;dbSNPBuildID=135
chr19   58865164    rs80109863  C   T   .   .   CAF=[0.9949,0.005051];COMMON=1;GNO;KGPROD;KGPhase1;OTHERKG;R5;RS=80109863;RSPOS=58865164;SAO=0;SSR=0;VC=SNV;VCFBED=chr19|58864565|58865165|A1BG&58864865;VP=0x050000020001000116000100;WGT=1;dbSNPBuildID=132
```

END_DOC

 */
@Program(name="vcfbed",
	description="Transfer information from a BED to a VCF",
	keywords={"bed","vcf","annotation"},
	biostars=247224
	)
public class VCFBed extends Launcher
	{

	private static final Logger LOG = Logger.build(VCFBed.class).make();

	
	

	@Parameter(names={"-o","--output"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private File outputFile = null;	
	@ParametersDelegate
	private CtxWriterFactory component = new CtxWriterFactory();
	
	
	private static final Map<String,Function<BedLine,Object>> MAPPER=new HashMap<>();
	static {
		MAPPER.put("bed",R->R);
		MAPPER.put("line",R->R.join());
		};
	
	private static class BedJEXLContext
		implements JexlContext
		{
		final BedLine bedLine;
		BedJEXLContext(final BedLine bedLine) {
			this.bedLine = bedLine;
			}
		@Override
		public Object get(final String name) {
			final Function<BedLine,Object> fun = MAPPER.get(name);
			return (fun!=null?fun.apply(this.bedLine):null);
			}
		@Override
		public boolean has(final String key) {
			return MAPPER.containsKey(key);
			}
		@Override
		public void set(final String key, Object arg1) {
			throw new UnsupportedOperationException();
			}
		@Override
		public String toString() {
			return "JexlContext for SAMRecord "+this.bedLine;
			}
		}
	
	
	@XmlType(name="vcfbed")
	@XmlRootElement(name="vcfbed")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class CtxWriterFactory 
		implements VariantContextWriterFactory
			{
			@Parameter(names={"-e","--expr","--jexl"},description="[20180124]A JEXL Expression returning a string (see https://software.broadinstitute.org/gatk/documentation/article.php?id=1255). The variable 'bed' is the current observed BedLine (see  https://github.com/lindenb/jvarkit/blob/7bddffca3899196e568fb5e1a479300c0038f74f/src/main/java/com/github/lindenb/jvarkit/util/bio/bed/BedLine.java )")
			private String formatPattern = "bed.get(0)+\":\"+bed.get(1)+\"-\"+bed.get(2)";
		
			@Parameter(names={"-T","--tag"},description="use the following INFO tag name")
			private String infoName = "VCFBED";
		
			@Parameter(names={"-B","--bed"},description="Tribble or Tabix bed file ")
			private File tabixFile = null;
		
			@Parameter(names={"-m","--map"},description="unindexed bed file, will be loaded in memory (faster than tribble/tabix but memory consumming)")
			private File treeMapFile = null;
		
			@Parameter(names={"-fo","--filteroverlap"},description="if defined, set this as a FILTER column if one or more BED line overlap a variant")
			private String filterOverlapStr = null;
		
			@Parameter(names={"-fn","--filternooverlap"},description="if defined, set this as a FILTER column if not any BED line overlap a variant")
			private String filterNoOverlapStr = null;
			
			@Parameter(names={"-ignoreFiltered","--ignoreFiltered"},description="[20171031] Ignore FILTERed Variants (should be faster)")
			private boolean ignoreFILTERed=false;
			@Parameter(names={"-x","--extend"},description="[20180123]if nothing was found in the BED file, extends the interval by 'x' bases and try again. Ignore if <1. Require that the VCF file has a Dictionary (##contig lines)")
			private int extend_by=0;
			@Parameter(names={"-mx","--max-extend"},description="[20180123] used with option 'x': don't extend to more than 'max' bases.")
			private int max_extend_by=1000;
			
			private IntervalTreeMap<Set<BedLine>> intervalTreeMap=null;
			private IndexedBedReader bedReader =null;
			private ContigNameConverter contigNameConverter = null;
			private Expression jexlExpr = null;
			
			private class CtxWriter extends DelegateVariantContextWriter
				{
				private final File tabixFile = CtxWriterFactory.this.tabixFile;
				private final File treeMapFile = CtxWriterFactory.this.treeMapFile;
				private final String infoName = CtxWriterFactory.this.infoName;
				private final String formatPattern = CtxWriterFactory.this.formatPattern;
				private final String filterNoOverlapStr = CtxWriterFactory.this.filterNoOverlapStr;
				private final String filterOverlapStr = CtxWriterFactory.this.filterOverlapStr;
				private final boolean ignoreFILTERed = CtxWriterFactory.this.ignoreFILTERed;
				private VCFFilterHeaderLine filterOverlap = null;
				private VCFFilterHeaderLine filterNoOverlap = null;
				private VCFInfoHeaderLine infoHeader = null;
				private final int extend_by = CtxWriterFactory.this.extend_by;
				private final int max_extend_by = CtxWriterFactory.this.max_extend_by;
				private SAMSequenceDictionary vcfDict=null;
				
				
				CtxWriter(final VariantContextWriter delegate) {
					super(delegate);
					
					}
				
				private CtxWriterFactory getOwner() { return CtxWriterFactory.this;}
				
				
				private String bedLineToString(final BedLine bedLine) {
					final Object o;
					try {
						o = getOwner().jexlExpr.evaluate(new BedJEXLContext(bedLine));
						}
					catch(final JexlException err) {
						throw new RuntimeException("Cannot evaluate JEXL expression \""+getOwner().formatPattern+"\" with BedRecord :"+bedLine);
						}
					if(o==null) return null;
					return String.valueOf(o);
					}
				
				@Override
				public void writeHeader(final VCFHeader header) {
					final File srcbedfile = this.tabixFile==null?this.treeMapFile:this.tabixFile;
					this.vcfDict = header.getSequenceDictionary();
					final VCFHeader h2=new VCFHeader(header);
					this.infoHeader= 
							new VCFInfoHeaderLine(
									this.infoName,
									VCFHeaderLineCount.UNBOUNDED,
									VCFHeaderLineType.String,
									"metadata added from "+ srcbedfile+
									" . Format was "+this.formatPattern
									);
					
					this.filterOverlap = 
							(this.filterOverlapStr==null || this.filterNoOverlapStr.trim().isEmpty()?null:
							new VCFFilterHeaderLine(this.filterOverlapStr, "Variant overlap with "+srcbedfile)	
							);
					
					this.filterNoOverlap = 
							(this.filterNoOverlapStr==null || this.filterNoOverlapStr.trim().isEmpty()?null:
							new VCFFilterHeaderLine(this.filterNoOverlapStr, "Variant having NO overlap with "+srcbedfile)	
							);
					
					if(this.filterOverlap!=null) h2.addMetaDataLine(this.filterOverlap);
					if(this.filterNoOverlap!=null) h2.addMetaDataLine(this.filterNoOverlap);
					
					h2.addMetaDataLine(this.infoHeader);
					//addMetaData(h2);
					super.writeHeader(h2);
					}
				@Override
				public void add(final VariantContext ctx) {
					if(this.ignoreFILTERed && ctx.isFiltered())
						{
						super.add(ctx);
						return;
						}
					
					final String normalizedContig = getOwner().contigNameConverter.apply(ctx.getContig());
					if(StringUtil.isBlank(normalizedContig)) {
						super.add(ctx);
						return;
						}
					Interval theInterval = new Interval(
							normalizedContig,
							ctx.getStart(),
							ctx.getEnd()
							);
					
					boolean found_overlap=false;
					final Set<String> annotations=new HashSet<String>();
					while(!found_overlap) {
						if(getOwner().intervalTreeMap!=null) {
							for(final Set<BedLine> bedLines :getOwner().intervalTreeMap.getOverlapping(theInterval)) {
								for(final BedLine bedLine:bedLines) {
								final String newannot= this.bedLineToString(bedLine);
								found_overlap=true;
								if(!StringUtil.isBlank(newannot))
									{
									annotations.add(VCFUtils.escapeInfoField(newannot));
									}
								}
							  }
							}
						else
							{
							CloseableIterator<BedLine> iter = null;
							try {
								iter = getOwner().bedReader.iterator(
										theInterval.getContig(),
										theInterval.getStart()-1,
										theInterval.getEnd()+1
										);
								while(iter.hasNext())
									{
									final BedLine bedLine = iter.next();
									
									if(!theInterval.getContig().equals(bedLine.getContig())) continue;
									if(theInterval.getStart() > bedLine.getEnd() ) continue;
									if(theInterval.getEnd() < bedLine.getStart() ) continue;
					
									found_overlap=true;
				
									final String newannot= this.bedLineToString(bedLine);
									if(!StringUtil.isBlank(newannot))
										annotations.add(VCFUtils.escapeInfoField(newannot));
									}
								CloserUtil.close(iter);
								}
							catch(final IOException ioe)
								{
								LOG.error(ioe);
								throw new RuntimeIOException(ioe);
								}
							finally
								{
								CloserUtil.close(iter);
								}
							}
						// can we extend the current interval
						if(found_overlap) break;
						if(this.extend_by<1) break;
						if(this.vcfDict==null || this.vcfDict.isEmpty()) break;
						final SAMSequenceRecord ssr = this.vcfDict.getSequence(theInterval.getContig());
						if(ssr==null || (theInterval.getStart()<=1 && theInterval.getEnd()>= ssr.getSequenceLength())) break;
						
						theInterval = new Interval(
								theInterval.getContig(),
								Math.max(1, theInterval.getStart() - this.extend_by),
								Math.min(ssr.getSequenceLength(), theInterval.getEnd() + this.extend_by)
								);
						if(ctx.getStart() - theInterval.getStart() > this.max_extend_by) break;
						if(theInterval.getEnd()  - ctx.getEnd() > this.max_extend_by) break;
						
						}// end of while
					final String filterToSet;
					if(found_overlap && this.filterOverlap!=null) {
						filterToSet =  this.filterOverlap.getID();
						}
					else if(!found_overlap &&  filterNoOverlap!=null) {
						filterToSet =  this.filterNoOverlap.getID();
						}
					else
						{
						filterToSet=null;
						}
					
					if(filterToSet==null && annotations.isEmpty())
						{
						super.add(ctx);
						}
					else
						{
						final VariantContextBuilder vcb=new VariantContextBuilder(ctx);
						if(!annotations.isEmpty()) {
							vcb.attribute(infoHeader.getID(), annotations.toArray());
							}
						if(filterToSet!=null) {
							vcb.filter(filterToSet);
							}
						else if(ctx.isNotFiltered())
							{
							vcb.passFilters();
							}
						super.add(vcb.make());
						}
					}
				}
			
	
			/** reads a Bed file and convert it to a IntervalTreeMap<Bedline> */
			private htsjdk.samtools.util.IntervalTreeMap<Set<com.github.lindenb.jvarkit.util.bio.bed.BedLine>> 
				readBedFileAsIntervalTreeMap(final java.io.File file) throws java.io.IOException
				{
				java.io.BufferedReader r=null;
				try
					{
					final  htsjdk.samtools.util.IntervalTreeMap<Set<com.github.lindenb.jvarkit.util.bio.bed.BedLine>> intervals = new
							 htsjdk.samtools.util.IntervalTreeMap<>();
					r=com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(file);
					String line;
					final com.github.lindenb.jvarkit.util.bio.bed.BedLineCodec codec = new com.github.lindenb.jvarkit.util.bio.bed.BedLineCodec();
					while((line=r.readLine())!=null) 
						{
						if(line.startsWith("#") ||  com.github.lindenb.jvarkit.util.bio.bed.BedLine.isBedHeader(line) ||  line.isEmpty()) continue; 
						final com.github.lindenb.jvarkit.util.bio.bed.BedLine bl = codec.decode(line);
						if(bl==null || bl.getStart()>bl.getEnd()) continue;
						final htsjdk.samtools.util.Interval interval= bl.toInterval();
						Set<BedLine> set = intervals.get(interval);
						if(set==null )
							{
							set = new HashSet<>();
							intervals.put(interval,set); 
							}
						set.add(bl);
						}
					return intervals;
					}
				finally
					{
					htsjdk.samtools.util.CloserUtil.close(r);
					}
				}

			
			@Override
			public int initialize() {

				if(this.tabixFile==null && this.treeMapFile==null)
					{
					LOG.error("Undefined tabix or memory file");
					return -1;
					}
				else if(this.tabixFile!=null && this.treeMapFile!=null)
					{
					LOG.error("You cannot use both options: tabix/in memory bed");
					return -1;
					}
				else if( this.tabixFile!=null) {
					LOG.info("opening Bed "+this.tabixFile);
					try 
						{
						this.bedReader= new IndexedBedReader(this.tabixFile);
						this.contigNameConverter = ContigNameConverter.fromContigSet(this.bedReader.getContigs());
						}
					catch(IOException err)
						{
						LOG.error(err);
						return -1;
						}
					}
				else 
					{
					try {
						this.intervalTreeMap = this.readBedFileAsIntervalTreeMap(this.treeMapFile);
						LOG.info("Number of items in "+this.treeMapFile+" "+this.intervalTreeMap.size());
						this.contigNameConverter = ContigNameConverter.fromIntervalTreeMap(this.intervalTreeMap);
						}
					catch(final Exception err) {
						LOG.error(err);
						return -1;
						}
					}
				
				this.contigNameConverter.setOnNotFound(OnNotFound.SKIP);
				
				if(this.infoName==null || this.infoName.trim().isEmpty())
					{
					LOG.error("Undefined INFO name.");
					return -1;
					}
				
				LOG.info("parsing JEXL expression: "+this.formatPattern);
				try {
					this.jexlExpr =VariantContextUtils.engine.get().createExpression(this.formatPattern);
					} 
				catch(final JexlException err) {
					LOG.error("Cannot compile JEXL expression", err);
					return -1;
					}
				return 0;
				}
			
			@Override
			public VariantContextWriter open(VariantContextWriter delegate) {
				return new CtxWriter(delegate);
				}
			
			@Override
			public void close() throws IOException {
				CloserUtil.close(this.bedReader);
				this.bedReader = null;
				this.intervalTreeMap=null;
				this.jexlExpr=null;
				}
			}
	
	@Override
	protected int doVcfToVcf(final String inputName, final  VcfIterator iter, final  VariantContextWriter delegate)
		{	
		final VariantContextWriter out = this.component.open(delegate);
		final SAMSequenceDictionaryProgress progress = new SAMSequenceDictionaryProgress(iter.getHeader()).logger(LOG);
		out.writeHeader(iter.getHeader());
		while(iter.hasNext())
			{
			out.add(progress.watch(iter.next()));
			}
		out.close();
		progress.finish();
		return 0;
		}
	

	@Override
	public int doWork(final List<String> args) {
		try
			{
			if(this.component.initialize()!=0) return -1;
			return doVcfToVcf(args, outputFile);
			}
		catch(final Exception err)
			{
			LOG.error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(this.component);	
			}
		}

	
	
	public static void main(String[] args) throws Exception
		{
		new VCFBed().instanceMainWithExit(args);
		}
}
