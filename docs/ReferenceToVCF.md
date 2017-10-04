# ReferenceToVCF

Creates a VCF containing all the possible substitutions from a Reference Genome.


## Usage

```
Usage: referencetovcf [options] Files
  Options:
    -L, --bed
      limit to this BED
    -d, --deletions
      generate deletions
      Default: 0
    -A, --disjoint
      disjoint ALT
      Default: false
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -i, --insertions
      generate insertions
      Default: 0
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * vcf
 * reference
 * fasta


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
$ make referencetovcf
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/ReferenceToVCF.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/ReferenceToVCF.java)


<details>
<summary>Git History</summary>

```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Mon Apr 10 17:44:58 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1a3303b52707e9ba8c9b913e0f82d2735698d24e
Thu Sep 11 17:17:39 2014 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/59b009e383f94d8f697b91fc7b2b0b08a2969f8a
Wed Sep 10 16:23:21 2014 +0200 ; Tool to generate all mutations in a reference ; https://github.com/lindenb/jvarkit/commit/c53e80ff6f6cf3d3cc30d555fa26f36b53669a62
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **referencetovcf** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


