package com.github.lindenb.jvarkit.util.vcf.predictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import com.github.lindenb.jvarkit.util.so.SequenceOntologyTree;

/**
 * ###INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_po
sition|Protein_position|Amino_acids|Codons|Existing_variation|HGNC|DISTANCE|SIFT|PolyPhen|CELL_TYPE"

 * @author lindenb
 *
 */
public class VepPredictionParser implements PredictionParser
	{
	private static final Logger LOG=Logger.getLogger("jvarkit");

	private enum COLS{
		Allele,
		Gene,
		Feature,
		Feature_type,
		Consequence,
		cDNA_position,
		CDS_position,
		Protein_position,
		Amino_acids,
		Codons,
		Existing_variation,
		HGNC,
		DISTANCE,
		SIFT,
		PolyPhen,
		CELL_TYPE,
		RefSeq,
		EXON,
		STRAND,
		SYMBOL,
		SYMBOL_SOURCE,
		HGNC_ID
		};
	private Map<COLS, Integer> col2col=new HashMap<COLS, Integer>();
	private Pattern pipe=Pattern.compile("[\\|]");
	private String tag;

	
	public VepPredictionParser(VCFHeader header)
		{		
		this(header,getDefaultTag());
		}
	
	@Override
	public String getTag()
		{
		return this.tag;
		}
	
	public static final String getDefaultTag()
		{
		return "CSQ";
		}
	
	public VepPredictionParser(VCFHeader header,String tag)
		{	
		this.tag=(tag==null?getDefaultTag():tag);
		VCFInfoHeaderLine info=header.getInfoHeaderLine(tag);
		if(info==null || info.getDescription()==null)
			{
			LOG.warning("NO "+tag+" found in header");
			return;
			}
		String description=info.getDescription();
		String chunck=" Format:";
		int i=description.indexOf(chunck);
		if(i==-1)
			{
			LOG.warning("Cannot find "+chunck+ " in "+description);
			return;
			}
		description=description.substring(i+chunck.length()).replaceAll("[ \'\\.\\(\\)]+","").trim();
		String tokens[]=pipe.split(description);

		for(i=0;i< tokens.length;++i)
			{
			if(tokens[i].isEmpty()) continue;
			COLS col=null;
			for(COLS c:COLS.values())
				{
				if(c.name().equalsIgnoreCase(tokens[i]))
					{
					col=c;
					}
				}
			if(col==null)
				{
				LOG.warning("Undefined VEP tag "+tokens[i]);
				continue;
				}
			col2col.put(col, i);
			}
		}
	
	
	
	@Override
	public List<VepPrediction> getPredictions(VariantContext ctx)
		{
		ArrayList<VepPrediction> preds= new ArrayList<VepPrediction>();
		if(col2col.isEmpty()) return preds;
		Object o=ctx.getAttribute(this.tag);
		if(o==null)
			{
			return preds;
			}
		
		if(o.getClass().isArray())
			{
			for(Object o2:(Object[])o) _predictions(preds,o2,ctx);
			}
		else if(o instanceof Collection)
			{
			for(Object o2:(Collection<?>)o)  _predictions(preds,o2,ctx);
			}
		else
			{
			_predictions(preds,o,ctx);
			}
		return preds;
		}
	
	private void _predictions( List<VepPrediction> preds,Object o,VariantContext ctx)
		{
		if(o==null) return;
		if(!(o instanceof String))
			{
			_predictions(preds, o.toString(),ctx);
			return;
			}
		String s=String.class.cast(o).trim();
		String tokens[]=pipe.split(s);
		preds.add(new VepPrediction(tokens,ctx));
		}
			
	
	public class VepPrediction
		implements Prediction
		{
		private String tokens[];
		VepPrediction(String tokens[],VariantContext ctx)
			{
			this.tokens=tokens;
			/** special case for ALT, can be '-' */
			Integer idx_allele = col2col.get(COLS.Allele);
			if(	idx_allele!=null && 
				idx_allele<tokens.length &&
				tokens[idx_allele].equals("-"))
				{
				if(ctx.getAlternateAlleles().size()==1)
					{
					this.tokens[idx_allele]=ctx.getAlternateAlleles().get(0).getDisplayString();
					}
				else
					{
					this.tokens[idx_allele]="<indel>";
					}
				}
			}
		private String getByCol(COLS col)
			{
			Integer idx=col2col.get(col);
			if(idx==null || idx>=tokens.length || tokens[idx].isEmpty())
				{
				return null;
				}
			return tokens[idx];
			}
		public Allele getAllele()
			{
			String s= getByCol(COLS.Allele);
			return s==null?null:Allele.create(s, false);
			}
		
		
		public String getExon()
			{
			return getByCol(COLS.EXON);
			}
		
		
		/** return -1 negative strand, 1 position strand , else 0 */
		public int getStrand()
			{
			String s=getByCol(COLS.STRAND);
			if(s==null) return 0;
			if(s.equals("1")) return 1;
			if(s.equals("-1")) return -1;
			return 0;
			}
		
		
		@Override
		public String getGeneName()
			{
			return getByCol(COLS.HGNC);
			}
		
		public String getHGNC()
			{
			return getByCol(COLS.HGNC);
			}
		public String getHgncId()
			{
			return getByCol(COLS.HGNC_ID);
			}
		
		public String getSymbol()
			{
			return getByCol(COLS.SYMBOL);
			}
		
		public String getRefSeq()
			{
			return getByCol(COLS.RefSeq);
			}
		
		public String getGene()
			{
			return getByCol(COLS.Gene);
			}
		
		public String getSymbolSource()
			{
			return getByCol(COLS.SYMBOL_SOURCE);
			}
		
		private Map<COLS,String> getMap()
			{
			Map<COLS, String> hash=new HashMap<COLS,String>();
			for(COLS c: col2col.keySet())
				{
				int idx=col2col.get(c);
				if(idx>=this.tokens.length) continue;
				hash.put(c, tokens[idx]);
				}
			return hash;
			}
		
		@Override
		public String getAltAminoAcid() {
			return null;
			}
		
		@Override
		public Integer getAminoAcidPosition()
			{
			return null;
			}
		@Override
		public String getReferenceAminoAcid() {
			return null;
			}
		
		@Override
		public String getEnsemblGene() {
			String s=getByCol(COLS.Gene);
			if(s==null) return null;
			return s;
			}
		
		@Override
		public String getEnsemblProtein() {
			return null;
			}
		
		public Double getSift() {
			String s=getByCol(COLS.SIFT);
			if(s==null) return null;
			try
				{
				return new Double(s);
				}
			catch(Exception err)
				{
				return null;
				}
			}
		
		public Double getPolyphen() {
			String s=getByCol(COLS.PolyPhen);
			if(s==null) return null;
			try
				{
				return new Double(s);
				}
			catch(Exception err)
				{
				return null;
				}
			}
		
		
		@Override
		public String getEnsemblTranscript() {
			String s=getByCol(COLS.Feature);
			if(s==null) return null;
			if(s.startsWith("ENST")) return s;
			return null;
			}
		
		@Override
		public Set<SequenceOntologyTree.Term> getSOTerms()
			{
			Set<SequenceOntologyTree.Term> set=new HashSet<SequenceOntologyTree.Term>();
			String EFF=getByCol(COLS.Consequence);
			if(EFF==null) return set;
			for(SequenceOntologyTree.Term t:SequenceOntologyTree.getInstance().getTerms())
				{
				for(String eff:EFF.split("[&]"))
					{
					if(t.getLabel().equals(eff))
						{
						set.add(t);
						}
					}
				}
			return set;
			}

		
	@Override
	public String toString() {
		return getMap().toString()+ " "+Arrays.asList(tokens);
		}
	}
		
	
	}
