package com.github.lindenb.jvarkit.tools.biostar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import com.github.lindenb.jvarkit.util.picard.PicardException;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMProgramRecord;
import htsjdk.samtools.SAMFileReader.ValidationStringency;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.util.CloserUtil;

import com.github.lindenb.jvarkit.util.AbstractCommandLineProgram;
import com.github.lindenb.jvarkit.util.picard.SamWriterFactory;

public class Biostar84452 extends AbstractCommandLineProgram
	{
	private Biostar84452()
		{
		}
	
	@Override
	public String getProgramDescription()
		{
		return "remove clipped bases from BAM. See: http://www.biostars.org/p/84452/";
		}
	
	@Override
	protected String getOnlineDocUrl()
		{
		return "https://github.com/lindenb/jvarkit/wiki/Biostar84452";
		}
	
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -o (filename) output file. default: stdout.");
		out.println(" -c (int) compression level");
		out.println(" -b force binary");
		out.println(" -t (tag) tag to flag samrecord as processed. default:XS");
		super.printOptions(out);
		}
	
	@Override
	public int doWork(String[] args)
		{
		String tag="XS";
		SamWriterFactory swf=SamWriterFactory.newInstance();
		File fileout=null;
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+ "o:bc:t:"))!=-1)
			{
			switch(c)
				{
				case 'o': fileout=new File(opt.getOptArg());break;
				case 'b': swf.setBinary(true);break;
				case 'c': swf.setCompressionLevel(Integer.parseInt(opt.getOptArg()));break;
				case 't':
					{	
					tag=opt.getOptArg();
					if(tag.length()!=2 || !tag.startsWith("X"))
						{
						error("Bad tag: expect length=2 && start with 'X'");
						return -1;
						}
					break;
					}
				default:
					{
					switch(handleOtherOptions(c, opt, args))
						{
						case EXIT_FAILURE: return -1;
						case EXIT_SUCCESS: return 0;
						default:break;
						}
					}
				}
			}
		SAMFileWriter sfw=null;
		SAMFileReader sfr=null;
		try
			{
			
			if(opt.getOptInd()==args.length)
				{
				info("Reading sfomr stdin");
				sfr=new SAMFileReader(System.in);
				}
			else if(opt.getOptInd()+1==args.length)
				{
				File filename=new File(args[opt.getOptInd()]);
				info("Reading from "+filename);
				sfr=new SAMFileReader(filename);
				}
			else
				{
				error("Illegal number of arguments.");
				return -1;
				}
			sfr.setValidationStringency(ValidationStringency.LENIENT);
			SAMFileHeader header=sfr.getFileHeader();
			SAMProgramRecord prg=header.createProgramRecord();
			prg.setProgramName(getProgramName());
			prg.setProgramVersion(getVersion());
			prg.setCommandLine(getProgramCommandLine());
			
			
			if(fileout==null)
				{
				sfw=swf.make(header);
				}
			else
				{
				sfw=swf.make(header,fileout);
				}
			long nChanged=0L;
			SAMRecordIterator iter=sfr.iterator();
			while(iter.hasNext())
				{
				SAMRecord rec=iter.next();
				if(rec.getReadUnmappedFlag())
					{
					sfw.addAlignment(rec);
					continue;
					}
				
				Cigar cigar=rec.getCigar();
				if(cigar==null)
					{
					sfw.addAlignment(rec);
					continue;
					}
				byte bases[]= rec.getReadBases();
				if(bases==null)
					{
					sfw.addAlignment(rec);
					continue;
					}
				
				ArrayList<CigarElement> L=new ArrayList<CigarElement>();
				ByteArrayOutputStream nseq=new ByteArrayOutputStream();
				ByteArrayOutputStream nqual=new ByteArrayOutputStream();
				
				byte quals[]= rec.getBaseQualities();
				int indexBases=0;
				for(CigarElement ce:cigar.getCigarElements())
					{
					switch(ce.getOperator())
						{
						case S: indexBases+=ce.getLength(); break;
						case H://cont
						case P: //cont
						case N: //cont
						case D:
							{
							L.add(ce);
							break;
							}
							
						case I:
						case EQ:
						case X:
						case M:
							{
							L.add(ce);
							nseq.write(bases,indexBases,ce.getLength());
							if(quals.length!=0) nqual.write(quals,indexBases,ce.getLength());
							indexBases+=ce.getLength(); 
							break;
							}
						default:
							{
							throw new PicardException("Unsupported Cigar opertator:"+ce.getOperator());
							}
						}
					
					}
				if(indexBases!=bases.length)
					{
					throw new PicardException("ERRROR "+rec.getCigarString());
					}
				if(L.size()==cigar.numCigarElements())
					{
					sfw.addAlignment(rec);
					continue;
					}
				++nChanged;
				rec.setAttribute(tag, 1);
				rec.setCigar(new Cigar(L));
				rec.setReadBases(nseq.toByteArray());
				if(quals.length!=0)  rec.setBaseQualities(nqual.toByteArray());
				sfw.addAlignment(rec);
				}
			info("Num records changed:"+nChanged);
			}
		catch(Exception err)
			{
			error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(sfw);
			CloserUtil.close(sfr);
			}
		return 0;
		}
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		new Biostar84452().instanceMainWithExit(args);
		}

	}
