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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloserUtil;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.Counter;

public class FindAllCoverageAtPosition extends AbstractFindAllCoverageAtPosition
	{
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FindAllCoverageAtPosition.class);

	private static final char INSERTION_CHAR='^';
	private static final char DELETION_CHAR='-';
	private static final char BASES_To_PRINT[]=new char[]{'A','C','G','T','N',INSERTION_CHAR,DELETION_CHAR};

	
	private static class Mutation
		{
		String chrom;
		int pos;
		Mutation(String chrom,int pos)
			{
			this.chrom=chrom;
			this.pos=pos;
			}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + chrom.hashCode();
			result = prime * result + pos;
			return result;
			}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)return true;
			Mutation other = (Mutation) obj;
			if (pos != other.pos) return false;
			 if (!chrom.equals(other.chrom))
				return false;
			
			return true;
		}
		
		@Override
		public String toString() {
			return chrom+":"+pos;
			}
		
		}
	
	private static class CigarAndBases
		{
		Counter<CigarOperator> operators = new Counter<>();
		Counter<Character> bases = new Counter<>();
		}
	
	private Mutation mutation=null;
	private PrintWriter out=null;
	private SamReaderFactory samReaderFactory;
    public FindAllCoverageAtPosition()
    	{
    	samReaderFactory=  SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT);
    	}		
    
    private Mutation convertFromSamHeader(File f,SAMFileHeader h)
    	{
    	SAMSequenceDictionary dict=h.getSequenceDictionary();
    	if(dict==null)
    		{
    		LOG.warn("No dictionary in "+h);
    		return null;
    		}
    	SAMSequenceRecord rec=dict.getSequence(this.mutation.chrom);
    	if(rec!=null) return this.mutation;
    	String chromName=this.mutation.chrom;
		if(chromName.startsWith("chr"))
			{
			rec=dict.getSequence(chromName.substring(3));
			if(rec!=null) return new Mutation(rec.getSequenceName(),this.mutation.pos);
			}
		else
			{
			rec=dict.getSequence("chr"+chromName);
			if(rec!=null) return new Mutation(rec.getSequenceName(),this.mutation.pos);
			}
	
		if(chromName.equals("MT") &&  dict.getSequence("chrM")!=null)
			{
			return new Mutation("chrM",this.mutation.pos);
			}
		if(chromName.equals("chrM") &&  dict.getSequence("MT")!=null)
			{
			return new Mutation("MT",this.mutation.pos);
			}
    	return null;
    	}

    private void scan(final BufferedReader in) throws Exception
    	{

    	final String DEFAULT_SAMPLE_NAME="(undefined)";
    	String line;
    	while((line=in.readLine())!=null)
			{
    		if(out.checkError()) break;
			if(line.isEmpty() || line.startsWith("#")) continue;
			File f=new File(line);
			if(!f.isFile()) continue;
			if(!f.canRead()) continue;
			String filename=f.getName();
			if(!filename.endsWith(".bam")) continue;
			
    			
			SamReader samReader=null;
			SAMRecordIterator iter=null;
			try
				{
				samReader = this.samReaderFactory.open(f);
				if(!samReader.hasIndex())
					{
					LOG.warn("no index for "+f);
					continue;
					}
				final SAMFileHeader header=samReader.getFileHeader();
				final Map<String, CigarAndBases> sample2count=new TreeMap<String,CigarAndBases>();
				for(SAMReadGroupRecord rg:header.getReadGroups())
					{
					if(rg!=null)
						{
						String sn= rg.getSample();
						if(sn!=null && !sn.trim().isEmpty())
							{
							sample2count.put(sn, new CigarAndBases());
							}
						}
					}
				
				if(sample2count.isEmpty())
					{
					sample2count.put(DEFAULT_SAMPLE_NAME, new CigarAndBases());
					}
				
				Mutation m = convertFromSamHeader(f,header);
				if(m==null) continue;
				iter=samReader.query(m.chrom, m.pos-1, m.pos+1,	false);
				while(iter.hasNext())
					{
					SAMRecord rec=iter.next();
					if(rec.getReadUnmappedFlag()) continue;
					if(rec.getReadFailsVendorQualityCheckFlag()) continue;
					if(rec.getNotPrimaryAlignmentFlag()) continue;
					if(rec.isSecondaryOrSupplementary()) continue;
					if(rec.getDuplicateReadFlag()) continue;
					final Cigar cigar=rec.getCigar();
					if(cigar==null) continue;
					final String readString = rec.getReadString().toUpperCase();
					String sampleName=DEFAULT_SAMPLE_NAME;
					SAMReadGroupRecord rg=rec.getReadGroup();
					if(rg!=null)
						{
						String sn= rg.getSample();
						if(sn!=null && !sn.trim().isEmpty())
							{
							sampleName=sn;
							}
						}
					CigarAndBases counter= sample2count.get(sampleName);
					if(counter==null)
						{
						counter=new CigarAndBases();
						sample2count.put(sampleName, counter);
						}	
					
					
					int ref= rec.getUnclippedStart();
					int readPos = 0;
					for(int k=0;k<cigar.numCigarElements() && ref< m.pos+1;++k)
						{
						CigarElement ce=cigar.getCigarElement(k);
						CigarOperator op=ce.getOperator();
						switch(op)
							{
							case P: break;
							case I: 
								{
								if(ref==m.pos)
									{
									counter.operators.incr(op);
									counter.bases.incr(readString.charAt(INSERTION_CHAR));
									}
								readPos += ce.getLength();
								break;
								}
							case D:case N:
							case M: case X: case EQ: 
							case H:
							case S:
								{
								for(int i=0;i< ce.getLength();++i )
									{
									if(ref==m.pos)
										{
										counter.operators.incr(op);
										switch(op)
											{
											case M:case X:case EQ:
												counter.bases.incr(readString.charAt(readPos));
												break;
											case D:case N:
												counter.bases.incr(DELETION_CHAR);
												break;
											default:break;
											}
										break;
										}	
									if(op.consumesReadBases()) ++readPos;
									ref++;
									}
								break;
								}
							default: throw new RuntimeException("unknown operator:"+op);
							}
						}
					}
				
				
				for(final String sample:sample2count.keySet())
					{
					final CigarAndBases counter= sample2count.get(sample);
					
					out.print(f);
					out.print('\t');
					out.print(m.chrom);
					out.print('\t');
					out.print(m.pos);
					out.print('\t');
					out.print(sample);
					out.print('\t');
					out.print(
							counter.operators.count(CigarOperator.M)+
							counter.operators.count(CigarOperator.EQ)+
							counter.operators.count(CigarOperator.X)
							);
					for(CigarOperator op:CigarOperator.values())
						{
						out.print('\t');
						out.print(counter.operators.count(op));
						}
					for(char c:BASES_To_PRINT)
						{
						out.print('\t');
						out.print(counter.bases.count(c));
						}
					
					out.println();
					}
				}
			catch(Exception err)
				{
				LOG.error(err);
				throw err;
				}
			finally
				{
				CloserUtil.close(iter);
				CloserUtil.close(samReader);
				}    				
			}		
	    
    	}
    
	@Override
	public void printOptions(PrintStream out) {
		out.println(" -p chrom:pos . Add this chrom/position. Required");
		super.printOptions(out);
		}
	@Override
	public Collection<Throwable> call() throws Exception
		{
		final String s = getPositionStr();
		if(s==null || s.trim().isEmpty())
			{
			return wrapException("undefined position \'str\'");
			}
		
		int colon=s.indexOf(':');
		if(colon==-1 || colon+1==s.length())
			{
			return wrapException("Bad chrom:pos "+s);
			}
		
		String chrom=s.substring(0,colon).trim();
		if(chrom.isEmpty())
			{
			return wrapException("Bad chrom:pos "+s);
			}
		Mutation m=new Mutation(chrom, Integer.parseInt(s.substring(colon+1)));
		LOG.info("adding "+m);
		this.mutation = m;
		
		final List<String> args = getInputFiles();
		BufferedReader r = null;
		try
			{
			this.out=openFileOrStdoutAsPrintWriter();
			
			out.print("#File");
			out.print('\t');
			out.print("CHROM");
			out.print('\t');
			out.print("POS");
			out.print('\t');
			out.print("SAMPLE");
			out.print('\t');
			out.print("DEPTH");
			for(CigarOperator op:CigarOperator.values())
				{
				out.print('\t');
				out.print(op.name());
				}
			for(char c:BASES_To_PRINT)
				{
				out.print('\t');
				out.print("Base("+c+")");
				}
			
			out.println();

			
			if(args.isEmpty())
				{
				LOG.info("Reading from stdin");
				r = new BufferedReader(new InputStreamReader(stdin()));
				scan(r);
				r.close();
				r=null;
				}
			else
				{				
				for(final String filename: args)
					{
					LOG.info("Reading from "+filename);
					r=IOUtils.openURIForBufferedReading(filename);
					scan(r);
					r.close();
					r=null;
					}
				}
			this.out.flush();
			return RETURN_OK;
			}
		catch(Exception err)
			{
			return wrapException(err);
			}
		finally
			{
			CloserUtil.close(this.out);
			CloserUtil.close(r);
			}
		}
	
	/**
	 * main
	 */
	public static void main(String[] args) {
		new FindAllCoverageAtPosition().instanceMainWithExit(args);

	}

}
