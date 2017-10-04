# VcfCalledWithAnotherMethod

Compare one vcf with other , add a flag to tell if a variant was called with another method. Vcf must be sorted on the same Dict.


## Usage

```
Usage: vcfcalledwithanothermethod [options] Files
  Options:
    --filter
      FILTER name: the variant was NOT found in another VCF 
      (CONTIG/POS/REF/at-least-one-ALT). Empty: no filter
      Default: VariantNotFoundElseWhere
    --foundCount
      INFO name for the file identifiers where a variant was found
      Default: FOUND_COUNT
    --foundKey
      INFO name for the file identifiers where a variant was found
      Default: FOUND_KEY
    --gtDiscordant
      FORMAT name for the number of time we didn't find the same genotype
      Default: COUNT_DISCORDANT
    --gtSame
      FORMAT name for the number of time we found the same genotype
      Default: COUNT_SAME
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --nocallhomref
      NO_CALL is same as HOM_REF
      Default: false
    -o, --output
      Output file. Optional . Default: stdout
    -f, --vcfs
      Add alternate VCF files. File ending with '.list' will be interpreted as 
      a list of path of vcf.
      Default: []
    --version
      print version and exit

```


## Keywords

 * vcf
 * compare
 * concordance


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
$ make vcfcalledwithanothermethod
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfCalledWithAnotherMethod.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfCalledWithAnotherMethod.java)


<details>
<summary>Git History</summary>

```
Thu Jun 29 17:31:10 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1aac040bed918f89b1ce68b2c8f7a0c6d5cfddd0
Fri Jun 23 16:37:51 2017 +0200 ; alt vs homref ; https://github.com/lindenb/jvarkit/commit/9ef5f8c8d0b33994515b0faac60e84b275ab34eb
Thu Jun 15 15:30:26 2017 +0200 ; update vcfcalledwithanothermethod, vcfucsc ; https://github.com/lindenb/jvarkit/commit/0efbf47c1a7be8ee9b0a6e2e1dbfd82ae0f8508f
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Mon May 22 17:20:59 2017 +0200 ; moving to jcommaner ; https://github.com/lindenb/jvarkit/commit/60cbfa764f7f5bacfdb78e48caf8f9b66e53a6a0
Fri May 5 15:06:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/4d2bbfed84609bdf14eb1b14a35ab24eb8ad5b26
Fri Feb 12 17:17:38 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/c613240c7f1a266ee7e60083ac906c24588bb4f5
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfcalledwithanothermethod** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example


```make
SHELL=/bin/bash

define ff
dir1/dir2/sample_variations.$(1).annotations.vcf.gz
endef

all :
	java -jar  dist/vcfcalledwithanothermethod.jar \
		-f $(call ff,samtools) \
		-f $(call ff,varscan) \
		-f $(call ff,freebayes) \
			$(call ff,gatkHapCaller)
	
```

output:

```
(...)
1	12718	.	G	C	197.77	VariantNotFoundElseWhere	AC=1;AF=0.500;AN=2;BaseQRankSum=-1.418;ClippingRankSum=1.220;DP=22;QD=8.99;ReadPosRankSum=1.022;SEGDUP=1:10485-19844,1:10464-40733,1:10000-19844,1:10485-40733,1:10000-87112,1:10000-20818	GT:AD:COUNT_DISCORDANT:COUNT_SAME:DP:GQ:PL	0/1:12,10:0:0:22:99:226,0,286
1	23119	.	T	G	637.77	PASS	FOUND_COUNT=2;FOUND_KEY=sample_variations.varscan.annotations,sample_variations.samtools.annotations;FS=34.631;GERP_SCORE=-0.558;MLEAC=1;MLEAF=0.500;MQ=25.98;MQ0=0;MQRankSum=-2.888;POLYX=1;PRED=uc010nxq.1|||||intron_variant;QD=18.22;ReadPosRankSum=1.634;SEGDUP=1:10485-19844,1:10464-40733,1:10000-19844,1:10485-40733,1:10000-87112,1:10000-20818	GT:AD:COUNT_DISCORDANT:COUNT_SAME:DP:GQ:PL	0/1:17,18:0:2:35:99:666,0,727

```



