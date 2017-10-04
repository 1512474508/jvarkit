# AlleleFrequencyCalculator

Allele Frequency Calculator


## DEPRECATED

Use bioalcidae

## Usage

```
Usage: allelefreqcalc [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * vcf
 * af


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
$ make allelefreqcalc
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/AlleleFrequencyCalculator.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/AlleleFrequencyCalculator.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Mon May 15 10:41:51 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/c13a658b2ed3bc5dd6ade57190e1dab05bf70612
Tue May 9 10:40:20 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/88cfdecb60c1f193ae8b3176ad86181c4a15256b
Sun Apr 30 13:35:22 2017 +0200 ; coverage consensus ; https://github.com/lindenb/jvarkit/commit/fa67b93976fce20feb93d5807dfbb2feb25a2406
Fri Jan 22 23:49:23 2016 +0100 ; vcfiterator is now an interface ; https://github.com/lindenb/jvarkit/commit/9f9b9314c4b31b21044c5911a7e79e1b3fb0af7a
Thu Nov 26 12:58:31 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/cfdff2e66fbeaa4627b50361a09196be2a2e1477
Tue Nov 3 22:42:18 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/4e4a9319be20626f0ea01dc2316c6420ba8e7dac
Wed Sep 23 18:01:13 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/6156d1359a63a80c24f5b7694dc70431f6816289
Mon Jun 1 15:27:11 2015 +0200 ; change getChrom() to getContig() ; https://github.com/lindenb/jvarkit/commit/5abd60afcdc2d5160164ae6e18087abf66d8fcfe
Wed Nov 5 10:45:45 2014 +0100 ; vcf merge fixed ; https://github.com/lindenb/jvarkit/commit/7ddcffc73f823f9e377ffd2a3644cbf50cf26581
Wed Jun 25 17:12:15 2014 +0200 ; allele freq calc ; https://github.com/lindenb/jvarkit/commit/e9707b8c5c1718fb1679cbf6f098371e84d0368c
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **allelefreqcalc** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)





```

 curl -s  "ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/phase1/analysis_results/integrated_call_sets/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes.vcf.gz" | gunzip -c | \
java -jar dist/allelefreqcalc.jar | head

CHR	POS	ID	REF	ALT	TOTAL_CNT	ALT_CNT	FRQ
22	16050408	rs149201999	T	C	2184	134	0.06135531
22	16050612	rs146752890	C	G	2184	184	0.08424909
22	16050678	rs139377059	C	T	2184	113	0.051739927
22	16050984	rs188945759	C	G	2184	5	0.0022893774
22	16051107	rs6518357	C	A	2184	127	0.058150183
22	16051249	rs62224609	T	C	2184	157	0.07188645
22	16051347	rs62224610	G	C	2184	650	0.29761904
22	16051453	rs143503259	A	C	2184	160	0.07326008
22	16051477	rs192339082	C	A	2184	2	9.157509E-4

```





