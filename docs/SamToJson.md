# SamToJson

Convert a SAM input to JSON


## Usage

```
Usage: sam2json [options] Files
  Options:
    -atts, --atts
      do not print attributes
      Default: false
    -cigar, --cigar
      expand cigar
      Default: false
    -flag, --flag
      expand SAm Flags
      Default: false
    -H, --header
      don't print SAM HEADER
      Default: false
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -name, --name
      do not print read name
      Default: false
    -out, --out
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * sam
 * bam
 * json


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
$ make sam2json
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/SamToJson.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/SamToJson.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Fri Mar 31 17:08:11 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/f78937d19c4b038e69a32fbcfa2aeab8fd8417c6
Thu Jul 7 17:21:44 2016 +0200 ; json, filterjs,... ; https://github.com/lindenb/jvarkit/commit/96ec92c986ddd3a75ab1a9b72b12e82b0df50959
Mon Apr 11 16:50:27 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/d84fc3f2d5da92ed230e79702df31c190bb0fb02
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Wed Apr 2 17:50:50 2014 +0200 ; cont rnaseq ; https://github.com/lindenb/jvarkit/commit/7b3f7e13a112b09018284931678ac78dd32cefcc
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **sam2json** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)




