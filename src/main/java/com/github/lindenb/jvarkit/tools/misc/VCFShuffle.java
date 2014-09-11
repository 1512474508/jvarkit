package com.github.lindenb.jvarkit.tools.misc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Random;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.SortingCollection;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.AbstractVCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.picard.AbstractDataCodec;
import com.github.lindenb.jvarkit.util.vcf.AbstractVCFFilter2;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.LineReader;
import htsjdk.tribble.readers.LineReaderUtil;


public class VCFShuffle extends AbstractVCFFilter2
	{
	
	private static class RLine
		{
		long rand;
		String line;
		}
	
	private static class RLineCodec
		extends AbstractDataCodec<RLine>
		{
		@Override
		public RLine decode(DataInputStream dis) throws IOException
			{
			RLine r=new RLine();
			try
				{
				r.rand=dis.readLong();
				}
			catch(IOException err)
				{
				return null;
				}
			r.line=readString(dis);
			return r;
			}
		@Override
		public void encode(DataOutputStream dos, RLine object)
				throws IOException {
			dos.writeLong(object.rand);
			writeString(dos,object.line);
			}
		@Override
		public AbstractDataCodec<RLine> clone() {
			return new RLineCodec();
			}
		}
	
	private static class RLineCmp
		implements Comparator<RLine>
		{
		@Override
		public int compare(RLine o1, RLine o2) {
			int i= o1.rand<o2.rand?-1:o1.rand>o2.rand?1:0;
			if(i!=0) return i;
			return o1.line.compareTo(o2.line);
			}
		}

	
	private VCFShuffle()
		{
		}
	

	@Override
	public String getProgramDescription() {
		return "Shuffle a VCF";
		}
	@Override
	protected String getOnlineDocUrl() {
		return "https://github.com/lindenb/jvarkit/wiki/VcfShuffle";
		}
	
	

	@Override
	protected void doWork(VcfIterator in, VariantContextWriter out)
			throws IOException {
		throw new IllegalStateException("Should never happen");//because I'm using a LineIterator below
		}
	
	
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -T (dir) tmp directory. Optional.");
		out.println(" -N (long) random seed. Optional.");
		out.println(" -m (int) max records in ram. Optional");
		super.printOptions(out);
		}
	
	@Override
	public int doWork(String[] args)
		{
		int maxRecordsInRAM=50000;
		File tmpFile=null;
		long seed=System.currentTimeMillis();
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+ "T:N:m:"))!=-1)
			{
			switch(c)
				{
				case 'T': tmpFile=new File(opt.getOptArg()); break;
				case 'N': seed=Long.parseLong(opt.getOptArg()); break;
				case 'm': maxRecordsInRAM=Math.max(10, Integer.parseInt(opt.getOptArg())); break;
				default: 
					{
					switch(handleOtherOptions(c, opt, null))
						{
						case EXIT_FAILURE:return -1;
						case EXIT_SUCCESS: return 0;
						default:break;
						}
					}
				}
			}
		if(tmpFile==null)
			{
			tmpFile=new File(System.getProperty("java.io.tmpdir"));
			}
		
		VariantContextWriter out=null;
		LineIterator lineIter=null;
		LineReader lr=null;
		SortingCollection<RLine> shuffled=null;

		try
			{
			if(opt.getOptInd()==args.length)
				{
				info("reading from stdin.");
				lr=LineReaderUtil.fromBufferedStream(System.in);
				}
			else if(opt.getOptInd()+1==args.length)
				{
				String filename=args[opt.getOptInd()];
				info("reading from "+filename);
				lr=LineReaderUtil.fromBufferedStream(IOUtils.openURIForReading(filename));
				}
			else
				{
				error("Illegal number of arguments.");
				return -1;
				}
			lineIter=new LineIteratorImpl(lr);
			info("Writing to stdout");
			out=createVariantContextWriter(null);
			
			AbstractVCFCodec vcfCodec = VCFUtils.createDefaultVCFCodec();
			Random random=new Random(seed);
			
			VCFHeader header=(VCFHeader)vcfCodec.readActualHeader(lineIter);
			header.addMetaDataLine(new VCFHeaderLine("VCFShuffle.Version",String.valueOf(getVersion())));
			out.writeHeader(header);
			info("shuffling");
			
			shuffled=SortingCollection.newInstance(
					RLine.class,
					new RLineCodec(),
					new RLineCmp(),
					maxRecordsInRAM,
					tmpFile
					);
			shuffled.setDestructiveIteration(true);
			while(lineIter.hasNext())
				{
				RLine rLine=new RLine();
				rLine.rand=random.nextLong();
				rLine.line=lineIter.next();
				shuffled.add(rLine);
				}
			shuffled.doneAdding();
			info("done shuffling");
			
			CloseableIterator<RLine> iter=shuffled.iterator();
			while(iter.hasNext())
				{
				VariantContext ctx=vcfCodec.decode(iter.next().line);
				out.add(ctx);
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
			if(shuffled!=null) shuffled.cleanup();
			CloserUtil.close(lineIter);
			CloserUtil.close(lr);
			CloserUtil.close(out);
			}
		
			
		}

	public static void main(String[] args)
		{
		new VCFShuffle().instanceMainWithExit(args);
		}
	}
