<?xml version='1.0'  encoding="UTF-8" ?>
<xsl:stylesheet
	version='1.0'
	xmlns:c="http://github.com/lindenb/jvarkit/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	>
<xsl:import href="mod.command.xsl"/>
<xsl:output method="text"/>
<xsl:param name="githash">undefined</xsl:param>
<xsl:param name="javaversion">7</xsl:param>

<xsl:template match="/">
 <xsl:apply-templates select="c:app"/>
</xsl:template>

<xsl:template match="c:app">
<xsl:apply-templates select="." mode="header"/>
package <xsl:apply-templates select="." mode="package"/>;


@javax.annotation.Generated("xslt")
public abstract class <xsl:apply-templates select="." mode="abstract-class-name"/>
	extends <xsl:choose>
		<xsl:when test="@extends"><xsl:value-of select="@extends"/></xsl:when>
		<xsl:otherwise> com.github.lindenb.jvarkit.util.command.Command </xsl:otherwise>
	</xsl:choose>
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(<xsl:apply-templates select="." mode="abstract-class-name"/>.class);
	<xsl:apply-templates select=".//c:option"/>
	
	<xsl:if test="c:output/@type='sam' or c:output/@type='bam'">
	private htsjdk.samtools.SamReader.Type outputformat= htsjdk.samtools.SamReader.Type.SAM_TYPE;
	</xsl:if>
	
	
	<xsl:if test="c:snippet[@id='sorting-collection'] or c:snippet[@id='tmp-dir']">
	/** list of tmp directories */
	private java.util.List&lt;java.io.File&gt; tmpDirs = null;
	
	
	/** add a temporary directory */
	public void addTmpDirectory(java.io.File dirOrFile)
		{
		if(dirOrFile==null) return;
		if(dirOrFile.isFile())
			{
			dirOrFile=dirOrFile.getParentFile();
			if(dirOrFile==null) return;
			}
		if(this.tmpDirs==null)
			{
			this.tmpDirs = new java.util.ArrayList&lt;java.io.File&gt;();
			}
		
		this.tmpDirs.add(dirOrFile);
		}
	/** returns a list of tmp directory */
	protected java.util.List&lt;java.io.File&gt; getTmpDirectories()
		{
		if(this.tmpDirs==null)
			{
			this.tmpDirs= new java.util.ArrayList&lt;java.io.File&gt;();
			}
		if(this.tmpDirs.isEmpty())
			{
			LOG.info("Adding 'java.io.tmpdir' directory to the list of tmp directories");
			this.tmpDirs.add(new java.io.File(System.getProperty("java.io.tmpdir")));
			}
		return this.tmpDirs;
		}
	</xsl:if>
		
	<xsl:if test="c:snippet[@id='sorting-collection']">
	/** When writing SAM files that need to be sorted, this will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed. */
	private int maxRecordsInRam = 500000;
	
	</xsl:if>
	
	<xsl:if test="c:snippet[@id='boolean.intervals']">

	protected htsjdk.samtools.util.IntervalTreeMap&lt;Boolean&gt; readBedFileAsBooleanIntervalTreeMap(final java.io.File file) throws java.io.IOException
		{
		java.io.BufferedReader r=null;
		try
			{
			final  htsjdk.samtools.util.IntervalTreeMap&lt;Boolean&gt; intervals = new
					 htsjdk.samtools.util.IntervalTreeMap&lt;Boolean&gt;();
			r=com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(file);
			String line;
			final java.util.regex.Pattern tab = java.util.regex.Pattern.compile("[\t]");
			while((line=r.readLine())!=null) 
				{
				if(line.startsWith("#") || line.startsWith("track") || line.startsWith("browser") ||  line.isEmpty()) continue; 
				final String tokens[]=tab.split(line,4);
				if(tokens.length &lt; 3)
					{
					throw new java.io.IOException("Bad bed line in "+file+" "+line);
					}	
				final htsjdk.samtools.util.Interval interval=new htsjdk.samtools.util.Interval(tokens[0],
						Integer.parseInt(tokens[1])+1,
						Integer.parseInt(tokens[2])
						);
				intervals.put(interval,true); 
				}
			return intervals;
			}
		finally
			{
			htsjdk.samtools.util.CloserUtil.close(r);
			}
		}

	</xsl:if>

	<xsl:if test="c:snippet[@id='md5']">
	 private java.security.MessageDigest _md5 = null;
	 
          protected String md5(final String in)
	    	{
		 if(_md5==null) {
	    	  try {
	              _md5 = java.security.MessageDigest.getInstance("MD5");
	          } catch (java.security.NoSuchAlgorithmException e) {
	              throw new RuntimeException("MD5 algorithm not found", e);
	          }}
	    	
    	 _md5.reset();
         _md5.update(in.getBytes());
         String s = new java.math.BigInteger(1, _md5.digest()).toString(16);
         if (s.length() != 32) {
             final String zeros = "00000000000000000000000000000000";
             s = zeros.substring(0, 32 - s.length()) + s;
         }
         return s;
    	}
	
	</xsl:if>
	

	
	
	<xsl:if test="not(@generate-constructor='false')">
	/** Constructor */
	protected <xsl:apply-templates select="." mode="abstract-class-name"/>()
		{
		}
	</xsl:if>


	/** return application Name */
	@Override
	public String getName()
		{
		return "<xsl:apply-templates select="." mode="class-name"/>";
		}
	
	<xsl:apply-templates select="." mode="label"/>
	<xsl:apply-templates select="." mode="description"/>	
	
	@Override
	protected void fillOptions(final org.apache.commons.cli.Options options)
		{
		<xsl:apply-templates select=".//c:option|.//c:options-group" mode="cli"/>
		
		
		<xsl:if test="not(@generate-output-option='false')">
		options.addOption(org.apache.commons.cli.Option
			.builder("o")
			.longOpt("output")
			.desc("output file. <xsl:if test="c:output/@type='sam' or c:output/@type='bam'"> extension should be .sam or .bam </xsl:if> Default: stdout")
			.argName("FILENAME")
			.hasArg(true)
			.type(org.apache.commons.cli.PatternOptionBuilder.FILE_VALUE)
			.build() );	
		</xsl:if>
		
		<xsl:if test="c:output/@type='sam' or c:output/@type='bam'">
		options.addOption(org.apache.commons.cli.Option
			.builder("formatout")
			.longOpt("formatout")
			.desc("output format : sam or bam if stdout")
			.argName("FORMAT")
			.hasArg(true)
			.type(org.apache.commons.cli.PatternOptionBuilder.STRING_VALUE)
			.build() );	
		</xsl:if>
		
		
		<xsl:if test="c:snippet[@id='sorting-collection'] or c:snippet[@id='tmp-dir']">
		options.addOption(org.apache.commons.cli.Option
			.builder("tmpdir")
			.longOpt("tmpdir")
			.desc("add tmp directory")
			.argName("DIR")
			.hasArg()
			.type(org.apache.commons.cli.PatternOptionBuilder.FILE_VALUE)
			.build() );	
		</xsl:if>
		
		<xsl:if test="c:snippet[@id='sorting-collection']">
		options.addOption(org.apache.commons.cli.Option
			.builder("maxrecordsinram")
			.longOpt("maxrecordsinram")
			.desc("When writing files that need to be sorted, this will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed.")
			.argName("MAXRECORDS")
			.hasArg(true)
			.type(org.apache.commons.cli.PatternOptionBuilder.NUMBER_VALUE)
			.build() );	
		</xsl:if>
		
		<xsl:if test="c:snippet[@id='javascript']">
		options.addOption(org.apache.commons.cli.Option
			.builder("f")
			.longOpt("scriptfile")
			.desc("javascript file.")
			.argName("FILE.js")
			.hasArg()
			.type(org.apache.commons.cli.PatternOptionBuilder.FILE_VALUE)
			.build() );	
			
		options.addOption(org.apache.commons.cli.Option
			.builder("e")
			.longOpt("expression")
			.desc("javascript expression.")
			.argName("JAVASCRIPT_EXPRESSION")
			.hasArg()
			.type(org.apache.commons.cli.PatternOptionBuilder.STRING_VALUE)
			.build() );	
			
			
		</xsl:if>
		
		
		super.fillOptions(options);
		}
	
	@Override
	protected  com.github.lindenb.jvarkit.util.command.Command.Status visit(final org.apache.commons.cli.Option opt)
		{
		<xsl:apply-templates select=".//c:option" mode="visit"/>
		

		
		<xsl:if test="not(@generate-output-option='false')">
		if(opt.getOpt().equals("o"))
			{
			java.io.File tmpf =  null;
			try { tmpf = new java.io.File(opt.getValue());}
			catch(Exception err) { LOG.error("Cannot cast "+opt.getValue()+" to output File",err); return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;}
			this.setOutputFile(tmpf);
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}
		</xsl:if>
		<xsl:if test="c:output/@type='sam' or c:output/@type='bam'">
		if(opt.getOpt().equals("formatout"))
			{
			String formatout= opt.getValue().toLowerCase();
			if(!formatout.startsWith(".")) formatout="."+formatout;
			if( formatout.equals(".bam"))
				{
				this.outputformat = htsjdk.samtools.SamReader.Type.BAM_TYPE;
				}
			else if( formatout.equals(".sam"))
				{
				this.outputformat = htsjdk.samtools.SamReader.Type.SAM_TYPE;
				}
			else
				{
				LOG.error(formatout+" is not a valid extension.");
				return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;
				}
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}
		</xsl:if>
		
		
		<xsl:if test="c:snippet[@id='sorting-collection'] or c:snippet[@id='tmp-dir']">
		if(opt.getOpt().equals("tmpdir"))
			{
			for(String s:opt.getValues())
				{
				try {
					java.io.File  dir = new java.io.File(s);
					this.addTmpDirectory(dir);
					}
				catch(Exception err) { LOG.error("Cannot cast "+s+" to File",err); return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;}
				}
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}

		</xsl:if>
		
		<xsl:if test="c:snippet[@id='sorting-collection']">
		if(opt.getOpt().equals("maxrecordsinram"))
			{
			try
				{
				this.maxRecordsInRam = Integer.parseInt(opt.getValue());
				if(this.maxRecordsInRam&lt;2)
					{
					LOG.error(opt.getValue()+" is not a valid value (&lt;2).");
					}
				}
			catch(Exception err)
				{
				LOG.error(opt.getValue()+" not a valid value for "+opt.getOpt());
				return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;
				}
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}
		</xsl:if>
		
		
		<xsl:if test="c:snippet[@id='javascript']">

		if(opt.getOpt().equals("f"))
			{
			java.io.File f =  null;
			try { f = new java.io.File(opt.getValue());}
			catch(Exception err) { LOG.error("Cannot cast "+opt.getValue()+" to output File",err); return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;}
			if(!(f.exists() &amp;&amp; f.isFile()))
				{
				LOG.error("Not an existing file:"+f);
				return com.github.lindenb.jvarkit.util.command.Command.Status.EXIT_FAILURE;
				}
			this.setJavascriptFile(f);
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}
		
		if(opt.getOpt().equals("e"))
			{
			this.setJavascriptExpr(opt.getValue());
			return com.github.lindenb.jvarkit.util.command.Command.Status.OK;
			}
		</xsl:if>
		
		return super.visit(opt);
		}
		
	@Override
	public String getGitHash()
		{
		return "<xsl:value-of select="$githash"/>";
		}
		
		
		<xsl:if test="c:output/@type='fastq'">
		
		/** open output as a  htsjdk.samtools.fastq.FastqWriter */
		protected htsjdk.samtools.fastq.FastqWriter openFastqWriter()
			{
			if(getOutputFile()!=null)
				{
				LOG.info("Writing to "+getOutputFile());
				return new htsjdk.samtools.fastq.BasicFastqWriter(getOutputFile());
				}
			else
				{
				LOG.info("Writing to stdout");
				return new htsjdk.samtools.fastq.BasicFastqWriter(stdout());
				}
			}
		</xsl:if>
		
		<xsl:if test="c:output/@type='sam' or c:output/@type='bam'">
		
				protected htsjdk.samtools.SAMFileWriter openSAMFileWriter(final htsjdk.samtools.SAMFileHeader header,final boolean presorted)
			{
			final htsjdk.samtools.SAMFileWriterFactory sfw= new htsjdk.samtools.SAMFileWriterFactory();
			if(getOutputFile()==null)
				{
				if(this.outputformat==null || this.outputformat.equals(htsjdk.samtools.SamReader.Type.SAM_TYPE))
					{
					LOG.info("Saving as SAM");
					return sfw.makeSAMWriter(header, presorted, stdout());
					}
				else if( this.outputformat.equals(htsjdk.samtools.SamReader.Type.BAM_TYPE))
					{
					LOG.info("Saving as BAM");
					return sfw.makeBAMWriter(header, presorted, stdout());
					}
				else
					{
					throw new IllegalStateException("Bad output format");
					}
				}
			else
				{
				LOG.info("Saving as "+ getOutputFile());
				return sfw.makeSAMOrBAMWriter(header, presorted, getOutputFile());
				}
			}

		
		</xsl:if>
		
		
		
		
		<xsl:if test="not(@generate-output-option='false')">

		
		protected java.io.PrintWriter openFileOrStdoutAsPrintWriter() throws java.io.IOException
			{
			if(getOutputFile()!=null)
				{
				return new java.io.PrintWriter(getOutputFile());
				}
			else
				{
				return new java.io.PrintWriter( stdout() );
				}
			}
		
		protected java.io.PrintStream openFileOrStdoutAsPrintStream() throws java.io.IOException
			{
			if(getOutputFile()!=null)
				{
				return new java.io.PrintStream(getOutputFile());
				}
			else
				{
				return stdout();
				}
			}
		protected java.io.OutputStream openFileOrStdoutAsStream() throws java.io.IOException
			{
			if(getOutputFile()!=null)
				{
				return com.github.lindenb.jvarkit.io.IOUtils.openFileForWriting(getOutputFile());
				}
			else
				{
				return stdout();
				}
			}
		</xsl:if>
		

		
		
		

		<xsl:if test="c:output/@type='vcf' and c:input/@type='vcf'">
		

		
		protected java.util.Collection&lt;Throwable&gt; doVcfToVcf(
			final String inputName,
			final com.github.lindenb.jvarkit.util.vcf.VcfIterator in,
			final htsjdk.variant.variantcontext.writer.VariantContextWriter out
			) throws java.io.IOException
			{
			throw new RuntimeException("No implemented!!!");
			}
		
		protected java.util.Collection&lt;Throwable&gt; doVcfToVcf(String inputName) throws Exception
			{
			com.github.lindenb.jvarkit.util.vcf.VcfIterator in = null;
			htsjdk.variant.variantcontext.writer.VariantContextWriter w=null;
			try {
				in= openVcfIterator(inputName);
				w = openVariantContextWriter();
				return doVcfToVcf(inputName==null?"&lt;STDIN&gt;":inputName,in,w);
			} catch (Exception e) {
				return wrapException(e);
				}
			finally
				{
				htsjdk.samtools.util.CloserUtil.close(in);
				htsjdk.samtools.util.CloserUtil.close(w);
				}
			}

		</xsl:if>
		
		
		<xsl:if test="c:output/@type='vcf' or c:snippet[@id='write-vcf']">
		
			
		/** count variants */
		private int <xsl:value-of select="concat('count_variants_',generate-id())"/> = 0;
		protected class VariantContextWriterCounter implements htsjdk.variant.variantcontext.writer.VariantContextWriter
			{
			htsjdk.variant.variantcontext.writer.VariantContextWriter delegate;
			VariantContextWriterCounter(final htsjdk.variant.variantcontext.writer.VariantContextWriter delegate)
				{
				this.delegate=delegate;
				<xsl:value-of select="concat('count_variants_',generate-id())"/> = 0 ; 
				}
			@Override
			public void add(final htsjdk.variant.variantcontext.VariantContext vc) {
				this.delegate.add(vc);
				++<xsl:value-of select="concat('count_variants_',generate-id())"/>;
				}
			@Override
			public boolean checkError() {
				return this.delegate.checkError();
				}
			@Override
			public void close() {
				this.delegate.close();
				}
			@Override
			public void writeHeader(final htsjdk.variant.vcf.VCFHeader header) {
				this.delegate.writeHeader(header);
				<xsl:value-of select="concat('count_variants_',generate-id())"/> = 0;
				}

			}
		/** return the number of variants in the output vcf */
		public int getVariantCount()
			{
			return <xsl:value-of select="concat('count_variants_',generate-id())"/>;
			}

		
		protected htsjdk.variant.vcf.VCFHeader addMetaData(final htsjdk.variant.vcf.VCFHeader header)
			{
			final java.util.Set&lt;htsjdk.variant.vcf.VCFHeaderLine&gt; set = new java.util.HashSet&lt;htsjdk.variant.vcf.VCFHeaderLine&gt;();
			addMetaData(set);
			for(final htsjdk.variant.vcf.VCFHeaderLine h : set)
				{
				header.addMetaDataLine(h);
				}
			return header;
			}
		
		protected java.util.Set&lt;htsjdk.variant.vcf.VCFHeaderLine&gt; addMetaData(final java.util.Set&lt;htsjdk.variant.vcf.VCFHeaderLine&gt; metaData)
			{
			metaData.add(new htsjdk.variant.vcf.VCFHeaderLine(getName()+"CmdLine",String.valueOf(getProgramCommandLine())));
			metaData.add(new htsjdk.variant.vcf.VCFHeaderLine(getName()+"Version",String.valueOf(getVersion())));
			metaData.add(new htsjdk.variant.vcf.VCFHeaderLine(getName()+"HtsJdkVersion",com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion
.getVersion()));
			metaData.add(new htsjdk.variant.vcf.VCFHeaderLine(getName()+"HtsJdkHome",com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion
.getHome()));
			return metaData;
			}
		
		/* creates a VariantContextWriter according to FileOUt */
		protected  VariantContextWriterCounter openVariantContextWriter()
			throws java.io.IOException
			{
			htsjdk.variant.variantcontext.writer.VariantContextWriter delegate = null;
			if(getOutputFile()!=null)
				{
				delegate = com.github.lindenb.jvarkit.util.vcf.VCFUtils.createVariantContextWriter(this.getOutputFile());
				}
			else
				{
				delegate = com.github.lindenb.jvarkit.util.vcf.VCFUtils.createVariantContextWriterToOutputStream(stdout());
				}
			return new VariantContextWriterCounter(delegate);
			}
		</xsl:if>
		

		<xsl:if test="c:input/@type='sam' or c:snippet[@id='read-sam']">
		
		protected htsjdk.samtools.SamReaderFactory createSamReaderFactory()
			{
			return  htsjdk.samtools.SamReaderFactory.makeDefault().validationStringency(htsjdk.samtools.ValidationStringency.LENIENT);
			}
		
		/** open a new SAM reader; If inputName==null, it reads from stdin */
		protected htsjdk.samtools.SamReader openSamReader(final String inputName)
			{
			final htsjdk.samtools.SamReaderFactory srf= this.createSamReaderFactory();
			if(inputName==null)
				{
				LOG.info("opening stdin");
				return srf.open(htsjdk.samtools.SamInputResource.of(stdin()));
				}
			else
				{
				LOG.info("opening "+inputName);
				return srf.open(htsjdk.samtools.SamInputResource.of(inputName));
				}
			}

		</xsl:if>

		
		<xsl:choose>
		<xsl:when test="not(c:input/@type)">
			<xsl:message terminate="no">warning: input type undefined</xsl:message>
		</xsl:when>
		<xsl:when test="c:input/@type='stdin-or-one' or c:input/@type='sam' or c:input/@type='vcf'">
		

		<xsl:if test="c:input/@type='vcf'">
		
		/* creates a VCF iterator from inputName. */
		protected com.github.lindenb.jvarkit.util.vcf.VcfIterator openVcfIterator(final String inputName)
			throws java.io.IOException
			{
			if( inputName == null )
					{
					return com.github.lindenb.jvarkit.util.vcf.VCFUtils.createVcfIteratorFromStream(stdin());
					}
				else
					{
					return com.github.lindenb.jvarkit.util.vcf.VCFUtils.createVcfIterator(inputName);
					}
			}

		</xsl:if>
		

		
		<xsl:if test="c:input/@type='sam' or c:snippet[@id='read-sam']">
		<!-- already done -->
		</xsl:if>
		
		/** program should process this file or stdin() if inputName is null */ 
		protected abstract java.util.Collection&lt;Throwable&gt; call(final String inputName) throws Exception;
		
		@Override
		public  java.util.Collection&lt;Throwable&gt; call() throws Exception
			{
			final java.util.List&lt;String&gt; args= getInputFiles();
			if(args.isEmpty())
				{
				return call(null);
				}
			else if(args.size()==1)
				{
				final String filename = args.get(0);
				return call(filename);
				}
			else
				{
				return wrapException(getMessageBundle("illegal.number.of.arguments"));
				}
			}
		</xsl:when>
		<xsl:when test="c:input/@type='strings'">
		/* input type is 'strings' */
		</xsl:when>
		<xsl:when test="c:input/@type='xml'">
		/* input type is 'xml' */
		</xsl:when>
		<xsl:when test="c:input/@type='fastq'">
		protected htsjdk.samtools.fastq.FastqReader openFastqReader(final String inputName)
			throws java.io.IOException
			{
			java.io.File f = null;
			java.io.BufferedReader r = null;
			if( inputName == null)
				{
				r = new java.io.BufferedReader( new java.io.InputStreamReader( stdin() ) );
				}
			else
				{
				f = new java.io.File(inputName);
				r = com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(f);
				}
			return new  htsjdk.samtools.fastq.FastqReader(f,r,false);
			}
		
		
		
		</xsl:when>
		
		
		<xsl:when test="c:input/@type='directory'">
		/** program should process this existing directory */ 
		protected abstract java.util.Collection&lt;Throwable&gt; call(final java.io.File directory) throws Exception;
		
		@Override
		public  java.util.Collection&lt;Throwable&gt; call() throws Exception
			{
			final java.util.List&lt;String&gt; args= getInputFiles();
			if(args.size()==1)
				{
				final String filename = args.get(0);
				final java.io.File dir = new java.io.File(filename);
				if(! dir.isDirectory())
					{
					return wrapException("Not a directory :"+dir);
					}
				return call(dir);
				}
			else
				{
				return wrapException(getMessageBundle("illegal.number.of.arguments"));
				}
			}
		</xsl:when>
		
		<xsl:when test="c:input/@type='stdin-or-many'">
		</xsl:when>
		<xsl:when test="c:input/@type='one-to-many'">
		</xsl:when>
		
		
		<xsl:otherwise>
		<xsl:message terminate="yes">undefined input/@type <xsl:value-of select="c:input/@type"/></xsl:message>
		</xsl:otherwise>
		</xsl:choose>
		
		
		<xsl:if test="c:snippet[@id='sorting-collection'] or c:snippet[@id='tmp-dir']">
		/** list of tmp directories */
		protected java.util.List&lt;java.io.File&gt; tmpDirs = new java.util.ArrayList&lt;java.io.File&gt;();
		
		
		protected java.util.List&lt;java.io.File&gt; getTmpDirectories()
			{
			return this.tmpDirs;
			}
		
		</xsl:if>
		
		<xsl:if test="c:snippet[@id='fastq-reader']">
		
		protected htsjdk.samtools.fastq.FastqReader openFastqFileReader(final java.io.File inputFile)
			throws java.io.IOException
			{
			java.io.File f = null;
			java.io.BufferedReader r = null;
			if( inputFile == null)
				{
				r = new java.io.BufferedReader( new java.io.InputStreamReader( stdin() ) );
				}
			else
				{
				f = inputFile;
				r = com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(f);
				}
			return new  htsjdk.samtools.fastq.FastqReader(f,r,false);
			}
		
		</xsl:if>
		
		<xsl:if test="c:snippet[@id='sorting-collection']">
		/** When writing SAM files that need to be sorted, this will specify the number of records stored in RAM before spilling to disk. Increasing this number reduces the number of file handles needed to sort a SAM file, and increases the amount of RAM needed. */
		protected int maxRecordsInRam = 500000;
		
		protected int getMaxRecordsInRam()
			{
			return this.maxRecordsInRam;
			}
		
		
		</xsl:if>
		
		
		<xsl:if test="c:snippet[@id='custom-chrom-mapping']">
		protected java.util.Map&lt;String,String&gt; loadCustomChromosomeMapping(final java.io.File mappingFile)
			throws java.io.IOException
			{
			java.util.Map&lt;String,String&gt; customMapping=new java.util.HashMap&lt;String,String&gt;();
			java.io.BufferedReader in = null;
			try
				{
				LOG.info("Loading custom mapping "+mappingFile);
				in = com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(mappingFile);
				String line;
				while((line=in.readLine())!=null)
					{
					if(line.isEmpty() || line.startsWith("#")) continue;
					String tokens[]=line.split("[\t]");
					if(tokens.length!=2
							|| tokens[0].trim().isEmpty()
							|| tokens[1].trim().isEmpty()
							|| tokens[0].equals(htsjdk.samtools.SAMRecord.NO_ALIGNMENT_REFERENCE_NAME)
							|| tokens[1].equals(htsjdk.samtools.SAMRecord.NO_ALIGNMENT_REFERENCE_NAME)
							)
							{
							in.close(); in =null;
							throw new java.io.IOException("Bad mapping line: \""+line+"\" ");
							}
					tokens[0]=tokens[0].trim();
					tokens[1]=tokens[1].trim();
					if(customMapping.containsKey(tokens[0]))
						{
						in.close(); in =null;
						throw new java.io.IOException("Mapping defined twice for: \""+tokens[0]+"\"");
						}
					customMapping.put(tokens[0], tokens[1]);
					}
				return customMapping;
				}
			finally
				{
				htsjdk.samtools.util.CloserUtil.close(in);
				}
			}
		</xsl:if>
		

		<xsl:if test="c:snippet[@id='javascript']">
		
		/** BEGIN : JAVASCRIPT SECTION ************************************************/
		
		<!-- defined in preproc 
		private String javascriptExpr=null;
		private	java.io.File javascriptFile=null;
		
		public String getJavascriptExpr() { return this.javascriptExpr;}
		public void setJavascriptExpr(final String ex) {  this.javascriptExpr = ex;}
		public java.io.File getJavascriptFile() { return this.javascriptFile;}	
		public void setJavascriptFile(final java.io.File f) {  this.javascriptFile = f;}
		-->
		
		
		protected boolean evalJavaScriptBoolean(
			final javax.script.CompiledScript compiledScript,
			final javax.script.Bindings bindings) throws javax.script.ScriptException
			{
			Object result = compiledScript.eval(bindings);
			if(result==null) return false;
			if(result instanceof Boolean)
				{
				if(Boolean.FALSE.equals(result)) return false;
				}
			else if(result instanceof Number)
				{
				if(((Number)result).intValue()!=1) return false;
				}
			else
				{
				LOG.warn("Script returned something that is not a boolean or a number:"+result.getClass());
				 return false;
				}
			return true;
			}
		
		/** compile the javascript script. Can be either from JavascriptFile or JavascriptExpr */
		protected javax.script.CompiledScript compileJavascript() throws Exception
			{
			if( getJavascriptExpr()!=null &amp;&amp; getJavascriptFile()!=null)
				{
				throw new RuntimeException("Both javascript expression and file defined.");
				}
			
			
			if( getJavascriptExpr()==null &amp;&amp; getJavascriptFile()==null)
				{
				throw new RuntimeException("User error : Undefined script. Check your parameters.");
				}
				
			LOG.info("getting javascript manager");
			final javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
			final javax.script.ScriptEngine engine = manager.getEngineByName("js");
			if(engine==null)
				{
				throw new RuntimeException("not available ScriptEngineManager: javascript. Use the SUN/Oracle JDK ?");
				}
			final javax.script.Compilable compilingEngine = (javax.script.Compilable)engine;
			if(getJavascriptFile()!=null)
				{
				LOG.info("Compiling "+getJavascriptFile());
				java.io.FileReader r = null;
				try
					{
					r = new java.io.FileReader(getJavascriptFile());
					return compilingEngine.compile(r);
					}
				finally
					{
					htsjdk.samtools.util.CloserUtil.close(r);
					}
				}
			else if(getJavascriptExpr()!=null)
				{
				LOG.info("Compiling "+getJavascriptExpr());
				return compilingEngine.compile(getJavascriptExpr());
				}
			else
				{
				throw new RuntimeException("illegal state");
				}
			}
		/** END : JAVASCRIPT SECTION ************************************************/
		</xsl:if>
		
		
		
		<xsl:if test="c:snippet[@id='read-string-set']">
		
		protected java.util.Set&lt;String&gt; readStringSet(final java.io.File f) throws java.io.IOException
			{
			final java.util.Set&lt;String&gt; set = new java.util.HashSet&lt;String&gt;();
			java.io.BufferedReader in= null;
			try
				{
				LOG.info("Reading "+f);
				in = com.github.lindenb.jvarkit.io.IOUtils.openFileForBufferedReading(f);
				String line;
				while((line=in.readLine())!=null)
					{
					if(line.startsWith("#")) continue;
					line=line.trim();
					if(line.trim().isEmpty()) continue;
					set.add(line);
					}
				return set;
				}
			finally
				{
				htsjdk.samtools.util.CloserUtil.close(in);
				}
			}
		
		</xsl:if>
	
	<xsl:if test="@ui-swing = 'true'">
	/** BEGIN SECTION SWING */
	
		@SuppressWarnings("serial")
		public static class <xsl:apply-templates select="." mode="swing-name"/>
			extends com.github.lindenb.jvarkit.tools.central.CentralPane
			{
			<xsl:apply-templates select="//c:option" mode="swing-declare"/>
			
			/* inputs : <xsl:value-of select="c:input/@type"/>*/
			<xsl:choose>
				<xsl:when test="c:input/@type='sam' or c:input/@type='vcf' or c:input/@type='stdin-or-one' ">
				private com.github.lindenb.jvarkit.util.swing.InputChooser _input = null;
				</xsl:when>
				<xsl:when test="c:input/@type='TODO'">

				</xsl:when>
				<xsl:otherwise>
				private com.github.lindenb.jvarkit.util.swing.MultipleInputChooser _inputs = null;
				</xsl:otherwise>
			</xsl:choose>
			
			
			
			public <xsl:apply-templates select="." mode="swing-name"/>()
				{
				super();
				final  javax.swing.JPanel top = new javax.swing.JPanel(new java.awt.BorderLayout());
				final javax.swing.JLabel title = new javax.swing.JLabel(getDescription());
				title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
				top.add(title,java.awt.BorderLayout.CENTER);
				this.add(top,java.awt.BorderLayout.NORTH);

				
				
				final  javax.swing.JPanel pane = new javax.swing.JPanel(new com.github.lindenb.jvarkit.util.swing.FormLayout());
				<xsl:apply-templates select="//c:option[not(@opt='o')]" mode="swing-build"/>
				
				
				/* inputs : <xsl:value-of select="c:input/@type"/>*/
				<xsl:choose>
					<xsl:when test="c:input/@type='sam' or c:input/@type='vcf'  or c:input/@type='stdin-or-one'">
					pane.add(new javax.swing.JLabel("Input:",javax.swing.JLabel.RIGHT));
					this._input = new com.github.lindenb.jvarkit.util.swing.InputChooser();
					pane.add(this._input);
					</xsl:when>
					<xsl:when test="c:input/@type='TODO'">
					</xsl:when>
					<xsl:otherwise>
					pane.add(new javax.swing.JLabel("Inputs:",javax.swing.JLabel.RIGHT));
					this._inputs = new com.github.lindenb.jvarkit.util.swing.MultipleInputChooser();
					pane.add(this._inputs);
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:apply-templates select="//c:option[@opt='o']" mode="swing-build"/>
				
				this.add(pane,java.awt.BorderLayout.CENTER);
				}
			<xsl:apply-templates select="." mode="label"/>
			<xsl:apply-templates select="." mode="description"/>
			@Override
			public Class&lt;? extends com.github.lindenb.jvarkit.util.command.Command&gt; getMainClass()
				{
				return <xsl:apply-templates select="." mode="class-name"/>.class;
				}
			@SuppressWarnings("unused")
			@Override
			public String getValidationMessage()
				{
				<xsl:apply-templates select="//c:option" mode="swing-validation"/>
				
				
				
				
				<xsl:choose>
					<xsl:when test="c:input/@type='sam' or c:input/@type='vcf' or c:input/@type='stdin-or-one'">
					if(this._input.getTextField().getText().trim().isEmpty())
						{
						return "No input defined";
						}
					</xsl:when>
					<xsl:otherwise>
					if(this._inputs.getAsList().isEmpty())
						{
						return "No input defined";
						}
					</xsl:otherwise>
				</xsl:choose>
				
				return super.getValidationMessage();
				}
			
			@Override
			public void fillCommandLine(final java.util.List&lt;String&gt; command)
				{
				super.fillCommandLine(command);
				<xsl:apply-templates select="//c:option" mode="swing-fill-command"/>
				<xsl:choose>
					<xsl:when test="c:input/@type='sam' or c:input/@type='vcf' or c:input/@type='stdin-or-one'">
						command.add(this._input.getTextField().getText().trim());
					</xsl:when>
					<xsl:otherwise>
					for(final String f:this._inputs.getAsList())
						{
						command.add(f);
						}
					</xsl:otherwise>
				</xsl:choose>
				}
			
			}
	/** END SECTION SWING */
	</xsl:if>
	
		
	<xsl:if test="number($javaversion) &gt;= 8">
	<xsl:apply-templates select="." mode="jfx"/>
	</xsl:if>
	}
</xsl:template>




</xsl:stylesheet>
