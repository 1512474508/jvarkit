# FindNewSpliceSites

use the 'N' operator in the cigar string to find unknown splice sites


## Usage

```
Usage: findnewsplicesites [options] Files
  Options:
    --bamcompression
      Compression Level.
      Default: 5
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -out, --out
      Output file. Optional . Default: stdout
    --samoutputformat
      Sam output format.
      Default: TypeImpl{name='SAM', fileExtension='sam', indexExtension='null'}
    --version
      print version and exit
    -d
      max distance between known splice site and cigar end
      Default: 10
    -k
      UCSC knownGene URI. Beware chromosome names are formatted the same as 
      your REFERENCE. A typical KnownGene file is 
      http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz 
      .If you only have a gff file, you can try to generate a knownGene file 
      with [http://lindenb.github.io/jvarkit/Gff2KnownGene.html](http://lindenb.github.io/jvarkit/Gff2KnownGene.html)
      Default: http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz

```


## Keywords

 * rnaseq
 * splice


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
$ make findnewsplicesites
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/rnaseq/FindNewSpliceSites.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/rnaseq/FindNewSpliceSites.java)


<details>
<summary>Git History</summary>

```
Thu Jun 15 15:30:26 2017 +0200 ; update vcfcalledwithanothermethod, vcfucsc ; https://github.com/lindenb/jvarkit/commit/0efbf47c1a7be8ee9b0a6e2e1dbfd82ae0f8508f
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Fri May 12 19:41:30 2017 +0200 ; fix make, empty doc ; https://github.com/lindenb/jvarkit/commit/52fcf6d46a779fd7153ebc032fae643d2e266e7e
Tue Apr 25 15:40:45 2017 +0200 ; cont jcommander ; https://github.com/lindenb/jvarkit/commit/16aeb209fda502b60dd75689b85d1304f469775b
Tue May 31 18:35:56 2016 +0200 ; fix interval maps ; https://github.com/lindenb/jvarkit/commit/f6afe341076b9da7c63c33b64c96d15d6cc7b596
Fri Jun 5 12:42:21 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/cc909f9f4ceea181bb65e4203e3fdbde176c6f2f
Mon Jun 1 15:27:11 2015 +0200 ; change getChrom() to getContig() ; https://github.com/lindenb/jvarkit/commit/5abd60afcdc2d5160164ae6e18087abf66d8fcfe
Fri May 23 15:32:54 2014 +0200 ; continue move to htsjdk ; https://github.com/lindenb/jvarkit/commit/b5a8a3bce5ecd952abffb7aae6223d1e03a9809e
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 14:06:30 2014 +0200 ; continue moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/011f098b6402da9e204026ee33f3f89d5e0e0355
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Wed Apr 9 18:05:31 2014 +0200 ; entropy fastq ; https://github.com/lindenb/jvarkit/commit/3dcd5d20d91258f8e332c666944ebb71c38495cd
Wed Apr 9 12:50:50 2014 +0200 ; bwa mem convert cigar to N ; https://github.com/lindenb/jvarkit/commit/dac74ffe48ee2fbf391a9e8e0056e9b8ee18babc
Wed Apr 2 17:50:50 2014 +0200 ; cont rnaseq ; https://github.com/lindenb/jvarkit/commit/7b3f7e13a112b09018284931678ac78dd32cefcc
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **findnewsplicesites** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)



## Example

```bash
$  java -jar dist/findnewsplicesites.jar \
     -k http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz \
      hg19.bam > out.sam
```


