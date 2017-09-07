# Bam2Raster

BAM to raster graphics


## Usage

```
Usage: bam2raster [options] Files
  Options:
    -clip, --clip
      Show clipping
      Default: false
    -depth, --depth
      Depth track height.
      Default: 100
    --groupby
      Group Reads by
      Default: sample
      Possible Values: [readgroup, sample, library, platform, center, sample_by_platform, sample_by_center, sample_by_platform_by_center, any]
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --highlight
      hightligth those positions.
      Default: []
    --mapqopacity
      How to handle the MAPQ/ opacity of the reads. all_opaque: no opacity, 
      handler 1: transparency under MAPQ=60
      Default: handler1
      Possible Values: [all_opaque, handler1]
    --limit, --maxrows
      Limit number of rows to 'N' lines. negative: no limit.
      Default: -1
    -minh, --minh
      Min. distance between two reads.
      Default: 2
    -N, --name
      print read name instead of base
      Default: false
    --noReadGradient
      Do not use gradient for reads
      Default: false
    -nobase, --nobase
      hide bases
      Default: false
    -o, --output
      Output file. Optional . Default: stdout
    -R, --reference
      Indexed fasta Reference file. This file must be indexed with samtools 
      faidx and with picard CreateSequenceDictionary
  * -r, --region
      Restrict to that region. An interval as the following syntax : 
      "chrom:start-end" or "chrom:middle+extend"  or 
      "chrom:start-end+extend".A program might use a Reference sequence to fix 
      the chromosome name (e.g: 1->chr1)
    -srf, --samRecordFilter
      A filter expression. Reads matching the expression will be filtered-out. 
      Empty String means 'filter out nothing/Accept all'. See https://github.com/lindenb/jvarkit/blob/master/src/main/resources/javacc/com/github/lindenb/jvarkit/util/bio/samfilter/SamFilterParser.jj 
      for a complete syntax.
      Default: mapqlt(1) || MapQUnavailable() || Duplicate() || FailsVendorQuality() || NotPrimaryAlignment() || SupplementaryAlignment()
    --spaceyfeature
      number of pixels between features
      Default: 4
    -V, --variants, --vcf
      VCF files used to fill the position to hightlight with POS
      Default: []
    --version
      print version and exit
    -w, --width
      Image width
      Default: 1000

```


## Keywords

 * bam
 * alignment
 * graphics
 * visualization
 * png



## See also in Biostars

 * [https://www.biostars.org/p/252491](https://www.biostars.org/p/252491)


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
$ make bam2raster
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bam2graphics/Bam2Raster.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bam2graphics/Bam2Raster.java)

