# VCFShuffle

Shuffle a VCF


## Usage

```
Usage: vcfshuffle [options] Files
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
    -N, --seed
      random seed. Optional. -1 = time.
      Default: -1
    --tmpDir
      tmp working directory. Default: java.io.tmpDir
      Default: []
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
$ make vcfshuffle
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VCFShuffle.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VCFShuffle.java)


<details>
<summary>Git History</summary>

```
Wed Jul 26 18:09:38 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/576fdd17812f9a47491945cb8bb74990ffb084c9
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Mon May 22 16:12:07 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/778fea07cb650833dcb197b69f1add57f23b21c3
Sun Apr 30 13:35:22 2017 +0200 ; coverage consensus ; https://github.com/lindenb/jvarkit/commit/fa67b93976fce20feb93d5807dfbb2feb25a2406
Fri Apr 14 17:09:04 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/881f52d5d3775325240114702b7f07148b626f4c
Mon Apr 11 16:50:27 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/d84fc3f2d5da92ed230e79702df31c190bb0fb02
Wed Jan 13 15:25:58 2016 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/db4a0f749e0c5b5a0ba067c7f4e89392ea6b62c3
Wed May 13 15:22:57 2015 +0200 ; bioalcidae: javascript-based file (vcf...) reformatter #tweet ; https://github.com/lindenb/jvarkit/commit/0f35c1940e5f381aaf593731bb3fa8d5540fa29f
Thu Sep 11 17:17:39 2014 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/59b009e383f94d8f697b91fc7b2b0b08a2969f8a
Thu Sep 11 09:36:01 2014 +0200 ; problem with java dataInputSTream: writeUTF requires line.length < SHORt_MAX ; https://github.com/lindenb/jvarkit/commit/19eac4ee36909a730903546b50461de3c19a5c1f
Mon Jun 23 12:34:44 2014 +0200 ; find-a-variation + using abstractcodec instead of vcfcodec ; https://github.com/lindenb/jvarkit/commit/da621ba8326d56da8f6907c845c539e4ea785284
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Sun Feb 2 18:55:03 2014 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/abd24b56ec986dada1e5162be5bbd0dac0c2d57c
Tue Dec 10 14:25:55 2013 +0100 ; vcf downsample ; https://github.com/lindenb/jvarkit/commit/6626570b5121011e3ae771798834e4ad31c1a3e4
Tue Dec 10 13:53:27 2013 +0100 ; vcfshuffle ; https://github.com/lindenb/jvarkit/commit/7bf652e6d8ff4f50bddb3386548ed35f4d53556c
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfshuffle** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

```
$ java -jar dist/vcfshuffle.jar input.vcf
```



