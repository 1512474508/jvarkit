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

package com.github.lindenb.jvarkit.tools.fastq;




import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import com.github.lindenb.jvarkit.util.picard.FastqReader;
import com.github.lindenb.jvarkit.util.picard.FourLinesFastqReader;

import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.fastq.BasicFastqWriter;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.util.CloserUtil;


/**
 * Pierre Lindenbaum PhD @yokofakun
 * @author lindenb
 * filters FASTQ with javascript
 */
public class FastqJavascript
	extends AbstractFastqJavascript
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(FastqJavascript.class);

	private CompiledScript  script=null;
	//private File failingReadsFile=null;
	private FastqWriter failingReadsWriter=null;
	//
	
	/** mutable fastq record */
	public static class Record
		{
		private long nLine;
		private String name;
		private String sequence;
		private String name2;
		private String qualities;
		
		Record(FastqRecord rec)
			{
			name=rec.getReadHeader();
			sequence=rec.getReadString();
			name2=rec.getBaseQualityHeader();
			qualities=rec.getBaseQualityString();
			}
		public String getReadHeader() { return name;}
		public String getReadString() { return sequence;}
		public String getBaseQualityHeader() { return name2;}
		public String getBaseQualityString() { return qualities;}
		public long getLine() {
			return nLine;
			}
		public void setName(String name) {
			this.name = name;
			}
		public void setReadString(String sequence) {
			this.sequence = sequence;
		}
		public void setBaseQualityHeader(String name2) {
			this.name2 = name2;
		}
		public void setBaseQualityString(String qualities) {
			this.qualities = qualities;
			}
		private FastqRecord toFastqRecord()
			{
			return new FastqRecord(name,sequence,name2,qualities);
			}
		@Override
		public int hashCode() {
			return toFastqRecord().hashCode();
			}
		@Override
		public String toString() {
			return toFastqRecord().toString();
			}
		}
	
	/** a pair of FASTQ */
	public static class Pair
		{
		private long nLine;
		private Record rec1;
		private Record rec2;
		Pair(Record rec1,Record rec2)
			{
			this.rec1=rec1;
			this.rec2=rec2;
			}
		public long getLine() {
			return nLine;
			}
		public Record getFirst() { return get(0);}
		public Record getSecond() { return get(1);}
		public Record get(int index)
			{
			switch(index)
				{
				case 0: return rec1;
				case 1: return rec2;
				default: throw new IllegalArgumentException("index:="+index);
				}
			}
		
		@Override
		public int hashCode() {
			return get(0).hashCode()*31+get(1).hashCode();
			}
		
		@Override
		public String toString() {
			return get(0).toString()+"\n"+get(1).toString();
			}
		}
	
	private static class InterleavedFastqReader implements FastqReader
		{
		private final FastqReader delegates[];
		private int side=0;
		public InterleavedFastqReader(final FastqReader R1,final FastqReader R2) {
			delegates = new FastqReader[]{R1,R2};
			}
		@Override
		public boolean hasNext() {
			return delegates[side].hasNext();
			}
		@Override
		public ValidationStringency getValidationStringency() {
			return delegates[0].getValidationStringency();
			}
		@Override
		public FastqRecord next() {
			final FastqRecord rec =  delegates[side].next();
			this.side=(side==0?1:0);
			return rec;
			}
		@Override
		public void close() throws IOException {
			delegates[0].close();
			delegates[1].close();
			}
		@Override
		public void setValidationStringency(ValidationStringency validationStringency) {
			delegates[0].setValidationStringency(validationStringency);
			}
		}
	
	private FastqJavascript()
		{
		
		}
	
	/* open failing bam if it was not already open */
	private boolean openFailing()
		{
		if(this.failingReadsFile==null) return false;
		if(this.failingReadsWriter==null)
			{
			LOG.info("Writing failings to "+ this.failingReadsFile);
			failingReadsWriter=new BasicFastqWriter(this.failingReadsFile);
			}
		return true;
		}
	
	private void failing(Record rec)
		{
		if(openFailing()) failingReadsWriter.write(rec.toFastqRecord());
		}
	
	private void doWork(final FastqReader r) 
		throws IOException,ScriptException
			{
			final FastqWriter fastqWriters[]={null,null};
			try
				{
				if(!interleaved)
					{
					if( super.R1FileOut!=null || super.R2FileOut!=null) {
						throw new IllegalStateException("This is not an interleaved input but option "+OPTION_R1FILEOUT+"/"+OPTION_R2FILEOUT+" were defined.");
						}
					if(super.getOutputFile()==null)
						{
						fastqWriters[0] = new BasicFastqWriter(new PrintStream(stdout()));
						}
					else
						{
						fastqWriters[0] = new BasicFastqWriter(super.getOutputFile());
						}
					fastqWriters[1] = fastqWriters[0];
					}
				else /* interleaved */
					{
					if( 	(super.R1FileOut==null && super.R2FileOut!=null) ||
							(super.R1FileOut!=null && super.R2FileOut==null)) {
							throw new IllegalStateException("Option "+OPTION_R1FILEOUT+" / " + OPTION_R2FILEOUT+" must be both defined");
						}
					else if( super.R1FileOut==null && super.R2FileOut==null) {
						if(super.getOutputFile()==null)
							{
							fastqWriters[0] = new BasicFastqWriter(new PrintStream(stdout()));
							}
						else
							{
							fastqWriters[0] = new BasicFastqWriter(super.getOutputFile());
							}
						fastqWriters[1] = fastqWriters[0];
						}
					else
						{
						fastqWriters[0] = new BasicFastqWriter(super.R1FileOut);
						fastqWriters[1] = new BasicFastqWriter(super.R2FileOut);
						}
					}
			
				long count=0L;
				final Bindings bindings = this.script.getEngine().createBindings();
		        
				while(r.hasNext())
					{
					final Record record=new Record(r.next());
					record.nLine=count;
					
					if(super.interleaved)
						{
						if(!r.hasNext()) throw new IOException("interleaved: mate missing");
						final Record mate= new Record(r.next());
						mate.nLine=count;
						final Pair pair=new Pair(record, mate);
						pair.nLine=count;
						
						
						
						bindings.put("pair", pair);
						if(!super.evalJavaScriptBoolean(this.script, bindings))
							{
							failing(pair.get(0));
							failing(pair.get(1));
							}
						else
							{
							fastqWriters[0].write(pair.get(0).toFastqRecord());
							fastqWriters[1].write(pair.get(1).toFastqRecord());
							}
						}
					else
						{
						bindings.put("rec", record);
						if(!super.evalJavaScriptBoolean(this.script, bindings))
							{
							failing(record);
							}
						else
							{
							fastqWriters[0].write(record.toFastqRecord());
							}
						}
					++count;
					if(this.LIMIT>0L && count>=this.LIMIT) break;
					}
				openFailing();
				}
			finally 
			
				{
				CloserUtil.close(fastqWriters[0]);
				CloserUtil.close(fastqWriters[1]);
				}
			}
	

	
	@Override
	public void printOptions(PrintStream out) {

		super.printOptions(out);
		}

	@Override
	public Collection<Throwable> call() throws Exception {
		final List<String> args = super.getInputFiles();
		
		if( (super.R1FileOut==null && super.R2FileOut!=null) ||
			(super.R1FileOut!=null && super.R2FileOut==null)) {
			return wrapException("Option "+OPTION_R1FILEOUT+" / " + OPTION_R2FILEOUT+" must be both defined");
		}
		
		try
			{
			this.script  = super.compileJavascript();
			
			
			
			if(args.isEmpty())
				{				
				final FastqReader in=new FourLinesFastqReader(stdin());
				doWork(in);
				in.close();
				}
			else if(args.size()==2)
				{
				LOG.info("2 fastqs: Reading as interleavel fastqs");
				final FastqReader in1=new FourLinesFastqReader(new File(args.get(0)));
				final FastqReader in2=new FourLinesFastqReader(new File(args.get(1)));
				final FastqReader in=new InterleavedFastqReader(in1,in2);
				super.interleaved = true;
				doWork(in);
				in.close();
				}
			else if(args.size()==1)
				{
				final FastqReader in=new FourLinesFastqReader(new File(args.get(0)));
				doWork(in);
				in.close();
				}
			else
				{
				return wrapException("Illegal number of arguments");
				}
			return RETURN_OK;
			}
		catch(final Exception err)
			{
			return wrapException(err);
			}
		finally
			{
			CloserUtil.close(failingReadsWriter);
			}
		}
	
	

		
	public static void main(String[] args) throws Exception
		{
		new FastqJavascript().instanceMainWithExit(args);
		}
	
	}
