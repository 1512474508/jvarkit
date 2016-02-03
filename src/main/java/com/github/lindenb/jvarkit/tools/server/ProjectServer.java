/*
The MIT License (MIT)

Copyright (c) 2016 Pierre Lindenbaum

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

*/
package com.github.lindenb.jvarkit.tools.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;

import com.github.lindenb.jvarkit.io.IOUtils;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.IOUtil;

public  class ProjectServer extends AbstractProjectServer {

	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(ProjectServer.class);
	
	private static class UserPrefs {
		
		}
	
	
	private static class ProjectFile {
		private final File configFile;
		private final Set<Path> ngsFiles = new HashSet<>();
		private String id="";
		private String label=null;
		private String description=null;
		ProjectFile(final File configFile) {
			this.configFile=configFile;
			this.id=configFile.getName();
			IOUtil.assertFileIsReadable(configFile);
			BufferedReader r=null;
			try {
				r=IOUtils.openFileForBufferedReading(this.configFile);
				String line;
				while((line=r.readLine())!=null)
					{	
					if(line.trim().isEmpty()) continue;
					if(line.startsWith("#"))
						{
						line=line.substring(1);
						int colon = line.indexOf(":");
						if(colon>0 )
							{
							final String key = line.substring(0,colon).trim().toLowerCase();
							final String value = line.substring(colon+1).trim();
							if(key.equals("id")) this.id=value;
							else if(key.equals("name") || key.equals("label")) this.label=value;
							else if(key.equals("desc") || key.equals("description")) this.description=value;
							}
						continue;
						}
					final Path file= Paths.get(line.trim());
					ngsFiles.add(file);
					}
			} catch (IOException err) {
				throw new RuntimeIOException(err);
			}
		}
		
		public String getId() {
			return this.id;
		}
		public String getLabel() {
			return this.label==null?getId():this.label;
		}
		
		public String getDefinition() {
			return this.description==null?getLabel():this.description;
		}
		
		void anchor(XMLStreamWriter w) throws XMLStreamException{
			w.writeStartElement("a");
			w.writeAttribute("href", "/project/"+getId());
			w.writeAttribute("title", getDefinition());
			w.writeCharacters(getLabel());
			w.writeEndElement();
		}
		
	List<Path> getBams() {
		return this.ngsFiles.stream().filter(p->p.endsWith(".bam")).collect(Collectors.toList());
		}
	List<Path> getVcfs() {
		return this.ngsFiles.stream().filter(p->p.endsWith(".vcf.gz")).collect(Collectors.toList());
		}
	}
	
	
	private static class ProjectHandler extends DefaultHandler{
		final List<ProjectFile> projects;
		ProjectHandler(final File configFile) {
		BufferedReader in= null;
		try {
			in= new BufferedReader(new FileReader(configFile));
		} catch (IOException e) {
			throw new RuntimeIOException(e);
			}
		finally 
			{
			CloserUtil.close(in);
			}
		}
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException
			  {
				request.getPathInfo();
			  if(target==null) target="/";
			  
			  doListProjects(target,baseRequest,request,response);
			 baseRequest.setHandled(true);
		}
	
	private void doShowBam(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)	throws IOException, ServletException
		{
		}
		
	private void doListProjects(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)	throws IOException, ServletException
		{
		 response.setContentType("text/html; charset=utf-8");
		 response.setCharacterEncoding("UTF-8");
		 PrintWriter pw = response.getWriter();
		 try {
			XMLStreamWriter w=XMLOutputFactory.newFactory().createXMLStreamWriter(pw);
			w.writeStartElement("html");
			w.writeStartElement("body");
			w.writeStartElement("dl");
			for(final ProjectFile pf:this.projects)
				{
				w.writeStartElement("dt");
				pf.anchor(w);
				w.writeEndElement();
				w.writeStartElement("dd");
				w.writeCharacters(pf.getDefinition());
				w.writeEndElement();
				}
			w.writeEndElement();
			w.writeEndElement();
			w.writeEndElement();
			w.flush();
			w.close();
		 	}
		 catch(XMLStreamException err) { throw new IOException(err);}
		 finally { pw.close(); }
		}
		
	}
	
	@Override
	protected Handler createDefaultHandler(final List<String> args) {
		return new ProjectHandler(new File(args.get(0)));
	}
	
	@Override
	public java.util.Collection<Throwable> call() throws Exception
	{
	try {
		final List<String> inputFiles = super.getInputFiles();
		if( inputFiles.size()!=1) {
			return wrapException("Illegal Argument. Expected one config file");
		}
		super.createAndRunServer(inputFiles);
	    return RETURN_OK;
		}
	catch (Exception e) {
		return wrapException(e);
		}
	
	}	


public static void main(String[] args) throws Exception{
    new ProjectServer().instanceMainWithExit(args);
	}

}
