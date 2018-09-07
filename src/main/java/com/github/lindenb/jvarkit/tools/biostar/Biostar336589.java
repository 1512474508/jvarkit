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
package com.github.lindenb.jvarkit.tools.biostar;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.lang.CharSplitter;
import com.github.lindenb.jvarkit.lang.JvarkitException;
import com.github.lindenb.jvarkit.util.bio.bed.BedLine;
import com.github.lindenb.jvarkit.util.bio.bed.BedLineCodec;
import com.github.lindenb.jvarkit.util.bio.fasta.ContigNameConverter;
import com.github.lindenb.jvarkit.util.bio.fasta.ContigNameConverter.OnNotFound;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.ns.XLINK;
import com.github.lindenb.jvarkit.util.svg.SVG;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Locatable;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.utils.SAMSequenceDictionaryExtractor;
/**
BEGIN_DOC

## input

input is a BED file. https://genome.ucsc.edu/FAQ/FAQformat.html#format1

  * column 1: chrom
  * column 2: start
  * column 3: end
  * column 4 is the name
  * column 5 is the score [0-1000] or '.'
  * column 6 ignored
  * column 7 ignored
  * column 8 ignored
  * column 9 is '.' or R,G,B


## Example

```
$ wget -O - -q  "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/gap.txt.gz" |\
	gunzip -c | awk '{printf("%s\t%s\t%s\t%s\t%d\t+\t.\t.\t%s\n",$2,$3,$4,$8,rand()*1000,NR%20==0?"255,0,250":".");}' |\
	java -jar dist/biostar336589.jar -R src/test/resources/human_b37.dict   --url 'http://genome.ucsc.edu/cgi-bin/hgTracks?org=Human&db=hg19&position=__CHROM__%3A__START__-__END__' --title gaps -mr 300 -fh 20 > ~/jeter.svg 
```



END_DOC
 */
@Program(name="biostar336589",
description="displays circular map as SVG from BED and REF file",
keywords= {"genome","browser","circular","bed","svg"},
biostars=336589
)

public class Biostar336589 extends Launcher{
	private static final Logger LOG = Logger.build(Biostar336589.class).make();

