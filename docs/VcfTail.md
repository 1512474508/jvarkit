# VcfTail

print the last variants of a vcf


## Usage

```
Usage: vcftail [options] Files
  Options:
    -c, --bycontig
      number of variants
      Default: false
    -n, --count
      number of variants
      Default: 10
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --out
      Output file. Optional . Default: stdout
    --outputbcf
      Output bcf (for streams)
      Default: false
    --vcfcreateindex
      VCF, create tribble or tabix Index when writing a VCF/BCF to a file.
      Default: false
    --vcfmd5
      VCF, create MD5 checksum when writing a VCF/BCF to a file.
      Default: false
    --version
      print version and exit

```


## Keywords

 * vcf


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
$ make vcftail
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfTail.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfTail.java)

Git History for this file:
```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Mon May 22 17:20:59 2017 +0200 ; moving to jcommaner ; https://github.com/lindenb/jvarkit/commit/60cbfa764f7f5bacfdb78e48caf8f9b66e53a6a0
Mon May 1 15:40:19 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/11aa7fcf4cc15aecc2cc2019fc2df8752731a278
Fri Apr 14 15:27:32 2017 +0200 ; annotation proc ; https://github.com/lindenb/jvarkit/commit/72b9383a8472e5a91120bab84d15b8acad4db8d4
Fri Mar 31 17:08:11 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/f78937d19c4b038e69a32fbcfa2aeab8fd8417c6
Mon Mar 27 17:55:22 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/3b7033dcb365493f62d7b78ca4410b6bf3cd716d
Fri Apr 29 17:26:25 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/231856146f1035e21b4adfbc4e87a01b60d0d39e
Wed Feb 10 10:00:55 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/0a518250fb9b2e9894b9329d01afe1dfe0a2f6de
Thu Nov 26 12:58:31 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/cfdff2e66fbeaa4627b50361a09196be2a2e1477
Tue Nov 3 22:42:18 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/4e4a9319be20626f0ea01dc2316c6420ba8e7dac
Wed Sep 23 15:36:21 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/03cc568ed51cf43d7af6f0d913a0f8c52ddfe5d7
Thu Mar 12 16:57:07 2015 +0100 ; tool to compare VCF with one sample called with multiple methods #tweet ; https://github.com/lindenb/jvarkit/commit/351c259dc9f1d8bebab19b3dc57fc6a610257542
Fri Feb 20 17:50:08 2015 +0100 ; continue integration in knime ; https://github.com/lindenb/jvarkit/commit/5693e07342a30f21c807a4e3a655e3446019458f
Fri Feb 20 13:25:12 2015 +0100 ; moving vcfhead, vcftail to knime. samsequenceprogress compact watch form ; https://github.com/lindenb/jvarkit/commit/34c137e5f12c05b7eb9d7ce1b546cde6c8890cc3
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Tue Dec 10 22:12:54 2013 +0100 ; vcf head tail ; https://github.com/lindenb/jvarkit/commit/25adbeaf3de1c64fd793c5ddf5236322adb430d1
```

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcftail** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

```
$  curl -s "https://raw.github.com/arq5x/gemini/master/test/test1.snpeff.vcf" |\
java -jar dist/vcftail.jar -n2 |\
grep -v "##"| cut -f 1,2,4,5

#CHROM  POS REF ALT
chr1    935492  G   T
chr1    1334052 CTAGAG  C
```


