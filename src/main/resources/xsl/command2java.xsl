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
		<xsl:otherwise>com.github.lindenb.jvarkit.util.command.CommandFactory</xsl:otherwise>
	</xsl:choose>
	{
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(<xsl:apply-templates select="." mode="abstract-class-name"/>.class);
	<xsl:apply-templates select=".//c:option"/>
	<xsl:if test="not(@generate-output-option='false')">
		/** option outputFile */
		protected java.io.File outputFile = null;
		
		/** getter for outputFile */
		public java.io.File getOutputFile()
			{
			return this.outputFile;
			}
		
		/** setter for outputFile */
		public  void  setOutputFile( final java.io.File outputFile)
			{
			this.outputFile = outputFile;
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
	
	<xsl:choose>
		<xsl:when test="c:label">
		@Override
		public String getLabel()
			{
			return "<xsl:value-of select="c:label"/>";
			}
		</xsl:when>
		<xsl:when test="@label">
		@Override
		public String getLabel()
			{
			return "<xsl:value-of select="@label"/>";
			}
		</xsl:when>
	</xsl:choose>
	
	<xsl:choose>
		<xsl:when test="c:description">
		@Override
		public String getDescription()
			{
			return "<xsl:value-of select="c:description"/>";
			}
		</xsl:when>
		<xsl:when test="@description">
		@Override
		public String getDescription()
			{
			return "<xsl:value-of select="@description"/>";
			}
		</xsl:when>
	</xsl:choose>
	
	@Override
	protected void fillOptions(final org.apache.commons.cli.Options options)
		{
		<xsl:apply-templates select=".//c:option|.//c:options-group" mode="cli"/>
		
		<xsl:if test="not(@generate-output-option='false')">
		options.addOption(org.apache.commons.cli.Option
			.builder("o")
			.longOpt("output")
			.desc("output file. Default: stdout")
			.argName("FILENAME")
			.hasArg(true)
			.type(org.apache.commons.cli.PatternOptionBuilder.FILE_VALUE)
			.build() );	
		</xsl:if>
		
		super.fillOptions(options);
		}
	
	@Override
	protected  com.github.lindenb.jvarkit.util.command.CommandFactory.Status visit(final org.apache.commons.cli.Option opt)
		{
		<xsl:apply-templates select=".//c:option" mode="visit"/>
		<xsl:if test="not(@generate-output-option='false')">
		if(opt.getOpt().equals("o"))
			{
			java.io.File tmpf =  null;
			try { tmpf = new java.io.File(opt.getValue());}
			catch(Exception err) { LOG.error("Cannot cast "+opt.getValue()+" to output File",err); return com.github.lindenb.jvarkit.util.command.CommandFactory.Status.EXIT_FAILURE;}
			this.setOutputFile(tmpf);
			return com.github.lindenb.jvarkit.util.command.CommandFactory.Status.OK;
			}
		</xsl:if>
		return super.visit(opt);
		}
		
	@Override
	public String getVersion()
		{
		return "<xsl:value-of select="$githash"/>";
		}
		
	/** Command */
	static abstract class <xsl:apply-templates select="." mode="abstract-command-name"/>
		extends
		<xsl:choose>
			<xsl:when test="@extends-command"><xsl:value-of select="@extends-command"/></xsl:when>
			<xsl:otherwise> com.github.lindenb.jvarkit.util.command.Command</xsl:otherwise>
		</xsl:choose>
		{
		protected <xsl:apply-templates select="." mode="abstract-command-name"/>()
			{
			setLog( <xsl:apply-templates select="." mode="abstract-class-name"/>.LOG);
			}
			
		@Override
		public void copyFrom(final com.github.lindenb.jvarkit.util.command.CommandFactory f) {
			<xsl:apply-templates select="." mode="abstract-class-name"/> factory = <xsl:apply-templates select="." mode="abstract-class-name"/>.class.cast(f);
			super.copyFrom(f);
			<xsl:apply-templates select=".//c:option" mode="copy"/>
			
			<xsl:if test="not(@generate-output-option='false')">
			this.setOutputFile(factory.getOutputFile());
			</xsl:if>
			
			}
			
			
		<xsl:apply-templates select=".//c:option"/>
		
		
		<xsl:if test="not(@generate-output-option='false')">
		/** option outputFile */
		protected java.io.File outputFile = null;
		
		/** getter for outputFile */
		public java.io.File getOutputFile()
			{
			return this.outputFile;
			}
		
		/** setter for outputFile */
		public  void  setOutputFile( final java.io.File outputFile)
			{
			this.outputFile = outputFile;
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
		
		@Override
		public void cleanup()
			{
			super.cleanup();
			}
		
		<xsl:choose>
		<xsl:when test="not(c:input/@type)">
			<xsl:message terminate="no">input type undefined</xsl:message>
		</xsl:when>
		<xsl:when test="c:input/@type='stdin-or-one'">
		
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
				return wrapException("Illegal number of arguments.");
				}
			}
		</xsl:when>
		<xsl:otherwise>
		<xsl:message terminate="yes">undefined input/@type </xsl:message>
		</xsl:otherwise>
		</xsl:choose>
		
		
		}
	<xsl:if test="number($javaversion) &gt;= 8">
	<xsl:apply-templates select="." mode="jfx"/>
	</xsl:if>
	
	
	}
</xsl:template>




</xsl:stylesheet>


