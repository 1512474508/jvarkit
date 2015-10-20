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


import htsjdk.samtools.util.CloserUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.github.lindenb.jvarkit.util.command.Command;
import com.github.lindenb.jvarkit.util.illumina.FastQName;

public class IlluminaDirectory
	extends AbstractIlluminaDirectory
	{
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(IlluminaDirectory.class);

	
	
	@Override
	public  Command createCommand() {
			return new MyCommand();
		}
		 
	public  class MyCommand extends AbstractIlluminaDirectory.AbstractIlluminaDirectoryCommand
	 	{		
		private int ID_GENERATOR=0;
    private class Folder
    	{
    	SortedMap<String, Sample> sampleMap=new TreeMap<String, MyCommand.Sample>();
    	List<Pair> undtermined=new ArrayList<Pair>();
    	void scan(File f)
    		{
    		if(f==null) return;
    		if(!f.canRead()) return;
    		LOG.info("Scanning "+f);
    		
			FastQName fq=FastQName.parse(f);
			if(!fq.isValid())
				{
				LOG.warn("invalid name:"+fq);
				return;
				}
			if(fq.isUndetermined())
				{
				for(int i=0;i< undtermined.size();++i)
	    			{
	    			Pair p=undtermined.get(i);
	    			if(p.complement(fq)) return;
	    			}
				undtermined.add(new Pair(fq));
				}
			else
				{
				Sample sample=this.sampleMap.get(fq.getSample());
				if(sample==null)
					{
					sample=new Sample();
					sample.name=fq.getSample();
					this.sampleMap.put(sample.name,sample);
					}
				sample.add(fq);
				}
				
    			
    		}
    	
    	void json(PrintStream out)
    		{
    		boolean first=true;
    		out.print("{\"samples\":[");
    		for(Sample S:this.sampleMap.values())
				{
    			if(!first) out.print(',');
    			first=false;
				S.json(out);
				}
    		out.print("],\"undetermined\":[");
    		first=true;
    		for(Pair p:undtermined)
				{
    			if(!first) out.print(',');
    			first=false;
				p.json(out);
				}
    		out.print("]}");
    		}
    	
    	void write(XMLStreamWriter w) throws XMLStreamException
    		{
    		w.writeStartElement("project");
    		w.writeAttribute("name", "Project1");
    		w.writeAttribute("center", "CENTER");
    		w.writeAttribute("haloplex", "false");
    		w.writeAttribute("wgs", "false");

    		for(Sample S:this.sampleMap.values())
    			{
    			S.write(w);
    			}
    		w.writeStartElement("undetermined");
    		for(Pair p:undtermined)
    			{
    			p.write(w);
    			}
    		w.writeEndElement();
    		w.writeEndElement();
    		}
    	
    	}
    
    /** 
     * A pair of fastq , Forward, reverse
     */
    private class Pair
    	{
    	int id;
    	FastQName forward;
    	FastQName reverse;
    	
    	Pair(FastQName fq)
    		{
    		id=++ID_GENERATOR;
    		switch(fq.getSide())
    			{
    			case Forward:forward=fq; break;
    			case Reverse:reverse=fq; break;
    			default:throw new RuntimeException("bad side "+fq);
    			}
    		}
    	
    	boolean complement(FastQName other)
    		{
    		if(forward!=null && reverse!=null) return false;
    		if(forward!=null && forward.isComplementOf(other))
    			{
    			reverse=other;
    			return true;
    			}
    		else if(reverse!=null && reverse.isComplementOf(other))
    			{
    			forward=other;
    			return true;
    			}
    		return false;
    		}
    	
    	void json(PrintStream out)
    		{
    		if(forward!=null && reverse!=null)
				{
	    		
	    		out.print("{");
	    		out.print("\"id\":\"p"+this.id+"\",");
	    		out.print("\"md5pair\":\""+md5(forward.getFile().getPath()+reverse.getFile().getPath())+"\",");
	    		out.print("\"lane\":"+forward.getLane()+",");
    			if(forward.getSeqIndex()!=null)
    				{
    				out.print("\"index\":\""+forward.getSeqIndex()+"\",");
    				}
    			else
    				{
    				out.print("\"index\":null,");
    				}
	    		out.print("\"split\":"+forward.getSplit()+",");
	    		out.print("\"forward\":{");
	    		out.print("\"md5filename\":\""+md5(forward.getFile().getPath())+"\",");
	    		out.print("\"path\":\""+forward.getFile().getPath()+"\",");
	    		out.print("\"side\":"+forward.getSide().ordinal()+",");
	    		out.print("\"file-size\":"+forward.getFile().length());
	    		out.print("},\"reverse\":{");
	    		out.print("\"md5filename\":\""+md5(reverse.getFile().getPath())+"\",");
	    		out.print("\"path\":\""+reverse.getFile().getPath()+"\",");
	    		out.print("\"side\":"+reverse.getSide().ordinal()+",");
	    		out.print("\"file-size\":"+reverse.getFile().length());
	    		out.print("}}");
				}
    		else
    			{
    			FastQName F=(forward==null?reverse:forward);
    			out.print("{");
    			out.print("\"id\":\"p"+this.id+"\",");
    			out.print("\"md5filename\":\""+md5(F.getFile().getPath())+"\",");
	    		out.print("\"lane\":"+F.getLane()+",");
    			if(forward.getSeqIndex()!=null)
    				{
    				out.print("\"index\":\""+F.getSeqIndex()+"\",");
    				}
    			else
    				{
    				out.print("\"index\":null,");
    				}
	    		out.print("\"split\":"+F.getSplit()+",");
	    		out.print("\"path\":\""+F.getFile().getPath()+"\",");
	    		out.print("\"side\":"+F.getSide()+",");
    			out.print("}");
    			}
			}
    	
    	void write(XMLStreamWriter w,String tagName,FastQName fastqFile) throws XMLStreamException
    		{
			w.writeStartElement(tagName);
			w.writeAttribute("md5filename",md5(fastqFile.getFile().getPath()));
			w.writeAttribute("file-size",String.valueOf( fastqFile.getFile().length()));
			w.writeCharacters(fastqFile.getFile().getPath());
			w.writeEndElement();
    		}
    	
    	void write(XMLStreamWriter w) throws XMLStreamException
    		{
			w.writeStartElement("fastq");
			w.writeAttribute("id","p"+this.id);
			w.writeAttribute("md5",md5(forward.getFile().getPath()+reverse.getFile().getPath()));
			w.writeAttribute("lane", String.valueOf(forward.getLane()));
			if(forward.getSeqIndex()!=null) w.writeAttribute("index", String.valueOf(forward.getSeqIndex()));
			w.writeAttribute("split", String.valueOf(forward.getSplit()));
			
			if(forward!=null && reverse!=null)
    			{
    			write(w,"for",forward);
    			write(w,"rev",reverse);
    			}
			else
				{
				write(w,"single",forward==null?reverse:forward);
				}
			
		
			w.writeEndElement();
    		}
    	}
    
    private class Sample
		{
		String name;
    	List<Pair> pairs=new ArrayList<Pair>();
    	
    	private void add(FastQName fq)
    		{
    		for(int i=0;i< pairs.size();++i)
    			{
    			Pair p=pairs.get(i);
    			if(p.complement(fq)) return;
    			}
    		pairs.add(new Pair(fq));
    		}
    	
    	void write(XMLStreamWriter w) throws XMLStreamException
			{
			w.writeStartElement("sample");
			w.writeAttribute("name",this.name);
			w.writeAttribute("father","undefined");
			w.writeAttribute("mother","undefined");
			w.writeAttribute("sex","undefined");
			
			for(Pair p:this.pairs)
				{
				p.write(w);
				}
				
			w.writeEndElement();
			}
    	
    	void json(PrintStream out)
    		{
    		out.print("{\"sample\":\""+ this.name +"\",\"files\":[");
    		for(int i=0;i< pairs.size();++i)
    			{
    			if(i>0) out.print(",");
    			pairs.get(i).json(out);
    			}
    		out.print("]}");
    		}
    	
    	
		}
    
    
    
    	@Override
    	public Collection<Throwable> call() throws Exception {
    		final List<String> args = getInputFiles();
			if(!args.isEmpty())
				{
				return wrapException("Expected to read filenames on stdin.");
				}
			PrintStream pw=null;
			try
				{
				List<Folder> folders=new ArrayList<Folder>();
				Folder folder=new Folder();
				folders.add(folder);
				String line;
				BufferedReader in=new BufferedReader(new InputStreamReader(stdin()));
				while((line=in.readLine())!=null)
					{
					if(line.isEmpty() || line.startsWith("#")) continue;
					if(!line.endsWith(".fastq.gz"))
						{
						LOG.warn("ignoring "+line);
						continue;
						}
					File f=new File(line);
					if(!f.exists())
						{
						return wrapException("Doesn't exist:"+f);
						}
					if(!f.isFile())
						{
						return wrapException("Not a file:"+f);
						}
					folder.scan(f);
					}
				in.close();
		    	
				pw =  openFileOrStdoutAsPrintStream();

		    	if(this.isJSON())
		    		{
		    		pw.print("[");
		    		for(int i=0;i< folders.size();++i)
		    			{
		    			if(i>0) pw.print(",");
		    			folders.get(i).json(pw);
		    			}
		    		
		    		pw.println("]");
		    		pw.flush();
		    		pw.close();
		    		return RETURN_OK;
		    		}
		    	else
		    		{
	    			XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
	    			XMLStreamWriter w= xmlfactory.createXMLStreamWriter(pw);
	    			w.writeStartDocument("UTF-8","1.0");
	    			w.writeStartElement("model");
	    			w.writeComment(this.getProgramCommandLine());
	    			for(Folder f:folders) f.write(w);
	    			w.writeEndElement();
	    			w.writeEndDocument();
	    			w.flush();
	    			w.close();
		    		}
				pw.close();
				return RETURN_OK;
				}
			catch(Exception err)
				{
				return wrapException(err);
				}
			finally
				{
				CloserUtil.close(pw);
				}
			}
	 	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		new IlluminaDirectory().instanceMainWithExit(args);
		}

}
