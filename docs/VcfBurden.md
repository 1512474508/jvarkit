# VcfBurden

Solena: vcf to (chrom/pos/ref/alt/individus(G:0/1/2/-9)


## DEPRECATED

deprecated

## Usage

```
Usage: vcfburden [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      zip file
    --version
      print version and exit
    -H
       only high damage
      Default: false
    -d
       (dir) base zip dir
      Default: burden
    -f
       print ALL consequence+ colum, SO terms (mail matilde 11/10/2015 11:38 
      AM) 
      Default: false
    -g
       (file) optional list of gene names (restrict genes, print genes without 
      data) 
    -p
       print position in CDS
      Default: false
    -q
       print VQSLOD
      Default: false

```

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
$ make vcfburden
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfBurden.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfBurden.java)


<details>
<summary>Git History</summary>

```
Mon May 22 17:20:59 2017 +0200 ; moving to jcommaner ; https://github.com/lindenb/jvarkit/commit/60cbfa764f7f5bacfdb78e48caf8f9b66e53a6a0
Thu May 4 13:06:07 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/b2f8f945cb8838c0289a7d850ce24603417eccde
Wed Feb 22 19:07:03 2017 +0100 ; refactor prediction parsers ; https://github.com/lindenb/jvarkit/commit/dc7f7797c60d63cd09d3b7712fb81033cd7022cb
Thu Mar 3 16:37:41 2016 +0100 ; matilde compte les nocall comme homref ; https://github.com/lindenb/jvarkit/commit/a1f328bfccff81e8f4736827d9755a79cf6e2829
Wed Feb 17 17:27:36 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/53a4e2e4fec16449c2bb1b3061a0d47abf695807
Wed Feb 10 11:51:22 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/c9eba81fa1d3a37186e5747a8ead116db835374b
Wed Feb 3 14:41:34 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/dea087858303eb791d6e68178742f1fbae2092f0
Tue Jan 26 14:46:11 2016 +0100 ; Merge branch 'master' of https://github.com/lindenb/jvarkit ; https://github.com/lindenb/jvarkit/commit/f65c284b3348597052f6d038403a708c33cd963c
Tue Jan 26 14:46:03 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/4ba62aa01f468dfc32d3289a10189ea4507362f3
Tue Jan 26 14:45:35 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/5201fb71870100bc4e4f40acf09d4e5f31ec4182
Mon Jan 18 16:58:08 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/83f80fdbe8d6be71539cfdbf60d61ce7ead9c0fd
Wed Jan 13 15:25:58 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/db4a0f749e0c5b5a0ba067c7f4e89392ea6b62c3
Mon Dec 14 18:23:03 2015 +0100 ; vcf burden ; https://github.com/lindenb/jvarkit/commit/5bf5e55768acffd5fa87dd682dc47e4c3e7fdd4b
Mon Dec 14 18:18:06 2015 +0100 ; vcf burden ; https://github.com/lindenb/jvarkit/commit/155d4bded8ea17cb8595fc13c99b4851966b455b
Mon Dec 14 17:18:02 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/9b271459821d8061aa07e98bc7f30232597f47c9
Wed Dec 9 10:00:26 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/926828692647bf204553d2c968447a0c625769c7
Tue Nov 24 16:06:19 2015 +0100 ; fix https://github.com/lindenb/jvarkit/issues/36 ; https://github.com/lindenb/jvarkit/commit/eac04e587d9e0f784dd1a00c2d1245891a537568
Mon Nov 16 12:21:53 2015 +0100 ; blastfilterjs ; https://github.com/lindenb/jvarkit/commit/6b885d465b47f339d323f909c2ae7a88641f08a4
Tue Oct 6 17:27:21 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/35fed6f953545afc1b47f1e4b6dc32f5837646c5
Thu Jul 9 17:51:16 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/4f797a9fbf2c3ceac9cec3c431c719ad794953c2
Tue Jul 7 16:03:42 2015 +0200 ; pcr slice reads ; https://github.com/lindenb/jvarkit/commit/fc442787c5e74077f0c7256750480b05b4b93317
Mon Jul 6 16:14:07 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/ee95fe6971b5655c61d7feb22e8fa877201a9ca6
Wed Jul 1 19:37:31 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/0c182acaac09c876387bbb4d0777fd6596284665
Tue Jun 30 17:45:37 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/7c0d31b60217243a99bc4e1ea0045c9f885ba9bd
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfburden** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


