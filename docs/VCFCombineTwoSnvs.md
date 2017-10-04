# VCFCombineTwoSnvs

Detect Mutations than are the consequences of two distinct variants. This kind of variant might be ignored/skipped from classical variant consequence predictor. Idea from @SolenaLS and then @AntoineRimbert


## Usage

```
Usage: vcfcombinetwosnvs [options] Files
  Options:
    -B, --bam
      Optional indexed BAM file used to get phasing information. This can be a 
      list of bam if the filename ends with '.list'
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
  * -k, --knownGene
      UCSC knownGene URI. Beware chromosome names are formatted the same as 
      your REFERENCE. A typical KnownGene file is 
      http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz 
      .If you only have a gff file, you can try to generate a knownGene file 
      with [http://lindenb.github.io/jvarkit/Gff2KnownGene.html](http://lindenb.github.io/jvarkit/Gff2KnownGene.html)
      Default: http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz
    --maxRecordsInRam
      When writing  files that need to be sorted, this will specify the number 
      of records stored in RAM before spilling to disk. Increasing this number 
      reduces the number of file  handles needed to sort a file, and increases 
      the amount of RAM needed
      Default: 50000
    -o, --output
      Output file. Optional . Default: stdout
  * -R, --reference
      Indexed fasta Reference file. This file must be indexed with samtools 
      faidx and with picard CreateSequenceDictionary
    --tmpDir
      tmp working directory. Default: java.io.tmpDir
      Default: []
    --version
      print version and exit

```


## Keywords

 * vcf
 * annotation
 * prediction
 * protein


## Compilation

### Requirements / Dependencies

* java compiler SDK 1.8 http://www.oracle.com/technetwork/java/index.html (**NOT the old java 1.7 or 1.6**) . Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make vcfcombinetwosnvs
```

The *.jar libraries are not included in the main jar file, so you shouldn't move them (https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfannot/VCFCombineTwoSnvs.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfannot/VCFCombineTwoSnvs.java)


<details>
<summary>Git History</summary>

```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Thu May 18 18:34:07 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/89cb7d10eaeef051af30f1043698546f555cbcd8
Wed May 17 14:09:36 2017 +0200 ; fix typo bioalcidae ; https://github.com/lindenb/jvarkit/commit/9db2344e7ce840df02c5a7b4e2a91d6f1a5f2e8d
Mon May 15 12:10:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/b4895dd40d1c34f345cd2807f7a81395ba27e8ee
Sun May 7 13:21:47 2017 +0200 ; rm xml ; https://github.com/lindenb/jvarkit/commit/f37088a9651fa301c024ff5566534162bed8753d
Thu Apr 20 17:17:22 2017 +0200 ; continue transition jcommander ; https://github.com/lindenb/jvarkit/commit/fcf5def101925bea9ddd001d8260cf65aa52d6a0
Tue May 31 18:35:56 2016 +0200 ; fix interval maps ; https://github.com/lindenb/jvarkit/commit/f6afe341076b9da7c63c33b64c96d15d6cc7b596
Thu Mar 3 16:37:41 2016 +0100 ; matilde compte les nocall comme homref ; https://github.com/lindenb/jvarkit/commit/a1f328bfccff81e8f4736827d9755a79cf6e2829
Fri Feb 19 17:36:28 2016 +0100 ; .github dir , filter in js , grantham score ; https://github.com/lindenb/jvarkit/commit/94d74481e3e488aeb890235cee47c595229cf18b
Fri Feb 19 09:41:06 2016 +0100 ; added option -Filter to vcffilterjs ; https://github.com/lindenb/jvarkit/commit/0c40527a6f551d0685612994659df3151a3c2388
Fri Feb 19 09:23:31 2016 +0100 ; version trois snp ; https://github.com/lindenb/jvarkit/commit/1834f8eb5898ed20a0849adf5603ea0ff9fb2daa
Wed Feb 17 17:27:36 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/53a4e2e4fec16449c2bb1b3061a0d47abf695807
Tue Feb 16 16:19:18 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/4453c9806caa4284eb3183d38b2aa35705bbe7c7
Mon Feb 15 16:29:54 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/1d908a20a451136a2888d1b58e8efc44914510f3
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfcombinetwosnvs** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)






