/*
The MIT License (MIT)

Copyright (c) 2015 Pierre Lindenbaum

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
* 2015 creation

*/
package com.github.lindenb.jvarkit.tools.jmx;

import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloserUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.github.lindenb.jvarkit.util.picard.AbstractBamWriterProgram;

/**
 * @author lindenb
 *
 */
public class SamJmx extends AbstractBamWriterProgram
	{
	private String projectName=null;
	
	public SamJmx() {
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
		}
	
	

	/* (non-Javadoc)
	 * @see com.github.lindenb.jvarkit.util.vcf.AbstractVCFFilter2#doWork(com.github.lindenb.jvarkit.util.vcf.VcfIterator, htsjdk.variant.variantcontext.writer.VariantContextWriter)
	 */
	private int doWork( SamReader in) throws IOException
		{
		String name=this.projectName;
		if(name==null || name.trim().isEmpty())
			{
			name= "undefined";
			}
	    MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		LocatableStreamInfo dynamicMBean=new LocatableStreamInfo( name );
		LocatableStreamInfo.Status status = LocatableStreamInfo.Status.IDLE;
		ObjectName objectMBean = null;
		SAMFileWriter out=null;
		SAMRecordIterator iter=null;
		try
			{
		    out = openSAMFileWriter(in.getFileHeader(), true);
		    
		    objectMBean = new ObjectName(
		    		dynamicMBean.getClass().getPackage()
		            .getName() + ":type=" + dynamicMBean.getClass().getSimpleName()
		            
		    		);
		    mbeanServer.registerMBean(dynamicMBean,objectMBean);
			iter = in.iterator();
			while(iter.hasNext())
				{
				SAMRecord rec = iter.next();
				out.addAlignment(rec);
				status = dynamicMBean.watch(rec);
				if( status == LocatableStreamInfo.Status.ABORT ||
				    status == LocatableStreamInfo.Status.BREAK)
					{
					error("#### Process \""+name+"\" received message "+status.name());
					break;
					}
				}
			if(status == LocatableStreamInfo.Status.ABORT)
				{
				error("#### Process \""+name+"\" : Exit failure");
				mbeanServer.unregisterMBean(objectMBean);
				if(getOutputFile()!=null)
					{
					CloserUtil.close(out);
					out=null;
					getOutputFile().delete();
					}
				System.exit(-1);
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
			CloserUtil.close(iter);
			CloserUtil.close(out);
			if(objectMBean!=null)
				{
				try { mbeanServer.unregisterMBean(objectMBean);}
				catch(Exception err2) {}
				}
			}
		}

	@Override
	public String getProgramDescription() {
		return "Monitor/interrupt/break a BAM/SAM stream with java JMX http://www.oracle.com/technetwork/articles/java/javamanagement-140525.html";
		}
	
	@Override
    protected String getOnlineDocUrl() {
    	return DEFAULT_WIKI_PREFIX+"SamJmx";
    }
	
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -p (stream-identifier). Optional.");
		super.printOptions(out);
		}

	
	@Override
	public int doWork(String[] args)
		{
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+"p:"))!=-1)
			{
			switch(c)
				{
				case 'p': this.setProjectName(opt.getOptArg());break;
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
		SamReader in=null;
		try
			{
			SamReaderFactory srf= SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT);
			if(opt.getOptInd()==args.length)
				{
				in = srf.open(SamInputResource.of(System.in));
				}
			else if(opt.getOptInd()+1==args.length)
				{
				in = srf.open(SamInputResource.of(args[opt.getOptInd()]));
				}
			else
				{
				error("Illegal number of arguments");
				return -1;
				}
			return doWork(in); 
			}
		catch(Exception err)
			{
			error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(in);
			}
		}

	
	public static void main(String[] args) throws IOException
		{
		new SamJmx().instanceMain(args);
		}

}
