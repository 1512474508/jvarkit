/**
 * Author:
 * 	Pierre Lindenbaum PhD
 * Date:
 * 	Fev-2014
 * Contact:
 * 	plindenbaum@yahoo.fr
 * Motivation:
 * 	Idea from Solena: successive synonymous mutations are a stop codong
 */
package com.github.lindenb.jvarkit.tools.vcfannot;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.lang.DelegateCharSequence;
import com.github.lindenb.jvarkit.util.bio.AcidNucleics;
import com.github.lindenb.jvarkit.util.bio.GeneticCode;
import com.github.lindenb.jvarkit.util.picard.AbstractDataCodec;
import com.github.lindenb.jvarkit.util.picard.GenomicSequence;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.ucsc.KnownGene;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.SortingCollection;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;



/**
 * VCFStopCodon
 * @SolenaLS 's idea: consecutive synonymous bases give a stop codon
 *
 */
public class VCFStopCodon extends AbstractVCFStopCodon
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(VCFStopCodon.class);
	/** known Gene collection */
	private final IntervalTreeMap<KnownGene> knownGenes=new IntervalTreeMap<KnownGene>();
	/** reference genome */
	private IndexedFastaSequenceFile indexedFastaSequenceFile=null;
	/** current genomic sequence */
	private GenomicSequence genomicSequence=null;
	/** all variants */
	private SortingCollection<Variant> variants= null;
	/** genetic Code used */
	private static final GeneticCode GENETIC_CODE = GeneticCode.getStandard();
	
	/** mutated cdna */
	private static class MutedSequence extends DelegateCharSequence
		{
		private int begin=-1;
		private int end=-1;
		private String newseq=null;
	
		MutedSequence(CharSequence wild)
			{
			super(wild);
			}
		
		void setMutation(int begin,int end,final String newseq)
			{
			if(this.newseq!=null) throw new IllegalStateException();
			this.newseq = newseq;
			this.begin=begin;
			this.end=end;
			if(this.begin>this.end) throw new IllegalArgumentException();
			if(this.end> getDelegate().length()) throw new IndexOutOfBoundsException();
			}
		@Override
		public int length() {
			int L = getDelegate().length();
			if(this.newseq!=null) {
				L-=(this.end-this.begin);
				L+=this.newseq.length();
				}
			return L;
			}
		@Override
		public char charAt(int index)
			{
			if(this.newseq==null || this.begin<index) return getDelegate().charAt(index);
			int idx2= index-this.begin;
			if(idx2 < this.newseq.length())
				{
				return this.newseq.charAt(idx2);
				}
			idx2-=this.newseq.length();
			return getDelegate().charAt(this.end+idx2);
			}
		
		}
	
	
	private static class ProteinCharSequence extends DelegateCharSequence
		{
		ProteinCharSequence(final CharSequence cDNA)
			{
			super(cDNA);
			}
		
		@Override
		public char charAt(int i)
			{
			return GENETIC_CODE.translate(
				getDelegate().charAt(i*3+0),
				getDelegate().charAt(i*3+1),
				getDelegate().charAt(i*3+2));
			}	
		
		@Override
		public int length()
			{
			return getDelegate().length()/3;
			}
		
		}
	
	/** load KnownGenes */
	private void loadKnownGenesFromUri() throws IOException
		{
		BufferedReader in = null;
		try {
			final SAMSequenceDictionary dict=this.indexedFastaSequenceFile.getSequenceDictionary();
	        if(dict==null) throw new IOException("dictionary missing");

			LOG.info("loading genes from "+this.kgURI);
			in =IOUtils.openURIForBufferedReading(this.kgURI);
			final Pattern tab=Pattern.compile("[\t]");
			String line = null;
			while((line=in.readLine())!=null)
				{
				line= line.trim();
				if(line.isEmpty()) continue;
				String tokens[]=tab.split(line);
				final KnownGene g=new KnownGene(tokens);
				if(g.isNonCoding()) continue;
				if(dict.getSequence(g.getContig())==null)
					{
					LOG.warn("Unknown chromosome "+g.getContig()+" in dictionary");
					continue;
					}
				//use 1 based interval
				final Interval interval=new Interval(g.getContig(), g.getTxStart()-1, g.getTxEnd());
				this.knownGenes.put(interval, g);
				}
			CloserUtil.close(in);in=null;
			LOG.info("genes:"+knownGenes.size());
			}
		finally
			{
			CloserUtil.close(in);
			}
		}
	
	
	
	private static int ID_GENERATOR=0;
	
	static private class AbstractContext {
		String contig;
		int genomicPosition1=0;
		Allele refAllele;
		Allele altAllele;
		int sorting_id;
		}
	
	static private class Variant extends AbstractContext
		{
		String transcriptName;
		int position_in_cdna=-1;
		String wildCodon=null;
		String mutCodon=null;
		
		Variant()
			{
			
			}
		
		Variant(final VariantContext ctx,final Allele allele,final KnownGene gene) {
			this.contig = ctx.getContig();
			this.genomicPosition1=ctx.getStart();
			this.transcriptName = gene.getName();
			this.refAllele = ctx.getReference();
			this.altAllele = allele;
			}
		int positionInCodon() { return  position_in_cdna%3;}
		int codonStart() { return this.position_in_cdna - this.positionInCodon();}
		
		public String getInfo(final Variant other) {
			return String.join("|",
					// common data
					"CHROM",this.contig,
					"POS",String.valueOf(genomicPosition1),
					"REF",this.refAllele.getBaseString(),
					"TRANSCRIPT",this.transcriptName,
					"cDdnaPos",String.valueOf(this.position_in_cdna+1),
					"CodonPos",String.valueOf(this.codonStart()+1),
					"CodonWild",this.wildCodon,
					"AAWild",new ProteinCharSequence(this.wildCodon).getString(),
					
					//data about this
					"PosInCodon1",String.valueOf(this.positionInCodon()+1),
					"Alt1",this.altAllele.getBaseString(),
					"Codon1",this.mutCodon,
					"AA1",new ProteinCharSequence(this.mutCodon).getString(),
					
					//data about the other
					"PosInCodon2",String.valueOf(other.positionInCodon()+1),
					"Alt2",other.altAllele.getBaseString(),
					"Codon2",other.mutCodon,
					"AA2",new ProteinCharSequence(other.mutCodon).getString()
					);
		}
		
		@Override
		public String toString() {
			return contig+"\t"+genomicPosition1+"\t"+refAllele.getBaseString()+"\t"+
					altAllele.getBaseString()+"\t"+
					transcriptName+"\t"+
					position_in_cdna+"\t"+
					codonStart()+"\t"+
					positionInCodon()+"\t"+
					wildCodon+"\t"+
					mutCodon
					;
			}
		
		}
	static private class VariantCodec extends AbstractDataCodec<Variant>
		{
		@Override
		public Variant decode(DataInputStream dis) throws IOException {
			String contig;
			try {
				contig = dis.readUTF();
			} catch (Exception e) {
				return null;
				}
			final Variant variant = new Variant();
			variant.contig = contig;
			variant.genomicPosition1 = dis.readInt();
			variant.transcriptName = dis.readUTF();
			variant.refAllele = Allele.create(dis.readUTF(), true);
			variant.altAllele = Allele.create(dis.readUTF(), false);
			variant.position_in_cdna = dis.readInt();
			variant.wildCodon = dis.readUTF();
			variant.mutCodon = dis.readUTF();
			variant.sorting_id = dis.readInt();
			return variant;
		}

		@Override
		public void encode(DataOutputStream dos, Variant v) throws IOException {
			dos.writeUTF(v.contig);
			dos.writeInt(v.genomicPosition1);
			dos.writeUTF(v.transcriptName);
			dos.writeUTF(v.refAllele.getBaseString());
			dos.writeUTF(v.altAllele.getBaseString());
			dos.writeInt(v.position_in_cdna);
			dos.writeUTF(v.wildCodon);
			dos.writeUTF(v.mutCodon);
			dos.writeInt(v.sorting_id);
		}

		@Override
		public VariantCodec clone() {
			return new VariantCodec();
		}
		
		}
	static private class VariantComparator implements Comparator<Variant>
		{
		SAMSequenceDictionary dict;
		VariantComparator(SAMSequenceDictionary dict) {
			this.dict = dict;
		}
		int contig(final Variant v) { return dict.getSequenceIndex(v.contig);}
		@Override
		public int compare(Variant o1, Variant o2) {
			int i= contig(o1) - contig(o2);
			if(i!=0) return i;
			i= o1.transcriptName.compareTo(o2.transcriptName);
			if(i!=0) return i;
			i= o1.position_in_cdna-o2.position_in_cdna;
			if(i!=0) return i;
			return o1.sorting_id - o2.sorting_id;
			}
		}
	
	
	private static class Mutation extends AbstractContext
		{
		String info=null;
		}

	static private class MutationCodec extends AbstractDataCodec<Mutation>
		{
		@Override
		public Mutation decode(DataInputStream dis) throws IOException {
			String contig;
			try {
				contig = dis.readUTF();
			} catch (Exception e) {
				return null;
				}
			final Mutation variant = new Mutation();
			variant.contig = contig;
			variant.genomicPosition1 = dis.readInt();
			variant.refAllele = Allele.create(dis.readUTF(), true);
			variant.altAllele = Allele.create(dis.readUTF(), false);
			variant.info = dis.readUTF();
			variant.sorting_id = dis.readInt();
			return variant;
		}
	
		@Override
		public void encode(final DataOutputStream dos, final Mutation v) throws IOException {
			dos.writeUTF(v.contig);
			dos.writeInt(v.genomicPosition1);
			dos.writeUTF(v.refAllele.getBaseString());
			dos.writeUTF(v.altAllele.getBaseString());
			dos.writeUTF(v.info);
			dos.writeInt(v.sorting_id);
		}
	
		@Override
		public MutationCodec clone() {
			return new MutationCodec();
		}
		
		}
	
