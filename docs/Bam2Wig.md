# Bam2Wig

Bam to fixedStep Wiggle converter. Parses the cigar String to get the depth. Memory intensive: must alloc sizeof(int)*size(chrom)


## Usage

```
Usage: bam2wig [options] Files
  Options:
    --filter
      A filter expression. Reads matching the expression will be filtered-out. 
      Empty String means 'filter out nothing/Accept all'. See https://github.com/lindenb/jvarkit/blob/master/src/main/resources/javacc/com/github/lindenb/jvarkit/util/bio/samfilter/SamFilterParser.jj 
      for a complete syntax.
      Default: Accept All/ Filter out nothing
    -t, --header
      print a UCSC custom track header
      Default: false
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -i, --integer
      cast to integer
      Default: false
    -d, --mindepth
      minimal depth before setting depth to zero
      Default: 0
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit
    -s, --windowShift
      window shift
      Default: 25
    -w, --windowSize
      window size
      Default: 100
    -g, --zerolength
      minimal zero-coverage length before writing a new header
      Default: 200

```


## Keywords

 * bam
 * wig
 * wiggle


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
$ make bam2wig
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bam2wig/Bam2Wig.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/bam2wig/Bam2Wig.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Fri May 5 21:02:19 2017 +0200 ; git move usage builder ; https://github.com/lindenb/jvarkit/commit/cc2f3aea9ca8fbb617b7f57a15b3294b6d6680e5
Fri Apr 7 16:35:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/54c5a476e62e021ad18e7fd0d84bf9e5396c8c96
Thu Nov 26 12:58:31 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/cfdff2e66fbeaa4627b50361a09196be2a2e1477
Thu Oct 1 14:55:56 2015 +0200 ; bam2wig ; https://github.com/lindenb/jvarkit/commit/dd0835cd10122f2196335ebff9b7f6d7f40c265c
Thu Oct 16 17:24:57 2014 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/f0697329ae2147544cd191da0a67824313227a3f
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Fri Mar 21 17:23:31 2014 +0100 ; continue ; https://github.com/lindenb/jvarkit/commit/3040855327a6578d058b81309e3d1aa631933497
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Tue Dec 3 17:50:17 2013 +0100 ; bam2wig ; https://github.com/lindenb/jvarkit/commit/25835dce4a1c841d0805416b9c389699060cf578
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **bam2wig** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example
the input file

```bash
java -jar dist/bam2wig.jar -w 1 -s 3 -i -t -L OFF examples/toy.bam
```

```
track type=wiggle_0 name="__REPLACE_WIG_NAME__" description="__REPLACE_WIG_DESC__"
fixedStep chrom=ref start=7 step=3 span=1
1
3
3
3
1
1
0
0
1
0
2
2
1
fixedStep chrom=ref2 start=1 step=3 span=1
1
2
3
4
5
6
6
5
4
3
3
```