Git History for this file:
```
Wed Jun 14 17:01:36 2017 +0200 ; fast genotype gvcf ; https://github.com/lindenb/jvarkit/commit/d77e93940ad9a7f8144527332067b663b55a10f6
Wed May 24 12:24:00 2017 +0200 ; lowres bam2raster ; https://github.com/lindenb/jvarkit/commit/28e8b5122b13f7cd55b2ca0894c59f0f295b1f3e
Tue May 23 23:01:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/3e06d9a32812f3e48cfd92d9e1031fff5ddbc383
Tue May 23 18:35:23 2017 +0200 ; lowres bam2raster ; https://github.com/lindenb/jvarkit/commit/e39a8f964b4bb11b28700c37ce1f2a7ba16b4653
Sat May 13 16:29:30 2017 +0200 ; alpha bam2raster ; https://github.com/lindenb/jvarkit/commit/460cb09e79dfa5207d6bd38f79aa5f9fed158663
Fri May 12 19:58:46 2017 +0200 ; fix null error ; https://github.com/lindenb/jvarkit/commit/ea996904bbf309f55b0837f7e43a1f5509bfc575
Fri May 12 12:31:52 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/79b31100024fed64156ce4e1796507814c20ebf1
Thu May 11 10:59:12 2017 +0200 ; samcolortag ; https://github.com/lindenb/jvarkit/commit/dfd3239dc49af52966e2259bf0a5f52dd34aac8e
Wed May 10 20:57:52 2017 +0200 ; YC tag ; https://github.com/lindenb/jvarkit/commit/a9515d969d27c76ccd0814a093e886d71904b0f2
Wed May 10 17:47:49 2017 +0200 ; samcolortag ; https://github.com/lindenb/jvarkit/commit/d02cc5469b98d2719ec93887788ce9c1cc181e08
Wed May 10 16:28:42 2017 +0200 ; update bam2raster ; https://github.com/lindenb/jvarkit/commit/5410b2f94a34a9f566ab58054b4b37e08b140169
Wed May 10 15:18:14 2017 +0200 ; bam2raster ; https://github.com/lindenb/jvarkit/commit/4f59fa50eb3be1389338e45302515fb4efd49eb5
Fri Apr 7 16:35:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/54c5a476e62e021ad18e7fd0d84bf9e5396c8c96
Sat Aug 27 12:05:09 2016 +0200 ; fix bug https://github.com/lindenb/jvarkit/issues/60 ; https://github.com/lindenb/jvarkit/commit/b51fe8f30c1ab3f7ec477a95786a5359f3e078a3
Thu Jul 7 17:21:44 2016 +0200 ; json, filterjs,... ; https://github.com/lindenb/jvarkit/commit/96ec92c986ddd3a75ab1a9b72b12e82b0df50959
Wed Dec 9 21:05:44 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/9a94c74ff2bbc322bcc0145e8d488ec8175065ec
Tue Nov 3 22:42:18 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/4e4a9319be20626f0ea01dc2316c6420ba8e7dac
Thu Sep 24 13:25:17 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/45943517fc1ae993f0d5c7fe5ee1e7d68fc92afe
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Tue Feb 4 17:51:57 2014 +0100 ; vcfin. Passer chercher du pain avant de rentrer ; https://github.com/lindenb/jvarkit/commit/6902c2223643e5f97eb5d276eeeead6c58f3a081
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Tue Dec 3 17:50:17 2013 +0100 ; bam2wig ; https://github.com/lindenb/jvarkit/commit/25835dce4a1c841d0805416b9c389699060cf578
Tue Nov 26 12:29:03 2013 +0100 ; unclipped start -> align start ; https://github.com/lindenb/jvarkit/commit/3944b21281c2b4afc1ef682f0abe020b26940e37
Fri Nov 8 12:16:42 2013 +0100 ; improve bam2raster ; https://github.com/lindenb/jvarkit/commit/424612723711516e92a338a9e86ecf8d8f0c46fd
Thu Nov 7 22:27:38 2013 +0100 ; enhance hershey font, bam2raster ; https://github.com/lindenb/jvarkit/commit/807970c0a3ac8e28fd5fbfd62b7010d1047e7919
Thu Nov 7 17:53:04 2013 +0100 ; raster ; https://github.com/lindenb/jvarkit/commit/64f88fd28dec8bd72436172b1c0a63054fd9e6f8
Thu Nov 7 13:54:08 2013 +0100 ; continue ; https://github.com/lindenb/jvarkit/commit/2a7db844ef92646208fb98090906fdb21163613d
Wed Nov 6 18:05:09 2013 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/6715aaa4a449f8874e59e16a14b55d4b145f4ffc
Fri Oct 25 17:42:45 2013 +0200 ; close Reference Fasta (picard.100) ; https://github.com/lindenb/jvarkit/commit/9c4a6831016175308ec9a80539e2093c32e78af9
Sun Jun 9 22:34:19 2013 +0200 ; hershey ; https://github.com/lindenb/jvarkit/commit/e7d9cfe298767e6098636b943cf03017a591ad2c
Fri Jun 7 17:30:06 2013 +0200 ; screenshot bam2raster ; https://github.com/lindenb/jvarkit/commit/0900d0b4de98fa82d7f5a548fe692b10f9c32e85
Fri Jun 7 14:29:12 2013 +0200 ; raster image ; https://github.com/lindenb/jvarkit/commit/bf71f1d43af2e7bde6ed67dbcf7364a4397c9aa6
```

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **bam2raster** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Examples


### Example 1

```
java -jar dist/bam2raster.jar \
	-o ~/jeter.png \
        -r 2:17379500-17379550 \
        -R  human_g1k_v37.fasta \
        sample.bam
```

### Example 2

```
java -jar dist/bam2raster.jar -R ref.fa -r rotavirus:150-200 data/*.bam -o out.png --limit 10 --clip  --noReadGradient  --highlight 175 
```
## Misc

I use the UCSC/IGV color tag 'YC' when available (see also samcolortag)

## Screenshots

<img src="https://raw.github.com/lindenb/jvarkit/master/doc/bam2graphics.png"/>

<img src="https://pbs.twimg.com/media/C_eTeXtW0AAAC-v.jpg"/>



## Example

```
$ java -jar  dist/bam2raster.jar -r "scf7180000354095:168-188"   \
	-o pit.png \
	-R  scf_7180000354095.fasta  scf7180000354095.bam 
	
	
```

batch:

```makefile
POS=1|123 2|345 3|456
IMAGES=
BAMS=	S1|f1.bam \
	S2|f2.bam \
	S3|f3.bam
	

define run
$(1)_$(3)_$(4).png: $(2)
	java -jar dist/bam2raster.jar -clip --highlight $(4)  --mapqopacity handler1 --nobase -r "chr$(3):$(4)+50"   --reference /commun/data/pubdb/broadinstitute.org/bundle/1.5/b37/human_g1k_v37_prefix.fasta -o $$@ $$<

IMAGES+=$(1)_$(3)_$(4).png

endef

all: all2

$(eval $(foreach P,$(POS),$(foreach B,$(BAMS),$(call run,$(word 1, $(subst |, ,${B})),$(word 2, $(subst |, ,${B})),$(word 1, $(subst |, ,${P})),$(word 2, $(subst |, ,${P}))))))

all2: ${IMAGES}
	rm -f jeter.zip
	zip jeter.zip $^
```

## screenshots

![https://raw.github.com/lindenb/jvarkit/master/doc/bam2graphics.png](https://raw.github.com/lindenb/jvarkit/master/doc/bam2graphics.png)

![https://pbs.twimg.com/media/BYi0X4_CYAAdXl-.png](https://pbs.twimg.com/media/BYi0X4_CYAAdXl-.png)

![https://pbs.twimg.com/media/C_eTeXtW0AAAC-v.jpg](https://pbs.twimg.com/media/C_eTeXtW0AAAC-v.jpg)

![http://i.imgur.com/lBSpTSW.png](http://i.imgur.com/lBSpTSW.png)




