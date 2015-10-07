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
* 2014-11 : handle clipped bases
* 2014 creation

*/
package com.github.lindenb.jvarkit.tools.sam2tsv;

import java.io.PrintStream;
import java.util.Collection;

import com.github.lindenb.jvarkit.util.command.Command;
import com.github.lindenb.jvarkit.util.picard.GenomicSequence;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;

import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMUtils;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.util.CloserUtil;

/**
 * https://github.com/lindenb/jvarkit/wiki/SAM2Tsv
 */
public class Sam2Tsv
	extends AbstractSam2Tsv
	{
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(Sam2Tsv.class);

	@Override
	public Command createCommand() {
		return new MyCommand();
		}
	
	static private class MyCommand extends AbstractSam2Tsv.AbstractSam2TsvCommand
		{    
		private IndexedFastaSequenceFile indexedFastaSequenceFile=null;
		private GenomicSequence genomicSequence=null;
		/** lines for alignments */
		private StringBuilder L1=null;
		private StringBuilder L2=null;
		private StringBuilder L3=null;
	
		private PrintStream out= stdout();
		
		private class Row
			{
			SAMRecord rec;
			byte readbases[];
			byte readQuals[];
	
			int readPos;
			int refPos;
			CigarOperator op;
			
			public char getRefBase()
				{
				if(refPos>=1 && refPos<= genomicSequence.length())
	 				{
					return genomicSequence.charAt(refPos-1);
	 				}
				return '.';
				}
			
			public char getReadBase()
				{
				return readPos==-1 || this.readPos>=this.readbases.length?'.':(char)this.readbases[this.readPos];
				}
			public char getReadQual()
				{
				byte c= readPos==-1 || this.readQuals==null || this.readPos>=this.readQuals.length?(byte)0:this.readQuals[this.readPos];
				return SAMUtils.phredToFastq(c);
				}
			
			}
		
		private void writeAln(final Row row)
				{
				char c1;
				char c3;
				this.out.print(row.rec.getReadName());
				this.out.print("\t");
				this.out.print(row.rec.getFlags());
				this.out.print("\t");
				this.out.print(row.rec.getReadUnmappedFlag()?".":row.rec.getReferenceName());
				this.out.print("\t");
				if(row.readPos!=-1)
					{
					c1 =row.getReadBase();
					this.out.print(row.readPos);
					this.out.print("\t");
					this.out.print(c1);
					this.out.print("\t");
					this.out.print(row.getReadQual());
					this.out.print("\t");
					}
				else
					{
					c1= '-';
					this.out.print(".\t.\t.\t");
					}
				
				if(row.refPos != -1)
					{
					c3 = row.getRefBase();
					this.out.print(row.refPos);
					this.out.print("\t");
					this.out.print(c3);
					this.out.print("\t");
					}
				else
					{
					c3= '-';
					this.out.print(".\t.\t");
					}
				this.out.print(row.op==null?".":row.op.name());
				this.out.println();
				
				if(this.printAlignment)
					{
					L1.append(c1);
					L3.append(c3);
					
					if(Character.isLetter(c1) &&  Character.toUpperCase(c1)== Character.toUpperCase(c3))
						{
						L2.append('|');
						}
					else
						{
						L2.append(' ');
						}
					}
				}
		
		private void printAln(final Row row)
			{
			final SAMRecord rec = row.rec;
			if(rec==null) return;
			Cigar cigar=rec.getCigar();
			if(cigar==null) return;
			
			row.readbases = rec.getReadBases();
			row.readQuals = rec.getBaseQualities();
			if(row.readbases==null )
				{
				row.op=null;
				row.refPos=-1;
				row.readPos=-1;
				writeAln(row);
				return;
				}
			if(rec.getReadUnmappedFlag())
				{
				row.op=null;
				row.refPos=-1;
				for(int i=0;i< row.readbases.length;++i)
					{
					row.readPos=i;
					writeAln(row);
					}
				return;
				}
			
			//fix hard clipped reads
			StringBuilder fixReadBases=new StringBuilder(row.readbases.length);
			StringBuilder fixReadQuals=new StringBuilder(row.readbases.length);
			int readIndex = 0;
			for (final CigarElement ce : cigar.getCigarElements())
			 {
			 CigarOperator op= ce.getOperator();
			 
			 for(int i=0;i< ce.getLength();++i)
				{
				if(op.equals(CigarOperator.H))
					{
					
					fixReadBases.append('*');
					fixReadQuals.append('*');
					}
				else if(!op.consumesReadBases())
					{
					break;
					}
				else
					{
					fixReadBases.append((char)row.readbases[readIndex]);
					fixReadQuals.append(
							row.readQuals==null ||
							row.readQuals.length<=readIndex ?
							'*':(char)row.readQuals[readIndex]);
					readIndex++;
					}
				}
			 }
			row.readbases = fixReadBases.toString().getBytes();
			row.readQuals = fixReadQuals.toString().getBytes();
	
			
			if(genomicSequence==null || !genomicSequence.getChrom().equals(rec.getReferenceName()))
				{
				genomicSequence=new GenomicSequence(this.indexedFastaSequenceFile, rec.getReferenceName());
				}
			
	
			 readIndex = 0;
			 int refIndex = rec.getUnclippedStart();
			 				 
			 for (final CigarElement e : cigar.getCigarElements())
				 {
				 row.op=e.getOperator();
				 
				 switch (e.getOperator())
					 {
					 case S :
					 case H : //length of read has been fixed previously, so same as 'S'
						 	{
						 	
					 		for(int i=0;i<e.getLength();++i)
					 			{
					 			row.readPos=  readIndex;
					 			row.refPos  = refIndex;
				 				writeAln(row);
					 			readIndex++;
					 			refIndex++;//because we used getUnclippedStart
					 			}
							break; 
						 	}
					 case P : 
						 	{
						 	row.refPos  = -1;
						 	row.readPos = -1;
					 		for(int i=0;i<e.getLength();++i)
					 			{
					 			writeAln(row);
					 			}
							break; 
						 	}
					 case I :
					 		{
					 		row.refPos  = -1;
					 		for(int i=0;i<e.getLength();++i)
					 			{
					 			row.readPos=readIndex;
					 			writeAln(row);
					 			readIndex++;
					 			}
					 		break;
					 		}
					 case N :  //cont. -- reference skip
					 case D :
					 		{
					 		row.readPos  = -1;
					 		for(int i=0;i<e.getLength();++i)
					 			{
					 			row.refPos = refIndex;
					 			writeAln(row);
					 			refIndex++;
					 			}
					 		break;
					 		}
					 case M :
					 case EQ :
					 case X :
				 			{
					 		for(int i=0;i< e.getLength();++i)
					 			{
					 			row.refPos = refIndex;
					 			row.readPos = readIndex;
					 			writeAln(row);
					 			refIndex++;
					 			readIndex++;
					 			}
					 		break;
				 			}
						
					 default : throw new IllegalStateException("Case statement didn't deal with cigar op: " + e.getOperator());
					 }
	
				 }
		
			
			
			 if(printAlignment)
					{
					
					int len=Math.max(rec.getReadNameLength(), rec.getReferenceName().length())+2;
	
					this.out.printf(":%"+len+"s %8d %s %-8d\n",
							rec.getReferenceName(),
							rec.getUnclippedStart(),
							L3.toString(),
							rec.getUnclippedEnd()
							);
					this.out.printf(":%"+len+"s %8s %s\n",
							"",
							"",
							L2.toString()
							);
	
					this.out.printf(":%"+len+"s %8d %s %-8d\n",
							rec.getReadName(),
							1,
							L1.toString(),
							rec.getReadLength()
							);
	
					L1.setLength(0);
					L2.setLength(0);
					L3.setLength(0);
					}
	
			}
		
		
		
		private Collection<Throwable> scan(SamReader r) 
			{
			Row row=new Row();
			SAMRecordIterator iter=null;
			try{
				SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(r.getFileHeader().getSequenceDictionary());
				iter=r.iterator();	
				while(iter.hasNext())
					{
					row.rec =iter.next();
					progress.watch(row.rec);
					printAln(row);
					if(this.out.checkError()) break;
					}
				out.flush();
				LOG.info("done");
				return RETURN_OK;
				}
			catch(Exception err)
				{
				return wrapException(err);
				}
			finally
				{
				CloserUtil.close(iter);
				}
			}
		
		@Override
		public Collection<Throwable> initializeKnime() {
			if(refFile==null)
				{
				return wrapException(getMessageBundle("reference.undefined"));
				}
			
			try {
				this.indexedFastaSequenceFile=new IndexedFastaSequenceFile(super.refFile);
				}
			catch (Exception e) {
				return wrapException(e);
				}
			
			return super.initializeKnime();
			}
		
		@Override
		public void cleanup() {
			CloserUtil.close(this.indexedFastaSequenceFile);
			this.indexedFastaSequenceFile=null;
			super.cleanup();
			}
		
		@Override
		protected Collection<Throwable> call(String inputName) throws Exception
				{
				if(printAlignment)
					{
					L1=new StringBuilder();
					L2=new StringBuilder();
					L3=new StringBuilder();
					}
				SamReader samFileReader=null;
				try
					{
					samFileReader  =  openSamReader(inputName);
					
					this.out = openFileOrStdoutAsPrintStream();
					out.println("#READ_NAME\tFLAG\tCHROM\tREAD_POS\tBASE\tQUAL\tREF_POS\tREF\tOP");
					
					
					return scan(samFileReader);
					}
				catch (Exception e)
					{
					return wrapException(e);
					}
				finally
					{
					CloserUtil.close(out);
					out=null;
					CloserUtil.close(samFileReader);
					}
				}
			}
	
	public static void main(String[] args)
		{
		new Sam2Tsv().instanceMainWithExit(args);
		}
}
