package com.github.lindenb.jvarkit.tools.samfixcigar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.picard.PicardException;
import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.samtools.Cigar;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.TextCigarCodec;
import net.sf.samtools.util.CloserUtil;

import com.github.lindenb.jvarkit.util.AbstractCommandLineProgram;
import com.github.lindenb.jvarkit.util.picard.GenomicSequence;

public class SamFixCigar extends AbstractCommandLineProgram
	{
	private IndexedFastaSequenceFile indexedFastaSequenceFile=null;
	
	@Override
	public String getProgramDescription() {
		return "Fix Cigar String in SAM replacing 'M' by 'X' or '='";
		}
	
	@Override
	protected String getOnlineDocUrl() {
		return "https://github.com/lindenb/jvarkit/wiki/SamFixCigar";
		}
	
	@Override
	public void printOptions(java.io.PrintStream out)
		{
		out.println(" -r (file) reference indexed with samtools faidx . Required.");
		out.println(" -o (file) BAM/SAM fileout. default:stdout.");

		
		out.println(" -h get help (this screen)");
		out.println(" -v print version and exit.");
		out.println(" -L (level) log level. One of java.util.logging.Level . currently:"+getLogger().getLevel());
		
		}
	
	@Override
	public int doWork(String[] args)
		{
		GenomicSequence genomicSequence=null;
		int maxRecordsInRam=10000;
		File faidx=null;
		File fout=null;
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args, "hvL:r:o:"))!=-1)
			{
			switch(c)
				{
				case 'r': faidx=new File(opt.getOptArg());break;
				case 'o': fout=new File(opt.getOptArg());break;
					
				case 'h': printUsage();return 0;
				case 'v': System.out.println(getVersion());return 0;
				case 'L': getLogger().setLevel(java.util.logging.Level.parse(opt.getOptArg()));break;
				case ':': System.err.println("Missing argument for option -"+opt.getOptOpt());return -1;
				default: System.err.println("Unknown option -"+opt.getOptOpt());return -1;
				}
			}
		
		if(faidx==null)
			{
			error("Reference was not specified.");
			return -1;
			}
		long nReads=0L;
		long nX=0L;
		SAMFileReader sfr=null;
		SAMFileWriter sfw=null;
		SAMFileHeader header;
		try
			{
			info("Loading reference");
			this.indexedFastaSequenceFile=new IndexedFastaSequenceFile(faidx);
			if(opt.getOptInd()==args.length)
				{
				info("Reading from stdin");
				}
			else
				{
				for(int i=opt.getOptInd();i< args.length;++i)
					{
					String filename=args[i];
					info("Reading from "+filename);
					}
				}
			if(opt.getOptInd()==args.length)
				{
				info("Reading stdin");
				sfr=new SAMFileReader(System.in);
				}
			else if(opt.getOptInd()+1==args.length)
				{
				File fin=new File(args[opt.getOptInd()]);
				info("Reading "+fin);
				sfr=new SAMFileReader(fin);
				}
			else
				{
				error("Illegal number of arguments");
				return -1;
				}
			sfr.setValidationStringency(ValidationStringency.LENIENT);
			header=sfr.getFileHeader();
			
			SAMFileWriterFactory sfwf=new SAMFileWriterFactory();
			sfwf.setCreateIndex(false);
			sfwf.setCreateMd5File(false);
			sfwf.setMaxRecordsInRam(maxRecordsInRam);
			
			if(fout==null)
				{
				sfw=sfwf.makeSAMWriter(header, header.getSortOrder()==SortOrder.coordinate,System.out);
				}
			else
				{
				sfw=sfwf.makeSAMOrBAMWriter(header, header.getSortOrder()==SortOrder.coordinate,fout);
				}
			List<CigarElement> newCigar=new ArrayList<CigarElement>();
			SAMRecordIterator iter=sfr.iterator();
			while(iter.hasNext())
				{
				++nReads;
				if(nReads%1E6==0)
					{
					info("Reads "+nReads+" operator:X="+nX);
					}
				SAMRecord rec=iter.next();
				Cigar cigar=rec.getCigar();
				byte bases[]=rec.getReadBases();
				if( rec.getReadUnmappedFlag() ||
					cigar==null ||
					cigar.getCigarElements().isEmpty() ||
					bases==null)
					{
					sfw.addAlignment(rec);
					continue;
					}
				
				if(genomicSequence==null ||
					genomicSequence.getSAMSequenceRecord().getSequenceIndex()!=rec.getReferenceIndex())
					{
					genomicSequence=new GenomicSequence(indexedFastaSequenceFile, rec.getReferenceName());
					}
				
				newCigar.clear();
				int refPos1=rec.getAlignmentStart();
				int readPos0=0;
				
				
				for(CigarElement ce:cigar.getCigarElements())
					{
					switch(ce.getOperator())
						{
    					case H:break;

    					
    					case P://cont
						case N://cont
						case D:
							{
							newCigar.add(ce);
							refPos1+=ce.getLength();
							break;
							}
						case S:
						case I:
							{
							newCigar.add(ce);
							readPos0+=ce.getLength();							
							break;
							}
						case EQ://cont
						case X:
							{
							newCigar.add(ce);
							refPos1+=ce.getLength();
							readPos0+=ce.getLength();	
							break;
							}
						case M:
							{
							boolean X=false;
							for(int i=0;i< ce.getLength();++i)
	    		    			{
								char c1=(char)bases[readPos0];
								char c2=(refPos1-1< genomicSequence.length()?genomicSequence.charAt(refPos1-1):'\0');
								
								if(Character.toUpperCase(c1)==Character.toUpperCase(c2))
									{
									newCigar.add(new CigarElement(1, CigarOperator.EQ));
									}
								else
									{
									newCigar.add(new CigarElement(1, CigarOperator.X));
									X=true;
									}
								
	    						refPos1++;
	    						readPos0++;
    		    				}
							if(X) nX++;
							break;
							}
						default: throw new PicardException("Cannot parse cigar "+rec.getCigarString()+" in "+rec.getReadName());
						}
					}
				int i=0;
				while(i< newCigar.size())
					{
					CigarOperator op1 = newCigar.get(i).getOperator();
					int length1 = newCigar.get(i).getLength();
					
					if( i+1 <  newCigar.size() &&
						newCigar.get(i+1).getOperator()==op1)
						{
						CigarOperator op2= newCigar.get(i+1).getOperator();
						int length2=newCigar.get(i+1).getLength();

						 newCigar.set(i,new CigarElement(length1+length2, op2));
						 newCigar.remove(i+1);
						}
					else
						{
						++i;
						}
					}
				cigar=new Cigar(newCigar);
				String newCigarStr=TextCigarCodec.getSingleton().encode(cigar);
				//info("changed "+rec.getCigarString()+" to "+newCigarStr+" "+rec.getReadName()+" "+rec.getReadString());
				rec.setCigar(cigar);
				rec.setCigarString(newCigarStr);
				
				sfw.addAlignment(rec);
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
			CloserUtil.close(this.indexedFastaSequenceFile);
			CloserUtil.close(sfr);
			CloserUtil.close(sfw);
			}
		}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SamFixCigar().instanceMainWithExit(args);

	}

}
