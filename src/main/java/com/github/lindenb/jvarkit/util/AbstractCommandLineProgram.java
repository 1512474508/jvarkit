package com.github.lindenb.jvarkit.util;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.github.lindenb.jvarkit.util.cli.GetOpt;

public abstract class AbstractCommandLineProgram
	{
	private static final Logger LOG=Logger.getLogger("jvarkit");
	private String commandLine="";
	private String version=null;
	private String compileDate;
	private List<File> tmpDirs=null;
	private ResourceBundle messagesBundle=null;
	
	protected AbstractCommandLineProgram()
		{
		final SimpleDateFormat datefmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LOG.setUseParentHandlers(false);
		LOG.addHandler(new Handler()
			{
			@Override
			public void publish(LogRecord record) {
				Date now = new Date(record.getMillis());
				System.err.print("["+record.getLevel()+"/"+AbstractCommandLineProgram.this.getClass().getSimpleName()+"]");
				System.err.print(" ");
				System.err.print(datefmt.format(now));
				System.err.print(" \"");
				System.err.print(record.getMessage());
				System.err.println("\"");
				if(record.getThrown()!=null)
					{
					record.getThrown().printStackTrace(System.err);
					}
				}
			
			@Override
			public void flush() {
				System.err.flush();
				}
			
			@Override
			public void close() throws SecurityException {
				
				}
			});
		try {
			this.messagesBundle=ResourceBundle.getBundle("messages");
			} 
		catch (Exception e)
			{
			LOG.warning("Cannot get messages bundle "+e);
			}
		}
	
	protected String getMessageBundle(String key)
		{
		if(key==null) return "(null)";
		if(this.messagesBundle==null) return key;
		try
			{
			return this.messagesBundle.getString(key);
			}
		catch(Exception err)
			{
			return key;
			}
		}
	
	/* logging stuff */
	protected void info(Object o)
		{
		getLogger().info(String.valueOf(o));
		}
	
	protected void error(Object o)
		{
		if(o!=null && (o instanceof Throwable))
			{
			Throwable T=Throwable.class.cast(o);
			error(T,T.getMessage());
			return;
			}
		getLogger().log(Level.SEVERE, String.valueOf(o));
		}
	
	protected void error(Throwable thrown,Object o)
		{
		getLogger().log(Level.SEVERE, String.valueOf(o), thrown);
		
		}
	
	protected void warning(Object o)
		{
		if(o!=null && (o instanceof Throwable))
			{
			Throwable T=Throwable.class.cast(o);
			warning(T,T.getMessage());
			return;
			}
		getLogger().log(Level.WARNING, String.valueOf(o));
		}
	
	protected void warning(Throwable thrown,Object o)
		{
		
		getLogger().log(Level.WARNING, String.valueOf(o), thrown);
		}
	
	protected void debug(Object o)
		{
		if(o!=null && (o instanceof Throwable))
			{
			Throwable T=Throwable.class.cast(o);
			debug(T,T.getMessage());
			return;
			}
		getLogger().log(Level.FINE, String.valueOf(o));
		}

	protected void debug(Throwable thrown,Object o)
		{
		
		getLogger().log(Level.FINE, String.valueOf(o), thrown);
		}

	
	
	protected void printStandardPreamble(PrintStream out)
		{
		out.println(getProgramName());
		out.println(getProgramDescription());
		out.println("Author      : "+getAuthorName());
		out.println("Mail        : "+getAuthorMail());
		out.println("WWW         : "+getOnlineDocUrl());
		out.println("Compilation : "+getCompileDate());
		out.println("Version     : "+getVersion());
		}
	
	
	
	protected String getOnlineDocUrl()
		{
		return "https://github.com/lindenb/jvarkit";
		}
	
	protected String getAuthorName()
		{
		return "Pierre Lindenbaum PhD.";
		}
	protected String getAuthorMail()
		{
		return "plinden"+"baum"+
				'@'+
				"yahoo"+
				'.'+
				"fr";
		}
	
	
	protected String getProgramCommandLine()
		{
		return commandLine;
		}
	
	
	protected void printSynopsis(PrintStream out)
		{
		out.println("\tjava -cp jar1.jar:jar2.jar:jar3.jar "+getClass().getName()+" [options] (files)");
		}
	public void printUsage(PrintStream out)
		{
		printStandardPreamble(out);
		out.println("Usage:");
		printSynopsis(out);
		out.println("Options:");
		printOptions(out);
		out.println();
		}
	
	public void printUsage()
		{
		printUsage(System.err);
		}
	
	public String getProgramName()
		{
		if(!getClass().isAnonymousClass()) return getClass().getSimpleName();
		return getClass().getName();
		}
	
	public String getProgramDescription()
		{
		return "";
		}
    
	public Logger getLogger()
		{
		return LOG;
		}
	
	public abstract int doWork(String args[]);
	
	private void loadManifest()
		{
		try
			{
			
				Enumeration<URL> resources = getClass().getClassLoader()
						  .getResources("META-INF/MANIFEST.MF");//not '/META-INF'
				while (resources.hasMoreElements())
					{
					URL url=resources.nextElement();
					InputStream in=url.openStream();
					if(in==null)
						{
						continue;
						}
					
					Manifest m=new Manifest(in);
					in.close();
					in=null;
					java.util.jar.Attributes attrs=m.getMainAttributes();
					if(attrs==null)
						{
						continue;
						}
					String s =attrs.getValue("Git-Hash");
					if(s!=null && !s.isEmpty() && !s.contains("$")) //ant failed
						{
						this.version=s;
						}
					
					s =attrs.getValue("Compile-Date");
					if(s!=null && !s.isEmpty()) //ant failed
						{
						this.compileDate=s;
						}
					}
			}	
		catch(Exception err)
			{
			
			}
		
		}
	
	public String getCompileDate()
		{
		if(this.compileDate==null)
			{
			this.compileDate="undefined";
			loadManifest();
			}
		return compileDate;
		}
	
	
	
	public String getVersion()
		{
		if(this.version==null)
			{
			this.version="1.0";
			loadManifest();
			}
		return version;
		}
	
	protected String getGetOptDefault()
		{
		return "hvL:";
		}
	
	protected enum GetOptStatus {OK,EXIT_FAILURE,EXIT_SUCCESS};
	
	

	public void printOptions(PrintStream out)
		{
		out.println(" -h get help (this screen)");
		out.println(" -v print version and exit.");
		out.println(" -L (level) log level. One of java.util.logging.Level . currently:"+getLogger().getLevel());
		}
	
	protected GetOptStatus handleOtherOptions(
			int c,
			com.github.lindenb.jvarkit.util.cli.GetOpt opt, String[] args
			)
		{
		switch(c)
			{
			case 'h': printUsage();return GetOptStatus.EXIT_SUCCESS;
			case 'v': System.out.println(getVersion());return GetOptStatus.EXIT_SUCCESS;
			case 'L':
				try
					{
					getLogger().setLevel(java.util.logging.Level.parse(opt.getOptArg()));
					}
				catch(Exception err)
					{
					System.err.println("Bad value for log level. Expected one of SEVERE WARNING ALL INFO FINE FINER FINEST OFF");
					return GetOptStatus.EXIT_FAILURE;
					}
				return GetOptStatus.OK;
			case GetOpt.MISSING_ARG: 
				{
				System.err.println("Missing argument for option -"+opt.getOptOpt());
				return GetOptStatus.EXIT_FAILURE;
				}
			case GetOpt.LONG_OPT:
				{
				if("help".equals(opt.getLongOpt()))
					{
					printUsage();
					return GetOptStatus.EXIT_SUCCESS;
					}
				System.err.println("Unknown long option \"--"+opt.getLongOpt()+"\"");
				return GetOptStatus.EXIT_FAILURE;
				}
			default:
				{
				System.err.println("Unknown option -"+opt.getOptOpt());
				return GetOptStatus.EXIT_FAILURE;
				}
			}
		}
 
	protected int getCountTmpDirectories()
		{
		return this.tmpDirs==null?0:this.tmpDirs.size();
		}
	
	protected void addTmpDirectory(File dirOrFile)
		{
		if(dirOrFile==null) return;
		if(dirOrFile.isFile())
			{
			dirOrFile=dirOrFile.getParentFile();
			if(dirOrFile==null) return;
			}
		if(this.tmpDirs==null)
			{
			this.tmpDirs=new ArrayList<File>();
			}
		
		this.tmpDirs.add(dirOrFile);
		}
	
	protected List<File> getTmpDirectories()
		{
		if(this.tmpDirs==null)
			{
			this.tmpDirs=new ArrayList<File>();
			}
		if(this.tmpDirs.isEmpty())
			{
			info("Adding 'java.io.tmpdir' directory to the list of tmp directories");
			this.tmpDirs.add(new File(System.getProperty("java.io.tmpdir")));
			}
		return this.tmpDirs;
		}
	
	protected int instanceMain(String args[])
		{
		StringBuilder b=new StringBuilder();
		for(int i=0;i< args.length;++i)
			{
			if(i!=0) b.append(' ');
			b.append(args[i]);
			}
		this.commandLine=b.toString().
				replace('\n', ' ').
				replace('\t', ' ').
				replace('\"', ' ').
				replace('\'', ' ');
		b=null;
		Date startDate=new Date();
		info("Starting JOB at "+startDate+" "+getClass().getName()+
				" version="+getVersion()+" "+
				" built="+getCompileDate());
		info("Command Line args : "+(this.commandLine.isEmpty()?"(EMPTY/NO-ARGS)":this.commandLine));
		String hostname="";
		try
			{
			hostname= InetAddress.getLocalHost().getHostName();
			}
		catch(Exception err)
			{
			hostname="host";
			}
		info("Executing as " +
                System.getProperty("user.name") + "@" + hostname +
                " on " + System.getProperty("os.name") + " " + System.getProperty("os.version") +
                " " + System.getProperty("os.arch") + "; " + System.getProperty("java.vm.name") +
                " " + System.getProperty("java.runtime.version") 
                );
		int ret=doWork(args);
	
		final Date endDate = new Date();
		final double elapsedMinutes = (endDate.getTime() - startDate.getTime()) / (1000d * 60d);
        final String elapsedString  = new DecimalFormat("#,##0.00").format(elapsedMinutes);
    	info("End JOB status="+ret+" [" + endDate + "] " + getClass().getName() + " done. Elapsed time: " + elapsedString + " minutes.");
		if(ret!=0)
			{
			error("##### ERROR: return status = "+ret+ "################");
			}
    	return ret;
		}
	
    public void instanceMainWithExit(final String[] argv) {
        System.exit(instanceMain(argv));
    }
}
