package com.github.lindenb.jvarkit.tools.vcfgo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.picard.cmdline.Usage;
import net.sf.picard.vcf.VcfIterator;

import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.variantcontext.VariantContextBuilder;
import org.broadinstitute.variant.variantcontext.writer.VariantContextWriter;
import org.broadinstitute.variant.vcf.VCFHeader;
import org.broadinstitute.variant.vcf.VCFHeaderLineCount;
import org.broadinstitute.variant.vcf.VCFHeaderLineType;
import org.broadinstitute.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.go.GoTree;

public class VcfGeneOntology extends AbstractVcfGeneOntology
	{
	@Usage(programVersion="1.0")
	public String USAGE=getStandardUsagePreamble()+" Find the GO terms for VCF annotated with SNPEFF or VEP. ";
	
	@Override
	public String getVersion()
		{
		return "1.0";
		}
	
	@Override
	protected void doWork(VcfIterator r, VariantContextWriter w)
			throws IOException
		{
		super.readGO();
		super.readGOA();
		super.loadBiomartHGNC();
		
		final String TAG="GOA";
		VCFHeader header=r.getHeader();
		
		
		
		VCFHeader h2=new VCFHeader(header.getMetaDataInInputOrder(),header.getSampleNamesInOrder());
		h2.addMetaDataLine(new VCFInfoHeaderLine(TAG,VCFHeaderLineCount.UNBOUNDED,VCFHeaderLineType.String,"GO terms from GO "+GO+" and GOA="+GOA));
		
		w.writeHeader(h2);
	
		while(r.hasNext())
			{
			VariantContext ctx=r.next();
			Set<String> geneNames=getGeneNames(ctx);
		
			
			List<String> atts=new ArrayList<String>();
			for(String GN:geneNames)
				{
				StringBuilder sb=new StringBuilder(GN);
				sb.append("|");
				Set<GoTree.Term> t2=super.name2go.get(GN);
				if(t2==null) continue;
				boolean first=true;
				for(GoTree.Term gt:t2)
					{
					if(!first) sb.append("&");
					sb.append(gt.getAcn());
					first=false;
					}
				atts.add(sb.toString());
				}
			if(atts.isEmpty())
				{
				w.add(ctx);
				continue;
				}
			VariantContextBuilder b=new VariantContextBuilder(ctx);

			b.attribute(TAG, atts);
			
			
			
			
			w.add(b.make());
			}
		}
	
	public static void main(String[] args)
		{
		new VcfGeneOntology().instanceMainWithExit(args);
		}
	}
