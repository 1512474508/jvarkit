JVARKIT
=======

Java utilities for Next Generation Sequencing

Pierre Lindenbaum PhD

http://plindenbaum.blogspot.com

@yokofakun	

## Download and install

see [Download and Install](https://github.com/lindenb/jvarkit/wiki/Compilation)

<h2>Tools</h2>
I'm slowly moving the pages below to the github wiki  ( https://github.com/lindenb/jvarkit/wiki/_pages )
<table>
<tr><th>Tool</th><th>Description</th></tr>
<tr><th>[IlluminaDir](https://github.com/lindenb/jvarkit/wiki/Illuminadir)</th><td>Create a structured (**JSON** or **XML**) representation of a directory containing some Illumina 
FASTQs.</td></tr>
<tr><th>[SamJS](https://github.com/lindenb/jvarkit/wiki/SamJS)</th><td>Filtering a SAM/BAM with javascript (rhino).</td></tr>
<tr><th>[VCFFilterJS](https://github.com/lindenb/jvarkit/wiki/VCFFilterJS)</th><td>Filtering a VCF with javascript (rhino)</td></tr>
</table>



<h3>SAM4WebLogo</h3>
<h4>Motivation</h4>
"Sequence logo ( http://weblogo.berkeley.edu/logo.cgi ) for different alleles or generated from SAM/BAM" http://www.biostars.org/p/73021

![ScreenShot](https://raw.github.com/lindenb/jvarkit/master/doc/sam2weblogo.png)


<h4>Compilation</h4>

```bash
ant sam4weblogo
```

<h4>Options</h4>

* INPUT=File                        A BAM file to process.  Required. 
* REGION=String Region to observe: chrom:start-end  Required.

<h4>Example</h4>
```bash
$ java -jar dist/sam4weblogo.jar I=path/to/samtools-0.1.18/examples/sorted.bam R=seq1:80-110 2> /dev/null | head -n 50
>B7_593:4:106:316:452/1
TGTTG--------------------------
>B7_593:4:106:316:452a/1
TGTTG--------------------------
>B7_593:4:106:316:452b/1
TGTTG--------------------------
>B7_589:8:113:968:19/2
TGGGG--------------------------
>B7_589:8:113:968:19a/2
TGGGG--------------------------
>B7_589:8:113:968:19b/2
TGGGG--------------------------
>EAS54_65:3:321:311:983/1
TGTGGG-------------------------
>EAS54_65:3:321:311:983a/1
TGTGGG-------------------------
>EAS54_65:3:321:311:983b/1
TGTGGG-------------------------
>B7_591:6:155:12:674/2
TGTGGGGG-----------------------
>B7_591:6:155:12:674a/2
TGTGGGGG-----------------------
>B7_591:6:155:12:674b/2
TGTGGGGG-----------------------
>EAS219_FC30151:7:51:1429:1043/2
TGTGGGGGGCGCCG-----------------
>EAS219_FC30151:7:51:1429:1043a/2
TGTGGGGGGCGCCG-----------------
>EAS219_FC30151:7:51:1429:1043b/2
TGTGGGGGGCGCCG-----------------
>B7_591:5:42:540:501/1
TGTGGGGGCCGCAGTG---------------
>EAS192_3:5:223:142:410/1
TGGGGGGGGCGCAGT----------------
>B7_591:5:42:540:501a/1
TGTGGGGGCCGCAGTG---------------
>EAS192_3:5:223:142:410a/1
TGGGGGGGGCGCAGT----------------
>B7_591:5:42:540:501b/1
TGTGGGGGCCGCAGTG---------------
>EAS192_3:5:223:142:410b/1
TGGGGGGGGCGCAGT----------------
```

<br/>
<h3>SAM2Tsv</h3>
display a tabular view of each base of the reads vs the reference.
<h4>Compilation</h4>
```bash
ant sam2tsv
```
<h4>Options</h4>
<ul>
<li>IN=File BAM files to process.  This option may be specified 0 or more times.</li>
<li>REGION=String restrict to that region (chr:start-end)  Default value: null. </li>
<li>REF=File Indexed reference  Required. </li>
<li>A={true,false} Use Alignment format.</li>
</ul>
<h4>Example</h4>
```bash
java -jar dist/sam2tsv.jar \
	I=sorted.bam \
	R=genome.fa \
	L=chr1:32944435-32944435


M00491:12:000000000-A3FL3:1:1101:16929:4287	147	1	A	20	chr22	544289	A	M	=
M00491:12:000000000-A3FL3:1:1101:16929:4287	147	2	G	28	chr22	544290	G	M	=
M00491:12:000000000-A3FL3:1:1101:16929:4287	147	3	A	32	chr22	544291	C	M	X
M00491:12:000000000-A3FL3:1:1101:16929:4287	147	4	T	37	chr22	544292	T	M	=
M00491:12:000000000-A3FL3:1:1101:16929:4287	147	5	C	36	chr22	544293	C	M	=
```

<br/>
<h3>cmpbam: Comparing two or more BAMS</h3>

<h4>Compilation</h4>
```bash
ant cmpbams
```

<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM files to process.  This option must be specified at least 2 times. </td></tr>
<tr><td>REGION=String</td><td>restrict to that region (chr:start-end)  Default value: null. </td></tr>
<tr><td>USESAMFLAG=Boolean</td><td>use SAM Flag when comparing.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>
<h4>Example</h4>
```bash
java -jar dist/cmpbams.ja \
	I=file1.bam \
	I=file2.bam \
	I=file3.bam \
	L=chr1:32944435-32944435
```

<br/>
<h3>Bam2Raster</h3>
(under development)
<h4>Motivation</h4>
Save a BAM alignment as a PNG image.
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM files to process.  Required. </td></tr>
<tr><td>OUT=File</td><td>Image name.  Required. </td></tr>
<tr><td>REGION=String</td><td>restrict to that region (chr:start-end)  Default value: null. </td></tr>
<tr><td>REF=File</td><td>Indexex reference  Required. </td></tr>
<tr><td>WIDTH=Integer</td><td>image width  Default value: 1000. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>NAME=Boolean</td><td>print read name.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr> 
<tr><td>BASE=Boolean</td><td>print base.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>
<h4>Compilation</h4>
```bash
ant bam2raster
```
<h4>Example</h4>
```bash
java -jar dist/bam2raster.jar \
	IN=sorted.bam L=seq1:200-300 \
	OUT=ouput.png \
	R=ex1.fa
```
![ScreenShot](https://raw.github.com/lindenb/jvarkit/master/doc/bam2graphics.png)

<br/>
<h3>VCF2SQL</h3>
<h4>Motivation</h4>
Generate the SQL code to insert a VCF into sqlite3.
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>VCF files to process.  This option may be specified 0 or more times. </td></tr>
<tr><td>SUFFIX=String</td><td>Table suffix  Default value: (empty).</td></tr>
<tr><td>USE_VEP=Boolean</td><td>Use  and explode VEP predictions  Default value: true. Possible values: {true, false} </td></tr>
<tr><td>USE_SNPEFF=Boolean</td><td>Use and explode SNPEFF predictions  Default value: true. Possible values: {true, false} </td></tr>
<tr><td>ENGINE=String</td><td>sql engine [sqlite,hsql]  Default value: sqlite. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>SPLIT4=Boolean</td><td>Split DP4  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>
<h4>Example</h4>
```bash
java -jar dist/vcf2sql.jar I=file.vcf | sqlite3 db.sqlite 
```
<h4>Compilation</h4>
```bash
ant vcf2sql
```
<h4>Schema</h4>
![Schema](https://chart.googleapis.com/chart?cht=gv&chl=digraph{HEADER->FILE;VARIATION->FILE;ALT->VARIATION;FILTER->VARIATION;INFO->VARIATION;SAMPLE->GENOTYPE;GENOTYPE->VARIATION;GTPROP->GENOTYPE;EXTRAINFO->INFO;EXTRAINFOPROP->EXTRAINFO;})

<h3>SortVCFOnRef</h3>
<h4>Motivation</h4>
Sort a VCF using the reference order. Version: 1.0
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>VCF file (or stdin).  Default value: null. </td></tr>
<tr><td>OUT=File</td><td>output file (or stdout).  Default value: null. </td></tr>
<tr><td>REF=File</td><td>Reference file.  Required. </td></tr>
</table>
<h4>Example</h4>
```bash
java -jar dist/sortvcfonref.jar I=in.vcf O=out.vcf REF=ref.fa
```
<h4>Compilation</h4>
```bash
ant sortvcfonref
```

<br/>


<br/>
<h3>Bam4DeseqIntervals</h3>
creates a table for DESEQ with the number of reads within a sliding window for multiple BAMS.Version: 1.0
<h4>Compilation</h4>
```bash
ant bam4deseq01
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM file to process  This option must be specified at least 1 times. </td></tr>
<tr><td>OUT=File</td><td>output filename. Default stdout. </td></tr>
<tr><td>WINDOW_SIZE=Integer</td><td>size of the observed window.   Default value: 500. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>WINDOW_SHIFT=Integer</td><td>shift window by SHIFT pb   Default value: 250. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>ONLY_COVERED=Boolean</td><td>ignore regions with NO coverage  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>HEADER=Boolean</td><td>print header  Default value: true. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>

<br/>
<h3>VCFStripAnnotations</h3>
<h4>Motivation</h4>
Removes one or more field from the INFO column of a VCF.Version
<h4>Compilation</h4>
```bash
ant vcfstripannot
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>VCF file to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
<tr><td>KEY=String</td><td>remove this INFO key  This option may be specified 0 or more times. </td></tr>
<tr><td>RESET_FILTER=Boolean</td><td>Reset the FILTER column  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>
<br/>
<h4>Example</h4>
```bash
$ java -jar dist/vcfstripannot.jar K=DP4 K=AC1 < in.vcf > out.vcf 
```

<h3>VCFFixIndels</h3>
<h4>Motivation</h4>
Fix samtools INDELS for @SolenaLS
<h4>Compilation</h4>
```bash
ant vcffixindels
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>VCF file to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>
<br/>
<h4>Example</h4>
```bash
$ java -jar dist/vcffixindels.jar I=input.vcf.gz | grep FIX

2	749862	.	T	TT	94.50	.	AC1=2;AF1=1;DP=13;DP4=0,0,5,8;FQ=-73.5;INDEL;INDELFIXED=CTTTTTT|CTTTTTTT|74954856;IS=13,1.000000;MQ=50;VDB=4.019414e-04	GT:PL:DP:GQ	1/1:135,39,0:13:75


```

<h3>BlastMapAnnots</h3>
<h4>Motivation</h4>
Maps uniprot/genbank annotations on a blast result. See http://www.biostars.org/p/76056
<h4>Compilation</h4>
This tools call ${JAVA_HOME}/bin/xjc. If you're working being a proxy, you might have to edit the build.xml file to add the -httpproxy option.
```bash
ant blastmapannots
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>XML sequence file Genbank.xml or uniprot.xml.  Required. </td></tr>
<tr><td>BLAST=File</td><td>BLAST XML output (or stdin).  Default value: null. </td></tr>
<tr><td>APPEND_ACN=Boolean</td><td>append the sequence accession before the feature name.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>INCL=String</td><td>Restrict to uniprot/feature/type of genbank/feature/key.  This option may be specified 0 or more times. </td></tr>
<tr><td>EXCL=String</td><td>Exclude uniprot/feature/type of genbank/feature/key.  This option may be specified 0 or more times. </td></tr>
</table>
<br/>
<h4>Example</h4>
Download uniprot P04514  ( Rotavirus Non-structural protein 3 )  as <b>XML</b>
```bash
$ curl -o P04514.xml "http://www.uniprot.org/uniprot/P04514.xml"
```
Download the same P04514 as <b>fasta</b>
```bash
$ curl -o P04514.fasta "http://www.uniprot.org/uniprot/P04514.fasta"
```

<b>TblastN</b> P04514.fasta vs a RNA of NSP3 in genbank http://www.ncbi.nlm.nih.gov/nuccore/AY065842.1 and save the ouput as XML:
```xml
<?xml version="1.0"?>
<!DOCTYPE BlastOutput PUBLIC "-//NCBI//NCBI BlastOutput/EN" "http://www.ncbi.nlm.nih.gov/dtd/NCBI_BlastOutput.dtd">
<BlastOutput>
  <BlastOutput_program>tblastn</BlastOutput_program>
(...)
<Hit>
  <Hit_num>1</Hit_num>
  <Hit_id>gi|18139606|gb|AY065842.1|</Hit_id>
  <Hit_def>Rhesus rotavirus nonstructural protein 3 (NSP3) gene, complete cds</Hit_def>
  <Hit_accession>AY065842</Hit_accession>
  <Hit_len>1078</Hit_len>
  <Hit_hsps>
    <Hsp>
      <Hsp_bit-score>546.584</Hsp_bit-score>
      <Hsp_score>1407</Hsp_score>
      <Hsp_evalue>0</Hsp_evalue>
      <Hsp_query-from>1</Hsp_query-from>
      <Hsp_query-to>313</Hsp_query-to>
      <Hsp_hit-from>26</Hsp_hit-from>
      <Hsp_hit-to>964</Hsp_hit-to> <Hsp_qseq>MLKMESTQQMASSIINTSFEAAVVAATSTLELMGIQYDYNEIYTRVKSKFDYVMDDSGVKNNLLGKAATIDQALNGKFGSVMRNKNWMTDSRTVAKLDEDVNKLRMMLSSKGIDQKMRVLNACFSVKRIPGKSSSVIKCTRLMKDKIERGAVEVDDSFVEEKMEVDTVDWKSRYDQLERRFESLKQRVNEKYTTWVQKAKKVNENMYSLQNVISQQQNQIADLQNYCSKLEADLQNKVGSLVSSVEWYLKSMELPDEVKTDIEQQLNSIDTISPINAIDDLEILIRNLIHDYDRTFLMFKGLLRQCNYEYAYE</Hsp_qseq>
      <Hsp_hseq>MLKMESTQQMASSIINSSFEAAVVAATSTLELMGIQYDYNEVYTRVKSKFDLVMDDSGVKNNLIGKAITIDQALNGKFSSAIRNRNWMTDSRTVAKLDEDVNKLRIMLSSKGIDQKMRVLNACFSVKRIPGKSSSIVKCTRLMKDKLERGEVEVDDSFVEEKMEVDTIDWKSRYEQLEKRFESLKHRVNEKYNHWVLKARKVNENMNSLQNVISQQQAHINELQMYNNKLERDLQSKIGSVVSSIEWYLRSMELSDDVKSDIEQQLNSIDQLNPVNAIDDFESILRNLISDYDRLFIMFKGLLQQCNYTYTYE</Hsp_hseq>
      <Hsp_midline>MLKMESTQQMASSIIN SFEAAVVAATSTLELMGIQYDYNE YTRVKSKFD VMDDSGVKNNL GKA TIDQALNGKF S  RN NWMTDSRTVAKLDEDVNKLR MLSSKGIDQKMRVLNACFSVKRIPGKSSS  KCTRLMKDK ERG VEVDDSFVEEKMEVDT DWKSRY QLE RFESLK RVNEKY  WV KA KVNENM SLQNVISQQQ  I  LQ Y  KLE DLQ K GS VSS EWYL SMEL D VK DIEQQLNSID   P NAIDD E   RNLI DYDR F MFKGLL QCNY Y YE</Hsp_midline>
    </Hsp>
  </Hit_hsps>
</Hit>
(...)
</Iteration>
</BlastOutput_iterations>
</BlastOutput>
```
Now produce a BED file with this blast result to map the features of P04514 to AY065842.

```bash
$ java -jar dist/blastmapannots.jar I=P04514.xml B=blast.xml

AY065842	25	961	Non-structural_protein_3	943	+	25961	255,255,255	1	936	25
AY065842	34	469	RNA-binding	970	+	34	469	255,255,255	1	435	34
AY065842	472	640	Dimerization	947	+	472	640	255,255,255	1	168	472
AY065842	532	724	Interaction_with_ZC3H7B	917	+	532	724	255,255,255	1	192	532
AY065842	646	961	Interaction_with_EIF4G1	905	+	646	961	255,255,255	1	315	646
AY065842	520	733	coiled-coil_region	916	+	520	733	255,255,255	1	213	520
```


<h3>VcfViewGui</h3>
<h4>Motivation</h4>
Simple java-Swing-based VCF viewer.
![ScreenShot](https://raw.github.com/lindenb/jvarkit/master/doc/vcfview.png)
<h4>Compilation</h4>
```bash
ant vcfviewgui
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>VCF files to process.  This option may be specified 0 or more times. </td></tr>
<tr><td>IGV_HOST=String</td><td>IGV host. example: '127.0.0.1'   Default value: null. </td></tr>
<tr><td>IGV_PORT=Integer</td><td>IGV IP. example: '60151'   Default value: null. </td></tr>
</table>

<h3>VCFGeneOntology</h3>
<h4>Motivation</h4>
Finds the GO terms for VCF annotated with SNPEFF or VEP
<h4>Compilation</h4>
```bash
ant vcfgo
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>GOA=String</td><td>GOA file/URI.  Default value: http://cvsweb.geneontology.org/cgi-bin/cvsweb.cgi/go/gene-associations/gene_association.goa_human.gz?rev=HEAD.</td></tr>
<tr><td>GO=String</td><td>GOA file/URI.  Default value: http://archive.geneontology.org/latest-termdb/go_daily-termdb.rdf-xml.gz.</td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout. </td></tr>
</table>
<h4>Example</h4>
```bash
$ java -jar dist/vcfgo.jar I="https://raw.github.com/arq5x/gemini/master/test/tes.snpeff.vcf" |\
	grep -v -E '^##' | head -n 3

#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	1094PC0005	1094PC0009	1094PC0012	1094PC0013
chr1	30860	.	G	C	33.46	.	AC=2;AF=0.053;AN=38;BaseQRankSum=2.327;DP=49;Dels=0.00;EFF=DOWNSTREAM(MODIFIER||||85|FAM138A|protein_coding|CODING|ENST00000417324|),DOWNSTREAM(MODIFIER|||||FAM138A|processed_transcript|CODING|ENST00000461467|),DOWNSTREAM(MODIFIER|||||MIR1302-10|miRNA|NON_CODING|ENST00000408384|),INTRON(MODIFIER|||||MIR1302-10|antisense|NON_CODING|ENST00000469289|),INTRON(MODIFIER|||||MIR1302-10|antisense|NON_CODING|ENST00000473358|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000423562|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000430492|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000438504|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000488147|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000538476|);FS=3.128;HRun=0;HaplotypeScore=0.6718;InbreedingCoeff=0.1005;MQ=36.55;MQ0=0;MQRankSum=0.217;QD=16.73;ReadPosRankSum=2.017	GT:AD:DP:GQ:PL	0/0:7,0:7:15.04:0,15,177	0/0:2,0:2:3.01:0,3,39	0/0:6,0:6:12.02:0,12,143	0/0:4,0:4:9.03:0,9,119
chr1	69270	.	A	G	2694.18	.	AC=40;AF=1.000;AN=40;DP=83;Dels=0.00;EFF=SYNONYMOUS_CODING(LOW|SILENT|tcA/tcG|S60|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=0.000;GOA=OR4F5|GO:0004984&GO:0005886&GO:0004930&GO:0016021;HRun=0;HaplotypeScore=0.0000;InbreedingCoeff=-0.0598;MQ=31.06;MQ0=0;QD=32.86	GT:AD:DP:GQ:PL	./.	./.	1/1:0,3:3:9.03:106,9,0	1/1:0,6:6:18.05:203,18,0
```

<h3>VCFFilter GO</h3>
<h4>Motivation</h4>
Set the <b>VCF FILTERs</b> on VCF files annotated with SNPEFF or VCP testing wether a Gene belong or not to the descendants of a GO term. 
<h4>Compilation</h4>
```bash
ant vcffiltergo
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>CHILD_OF=String</td><td>list of GO accessions for gene having a GO-term children of the user output.  This option may be specified 0 or more times. </td></tr>
<tr><td>FILTER=String</td><td>Filter name.  Default value: GO. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>GOA=String</td><td>GOA file/URI.  Default value: http://cvsweb.geneontology.org/cgi-bin/cvsweb.cgi/go/gene-associations/gene_association.goa_human.gz?rev=HEAD. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>GO=String</td><td>GOA file/URI.  Default value: http://archive.geneontology.org/latest-termdb/go_daily-termdb.rdf-xml.gz. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout. </td></tr>
</table>
<h4>Example</h4>
```bash
$  java -jar dist/vcffiltergo.jar I="https://raw.github.com/arq5x/gemini/master/test/test1.snpeff.vcf"  \
	CHILD_OF=GO:0005886 FILTER=MEMBRANE  |\
	grep -v "^##"   | head -n 3

#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	1094PC0005	1094PC0009	1094PC0012	1094PC0013
chr1	30860	.	G	C	33.46	PASS	AC=2;AF=0.053;AN=38;BaseQRankSum=2.327;DP=49;Dels=0.00;EFF=DOWNSTREAM(MODIFIER||||85|FAM138A|protein_coding|CODING|ENST00000417324|),DOWNSTREAM(MODIFIER|||||FAM138A|processed_transcript|CODING|ENST00000461467|),DOWNSTREAM(MODIFIER|||||MIR1302-10|miRNA|NON_CODING|ENST00000408384|),INTRON(MODIFIER|||||MIR1302-10|antisense|NON_CODING|ENST00000469289|),INTRON(MODIFIER|||||MIR1302-10|antisense|NON_CODING|ENST00000473358|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000423562|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000430492|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000438504|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000488147|),UPSTREAM(MODIFIER|||||WASH7P|unprocessed_pseudogene|NON_CODING|ENST00000538476|);FS=3.128;HRun=0;HaplotypeScore=0.6718;InbreedingCoeff=0.1005;MQ=36.55;MQ0=0;MQRankSum=0.217;QD=16.73;ReadPosRankSum=2.017	GT:AD:DP:GQ:PL	0/0:7,0:7:15.04:0,15,177	0/0:2,0:2:3.01:0,3,39	0/0:6,0:6:12.02:0,12,143	0/0:4,0:4:9.03:0,9,119
chr1	69270	.	A	G	2694.18	MEMBRANE	AC=40;AF=1.000;AN=40;DP=83;Dels=0.00;EFF=SYNONYMOUS_CODING(LOW|SILENT|tcA/tcG|S60|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=0.000;HRun=0;HaplotypeScore=0.0000;InbreedingCoeff=-0.0598;MQ=31.06;MQ0=0;QD=32.86	GT:AD:DP:GQ:PL	./.	./.	1/1:0,3:3:9.03:106,9,0	1/1:0,6:6:18.05:203,18,0

```

<h3>VCFBed</h3>
<h4>Motivation</h4>
Annotate a VCF with the content of a BED file indexed with tabix
<h4>Compilation</h4>
```bash
ant vcfbed
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>FORMAT=String</td><td>format. Field with ${number} will be replaced with the column of the BED.  Default value: ${1}:${2}-${3}. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>TABIXFILE=String</td><td>BED file indexed with tabix  Required. </td></tr>
<tr><td>TAG=String</td><td>Key for the INFO field  Default value: TAG. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>

<h4>Example</h4>
Map the NCBI biosystems to a BED file using the following script:    <a href="https://gist.github.com/6024788">https://gist.github.com/6024788</a> .

```bash
$ gunzip -c ~/ncbibiosystem.bed.gz | head
1	69091	70008	79501	106356	30	Signaling_by_GPCR
1	69091	70008	79501	106383	50	Olfactory_Signaling_Pathway
1	69091	70008	79501	119548	40	GPCR_downstream_signaling
1	69091	70008	79501	477114	30	Signal_Transduction
1	69091	70008	79501	498	40	Olfactory_transduction
1	69091	70008	79501	83087	60	Olfactory_transduction
1	367640	368634	26683	106356	30	Signaling_by_GPCR
1	367640	368634	26683	106383	50	Olfactory_Signaling_Pathway
1	367640	368634	26683	119548	40	GPCR_downstream_signaling
1	367640	368634	26683	477114	30	Signal_Transduction
```

Now, annotate a remote VCF with the data of NCBI biosystems.
```bash
curl "https://raw.github.com/arq5x/gemini/master/test/test1.snpeff.vcf" |\
 sed 's/^chr//' |\
 java -jar  dist/vcfbed.jar TABIXFILE=~/ncbibiosystem.bed.gz TAG=NCBIBIOSYS  FMT='($4|$5|$6|$7)' |\
 grep -E '(^#CHR|NCBI)'

##INFO=<ID=NCBIBIOSYS,Number=.,Type=String,Description="metadata added from /home/lindenb/ncbibiosystem.bed.gz . Format was ($4|$5|$6|$7)">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	1094PC0005	1094PC0009	1094PC0012	1094PC0013
1	69270	.	A	G	2694.18	.	AC=40;AF=1.000;AN=40;DP=83;Dels=0.00;EFF=SYNONYMOUS_CODING(LOW|SILENT|tcA/tcG|S60|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=0.000;HRun=0;HaplotypeScore=0.0000;InbreedingCoeff=-0.0598;MQ=31.06;MQ0=0;NCBIBIOSYS=(79501|119548|40|GPCR_downstream_signaling),(79501|106356|30|Signaling_by_GPCR),(79501|498|40|Olfactory_transduction),(79501|83087|60|Olfactory_transduction),(79501|477114|30|Signal_Transduction),(79501|106383|50|Olfactory_Signaling_Pathway);QD=32.86	GT:AD:DP:GQ:PL	./.	./.	1/1:0,3:3:9.03:106,9,0	1/1:0,6:6:18.05:203,18,0
1	69511	.	A	G	77777.27	.	AC=49;AF=0.875;AN=56;BaseQRankSum=0.150;DP=2816;DS;Dels=0.00;EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|Aca/Gca|T141A|305|OR4F5|protein_coding|CODING|ENST00000335137|exon_1_69091_70008);FS=21.286;HRun=0;HaplotypeScore=3.8956;InbreedingCoeff=0.0604;MQ=32.32;MQ0=0;MQRankSum=1.653;NCBIBIOSYS=(79501|119548|40|GPCR_downstream_signaling),(79501|106356|30|Signaling_by_GPCR),(79501|498|40|Olfactory_transduction),(79501|83087|60|Olfactory_transduction),(79501|477114|30|Signal_Transduction),(79501|106383|50|Olfactory_Signaling_Pathway);QD=27.68;ReadPosRankSum=2.261	GT:AD:DP:GQ:PL	./.	./.	0/1:2,4:6:15.70:16,0,40	0/1:2,2:4:21.59:22,0,40
```
<h3>Biostar76892</h3>
<h4>Motivation</h4>
Fix strand of two paired reads close but on the same strand http://www.biostars.org/p/76892/
<h4>Compilation</h4>
```bash
ant biostar76892
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>ONLYSAVEFIXED=Boolean</td><td>only save pairs of reads fixed.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>DISTANCE=Integer</td><td>distance beween two reads.  Default value: 500. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>IN=File</td><td>BAM file to process.  Required. </td></tr>
<tr><td>OUT=File</td><td>BAM file fixed.  Required. </td></tr>
</table>
<h4>Example</h4>

Before fixing:
```bash
samtools view src.bam | grep "HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906"
HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906        177     3       1264832 37      101M    =       1264940 109     AGGTGGTGAAGCATGAGATGTAGGGAGAGCTGCTTTAAAACCCAGCACAAGGCTGGTTGTACTGGCTCACACCTGTAATCCCAGGTCTTTGGGAGGCTGAG    """#""""#"""#"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""#""""""#""##"""""""#"#"   X0:i:1  X1:i:0  BD:Z:ABBACCABBABAAABABAABAACBBABAA@AAABAABBAABB@AAAAAAABA@ABAA@BAA@@BAAAAAAAA@@BAAAABA@ABAAABAACBBACBAABAA       MD:Z:0C0C0C1C0C0G1G0T1T0T0G0C0C0T0T0C0T0G0T0A0C0A2T2C0A0T0G0C1T0G0T0G2G0T0T0T0G1G0T1T1C0C0A0A0G0T0G0C0G0A1T0G1G0C0T1T0T2C0G0T0G0T0G1C1C0A1G1G0T1C0C1T0T2C0A0     RG:Z:idp63088   XG:i:0  BI:Z:ABBAEDCCCBCBBABABAAAA@CBBAB@A@@A@AAAAAAABA@@AAA@A@BA@@BAA@A@@@@BAA@A@@@A@@AAAAABA@@BAAABBACBBACBBABAA       AM:i:37 NM:i:75 SM:i:37 XM:i:1  XO:i:0  MQ:i:37 XT:A:U
HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906        113     3       1264940 37      101M    =       1264832 -109    GGTATCTCCATGCTCGAAGCCCTGACCTACTGTATTGCCCCGAAAGTCTTCCCTGCTGTGGCTGCATCTTTTCCACGTGGATAATCTTGGTTCATCTCTAG    """##"""""""""""""""""""""""""#"""""""""""""""""""""""""""#"""""""""""""""#""""""""""""#""#""##"#"##"   X0:i:1  X1:i:0  BD:Z:BBAABBBBAAABBBCBAABCBA@BAAAAAAABAAAAACCCBABAAAAAAACBAAAAABABA@AA@AAABBAAAAACB@BBAAAAAAAABBBAABBBBAAAA       MD:Z:0T0T1G0C0A1G0T1C0C0A1G0T0G0C0A0T0G0T0G0T0G2A0T4G0C0A0A0T0G0T0G0C1G0G0T0G1C0A0G0T0T0G0C0A4C1A0T0G0C0G0T0G2G0G1C0G0T0G0A1C0G0T0G1G0C2T2T0C0G0T0G0T0A0T1       RG:Z:idp63088   XG:i:0  BI:Z:BABADDCCBBBCBBCBAABCBA@AABAAA@AAAAA@BBBBBAAA@AA@AABA@@A@@A@BA@@A@AA@AAAAAAABB@BAAAAAAAA@CBAAABBBBAAAA       AM:i:37 NM:i:74 SM:i:37 XM:i:0  XO:i:0  MQ:i:37 XT:A:U
```
Fixing:
```bash
 java -jar dist/biostar76892.jar ONLYSAVEFIXED=true \
 	IN=src.bam \
 	OUT=fix.bam  \
 	VALIDATION_STRINGENCY=LENIENT
```
result

```bash
samtools view fix.bam | grep "HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906"
HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906        163     3       1264832 37      101M    =       1264940 109     AGGTGGTGAAGCATGAGATGTAGGGAGAGCTGCTTTAAAACCCAGCACAAGGCTGGTTGTACTGGCTCACACCTGTAATCCCAGGTCTTTGGGAGGCTGAG    """#""""#"""#"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""#""""""#""##"""""""#"#"   X0:i:1  X1:i:0  BD:Z:ABBACCABBABAAABABAABAACBBABAA@AAABAABBAABB@AAAAAAABA@ABAA@BAA@@BAAAAAAAA@@BAAAABA@ABAAABAACBBACBAABAA       MD:Z:0C0C0C1C0C0G1G0T1T0T0G0C0C0T0T0C0T0G0T0A0C0A2T2C0A0T0G0C1T0G0T0G2G0T0T0T0G1G0T1T1C0C0A0A0G0T0G0C0G0A1T0G1G0C0T1T0T2C0G0T0G0T0G1C1C0A1G1G0T1C0C1T0T2C0A0     RG:Z:idp63088   XG:i:0  BI:Z:ABBAEDCCCBCBBABABAAAA@CBBAB@A@@A@AAAAAAABA@@AAA@A@BA@@BAA@A@@@@BAA@A@@@A@@AAAAABA@@BAAABBACBBACBBABAA       AM:i:37 NM:i:75 SM:i:37 XM:i:1  XO:i:0  MQ:i:37 XT:A:U  rv:i:1
HWI-1KL149:6:C0KTCACXX:5:2107:2283:35906        83      3       1264940 37      101M    =       1264832 -109    GGTATCTCCATGCTCGAAGCCCTGACCTACTGTATTGCCCCGAAAGTCTTCCCTGCTGTGGCTGCATCTTTTCCACGTGGATAATCTTGGTTCATCTCTAG    """##"""""""""""""""""""""""""#"""""""""""""""""""""""""""#"""""""""""""""#""""""""""""#""#""##"#"##"   X0:i:1  X1:i:0  BD:Z:BBAABBBBAAABBBCBAABCBA@BAAAAAAABAAAAACCCBABAAAAAAACBAAAAABABA@AA@AAABBAAAAACB@BBAAAAAAAABBBAABBBBAAAA       MD:Z:0T0T1G0C0A1G0T1C0C0A1G0T0G0C0A0T0G0T0G0T0G2A0T4G0C0A0A0T0G0T0G0C1G0G0T0G1C0A0G0T0T0G0C0A4C1A0T0G0C0G0T0G2G0G1C0G0T0G0A1C0G0T0G1G0C2T2T0C0G0T0G0T0A0T1       RG:Z:idp63088   XG:i:0  BI:Z:BABADDCCBBBCBBCBAABCBA@AABAAA@AAAAA@BBBBBAAA@AA@AABA@@A@@A@BA@@A@AA@AAAAAAABB@BAAAAAAAA@CBAAABBBBAAAA       AM:i:37 NM:i:74 SM:i:37 XM:i:0  XO:i:0  MQ:i:37 XT:A:U  rv:i:1
```
<h3>FixVCF</h3>
<h4>Motivation</h4>
Fix a VCF HEADER when I forget to declare a FILTER or an INFO field in the HEADER
<h4>Compilation</h4>
```bash
ant fixvcf
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.</td></tr>
</table>

<h3>VCFTrio</h3>
<h4>Motivation</h4>
Check for mendelian incompatibilities in a VCF
<h4>Compilation</h4>
```bash
ant vcftrio
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>PEDIGREE=File</td><td> Pedigree file (plink format) Required. </td></tr>
<tr><td>FILTER=Boolean</td><td>Set filter 'MENDEL' if incompatibilities found.  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>

<h3>MapUniProtFeatures</h3>
<h4>Motivation</h4>
map uniprot features on reference genome.

<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>REF=File</td><td>Reference  Required. </td></tr>
<tr><td>OUT=File</td><td>output name (default: stdout)  Required. </td></tr>
<tr><td>KGURI=String</td><td>KnownGene data  Default value: http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>UNIPROT=String</td><td>Uniprot URL/File  Default value: ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz. This option can be set to 'null' to clear the default value. </td></tr>
</table>
<h4>Example</h4>
```bash
$ java  -jar dist/mapuniprot.jar \
	REF=/path/to/human_g1k_v37.fasta \
	UNIPROT=/path/uri/uniprot.org/uniprot_sprot.xml.gz  \
	kgUri=<(curl -s "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz" | gunzip -c | awk -F '        ' '{if($2 ~ ".*_.*") next; OFS="       "; gsub(/chr/,"",$2);print;}'   ) |\
	LC_ALL=C sort -t '	' -k1,1 -k2,2n -k3,3n  | uniq | head


1	69090	69144	topological_domain	1000	+	69090	69144	255,0,0	1	54	0
1	69144	69216	transmembrane_region	1000	+	69144	69216	255,0,0	1	72	0
1	69216	69240	topological_domain	1000	+	69216	69240	255,0,0	1	24	0
1	69240	69306	transmembrane_region	1000	+	69240	69306	255,0,0	1	66	0
1	69306	69369	topological_domain	1000	+	69306	69369	255,0,0	1	63	0
1	69357	69636	disulfide_bond	1000	+	69357	69636	255,0,0	1	279	0
1	69369	69429	transmembrane_region	1000	+	69369	69429	255,0,0	1	60	0
1	69429	69486	topological_domain	1000	+	69429	69486	255,0,0	1	57	0
1	69486	69543	transmembrane_region	1000	+	69486	69543	255,0,0	1	57	0
1	69543	69654	topological_domain	1000	+	69543	69654	255,0,0	1	111	0
```
<h3>ExtendBed</h3>
<h4>Motivation</h4>
Extends a BED file by 'X' bases.
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=String</td><td>BED Input URI/file. default: stdin  Default value: null. </td></tr>
<tr><td>REF=File</td><td>Reference  Required. </td></tr>
<tr><td>OUT=File</td><td>output name (default: stdout)</td></tr>
<tr><td>EXTEND=Integer</td><td>extend by 'X' bases.  Default value: 0. This option can be set to 'null' to clear the default value. </td></tr>
</table>
<h4>Example</h4>
```bash
head test.bed |\
	java -jar dist/extendbed.jar \
	X=100 REF=human_g1k_v37.fa
```
<h3>Biostar77288</h3>
<h4>Motivation</h4>
Low resolution sequence alignment visualization http://www.biostars.org/p/77288/
<h4>Compilation</h4>
```bash
ant biostar77288
 ```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=String</td><td>MSA file/URI (default:stdin)</td></tr>
<tr><td>ALN_WIDTH=Integer</td><td>Alignment width  Default value: 1000. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>SEQLOGO=Boolean</td><td>Input is seqLogo (see https://github.com/lindenb/jvarkit#sam4weblogo)  Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>
<h4>Example</h4>
```bash
curl -s "http://www.tcoffee.org/Courses/Exercises/saragosa_pb_2010/practicals/practical_2/ex.1.19/file/clustalw.msa" |\
	java -jar dist/biostar77288.jar  > result.svg
```
![ScreenShot](https://raw.github.com/lindenb/jvarkit/master/doc/biostar77288.png)
```bash
$ java -jar dist/sam4weblogo.jar IN=in.bam   REGION="1:630-719" |\
	java -jar dist/biostar77288.jar  SEQLOGO=true > result.svg
```

<h3>VCFAnnotator</h3>
<h4>Motivation</h4>
 Basic Variant Effect prediction . First described in http://plindenbaum.blogspot.fr/2011/01/my-tool-to-annotate-vcf-files.html
<h4>Compilation</h4>
```bash
ant vcfpredictions
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>REF=File</td><td>indexed Fasta genome REFERENCE.  Required. </td></tr>
<tr><td>KGURI=String</td><td>KnownGene data URI/File. should look like http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz . Beware chromosome names are formatted the same as your REFERENCE.  Default value: http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>
<h4>Example</h4>
```bash
java -jar  dist/vcfpredictions.jar  \
	REF=human_g1k_v37.fasta \
	kgUri=<(curl  "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz" | gunzip -c | awk -F '   ' '{if($2 ~ ".*_.*") next; OFS="        "; gsub(/chr/,"",$2);print;}'  ) \
	I=~/WORK/variations.gatk.annotations.vcf.gz 
```


<h3>VCFBigWig</h3>
<h4>Motivation</h4>
Annotate a VCF with the data of a bigwig file.
<h4>Compilation</h4>
Compiling requires the bigwig library http://code.google.com/p/bigwig/

```bash

$ more build.properties 
(...)
bigwig.dir=/path/to/bigwig
(...)

$ ant vcfbigwig
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>BW=File</td><td> Path to the bigwig file. The chromosome must have the same names than in the VCF. Required.</td></tr>
<tr><td>INFOTAG=String</td><td>name of the INFO tag. default: name of the bigwig.  Default value: null</td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>
<h4>Example</h4>
```bash
 java -jar dist/vcfbigwig.jar \
 	IN=input.vcf.gz \
 	INFOTAG=GERP \
 	BW=gerp.bw
	
##INFO=<ID=GERP,Number=1,Type=Float,Description="Values from bigwig file: com.github.lindenb.jvarkit.tools.vcfbigwig.VCFBigWig BIGWIG=gerp.bw TAG=GERP IN=input.vcf.gz    VERBOSITY=INFO QUIET=false VALIDATION_STRINGENCY=STRICT COMPRESSION_LEVEL=5 MAX_RECORDS_IN_RAM=500000 CREATE_INDEX=false CREATE_MD5_FILE=false">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO(...)
A	33926	.	G	A	182	.	GERP=-6.35(...)
A	45365	.	A	G	222	.	GERP=-3.55(...)
```

<h3>VCFTabixml</h3>
<h4>Motivation</h4>
Annotate a value from a vcf+xml file.4th column of the BED indexed with TABIX is a XML string.It will be processed with the xslt-stylesheet and should procuce a xml result &lt;properties>&lt;property key='key1'>value1&lt;/property>&lt;property key='key2'>values1&lt;/property>&lt;/properies> INFO fields. Carriage returns will be removed.Parameters to be passed to the stylesheet: vcfchrom (string) vcfpos(int) vcfref(string) vcfalt(string). Version: 1.0
See also evs2bed

<h4>Compilation</h4>
```bash
ant vcftabixml
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>BEDFILE=String</td><td> BED file indexed with tabix. The 4th column *is* a XML string.)  Required. </td></tr>
<tr><td>STYLESHEET=File</td><td>x xslt-stylesheet. REQUIRED. Should produce a valid set of INFO field.  Required. </td></tr>
<tr><td>TAGS=File</td><td>file containing extra INFO headers line to add version: 4.1  Required. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>
<h4>Example</h4>
The header of the tabix-index BED+XML file:
```
1	69427	69428	<snpList><positionString>1:69428</positionString><chrPosition>69428</chrPosition><alleles>G/T</alleles><ua..
1	69475	69476	<snpList><positionString>1:69476</positionString><chrPosition>69476</chrPosition><alleles>C/T</alleles><ua..
1	69495	69496	<snpList><positionString>1:69496</positionString><chrPosition>69496</chrPosition><alleles>A/G</alleles><ua..
1	69510	69511	<snpList><positionString>1:69511</positionString><chrPosition>69511</chrPosition><alleles>G/A</alleles><ua...
1	69589	69590	<snpList><positionString>1:69590</positionString><chrPosition>69590</chrPosition><alleles>A/T</alleles><ua...
1	69593	69594	<snpList><positionString>1:69594</positionString><chrPosition>69594</chrPosition><alleles>C/T</alleles><ua..
1	69619	69620	<snpList><positionString>1:69620</positionString><chrPosition>69620</chrPosition><alleles>T/TA</alleles><u...
1	69744	69745	<snpList><positionString>1:69745</positionString><chrPosition>69745</chrPosition><alleles>CA/C</alleles><u..
1	69760	69761	<snpList><positionString>1:69761</positionString><chrPosition>69761</chrPosition><alleles>T/A</alleles><ua...
1	801942	801943	<snpList><positionString>1:801943</positionString><chrPosition>801943</chrPosition><alleles>T/C</alleles...
(...)
```
The content of the tags.txt file:
```
##INFO=<ID=EVS_AAMAF,Number=1,Type=String,Description="EVS_AAMAF from EVS">
##INFO=<ID=EVS_AVGSAMPLEREADDEPTH,Number=1,Type=String,Description="EVS_AVGSAMPLEREADDEPTH from EVS">
##INFO=<ID=EVS_CLINICALLINK,Number=1,Type=String,Description="EVS_CLINICALLINK from EVS">
##INFO=<ID=EVS_CONFLICTALT,Number=1,Type=String,Description="EVS_CONFLICTALT from EVS">
##INFO=<ID=EVS_CONSERVATIONSCOREGERP,Number=1,Type=String,Description="EVS_CONSERVATIONSCOREGERP from EVS">
##INFO=<ID=EVS_CONSERVATIONSCORE,Number=1,Type=String,Description="EVS_CONSERVATIONSCORE from EVS">
##INFO=<ID=EVS_GENELIST,Number=1,Type=String,Description="EVS_GENELIST from EVS">
##INFO=<ID=EVS_GWASPUBMEDIDS,Number=1,Type=String,Description="EVS_GWASPUBMEDIDS from EVS">
##INFO=<ID=EVS_ONEXOMECHIP,Number=1,Type=String,Description="EVS_ONEXOMECHIP from EVS">
##INFO=<ID=EVS_RSIDS,Number=1,Type=String,Description="EVS_RSIDS from EVS">
##INFO=<ID=EVS_TOTALMAF,Number=1,Type=String,Description="EVS_TOTALMAF from EVS">
##INFO=<ID=EVS_UAMAF,Number=1,Type=String,Description="EVS_UAMAF from EVS">
```
The XSLT file:

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 
<xsl:output method="xml"/>
<xsl:param name="vcfchrom"/>
<xsl:param name="vcfpos"/>
<xsl:param name="vcfref"/>
<xsl:param name="vcfalt"/>
<xsl:template match="/">
<properties>
<xsl:apply-templates select="evsData|snpList"/>
</properties>
</xsl:template>
<xsl:template match="evsData">
<xsl:apply-templates select="snpList"/>
</xsl:template>
<xsl:template match="snpList">
<xsl:choose>
 <xsl:when test="chromosome=$vcfchrom and chrPosition=$vcfpos and refAllele=$vcfref">
<xsl:apply-templates select="clinicalLink|rsIds|uaMAF|aaMAF|totalMAF|avgSampleReadDepth|geneList|conservationScore|conservationScoreGERP|gwasPubmedIds|onExomeChip|gwasPubmedIds"/>
  <xsl:if test="altAlleles!=$vcfalt">
  	<entry key="EVS_CONFLICTALT">
  		<xsl:value-of select="altAlleles"/>
  	</entry>		
  	</xsl:if>
  </xsl:when>
  <xsl:otherwise/>
</xsl:choose>
</xsl:template>
<xsl:template match="clinicalLink|rsIds|uaMAF|aaMAF|totalMAF|avgSampleReadDepth|geneList|conservationScore|conservationScoreGERP|onExomeChip|gwasPubmedIds">
<xsl:if test="string-length(normalize-space(text()))&gt;0">
<entry>
<xsl:attribute name="key"><xsl:value-of select="concat('EVS_',translate(name(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'))"/></xsl:attribute>
<xsl:value-of select="text()"/>
</entry>
</xsl:if>
</xsl:template>
</xsl:stylesheet>
```

Running:

```bash
java -jar dist/vcftabixml.jar \
	IN=input.vcf.gz \
	XSL=evs2vcf.xsl \
	BEDFILE=evs.data.gz \
	TAGS=tags.txt | grep -v "##" | head

#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	U0925
A	613326	.	G	A	182	.	AC1=1;AF1=0.5;DP=327;DP4=0,147,0,178;FQ=163;MQ=47;PV4=1,0.21,1.2e-35,1;RPB=6.999638e+00;VDB=8.105711e-05	GT:PL:DP:GQ	0/1:212,0,190:325:99
A	614565	.	A	G	222	.	AC1=2;AF1=1;DP=910;DP4=0,1,71,836;EVS_AAMAF=23.2659;EVS_AVGSAMPLEREADDEPTH=70;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.5;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs26;EVS_TOTALMAF=30.5519;EVS_UAMAF=33.7209;FQ=-282;MQ=41;PV4=1,0.26,0.032,1;RPB=1.705346e+00;VDB=1.656917e-35	GT:PL:DP:GQ	1/1:255,255,0:908:99
A	614379	.	C	T	225	.	AC1=1;AF1=0.5;DP=979;DP4=33,440,37,456;EVS_AAMAF=2.8179;EVS_AVGSAMPLEREADDEPTH=59;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-4.7;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs249;EVS_TOTALMAF=8.8261;EVS_UAMAF=11.4393;FQ=225;MQ=42;PV4=0.8,1,3.8e-152,1;RPB=1.317662e+01;VDB=3.857882e-21	GT:PL:DP:GQ	0/1:255,0,255:966:99
A	614565	.	T	G	222	.	AC1=2;AF1=1;DP=209;DP4=0,0,0,187;FQ=-282;MQ=46;VDB=2.410569e-12	GT:PL:DP:GQ	1/1:255,255,0:187:99
A	614953	.	C	T	225	.	AC1=1;AF1=0.5;DP=810;DP4=197,214,194,195;EVS_AAMAF=2.4603;EVS_AVGSAMPLEREADDEPTH=45;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.1;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs733;EVS_TOTALMAF=7.0506;EVS_UAMAF=9.4205;FQ=225;MQ=47;PV4=0.62,1,9.2e-58,0.015;RPB=4.560133e+00;VDB=6.880316e-03	GT:PL:DP:GQ	0/1:255,0,255:800:99
A	614922	.	G	A	130	.	AC1=1;AF1=0.5;DP=183;DP4=0,94,0,86;EVS_AAMAF=2.3053;EVS_AVGSAMPLEREADDEPTH=43;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.0;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs202;EVS_TOTALMAF=7.1215;EVS_UAMAF=9.6386;FQ=133;MQ=44;PV4=1,0.0065,3.7e-51,1;RPB=3.500959e+00;VDB=9.915205e-29	GT:PL:DP:GQ	0/1:160,0,184:180:99
A	614986	.	G	C	188	.	AC1=2;AF1=1;DP=176;DP4=0,0,0,175;FQ=-282;MQ=46;VDB=0.000000e+00	GT:PL:DP:GQ	1/1:221,255,0:175:99
A	615009	.	T	A	125	.	AC1=1;AF1=0.5;DP=103;DP4=45,0,56,0;FQ=120;MQ=45;PV4=1,0.14,2.8e-19,1;RPB=1.520268e+00;VDB=1.539079e-06	GT:PL:DP:GQ	0/1:155,0,148:101:99
A	615037	.	C	T	161	.	AC1=1;AF1=0.5;DP=353;DP4=0,164,0,165;FQ=110;MQ=48;PV4=1,1,1.1e-23,1;RPB=5.549816e+00;VDB=1.486773e-11	GT:PL:DP:GQ	0/1:191,0,137:329:99
```

### SplitBam

####Motivation

Split a BAM by chromosome group. Create EMPTY bams if no reads was found for a given group.

####Compilation

```bash
ant splitbam
```

#### Options

<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>REF=File</td><td>Indexex reference  Required. </td></tr>
<tr><td>IN=File</td><td>BAM file to process. Default stdin.   Default value: null. </td></tr>
<tr><td>GENERATE_EMPTY_BAM=Boolean</td><td>generate EMPTY bams for chromosome having no read mapped.   Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>CHROM_GROUP=File</td><td>Chromosome group file.   Default value: null. </td></tr>
<tr><td>ADD_MOCK_RECORD=Boolean</td><td>add a mock pair of sam records to the bam.   Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>OUT_FILE_PATTERN=String</td><td>MUST contain __CHROM__ and end with .bam.   Default value: . This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>UNDERTERMINED_NAME=String</td><td>Unmapped chromosome name.   Default value: Unmapped. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>INPUT_IS_SORTED=Boolean</td><td>input is sorted.   Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
</table>

#### Example

the content of 'split_g1k_v37_01.txt'

```
CHROMS_01_09	1 2 3 4 5 6 7 8 9
CHROMS_10_0Y	10 11 12 13 14 15 16 17 18 19 20 21 22 X Y 
CHROMS_OTHER	MT GL000207.1 GL000226.1 GL000229.1 GL000231.1 GL000210.1 GL000239.1 GL000235.1 GL000201.1 GL000247.1 GL000245.1 GL000197.1 GL000203.1 GL000246.1 GL000249.1 GL000196.1 GL000248.1 GL000244.1 GL000238.1 GL000202.1 GL000234.1 GL000232.1 GL000206.1 GL000240.1 GL000236.1 GL000241.1 GL000243.1 GL000242.1 GL000230.1 GL000237.1 GL000233.1 GL000204.1 GL000198.1 GL000208.1 GL000191.1 GL000227.1 GL000228.1 GL000214.1 GL000221.1 GL000209.1 GL000218.1 GL000220.1 GL000213.1 GL000211.1 GL000199.1 GL000217.1 GL000216.1 GL000215.1 GL000205.1 GL000219.1 GL000224.1 GL000223.1 GL000195.1 GL000212.1 GL000222.1 GL000200.1 GL000193.1 GL000194.1 GL000225.1 GL000192.1 
```

split the output of bwa sampe on the fly:

```bash
bwa sampe (...) |\
java -jar dist/splitbam.jar \
	VALIDATION_STRINGENCY=LENIENT  \
	OUT_FILE_PATTERN=TESTSPLITBAM/__CHROM__.bam \
	REF=human_g1k_v37.fasta \
	ADD_MOCK_RECORD=true \
	GENERATE_EMPTY_BAM=true \
	GP=split_g1k_v37_01.txt 


[Fri Jul 26 13:25:56 CEST 2013] Executing as lindenb@master on Linux 2.6.32-358.6.2.el6.x86_64 amd64; OpenJDK 64-Bit Server VM 1.7.0_19-mockbuild_2013_04_17_19_18-b00; Picard version: null
INFO	2013-07-26 13:25:56	SplitBam	reading stdin
INFO	2013-07-26 13:25:56	SplitBam	opening TESTSPLITBAM/CHROMS_01_09.bam
INFO	2013-07-26 13:25:57	SplitBam	opening TESTSPLITBAM/CHROMS_10_0Y.bam
INFO	2013-07-26 13:25:58	SplitBam	opening TESTSPLITBAM/CHROMS_OTHER.bam
INFO	2013-07-26 13:35:58	SplitBam	closing group CHROMS_01_09
INFO	2013-07-26 13:35:59	SplitBam	closing group CHROMS_10_0Y
INFO	2013-07-26 13:35:59	SplitBam	closing group CHROMS_OTHER
INFO	2013-07-26 13:36:00	SplitBam	closing group Unmapped
Runtime.totalMemory()=1916600320
```


### FindCorruptedFiles

#### Motivation

Searches for corrupted NGS files (VCF/BAM/FASTQ). 

When a parallel workflow/Makefile/cluster/qmake fails, some files may be truncated, this tool will detect the files having a problem.

#### Compilation

```bash
ant findcorruptedfiles
```

#### Options

<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>File(s) and/or directories  This option may be specified 0 or more times. </td></tr>
<tr><td>NUM=Long</td><td>number of features (sam-record, variant, fastq) to read. -1= read everything.   Default value: 100. This option can be set to 'null' to clear the default value. </td></tr>
</table>

#### Example

```bash
java -jar dist/findcorruptedfiles.jar \
	I=path1 \
	I=path/to/dir2 \
	VALIDATION_STRINGENCY=SILENT 2> /dev/null
	
```

### NgsFilesSummary

#### Motivation

Scan folders and generate a summary of the files (SAMPLE/BAM SAMPLE/VCF etc..)

#### Compilation

```bash
ant ngsfilessummary
```

#### Options

<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>File(s) and/or directories  This option may be specified 0 or more times. </td></tr>
</table>

#### Example

```bash
java -jar dist/ngsfilessummary.jar \
	I=/projects/align01/ \
	VALIDATION_STRINGENCY=SILENT 2> /dev/null

SAMPLE1	BAM	/projects/align01/Samples/SAMPLE1/BAM/SAMPLE1_final.bam	321262321	Wed Jun 26 10:30:07 CEST 2013
SAMPLE1	FASTQ	/projects/data
SAMPLE1	VCF	/projects/align01/Samples/SAMPLE1/VCF/SAMPLE1_variations.freebayes.vcf.gz	184191	Mon Jun 17 14:47:22 CEST 2013
SAMPLE1	VCF	/projects/align01/Samples/SAMPLE1/VCF/SAMPLE1_variations.gatk.vcf.gz	113341	Mon Jun 17 11:57:19 CEST 2013
SAMPLE1	VCF	/projects/align01/Samples/SAMPLE1/VCF/SAMPLE1_variations.samtools.vcf.gz	57518	Mon Jun 17 11:58:49 CEST 2013
SAMPLE2	BAM	/projects/align01/Samples/SAMPLE2/BAM/SAMPLE2_final.bam	286100773	Wed Jun 26 10:47:09 CEST 2013
SAMPLE2	FASTQ	/projects/data
SAMPLE2	VCF	/projects/align01/Samples/SAMPLE2/VCF/SAMPLE2_variations.freebayes.vcf.gz	172970	Mon Jun 17 14:45:51 CEST 2013
SAMPLE2	VCF	/projects/align01/Samples/SAMPLE2/VCF/SAMPLE2_variations.gatk.vcf.gz	106390	Mon Jun 17 11:57:19 CEST 2013
SAMPLE2	VCF	/projects/align01/Samples/SAMPLE2/VCF/SAMPLE2_variations.samtools.vcf.gz	52709	Mon Jun 17 11:58:04 CEST 2013
```

### Biostar77828

#### Motivation

Divide the human genome among X cores, taking into account gaps See http://www.biostars.org/p/77828/

#### Compilation
```bash
ant biostar77828
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>Bed file input (default stdin)  Default value: null. </td></tr>
<tr><td>MIN_CORE=Integer</td><td>min_core  Default value: 20. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>MAX_CORE=Integer</td><td>max_core  Default value: 30. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>N_ITERATIONS=Long</td><td>number of iterations  Default value: 1000000. This option can be set to 'null' to clear the default value. </td></tr>
</table>

### Biostar78285

#### Motivation

Extract regions of genome that have 0 coverage See http://www.biostars.org/p/78285/

#### Compilation
```bash
ant biostar78285
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM file (sorted on coordinate). Default:stdin  Default value: null. </td></tr>
<tr><td>USECIGAR=Boolean</td><td>scan the CIGAR string & detect the gaps in the reads. Slower & requires more memory  Default value: false.</td></tr>
</table>

#### Example:
```bash
 $ java -jar dist/biostar78285.jar \
 	I=sorted.bam \
 	USECIGAR=false \
 	VALIDATION_STRINGENCY=LENIENT

seq1	1569	1575
seq2	1567	1584
```

### Biostar78400

#### Motivation

add the read group info to the sam file on a per lane basis

#### Compilation
```bash
ant biostar78400
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM file to process (or stdin).  Default value: null. </td></tr>
<tr><td>OUT=File</td><td>BAM file (or stdout).  Default value: null. </td></tr>
<tr><td>XML=File</td><td>XML desfription of the groups. See below Required. </td></tr>
</table>

#### XML config
the XML should look like this:

```xml
<read-groups>
<flowcell name="HS2000-1259_127">
 <lane index="1">
   <group ID="X1">
     <library>L1</library>
     <platform>P1</platform>
     <sample>S1</sample>
     <platformunit>PU1</platformunit>
     <center>C1</center>
     <description>blabla</description>
   </group>
 </lane>
</flowcell>
<flowcell name="HS2000-1259_128">
 <lane index="2">
   <group ID="x2">
     <library>L2</library>
     <platform>P2</platform>
     <sample>S2</sample>
     <platformunit>PU1</platformunit>
     <center>C1</center>
     <description>blabla</description>
   </group>
 </lane>
</flowcell>
</read-groups>
```

#### Example:

```bash
$ cat input.sam 
@SQ	SN:ref	LN:45
@SQ	SN:ref2	LN:40
HS2000-1259_127:1:1210:15640:52255	163	ref	7	30	8M4I4M1D3M	=	37	39	
TTAGATAAAGAGGATACTG	*	XX:B:S,12561,2,20,112
HS2000-1259_128:2:1210:15640:52255	0	ref	9	30	1S2I6M1P1I1P1I4M2I	*	0	
0	AAAAGATAAGGGATAAA	*

$java -jar dist/biostar78400.jar \
	XML=groups.xml \
	I=input.sam \
 	VALIDATION_STRINGENCY=LENIENT

@HD	VN:1.4	SO:unsorted
@SQ	SN:ref	LN:45
@SQ	SN:ref2	LN:40
@RG	ID:X1	PL:P1	PU:P1	LB:L1	DS:blabla	SM:S1	CN:C1
@RG	ID:x2	PL:P2	PU:P2	LB:L2	DS:blabla	SM:S2	CN:C1
@PG	ID:Biostar78400	PN:Biostar78400	PP:Biostar78400	VN:1.0	(...)
HS2000-1259_127:1:1210:15640:52255	163	ref	7	30	8M4I4M1D3M	=	37	39	TTAGATAAAGAGGATACTG	*	RG:Z:X1	XX:B:S,12561,2,20,112
HS2000-1259_128:2:1210:15640:52255	0	ref	9	30	1S2I6M1P1I1P1I4M2I	*	0	0AAAAGATAAGGGATAAA	*	RG:Z:x2
```

### BamStats04

#### Motivation

 Coverage statistics for a BED file. It uses the Cigar string instead of the start/end to compute the coverage
 
#### Compilation
```bash
ant bamstats04
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM file to process.  Required. </td></tr>
<tr><td>BEDILE=File</td><td>BED File.  Required. </td></tr>
<tr><td>NO_DUP=Boolean</td><td>discard duplicates  Default value: true. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>NO_ORPHAN=Boolean</td><td>discard not properly paired  Default value: true. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>NO_VENDOR=Boolean</td><td>discard failing Vendor Quality  Default value: true. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>MMQ=Integer</td><td>min mapping quality  Default value: 0. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>MIN_COVERAGE=Integer</td><td>min coverage to say the position is not covered  Default value: 0. This option can be set to 'null' to clear the default value. </td></tr>
</table>


#### Example:

```bash
$ java -jar dist/bamstats04.jar \
	BED=data.bed \
	I=f.bam
#chrom	start	end	length	mincov	maxcov	mean	nocoveragepb	percentcovered
1	429665	429785	120	42	105	72.36666666666666	0	100
1	430108	430144	36	9	9	9.0	0	100
1	439811	439904	93	0	36	3.6451612903225805	21	77
1	550198	550246	48	1325	1358	1344.4583333333333	0	100
1	629855	629906	51	223	520	420.70588235294116	0	100
1	689960	690029	69	926	1413	1248.9420289855072	0	100
1	690852	690972	120	126	193	171.24166666666667	0	100
1	787283	787406	123	212	489	333.9756097560976	0	100
1	789740	789877	137	245	688	528.6715328467153	0	1
```

### VCFAnnoBam

#### Motivation

Annotate a VCF with the Coverage statistics of a BAM file +  BED file of capture.
It uses the Cigar string instead of the start/end to get the voverage

 
#### Compilation  #### 
```bash
ant vcfannobam
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>BEDILE=File</td><td>BED File capture.  Required. </td></tr>
<tr><td>BAMFILE=File</td><td>indexed BAM File.  This option must be specified at least 1 times. </td></tr>
<tr><td>MMQ=Integer</td><td>min mapping quality  Default value: 0. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>MIN_COVERAGE=Integer</td><td>min coverage to say the position is not covered  Default value: 0. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>


Example:  #### 

```bash
$  java -jar dist/vcfannobam.jar \
		BAM=input.bam\
		IN=input.vcf.gz \
		BED=capture.bed \
		VALIDATION_STRINGENCY=SILENT

(...)
##INFO=<ID=CAPTURE,Number=1,Type=String,Description="Capture stats: Format is (start|end|mean|min|max|length|not_covered|percent_covered) ">
(...)
2	16100665	.	A	T	13261.77	.	CAPTURE=16100619|16100715|1331.96|1026.0|1773.0|97|0|100
2	178395141	.	T	A	1940.77	.	CAPTURE=178394991|178395199|193.11|100.0|276.0|209|0|100
(...)
```

### BamStats01 ###

#### Motivation ####

Statistics about the reads in a BAM

#### Compilation  #### 
```bash
ant bamstats01
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM files to process.  This option must be specified at least 1 times. </td></tr>
<tr><td>OUT=File</td><td>Ouput. Default stdout.   Default value: null. </td></tr>
<tr><td>QUAL=Double</td><td>Default treshold quality  Default value: 30.0. This option can be set to 'null' to clear the default value. </td></tr>
<tr><td>BEDILE=File</td><td>BED File.  Default value: null. </td></tr>
</table>


#### Example #### 

```bash
$  jjava -jar dist/bamstats01.jar \
		IN=my.bam \
		BED=capture.bed

(...)
#Filename	Sample	ALL_TOTAL	ALL_PAIRED	ALL_UNMAPPED	ALL_MAPPED	ALL_PROPER_PAIR	ALL_PLUS_STRAND	ALL_MINUS_STRAND	ALL_PRIMARY_ALIGNMENT	ALL_FAIL_MAPPING_QUALITY	ALL_DUPLICATE	ALL_FAIL_VENDOR_QUALITY	IN_TARGET_TOTAL	IN_TARGET_PAIRED	IN_TARGET_UNMAPPED	IN_TARGET_MAPPED	IN_TARGET_PROPER_PAIR	IN_TARGET_PLUS_STRAND	IN_TARGET_MINUS_STRAND	IN_TARGET_PRIMARY_ALIGNMENT	IN_TARGET_FAIL_MAPPING_QUALITY	IN_TARGET_DUPLICATE	IN_TARGET_FAIL_VENDOR_QUALITY	OFF_TARGET_TOTAL	OFF_TARGET_PAIRED	OFF_TARGET_UNMAPPED	OFF_TARGET_MAPPED	OFF_TARGET_PROPER_PAIR	OFF_TARGET_PLUS_STRAND	OFF_TARGET_MINUS_STRAND	OFF_TARGET_PRIMARY_ALIGNMENT	OFF_TARGET_FAIL_MAPPING_QUALITY	OFF_TARGET_DUPLICATE	OFF_TARGET_FAIL_VENDOR_QUALITY
my.bam	Sample	1617984	1617984	3966	1614018	1407862	806964	807054	1614018	56980	0	0	1293922	1293922	0	1293922	1133808	644741	649181	1293922	14087	0	0	320096	320096	0	320096	274054	162223	157873	320096	42893	0	0
(...)
```
### EVS2Bed ###

#### Motivation ####

Download data from EVS http://evs.gs.washington.edu/EVS as a BED chrom/start/end/XML
For later use, see vcftabixml

#### Compilation  #### 

```bash
ant bamstats01
```	

#### Example  ####
```bash
$ java  -jar dist/evs2bed.jar L=10 2> /dev/null | cut -c 1-100
1	69427	69428	<snpList><positionString>1:69428</positionString><chrPosition>69428</chrPosition><alle..
1	69475	69476	<snpList><positionString>1:69476</positionString><chrPosition>69476</chrPosition><alle..
1	69495	69496	<snpList><positionString>1:69496</positionString><chrPosition>69496</chrPosition><alle..
1	69510	69511	<snpList><positionString>1:69511</positionString><chrPosition>69511</chrPosition><alle..
```

### Biostar81455 ###

#### Motivation ####

Question: Defining precisely the genomic context based on a position http://www.biostars.org/p/81455/

#### Compilation  #### 

```bash
ant biostar81455
```	
#### Example  ####
```bash
echo -e "chr22\t41258261\nchr22\t52000000\nchr22\t0" |\
	java   dist/biostar81455.jar 

chr22	41258261	uc003azg.2	41253084	41258785	POSITIVE	Exon 2	41257621	41258785	0
chr22	41258261	uc011aox.2	41253084	41305239	POSITIVE	Exon 1	41253084	41253249	-5012
chr22	41258261	uc003azi.3	41253084	41328823	POSITIVE	Exon 1	41253084	41253249	-5012
chr22	41258261	uc003azj.3	41255553	41258130	NEGATIVE	Exon 1	41255553	41258130	-131
chr22	41258261	uc010gyh.1	41258260	41282519	POSITIVE	Exon 1	41258260	41258683	0
chr22	41258261	uc011aoy.1	41258260	41363888	POSITIVE	Exon 1	41258260	41258683	0
chr22	52000000	uc011asd.2	51195513	51227614	POSITIVE	Exon 4	51227177	51227614	-772386
chr22	52000000	uc003bni.3	51195513	51238065	POSITIVE	Exon 4	51237082	51238065	-761935
chr22	52000000	uc011ase.1	51205919	51220775	NEGATIVE	Exon 1	51220615	51220775	-779225
chr22	52000000	uc003bnl.1	51205919	51222087	NEGATIVE	Exon 1	51221928	51222087	-777913
chr22	52000000	uc003bns.3	51222156	51238065	POSITIVE	Exon 3	51237082	51238065	-761935
chr22	52000000	uc003bnq.1	51222224	51227600	POSITIVE	Exon 4	51227322	51227600	-772400
chr22	52000000	uc003bnr.1	51222224	51227781	POSITIVE	Exon 4	51227319	51227781	-772219
chr22	52000000	uc010hbj.3	51222224	51238065	POSITIVE	Exon 3	51237082	51238065	-761935
chr22	0	uc002zks.4	16150259	16193004	NEGATIVE	Exon 8	16150259	16151821	16150259
chr22	0	uc002zkt.3	16162065	16172265	POSITIVE	Exon 1	16162065	16162388	16162065
chr22	0	uc002zku.3	16179617	16181004	NEGATIVE	Exon 1	16179617	16181004	16179617
chr22	0	uc002zkv.3	16187164	16193004	NEGATIVE	Exon 5	16187164	16187302	16187164	
```
### VCFPolyX ###

#### Motivation ####

Number of repeated REF bases around POS.
 
#### Compilation  #### 

```bash
ant vcfpolyx
```	
#### Options  #### 
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>REF=File</td><td>Reference  Required. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout. </td></tr>
</table>

#### Example  ####
```bash
$ java  -jar dist/vcfpolyx.jar I=input.vcf REF=reference.fa
(...)
2	1133956	.	A	G	2468.84	.	POLYX=23
2	1133956	.	A	AG	3604.25	.	POLYX=23
2	2981671	.	T	G	47.18	.	POLYX=24
(...)
```

### IlluminaDir ###

#### Motivation ####



[Illuminadir](wiki/Illuminadir) scans folders , search for FASTQs and generate a structured summary of the files (xml or json).
Currently only tested with HiSeq data having an index
 
#### Compilation  #### 

```bash
ant illuminadir
```	

<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>root directories  This option may be specified 0 or more times. </td></tr>
<tr><td>JSON=Boolean</td><td>json output  Default value: false.  Ouput, could be used with jsvelocity https://github.com/lindenb/jsvelocity </td></tr>
</table>

#### Example  ####
```bash
$ java  -jar dist/illuminadir.jar \
	I=dir1 \
	I=dir2 | xsltproc xml2script.xslt > script.bash
(...)
```

