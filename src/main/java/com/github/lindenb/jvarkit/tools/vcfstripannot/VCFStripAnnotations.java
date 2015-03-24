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
package com.github.lindenb.jvarkit.tools.vcfstripannot;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.AbstractVCFFilter3;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


public class VCFStripAnnotations extends AbstractVCFFilter3
	{
	private Set<String> KEY=new HashSet<String>();
	private Set<String> FORMAT=new HashSet<String>();
	private Set<String> FILTER=new HashSet<String>();
	private boolean inverse=false;
	
	public VCFStripAnnotations()
		{
		}
	
	@Override
	protected String getOnlineDocUrl()
		{
		return DEFAULT_WIKI_PREFIX+"VCFStripAnnotations";
		}
	
	@Override
	protected String getProgramCommandLine()
		{
		return " Removes one or more field from the INFO/FORMAT column of a VCF.";
		}
	
	@Override
	public void printOptions(PrintStream out)
		{
		out.println(" -k (key) remove this INFO attribute. '*'= all keys");
		out.println(" -f (format) remove this FORMAT attribute. '*'= all keys BUT GT/DP/AD/GQ/PL");
		out.println(" -F (filter) remove this FILTER. '*'= all keys.");
		out.println(" -v inverse selection.");
		out.println(" -o (file) output file. Default stdout.");
		super.printOptions(out);
		}
	
	private boolean inSet(Set<String> set,String key)
		{
		if(!inverse)
			{
			return set.contains(key);
			}
		else
			{
			return !set.contains(key);
			}
		}
	
	@Override
	protected void doWork(String source,VcfIterator r, VariantContextWriter w)
			throws IOException
			{
			VCFHeader header=r.getHeader();
			boolean remove_all_info=this.KEY.contains("*");
			boolean remove_all_format=this.FORMAT.contains("*");
			boolean remove_all_filters=this.FILTER.contains("*");
			
			VCFHeader h2=new VCFHeader(header.getMetaDataInInputOrder(),header.getSampleNamesInOrder());
			
			for(Iterator<VCFInfoHeaderLine> h=h2.getInfoHeaderLines().iterator();
					h.hasNext();)
				{
				VCFInfoHeaderLine vih=h.next();
				if(inSet(this.KEY,vih.getID()))
					h.remove();
				}
			header.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"CmdLine",String.valueOf(getProgramCommandLine())));
			header.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"Version",String.valueOf(getVersion())));
			
			
			SAMSequenceDictionaryProgress progress= new SAMSequenceDictionaryProgress(h2);

			
			w.writeHeader(h2);
			
			while(r.hasNext())
				{
				VariantContext ctx=progress.watch(r.next());
				VariantContextBuilder b=new VariantContextBuilder(ctx);
				/* INFO */
				if(remove_all_info)
					{
					for(String k2: ctx.getAttributes().keySet())
						{
						b.rmAttribute(k2);
						}
					}
				else if(!KEY.isEmpty())
					{
					for(String k2: ctx.getAttributes().keySet())
						{
						if(inSet(KEY, k2))
							{
							b.rmAttribute(k2);
							}
						}
					}
				
				/* formats */
				if(remove_all_format)
					{
					List<Genotype> genotypes=new ArrayList<Genotype>();
					for(Genotype g:ctx.getGenotypes())
						{
						GenotypeBuilder gb=new GenotypeBuilder(g);
						gb.attributes(new HashMap<String, Object>());
						genotypes.add(gb.make());
						}
					b.genotypes(genotypes);
					}
				else if(! this.FORMAT.isEmpty())
					{
					List<Genotype> genotypes=new ArrayList<Genotype>();
					for(Genotype g:ctx.getGenotypes())
						{
						GenotypeBuilder gb=new GenotypeBuilder(g);
						Map<String, Object> map=new HashMap<String, Object>();
						for(String key: g.getExtendedAttributes().keySet())
							{
							if(inSet(this.FORMAT,key)) continue;
							map.put(key, g.getExtendedAttribute(key));
							}
						gb.attributes(map);
						genotypes.add(gb.make());
						}
					b.genotypes(genotypes);
					}
				
				/* filters */
				if(remove_all_filters)
					{
					b.unfiltered();
					}
				else if(! this.FILTER.isEmpty())
					{
					b.unfiltered();
					for(String key:ctx.getFilters())
						{
						if(inSet(this.FILTER,key)) continue;
						if(key.equals("PASS")) continue;
						b.filter(key);
						}
					}
				w.add(b.make());
				this.incrVariantCount();
				if(this.checkOutputError()) break;
				}	
			progress.finish();
			}
	
	
	@Override
	public int doWork(String[] args)
		{
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+ "o:k:F:f:v"))!=-1)
			{
			switch(c)
				{
				case 'v': inverse=true;break;
				case 'k': this.KEY.add(opt.getOptArg()); break;
				case 'f': this.FORMAT.add(opt.getOptArg()); break;
				case 'F': this.FILTER.add(opt.getOptArg()); break;
				case 'o': this.setOutputFile(opt.getOptArg());break;
				default: 
					{
					switch(handleOtherOptions(c, opt, args))
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

	
	public static void main(String[] args) throws IOException
		{
		new VCFStripAnnotations().instanceMainWithExit(args);
		}
	}
