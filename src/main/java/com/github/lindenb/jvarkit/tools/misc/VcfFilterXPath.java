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
* 2015 : creation

*/
package com.github.lindenb.jvarkit.tools.misc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.xml.sax.InputSource;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.AbstractVCFFilter3;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


public class VcfFilterXPath
	extends AbstractVCFFilter3
	{
	/** the INFO tag to use in the VCF input */
	private String infoTag=null;
	/** user xpath expression */
	private String xpathExpression=null;
	/** compiled XPath expression */
	private XPathExpression xpathExpr=null;
	/** namespace mapping for xpath object */
	private final Map<String, String> prefix2uri = new HashMap<String, String>();
	/** variable mapping for xpath object */
	private final Map<QName,Object> xpathVariableMap = new HashMap<QName, Object>();
	
	public VcfFilterXPath()
		{
		}
	
	public void setInfoTag(String infoTag) {
		this.infoTag = infoTag;
		}
	public void setXpathExpression(String xpathExpression) {
		this.xpathExpression = xpathExpression;
		}
	
	@Override
	public String getProgramDescription() {
		return "Filter a VCF with a XPATH expression on a INFO tag containing a base64 encodede xml document";
		}
	@Override
	protected String getOnlineDocUrl() {
		return DEFAULT_WIKI_PREFIX+"VcfFilterXPath";
		}
	
	@Override
	protected void doWork(String inpuSource,VcfIterator in, VariantContextWriter out)
		throws IOException
		{
		setVariantCount(0);
		try {
			//TODO in jdk8 replace with http://docs.oracle.com/javase/8/docs/api/java/util/Base64.html
			sun.misc.BASE64Decoder base64Decoder=new sun.misc.BASE64Decoder();
			VCFHeader header=in.getHeader();
			VCFInfoHeaderLine infoHeader = header.getInfoHeaderLine(this.infoTag);
			if(infoHeader==null)
				{
				warning("No INFO header line for "+this.infoTag+" in "+inpuSource);
				}
			else if(!(infoHeader.getCountType()==VCFHeaderLineCount.INTEGER &&
					 infoHeader.getCount()==1 &&
					 infoHeader.getType()==VCFHeaderLineType.String))
				{
				warning("Bad definition of INFO header line for "+this.infoTag+" in "+inpuSource+" expected one 'string' got "+infoHeader);
				infoHeader=null;
				}
			
			VCFHeader h2=new VCFHeader(header);
			h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"CmdLine",String.valueOf(getProgramCommandLine())));
			h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"Version",String.valueOf(getVersion())));
			h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkVersion",HtsjdkVersion.getVersion()));
			h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkHome",HtsjdkVersion.getHome()));
			SAMSequenceDictionaryProgress progess=new SAMSequenceDictionaryProgress(header.getSequenceDictionary());
			
			out.writeHeader(h2);
			while(in.hasNext() )
				{	
				VariantContext ctx = progess.watch(in.next());
				if(infoHeader==null)//no tag in header
					{
					incrVariantCount();
					out.add(ctx);
					continue;
					}
				
				Object o=ctx.getAttribute(this.infoTag);
				if(o==null)
					{
					incrVariantCount();
					out.add(ctx);
					continue;
					}
				StringBuilder base64=new StringBuilder(o.toString());
				while(base64.length()%4!=0) base64.append('=');
				ByteArrayInputStream xmlBytes =  new ByteArrayInputStream(base64Decoder.decodeBuffer(base64.toString()));
				InputSource inputSource=new InputSource(xmlBytes);
				xpathVariableMap.put(new QName("chrom"), ctx.getContig());
				xpathVariableMap.put(new QName("start"), ctx.getStart());
				xpathVariableMap.put(new QName("end"), ctx.getEnd());
				xpathVariableMap.put(new QName("id"), ctx.hasID()?ctx.getID():".");
				boolean accept=(Boolean)xpathExpr.evaluate(inputSource, XPathConstants.BOOLEAN);
				if(accept)
					{
					incrVariantCount();
					out.add(ctx);
					}
				if(checkOutputError()) break;
				}
			
			progess.finish();
			}
		catch(XPathException err)
			{
			throw new IOException(err);
			}
		finally
			{
			xpathVariableMap.clear();
			}
		}
		
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -T (info tag) INFO tag containing a base64-encoded XML document.");
		out.println(" -x (xpath) XPath expression.");
		out.println(" -o (file) filename out . Default: stdout ");
		out.println(" -n prefix=uri (add this namespace mapping to xpath context) ");
		super.printOptions(out);
		}
		
	@Override
	public int initializeKnime() {
		if(this.infoTag==null || this.infoTag.isEmpty())
			{
			error("Info Tag is undefined");
			return -1;
			}
		if(this.xpathExpression==null || this.xpathExpression.isEmpty())
			{
			error("XPath Expression is undefined");
			return -1;
			}
		try {
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath= xpf.newXPath();
			xpath.setXPathVariableResolver(new XPathVariableResolver()
				{
				@Override
				public Object resolveVariable(QName qname)
					{
					return xpathVariableMap.get(qname);
					}
				});
			xpath.setNamespaceContext(new NamespaceContext()
				{
				@Override
				public Iterator<? extends Object> getPrefixes(String namespaceURI)
					{
					List<String> L=new ArrayList<>();
					for(String pfx:prefix2uri.keySet())
						{
						if(prefix2uri.get(pfx).equals(namespaceURI))
							{
							L.add(pfx);
							}
						}

					return L.iterator();
					}
				
				@Override
				public String getPrefix(String namespaceURI)
					{
					for(String pfx:prefix2uri.keySet())
						{
						if(prefix2uri.get(pfx).equals(namespaceURI))
							{
							return pfx;
							}
						}
					return null;
					}
				
				@Override
				public String getNamespaceURI(String prefix)
					{
					return prefix2uri.get(prefix);
					}
				});
			this.xpathExpr=xpath.compile(this.xpathExpression);
		} catch (Exception e) {
			error(e);
			return -1;
			}
		return super.initializeKnime();
		}
	
	@Override
	public int doWork(String[] args)
		{
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+ "T:x:o:n:"))!=-1)
			{
			switch(c)
				{
				case 'o': this.setOutputFile(new File(opt.getOptArg()));break;
				case 'x': this.setXpathExpression(opt.getOptArg()); break;
				case 'T': this.setInfoTag(opt.getOptArg()); break;
				case 'n': 
					{
					String s= opt.getOptArg();
					int eq=s.indexOf('=');
					if(eq<=0)
						{
						error("'=' missing in "+s);
						}
					this.prefix2uri.put(s.substring(0,eq),s.substring(eq+1));
					break;
					}
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
		return mainWork(opt.getOptInd(), args);
		}
		
		
		
	public static void main(String[] args)
		{
		new VcfFilterXPath().instanceMainWithExit(args);
		}
	}
