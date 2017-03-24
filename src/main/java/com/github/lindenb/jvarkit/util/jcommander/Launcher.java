package com.github.lindenb.jvarkit.util.jcommander;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.zip.Deflater;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import com.beust.jcommander.converters.FileConverter;
import com.beust.jcommander.converters.IntegerConverter;
import com.github.lindenb.jvarkit.util.log.Logger;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.IOUtil;



public class Launcher {
private static final Logger LOG=Logger.build().
			prefix("Launcher").
			make();
	
	
public enum Status { OK, PRINT_HELP,PRINT_VERSION,EXIT_SUCCESS,EXIT_FAILURE};


/** custom instance of jcommander, don't add same command twice. */
private class MyJCommander extends JCommander
	{
	/** when registering the option for jcommander, we take care of not visiting object twice */
	private Collection<Object> ojectsVisitedByJCommander = new ArrayList<>();

	
	@Override
	public void usage(StringBuilder sb) {
		super.usage(sb);
		InputStream in=null;
		try {
			final Class<?> clazz=Launcher.this.getClass();
			String className=clazz.getName();
			int dollar=className.indexOf('$');
			if(dollar!=-1) className=className.substring(0, dollar);
			className=className.replace('.', '/')+".java";
			in=clazz.getResourceAsStream("/"+className);
			if(in!=null){
				BufferedReader r=new BufferedReader(new InputStreamReader(in));
				String line;
				boolean ok=false;
				while((line=r.readLine())!=null)
					{
					if(line.contains("BEGIN_DOC"))
						{
						ok=true;
						}
					else if(line.contains("END_DOC"))
						{
						ok=false;
						}
					else if(ok)
						{
						if(line.startsWith(" *")) line=line.substring(0, 2);
						sb.append(line).append("\n");
						}
					}
				r.close();
				}
			else
				{
				System.err.println("cannot find "+className);
				}
			}
		catch(final Exception err) {
			
			}
		finally
			{
			CloserUtil.close(in);
			}
		}
	
	@Override
	public void addCommand(String name, Object object, String... aliases) 
			{
			if(this.ojectsVisitedByJCommander.stream().anyMatch(O->O==object)) return;
			this.ojectsVisitedByJCommander.add(object);
			super.addCommand(name, object, aliases);
			}
	}
	

/**
 * Special converter for Zip compression. Bound the values between 0 and 9
 * "best" is interpreted as BEST_COMPRESSION
 * "none" is no compression
 */
public static class CompressionConverter
extends IntegerConverter implements Function<String, Integer> {
	public CompressionConverter() {
		super("");
		}
	public CompressionConverter(final String arg) {
		super(arg);
		}

	@Override
	public final Integer apply(String t) {
		return convert(t);
		}
	
	@Override
	public Integer convert(final String s) {
		if(s!=null) {
			if(s.equals("best")) return Deflater.BEST_COMPRESSION;
			if(s.equals("none")) return Deflater.NO_COMPRESSION;
		}
		final Integer n = super.convert(s);
		if(n!=null) {
			if(n<0) return Deflater.NO_COMPRESSION;
			if(n>9) return Deflater.BEST_COMPRESSION;
		}
		return n;
	}
	@Override
	public String toString() {
		return "Compression converter";
		}
	}




/** original arc/argv */
private List<String> argcargv=Collections.emptyList();
private final JCommander jcommander = new MyJCommander();


@Parameter(names = {"-h","--help"}, help = true)
private boolean print_help = false;
@Parameter(names = {"--version"}, help = true)
private boolean print_version = false;
@Parameter(description = "Files")
private List<String> files = new ArrayList<>();


public class CompressionArgs
	{
	@Parameter(names={"--compression"},description="Compression Level.",converter=CompressionConverter.class)
	public int compressionLevel=5;
	}
public CompressionArgs compressionArgs=new CompressionArgs();

public static class DirectoryExists implements IValueValidator<File> {
	@Override
	public void validate(String arg, final File dir) throws ParameterException {
		if(dir==null || !dir.exists() || !dir.isDirectory()) {
			throw new ParameterException("option :"+arg+": this is not a directory or it doesn't exists: "+dir);
			}
		}
	}

public static class TmpDirectoryArgs
	{
	@Parameter(names={"--tmpDir"},description="Temporary Directory.",validateValueWith=DirectoryExists.class)
	public File tmpDir=new File(System.getProperty("java.io.tmpdir","."));
	}


public static class SortingCollectionArgs
	{
	@ParametersDelegate
	private TmpDirectoryArgs tmpDirArgs;
	SortingCollectionArgs(TmpDirectoryArgs tmpDirArgs) {
		this.tmpDirArgs=tmpDirArgs;
		}
	@Parameter(names={"--xx"},description="Compression Level.",converter=CompressionConverter.class)
	public int compressionLevel=5;
	}


public static class WritingBamArgs
	{
	@Parameter(names={"--xx"},description="Compression Level.",converter=CompressionConverter.class)
	public int compressionLevel=5;
	}

public Launcher()
	{
	this.jcommander.addObject(this);	
	}

protected JCommander getJCommander()
	{
	return this.jcommander;
	}

/** called AFTER argc/argv has been initialized */
protected int initialize() {
	return 0;
	}

/** called AFTER the work is done, before returning the status */
protected void cleanup() {
	
	}

protected Status parseArgs(final String args[])
	{
	System.err.println(Arrays.asList(args));
	 try
	  	{
		getJCommander().parse(args);
	  	}
	 catch(final com.beust.jcommander.ParameterException err) {
		stderr().println("There was an error in the input parameters.");
		stderr().println(err.getMessage());
		return Status.EXIT_FAILURE; 
	 	}
	 
	 if (this.print_help) return Status.PRINT_HELP;
	 if (this.print_version) return Status.PRINT_VERSION;
	 return Status.OK;
	}

protected int doVcfToVcf(final String inputNameOrNull) {
	return -1;
	}



protected String oneFileOrNull(final List<String> args) {
	switch(args.size())
	{
	case 0: return null;
	case 1: return args.get(0);
	default: throw new IllegalArgumentException("Expected one or zero argument but got "+args.size());
	}
}

public int doWork(final List<String> args)
	{
	return -1;
	}
public int instanceMain(final String args[]) {
	int ret=0;
	try 
		{
		final Status status = parseArgs(args);
		switch(status)
			{
			case EXIT_FAILURE: return -1;
			case EXIT_SUCCESS: return 0;
			case PRINT_HELP: getJCommander().usage(); return 0;
			case PRINT_VERSION: return 0;
			case OK:break;
			}
		
		try 
			{
			ret=initialize();
			if(ret!=0) return ret;
			}
		catch(Throwable err)
			{
			LOG.fatal(err);
			return -1;
			}
		try 
			{
			ret=doWork(getFiles());
			if(ret!=0) return ret;
			}
		catch(Throwable err)
			{
			LOG.fatal(err);
			return -1;
			}
		}
	finally
		{
		cleanup();
		}
	return 0;
	}

public List<String> getFiles() {
	return Collections.unmodifiableList(files);
	}

public PrintStream stdout() { return System.out;}
public PrintStream stderr() { return System.err;}
public InputStream stdin() { return System.in;}




public void instanceMainWithExit( final String args[]) {
	System.exit( instanceMain(args) );
	}
public static void main(String[] args) {
	args=new String[]{ "--compression","best","a","b"};
	Launcher l=new Launcher();
	l.instanceMain(args);
	System.err.println(l.print_help);
	System.err.println(l.getFiles());
}
}
