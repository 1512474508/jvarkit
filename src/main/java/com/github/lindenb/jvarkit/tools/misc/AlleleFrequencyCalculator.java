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

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

public class AlleleFrequencyCalculator extends Launcher
	{
	private static final Logger LOG = Logger.build(AlleleFrequencyCalculator.class).make();
	@Parameter(names={"-o","--output"},description="Output file. Optional . Default: stdout")
	private File outputFile = null;

	public AlleleFrequencyCalculator()
		{
		
		}
	@Override
	public int doWork(List<String> args)
		{
		PrintStream out = null;
		VcfIterator in = null;
		try
			{
			in = super.openVcfIterator(oneAndOnlyOneFile(args));
		
			out = openFileOrStdoutAsPrintStream(outputFile);
			
			
			out.println("CHR\tPOS\tID\tREF\tALT\tTOTAL_CNT\tALT_CNT\tFRQ");
			while(in.hasNext() && !out.checkError())
				{
				
				VariantContext ctx=in.next();
				Allele ref=ctx.getReference();
				if(ref==null) continue;
				if(ctx.getNSamples()==0 || ctx.getAlternateAlleles().isEmpty()) continue;
				Allele alt=ctx.getAltAlleleWithHighestAlleleCount();
				if(alt==null) continue;
				
				GenotypesContext genotypes=ctx.getGenotypes();
				if(genotypes==null) continue;
				int total_ctn=0;
				int alt_ctn=0;
				for(int i=0;i< genotypes.size();++i)
					{
					Genotype g=genotypes.get(i);
					for(Allele allele: g.getAlleles())
						{
						if(allele.equals(ref))
							{
							total_ctn++;
							}
						else if (allele.equals(alt))
							{
							total_ctn++;
							alt_ctn++;
							}
						}
					
					}
				
				
				out.print(ctx.getContig());
				out.print("\t");
				out.print(ctx.getStart());
				out.print("\t");
				out.print(ctx.hasID()?ctx.getID():".");
				out.print("\t");
				out.print(ref.getBaseString());
				out.print("\t");
				out.print(alt.getBaseString());
				out.print("\t");
				out.print(total_ctn);
				out.print("\t");
				out.print(alt_ctn);
				out.print("\t");
				out.print(alt_ctn/(float)total_ctn);
				out.println();
				}
			out.flush();
			
			
		return RETURN_OK;
		}
	catch(Exception err)
		{
		LOG.error(err);
		return -1;
		}
	finally
		{
		CloserUtil.close(out);
		CloserUtil.close(in);
		}
	}
	 	

			 
	
	public static void main(String[] args) {
		new AlleleFrequencyCalculator().instanceMainWithExit(args);

	}

}
