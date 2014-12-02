/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

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


History:
* 2014 creation

*/
package com.github.lindenb.jvarkit.tools.misc;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.filter.FilteringIterator;
import htsjdk.samtools.filter.SamRecordFilter;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.SequenceUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.AbstractCommandLineProgram;
import com.github.lindenb.jvarkit.util.BufferedList;
import com.github.lindenb.jvarkit.util.Hershey;
import com.github.lindenb.jvarkit.util.picard.MergingSamRecordIterator;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;

public class BamCmpCoverage extends AbstractCommandLineProgram
	{
	private Map<String,Integer> sample2column=new HashMap<>();
	private int imgageSize=1000;
	private BufferedImage image=null;
	private double marginWidth=0;
	private double sampleWidth=0;
	private int minDepth=0;
	private int maxDepth=1000;
	/** restrict to BED */
	IntervalTreeMap<Boolean> intervals=null;
	
	
	/** delegate  bitmap for a matrix comparing two samples */
	private class BitDepthMatrix
		{
		/** owner bitmap */
		private BitSampleMatrix owner;
		/** index sample x in owner matrix */
		private int sample_x;
		/** index sample y in owner matrix */
		private int sample_y;
		BitDepthMatrix(BitSampleMatrix owner)
			{
			this.owner=owner;
			}
		public boolean getXY(int x,int y)
			{
			int left= sample_x*owner.bitSize +x;
			int top = sample_y*owner.bitSize +y ;
			
			int bit_index= left + top *owner.getWidth();
			if(bit_index> owner.getWidth()*owner.getWidth()) throw new RuntimeException();
			return this.owner.bitSet.get(bit_index);
			}
		private int convertDepthToPix(int depth)
			{	
			return (int)((((double)depth)/(double)(maxDepth-minDepth))*owner.bitSize);
			}

		public void setDepth(int depthx,int depthy)
			{
			if(depthx>=(maxDepth-minDepth)) return;
			if(depthy>=(maxDepth-minDepth)) return;
			int scaledx=convertDepthToPix(depthx);
			int scaledy=convertDepthToPix(depthy);
			int tx = this.sample_x*owner.bitSize + scaledx ;
			int ty =(this.sample_y*owner.bitSize + scaledy) *owner.getWidth(); 
			
			
			int bit_index= (int)(tx+ty);
			if(bit_index> owner.getWidth()*owner.getWidth())
				{
				throw new RuntimeException(
						" sample "+this.sample_x+"/"+this.sample_y+" "+
						"bit index:"+bit_index+" for "+depthx+"/"+depthy+" "+owner.bitSet.size());
				}
			
			this.owner.bitSet.set(bit_index);
			}
		}
	
	/** stores a bitmap for a matrix of 'N' sample */
	private class BitSampleMatrix
		{
		private BitSet bitSet;
		/** number of pixels required for a square in the bitmap */
		private int bitSize;
		/** number of samples */
		private int n_samples;
		/** bitmap */
		private BitDepthMatrix depthMatrix=null;
		
		
		BitSampleMatrix(int n_samples)
			{
			/* max-min coverage */
			int diffDepth=  BamCmpCoverage.this.maxDepth-BamCmpCoverage.this.minDepth;
			info("diffDepth:"+diffDepth);
			
			/* square size according to final image size */
			int squarePixel = (int)Math.ceil(BamCmpCoverage.this.sampleWidth);
			info("squarePixel:"+squarePixel+" ("+BamCmpCoverage.this.sampleWidth+")");
			
			/* we use the minimal size */
			this.bitSize= Math.min(diffDepth,squarePixel);
			if(this.bitSize <1) this.bitSize=1;
			info("bitSize:"+bitSize);
			this.n_samples=n_samples;
			int matrix_size =(n_samples*n_samples)*(this.bitSize*this.bitSize); 
			info("Alloc memory for biset size:"+matrix_size);
			this.bitSet = new BitSet(matrix_size);
			this.depthMatrix=new BitDepthMatrix(this);
			}
		
		public int getWidth()
			{
			return this.n_samples*this.bitSize;
			}
		
		
		public BitDepthMatrix get(int sample_x,int sample_y)
			{
			this.depthMatrix.sample_x=sample_x;
			this.depthMatrix.sample_y=sample_y;
			return this.depthMatrix;
			}
		}
	
	private class Depth
		{
		int tid=0;
		int pos=0;
		int depths[]=new int[sample2column.size()];
		public String toString()
			{
			return "("+(tid+1)+"):"+pos;
			}
		}

	private void paint(Graphics2D g,BitDepthMatrix matrix)
		{
		final Line2D.Double segment=new Line2D.Double();
		for(int i=0;i< matrix.owner.bitSize;++i)
			{
			for(int j=0;j<  matrix.owner.bitSize;++j)
				{	
				if(!matrix.getXY(i, j)) continue;
				
				double x=this.marginWidth+matrix.sample_x*this.sampleWidth;
				x+= this.sampleWidth*(i)/((double)matrix.owner.bitSize);

				
				double y=this.marginWidth+matrix.sample_y*this.sampleWidth;
				y+= this.sampleWidth*(j)/((double)matrix.owner.bitSize);
				
				segment.x1= x;
				segment.y1= y;
				segment.x2= x+0.5;
				segment.y2= y+0.5;
				g.draw(segment);
				}
			}
		}
	
	private void paint(BitSampleMatrix g,final Depth depth)
		{
		final int sampleUnit= this.maxDepth-this.minDepth;
		
		for(int i=0;i< depth.depths.length;++i)
			{		
			int di = depth.depths[i]-this.minDepth;
			if(di<0 || di>=sampleUnit) continue;
			
			
			
			for(int j=0;j< depth.depths.length;++j)
				{	
				int dj = depth.depths[j]-this.minDepth;
				if(dj<0 || dj>=sampleUnit) continue;
				
				g.get(i, j).setDepth(di, dj);
				g.get(j, i).setDepth(dj, di);
				
				}
			}
		}
	
	@Override
	protected String getOnlineDocUrl() {
		return "https://github.com/lindenb/jvarkit/wiki/BamCmpCoverage";
		}
	
	@Override
	public String getProgramDescription() {
		return "Creates an image for a comparative view of the depths sample vs sample.";
		}
	
	@Override
	public void printOptions(java.io.PrintStream out)
		{
		out.println(" -o (file.png) output image name . Required");
		out.println(" -w (int) image width . Default: "+this.imgageSize);
		out.println(" -m (int) min depth . Default: "+this.minDepth);
		out.println(" -M (int) max depth . Default: "+this.maxDepth);
		out.println(" -r (chrom:start-end) region . Optional.");
		out.println(" -b (file) restrict to BED file . Optional.");
		super.printOptions(out);
		}
	
	private void readBedFile(String bedFile)
		{
		if(this.intervals==null)
			{
			intervals=new IntervalTreeMap<Boolean>();
			}
		try
			{
			info("Reading "+bedFile);
			BufferedReader r=IOUtils.openURIForBufferedReading(bedFile);
			String line;
			Pattern tab=Pattern.compile("[\t]");
			while((line=r.readLine())!=null)
				{
				if(line.startsWith("#") || line.isEmpty()) continue;
				String tokens[]=tab.split(line);
				if(tokens.length<3)
					{
					throw new IOException("Bad bed line in "+bedFile+" "+line);
					}
				Interval interval=new Interval(tokens[0],
						Integer.parseInt(tokens[1])+1,
						Integer.parseInt(tokens[2])
						);
				intervals.put(interval,true);
				}
			CloserUtil.close(r);
			}
		catch(IOException err)
			{
			throw new RuntimeException(err);	
			}
		}
	
	@Override
	public int doWork(String[] args)
		{
		String regionStr=null;
		File imgOut=null;
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+"b:o:w:m:M:r:"))!=-1)
			{
			switch(c)
				{
				case 'b': readBedFile(opt.getOptArg());break;
				case 'o': imgOut=new File(opt.getOptArg());break;
				case 'w': this.imgageSize=Integer.parseInt(opt.getOptArg());break;
				case 'm': this.minDepth=Integer.parseInt(opt.getOptArg());break;
				case 'M': this.maxDepth=Integer.parseInt(opt.getOptArg());break;
				case 'r': regionStr=opt.getOptArg();break;
				default:
					{
					switch(handleOtherOptions(c, opt,args))
						{
						case EXIT_FAILURE: return -1;
						case EXIT_SUCCESS: return 0;
						default:break;
						}
					}
				}
			}
		if(imgOut==null)
			{
			error("output image file not defined");
			return -1;
			}
		
		if(this.imgageSize<1)
			{
			error("Bad image size:" +this.imgageSize);
			return -1;
			}
		
		if(this.minDepth<0)
			{
			error("Bad min depth : "+this.minDepth);
			return -1;
			}
		if(this.minDepth>=this.maxDepth)
			{
			error("Bad min<max depth : "+this.minDepth+"<"+this.maxDepth);
			return -1;
			}
		
		final SamRecordFilter filter=new SamRecordFilter()
			{
			@Override
			public boolean filterOut(SAMRecord first, SAMRecord second) {
				return filterOut(first);
				}
			
			@Override
			public boolean filterOut(SAMRecord rec) {
				if(rec.getReadUnmappedFlag()) return true;
				if(rec.isSecondaryOrSupplementary()) return true;
				if(rec.getDuplicateReadFlag())return true;
				if(rec.getNotPrimaryAlignmentFlag()) return true;
				if(rec.getReadFailsVendorQualityCheckFlag())return true;
				return false;
				}
			};
		Set<File> files=new HashSet<File>();
		try
			{
			SamReaderFactory srf=SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
			srf.disable(SamReaderFactory.Option.EAGERLY_DECODE);
			srf.disable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS);
			srf.disable(SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS);
			
			for(int i=opt.getOptInd();i< args.length;++i)
				{
				File f=new File(args[i]);
				if(f.getName().endsWith(".list"))
					{
					info("Reading BAM list from "+f);
					BufferedReader in=IOUtils.openFileForBufferedReading(f);
					String line;
					while((line=in.readLine())!=null)
						{
						if(line.trim().isEmpty() || line.startsWith("#")) continue;
						files.add(new File(line));
						}
					in.close();
					}
				else
					{
					files.add(f);
					}
				}
			if(files.isEmpty())
				{
				error("No BAM defined");
				return -1;
				}
			
			Comparator<SAMRecord> comparator=new Comparator<SAMRecord>()
				{
				@Override
				public int compare(SAMRecord samRecord1, SAMRecord samRecord2)
					{
					final int refIndex1 = samRecord1.getReferenceIndex();
			        final int refIndex2 = samRecord2.getReferenceIndex();
			        if (refIndex1 == -1) {
			            return (refIndex2 == -1? 0: 1);
			        } else if (refIndex2 == -1) {
			            return -1;
			        }
			        final int cmp = refIndex1 - refIndex2;
			        if (cmp != 0)
			        	{
			            return cmp;
			        	}
			        return samRecord1.getAlignmentStart() - samRecord2.getAlignmentStart();
					}
				};
			List<SamReader> readers=new ArrayList<SamReader>(files.size());
			List<CloseableIterator<SAMRecord>> iterators=new ArrayList<CloseableIterator<SAMRecord>>(files.size());
			

			
			Set<String> samples=new TreeSet<String>();
			SAMSequenceDictionary dict=null;
			//scan samples names
			for(File bamFile:files)
				{
				SamReader r= srf.open(bamFile);
				readers.add(r);
				
				SAMFileHeader h=r.getFileHeader();
				if(h.getSortOrder()!=SortOrder.coordinate)
					{
					r.close();
					error("file "+bamFile+" not sorted on coordinate");
					return -1;
					}
				if(dict==null)
					{
					dict=h.getSequenceDictionary();
					}
				else if(!SequenceUtil.areSequenceDictionariesEqual(dict,h.getSequenceDictionary()))
					{
					error("Found more than one dictint sequence dictionary");
					return -1;
					}

				
				for(SAMReadGroupRecord rg:h.getReadGroups())
					{
					String sample=rg.getSample();
					if(sample==null) continue;
					samples.add(sample);
					}
				
				if(regionStr==null)
					{
					iterators.add(new FilteringIterator(r.iterator(),filter));
					}
				else
					{
					int colon=regionStr.indexOf(':');
					String chrom;
					int chromStart;
					int chromEnd;

					if(colon==-1)
						{
						chrom=regionStr;
						}
					else
						{
						chrom=regionStr.substring(0,colon);
						}
					
					SAMSequenceRecord ssr= dict.getSequence(chrom);
					if(ssr==null)
						{
						error("Chromosome "+chrom+" not present in dictionary");
						return -1;
						}
					int hyphen=regionStr.indexOf('-', colon+1);
					if(hyphen!=-1)
						{
						chromStart=Integer.parseInt(regionStr.substring(colon+1,hyphen));
						chromEnd=Integer.parseInt(regionStr.substring(hyphen+1));
						}
					else
						{
						chromStart = 0;
						chromEnd = ssr.getSequenceLength()-1;
						}
					if(chromStart<0 || chromEnd<chromStart)
						{
						error("bad position in "+regionStr);
						return -1;
						}
					iterators.add(new FilteringIterator(r.query(chrom,chromStart,chromEnd,false),filter));
					
					}
				}
			info("Samples:"+samples.size());
			for(String sample:samples)
				{
				this.sample2column.put(sample, this.sample2column.size());
				}
			
			//create merging sam-reader
			MergingSamRecordIterator iter=new MergingSamRecordIterator(
					comparator,iterators);
			
			
			//create image
			info("Creating image "+this.imgageSize+"x"+this.imgageSize);
			this.image=new BufferedImage(this.imgageSize, this.imgageSize, BufferedImage.TYPE_INT_RGB);
			Graphics2D g=this.image.createGraphics();
			this.marginWidth=this.imgageSize*0.05;
			double drawingWidth=(this.imgageSize-1)-marginWidth;
			this.sampleWidth=drawingWidth/samples.size();
			//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.imgageSize, this.imgageSize);
			g.setColor(Color.BLACK);
			Hershey hershey =new Hershey();
			for(String sample_x:samples)
				{
				g.setColor(Color.BLACK);
				hershey.paint(g,
						sample_x,
						marginWidth + sample2column.get(sample_x)*sampleWidth,
						0.0,
						sampleWidth*0.9,
						marginWidth
						);
				
        		AffineTransform old=g.getTransform();
        		AffineTransform tr= AffineTransform.getTranslateInstance(
        				marginWidth,
        				marginWidth + sample2column.get(sample_x)*sampleWidth
        				);
        		tr.rotate(Math.PI/2);
        		g.setTransform(tr);
        		hershey.paint(g,
						sample_x,
						0.0,
						0.0,
						sampleWidth*0.9,
						marginWidth
						);        		//g.drawString(this.tabixFile.getFile().getName(),0,0);
        		g.setTransform(old);
				
				for(String sample_y:samples)
					{
					
					Rectangle2D rect=new Rectangle2D.Double(
							marginWidth + sample2column.get(sample_x)*sampleWidth,
							marginWidth + sample2column.get(sample_y)*sampleWidth,
							sampleWidth,
							sampleWidth
							);
					g.setColor(Color.GRAY);
					g.draw(new Line2D.Double(
							rect.getMinX(),rect.getMinY(),
							rect.getMaxX(),rect.getMaxY())
							);
					g.setColor(Color.BLACK);
					g.draw(rect);
					}
				}
			
			
			//ceate bit-array
			BitSampleMatrix bitMatrix=new BitSampleMatrix( samples.size());

						
			//preivous chrom
			//int prev_tid=-1;
			BufferedList<Depth> depthList=new BufferedList<Depth>();
			Stroke oldStroke=g.getStroke();
			g.setStroke(new BasicStroke(0.5f));
			g.setColor(Color.BLACK);
			SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(dict);
			info("Scanning bams...");
			while(iter.hasNext())
				{
				SAMRecord rec=iter.next();
				if(filter.filterOut(rec)) continue;
				progress.watch(rec);

				SAMReadGroupRecord gr=rec.getReadGroup();
				if(gr==null) continue;
				String sample=gr.getSample();
				if(sample==null) continue;
				int sample_id= this.sample2column.get(sample);
				
				Cigar cigar= rec.getCigar();
				if(cigar==null) continue;
				int refPos=rec.getAlignmentStart();
				
				
				/* cleanup front pos */
				while(!depthList.isEmpty())
					{
					Depth front=depthList.getFirst();

					
					if(front.tid!=rec.getReferenceIndex().intValue() ||
						front.pos < refPos )
						{
						paint(bitMatrix,front);
						depthList.removeFirst();
						continue;
						}
					else
						{
						break;
						}		
					}
				
				//ignore non-overlapping BED
				if(this.intervals!=null &&
					!this.intervals.containsOverlapping(
					new Interval(rec.getReferenceName(),
							refPos,
							rec.getAlignmentEnd()))
							)
					{
					continue;
					}

				
				
				for(CigarElement ce:cigar.getCigarElements())
					{
					CigarOperator op=ce.getOperator();
					if(!op.consumesReferenceBases()) continue;
					if(op.consumesReadBases())
						{
						for(int i=0;i< ce.getLength();++i)
							{
							Depth depth=null;
							int pos=refPos+i;
							
							//ignore non-overlapping BED
							if(this.intervals!=null &&
								!this.intervals.containsOverlapping(new Interval(rec.getReferenceName(),pos,pos)))
								{
								continue;
								}
							else if(depthList.isEmpty())
								{
								depth=new Depth();
								depth.pos=pos;
								depth.tid=rec.getReferenceIndex();
								depthList.add(depth);
								}
							else if(depthList.getLast().pos< pos)
								{
								Depth prev=depthList.getLast();

								while(prev.pos< pos)
									{
									depth=new Depth();
									depth.pos=prev.pos+1;
									depth.tid=rec.getReferenceIndex();
									depthList.add(depth);
									prev=depth;
									}
								depth=prev;
								}
							
							else
								{
								int lastPos=depthList.get(depthList.size()-1).pos;
								int distance= lastPos-pos;
								int indexInList=(depthList.size()-1)-(distance);
								if(indexInList<0)
									{
									//can appen when BED declared and partially overlap the read
									continue;
									}
								
								depth = depthList.get((depthList.size()-1)-(distance));
								if(depth.pos!=pos)
									{
									error(" "+pos+" vs "+depth.pos+" "+lastPos);
									System.exit(-1);
									}
								}
							depth.depths[sample_id]++;
							}
						}
					refPos+=ce.getLength();
					}
				}
			while(!depthList.isEmpty())
				{
				//paint(g,depthList.remove(0));
				paint(bitMatrix,depthList.remove(0));
				}
			progress.finish();
			iter.close();
			
			//paint bitset

		
			for(int x=0;x< bitMatrix.n_samples;++x)
				{
				for(int y=0;y< bitMatrix.n_samples;++y)
					{
					info("Painting...("+x+"/"+y+")");
					paint(g, bitMatrix.get(x, y));
					}
				}
			
			g.setStroke(oldStroke);
			g.dispose();
			//close readers
			for(SamReader r:readers) r.close();
			
			//save file
			if(imgOut.getName().toLowerCase().endsWith(".png"))
				{
				ImageIO.write(this.image, "PNG", imgOut);
				}
			else
				{
				ImageIO.write(this.image, "JPG", imgOut);
				}
			
			return 0;
			}
		catch(Exception err)
			{
			error(err);
			return -1;
			}
		finally
			{
			
			}
		}
	public static void main(String[] args) {
		new BamCmpCoverage().instanceMainWithExit(args);
	}
	}
