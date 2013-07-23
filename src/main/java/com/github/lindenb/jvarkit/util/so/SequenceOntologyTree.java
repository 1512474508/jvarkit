package com.github.lindenb.jvarkit.util.so;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**Sequence ontology tree */
public class SequenceOntologyTree
	{
	private static SequenceOntologyTree INSTANCE=null;
	private Map<String,TermImpl> uri2term=new HashMap<String,TermImpl>();
	
	
	public interface Term
		{
		public String getAcn();
		public String getLabel();
		public Set<Term> getParents();
		public Set<Term> getChildren();

		}
	private class TermImpl implements Term
		{
		String accession;
		String name;
		Set<String> parents=new HashSet<String>();
		Set<String> children=new HashSet<String>();
		

		
		public String getAcn() {
			return accession;
			}
		@Override
		public String getLabel() {
			return name;
			}
		
		private Set<Term> convert(Set<String> S1)
			{
			Set<Term> S2=new HashSet<Term>(S1.size());
			for(String s:S1)
				{
				Term t=uri2term.get(s);
				if(t==null) continue;
				S2.add(t);
				}
			return S2;
			}
		
		@Override
		public Set<Term> getChildren()
			{
			return convert(children);
			}
		
		@Override
		public Set<Term> getParents()
			{
			return convert(parents);
			}
		
		@Override
		public int hashCode()
			{
			return accession.hashCode();
			}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TermImpl other = (TermImpl) obj;
			return accession.equals(other.accession);
			}
		@Override
		public String toString() {
			return accession;
			}

		}
	
	
	private void addTerm(String acn,String label,String parent,String children)
		{
		TermImpl t=new TermImpl();
		t.accession=acn;
		t.name=label;
		for(String s:parent.split("[,]"))
			{
			if(s.isEmpty()) continue;
			t.parents.add(s);
			}
		for(String s:children.split("[,]"))
			{
			if(s.isEmpty()) continue;
			t.children.add(s);
			}
		uri2term.put(t.accession, t);
		}
	
	public Term getTermByAcn(String s)
		{
		return this.uri2term.get(s);
		}
	
	private SequenceOntologyTree()
		{
		
		}
	
	public static  SequenceOntologyTree getInstance()
		{
		if(INSTANCE==null)
			{
			synchronized (SequenceOntologyTree.class)
				{
				if(INSTANCE==null)
					{
					/** curl -s "http://www.sequenceontology.org/browser/current_svn/export/complete_term_graph/csv_text/SO:0001060" */

					INSTANCE=new SequenceOntologyTree();
					//INSTANCE.addTerm("Accession","Name","DB_Xrefs","Parents","Children");
					INSTANCE.addTerm("SO:0001628","intergenic_variant","SO:0001878","");
					INSTANCE.addTerm("SO:0001782","TF_binding_site_variant","SO:0001566","");
					INSTANCE.addTerm("SO:0001565","gene_fusion","SO:0001564","");
					INSTANCE.addTerm("SO:0001826","disruptive_inframe_deletion","SO:0001822","");
					INSTANCE.addTerm("SO:0001537","structural_variant","SO:0001060","SO:0001880,SO:0001882,SO:0001879,SO:0001878,SO:0001881,SO:0001563");
					INSTANCE.addTerm("SO:0001591","frame_restoring_variant","SO:0001589","");
					INSTANCE.addTerm("SO:0001610","elongated_polypeptide_C_terminal","SO:0001609","SO:0001612,SO:0001613");
					INSTANCE.addTerm("SO:0001912","copy_number_decrease","SO:0001563","");
					INSTANCE.addTerm("SO:0001060","sequence_variant","","SO:0001536,SO:0001537");
					INSTANCE.addTerm("SO:0001575","splice_donor_variant","SO:0001629","");
					INSTANCE.addTerm("SO:0001541","decreased_transcript_level_variant","SO:0001540","");
					INSTANCE.addTerm("SO:0001583","missense_variant","SO:0001650","SO:0001586,SO:0001585");
					INSTANCE.addTerm("SO:0001560","inactive_ligand_binding_site","SO:0001559","SO:0001618");
					INSTANCE.addTerm("SO:0001552","decreased_transcription_rate_variant","SO:0001550","");
					INSTANCE.addTerm("SO:0001787","splice_donor_5th_base_variant","SO:0001629","");
					INSTANCE.addTerm("SO:0001555","decreased_translational_product_level","SO:0001553","");
					INSTANCE.addTerm("SO:0001818","protein_altering_variant","SO:0001580","SO:0001650,SO:0001589");
					INSTANCE.addTerm("SO:0001791","exon_variant","SO:0001576","SO:0001580,SO:0001792");
					INSTANCE.addTerm("SO:0001550","rate_of_transcription_variant","SO:0001549","SO:0001551,SO:0001552");
					INSTANCE.addTerm("SO:0001894","regulatory_region_ablation","SO:0001879","SO:0001895");
					INSTANCE.addTerm("SO:0001822","inframe_deletion","SO:0001820,SO:0001906","SO:0001826,SO:0001825");
					INSTANCE.addTerm("SO:0001803","decreased_polyadenylation_variant","SO:0001545","");
					INSTANCE.addTerm("SO:0001824","disruptive_inframe_insertion","SO:0001821","");
					INSTANCE.addTerm("SO:0001605","amino_acid_insertion","SO:0001603","");
					INSTANCE.addTerm("SO:0001632","downstream_gene_variant","SO:0001878","SO:0001633");
					INSTANCE.addTerm("SO:0001626","incomplete_terminal_codon_variant","SO:0001650,SO:0001590","");
					INSTANCE.addTerm("SO:0001574","splice_acceptor_variant","SO:0001629","");
					INSTANCE.addTerm("SO:0001585","conservative_missense_variant","SO:0001583","");
					INSTANCE.addTerm("SO:0001895","TFBS_ablation","SO:0001894","");
					INSTANCE.addTerm("SO:0001612","elongated_in_frame_polypeptide_C_terminal","SO:0001610","");
					INSTANCE.addTerm("SO:0001616","polypeptide_fusion","SO:0001603","");
					INSTANCE.addTerm("SO:0001882","feature_fusion","SO:0001537","SO:0001886,SO:0001887,SO:0001890");
					INSTANCE.addTerm("SO:0001547","decreased_transcript_stability_variant","SO:0001546","");
					INSTANCE.addTerm("SO:0001593","minus_2_frameshift_variant","SO:0001589","");
					INSTANCE.addTerm("SO:0001578","stop_lost","SO:0001650,SO:0001907,SO:0001590","");
					INSTANCE.addTerm("SO:0001598","translational_product_structure_variant","SO:0001564","SO:0001602,SO:0001603,SO:0001599");
					INSTANCE.addTerm("SO:0001613","elongated_out_of_frame_polypeptide_C_terminal","SO:0001610","");
					INSTANCE.addTerm("SO:0001878","feature_variant","SO:0001537","SO:0001628,SO:0001564,SO:0001631,SO:0001566,SO:0001632,SO:0001907,SO:0001906,SO:0001017");
					INSTANCE.addTerm("SO:0001562","polypeptide_post_translational_processing_variant","SO:0001554","");
					INSTANCE.addTerm("SO:0001908","internal_feature_elongation","SO:0001907","SO:0001909,SO:0001821");
					INSTANCE.addTerm("SO:0001621","NMD_transcript_variant","SO:0001576","");
					INSTANCE.addTerm("SO:0001587","stop_gained","SO:0001650,SO:0001906","");
					INSTANCE.addTerm("SO:0001615","elongated_out_of_frame_polypeptide_N_terminal","SO:0001611","");
					INSTANCE.addTerm("SO:0001542","increased_transcript_level_variant","SO:0001540","");
					INSTANCE.addTerm("SO:0001636","2KB_upstream_variant","SO:0001635","");
					INSTANCE.addTerm("SO:0001548","increased_transcript_stability_variant","SO:0001546","");
					INSTANCE.addTerm("SO:0001609","elongated_polypeptide","SO:0001603","SO:0001610,SO:0001611");
					INSTANCE.addTerm("SO:0001559","polypeptide_loss_of_function_variant","SO:0001554","SO:0001561,SO:0001560");
					INSTANCE.addTerm("SO:0001557","polypeptide_gain_of_function_variant","SO:0001554","");
					INSTANCE.addTerm("SO:0001546","transcript_stability_variant","SO:0001538","SO:0001547,SO:0001548");
					INSTANCE.addTerm("SO:0001619","nc_transcript_variant","SO:0001576","SO:0001620");
					INSTANCE.addTerm("SO:0001549","transcription_variant","SO:0001538","SO:0001550");
					INSTANCE.addTerm("SO:0001884","regulatory_region_translocation","SO:0001881","SO:0001885");
					INSTANCE.addTerm("SO:0001792","non_coding_exon_variant","SO:0001791","");
					INSTANCE.addTerm("SO:0001544","editing_variant","SO:0001543","");
					INSTANCE.addTerm("SO:0001889","transcript_amplification","SO:0001880","");
					INSTANCE.addTerm("SO:0001880","feature_amplification","SO:0001537","SO:0001891,SO:0001889");
					INSTANCE.addTerm("SO:0001571","cryptic_splice_donor","SO:0001569","");
					INSTANCE.addTerm("SO:0001892","TFBS_amplification","SO:0001891","");
					INSTANCE.addTerm("SO:0001802","increased_polyadenylation_variant","SO:0001545","");
					INSTANCE.addTerm("SO:0001554","polypeptide_function_variant","SO:0001539","SO:0001557,SO:0001562,SO:0001558,SO:0001559");
					INSTANCE.addTerm("SO:0001909","frameshift_elongation","SO:0001589,SO:0001908","");
					INSTANCE.addTerm("SO:0001886","transcript_fusion","SO:0001882","");
					INSTANCE.addTerm("SO:0001595","plus_2_frameshift_variant","SO:0001589","");
					INSTANCE.addTerm("SO:0001539","translational_product_function_variant","SO:0001536","SO:0001553,SO:0001554");
					INSTANCE.addTerm("SO:0001879","feature_ablation","SO:0001537","SO:0001893,SO:0001894");
					INSTANCE.addTerm("SO:0001589","frameshift_variant","SO:0001818","SO:0001594,SO:0001592,SO:0001591,SO:0001909,SO:0001595,SO:0001593,SO:0001910");
					INSTANCE.addTerm("SO:0001906","feature_truncation","SO:0001878","SO:0001587,SO:0001910,SO:0001822");
					INSTANCE.addTerm("SO:0001567","stop_retained_variant","SO:0001819,SO:0001590","");
					INSTANCE.addTerm("SO:0001597","compensatory_transcript_secondary_structure_variant","SO:0001596","");
					INSTANCE.addTerm("SO:0001627","intron_variant","SO:0001576","SO:0001629");
					INSTANCE.addTerm("SO:0001911","copy_number_increase","SO:0001563","");
					INSTANCE.addTerm("SO:0001540","level_of_transcript_variant","SO:0001538","SO:0001542,SO:0001541");
					INSTANCE.addTerm("SO:0001551","increased_transcription_rate_variant","SO:0001550","");
					INSTANCE.addTerm("SO:0001592","minus_1_frameshift_variant","SO:0001589","");
					INSTANCE.addTerm("SO:0001573","intron_gain","SO:0001568","");
					INSTANCE.addTerm("SO:0001604","amino_acid_deletion","SO:0001603","");
					INSTANCE.addTerm("SO:0001611","elongated_polypeptide_N_terminal","SO:0001609","SO:0001614,SO:0001615");
					INSTANCE.addTerm("SO:0001630","splice_region_variant","SO:0001568","");
					INSTANCE.addTerm("SO:0001553","translational_product_level_variant","SO:0001539","SO:0001555,SO:0001556");
					INSTANCE.addTerm("SO:0001576","transcript_variant","SO:0001564","SO:0001621,SO:0001619,SO:0001791,SO:0001577,SO:0001596,SO:0001627,SO:0001622");
					INSTANCE.addTerm("SO:0001819","synonymous_variant","SO:0001580","SO:0001567");
					INSTANCE.addTerm("SO:0001538","transcript_function_variant","SO:0001536","SO:0001543,SO:0001540,SO:0001549,SO:0001546");
					INSTANCE.addTerm("SO:0001590","terminator_codon_variant","SO:0001580","SO:0001626,SO:0001578,SO:0001567");
					INSTANCE.addTerm("SO:0001545","polyadenylation_variant","SO:0001543","SO:0001803,SO:0001802");
					INSTANCE.addTerm("SO:0001601","conformational_change_variant","SO:0001599","");
					INSTANCE.addTerm("SO:0001603","polypeptide_sequence_variant","SO:0001598","SO:0001604,SO:0001617,SO:0001616,SO:0001605,SO:0001609,SO:0001606");
					INSTANCE.addTerm("SO:0001570","cryptic_splice_acceptor","SO:0001569","");
					INSTANCE.addTerm("SO:0001582","initiator_codon_variant","SO:0001650","");
					INSTANCE.addTerm("SO:0001607","conservative_amino_acid_substitution","SO:0001606","");
					INSTANCE.addTerm("SO:0001825","conservative_inframe_deletion","SO:0001822","");
					INSTANCE.addTerm("SO:0001618","inactive_catalytic_site","SO:0001560","");
					INSTANCE.addTerm("SO:0001890","transcript_regulatory_region_fusion","SO:0001882","");
					INSTANCE.addTerm("SO:0001617","polypeptide_truncation","SO:0001603","");
					INSTANCE.addTerm("SO:0001629","splice_site_variant","SO:0001627","SO:0001574,SO:0001575,SO:0001787");
					INSTANCE.addTerm("SO:0001820","inframe_indel","SO:0001650","SO:0001821,SO:0001822");
					INSTANCE.addTerm("SO:0001569","cryptic_splice_site_variant","SO:0001568","SO:0001570,SO:0001571");
					INSTANCE.addTerm("SO:0001017","silent_mutation","SO:0001878","");
					INSTANCE.addTerm("SO:0001561","polypeptide_partial_loss_of_function","SO:0001559","");
					INSTANCE.addTerm("SO:0001580","coding_sequence_variant","SO:0001791","SO:0001819,SO:0001818,SO:0001590");
					INSTANCE.addTerm("SO:0001566","regulatory_region_variant","SO:0001878","SO:0001782");
					INSTANCE.addTerm("SO:0001634","500B_downstream_variant","SO:0001633","");
					INSTANCE.addTerm("SO:0001786","loss_of_heterozygosity","SO:0001536","");
					INSTANCE.addTerm("SO:0001907","feature_elongation","SO:0001878","SO:0001578,SO:0001908");
					INSTANCE.addTerm("SO:0001577","complex_transcript_variant","SO:0001576","");
					INSTANCE.addTerm("SO:0001623","5_prime_UTR_variant","SO:0001622","");
					INSTANCE.addTerm("SO:0001821","inframe_insertion","SO:0001820,SO:0001908","SO:0001823,SO:0001824");
					INSTANCE.addTerm("SO:0001568","splicing_variant","SO:0001564","SO:0001573,SO:0001572,SO:0001569,SO:0001630");
					INSTANCE.addTerm("SO:0001564","gene_variant","SO:0001878","SO:0001576,SO:0001565,SO:0001568,SO:0001598");
					INSTANCE.addTerm("SO:0001883","transcript_translocation","SO:0001881","");
					INSTANCE.addTerm("SO:0001558","polypeptide_localization_variant","SO:0001554","");
					INSTANCE.addTerm("SO:0001622","UTR_variant","SO:0001576","SO:0001623,SO:0001624");
					INSTANCE.addTerm("SO:0001536","functional_variant","SO:0001060","SO:0001786,SO:0001539,SO:0001538");
					INSTANCE.addTerm("SO:0001881","feature_translocation","SO:0001537","SO:0001884,SO:0001883");
					INSTANCE.addTerm("SO:0001586","non_conservative_missense_variant","SO:0001583","");
					INSTANCE.addTerm("SO:0001543","transcript_processing_variant","SO:0001538","SO:0001545,SO:0001544");
					INSTANCE.addTerm("SO:0001823","conservative_inframe_insertion","SO:0001821","");
					INSTANCE.addTerm("SO:0001624","3_prime_UTR_variant","SO:0001622","");
					INSTANCE.addTerm("SO:0001887","regulatory_region_fusion","SO:0001882","SO:0001888");
					INSTANCE.addTerm("SO:0001891","regulatory_region_amplification","SO:0001880","SO:0001892");
					INSTANCE.addTerm("SO:0001599","3D_polypeptide_structure_variant","SO:0001598","SO:0001601,SO:0001600");
					INSTANCE.addTerm("SO:0001608","non_conservative_amino_acid_substitution","SO:0001606","");
					INSTANCE.addTerm("SO:0001888","TFBS_fusion","SO:0001887","");
					INSTANCE.addTerm("SO:0001620","mature_miRNA_variant","SO:0001619","");
					INSTANCE.addTerm("SO:0001633","5KB_downstream_variant","SO:0001632","SO:0001634");
					INSTANCE.addTerm("SO:0001606","amino_acid_substitution","SO:0001603","SO:0001608,SO:0001607");
					INSTANCE.addTerm("SO:0001594","plus_1_frameshift_variant","SO:0001589","");
					INSTANCE.addTerm("SO:0001910","frameshift_truncation","SO:0001589,SO:0001906","");
					INSTANCE.addTerm("SO:0001650","inframe_variant","SO:0001818","SO:0001626,SO:0001583,SO:0001582,SO:0001587,SO:0001820,SO:0001578");
					INSTANCE.addTerm("SO:0001635","5KB_upstream_variant","SO:0001631","SO:0001636");
					INSTANCE.addTerm("SO:0001572","exon_loss","SO:0001568","");
					INSTANCE.addTerm("SO:0001631","upstream_gene_variant","SO:0001878","SO:0001635");
					INSTANCE.addTerm("SO:0001563","copy_number_change","SO:0001537","SO:0001911,SO:0001912");
					INSTANCE.addTerm("SO:0001600","complex_3D_structural_variant","SO:0001599","");
					INSTANCE.addTerm("SO:0001893","transcript_ablation","SO:0001879","");
					INSTANCE.addTerm("SO:0001602","complex_change_of_translational_product_variant","SO:0001598","");
					INSTANCE.addTerm("SO:0001614","elongated_in_frame_polypeptide_N_terminal_elongation","SO:0001611","");
					INSTANCE.addTerm("SO:0001556","increased_translational_product_level","SO:0001553","");
					INSTANCE.addTerm("SO:0001596","transcript_secondary_structure_variant","SO:0001576","SO:0001597");
					INSTANCE.addTerm("SO:0001885","TFBS_translocation","SO:0001884","");
					}
				}
			}
		return INSTANCE;
		}
	}
