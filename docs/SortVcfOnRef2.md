# SortVcfOnRef2

Sort a VCF using the internal dictionary or an external reference order (Deprecated: use picard SortVcf).


## DEPRECATED

use picard sortvcf

## Usage

```
Usage: sortvcfonref2 [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --maxRecordsInRam
      When writing  files that need to be sorted, this will specify the number 
      of records stored in RAM before spilling to disk. Increasing this number 
      reduces the number of file  handles needed to sort a file, and increases 
      the amount of RAM needed
      Default: 50000
    -o, --output
      Output file. Optional . Default: stdout
    -R, --reference
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
 * sort


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
$ make sortvcfonref2
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/sortvcfonref/SortVcfOnRef2.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/sortvcfonref/SortVcfOnRef2.java)


<details>
<summary>Git History</summary>

```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Mon May 15 12:10:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/b4895dd40d1c34f345cd2807f7a81395ba27e8ee
Thu May 11 16:48:26 2017 +0200 ; fix make ; https://github.com/lindenb/jvarkit/commit/5bcabe3cfabbd2a4e65167284494376b91f15fe7
Wed Apr 26 17:26:23 2017 +0200 ; cont jcommander ; https://github.com/lindenb/jvarkit/commit/ab6c7b760cd5376e08da24426cede7f84a6b3ae2
Tue Mar 22 17:19:22 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/97e0e23bddd49049c71d56d495d090c0af636670
Mon Jul 6 16:14:07 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/ee95fe6971b5655c61d7feb22e8fa877201a9ca6
Mon Feb 23 16:53:54 2015 +0100 ; Moving code from Ant to Make. NO MAVEN, NO ANT, NO NO NO #tweet ; https://github.com/lindenb/jvarkit/commit/bd5eb283b4e86b42d05a260d87e137930faef4a6
Thu Sep 11 09:36:01 2014 +0200 ; problem with java dataInputSTream: writeUTF requires line.length < SHORt_MAX ; https://github.com/lindenb/jvarkit/commit/19eac4ee36909a730903546b50461de3c19a5c1f
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Fri Jan 17 19:09:22 2014 +0100 ; vcf sort2, treepack ; https://github.com/lindenb/jvarkit/commit/31c8c5d190d447215a81f644f0db04529d481795
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **sortvcfonref2** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


### Deprecated

Use picard SortVcf  http://broadinstitute.github.io/picard/command-line-overview.html#SortVcf.

### Example

```
cat input.vcf |\
   java -jar dist/sortvcfonref2.jar  -R ref.fa |\
   bgzip -c > result.vcf.gz && \
   tabix -p vcf -f result.vcf.gz
```


