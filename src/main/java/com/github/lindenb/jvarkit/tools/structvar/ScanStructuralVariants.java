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

import java.io.Closeable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.lang.JvarkitException;
import com.github.lindenb.jvarkit.lang.StringUtils;
import com.github.lindenb.jvarkit.samtools.Decoy;
import com.github.lindenb.jvarkit.util.JVarkitVersion;
import com.github.lindenb.jvarkit.util.bio.DistanceParser;
import com.github.lindenb.jvarkit.util.bio.SequenceDictionaryUtils;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.log.ProgressFactory;
import com.github.lindenb.jvarkit.variant.variantcontext.Breakend;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.CoordMath;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.SequenceUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import htsjdk.variant.vcf.VCFStandardHeaderLines;

/**
BEGIN_DOC

## Example

```
find CONTROLS/ -name "*.vcf.gz" > controls.list

java -Xmx3g -Djava.io.tmpdir=. -jar scansv.jar --controls controls.list -d2 25 --fraction 0.6 cases1.vcf cases2.vcf > out.vcf

```


END_DOC

 */

@Program(name="scansv",
generate_doc=false,
description="Scan structural variants for case/controls data",
keywords= {"cnv","indel","sv","pedigree"},
creationDate="20190815",
modificationDate="20190815"
)
public class ScanStructuralVariants extends Launcher{
	private static final Logger LOG = Logger.build(ScanStructuralVariants.class).make();
	private  static final String ATT_FILENAME="SOURCE";
	private static final String ATT_CLUSTER="CLUSTER";
	private static final String ATT_CONTROL="FOUND_IN_CONTROL";
	@Parameter(names={"-o","--out"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private File outputFile=null;
	@Parameter(names={"-d","--distance"},description="Two BND variants are the same if their bounds are distant by less than xxx bases. "+ DistanceParser.OPT_DESCRIPTION,converter=DistanceParser.StringConverter.class ,splitter=com.github.lindenb.jvarkit.util.jcommander.NoSplitter.class)
	private int max_distance = 100;
	@Parameter(names={"-d2"},description="Two variants are the same if they overlap and both have a length<= 'd2'. "+ DistanceParser.OPT_DESCRIPTION,converter=DistanceParser.StringConverter.class ,splitter=com.github.lindenb.jvarkit.util.jcommander.NoSplitter.class)
	private int small_distance = 10;
	@Parameter(names={"-f","--fraction"},description="Two CNV/DEL/.. variants are the same if they share 'x' fraction of their size.")
	private double max_fraction = 0.75;
	@Parameter(names={"-c","--controls"},description="Controls indexed VCF files. a file endings with the suffix '.list' is interpretted as a list of path.")
	private List<Path> controlsPath =new ArrayList<>();
	@Parameter(names={"--all"},description="Print all original variants from each file instead of printing just one.")
	private boolean print_all_ctx=false;
	@Parameter(names={"--maf"},description="Max frequency of variants found in controls.")
	private double max_maf = 0.0;
	@Parameter(names={"-L","--large"},description="Large number of controls: By default, all VCF readers for controls are opened and are kept opened."
			+ " It's fast but requires a lot of resources. This option open+close the controls if needed but it makes things slower."
			+ " It's the number of VCF that should be keept open, So '0' = ignore/all re-open+close (slow)")
	private int max_control_large_flag=0;


	private int ID_GENERATOR=0;


	/** wrap a VCFFileReader to be used with option control_large_flag : will really close the vcf reader if needed */
	private static class ShadowedVcfReader implements Closeable {
		private VCFFileReader vcfReader  = null;
		final Path vcfPath;
		private final boolean closeOnclose;
		ShadowedVcfReader(final Path vcfPath,boolean closeOnclose) {
			this.vcfPath=vcfPath;
			this.closeOnclose = closeOnclose;
			}
		CloseableIterator<VariantContext> query(String contig, int start, int end) {
			if(this.vcfReader==null) {
				this.vcfReader = new VCFFileReader(this.vcfPath, true);
				}
			return this.vcfReader.query(contig, start, end);
			}
		void realClose() {
			if(this.vcfReader!=null)
				{
				this.vcfReader.close();
				this.vcfReader=null;
				}
			}
		@Override
		public void close()  {
			if(closeOnclose) realClose();
			}
		}
	
	
	
	
	private String getSvtype(final VariantContext ctx) {
		return ctx.getAttributeAsString(VCFConstants.SVTYPE, ".");
	}
	
	private boolean testOverlapping(final VariantContext a,final VariantContext b ) {
		final String typea = getSvtype(a);
		final String typeb = getSvtype(b);
		if(!a.getContig().equals(b.getContig())) return false;
		
		

		if(typea.equals("BND") &&  typeb.equals("BND")) {
			return  a.withinDistanceOf(b, Math.max(this.max_distance,this.small_distance) )
					;
			}
		else if(typea.equals("BND") ||  typeb.equals("BND"))
			{
			return false;
			}
		else
			{
			final Interval interval1 = new Interval(a);
			final Interval interval2 = new Interval(b);
			if(!interval1.overlaps(interval2)) return false;

			/* example
			chrZ    137402727       MantaINS:160764:0:0:0:0:0       CTAA    CACCCGGCTAAT    .       .       CIGAR=1M11I3D;CLUSTER=CTX8375;DOWNSTREAM_PAIR_COUNT=0;END=137402730;PAIR_COUNT=0;SVLEN=11;SVTYPE=INS;UPSTREAM_PAIR_COUNT=0
			chrZ    137402727       MantaINS:152106:0:0:0:1:0       CT      CACCCGGCG       .       .       CIGAR=1M8I1D;CLUSTER=CTX8140;DOWNSTREAM_PAIR_COUNT=0;END=137402728;PAIR_COUNT=0;SVLEN=8;SVTYPE=INS;UPSTREAM_PAIR_COUNT=1
			*/

			if( interval1.length() <= this.small_distance && interval2.length() <= this.small_distance) return true;

			int p1 = Math.max(interval1.getStart(),interval2.getStart());
			int p2 = Math.min(interval1.getEnd(),interval2.getEnd());
			double len = CoordMath.getLength(p1,p2);
			if(len/interval1.getLengthOnReference() < this.max_fraction ) return false; 
			if(len/interval2.getLengthOnReference() < this.max_fraction ) return false; 
			return true;
			}
		}
	

	private int recursive(final VariantContext ctx,
			final List<VariantContext> candidates,
			final List<VCFFileReader> vcfFilesInput,
			final List<ShadowedVcfReader> shadowControls,
			final VariantContextWriter out) {
		if(candidates.size()==vcfFilesInput.size()) {
			final int max_controls = (int)(shadowControls.size() * max_maf);
			int count_matching_controls = 0;
			for(final ShadowedVcfReader vcfReader: shadowControls) {
				final CloseableIterator<VariantContext> iter = vcfReader.query(
						ctx.getContig(),
						Math.max(1,ctx.getStart()-this.max_distance),
						ctx.getEnd()+this.max_distance
						);
				while(iter.hasNext()) {
					final VariantContext ctx3 = iter.next();
					if(testOverlapping(ctx3, ctx)) {
						count_matching_controls++;
						candidates.add(new VariantContextBuilder(ctx3).
							noGenotypes().
							filter(ATT_CONTROL).
							attribute(ATT_FILENAME, vcfReader.vcfPath.toString()).
							make()
							);
						break;
						}
					}
				iter.close();
				vcfReader.close();
				if(count_matching_controls > max_controls) return -1;
				}
			if(this.print_all_ctx) {
				final String cluster = "CTX"+(++ID_GENERATOR);
				for(int x=0;x< candidates.size();++x) {
					out.add(new VariantContextBuilder(candidates.get(x)).noGenotypes().
							attribute(ATT_CLUSTER, cluster).make()
							);
					}
				
				return 0;
				}
			
			final VariantContextBuilder vcb = new VariantContextBuilder();
			vcb.chr(ctx.getContig());
			vcb.start(ctx.getStart());
			vcb.stop(ctx.getEnd());
			vcb.attribute(VCFConstants.END_KEY, ctx.getEnd());
			vcb.attribute("SVLEN", ctx.getLengthOnReference());
			final String svType= getSvtype(ctx);
			vcb.attribute(VCFConstants.SVTYPE, svType);
			vcb.attribute("IMPRECISE", true);

			
			for(int side=0;side<2;side++)
				{
				final Function<VariantContext,Integer> coordExtractor;
				if(side==0)
					{
					coordExtractor = C->C.getStart();
					}
				else
					{
					coordExtractor = C->C.getEnd();
					}
				final List<Integer> list = Arrays.asList(
					candidates.stream().
						mapToInt(C->coordExtractor.apply(C)-coordExtractor.apply(ctx)).
						min().
						orElse(0),
					candidates.stream().
						mapToInt(C->coordExtractor.apply(C)-coordExtractor.apply(ctx)).
						max().
						orElse(0)
					);
				vcb.attribute(
						side==0?"CIPOS":"CIEND", 
						list
						);
				}
			
			final Allele ref=Allele.create("N", true);
			final Allele alt=Allele.create("<"+svType+">",false);
			
			vcb.alleles(Arrays.asList(ref,alt));
			
			out.add(vcb.make());
			return 0;
			}
		VariantContext ctx2=null;;
		final CloseableIterator<VariantContext> iter = vcfFilesInput.get(candidates.size()).query(
				ctx.getContig(),
				Math.max(1,ctx.getStart()-this.max_distance),
				ctx.getEnd()+this.max_distance
				);
		while(iter.hasNext()) {
			final VariantContext ctx3 = iter.next();
			if(testOverlapping(ctx3, ctx)) {
				ctx2=ctx3;
				break;
				}
			}
		iter.close();
		if(ctx2==null) return -1;
		candidates.add(ctx2);
		return recursive(ctx,candidates,vcfFilesInput,shadowControls,out);
		}
	
	
	@Override
	public int doWork(final List<String> args) {
		final List<VCFFileReader> casesFiles = new ArrayList<>();

		if(this.max_distance<0) {
			LOG.error("bad max_distance :" +this.max_distance);
			return -1;
			}
		VariantContextWriter out = null;
		try {
			final List<Path> casesPaths=(IOUtils.unrollPaths(args));
			if(casesPaths.isEmpty()) {
				LOG.error("cases list is empty");
				return -1;
				}
			
			if(!print_all_ctx && casesPaths.size()==1) {
				LOG.warning("One case: switching to --all");
				print_all_ctx=true;
			}
			
			if(this.controlsPath.size()==1 && this.controlsPath.get(0).toString().endsWith(".list")) {
				this.controlsPath = Files.lines(this.controlsPath.get(0)).
						filter(L->!(L.startsWith("#") || StringUtils.isBlank(L))).
						map(L->Paths.get(L)).
						collect(Collectors.toList());
				}
			
			SAMSequenceDictionary dict = null;
			
			final Set<VCFHeaderLine> metadata = new HashSet<>();
			
			for(int side=0;side<2;side++) {
			for(final Path input: (side==0?casesPaths:this.controlsPath)) {
				final VCFFileReader vcfInput = new VCFFileReader(input,true);
				
				final VCFHeader header = vcfInput.getFileHeader();
				
				if(side==0)
					{
					casesFiles.add(vcfInput);
					}
				else
					{
					vcfInput.close();
					}
				
				final SAMSequenceDictionary dict2 = SequenceDictionaryUtils.extractRequired(header);
				if(dict==null)
					{
					dict = dict2;
					}
				else if(!SequenceUtil.areSequenceDictionariesEqual(dict, dict2))
					{
					LOG.error(JvarkitException.DictionariesAreNotTheSame.getMessage(dict2, dict));
					return -1;
					}
				}
			}
			
			
			casesFiles.stream().
				flatMap(F->F.getFileHeader().getMetaDataInInputOrder().stream()).
				forEach(H->metadata.add(H));	
			
			
			VCFStandardHeaderLines.addStandardFormatLines(metadata, true, 
					VCFConstants.GENOTYPE_KEY
					);
			VCFStandardHeaderLines.addStandardInfoLines(metadata, true, 
					VCFConstants.END_KEY
					);
			
			metadata.add(new VCFInfoHeaderLine("SAMPLES",
					VCFHeaderLineCount.UNBOUNDED,
					VCFHeaderLineType.String,
					"Samples carrying the SV"
					));
			metadata.add(new VCFInfoHeaderLine("NSAMPLES",
					1,
					VCFHeaderLineType.Integer,
					"Number of Samples carrying the SV"
					));
			
			metadata.add(new VCFInfoHeaderLine("SVLEN",
					1,
					VCFHeaderLineType.Integer,
					"SV length"
					));
			metadata.add(new VCFInfoHeaderLine("CIPOS",
					2,
					VCFHeaderLineType.Integer,
					"Confidence interval around POS for imprecise variants"
					));
			metadata.add(new VCFInfoHeaderLine("CIEND",
					2,
					VCFHeaderLineType.Integer,
					"Confidence interval around END for imprecise variants"
					));
			
			metadata.add(new VCFInfoHeaderLine("IMPRECISE",
					0,
					VCFHeaderLineType.Flag,
					"Imprecise structural variation"
					));
			metadata.add(new VCFInfoHeaderLine(ATT_FILENAME,
					1,
					VCFHeaderLineType.String,
					"Source of variant"
					));
			metadata.add(new VCFInfoHeaderLine(ATT_CLUSTER,
					1,
					VCFHeaderLineType.String,
					"Variant cluster"
					));
			/*metadata.add(new VCFFormatHeaderLine(
					"OV",1,
					VCFHeaderLineType.Integer,
					"Number calls (with different sample) overlapping this genotype"
					));*/
			
			metadata.add(new VCFInfoHeaderLine(
					VCFConstants.SVTYPE,1,
					VCFHeaderLineType.String,
					"SV type"
					));
			metadata.add(new VCFFilterHeaderLine(ATT_CONTROL,
					"Variant is found in controls (max MAF="+this.max_maf+")"));
			
			final VCFHeader header = new VCFHeader(metadata);
			
			header.setSequenceDictionary(dict);
			JVarkitVersion.getInstance().addMetaData(this, header);
			
			
			final List<ShadowedVcfReader> controlShadowReaders = new ArrayList<>(this.controlsPath.size());
			for(int i=0;i< this.controlsPath.size();i++)
				{
				boolean large_flag = this.max_control_large_flag<0 || i>= this.max_control_large_flag;
				controlShadowReaders.add(new ShadowedVcfReader(this.controlsPath.get(i), large_flag));
				}
			
			out =  super.openVariantContextWriter(this.outputFile);
			out.writeHeader(header);
			final CloseableIterator<VariantContext> iter = casesFiles.get(0).iterator();
			final ProgressFactory.Watcher<VariantContext> progress = ProgressFactory.newInstance().dictionary(dict).logger(LOG).build();
			final Decoy decoy = Decoy.getDefaultInstance();
			VariantContext prevCtx = null;
			while(iter.hasNext()) {
				final VariantContext ctx= progress.apply(iter.next());
				if(decoy.isDecoy(ctx.getContig())) continue;
				
				if(Breakend.parse(ctx).stream().anyMatch(B->decoy.isDecoy(B.getContig()))) continue;
				
				// in manta, I see the same variant multiple times in the same vcf
				if(prevCtx!=null &&
					ctx.getContig().equals(prevCtx.getContig()) &&
					ctx.getStart() == prevCtx.getStart() &&
					ctx.getEnd() == prevCtx.getEnd()) continue;
					
				prevCtx=ctx;
				
				
				final List<VariantContext> candidate = new ArrayList<>(casesFiles.size());
				candidate.add(ctx);
				recursive(ctx,candidate,casesFiles,controlShadowReaders,out);
				}
			iter.close();
			progress.close();		
			
			out.close();
			out=null;
			casesFiles.stream().forEach(F->F.close());
			controlShadowReaders.stream().forEach(F->F.realClose());
			return 0;
		} catch(final Throwable err) {
			LOG.error(err);
			return -1;
		} finally {
			CloserUtil.close(out);
		}
		}
	
	
public static void main(final String[] args) {
	new ScanStructuralVariants().instanceMainWithExit(args);
}
}
