package com.github.lindenb.jvarkit.tools.mito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import com.github.lindenb.jvarkit.lang.JvarkitException;
import com.github.lindenb.jvarkit.util.bio.SequenceDictionaryUtils;
import com.github.lindenb.jvarkit.util.bio.fasta.ReferenceFileSupplier;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.log.Logger;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;

public class BamHeteroplasmy extends Launcher {
	private static final Logger LOG = Logger.build(BamHeteroplasmy.class).make();

	private Path faidx=null;
	private int totalexonbases = 70757781;
	private int type = 1;//1=exome, 2=whole genome, 3= RNAseq, 4 = mitochondria only
	private static final char ATGCNatgcn[]=new char[] {'A','T','G','C','N','a','t','g','c','n'};
	private static final int ATGC[]=new int[] {'A','T','G','C'};
	private boolean is_all=false;
	
	
	private  class Pileup {
		final Map<Character,Integer> counter = new HashMap<>(ATGCNatgcn.length);
		Pileup(int pos1,byte ref) {
			for(char base : ATGCNatgcn) {
				counter.put(base, 0);
				}
			}
		void visit(boolean negativeStrand,byte base) {
			char c =  negativeStrand ?
				(char)Character.toLowerCase(base):
				(char)Character.toUpperCase(base);
			this.counter.put(c, counter.getOrDefault(c,0)+1);
			}
		int countIgnoreCase(char ch) {
			return  counter.getOrDefault(Character.toLowerCase(ch),0)+
					counter.getOrDefault(Character.toUpperCase(ch),0);
			}
		List<Character> getMajorMinorAlleles() {
			return Arrays.stream(ATGC).
					mapToObj(C->new AbstractMap.SimpleEntry<Character,Integer>((char)C,countIgnoreCase((char)C))).
					sorted((A,B)->B.getValue().compareTo(A.getValue())).//decreasing
					limit(2L).
					map(K->K.getKey()).
					collect(Collectors.toList());
			}
		
		int getTotalDepth() {
			return Arrays.stream(ATGC).
					map(C->countIgnoreCase((char)C)).
					sum();
			}
		
		
		
		// minor allele / depth
		OptionalDouble getHeteroplasmy() {
			int dp ;
			List<Character> L= getMajorMinorAlleles();
			if(is_all) {
				dp=getTotalDepth();
				}
			else
				{
				dp = countIgnoreCase(L.get(0)) + countIgnoreCase(L.get(1));
				}
			if(dp==0) return OptionalDouble.empty();
			return OptionalDouble.of(countIgnoreCase(L.get(1))/dp);
			}
		
		private double[] getConfidenceInterval(double p,int n) {
		    double  tmp = 1.96 * Math.sqrt( ( p * ( 1 - p ) ) / n );
			return new double[] {
					Math.max(p-tmp, 0.0),
					Math.min(p+tmp, 1.0),
					};
			}
		
