# Biostar154220

Cap BAM to a given coverage


## Usage

```
Usage: biostar154220 [options] Files
  Options:
    --bamcompression
      Compression Level.
      Default: 5
    -n, --depth
      number of reads
      Default: 20
    -filter, --filter
      A filter expression. Reads matching the expression will be filtered-out. 
      Empty String means 'filter out nothing/Accept all'. See https://github.com/lindenb/jvarkit/blob/master/src/main/resources/javacc/com/github/lindenb/jvarkit/util/bio/samfilter/SamFilterParser.jj 
      for a complete syntax.
      Default: mapqlt(1) || MapQUnavailable() || Duplicate() || FailsVendorQuality() || NotPrimaryAlignment() || SupplementaryAlignment()
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      Output file. Optional . Default: stdout
    --samoutputformat
      Sam output format.
      Default: TypeImpl{name='SAM', fileExtension='sam', indexExtension='null'}
    --version
      print version and exit

```


## See also in Biostars

 * [https://www.biostars.org/p/154220](https://www.biostars.org/p/154220)


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
$ make biostar154220
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar154220.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar154220.java)


<details>
<summary>Git History</summary>

```
Mon May 29 12:33:45 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/870be8e90d7e98d947f73e67ef9965f12f351846
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Fri Apr 7 16:35:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/54c5a476e62e021ad18e7fd0d84bf9e5396c8c96
Fri Aug 28 08:53:17 2015 +0000 ; fixed memory leak ; https://github.com/lindenb/jvarkit/commit/3e121125caf1f137577a6d10a161e519ffdef57b
Wed Aug 12 18:34:11 2015 +0200 ; ignore mapq=0 ; https://github.com/lindenb/jvarkit/commit/484ae4fc0e3333893aa6f343db8774f0ede40dda
Wed Aug 12 15:39:20 2015 +0200 ; Tool to sort BAM on REF/name , tool to 'cap' bam. #tweet ; https://github.com/lindenb/jvarkit/commit/c12609cb0bb6c893e6477533ae3c9a60e8b9f3b5
Wed Aug 12 15:35:53 2015 +0200 ; Tool to sort BAM on REF/name , tool to 'cap' bam. ; https://github.com/lindenb/jvarkit/commit/6f7e43d6fb776bf2a549700d71220b3d2693862d
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **biostar154220** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

## Example

```bash
$ java -jar dist/sortsamrefname.jar -T bam /input.bam  |\
  java -jar dist/biostar154220.jar  -n 20 -T bam |\
  samtools sort - output


$ samtools mpileup output.bam  | cut -f 4 | sort | uniq -c

  12692 0
 596893 1
  94956 10
  56715 11
  76947 12
  57912 13
  66585 14
  51961 15
  63184 16
  47360 17
  65189 18
  65014 19
 364524 2
 169064 20
  72078 3
 118288 4
  54802 5
  82555 6
  53175 7
  78474 8
  54052 9

```


