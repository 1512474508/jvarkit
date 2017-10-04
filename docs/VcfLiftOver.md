# VcfLiftOver

Lift-over a VCF file


## Usage

```
Usage: vcfliftover [options] Files
  Options:
    --adaptivematch
      Use adapative liftover minmatch using the ratio between the min allele 
      size and the longest allele size
      Default: false
  * -f, --chain
      LiftOver file.
    --chainvalid
      Ignore LiftOver chain validation
      Default: false
    -check, --check
      Check variant allele sequence is the same on REF
      Default: false
    -x, --failed
      (file.vcf) write variants failing the liftOver here. Optional.
    -failtag, --failtag
      failed INFO tag
      Default: LIFTOVER_FAILED
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --indel, --indels
      do not LiftOver indels
      Default: false
    --info
      remove attribute from INFO on the fly
      Default: []
    -m, --minmatch
      lift over min-match.
      Default: 0.95
    -o, --output
      Output file. Optional . Default: stdout
  * -D, -R, -r, --reference
      Indexed fasta Reference file. This file must be indexed with samtools 
      faidx and with picard CreateSequenceDictionary
    -T, --tag
      INFO tag
      Default: LIFTOVER
    --version
      print version and exit

```


## Keywords

 * vcf
 * liftover


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
$ make vcfliftover
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/liftover/VcfLiftOver.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/liftover/VcfLiftOver.java)


<details>
<summary>Git History</summary>

```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Wed May 17 14:09:36 2017 +0200 ; fix typo bioalcidae ; https://github.com/lindenb/jvarkit/commit/9db2344e7ce840df02c5a7b4e2a91d6f1a5f2e8d
Sat Apr 29 18:45:47 2017 +0200 ; partition ; https://github.com/lindenb/jvarkit/commit/7d72633d50ee333fcad0eca8aaa8eec1a475cc4d
Thu Apr 27 17:22:22 2017 +0200 ; cont jcommander ; https://github.com/lindenb/jvarkit/commit/0a27a246a537d2b48201596067652ea26bfc28d6
Tue Apr 25 19:03:54 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/049149783826a89f69e6e1e442ab1a7988326bf7
Tue Apr 25 17:33:00 2017 +0200 ; fix make ; https://github.com/lindenb/jvarkit/commit/621fdfbe039f474e187e8f5c67ff17b368b77289
Tue Apr 25 16:00:12 2017 +0200 ; fix make ; https://github.com/lindenb/jvarkit/commit/3af5d60f3d722c5638044aa98ba7398abc63fe7b
Tue Apr 25 15:40:45 2017 +0200 ; cont jcommander ; https://github.com/lindenb/jvarkit/commit/16aeb209fda502b60dd75689b85d1304f469775b
Thu Jun 2 09:49:17 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/2ae46b7df29c6f1b66ce5104ea03bf6390db120d
Mon Jun 15 17:24:26 2015 +0200 ; vep as xml base 64 ; https://github.com/lindenb/jvarkit/commit/d629603576c14a970208c9599b39ecb2a8b39994
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Sun Jan 5 16:10:56 2014 +0100 ; vcf set dict ; https://github.com/lindenb/jvarkit/commit/f023bc9b0685266627a260c67813e7b76d42bef1
Sat Dec 28 16:08:15 2013 +0100 ; lift over ; https://github.com/lindenb/jvarkit/commit/543e55dea8caef38bd3bb8b726b04258d8b6f1c2
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfliftover** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


