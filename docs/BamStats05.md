# BamStats05

Coverage statistics for a BED file, group by gene


## Usage

```
Usage: bamstats05 [options] Files
  Options:
  * -B, --bed
      bed file (columns: chrom start end GENE)
    -f, --filter
      A filter expression. Reads matching the expression will be filtered-out. 
      Empty String means 'filter out nothing/Accept all'. See https://github.com/lindenb/jvarkit/blob/master/src/main/resources/javacc/com/github/lindenb/jvarkit/util/bio/samfilter/SamFilterParser.jj 
      for a complete syntax.
      Default: mapqlt(1) || MapQUnavailable() || Duplicate() || FailsVendorQuality() || NotPrimaryAlignment() || SupplementaryAlignment()
    --groupby
      Group Reads by
      Default: sample
      Possible Values: [readgroup, sample, library, platform, center, sample_by_platform, sample_by_center, sample_by_platform_by_center, any]
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -m, --mincoverage
      min coverage to say the position is not covered
      Default: 0
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * bam
 * coverage
 * statistics
 * bed


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
$ make bamstats05
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bamstats04/BamStats05.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bamstats04/BamStats05.java)


<details>
<summary>Git History</summary>

```
Tue Jun 20 15:07:17 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/8e712ff2b8d4b73d71cd2035cfac57381d3e9d4b
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Sat Apr 29 18:45:47 2017 +0200 ; partition ; https://github.com/lindenb/jvarkit/commit/7d72633d50ee333fcad0eca8aaa8eec1a475cc4d
Fri Apr 7 16:35:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/54c5a476e62e021ad18e7fd0d84bf9e5396c8c96
Wed Oct 5 21:00:52 2016 +0200 ; fix https://github.com/lindenb/jvarkit/issues/66 ; https://github.com/lindenb/jvarkit/commit/abee996272e161088c4fd81d4c4d36ad8c5e9f5d
Mon May 30 09:56:31 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/e7b2fe070bf124c8b71611d621a2efb4d0fab90a
Thu Nov 26 17:41:15 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/899c60335614350d463be66ec21e994b34dc55be
Mon Nov 16 12:21:53 2015 +0100 ; blastfilterjs ; https://github.com/lindenb/jvarkit/commit/6b885d465b47f339d323f909c2ae7a88641f08a4
Tue Oct 13 15:46:31 2015 +0200 ; bamstats05 ; https://github.com/lindenb/jvarkit/commit/e6b4b771f21e6973664c336243c814b2342a9cc4
Mon Oct 12 17:40:15 2015 +0200 ; bamstats05 ; https://github.com/lindenb/jvarkit/commit/a68434654d5917076bee6d76c38c4c4555ce4f0f
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **bamstats05** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

```

$ head genes.bed
1	179655424	179655582	ZORG
1	179656788	179656934	ZORG

$ java -jar  dist/bamstats05.jar -B genes.bed --mincoverage 10  > out.txt

$ head out.txt
#chrom	start	end	gene	sample	length	mincov	maxcov	avg	nocoverage.bp	percentcovered
1	179655424	179656934	ZORG	SAMPLE1	304	27	405	216.80921052631578	0	100

```


