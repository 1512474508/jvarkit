package com.github.lindenb.jvarkit.tools.impactdup;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;
import net.sf.picard.cmdline.Usage;
import net.sf.picard.io.IoUtil;
import net.sf.picard.util.Log;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.util.BinaryCodec;
import net.sf.samtools.util.CloseableIterator;
import net.sf.samtools.util.SortingCollection;

public class ImpactOfDuplicates extends CommandLineProgram
    {
    private static final Log log = Log.getInstance(ImpactOfDuplicates.class);
    @Usage
    public String USAGE = getStandardUsagePreamble() + "Impact of Duplicates per BAM.";

    
    @Option(shortName=StandardOptionDefinitions.INPUT_SHORT_NAME, doc="SAM or BAM input file", minElements=1)
    public List<File> INPUT = new ArrayList<File>();
    @Option(shortName=StandardOptionDefinitions.OUTPUT_SHORT_NAME, doc="Save result as... default is stdout", optional=true)
    public File OUTPUT = null;

    /** sam file dict, to retrieve the sequences names  */
    private List< SAMSequenceDictionary> samFileDicts=new ArrayList<SAMSequenceDictionary>();
    /** buffer for Duplicates */
    private List<Duplicate> duplicatesBuffer=new ArrayList<Duplicate>();
    /** output */
    private PrintStream out=System.out;
    
    
    /* current index in BAM list */
    private int bamIndex;
    /* all duplicates, sorted */
    private SortingCollection<Duplicate> duplicates;
    
    private class Duplicate implements Comparable<Duplicate>
        {
        int tid;
        int pos;
        int size;
        int bamIndex;
        
        public String getReferenceName()
        	{
        	return samFileDicts.get(this.bamIndex).getSequence(this.tid).getSequenceName();
        	}
        public int compareChromPosSize(final Duplicate o)
        	{
        	int i=getReferenceName().compareTo(o.getReferenceName());
        	if(i!=0) return i;
        	i=pos-o.pos;
        	if(i!=0) return i;
        	i=size-o.size;
        	return i;
        	}
        
        
        
        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + bamIndex;
			result = prime * result + pos;
			result = prime * result + size;
			result = prime * result + tid;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Duplicate other = (Duplicate) obj;
			return compareTo(other)==0;
			}
		@Override
        public int compareTo(final Duplicate o)
        	{
        	int i=compareChromPosSize(o);
        	if(i!=0) return i;
        	return bamIndex-o.bamIndex;
        	}
		
		@Override
		public String toString() {
			return "(bamIndex:"+bamIndex+" pos:"+pos+" size:"+size+" tid:"+tid+")";
			}
		
        }
    
    

    private  class DuplicateCodec
    	extends BinaryCodec
        implements SortingCollection.Codec<Duplicate >
        {
    	
        @Override
        public void encode(final Duplicate d)
            {
        	
	        	this.writeInt(d.tid);
	        	this.writeInt(d.pos);
	        	this.writeInt(d.size);
	        	this.writeInt(d.bamIndex);
	        	
            }
        
    
        
        @Override
        public Duplicate decode()
            {
        	Duplicate d=new Duplicate();
        	try
	        	{
	            d.tid=this.readInt();
	        	}
        	catch(net.sf.samtools.util.RuntimeEOFException err)
        		{
        		return null;
        		}
            d.pos=this.readInt();
            d.size=this.readInt();
            d.bamIndex=this.readInt();
	        return d;
            }
        
        @Override
        public DuplicateCodec clone()
            {
            return new DuplicateCodec();
            }
        }
    
   
    
    private void dumpDuplicatesBuffer()
		{
    	if(this.duplicatesBuffer.isEmpty()) return;
    	int counts[]=new int[INPUT.size()];
    	Arrays.fill(counts, 0);
    	int maxDup=0;
    	for(int i=0;i< this.duplicatesBuffer.size();++i)
    		{
    		Duplicate di=this.duplicatesBuffer.get(i);
    		counts[di.bamIndex]++;
    		maxDup=Math.max(maxDup,counts[di.bamIndex]);
    		}
    	
    	
    	
    	if(maxDup<10)
    		{
    		this.duplicatesBuffer.clear();
    		return;
    		}
    	Duplicate front=this.duplicatesBuffer.get(0);
    	out.print(
    			front.getReferenceName()+":"+
    			front.pos+"-"+
    			(front.pos+front.size)
    			);

    	for(int i=0;i< counts.length;++i)
    		{
    		out.print('\t');
    		out.print(counts[i]);
    		}

    	out.println();
    	this.duplicatesBuffer.clear();
		}
    

    
    @Override
    protected int doWork()
        {
       this.duplicates=SortingCollection.newInstance(
                Duplicate.class,
                new DuplicateCodec(),
                new Comparator<Duplicate>()
	            	{
	        		@Override
	        		public int compare(Duplicate o1, Duplicate o2)
	        			{
	        			return o1.compareTo(o2);
	        			}
	            	},
                super.MAX_RECORDS_IN_RAM,
                super.TMP_DIR
                );
       CloseableIterator<Duplicate> dupIter=null;

        try
            {
            for(this.bamIndex=0;
        		this.bamIndex< this.INPUT.size();
        		this.bamIndex++)
                {
            	int prev_tid=-1;
            	int prev_pos=-1;
            	long nLines=0L;
            	File inFile=this.INPUT.get(this.bamIndex);
            	log.info("Processing "+inFile);
                IoUtil.assertFileIsReadable(inFile);
                SAMFileReader samReader=null;
                SAMRecordIterator iter=null;
                try
	                {
	                samReader=new SAMFileReader(inFile);
	                final SAMFileHeader header=samReader.getFileHeader();
	                this.samFileDicts.add(header.getSequenceDictionary());
	                samReader.setValidationStringency(super.VALIDATION_STRINGENCY);
	                iter=samReader.iterator();
	                while(iter.hasNext())
	                    {
	                    SAMRecord rec=iter.next();
	                    if(rec.getReadUnmappedFlag()) continue;
	                    if(!rec.getReadPairedFlag()) continue;
	                    if(rec.getReferenceIndex()!=rec.getMateReferenceIndex()) continue;
	                    if(!rec.getProperPairFlag()) continue;
	                    if(!rec.getFirstOfPairFlag()) continue;
	                    
	                    if(prev_tid!=-1 )
	                    	{
	                    	if(prev_tid> rec.getReferenceIndex())
	                    		{
	                    		throw new IOException("Bad sort order from "+rec);
	                    		}
	                    	else if(prev_tid==rec.getReferenceIndex() && prev_pos>rec.getAlignmentStart())
	                    		{
	                    		throw new IOException("Bad sort order from "+rec);
	                    		}
	                    	else
	                    		{
	                    		prev_pos=rec.getAlignmentStart();
	                    		}
	                    	}
	                    else
	                    	{
	                    	prev_tid=rec.getReferenceIndex();
	                    	prev_pos=-1;
	                    	}
	                    
	                    
	                    if((++nLines)%1000000==0)
	                    	{
	                    	log.info("In "+inFile+" N="+nLines);
	                    	}
	                    Duplicate dup=new Duplicate();
	                	dup.bamIndex=this.bamIndex;
	                	dup.pos=Math.min(rec.getAlignmentStart(),rec.getMateAlignmentStart());
	                	dup.tid=rec.getReferenceIndex();
	                	dup.size=Math.abs(rec.getInferredInsertSize());
	                	this.duplicates.add(dup);
	                    }
	                }
                finally
	                {
	                if(iter!=null) iter.close();
	                if(samReader!=null) samReader.close();
	                }
                log.info("done "+inFile);
                }
            /** loop done, now scan the duplicates */
            
            log.info("doneAdding");
            this.duplicates.doneAdding();
            
            if(this.OUTPUT!=null)
            	{
            	this.out=new PrintStream(OUTPUT);
            	}
            
        	out.print("#INTERVAL");
        	for(int i=0;i< INPUT.size();++i)
        		{
        		out.print('\t');
        		out.print(INPUT.get(i));
        		}
        	out.println();

           dupIter=this.duplicates.iterator();
           while(dupIter.hasNext())
            	{
        	    Duplicate dup=dupIter.next();
	        	if( this.duplicatesBuffer.isEmpty() ||
	        		dup.compareChromPosSize(this.duplicatesBuffer.get(0))==0)
	            	{
	        		this.duplicatesBuffer.add(dup);
	            	}
	            else
	            	{
	            	dumpDuplicatesBuffer();
	            	this.duplicatesBuffer.add(dup);
	            	}
            	}
            dumpDuplicatesBuffer();
            log.info("end iterator");
            out.flush();
            out.close();
            }
        catch (Exception e) {
            log.error(e);
            return -1;
            }
        finally
        	{
        	if(dupIter!=null) dupIter.close();
        	log.info("cleaning duplicates");
        	this.duplicates.cleanup();
        	}
        return 0;
        }
    public static void main(final String[] argv)
		{
	    new ImpactOfDuplicates().instanceMainWithExit(argv);
		}	

    }
