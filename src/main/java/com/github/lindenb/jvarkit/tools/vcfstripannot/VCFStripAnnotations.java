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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


public class VCFStripAnnotations extends AbstractVCFStripAnnotations
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(VCFStripAnnotations.class);

	public VCFStripAnnotations()
		{
		}
	
	private boolean inSet(final Set<String> set,final String key)
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
	protected Collection<Throwable> doVcfToVcf(
			final String inputName,
			final VcfIterator r,
			final VariantContextWriter w
		) throws IOException {
		final VCFHeader header=r.getHeader();
		
		boolean remove_all_info=this.KEY.contains("*");
		boolean remove_all_format=this.FORMAT.contains("*");
		boolean remove_all_filters=this.FILTER.contains("*");
		
		final VCFHeader h2= new VCFHeader(header);
		addMetaData(h2);
		for(final Iterator<VCFInfoHeaderLine> h=h2.getInfoHeaderLines().iterator();
				h.hasNext();)
			{
			final VCFInfoHeaderLine vih=h.next();
			if(inSet(this.KEY,vih.getID()))
				h.remove();
			}			
		
		final SAMSequenceDictionaryProgress progress= new SAMSequenceDictionaryProgress(h2);
		
		w.writeHeader(h2);
		
		while(r.hasNext())
			{
			final VariantContext ctx=progress.watch(r.next());
			final VariantContextBuilder b=new VariantContextBuilder(ctx);
			/* INFO */
			if(remove_all_info)
				{
				for(final String k2: ctx.getAttributes().keySet())
					{
					b.rmAttribute(k2);
					}
				}
			else if(!KEY.isEmpty())
				{
				for(final String k2: ctx.getAttributes().keySet())
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
				final List<Genotype> genotypes=new ArrayList<Genotype>();
				for(Genotype g:ctx.getGenotypes())
					{
					final GenotypeBuilder gb=new GenotypeBuilder(g);
					gb.attributes(new HashMap<String, Object>());
					genotypes.add(gb.make());
					}
				b.genotypes(genotypes);
				}
			else if(! this.FORMAT.isEmpty())
				{
				final List<Genotype> genotypes=new ArrayList<Genotype>();
				for(final Genotype g:ctx.getGenotypes())
					{
					final GenotypeBuilder gb=new GenotypeBuilder(g);
					final Map<String, Object> map=new HashMap<String, Object>();
					for(final String key: g.getExtendedAttributes().keySet())
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
			
			if(w.checkError()) break;
			}	
		progress.finish();
		LOG.info("done");
		return RETURN_OK;
		}
	
	@Override
	protected Collection<Throwable> call(String inputName) throws Exception {
		return doVcfToVcf(inputName);
		}
	
	public static void main(String[] args) throws IOException
		{
		new VCFStripAnnotations().instanceMainWithExit(args);
		}
	}
