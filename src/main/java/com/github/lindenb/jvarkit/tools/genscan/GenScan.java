package com.github.lindenb.jvarkit.tools.genscan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.SynchronousLineReader;
import htsjdk.variant.utils.SAMSequenceDictionaryExtractor;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.picard.AbstractDataCodec;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.SortingCollection;

/**
 * GenScan
 *
 */
@Program(
	name="genscan",
	description="Paint a Genome Scan picture from a Tab delimited file (CHROM/POS/VALUE1/VALUE2/....).",
	keywords={"chromosome","reference","chart","visualization"})
public class GenScan extends AbstractGeneScan
	{
	private static final Logger LOG = Logger.build(GenScan.class).make();
	@Parameter(names={"-o","--output"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private File outputFile = null;
	@Parameter(names={"-R","--reference"},description=INDEXED_FASTA_REFERENCE_DESCRIPTION,required=true)
	private File faidxFile = null;
	@ParametersDelegate
	private WritingSortingCollection writingSortingCollection = new WritingSortingCollection();
 
    
	private final Map<String,ChromInfo> chrom2chromInfo=new HashMap<String,ChromInfo>();
	private SortingCollection<DataPoint> dataPoints=null;
	private boolean drawLines=true;
	private boolean firstLineIsHeader=true;

	
	private class DataPoint implements Comparable<DataPoint>
		{
		int tid;
		int pos;
		double values[]=new double[samples.size()];
		@Override
		public int compareTo(DataPoint other)
			{
			int i=this.tid-other.tid;
			if(i!=0) return i;
			i=this.pos-other.pos;
			if(i!=0) return i;
			return 0;
			}
		}
	
	private class DataPointCodec 
		extends AbstractDataCodec<DataPoint>
		{
		@Override
		public DataPoint decode(DataInputStream dis) throws IOException
			{
			int tid;
			try {  tid=dis.readInt();}
			catch(IOException err) { return null;}
			DataPoint dpt=new DataPoint();
			dpt.tid=tid;
			dpt.pos=dis.readInt();
			for(int i=0;i< dpt.values.length;++i)
				{
				 dpt.values[i]=dis.readDouble();
				}
			return dpt;
			}
		
		@Override
		public void encode(DataOutputStream out, DataPoint o)
				throws IOException
			{
			out.writeInt(o.tid);
			out.writeInt(o.pos);
			for(int i=0;i< o.values.length;++i)
				{
				out.writeDouble(o.values[i]);
				}
			}
		
		@Override
		public DataPointCodec clone()
			{
			return new DataPointCodec();
			}
		}
	
	
	
	/*
	protected int doWork()
		{
		CloseableIterator<DataPoint> iter=null;
		IndexedFastaSequenceFile faidx=null;
		try {
			this.chromInfos=new ArrayList<GenScan.ChromInfo>(dict.size());
			
			
			this.dataPoints= SortingCollection.newInstance(DataPoint.class,
					new DataPointCodec() , 
					new Comparator<DataPoint>()
						{
						@Override
						public int compare(DataPoint p0, DataPoint p1)
							{
							return p0.compareTo(p1);
							}
						},
					super.MAX_RECORDS_IN_RAM); 
			
			if(IN.isEmpty())
				{
				LOG.info("reading from stdin");
				read(System.in);
				}
			else
				{
				for(File f: IN)
					{
					LOG.info("reading "+f);
					InputStream in=IoUtil.openFileForReading(f);
					read(in);
					in.close();
					}
				}
			
			dataPoints.setDestructiveIteration(true);
			dataPoints.doneAdding();
			
			if(DICARD_UNSEEN_CHROM)
				{
				int n=0;
				while(n<this.chromInfos.size())
					{
					if(this.chromInfos.get(n).max_pos < this.chromInfos.get(n).min_pos)
						{
						this.chromInfos.remove(n);
						}
					else
						{
						n++;
						}		
					}
				}
			long genomeSize=0L;
			for(ChromInfo ci:this.chromInfos)
				{
				genomeSize+=ci.getSequenceLength();
				}
			
			double x=this.insets.left;
			for(ChromInfo ci:this.chromInfos)
				{
				ci.x=x;
				ci.width=(ci.getSequenceLength()/(double)genomeSize)*(WIDTH-(insets.left+insets.right));
				x=ci.x+ci.width;
				}
			double log10=Math.pow(10,Math.floor(Math.log10(this.max_value)));
			//max_value=(this.max_value/log10);
			this.max_value=Math.max(this.max_value,Math.ceil(this.max_value/log10)*log10);
			
			log10=Math.pow(10,Math.floor(Math.log10(this.min_value)));
			this.min_value=Math.min(this.min_value,Math.floor(this.min_value/log10)*log10);
			
			
		} catch (Exception e)
			{
			e.printStackTrace();
			return -1;
			}
		finally
			{
			CloserUtil.close(iter);
			CloserUtil.close(faidx);
			if(dataPoints!=null) dataPoints.cleanup();
			}
		return 0;
		}*/
	
	
	protected void drawPoints(Graphics2D g)
		{
		Point2D.Double[] previous=null;
		int prev_tid=-1;
		if(drawLines)
			{
			previous=new Point2D.Double[samples.size()];
			}
		CloseableIterator<DataPoint> iter=dataPoints.iterator();
		while(iter.hasNext())
			{
			DataPoint dpt=iter.next();
			ChromInfo ci=this.chromInfos.get(dpt.tid);
			if(!ci.visible) continue;
			double x=ci.x;
			if(drawLines && prev_tid!=ci.tid)
				{
				Arrays.fill(previous, null);
				prev_tid=ci.tid;
				}
			
			if(!super.DISCARD_BOUNDS)
				{
				x+=(((double)dpt.pos/ci.minmaxBase.getMax())*ci.width);
				}
			else
				{
				x+=((((double)dpt.pos-ci.minmaxBase.getMin())/ci.minmaxBase.getAmplitude())*ci.width);
				}
			
			for(int i=0;i< getSamples().size();++i)
				{
				Sample sample=getSamples().get(i);
				if(!sample.visible) continue;
				double value=dpt.values[i];
				if(Double.isNaN(value)) continue;
				if(!drawLines && !this.minMaxY.contains(value))
					{
					continue;
					}
				
				double y= sample.y + sample.height -sample.height*this.minMaxY.getFraction(value);
				
				g.setColor(i%2==0?Color.YELLOW:Color.GREEN);
				
				Shape clip=g.getClip();
				g.setClip(new Rectangle2D.Double(
						ci.x,sample.y,
						ci.width,sample.height
						));
				if(drawLines)
					{
					if(previous[i]!=null)
						{
						g.draw(new Line2D.Double(
								previous[i].x, previous[i].y,
								x, y
								));
						
						}
					previous[i]=new Point2D.Double(x,y);
					}
				else
					{
					g.draw(new Line2D.Double(x-1,y,x+1, y));
					g.draw(new Line2D.Double(x, y-1,x,y+1));
					}
				g.setClip(clip);
				g.setColor(Color.BLACK);
				}
			}
		iter.close();
		iter=null;
		}
	
	
	
	private void readTsv(final InputStream in) throws IOException
		{
		LOG.info("reading tsv");
		int nLines=0;
		Pattern tab=Pattern.compile("[\t]");
		
		LineIterator r= IOUtils.openStreamForLineIterator(in);
		boolean foundHeader=false;
		if(firstLineIsHeader)
			{
			if(!r.hasNext()) throw new IOException("First line/header is Missing");
			String line=r.next();
			String tokens[]=tab.split(line);
			if(tokens.length<4) throw new IOException("Expected at least 4 columns in "+line);
			for(int c=3;c<tokens.length;++c)
				{
				Sample sample=new Sample();
				sample.sample_id=this.samples.size();
				sample.name=tokens[c];
				this.samples.add(sample);
				LOG.info("Adding sample "+sample.name+" "+samples.size());
				}
			foundHeader=true;
			++nLines;
			}
		
		
		while(r.hasNext())
			{
			String line=r.next();
			++nLines;
			if(line.isEmpty() ) continue;
			String tokens[]=tab.split(line);
			if(tokens.length<4) throw new IOException("Expected at least 4 columns in "+line);
			if(!foundHeader && !firstLineIsHeader)
				{
				for(int c=3;c<tokens.length;++c)
					{
					Sample sample=new Sample();
					sample.sample_id=this.samples.size();
					sample.name="$"+(c-2);
					this.samples.add(sample);
					LOG.info("Adding sample "+sample.name+" "+samples.size());
					}
				foundHeader=true;
				}
			else
				{
				if(tokens.length!=(3+samples.size()))
					{
					throw new IOException(
							"Expected at least "+(3+samples.size())+" columns in "+line+" but got "+tokens.length);
					}
				}
			
			DataPoint pt=new DataPoint();
			ChromInfo ci=this.chrom2chromInfo.get(tokens[0]);
			if(ci==null)
				{
				if(this.faidxFile!=null) //dict provided by user
					{
					LOG.warning("chromosome "+tokens[0]+" was not defined in dictionary.");
					continue;
					}
				ci=new ChromInfo();
				ci.sequenceName=tokens[0];
				ci.tid=this.chromInfos.size();
				this.chromInfos.add(ci);
				this.chrom2chromInfo.put(tokens[0], ci);
				}
			
			pt.tid=ci.tid;
			
			try {
				pt.pos=Integer.parseInt(tokens[1]);
				if(pt.pos<0)
					{
					LOG.warning("Position <0 in "+line);
					continue;
					}
				if(this.faidxFile!=null && (pt.pos< 0 || pt.pos > ci.dictSequenceLength))
					{
					LOG.warning("Position 0<"+pt.pos+"<"+ci.dictSequenceLength+" out of range in "+line);
					continue;
					}
				
				
				}
			catch (Exception e) {
				LOG.warning("bad pos in  "+line);
				continue;
				}
			for(int i=0;i< samples.size();++i)
				{
				try {
					pt.values[i]=Double.parseDouble(tokens[3+i]);
					if(Double.isNaN(pt.values[i]))
						{
						LOG.warning("bad value in "+tokens[0]+":"+tokens[1]+":"+tokens[2]+"="+tokens[3+i]);
						}
					}
				catch (Exception e) {
					LOG.warning("bad value in "+tokens[0]+":"+tokens[1]+":"+tokens[2]+"="+tokens[3+i]);
					pt.values[i]=Double.NaN;
					}
				samples.get(i).minmax.visit(pt.values[i]);
				}
			this.dataPoints.add(pt);
			if(this.faidxFile==null)
				{
				ci.minmaxBase.visit(pt.pos);
				}
			}
		CloserUtil.close(r);
		LOG.info("num lines:"+nLines);
		}
	
	@Override
	protected List<ChromInfo> getChromInfos() {
		return this.chromInfos;
		}
	
	
	
	@Override
	public int doWork(final List<String> args) {
		if(faidxFile!=null)
			{
			LOG.error("Ref file missing");
			return -1;
			}
	
		InputStream in=null;
		try
			{
			
			final SAMSequenceDictionary dict= SAMSequenceDictionaryExtractor.extractDictionary(faidxFile);
			if(dict==null) {
				LOG.error("cannot get dict from "+faidxFile);
				return -1;
				}
			 dict.getSequences().stream().forEach(rec->
				{
				final ChromInfo ci=new ChromInfo();
				ci.dictSequenceLength=rec.getSequenceLength();
				ci.sequenceName=rec.getSequenceName();
				ci.tid=chromInfos.size();
				this.chromInfos.add(ci);
				this.chrom2chromInfo.put(ci.sequenceName, ci);
				});
			
			in = openInputStream(oneFileOrNull(args)); 			
			
			this.dataPoints =  SortingCollection.newInstance(
					DataPoint.class, 
					new DataPointCodec(),
					(o1,o2) -> o1.compareTo(o2),
					writingSortingCollection.getMaxRecordsInRam(),
					writingSortingCollection.getTmpDirectories()
					);

			
			readTsv(in);
			
			this.dataPoints.doneAdding();
			this.dataPoints.setDestructiveIteration(true);
			BufferedImage img=makeImage();
			this.dataPoints.cleanup();
			this.dataPoints=null;

			
			if(outputFile==null)
				{
				showGui(img);
				}
			else
				{
				ImageIO.write(img, "JPG", outputFile);
				}
			return 0;
			}
		catch(Exception err)
			{
			LOG.error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(in);
			try { if(this.dataPoints!=null) this.dataPoints.cleanup();}
			catch(Exception er){}
			}
		
		}
	public static void main(String[] args)
		{
		new GenScan().instanceMainWithExit(args);
		}
	
	}