		double[] getConfidenceInterval() {
			OptionalDouble het=getHeteroplasmy();
			if(!het.isPresent()) return new double[] {0,0};
			int dp;
			if(is_all) {
				dp=getTotalDepth();
				}
			else
				{
				List<Character> L= getMajorMinorAlleles();
				dp = countIgnoreCase(L.get(0)) + countIgnoreCase(L.get(1));
				}
			return getConfidenceInterval(het.getAsDouble(),dp);
			}
		
		}
	@Override
	public int doWork(List<String> args) {
		try {
			ReferenceSequenceFile referenceSequenceFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(this.faidx);
			final SamReaderFactory srf = super.createSamReaderFactory();
			if(this.faidx!=null) srf.referenceSequence(this.faidx);
			final Path bam1File = Paths.get(args.get(0)); 
			SamReader bam1 = srf.open(bam1File);
			SAMFileHeader header1 = bam1.getFileHeader();
			final SAMSequenceDictionary dict1 = SequenceDictionaryUtils.extractRequired(header1);
			final long totalgenomebases = dict1.getReferenceLength();
			final SAMSequenceRecord ssr1 = dict1.getSequences().stream().filter(SSR->SSR.getSequenceName().matches("(chr)?(M|MT)")).findFirst().orElseThrow(()->new JvarkitException.ContigNotFoundInDictionary("mitochondrial chromosome", dict1));
			final ReferenceSequence chrM = referenceSequenceFile.getSequence(ssr1.getSequenceName());
			
			final Pileup pileups[]=new Pileup[ssr1.getSequenceLength()];
			for(int i=0;i< pileups.length;i++) {
				pileups[i] = new Pileup(i+1,chrM.getBases()[i]);
			}
			
			SAMRecordIterator iter=bam1.queryOverlapping(ssr1.getSequenceName(), 1, ssr1.getSequenceLength());
			while(iter.hasNext()) {
				SAMRecord rec = iter.next();
				if(rec.getReadUnmappedFlag()) continue;
				if(rec.getMappingQuality()< 20) continue;
				if(rec.isSecondaryOrSupplementary()) continue;
				Cigar cigar = rec.getCigar();
				if(cigar==null || cigar.isEmpty()) continue;
				byte bases[]=rec.getReadBases();
				if(bases==SAMRecord.NULL_SEQUENCE) continue;
				int refpos = rec.getUnclippedStart();
				int readpos=0;
				for(final CigarElement ce:cigar) {
					final CigarOperator op =ce.getOperator();
					if(op.consumesReferenceBases()) {
						if(op.consumesReadBases())
							{
							for(int i=0;i< ce.getLength();i++) {
								int pos1 = refpos+i;
								if(pos1<1 || pos1>pileups.length) continue;
								pileups[pos1-1].visit(rec.getReadNegativeStrandFlag(),bases[readpos+i]);
								}
							}
						refpos+=ce.getLength();
						}
					if(op.consumesReadBases()) {
						readpos+=ce.getLength();
						}
					}
				}
			iter.close();
			bam1.close();
			return 0;
		} catch (Exception e) {
			LOG.error(e);
			return -1;
		} finally {
			
			}
		}
}
/**


sub _main{
    print "=" x 50, "\n";
    print "Start analyzing:\n";
    my $index=1;
    _get_mitochondrial_bed( $inbam1, $regionbed );
    
    $ischr=_determine_chr_from_bam($inbam1,$isbam);
    #Assign values to 
    if($type==1 || $type==3){
        $totalbases=$totalexonbases;
        if($ischr){
            $totalbed=$exonbed{'withchr'};
        }else{
            $totalbed=$exonbed{'withoutchr'};
        }
    }else{
        $totalbases=$totalgenomebases;
        if($ischr){
            $totalbed=$genomebed{'withchr'};
        }else{
            $totalbed=$genomebed{'withoutchr'};
        }
    }
    
    if($inbam2){
        _info($index.".1,Extracting reads in mitochondria from '$inbam1' (Output: $mitobam1)");
        
        if($advance){
            _get_mitochondrial_bam_advance($inbam1,$isbam,$regionbed,$mmq,$mitobam1);
        }else{
            _get_mitochondrial_bam( $inbam1, $isbam, $regionbed, $mmq, $mitobam1 );
        }   
        _info($index++.".2,Extracting reads in mitochondria from '$inbam2' (Output: $mitobam2)");
        if($advance){
            _get_mitochondrial_bam_advance( $inbam2, $isbam, $regionbed, $mmq, $mitobam2 );
        }else{
            _get_mitochondrial_bam( $inbam2, $isbam, $regionbed, $mmq, $mitobam2 );
        }
               
        _info($index.".1,Checking Reads number in '$mitobam1'",1);
        _print_read($mitobam1);
         _info($index++.".2,Checking Reads number in '$mitobam2'",1);
         _print_read($mitobam2);
        
        _info($index++.",Moving mitochondrial genome '".$mitogenome{$inref}. "' into $folder (Output:$reference)");
        _move_mitogenome( $mitogenome{$inref}, $regionbed, $reference );
        
        _info($index.".1,Pileuping '$mitobam1' (Output:$mitopileup1)");
        _pileup( $mitobam1, $isbam, $mbq, $regionbed, $reference, $mitopileup1 );
        _info($index++.".2,Pileuping '$mitobam2' (Output:$mitopileup2)");
        _pileup( $mitobam2, $isbam, $mbq, $regionbed, $reference, $mitopileup2 );       
        
        _info($index.".1,Parsing pileup file of '$mitopileup1' (Output: $mitobasecall1)");
        _parse_pileup( $mitopileup1, $mbq, $mitooffset1, $mitobasecall1 );
        _info($index++.".2,Parsing pileup file of '$mitopileup2' (Output: $mitobasecall2)");
        _parse_pileup( $mitopileup2, $mbq, $mitooffset2, $mitobasecall2 );
        
        if($qc){
            _info($index.".1,Getting quality metrics on '$mitobam1' ");
            $mitooffset1 = _mito_qc_stat(
            $mitobam1,                  $isbam,
            $mitopileup1,               $regionbed,
            $perbasequality_figure1,    $mappingquality_figure1,
            $depthdistribution_figure1, $templatelengthdistribution_figure1,
            $perbasequality_table1,     $mappingquality_table1,
            $depthdistribution_table1,  $templatelengthdistribution_table1,
            $percentofbasepairscovered_table1
            );
            _info($index++.".2,Getting quality metrics on '$mitobam2' ");
            $mitooffset2 = _mito_qc_stat(
            $mitobam2,                  $isbam,
            $mitopileup2,               $regionbed,
            $perbasequality_figure2,    $mappingquality_figure2,
            $depthdistribution_figure2, $templatelengthdistribution_figure2,
            $perbasequality_table2,     $mappingquality_table2,
            $depthdistribution_table2,  $templatelengthdistribution_table2,
            $percentofbasepairscovered_table2
            );
        }
        
        _info($index.".1,Detecting heteroplasmy from '$mitobam1' (Output: $mitoheteroplasmy1)");
        _determine_heteroplasmy( $mitobasecall1, $hp, $ha, $isall, $sb,$mitoheteroplasmy1 );
        _info($index++.".2,Detecting heteroplasmy from '$mitobam2' (Output: $mitoheteroplasmy2)");
        _determine_heteroplasmy( $mitobasecall2, $hp, $ha, $isall, $sb,$mitoheteroplasmy2 );
        
        #Plot heteroplasmy ciros plot
        if($producecircosplot){
            $circos->build($outref);
            $circos->circosoutput($mitocircosheteroplasmyfigure1);
            $circos->configoutput($mitocircosheteroplasmyconfig1);
            $circos->datafile($mitoheteroplasmy1);
            $circos->textoutput($mitoheteroplasmytextoutput1);
            $circos->scatteroutput($mitoheteroplasmyscatteroutput1);
            $circos->cwd(getcwd()."/circos");
            $circos->prepare("heteroplasmy");
            $circos->plot();
            
            #For mito2
            $circos->circosoutput($mitocircosheteroplasmyfigure2);
            $circos->configoutput($mitocircosheteroplasmyconfig2);
            $circos->datafile($mitoheteroplasmy2);
            $circos->textoutput($mitoheteroplasmytextoutput2);
            $circos->scatteroutput($mitoheteroplasmyscatteroutput2);
            $circos->prepare("heteroplasmy");
            $circos->plot();
            
        }
        
        _info($index.".1,Detecting structure variants from '$mitobam1' (Output: $mitostructure1 | $mitostructuredeletion1)");
        _structure_variants( $inbam1, $isbam, $mmq, $regionbed, $str,$strflagmentsize,$mitostructure1,$mitostructuredeletion1);
        _info($index++.".2,Detecting structure variants from '$mitobam2' (Output: $mitostructure2 | $mitostructuredeletion2)");
        _structure_variants( $inbam2, $isbam, $mmq, $regionbed, $str,$strflagmentsize,$mitostructure2,$mitostructuredeletion2);
        
        _info($index++.",Detecting somatic mutations (Output: $mitosomatic)");
         _determine_somatic( $mitobasecall1, $mitobasecall2, $sp, $sa, $isall,$mitosomatic );
         
         if($cs){
            $circos->build($outref);
            $circos->changeconfig("somatic");
            $circos->circosoutput($mitocircossomaticfigure);
            $circos->configoutput($mitocircossomaticconfig);
            $circos->datafile($mitosomatic);
            $circos->textoutput($mitosomatictextoutput);
            $circos->cwd(getcwd()."/circos");
            $circos->prepare("somatic");
            $circos->plot();
         }
         
          if($cn && $type!=4){
            _info($index++.",Estimating relative copy number of '$mitobam1' (Output: $mitocnv1)");
            _wrap_mito_cnv($mitobam1,$inbam1,$mitobases,$totalbases,$isbam,$mbq,$mmq,$totalbed,$mitodepth1,$sampledepthi,$mitocnv1);
            _info($index++.",Estimating relative copy number of '$mitobam2' (Output: $mitocnv2)");
            _wrap_mito_cnv($mitobam2,$inbam2,$mitobases,$totalbases,$isbam,$mbq,$mmq,$totalbed,$mitodepth2,$sampledephtj,$mitocnv2);
         }
        

    }else{
        _info($index++.",Extracting reads in mitochondria from '$inbam1' (Output: $mitobam1)");
        if($advance){
            _get_mitochondrial_bam_advance( $inbam1, $isbam, $regionbed, $mmq, $mitobam1 );
        }else{
            _get_mitochondrial_bam( $inbam1, $isbam, $regionbed, $mmq, $mitobam1 );
        }
        
        _info($index++.",Checking Reads number in '$mitobam1'",1);
        _print_read($mitobam1);
        
        _info($index++.",Moving mitochondrial genome '".$mitogenome{$inref}. "' into $folder (Output:$reference)");
        _move_mitogenome( $mitogenome{$inref}, $regionbed, $reference );
        
        _info($index++.",Pileuping '$mitobam1' (Output:$mitopileup1)");
        _pileup( $mitobam1, $isbam, $mbq, $regionbed, $reference, $mitopileup1 );
        
        _info($index++.",Parsing pileup file of '$mitopileup1' (Output: $mitobasecall1)");
        _parse_pileup( $mitopileup1, $mbq, $mitooffset1, $mitobasecall1 );
        if($qc){
            _info($index++.",Getting quality metrics on '$mitobam1' ");
            $mitooffset1 = _mito_qc_stat(
            $mitobam1,                  $isbam,
            $mitopileup1,               $regionbed,
            $perbasequality_figure1,    $mappingquality_figure1,
            $depthdistribution_figure1, $templatelengthdistribution_figure1,
            $perbasequality_table1,     $mappingquality_table1,
            $depthdistribution_table1,  $templatelengthdistribution_table1,
            $percentofbasepairscovered_table1
            );
        }
        _info($index++.",Detecting heteroplasmy from '$mitobam1' (Output: $mitoheteroplasmy1)");
        _determine_heteroplasmy( $mitobasecall1, $hp, $ha, $isall, $sb,$mitoheteroplasmy1 );
        if($producecircosplot){
            $circos->build($outref);
            $circos->circosoutput($mitocircosheteroplasmyfigure1);
            $circos->configoutput($mitocircosheteroplasmyconfig1);
            $circos->datafile($mitoheteroplasmy1);
            $circos->textoutput($mitoheteroplasmytextoutput1);
            $circos->scatteroutput($mitoheteroplasmyscatteroutput1);
            $circos->cwd(getcwd()."/circos");
            $circos->prepare("heteroplasmy");
            $circos->plot();
         }
        
        _info($index++.",Detecting structure variants from '$mitobam1' (Output: $mitostructure1 | $mitostructuredeletion1)");
        _structure_variants( $inbam1, $isbam, $mmq, $regionbed, $str,$strflagmentsize,$mitostructure1,$mitostructuredeletion1); 
    
         if($cn && $type!=4){
            _info($index++.",Estimating relative copy number of '$mitobam1' (Output: $mitocnv1)");
            
            _wrap_mito_cnv($mitobam1,$inbam1,$mitobases,$totalbases,$isbam,$mbq,$mmq,$totalbed,$mitodepth1,$sampledepthi,$mitocnv1);
         }
    }
    _info($index++.",Generating report (Output: $mitoreport)");
    print "=" x 50, "\n";
    
}

*/