JVARKIT
=======

Java utilities for Next Generation Sequencing

Pierre Lindenbaum PhD

http://plindenbaum.blogspot.com

@yokofakun	

Dependencies
------------

Tested with java 1.7 and the Picard library 1.91 ( http://sourceforge.net/projects/picard/)


Download & Install
------------------

```bash
git clone "https://github.com/lindenb/jvarkit.git"
cd jvarkit.git
```

edit **build.properties** to configure the project. Something like:

```
picard.version=1.91
picard.dir=/home/lindenb/package/picard-tools-${picard.version}
picard.jar=${picard.dir}/picard-${picard.version}.jar
sam.jar=${picard.dir}/sam-${picard.version}.jar
variant.jar=${picard.dir}/variant-${picard.version}.jar
tribble.jar=${picard.dir}/tribble-${picard.version}.jar
```

Working behind a proxy ? Edit the file build.dtd.
Change it to:
```xml
<!ENTITY httpProxyHost "proxy-upgrade.univ-nantes.prive">
<!ENTITY httpProxyPort "3128">
<!ENTITY xjcproxyarg " <arg value='-httpproxy'/> <arg value='&httpProxyHost;:&httpProxyPort;'/>">
(...)
```
no proxy, set build.dtd to

```xml
<!ENTITY httpProxyHost "">
<!ENTITY httpProxyPort "">
<!ENTITY xjcproxyarg "">
(...)
```


<h2>Tools</h2>

<h3> Filtering VCF with javascript (rhino) </h3>
<h4>Compilation</h4>
```
ant vcffilterjs
```
<h4>Usage</h4>
```
 java -jar dist/vcffilterjs.jar [options]
```
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>SCRIPT_FILE=File</td><td>javascript file   Default value: null. </td></tr>
<tr><td>SCRIPT_EXPRESSION=String</td><td>javascript expression   Default value: null. </td></tr>
<tr><td>IN=String</td><td>VCF file/URL to process. Default stdin.   Default value: null. </td></tr>
<tr><td>OUT=File</td><td>VCF file to generate. Default stdout.   Default value: null. </td></tr>
</table>



the script binds the following variables:

* **variant** : the current variation;  a org.broadinstitute.variant.variantcontext.VariantContext ( http://sourceforge.net/p/picard/code/HEAD/tree/trunk/src/java/org/broadinstitute/variant/variantcontext/VariantContext.java )
* **header** : the VCF header org.broadinstitute.variant.vcf.VCFHeader ( http://sourceforge.net/p/picard/code/HEAD/tree/trunk/src/java/org/broadinstitute/variant/vcf/VCFHeader.java).

the script should return '1' or true if the current VCF file should be printed.

<h4>Example</h4>

the file filter.js

```javascript
/** prints a VARIATION if two samples at least
have a DP<200 */ 
function myfilterFunction()
	{
	var samples=header.genotypeSamples;
	var countOkDp=0;


	for(var i=0; i< samples.size();++i)
		{
		var sampleName=samples.get(i);
		if(! variant.hasGenotype(sampleName)) continue;
		var genotype = variant.genotypes.get(sampleName);
		if( ! genotype.hasDP()) continue;
		var dp= genotype.getDP();
		if(dp < 200 ) countOkDp++;
		}
	return (countOkDp>2)
	}
myfilterFunction();
```

```bash
$ curl -s "https://raw.github.com/jamescasbon/PyVCF/master/vcf/test/gatk.vcf" |\
   java -jar  dist/vcffilterjs.jar  SCRIPT_FILE=filter.js
   
##fileformat=VCFv4.1
##FORMAT=<ID=AD,Number=.,Type=Integer,Description="Allelic depths for the ref and alt alleles in the order listed">
##FORMAT=<ID=DP,Number=1,Type=Integer,Description="Approximate read depth (reads with MQ=255 or with bad mates are filtered)">
##FORMAT=<ID=GQ,Number=1,Type=Integer,Description="Genotype Quality">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	BLANK	NA12878	NA12891	NA12892	NA19238	NA19239	NA19240
chr22	42526449	.	T	A	151.47	.	AC=1;AF=0.071;AN=14;BaseQRankSum=2.662;DP=1226;DS;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=41.2083;MQ=240.47;MQ0=0;MQRankSum=0.578;QD=4.89;ReadPosRankSum=3.611	GT:AD:DP:GQ:PL	0/1:23,8:31:99:190,0,694	0/0:188,0:190:99:0,478,5376	0/0:187,0:187:99:0,493,5322	0/0:247,0:249:99:0,634,6728	0/0:185,0:185:99:0,487,5515	0/0:202,0:202:99:0,520,5857	0/0:181,1:182:99:0,440,5362
chr22	42526634	.	T	C	32.60	.	AC=1;AF=0.071;AN=14;BaseQRankSum=1.147;DP=1225;DS;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=50.0151;MQ=240.65;MQ0=0;MQRankSum=1.151;QD=1.30;ReadPosRankSum=1.276	GT:AD:DP:GQ:PL	0/1:21,4:25:71:71,0,702	0/0:187,2:189:99:0,481,6080	0/0:233,0:233:99:0,667,7351	0/0:230,0:230:99:0,667,7394	0/0:174,1:175:99:0,446,5469	0/0:194,2:196:99:0,498,6239	0/0:174,0:175:99:0,511,5894
chr22	42527793	rs1080989	C	T	3454.66	.	AC=2;AF=0.167;AN=12;BaseQRankSum=-3.007;DB;DP=1074;DS;Dels=0.01;FS=0.000;HRun=1;HaplotypeScore=75.7865;MQ=209.00;MQ0=0;MQRankSum=3.014;QD=9.36;ReadPosRankSum=0.618	GT:AD:DP:GQ:PL	./.	0/1:72,90:162:99:1699,0,1767	0/1:103,96:202:99:1756,0,2532	0/0:188,0:188:99:0,526,5889	0/0:160,0:160:99:0,457,4983	0/0:197,0:198:99:0,544,6100	0/0:156,0:156:99:0,439,5041

```

<h4>compile</h4>

```bash
$ ant vcffilterjs 
Buildfile: /home/lindenb/src/jvarkit-git/build.xml

vcffilterjs:
    (...)

BUILD SUCCESSFUL
Total time: 1 second

```

<br/>
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
<h3>SamJS: filtering a SAM/BAM file with javascript.</h3>
<h4>Motivation</h4>
Filters a BAM using javascript( java rhino engine).
<h4>Scripting</h4>
The script puts '<b>record</b>' a SamRecord (http://picard.sourceforge.net/javadoc/net/sf/samtools/SAMRecord.html) 
and '<b>header</b>' ( http://picard.sourceforge.net/javadoc/net/sf/samtools/SAMFileHeader.html) in the script context 
<h4>Options</h4>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>IN=File</td><td>BAM file to process. Default stdin. </td></tr>
<tr><td>OUT=File</td><td>output filename. Default stdin. </td></tr>
<tr><td>SCRIPT_FILE=File</td><td>javascript file </td></tr>
<tr><td>SCRIPT_EXPRESSION=String</td><td>javascript expression </td></tr>
<tr><td>SAM_OUTPUT=Boolean</td><td>sam output   Default value: false. This option can be set to 'null' to clear the default value. Possible values: {true, false} </td></tr>
<tr><td>LIMIT=Long</td><td>limit to 'L' records. </td></tr>
</table>
<h4>Compilation</h4>
```bash
ant samjs
```
<h4>Example</h4>
get a SAM where the  read OR the mate is unmapped

```bash
java -jar dist/samjs.jar I=ex1.bam \
	VALIDATION_STRINGENCY=SILENT \
	SCRIPT_EXPRESSION="record.readUnmappedFlag || record.mateUnmappedFlag;" \
	SAM=true 

@HD	VN:1.4	SO:unsorted
@SQ	SN:seq1	LN:1575
@SQ	SN:seq2	LN:1584
B7_591:4:96:693:509	73	seq1	1	99	36M	*	0	0	CACTAGTGGCTCATTGTAAATGTGTGGTTTAACTCG	<<<<<<<<<<<<<<<;<<<<<<<<<5<<<<<;:<;7	H0:i:1	H1:i:0	MF:i:18	NM:i:0	UQ:i:0	Aq:i:73
EAS54_65:7:152:368:113	73	seq1	3	99	35M	*	0	0	CTAGTGGCTCATTGTAAATGTGTGGTTTAACTCGT	<<<<<<<<<<0<<<<655<<7<<<:9<<3/:<6):	H0:i:1	H1:i:0	MF:i:18	NM:i:0	UQ:i:0	Aq:i:66
EAS51_64:8:5:734:57	137	seq1	5	99	35M	*	0	0	AGTGGCTCATTGTAAATGTGTGGTTTAACTCGTCC	<<<<<<<<<<<7;71<<;<;;<7;<<3;);3*8/5	H0:i:1	H1:i:0	MF:i:18	NM:i:0	UQ:i:0	Aq:i:66
B7_591:1:289:587:906	137	seq1	6	63	36M	*	0	0	GTGGCTCATTGTAATTTTTTGTTTTAACTCTTCTCT	(-&----,----)-)-),'--)---',+-,),''*,	H0:i:0	H1:i:0	MF:i:130	NM:i:5	UQ:i:38	Aq:i:63
EAS56_59:8:38:671:758	137	seq1	9	99	35M	*	0	0	GCTCATTGTAAATGTGTGGTTTAACTCGTCCATGG	<<<<<<<<<<<<<<<;<;7<<<<<<<<7<<;:<5%	H0:i:1	H1:i:0	MF:i:18	NM:i:0	UQ:i:0	Aq:i:72
EAS56_61:6:18:467:281	73	seq1	13	99	35M	*	0	0	ATTGTAAATGTGTGGTTTAACTCGTCCCTGGCCCA	<<<<<<<<;<<<8<<<<<;8:;6/686&;(16666	H0:i:0	H1:i:1	MF:i:18	NM:i:1	UQ:i:5	Aq:i:39
EAS114_28:5:296:340:699	137	seq1	13	99	36M	*	0	0	ATTGTAAATGTGTGGTTTAACTCGTCCATGGCCCAG	<<<<<;<<<;<;<<<<<<<<<<<8<8<3<8;<;<0;	H0:i:1	H1:i:0	MF:i:18	NM:i:0	UQ:i:0	Aq:i:73
B7_597:6:194:894:408	73	seq1	15	99	35M	*	0	0	TGTAAATGTGTGGTTTAACTCGTCCATTGCCCAGC	<<<<<<<<<7<<;<<<<;<<<7;;<<<*,;;572<	H0:i:0	H1:i:1	MF:i:18	NM:i:1	UQ:i:9	Aq:i:43
EAS188_4:8:12:628:973	89	seq1	18	75	35M	*	0	0	AAATGTGTGGTTTAACTCGTCCATGGCCCAGCATT	==;=:;:;;:====;=;===:=======;==;===	H0:i:1	H1:i:0	MF:i:64	NM:i:0	UQ:i:0	Aq:i:0
(...)
```

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
