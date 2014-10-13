/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum PhD.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

*/
package com.github.lindenb.jvarkit.tools.misc;

import java.io.File;
import java.io.PrintStream;

import htsjdk.samtools.fastq.FastqConstants;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloserUtil;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.AbstractCommandLineProgram;
import com.github.lindenb.jvarkit.util.bio.AcidNucleics;
import com.github.lindenb.jvarkit.util.picard.FastqReader;
import com.github.lindenb.jvarkit.util.picard.FourLinesFastqReader;

public class FastqRevComp extends AbstractCommandLineProgram
	{
	private boolean only_R1=false;
	private boolean only_R2=false;

	private FastqRevComp()
		{
		}
	
	@Override
	protected String getOnlineDocUrl() {
		return "https://github.com/lindenb/jvarkit/wiki/FastqRevComp";
		}
	
	@Override
	public String getProgramDescription() {
		return "produces a reverse-complement fastq (for mate pair alignment see http://seqanswers.com/forums/showthread.php?t=5085 )";
		}
	
	
	
	
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -o (fileout) Filename output . Optional ");
		out.println(" -1 interleaced input : only reverse complement R1 . Optional ");
		out.println(" -2 interleaced input : only reverse complement R2 . Optional ");
		super.printOptions(out);
		}
	
	
	
	private void run(FastqReader r,PrintStream out)
		{
		String s;
		long nRec=0L;
		r.setValidationStringency(ValidationStringency.LENIENT);
		while(r.hasNext())
			{
			if(++nRec%1E6==0)
				{
				info("N-Reads:"+nRec);
				}
			FastqRecord fastq=r.next();
			
			
			out.print(FastqConstants.SEQUENCE_HEADER);
			out.println(fastq.getReadHeader());
			s=fastq.getReadString();

			if((this.only_R2 && nRec%2==1) || (this.only_R1 && nRec%2==0) ) //interleaced
				{
				out.print(s);
				}
			else
				{
				for(int i=s.length()-1;i>=0;i--)
					{
					out.print(AcidNucleics.complement(s.charAt(i)));
					}
				}
			out.println();
			
			out.print(FastqConstants.QUALITY_HEADER);
			s=fastq.getBaseQualityHeader();
			if(s!=null) out.print(s);
			out.println();
			
			s=fastq.getBaseQualityString();
			if((this.only_R2 && nRec%2==1) || (this.only_R1 && nRec%2==0) ) //interleaced
				{
				out.print(s);
				}
			else
				{
				for(int i=s.length()-1;i>=0;i--)
					{
					out.print(s.charAt(i));
					}
				}
			out.println();
			if(out.checkError()) break;
			}
		out.flush();
		info("Done. N-Reads:"+nRec);
		}
	
	@Override
	public int doWork(String[] args)
		{
		File fileout=null;
		com.github.lindenb.jvarkit.util.cli.GetOpt getopt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=getopt.getopt(args, getGetOptDefault()+"o:12"))!=-1)
			{
			switch(c)
				{
				case 'o': fileout=new File(getopt.getOptArg());break;
				case '1': only_R1=true; break;
				case '2': only_R2=true; break;
				default: 
					{
					switch(handleOtherOptions(c, getopt, args))
						{
						case EXIT_FAILURE: return -1;
						case EXIT_SUCCESS: return 0;
						default: break;
						}
					}
				}
			}
		if(only_R1 && only_R2)
			{
			error("Both options -1 && -2 used.");
			return -1;
			}
		PrintStream out=System.out;
		try
			{
			if(fileout!=null)
				{
				info("Writing to "+fileout);
				out=new PrintStream(IOUtils.openFileForWriting(fileout));
				}
			else
				{
				info("Writing to stdout");
				out=System.out;
				}
			
			if(getopt.getOptInd()==args.length)
				{
				info("Reading from stdin");
				FastqReader fqR=new FourLinesFastqReader(System.in);
				run(fqR,out);
				fqR.close();
				}
			else for(int optind=getopt.getOptInd(); optind < args.length; ++optind)
				{
				File f=new File(args[optind]);
				info("Reading from "+f);
				FastqReader fqR=new FourLinesFastqReader(f);
				run(fqR,out);
				fqR.close();
				}
			out.flush();
			}
		catch(Exception err)
			{
			error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(out);
			}
		return 0;
		}
	
	public static void main(String[] args) {
		new FastqRevComp().instanceMainWithExit(args);

	}

}