### Output



#### Example



```

##fileformat=VCFv4.2
##FILTER=<ID=TwoStrands,Description="(number of reads carrying both mutation) < (reads carrying variant 1 + reads carrying variant 2)">
##INFO=<ID=CodonVariant,Number=.,Type=String,Description="Variant affected by two distinct mutation. Format is defined in the INFO column. INFO_AC:Allele count in genotypes, for each ALT allele, in the same order as listed.INFO_AF:Allele Frequency, for each ALT allele, in the same order as listed.INFO_MLEAC:Maximum likelihood expectation (MLE) for the allele counts (not necessarily the same as the AC), for each ALT allele, in the same order as listed.INFO_MLEAF:Maximum likelihood expectation (MLE) for the allele frequency (not necessarily the same as the AF), for each ALT allele, in the same order as listed.">
##VCFCombineTwoSnvsCmdLine=-k jeter.knownGene.txt -tmpdir tmp/ -R /commun/data/pubdb/broadinstitute.org/bundle/1.5/b37/human_g1k_v37.fasta -B /commun/data/projects/plateforme/NTS-017_HAL_Schott_mitral/20141106/align20141106/Samples/CD13314/BAM/Haloplex20141106_CD13314_final.bam
##VCFCombineTwoSnvsHtsJdkHome=/commun/data/packages/htsjdk/htsjdk-2.0.1
##VCFCombineTwoSnvsHtsJdkVersion=2.0.1
##VCFCombineTwoSnvsVersion=c5af7d1bd367562b3578d427d24ec62856835d38
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
1	120612013	rs200646249	G	A	.	.	CodonVariant=CHROM|1|REF|G|TRANSCRIPT|uc001eil.3|cDdnaPos|8|CodonPos|7|CodonWild|GCC|AAPos|3|AAWild|A|POS1|120612013|ID1|rs200646249|PosInCodon1|2|Alt1|A|Codon1|GTC|AA1|V|INFO_MLEAC_1|1|INFO_AC_1|1|INFO_MLEAF_1|0.500|INFO_AF_1|0.500|POS2|120612014|ID2|.|PosInCodon2|1|Alt2|A|Codon2|TCC|AA2|S|INFO_MLEAC_2|1|INFO_AC_2|1|INFO_MLEAF_2|0.500|INFO_AF_2|0.500|CombinedCodon|TTC|CombinedAA|F|CombinedSO|nonsynonymous_variant|CombinedType|combined_is_new|N_READS_BOTH_VARIANTS|168|N_READS_NO_VARIANTS|1045|N_READS_TOTAL|1213|N_READS_ONLY_1|0|N_READS_ONLY_2|0,CHROM|1|REF|G|TRANSCRIPT|uc001eik.3|cDdnaPos|8|CodonPos|7|CodonWild|GCC|AAPos|3|AAWild|A|POS1|120612013|ID1|rs200646249|PosInCodon1|2|Alt1|A|Codon1|GTC|AA1|V|INFO_MLEAC_1|1|INFO_AC_1|1|INFO_MLEAF_1|0.500|INFO_AF_1|0.500|POS2|120612014|ID2|.|PosInCodon2|1|Alt2|A|Codon2|TCC|AA2|S|INFO_MLEAC_2|1|INFO_AC_2|1|INFO_MLEAF_2|0.500|INFO_AF_2|0.500|CombinedCodon|TTC|CombinedAA|F|CombinedSO|nonsynonymous_variant|CombinedType|combined_is_new|N_READS_BOTH_VARIANTS|168|N_READS_NO_VARIANTS|1045|N_READS_TOTAL|1213|N_READS_ONLY_1|0|N_READS_ONLY_2|0;EXAC03_AC_NFE=641;EXAC03_AN_NFE=48948
1	120612014	.	C	A	.	.	CodonVariant=CHROM|1|REF|C|TRANSCRIPT|uc001eik.3|cDdnaPos|7|CodonPos|7|CodonWild|GCC|AAPos|3|AAWild|A|POS1|120612014|ID1|.|PosInCodon1|1|Alt1|A|Codon1|TCC|AA1|S|INFO_MLEAC_1|1|INFO_AC_1|1|INFO_MLEAF_1|0.500|INFO_AF_1|0.500|POS2|120612013|ID2|rs200646249|PosInCodon2|2|Alt2|A|Codon2|GTC|AA2|V|INFO_MLEAC_2|1|INFO_AC_2|1|INFO_MLEAF_2|0.500|INFO_AF_2|0.500|CombinedCodon|TTC|CombinedAA|F|CombinedSO|nonsynonymous_variant|CombinedType|combined_is_new|N_READS_BOTH_VARIANTS|168|N_READS_NO_VARIANTS|1045|N_READS_TOTAL|1213|N_READS_ONLY_1|0|N_READS_ONLY_2|0,CHROM|1|REF|C|TRANSCRIPT|uc001eil.3|cDdnaPos|7|CodonPos|7|CodonWild|GCC|AAPos|3|AAWild|A|POS1|120612014|ID1|.|PosInCodon1|1|Alt1|A|Codon1|TCC|AA1|S|INFO_MLEAC_1|1|INFO_AC_1|1|INFO_MLEAF_1|0.500|INFO_AF_1|0.500|POS2|120612013|ID2|rs200646249|PosInCodon2|2|Alt2|A|Codon2|GTC|AA2|V|INFO_MLEAC_2|1|INFO_AC_2|1|INFO_MLEAF_2|0.500|INFO_AF_2|0.500|CombinedCodon|TTC|CombinedAA|F|CombinedSO|nonsynonymous_variant|CombinedType|combined_is_new|N_READS_BOTH_VARIANTS|168|N_READS_NO_VARIANTS|1045|N_READS_TOTAL|1213|N_READS_ONLY_1|0|N_READS_ONLY_2|0;EXAC03_AC_NFE=640;EXAC03_AN_NFE=48228

```





