# VcfToHilbert

Plot a Hilbert Curve from a VCF file.


## Usage

```
Usage: vcf2hilbert [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --out
      Output file. Optional . Default: stdout
    -r, --radius
      Radius Size
      Default: 3.0
    --version
      print version and exit
    -w, --width
      Image width
      Default: 1000

```


## Keywords

 * vcf
 * image
 * visualization


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
$ make vcf2hilbert
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfToHilbert.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfToHilbert.java)


<details>
<summary>Git History</summary>

```
Tue Jul 18 23:00:49 2017 +0200 ; gen scan in vcfstats, dict ; https://github.com/lindenb/jvarkit/commit/f49878cdedc9fdb934a27f63416cb072e744a7b0
Thu Jun 15 15:30:26 2017 +0200 ; update vcfcalledwithanothermethod, vcfucsc ; https://github.com/lindenb/jvarkit/commit/0efbf47c1a7be8ee9b0a6e2e1dbfd82ae0f8508f
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Mon May 15 12:10:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/b4895dd40d1c34f345cd2807f7a81395ba27e8ee
Thu May 11 16:20:27 2017 +0200 ; move to jcommander ; https://github.com/lindenb/jvarkit/commit/15b6fabdbdd7ce0d1e20ca51e1c1a9db8574a59e
Tue Apr 18 18:26:58 2017 +0200 ; which changes ?? ; https://github.com/lindenb/jvarkit/commit/2d7cf86faca95815601e4bdd516a757c960749a3
Fri Mar 31 17:08:11 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/f78937d19c4b038e69a32fbcfa2aeab8fd8417c6
Tue Apr 12 12:12:59 2016 +0200 ; remove unicode from ./main/java/com/github/lindenb/jvarkit/tools/misc/VcfToHilbert.java https://github.com/lindenb/jvarkit/issues/50 ; https://github.com/lindenb/jvarkit/commit/c072292e4023b7943c99594616496f49241d0031
Tue Jun 9 12:17:32 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/3601851f8d35e25d0130b1cb765c936e53292750
Fri Jun 5 12:42:21 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/cc909f9f4ceea181bb65e4203e3fdbde176c6f2f
Mon Jun 1 15:27:11 2015 +0200 ; change getChrom() to getContig() ; https://github.com/lindenb/jvarkit/commit/5abd60afcdc2d5160164ae6e18087abf66d8fcfe
Mon Jan 12 18:02:41 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/801e96ea74dc515bb5de8dd02f64063c0cd137aa
Wed Dec 3 12:56:00 2014 +0100 ; first hilbert, vcf detect fileformat, label height dans bamcmpdepth ; https://github.com/lindenb/jvarkit/commit/eb3aaf4591d7f520438521edf07751ba6968731c
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcf2hilbert** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


##Example

```bash
$  curl -s "http://ftp.1000genomes.ebi.ac.uk/vol1/ftp/technical/working/20140123_NA12878_Illumina_Platinum/NA12878.wgs.illumina_platinum.20140404.snps_v2.vcf.gz" | gunzip -c |\
 java -jar dist/vcf2hilbert.jar  -r 1.1 -w 1000 -o hilbert.png
```