	@Parameter(names={"-o","--out"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private File outputFile = null;
	@Parameter(names="-R",description=INDEXED_FASTA_REFERENCE_DESCRIPTION,required=true)
	private File faidx=null;
	@Parameter(names="-md",description="min distance in bp between two features on the same arc.")
	private int min_distance_bp =100;
	@Parameter(names="-mr",description="min internal radius")
	private double min_internal_radius =100;
	@Parameter(names="-fh",description="arc height")
	private double feature_height =10;
	@Parameter(names="-da",description="distance between arcs ")
	private double distance_between_arc =10;
	@Parameter(names="-ms",description="skip chromosome reference length lower than this value. ignore if <=0")
	private int skip_chromosome_size = -1;
	@Parameter(names={"-u","--url","--hyperlink"},description=
			"creates a hyperlink when 'click' in an area. "
			+ "The URL must contains __CHROM__, __START__ and __END__ that will be replaced by their values. "
			+ "IGV : \"http://localhost:60151/goto?locus=__CHROM__%3A__START__-__END__\" , "
			+ "UCSC: \"http://genome.ucsc.edu/cgi-bin/hgTracks?org=Human&db=hg19&position=__CHROM__%3A__START__-__END__\" "
			)
	private String hyperlinkType = "none";
	@Parameter(names={"--title"},description="document title")
	private String domTitle = Biostar336589.class.getSimpleName();

	private class Arc implements Locatable {
		int tid;
		int start;
		int end;
		String name;
		int score  =0;
		Color itemRgb = null;
		@Override
		public String getContig() {
			return dict.getSequence(this.tid).getSequenceName();
			}
		@Override
		public int getStart() {
			return start;
			}
		@Override
		public int getEnd() {
			return end;
			}
		
		
		}
	
	private SAMSequenceDictionary dict;
	private long reference_length;
	private long tid_to_start[];
	
	private final DecimalFormat decimalFormater = new DecimalFormat("##.##");
	private final DecimalFormat niceIntFormat = new DecimalFormat("###,###");

	
	private String format(double v) {
		return this.decimalFormater.format(v);
	}
	
	@Override
	public int doWork(final List<String> args) {
		if(this.faidx==null) {
			LOG.error("undefined REF");
			return -1;
		}
		if(this.min_internal_radius < 10) {
			this.min_internal_radius = 10;
		}
		if(this.feature_height < 2) {
			this.feature_height = 2;
		}
		int maxScore=0;
		final List<List<Arc>> rows = new ArrayList<>();
		BufferedReader br = null;
		PrintWriter out = null;
		try
			{
			this.dict = SAMSequenceDictionaryExtractor.extractDictionary(this.faidx.toPath());
			if(this.dict==null ) {
				throw new JvarkitException.DictionaryMissing(String.valueOf(this.faidx.toString()));
				}
			if(this.skip_chromosome_size > 0 )
				{
				this.dict = new SAMSequenceDictionary(
					this.dict.getSequences().stream().
					filter(S->S.getSequenceLength()> this.skip_chromosome_size).
					collect(Collectors.toList()));
				}
			if(this.dict.isEmpty()) {
				throw new JvarkitException.DictionaryMissing(String.valueOf(this.faidx.toString()));
				}
			this.reference_length = this.dict.getReferenceLength();
			this.tid_to_start = new long[this.dict.size()];
			Arrays.fill(this.tid_to_start,0L);
			
			long n = 0;
			for(int i=0;i< dict.size();i++) {
				this.tid_to_start[i] = n;
				n += dict.getSequence(i).getSequenceLength();
				}

			
			final Set<String> skipped_contigs = new HashSet<>();
			final ContigNameConverter converter = ContigNameConverter.fromOneDictionary(this.dict);
			converter.setOnNotFound(OnNotFound.SKIP);
			final BedLineCodec codec = new BedLineCodec();
			br = super.openBufferedReader(oneFileOrNull(args));
			String line;
			while((line=br.readLine())!=null)
				{
				if(StringUtil.isBlank(line) || BedLine.isBedHeader(line)) continue;
				final BedLine bedLine = codec.decode(line);
				final String newCtg = converter.apply(bedLine.getContig());
				if(StringUtil.isBlank(newCtg)) {
					if(skipped_contigs.add(bedLine.getContig())) {
						LOG.warn("unknown contig "+bedLine.getContig()+". Skipping.");
						}
					continue;
				}
				final SAMSequenceRecord ssr = this.dict.getSequence(newCtg);
				if(ssr==null) continue;
				if(bedLine.getStart() > ssr.getSequenceLength()) continue;
				if(bedLine.getEnd() < 1) continue;
				final Arc arc = new Arc();
				arc.tid = ssr.getSequenceIndex();
				arc.start = Math.max(bedLine.getStart(),0);
				arc.end = Math.min(bedLine.getEnd(),ssr.getSequenceLength());
				arc.name = bedLine.getOrDefault(3,"");
				try {
					final String s = bedLine.getOrDefault(4,"0");
					if(!StringUtil.isBlank(s)) arc.score= Math.min(1000,Math.max(0,Integer.parseInt(s)));
					maxScore = Math.max(maxScore, arc.score);
					}
				catch(final NumberFormatException err)
					{
					arc.score=0;
					}
				
				try {
					final String s = bedLine.getOrDefault(8,"");
					final String tokens[] = CharSplitter.COMMA.split(s);
					if(tokens.length==3)
						{
						final int r = Integer.parseInt(tokens[0]);
						final int g = Integer.parseInt(tokens[1]);
						final int b = Integer.parseInt(tokens[2]);
						if(r>=0 && r<256 && g>=0 && g<256 && b>=0 && b<256)
							{
							arc.itemRgb = new Color(r, g, b);
							}
						}
						
					}
				catch(final NumberFormatException err)
					{
					arc.itemRgb=null;
					}
				
				int y=0;
				for(y=0;y< rows.size();++y)
					{
					final List<Arc> row = rows.get(y);
					if(row.stream().noneMatch(A->A.withinDistanceOf(arc,min_distance_bp)))
						{
						row.add(arc);
						break;
						}
					}
				if(y==rows.size())
					{
					final List<Arc> row = new ArrayList<>();
					rows.add(row);
					row.add(arc);
					}
				}
			br.close();
			LOG.info("number of arcs : "+rows.size());
			
			final double img_radius = 
					this.min_internal_radius +
					(rows.size()+4)*(this.feature_height+this.distance_between_arc)
					;
			double radius = this.min_internal_radius;
			
			LOG.info("image radius : "+img_radius);

			
			out = super.openFileOrStdoutAsPrintWriter(this.outputFile);
			final XMLOutputFactory xof = XMLOutputFactory.newInstance();
			final XMLStreamWriter w = xof.createXMLStreamWriter(out);
			w.writeStartDocument("UTF-8", "1.0");
			w.writeStartElement("svg");
			w.writeAttribute("width", String.valueOf(Math.ceil(img_radius*2)));
			w.writeAttribute("height", String.valueOf(Math.ceil(img_radius*2)));
			w.writeDefaultNamespace(SVG.NS);
			w.writeNamespace("xlink", XLINK.NS);
			
			w.writeStartElement("style");
			w.writeCharacters(
					"g.maing {stroke:black;stroke-width:0.5px;fill:whitesmoke;font-size:10pt;}\n"+
					"path.feature {stroke:lightcoral;stroke-width:0.3px;fill:rosybrown;opacity:0.8;pointer-events:all;cursor:crosshair;}"+
					"path.contig0 {stroke:dimgray;stroke-width:0.8px;fill:gainsboro;}"+
					"path.contig1 {stroke:dimgray;stroke-width:0.8px;fill:lightgrey;}"+
					"text.contig {stroke:none;fill:steelblue;}"
					);
			w.writeEndElement();//style
			
			
			w.writeStartElement("script");
			
			final StringBuilder openBrowserFunction = new StringBuilder(
					"function openGenomeBrowser(contig,chromStart,chromEnd) {\n"
					);
			if( hyperlinkType.contains("__CHROM__") &&
				hyperlinkType.contains("__START__")	&&
				hyperlinkType.contains("__END__") &&
				!hyperlinkType.contains("\"")
				)
				{
				openBrowserFunction.append("var url=\""+this.hyperlinkType+"\".replace(/__CHROM__/g,contig).replace(/__START__/g,chromStart).replace(/__END__/g,chromEnd);\n");
				openBrowserFunction.append("window.open(url,'_blank');\n");

				}
			else
				{
				//nothing
				}
			openBrowserFunction.append("}\n");
			
			w.writeCData(
				openBrowserFunction.toString() +
				"function clicked(evt,contig,chromStart,chromEnd){\n" +
			    "    var e = evt.target;\n" +
			    "    var dim = e.getBoundingClientRect();\n" +
			    "    var x = 1.0 * evt.clientX - dim.left;\n" + 
			    "    var cLen = 1.0* (chromEnd - chromStart); if(cLen<1) cLen=1.0;\n" + 
			    "    var pos1 = chromStart + parseInt(((x+0)/dim.width)*cLen);\n" +
			    "    var pos2 = chromStart + parseInt(((x+1)/dim.width)*cLen);\n" +
			    "   openGenomeBrowser(contig,pos1,pos2);\n" +
			    "}\n");                
			w.writeEndElement();//script

			
			w.writeStartElement("title");
			w.writeCharacters(this.domTitle);
			w.writeEndElement();
			
			w.writeStartElement("g");
			w.writeAttribute("class", "maing");
			w.writeAttribute("transform", "translate("+format(img_radius)+","+format(img_radius)+")");
			w.writeStartElement("g");
			for(final List<Arc> row:rows)
				{
				w.writeStartElement("g");
				for(final Arc arc: row)
					{
					final String clickedAttribute = "clicked(evt,\""+arc.getContig()+"\","+arc.getStart()+","+arc.getEnd()+")";
					
					w.writeStartElement("path");
					w.writeAttribute("d", arc(arc.tid,radius,arc.start,arc.end));

					w.writeAttribute("onclick", clickedAttribute);
					
					w.writeAttribute("class", "feature");
					if(arc.itemRgb!=null)
						{
						w.writeAttribute("style", "fill:rgb("
								+arc.itemRgb.getRed()+ ","+
								+arc.itemRgb.getGreen()+","+
								+arc.itemRgb.getBlue()+");"
								);
						}
					else if(maxScore>0)
						{
						final int g = (int)((arc.score/(double)maxScore)*255);
						w.writeAttribute("style", "fill:rgb(" +
								g+ ","+ +g+","+ +g+
								")");
						}
					

					
					
						w.writeStartElement("title");
						if(!StringUtil.isBlank(arc.name))
							{
							w.writeCharacters(arc.name);
							}
						else
							{
							w.writeCharacters(arc.getContig()+":"+
									niceIntFormat.format(arc.getStart())+"-"+
									niceIntFormat.format(arc.getEnd())
									);
							}
						w.writeEndElement();
					
					w.writeEndElement();//path
					
					}
				w.writeEndElement();//g
				radius += this.distance_between_arc+this.feature_height;
				}
			w.writeEndElement();//g
			w.writeStartElement("g");
			radius+=this.feature_height;
			for(int tid=0;tid< dict.size();++tid)
				{
				final SAMSequenceRecord ssr = this.dict.getSequence(tid);
				w.writeStartElement("path");
				w.writeAttribute("class", "contig"+(tid%2));
				w.writeAttribute("d", arc(tid,radius,0,ssr.getSequenceLength()));

				w.writeStartElement("title");
				w.writeCharacters(ssr.getSequenceName());
				w.writeEndElement();
					
				
				w.writeEndElement();//path
				
				w.writeStartElement("text");
				w.writeAttribute("class", "contig");
				w.writeAttribute("x", format(radius+this.distance_between_arc+this.feature_height));
				w.writeAttribute("y", "0");
				w.writeAttribute("transform","rotate("+format(Math.toDegrees(
						refpos2ang(this.tid_to_start[tid]+(long)(ssr.getSequenceLength()/2.0))
						))+")");
				w.writeCharacters(ssr.getSequenceName());
				w.writeEndElement();
				}
			w.writeEndElement();//g
			
			w.writeEndElement();//g
			
			w.writeEndElement();//svg
			w.writeEndDocument();
			
			
			w.flush();
			w.close();
			CloserUtil.close(w);
			return 0;
			}
		catch(final Exception err)
			{
			LOG.error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(br);
			}
	
		}
	private String pointToStr(final Point2D.Double p)
		{
		return format(p.x)+" "+format(p.y);
		}
	
	private double refpos2ang(long pos) {
		return (pos/((double)this.reference_length))*(2.0*Math.PI);
		}
	
	private String arc(final int tid,final double radius,final int start,final int end) {
		final long index_at_start = this.tid_to_start[tid];
		
		final double r_start = refpos2ang(index_at_start+ start);
		final double r_end = refpos2ang(index_at_start+ end);
		final Point2D.Double p1 = polarToCartestian(radius, r_start);
		final Point2D.Double p2 = polarToCartestian(radius, r_end);
		final Point2D.Double p3 = polarToCartestian(radius+this.feature_height, r_end);
		final Point2D.Double p4 = polarToCartestian(radius+this.feature_height, r_start);
		final StringBuilder sb = new StringBuilder();
		sb.append("M ").
			append(pointToStr(p1));
		
		sb.append(" A ").
			append(format(radius)).
			append(" ").
			append(format(radius)).
			append(" 0"). //X axis rotation
			append(" 0").// large arc
			append(" 1 ").// sweep flag (positive angle direction)
			append(pointToStr(p2));
		
		sb.append(" L").append(pointToStr(p3));
		
		sb.append(" A ").
		append(format(radius+this.feature_height)).
		append(" ").
		append(format(radius+this.feature_height)).
		append(" 0"). //X axis rotation
		append(" 0").// large arc
		append(" 0 ").// sweep flag (positive angle direction)
		append(pointToStr(p4));
		
		sb.append(" L ").append(pointToStr(p1));
		
		sb.append(" Z");
		
		return sb.toString();
		}
	
	private Point2D.Double polarToCartestian(double radius,double angle)
		{
		return new Point2D.Double(
				Math.cos(angle)*radius,
				Math.sin(angle)*radius
				);
		}
	
	public static void main(final String[] args) {
		new Biostar336589().instanceMainWithExit(args);
	}
}
