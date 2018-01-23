# VcfStats

![Last commit](https://img.shields.io/github/last-commit/lindenb/jvarkit.png)

Produce VCF statitics


## Usage

```
Usage: vcfstats [options] Files
  Options:
    --binSize
      [20170718] When plotting data over a genome, divide it into 'N' bp.
      Default: 1000000
    --disableGTConcordance
      Disable Plot Sample vs Sample Genotypes (Faster...)
      Default: false
    --disableMAFPlot
      Disable MAF plot
      Default: false
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -K, -kg, --knownGenes
      UCSC knownGene File/URL. The knowGene format is a compact alternative to 
      GFF/GTF because one transcript is described using only one line.	Beware 
      chromosome names are formatted the same as your REFERENCE. A typical 
      KnownGene file is 
      http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz 
      .If you only have a gff file, you can try to generate a knownGene file 
      with [http://lindenb.github.io/jvarkit/Gff2KnownGene.html](http://lindenb.github.io/jvarkit/Gff2KnownGene.html)
    -mafTag, --mafTag
      Do not calculate MAF for controls, but use this tag to get Controls' MAF
    -nchr, --nocallishomref
      treat no call as HomRef
      Default: false
  * -o, --output
      output Directory or zip file. The output contains the data files as well 
      as a Makefile to convert the data files to graphics using gnuplot.
    -ped, --pedigree
      A pedigree is a text file delimited with tabs. No header. Columns are 
      (1) Family (2) Individual-ID (3) Father Id or '0' (4) Mother Id or '0' 
      (5) Sex : 1 male/2 female / 0 unknown (6) Status : 0 unaffected, 1 
      affected,-9 unknown
    --prefix
      File/zip prefix
      Default: <empty string>
    -so, --soterms
      Sequence ontology Accession to observe. VCF must be annotated with 
      SNPEFF or VEP. e.g: "SO:0001818" (protein altering variant) "SO:0001819" 
      (synonymouse variant)
      Default: []
    -tee, --tee
      output the incoming vcf to stdout. Useful to get intermediary stats in a 
      pipeline 
      Default: false
    --trancheAffected
      tranches for the number of affected. A 'range of integers' is a list of 
      integers in ascending order separated with semicolons.
      Default: [[-Inf/0[, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, [10/20[, [20/50[, [50/100[, [100/200[, [200/300[, [300/400[, [400/500[, [500/1000[, [1000/Inf[]
    --trancheAlts
      tranches for the number of ALTs. A 'range of integers' is a list of 
      integers in ascending order separated with semicolons.
      Default: [[-Inf/0[, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, [10/Inf[]
    --trancheDP
      tranches for the DEPTH. A 'range of integers' is a list of integers in 
      ascending order separated with semicolons.
      Default: [[-Inf/0[, [0/10[, [10/20[, [20/30[, [30/50[, [50/100[, [100/200[, [200/300[, [300/400[, [400/500[, [500/600[, [600/700[, [700/800[, [800/900[, [900/1000[, [1000/2000[, [2000/3000[, [3000/4000[, [4000/5000[, [5000/10000[, [10000/20000[, [20000/30000[, [30000/40000[, [40000/50000[, [50000/100000[, [100000/Inf[]
    --trancheDistance
      tranches for the distance between the variants. A 'range of integers' is 
      a list of integers in ascending order separated with semicolons.
      Default: [[-Inf/0[, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, [10/20[, [20/100[, [100/200[, [200/300[, [300/400[, [400/500[, [500/1000[, [1000/Inf[]
    --trancheIndelSize
      tranches for the Indel size A 'range of integers' is a list of integers 
      in ascending order separated with semicolons.
      Default: [[-Inf/0[, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, [10/15[, [15/20[, [20/50[, [50/100[, [100/Inf[]
    --vckey
      Variant Context Key. if defined, I will look at this key in the INFO 
      column and produce a CASE/CTRL graf for each item. If undefined, I will 
      produce a default graph with all variant
    --version
      print version and exit

```


## Keywords

 * vcf
 * stats
 * burden
 * gnuplot


## Compilation

### Requirements / Dependencies

* java [compiler SDK 1.8](http://www.oracle.com/technetwork/java/index.html) (**NOT the old java 1.7 or 1.6**) and avoid OpenJdk, use the java from Oracle. Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make vcfstats
```

The *.jar libraries are not included in the main jar file, [so you shouldn't move them](https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

Experimental: you can also create a [fat jar](https://stackoverflow.com/questions/19150811/) which contains classes from all the libraries, on which your project depends (it's bigger). Those fat-jar are generated by adding `standalone=yes` to the gnu make command, for example ` make vcfstats standalone=yes`.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfstats/VcfStats.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfstats/VcfStats.java)


<details>
<summary>Git History</summary>

```
Wed Jan 17 15:25:41 2018 +0100 ; add doc, support for lumpy ; https://github.com/lindenb/jvarkit/commit/89020a4dc4140909b0bc6a1dae20f798ef4d3e10
Tue Nov 14 16:13:41 2017 +0100 ; epsitatis01, strange bug in htsjdk https://github.com/samtools/htsjdk/issues/1026 ; https://github.com/lindenb/jvarkit/commit/871a7cc3ed14df5d5b6cf19ef9bef87160795c16
Tue Jul 18 23:00:49 2017 +0200 ; gen scan in vcfstats, dict ; https://github.com/lindenb/jvarkit/commit/f49878cdedc9fdb934a27f63416cb072e744a7b0
Tue Jul 18 15:02:27 2017 +0200 ; gen scan in vcfstats ; https://github.com/lindenb/jvarkit/commit/b7fe9f09ecda344a370a888f36e5eca25a6a761e
Thu Jul 13 20:16:36 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/85b6c9c196e9a065dfd47bee37fe50238af41660
Thu Jul 13 18:38:11 2017 +0200 ; bioalcidaejdk / vcfstats ; https://github.com/lindenb/jvarkit/commit/b403134b5c8e9961489ac8b41d477947a83ff2c4
Wed Jul 12 17:06:39 2017 +0200 ; bioalcidaejdk / vcfstats ; https://github.com/lindenb/jvarkit/commit/c610681de3f3ba3f34efe9076a01484580d0d0f3
Tue Jul 11 17:57:33 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1f248bc7f1fd8a0824bb65a4c67eb052d5a6e381
Fri Jun 30 17:20:28 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/5fe0a8568d706e8cd5898ff66c0ebd8b1f8447a5
Mon Jun 26 17:29:03 2017 +0200 ; burden ; https://github.com/lindenb/jvarkit/commit/a3b7abf21d07f0366e81816ebbb2cce26b2341e7
Wed Jun 14 17:01:36 2017 +0200 ; fast genotype gvcf ; https://github.com/lindenb/jvarkit/commit/d77e93940ad9a7f8144527332067b663b55a10f6
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Fri May 19 15:26:44 2017 +0200 ; vcf stats ; https://github.com/lindenb/jvarkit/commit/965f6bb8151a7748a4d15bc7520468492da9c40b
Thu May 18 21:39:06 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/bc38632dd0a04efa638370571cd320b876accb86
Thu May 18 18:34:07 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/89cb7d10eaeef051af30f1043698546f555cbcd8
Wed May 17 18:02:11 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/7d5989fdf77a7a4a616a29876997809683224316
Wed May 17 14:09:36 2017 +0200 ; fix typo bioalcidae ; https://github.com/lindenb/jvarkit/commit/9db2344e7ce840df02c5a7b4e2a91d6f1a5f2e8d
Wed May 3 17:57:20 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/db456cbf0b6586ea60a4fe8ea05a5af7457d5d6e
Wed Feb 22 19:07:03 2017 +0100 ; refactor prediction parsers ; https://github.com/lindenb/jvarkit/commit/dc7f7797c60d63cd09d3b7712fb81033cd7022cb
Fri Jan 22 23:49:23 2016 +0100 ; vcfiterator is now an interface ; https://github.com/lindenb/jvarkit/commit/9f9b9314c4b31b21044c5911a7e79e1b3fb0af7a
Mon Jan 18 16:58:08 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/83f80fdbe8d6be71539cfdbf60d61ce7ead9c0fd
Mon Jun 1 15:27:11 2015 +0200 ; change getChrom() to getContig() ; https://github.com/lindenb/jvarkit/commit/5abd60afcdc2d5160164ae6e18087abf66d8fcfe
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Thu Dec 12 20:55:10 2013 +0100 ; moved to html ; https://github.com/lindenb/jvarkit/commit/ca74712f3de257ee4b3632a0fdf6dfeb28338eeb
Thu Dec 12 16:49:38 2013 +0100 ; vcf cut samples & stats ; https://github.com/lindenb/jvarkit/commit/350287a672873076beab8c0e9b2e5b1c39581995
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfstats** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Tip: Adding a new key in the INFO field

Using vcffilterjs :



the script:

```
var ArrayList = Java.type("java.util.ArrayList");
var VariantContextBuilder = Java.type("htsjdk.variant.variantcontext.VariantContextBuilder");


function addInfo(v)
	{
	var vcb = new VariantContextBuilder(v);
	var atts = new ArrayList();
	atts.add(v.getType().name()+ (variant.isFiltered()?"_FILTERED":"_UNFILTERED"));
	atts.add(v.getType().name()+ (variant.hasID()?"_ID":"_NOID"));
	vcb.attribute("MYKEY",atts);
	return vcb.make();
	}


addInfo(variant);
```

run the program, but first use awk to insert the new INFO definition for 'MYKEY'

```
cat input.vcf |\
	awk '/^#CHROM/ {printf("##INFO=<ID=MYKEY,Number=.,Type=String,Description=\"My key\">\n");} {print;}' |\
	java -jar dist/vcffilterjs.jar -f script.js 
```


## Example

```
$ java -jar $< -o tmp --soterms SO:0001818 --soterms SO:0001819 -K ucsc/hg19/database/knownGene_noPrefix.txt.gz input.vcf
$ (cd tmp && make) 
$ ls tmp/
ALL.affectedSamples.png     ALL.countDepthBySample.tsv      ALL.countDistances.png  ALL.geneLoc.tsv  ALL.predictionsBySample.png  ALL.sample2gtype.tsv  Makefile
ALL.affectedSamples.tsv     ALL.countDepth.png              ALL.countDistances.tsv  ALL.maf.png      ALL.predictionsBySample.tsv  ALL.transvers.png
ALL.countAltAlleles.png     ALL.countDepth.tsv              ALL.countIndelSize.png  ALL.maf.tsv      ALL.predictions.png          ALL.transvers.tsv
ALL.countAltAlleles.tsv     ALL.countDistancesBySample.png  ALL.countIndelSize.tsv  ALL.mendel.png   ALL.predictions.tsv          ALL.variant2type.png
ALL.countDepthBySample.png  ALL.countDistancesBySample.tsv  ALL.geneLoc.png         ALL.mendel.tsv   ALL.sample2gtype.png         ALL.variant2type.tsv

$ head -n3 tmp/*.tsv
==> tmp/ALL.affectedSamples.tsv <==
1/57	182265
2/57	98512
3/57	67449

==> tmp/ALL.countAltAlleles.tsv <==
2	11699
3	2406
4	679

==> tmp/ALL.countDepthBySample.tsv <==
Sample	[-Inf/0[	[0/10[	[10/20[	[20/30[	[30/50[	[50/100[	[100/200[	[200/Inf[
10_T1245	0	305229	97739	61972	75791	72779	14008	686
11AG09	0	400147	62261	39411	55254	86883	86192	46749

==> tmp/ALL.countDepth.tsv <==
[0/10[	19435
[10/20[	95369
[20/30[	92378

==> tmp/ALL.countDistancesBySample.tsv <==
Sample	[-Inf/0[	0	1	2	3	4	5	6	7	8	9	[10/20[	[20/100[	[100/200[	[200/300[	[300/400[	[400/500[	[500/1000[	[1000/Inf[
S1	0	0	2643	1755	1491	1430	1156	1106	893	858	867	6826	26696	18252	12411	9348	7270	20975	85769
S2	0	0	3632	2397	1967	1855	1516	1425	1246	1179	1109	8917	36006	25455	18652	14951	11912	37828	103341

==> tmp/ALL.countDistances.tsv <==
1	16275
2	11184
3	8505

==> tmp/ALL.countIndelSize.tsv <==
2	59420
3	22727
4	10216

==> tmp/ALL.geneLoc.tsv <==
first_exon	51180
internal_exon	70074
last_exon	75341

==> tmp/ALL.maf.tsv <==
0.029411764705882353	0.027777777777777776
0.5	0.43333333333333335
0.05263157894736842	0.0

==> tmp/ALL.mendel.tsv <==
Sample	synonymous_variant	protein_altering_variant
10_T1245	0	0
11AG09	170	298

==> tmp/ALL.predictionsBySample.tsv <==
Sample	synonymous_variant	protein_altering_variant
S1	13453	14527
S2	12820	14077

==> tmp/ALL.predictions.tsv <==
protein_altering_variant	59516
synonymous_variant	47454

==> tmp/ALL.sample2gtype.tsv <==
Sample	NO_CALL	HOM_REF	HET	HOM_VAR	UNAVAILABLE	MIXED
S1	345776	429002	98825	100945	0	0
S2	196925	504211	132576	140836	0	0

==> tmp/ALL.transvers.tsv <==
TYPE	TRANSITION	TRANSVERSION
ALL	581537	133472
CDS	533340	120720

==> tmp/ALL.variant2type.tsv <==
Type	Count
INDEL	126219
SNP	846677

```



