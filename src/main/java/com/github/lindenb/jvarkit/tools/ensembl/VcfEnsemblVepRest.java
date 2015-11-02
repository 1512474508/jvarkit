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
* 2015 creation

*/
package com.github.lindenb.jvarkit.tools.ensembl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ensembl.vep.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.lindenb.jvarkit.io.TeeInputStream;
import com.github.lindenb.jvarkit.util.command.Command;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.so.SequenceOntologyTree;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

/**
 * 
 * VcfEnsemblVepRest
 *
 */
public class VcfEnsemblVepRest 
	extends AbstractVcfEnsemblVepRest
	{
	public static final String TAG="VEPTRCSQ";
	@SuppressWarnings("unused")
	private static final ObjectFactory _fool_javac=null;
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(VcfEnsemblVepRest.class);

	@Override
	public Command createCommand() {
		return new MyCommand();
		}

	static private class MyCommand extends AbstractVcfEnsemblVepRest.AbstractVcfEnsemblVepRestCommand
		{    


	private Unmarshaller unmarshaller=null;
	private DocumentBuilder documentBuilder;
	private Transformer xmlSerializer;
	
	
	
	private static String createInputContext(VariantContext ctx)
		{
		StringBuilder sb=new StringBuilder();
		sb.append(ctx.getContig()).
			append(" ").
			append(ctx.getStart()).
			append(" ").
			append(!ctx.hasID()?".":ctx.getID()).
			append(" ").
			append(ctx.getReference().getBaseString()).
			append(" ")
			;
		List<Allele> alts=ctx.getAlternateAlleles();
		if(alts.isEmpty())
			{
			sb.append(".");
			}
		else
			{
			for(int j=0;j< alts.size();++j )
				{
				if(j>0) sb.append(",");
				sb.append(alts.get(j).getBaseString());
				}
			}
		sb.append(" . . .");
		return sb.toString();
		}
	
	private static String empty(Object s)
		{
		return s==null || String.valueOf(s).trim().isEmpty()?"":String.valueOf(s);
		}
	
	private long lastMillisec=-1L;
	private Object generic_vep(List<VariantContext> contexts,boolean xml_answer) throws IOException
		{
		LOG.info("Running VEP "+contexts.size());
		OutputStream wr=null;
		URLConnection urlConnection=null;
		HttpURLConnection  httpConnection=null;
		InputStream response =null;
		javax.xml.transform.Source inputSource=null;
		try {
		    if ( this.lastMillisec!=-1L && this.lastMillisec+ 5000<  System.currentTimeMillis())
		    	{
		    	try {Thread.sleep(1000);} catch(Exception err){}
		    	}
		
			 URL url = new URL(this.server + this.extension);
			 StringBuilder queryb=new StringBuilder();
			 queryb.append("{ \"variants\" : [");
			 for(int i=0;i< contexts.size();++i)
			 	{
				VariantContext ctx=contexts.get(i);
				if(i>0) queryb.append(",");
				queryb.append("\"").
					append(createInputContext(ctx)).
					append("\"");
			 	}
			 queryb.append("]");
			 for(String s: new String[]{"canonical","ccds","domains","hgvs","numbers","protein","xref_refseq"})
			 	{
				 queryb.append(",\"").append(s).append("\":1");
			 	}
			 queryb.append("}");
			 byte postBody[] = queryb.toString().getBytes();
			 urlConnection = url.openConnection();
			 httpConnection = (HttpURLConnection)urlConnection;
			 httpConnection.setRequestMethod("POST");
			 httpConnection.setRequestProperty("Content-Type", "application/json");
			 httpConnection.setRequestProperty("Accept", "text/xml");
			 httpConnection.setRequestProperty("Content-Length", Integer.toString(postBody.length));
			 httpConnection.setUseCaches(false);
			 httpConnection.setDoInput(true);
			 httpConnection.setDoOutput(true);
			  
			 wr = httpConnection.getOutputStream();
			 wr.write(postBody);
			 wr.flush();
			 wr.close();
			 wr=null;
			  
			 //response = new TeeInputStream( httpConnection.getInputStream(),System.err,false);
			 response =httpConnection.getInputStream();
			 if(this.teeResponse)
				 {
				 System.err.println(queryb);
				 response = new TeeInputStream(response,System.err,false);
				 }
			 
			 int responseCode = httpConnection.getResponseCode();
			  
			 if(responseCode != 200)
			 	{
				throw new RuntimeException("Response code was not 200. Detected response was "+responseCode);
			 	}
			
			  
			if(xml_answer)
				{
				return documentBuilder.parse(response);
				}
			else
				{
				inputSource =new StreamSource(response);
				return unmarshaller.unmarshal(inputSource, Opt.class).getValue();
				}
			} 
		catch (Exception e)
			{
			throw new IOException(e);
			}
		finally
			{
			CloserUtil.close(wr);
			CloserUtil.close(response);
			if(httpConnection!=null) httpConnection.disconnect();
			this.lastMillisec = System.currentTimeMillis(); 
			}
		}
	
	private Opt vep(List<VariantContext> contexts) throws IOException
		{
		return (Opt)generic_vep(contexts,false);
		}
	private Document vepxml(List<VariantContext> contexts) throws IOException
		{
		return (Document)generic_vep(contexts,true);
		}

	@Override
	protected Collection<Throwable> doVcfToVcf(String inputName,
			VcfIterator vcfIn, VariantContextWriter out) throws IOException
		{
		sun.misc.BASE64Encoder base64Encoder=new sun.misc.BASE64Encoder();
		final SequenceOntologyTree soTree= SequenceOntologyTree.getInstance();
		VCFHeader header=vcfIn.getHeader();
		List<VariantContext> buffer=new ArrayList<>(this.batchSize+1);
		VCFHeader h2= new VCFHeader(header);
		addMetaData(h2);
		
		if(!xmlBase64)
			{
			h2.addMetaDataLine(new VCFInfoHeaderLine(
					TAG,
					VCFHeaderLineCount.UNBOUNDED,
					VCFHeaderLineType.String,
					"VEP Transcript Consequences. Format :(biotype|cdnaStart|cdnaEnd|cdsStart|cdsEnd|geneId|geneSymbol|geneSymbolSource|hgnc|strand|transcript|variantAllele|so_acns)"
					));
			}
		else
			{
			h2.addMetaDataLine(new VCFInfoHeaderLine(
					TAG,
					1,
					VCFHeaderLineType.String,
					"VEP xml answer encoded as base 64"
					));
			}
		
		out.writeHeader(h2);
		SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(header);
		for(;;)
			{
			VariantContext ctx=null;
			if(vcfIn.hasNext())
				{
				buffer.add((ctx=progress.watch(vcfIn.next())));
				}
			if(ctx==null || buffer.size()>=this.batchSize)
				{
				if(!buffer.isEmpty())
					{
					if(!xmlBase64)
						{
						Opt opt = vep(buffer);
						for(VariantContext ctx2:buffer)
							{
							VariantContextBuilder vcb=new VariantContextBuilder(ctx2);
							String inputStr = createInputContext(ctx2);
							Data mydata=null;
							for(Data data:opt.getData())
								{
								if(!inputStr.equals(data.getInput())) continue;
								mydata=data;
								break;
								}
							if(mydata==null)
								{
								LOG.info("No Annotation found for "+inputStr);
								out.add(ctx2);
								continue;
								}
							List<String> infoList=new ArrayList<>();
							List<TranscriptConsequences> csql=mydata.getTranscriptConsequences();
							for(int i=0;i< csql.size();++i)
								{
								TranscriptConsequences csq= csql.get(i);
								StringBuilder sb=new StringBuilder();
								sb.append(empty(csq.getBiotype())).append("|").
									append(empty(csq.getCdnaStart())).append("|").
									append(empty(csq.getCdnaEnd())).append("|").
									append(empty(csq.getCdsStart())).append("|").
									append(empty(csq.getCdsEnd())).append("|").
									append(empty(csq.getGeneId())).append("|").
									append(empty(csq.getGeneSymbol())).append("|").
									append(empty(csq.getGeneSymbolSource())).append("|").
									append(empty(csq.getHgncId())).append("|").
									append(empty(csq.getStrand())).append("|").
									append(empty(csq.getTranscriptId())).append("|").
									append(empty(csq.getVariantAllele())).append("|")
										;
								List<String> terms=csq.getConsequenceTerms();
								for(int j=0;j< terms.size();++j)
									{
									if(j>0) sb.append("&");
									SequenceOntologyTree.Term term = soTree.getTermByLabel(terms.get(j));
									if(term==null)
										{
										sb.append(terms.get(j));
										LOG.warn("No SO:Term found for "+terms.get(j));
										}
									else
										{
										sb.append(term.getAcn());
										}
									}
								infoList.add(sb.toString());
								}
							if(!infoList.isEmpty())
								{
								vcb.attribute(TAG, infoList);
								}
							
							out.add(vcb.make());
							}
						}//end of not(XML base 64)
					else
						{
						Document opt = vepxml(buffer);
						Element root= opt.getDocumentElement();
						if(!root.getNodeName().equals("opt"))
							throw new IOException("Bad root node "+root.getNodeName());
						
						for(VariantContext ctx2:buffer)
							{
							String inputStr = createInputContext(ctx2);							
							Document newdom=null;
							
							//loop over <data/>
							for(Node dataNode =root.getFirstChild();
									dataNode!=null;
									dataNode=dataNode.getNextSibling())
								{
								if(dataNode.getNodeType()!=Node.ELEMENT_NODE) continue;
								Attr att = Element.class.cast(dataNode).getAttributeNode("input");
								if(att==null)
									{
									LOG.warn("no @input in <data/>");
									continue;
									}

								if(!att.getValue().equals(inputStr)) continue;
								if(newdom==null)
									{
									newdom = this.documentBuilder.newDocument();
									newdom.appendChild(newdom.createElement("opt"));
									}
								newdom.getDocumentElement().appendChild(newdom.importNode(dataNode, true));
								}
							if(newdom==null)
								{
								LOG.warn("No Annotation found for "+inputStr);
								out.add(ctx2);
								continue;
								}
							StringWriter sw=new StringWriter();
							try {
								this.xmlSerializer.transform(
										new DOMSource(newdom),
										new StreamResult(sw)
										);
								} 
							catch (TransformerException err)
								{
								throw new IOException(err);
								}
							//TODO in jdk8 replace with http://docs.oracle.com/javase/8/docs/api/java/util/Base64.html
							VariantContextBuilder vcb=new VariantContextBuilder(ctx2);
							vcb.attribute(TAG,base64Encoder.encode(sw.toString().getBytes()).
									replaceAll("[\\s=]", ""));
							out.add(vcb.make());
							}
						}//end of XML base 64
					}
				if(ctx==null) break;
				buffer.clear();
				}
			if(out.checkError()) break;
			}
		progress.finish();
		return RETURN_OK;
		}
	
	@Override
	public Collection<Throwable> initializeKnime() {
			
		JAXBContext context;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			this.documentBuilder=dbf.newDocumentBuilder();
			
			context = JAXBContext.newInstance("org.ensembl.vep");
			this.unmarshaller=context.createUnmarshaller();
			
			TransformerFactory trf=TransformerFactory.newInstance();
			this.xmlSerializer = trf.newTransformer();
		} catch (Exception e) {
			return wrapException(e);
		}
		return super.initializeKnime();
		}
	
	@Override
		protected Collection<Throwable> call(String inputName) throws Exception {
			return doVcfToVcf(inputName);
			}
	
	@Override
	public void disposeKnime() {
		this.unmarshaller=null;
		super.disposeKnime();
		}
	}
	
	public static void main(String[] args) {
		new VcfEnsemblVepRest().instanceMainWithExit(args);
	}
	}