static private class MutationComparator implements Comparator<Mutation>
	{
	final SAMSequenceDictionary dict;
	MutationComparator(SAMSequenceDictionary dict) {
		this.dict = dict;
	}
	int contig(final Mutation v) { return dict.getSequenceIndex(v.contig);}
	@Override
	public int compare(Mutation o1, Mutation o2) {
		int i= contig(o1) - contig(o2);
		if(i!=0) return i;
		i= o1.genomicPosition1-o2.genomicPosition1;
		if(i!=0) return i;
		i =  o1.refAllele.compareTo(o2.refAllele);
		if(i!=0) return i;
		return o1.sorting_id - o2.sorting_id;
		}
	}

	@Override
	protected Collection<Throwable> doVcfToVcf(final String inputName,final VcfIterator r,final VariantContextWriter w) throws IOException {
			SortingCollection<Mutation> mutations = null;
			CloseableIterator<Variant> varIter = null;
			CloseableIterator<Mutation> mutIter = null;
			try {
			LOG.info("opening REF:"+referenceFile);
			
			this.indexedFastaSequenceFile=new IndexedFastaSequenceFile(this.referenceFile);
	        final SAMSequenceDictionary dict=this.indexedFastaSequenceFile.getSequenceDictionary();
	        if(dict==null) throw new IOException("dictionary missing");
			loadKnownGenesFromUri();
			
			this.variants = SortingCollection.newInstance(Variant.class,
					new VariantCodec(),
					new VariantComparator(dict),
					super.getMaxRecordsInRam(),
					super.getTmpDirectories()
					);
			this.variants.setDestructiveIteration(true);
			
			
			final VCFHeader header=(VCFHeader)r.getHeader();
			
	       SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(header);
			while(r.hasNext())
				{
				final VariantContext ctx= progress.watch(r.next());
				/* discard non SNV variant */
				if(!ctx.isSNP() ||  !ctx.isVariant())
					{
					continue;
					}
				
				final Collection<KnownGene> genes= this.knownGenes.getOverlapping(
						new Interval(ctx.getContig(),ctx.getStart(),ctx.getEnd())
						); 
				for(final KnownGene kg:genes) {
					for(final Allele alt: ctx.getAlternateAlleles()) {
						challenge(ctx,alt,kg);
						}
					}
				}
			progress.finish();
			this.variants.doneAdding();
			
			mutations = SortingCollection.newInstance(Mutation.class,
					new MutationCodec(),
					new MutationComparator(dict),
					super.getMaxRecordsInRam(),
					super.getTmpDirectories()
					);
			mutations.setDestructiveIteration(true);
			
			
			
			varIter = this.variants.iterator();
			final ArrayList<Variant> buffer= new ArrayList<>();
			for(;;)
				{
				Variant variant = null;
				if(varIter.hasNext())
					{
					variant = varIter.next();
					}
				if(variant==null || !(!buffer.isEmpty() && buffer.get(0).contig.equals(variant.contig) &&  buffer.get(0).transcriptName.equals(variant.transcriptName)))
					{
					if(!buffer.isEmpty()) {
					for(int i=0;i< buffer.size();++i)
						{
						final Variant v1  = buffer.get(i);
						for(int j=i+1;j< buffer.size();++j)
							{
							final Variant v2  = buffer.get(j);
							if(v1.codonStart() != v2.codonStart()) continue;
							if(v1.positionInCodon() == v2.positionInCodon()) continue;
							if(!v1.wildCodon.equals(v2.wildCodon))
								{
								throw new IllegalStateException();
								}
							
							final StringBuilder combinedCodon = new StringBuilder(v1.wildCodon);
							combinedCodon.setCharAt(v1.positionInCodon(), v1.mutCodon.charAt(v1.positionInCodon()));
							combinedCodon.setCharAt(v2.positionInCodon(), v2.mutCodon.charAt(v2.positionInCodon()));
							
							final String pwild = new ProteinCharSequence(v1.wildCodon).getString();
							final String p1 = new ProteinCharSequence(v1.mutCodon).getString();
							final String p2 = new ProteinCharSequence(v2.mutCodon).getString();
							final String pCombined = new ProteinCharSequence(combinedCodon).getString();
							if(
								(!pCombined.equals(pwild) && p1.equals(pwild) && p2.equals(pwild)) ||//new variant created
								(!pCombined.equals(p1) && !pCombined.equals(p2) && !pCombined.equals(pwild)) //new amino acid (higher consequence ?)
								) {
								final String infoSuffix="|"+String.join("|", "CombinedCodon",combinedCodon,"ComibinedAA",pCombined);
								
								final Mutation m1 = new Mutation();
								m1.contig = v1.contig;
								m1.genomicPosition1 = v1.genomicPosition1;
								m1.refAllele = v1.refAllele;
								m1.altAllele = v1.altAllele;
								m1.info = v1.getInfo(v2)+ infoSuffix;
								m1.sorting_id = ID_GENERATOR++;
								mutations.add(m1);
								
								final Mutation m2 = new Mutation();
								m2.contig = v2.contig;
								m2.genomicPosition1 = v2.genomicPosition1;
								m2.refAllele = v2.refAllele;
								m2.altAllele = v2.altAllele;
								m2.info = v2.getInfo(v1) + infoSuffix;
								m2.sorting_id = ID_GENERATOR++;
								mutations.add(m2);
							}
							
							}
						}
					}
					buffer.clear();
					if(variant==null) break;
					}
				buffer.add(variant);
				}
			
			mutations.doneAdding();
			varIter.close();varIter=null;
			variants.cleanup();variants=null;
			final ArrayList<Mutation> mBuffer= new ArrayList<>();
			
			final VCFHeader header2 = new VCFHeader();
			header.setSequenceDictionary(header.getSequenceDictionary());
			final VCFInfoHeaderLine infoHeaderLine = new VCFInfoHeaderLine("CODONVARIANT",VCFHeaderLineCount.UNBOUNDED,VCFHeaderLineType.String,
					"todo");
			super.addMetaData(header2);
			header2.addMetaDataLine(infoHeaderLine);
			w.writeHeader(header2);
			
			mutIter = mutations.iterator();
			for(;;)
				{
				Mutation variant = null;
				if(mutIter.hasNext())
					{
					variant = mutIter.next();
					}
				if(variant==null || !(!mBuffer.isEmpty() && mBuffer.get(0).contig.equals(variant.contig) &&  mBuffer.get(0).refAllele.equals(variant.refAllele)))
					{
					if(!mBuffer.isEmpty()) {
					final Mutation first  = mBuffer.get(0);
					final Set<Allele> alleles = new HashSet<>();
					final Set<String> info = new HashSet<>();
					alleles.add(first.refAllele);
					final VariantContextBuilder vcb=new VariantContextBuilder();
					vcb.chr(first.contig);
					vcb.start(first.genomicPosition1);
					vcb.stop(first.genomicPosition1 + first.refAllele.length()-1);
					for(final Mutation m:mBuffer){
						alleles.add(m.altAllele);
					}
					vcb.attribute(infoHeaderLine.getID(), new ArrayList<String>(info));
					w.add(vcb.make());
					}
					mBuffer.clear();
					if(variant==null) break;
					}
				mBuffer.add(variant);
				}
			mutIter.close();
			mutations.cleanup();mutations=null;
			
			
			return RETURN_OK;
			}
		catch(Exception err)
			{
			return wrapException(err);
			}
		finally
			{
			CloserUtil.close(this.indexedFastaSequenceFile);
			CloserUtil.close(mutIter);
			CloserUtil.close(varIter);
			if(this.variants!=null) this.variants.cleanup();
			if(mutations!=null) mutations.cleanup();
			this.variants=null;
			}
		
		}
	
	private void challenge(
			final VariantContext ctx,
			final Allele allele,
			final KnownGene gene
			) throws IOException
		{
		if(genomicSequence==null || !genomicSequence.getChrom().equals(ctx.getContig()))
			{
			LOG.info("getting genomic Sequence for "+gene.getContig());
			genomicSequence=new GenomicSequence(this.indexedFastaSequenceFile, gene.getContig());
			}
		final int positionContext0 = ctx.getStart() -1;
		int cdna_length = 3;//get cDNA length including stop codon: so, we're starting from 3
		for(int exon_index=0;exon_index< gene.getExonCount();++exon_index)
			{
			final KnownGene.Exon exon= gene.getExon(exon_index);
			final int eStart = Math.max(exon.getStart(),gene.getCdsStart());
			final int eEnd =   Math.min(exon.getEnd(),gene.getCdsEnd());
			cdna_length += (eStart < eEnd ? eEnd-eStart : 0);
			}
		
		Variant variant=null;
    		
		final StringBuilder wildRNA=new StringBuilder(cdna_length);
		final MutedSequence mutRNA=new MutedSequence(wildRNA);
    	
		if(gene.isPositiveStrand())
			{
	    	for(int exon_index=0;exon_index< gene.getExonCount();++exon_index)
	    		{
	    		final KnownGene.Exon exon= gene.getExon(exon_index);
	    		for(int i= exon.getStart();
						i< exon.getEnd() && wildRNA.length()< cdna_length;
						++i)
					{
					if(i<  gene.getCdsStart()) continue;
					//if(i>= gene.getCdsEnd()) break; no, we"re using cdna_length

					wildRNA.append(genomicSequence.charAt(i));
					
					if(variant==null && positionContext0 ==i)
						{
						variant = new Variant(ctx,allele,gene);
						variant.sorting_id = ID_GENERATOR++;
						variant.position_in_cdna=wildRNA.length()-1;
						char mutBase= allele.getBaseString().charAt(0);
						mutRNA.setMutation( wildRNA.length()-1, wildRNA.length(),""+mutBase 	);
						}
					}
	    		}
			}
		else
			{
			int exon_index = gene.getExonCount()-1;
			while(exon_index >=0)
				{
				final KnownGene.Exon exon= gene.getExon(exon_index);
				
				for(int i= exon.getEnd()-1;
					    i>= exon.getStart() && wildRNA.length()< cdna_length;
					--i)
					{
					if(i>= gene.getCdsEnd()) continue;
					//if(i<  gene.getCdsStart()) break; no we're using cdna_length;
	
					wildRNA.append(AcidNucleics.complement(genomicSequence.charAt(i)));
					
					if(variant==null && positionContext0 ==i)
						{
						variant = new Variant(ctx,allele,gene);
						variant.sorting_id = ID_GENERATOR++;
						variant.position_in_cdna=wildRNA.length()-1;
						char mutBase=AcidNucleics.complement(allele.getBaseString().charAt(0));
						mutRNA.setMutation( wildRNA.length()-1, wildRNA.length(),""+mutBase 	);
						}
    				}
    			--exon_index;
    			}
			}
        	
    	if(variant!=null)
    		{
    		variant.wildCodon="";
    		variant.mutCodon="";
    		for(int i=0;i< 3;++i)
    			{
    			int pos = variant.codonStart()+i;
    			variant.wildCodon += (pos< wildRNA.length()?wildRNA.charAt(pos):'*');
    			variant.mutCodon +=  (pos< mutRNA.length()?mutRNA.charAt(pos):'*');
    			}
    		variant.wildCodon = variant.wildCodon.toUpperCase();
    		variant.mutCodon = variant.mutCodon.toUpperCase();
    		
    		if(variant.wildCodon.equals(variant.mutCodon)) {
    			LOG.info("Uh???????");
    			return;
    			}
    		this.variants.add(variant);
    		}
		}
	
	@Override
	protected Collection<Throwable> call(String inputName) throws Exception {
		if(this.referenceFile==null)
			{
			return wrapException("Undefined REFERENCE. option: -"+OPTION_REFERENCEFILE);
			}
		if(this.kgURI==null || this.kgURI.trim().isEmpty())
			{
			return wrapException("Undefined kgURI. option: -"+OPTION_KGURI);
			}
		return doVcfToVcf(inputName);
		}
	
	public static void main(String[] args)
		{
		new VCFStopCodon().instanceMainWithExit(args);
		}
	}