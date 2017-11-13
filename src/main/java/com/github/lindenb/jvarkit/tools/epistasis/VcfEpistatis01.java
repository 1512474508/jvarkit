/*
The MIT License (MIT)

Copyright (c) 2017 Pierre Lindenbaum

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
package com.github.lindenb.jvarkit.tools.epistasis;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.tools.vcflist.VcfList;
import com.github.lindenb.jvarkit.tools.vcflist.VcfOffsetsIndexFactory;
import com.github.lindenb.jvarkit.util.Pedigree;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.log.Logger;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

public class VcfEpistatis01 extends Launcher {
	private static final Logger LOG = Logger.build(VcfEpistatis01.class).make();

	@Parameter(names={"-o","--output"},description=OPT_OUPUT_FILE_OR_STDOUT)
	private File outputFile = null;
	
	@Parameter(names={"-p","--pedigree"},description=Pedigree.OPT_DESCRIPTION)
	private File pedigreeFile = null;
	@Parameter(names={"--memory"},description="Load all variants in memory")
	private  boolean load_variants_in_memory=false;
	@Parameter(names={"-j","--jobs"},description="Number of parallel jobs.")
	private  int number_of_jobs =1;
	
	
	private static final Function<Long,Integer> CTRLS_nAlt2score=(N)->{switch(N.intValue()){
		case 0: return 0;
		case 1 : return -10;
		case 2: //cont
		default: return -30;
		}};
	private static final Function<Long,Integer> CASES_nAlt2score=(N)->{switch(N.intValue()){
		case 0: return 0;
		case 1 : return 10;
		case 2: //cont
		default: return 30;
		}};
	
	private static class Result
		{
		final VariantContext ctx1;
		final Allele a1;
		final int idx1;
		final VariantContext ctx2;
		final Allele a2;
		final int idx2;
		final double score;
		Result(final VariantContext ctx1,final Allele a1,int idx1,
			   final VariantContext ctx2, final Allele a2,int idx2,
			   double score
				)
			{
			this.ctx1 = ctx1;
			this.a1 = a1;
			this.idx1 = idx1;
			this.ctx2 = ctx2;
			this.a2 = a2;
			this.idx2 = idx2;
			this.score=score;
			}
		@Override
		public String toString() {
			return 
					ctx1.getContig()+":"+ctx1.getStart()+":"+ctx1.getReference()+"/"+a1+"["+idx1+"] | "+
					ctx2.getContig()+":"+ctx2.getStart()+":"+ctx2.getReference()+"/"+a2+"["+idx2+"] | "+
					score;
			}
		}
	
	
	private static  class Runner implements Callable<Result>
		{
		private final List<VariantContext> variants;
		private final List<Pedigree.Person> ctrlSamples;
		private final List<Pedigree.Person> caseSamples;
		private final int startIndex;
		private Result result = null;
		private long duration=0L;
		Runner(
				final List<VariantContext> variants,
				final int startIndex,
				final List<Pedigree.Person> ctrlSamples,
				final List<Pedigree.Person> caseSamples
				)
			{
			this.variants = variants;
			this.startIndex = startIndex;
			this.ctrlSamples = ctrlSamples;
			this.caseSamples = caseSamples;
			}
		
		private double score(
				final VariantContext ctx,
				final Allele alt,
				final List<Pedigree.Person> samples,
				final Function<Long,Integer> nAlt2score
				) 
			{
			double score_a1 = 0;
			
			for(final Pedigree.Person p : samples) {
				final Genotype g = ctx.getGenotype(p.getId());
				if(g==null || g.isFiltered()) continue;
				score_a1 +=nAlt2score.apply( g.getAlleles().stream().filter(A->A.equals(alt)).count());
				}
			return score_a1;
			}
		@Override
		public Result call() throws Exception {
			final VariantContext ctx1 = this.variants.get(this.startIndex);
			final long startup = System.currentTimeMillis();
			int i = this.startIndex + 1;
			
			while(i< this.variants.size())
				{
				final VariantContext ctx2 = this.variants.get(i);
				for(final Allele a1 : ctx1.getAlleles())//faster then getAlternateAlleles
					{
					if(a1.isReference()) continue;
					double score_a1 = 0;
					score_a1 += score(ctx1,a1,this.ctrlSamples,CTRLS_nAlt2score);
					score_a1 += score(ctx1,a1,this.caseSamples,CASES_nAlt2score);
					
					for(final Allele a2 : ctx2.getAlleles())//faster then getAlternateAlleles
						{
						if(a2.isReference()) continue;
						double score_a2 = score_a1;
						score_a2 += score(ctx2,a2,this.ctrlSamples,CTRLS_nAlt2score);
						score_a2 += score(ctx2,a2,this.caseSamples,CASES_nAlt2score);
						if(this.result == null || this.result.score< score_a2)
							{
							final Result nr = new Result(ctx1,a1,this.startIndex,ctx2,a2,i,score_a2);
							
							
							this.result = nr;
							}
						
						}
					}
				i++;
				}
			if(this.variants instanceof VcfList)
				{
				CloserUtil.close(VcfList.class.cast(this.variants));
				}
			this.duration = System.currentTimeMillis() - startup;
			LOG.info("index ["+startIndex+"] That took "+(duration/1000f)+" seconds.");
			return this.result;
			}
		}
	
	public VcfEpistatis01()
		{
		}
	
	
	@Override
	public int doWork(final List<String> args) {
		if(this.number_of_jobs<1) {
			LOG.error("bad number of jobs");
			return -1;
			}
		try
			{
			final int variantsCount;
			final List<VariantContext> inMemoryVariants;
			final File vcfFile = new File(oneAndOnlyOneFile(args));
			
			if(vcfFile.equals(this.outputFile))
				{
				LOG.error("input == output");
				return -1;
				}
			
			VCFFileReader vcfFileReader = new VCFFileReader(vcfFile,false);
			if(this.load_variants_in_memory) {
				LOG.info("loading variant in memory");
				final CloseableIterator<VariantContext> iter2=vcfFileReader.iterator();
				inMemoryVariants = iter2.stream().collect(Collectors.toList());
				variantsCount = inMemoryVariants.size();
				iter2.close();
				}
			else
				{
				new VcfOffsetsIndexFactory().setLogger(LOG).indexVcfFileIfNeeded(vcfFile);
				CloseableIterator<VariantContext> iter2=vcfFileReader.iterator();
				variantsCount = (int)iter2.stream().count();
				iter2.close();
				inMemoryVariants = null;
				}
			final VCFHeader header =  vcfFileReader.getFileHeader();
			vcfFileReader.close();
			
			
			final Pedigree pedigree;
			if(this.pedigreeFile!=null)
				{
				pedigree = new Pedigree.Parser().parse(this.pedigreeFile);
				}
			else
				{
				pedigree = new Pedigree.Parser().parse(header);
				}
			
			
			pedigree.verifyPersonsHaveUniqueNames();
			
			
			final List<Pedigree.Person> ctrlSamples = new ArrayList<>(pedigree.getUnaffected());
			final List<Pedigree.Person> caseSamples = new ArrayList<>(pedigree.getAffected());
			if( ctrlSamples.isEmpty() || caseSamples.isEmpty() )
					{
					LOG.error("empty ped or no case/ctrl");
					return -1;
					}
			
			Result bestResult =null;
			int x=0;
			while(x+1 < variantsCount)
				{
				final List<Runner> runners = new Vector<>(this.number_of_jobs);
				while(x+1 < variantsCount && runners.size() < this.number_of_jobs)
					{
					LOG.info("starting "+x+"/"+variantsCount);
					runners.add(new Runner(
							inMemoryVariants == null? VcfList.fromFile(vcfFile): inMemoryVariants
							,x,ctrlSamples,caseSamples));
					++x;
					}
				final ExecutorService execSvc;
				if(this.number_of_jobs==1)
					{
					execSvc = null;
					}
				else
					{
					execSvc = Executors.newFixedThreadPool(this.number_of_jobs);;
					}

				if(this.number_of_jobs==1)
					{
					runners.get(0).call();
					}
				else
					{
					execSvc.invokeAll(runners).stream().forEach(F->{try {F.get();}catch(Exception err2) {err2.printStackTrace();}});
					}
					
				runners.stream().mapToLong(R->R.duration).min().ifPresent(D->{
					LOG.info("That took "+ (D/1000f)+" seconds.");
					});
				
				if(execSvc!=null) execSvc.shutdownNow();
				
				for(final Runner r: runners)
					{
					final Result rez=r.result;
					if(rez==null) continue;
					if(bestResult==null || bestResult.score<rez.score)
						{
						bestResult =rez;
						
						final VariantContextWriter w = openVariantContextWriter(this.outputFile);
						final VCFHeader header2= new VCFHeader(header);
						header2.addMetaDataLine(new VCFHeaderLine(VcfEpistatis01.class.getName(),bestResult.toString()));
						w.writeHeader(header2);
						w.add(bestResult.ctx1);
						w.add(bestResult.ctx2);
						w.close();
						}
					}
				LOG.info("best: "+bestResult);
				}
			
			
			return 0;
			}
		catch(final Exception err)
			{
			err.printStackTrace();
			LOG.error(err);
			return -1;
			}
		finally
			{
			}
		}
	 	
	
	public static void main( String[] args)
		{
		//args=new String[] {"-j","2","--memory","-o","/home/lindenb/jeter2.vcf","/home/lindenb/jeter.vcf"};
		new VcfEpistatis01().instanceMainWithExit(args);
		}

}
