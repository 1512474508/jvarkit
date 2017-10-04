# Biostar103303

Calculate Percent Spliced In (PSI).


## Usage

```
Usage: biostar103303 [options] Files
  Options:
  * -g, --gtf
      GTF file
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


## See also in Biostars

 * [https://www.biostars.org/p/103303](https://www.biostars.org/p/103303)


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
$ make biostar103303
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar103303.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar103303.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Thu May 11 16:20:27 2017 +0200 ; move to jcommander ; https://github.com/lindenb/jvarkit/commit/15b6fabdbdd7ce0d1e20ca51e1c1a9db8574a59e
Fri May 5 15:06:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/4d2bbfed84609bdf14eb1b14a35ab24eb8ad5b26
Thu Apr 6 18:34:56 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/883b4ba4b693661663694256f16b137e371147fa
Thu Nov 26 17:41:15 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/899c60335614350d463be66ec21e994b34dc55be
Fri Oct 2 16:11:52 2015 +0200 ; bioalci ; https://github.com/lindenb/jvarkit/commit/5506708d3f20dc4a6f6b9d43805f9722d47582b5
Tue Jul 21 17:15:14 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/cc3230eabbfb7c2c9763528c63c1f42ae1281351
Sat Jun 14 13:08:42 2014 +0200 ; fix103303 ; https://github.com/lindenb/jvarkit/commit/6294613b8eff3419427ddff13a37a84fcffaba21
Thu Jun 12 22:47:56 2014 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/6783353ca2c14435bc4fdef44198d967d5bc02f8
Thu Jun 12 22:39:20 2014 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/88df95b81ced6ceabea3a80a466ccd32c6ef6178
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **biostar103303** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


##Example

```bash
$   curl -s "http://hgdownload-test.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeCshlLongRnaSeq/wgEncodeCshlLongRnaSeqA549CellLongnonpolyaAlnRep1.bam" |\
  java -jar dist/biostar103303.jar -g "http://atgu.mgh.harvard.edu/plinkseq/dist/aux/gencodeBasicV11-hg19.gtf.gz"  > result.tsv
```