#### Fields

```
KEYEXAMPLEDescription
CHROM1Chromosome for current variant.
REFCReference Allele for current variant
TRANSCRIPTuc001eik.3UCSC knownGene Transcript
cDdnaPos7+1 based position in cDNA
CodonPos7+1 based position of the codon in cNA
CodonWildGCCWild codon
AAPos3+1 based position of amino acid
AAWildAWild amino acid
POS1120612014+1 based position of variant 1
ID1.RS ID of variant 1
PosInCodon11Position in codon (1,2,3) of variant 1
Alt1AAlternate allele of variant 1
Codon1TCC Codon with variant 1 alone
AA1SAmino acid prediction for variant 1
INFO_*_11Data about alternate allele 1 taken out of original VCF
POS2120612013+1 based position of variant 1
ID2rs200646249RS ID of variant 1
PosInCodon22Position in codon (1,2,3) of variant 2
Alt2AAlternate allele of variant 2
Codon2GTC Codon with variant 2alone
AA2VAmino acid prediction for variant 2
INFO_*_21Data about alternate allele 2 taken out of original VCF
CombinedCodonTTCCombined codon with ALT1 and ALT2
CombinedAAFCombined amino acid with ALT1 and ALT2
CombinedSOnonsynonymous_variantSequence Ontology term
CombinedTypecombined_is_newtype of new mutation
N_READS_BOTH_VARIANTS168Number of reads carrying both variants
N_READS_NO_VARIANTS1045Number of reads carrying no variants
N_READS_TOTAL1213Total Number of reads
N_READS_ONLY_10Number of reads carrying onlt variant 1
N_READS_ONLY_20Number of reads carrying onlt variant 2
```



### See also

http://bmcresnotes.biomedcentral.com/articles/10.1186/1756-0500-5-615




