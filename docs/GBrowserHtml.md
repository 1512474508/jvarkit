# GBrowserHtml

Fork a VCF.


## Usage

```
Usage: forkvcf [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      Output file. Optional . Default: stdout
    -prefix, --prefix
      Zip Prefix
      Default: gbrowse/
    --version
      print version and exit

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
$ make forkvcf
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/GBrowserHtml.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/GBrowserHtml.java)


<details>
<summary>Git History</summary>

```
Mon May 15 10:41:51 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/c13a658b2ed3bc5dd6ade57190e1dab05bf70612
Sun May 7 13:21:47 2017 +0200 ; rm xml ; https://github.com/lindenb/jvarkit/commit/f37088a9651fa301c024ff5566534162bed8753d
Thu Apr 27 17:22:22 2017 +0200 ; cont jcommander ; https://github.com/lindenb/jvarkit/commit/0a27a246a537d2b48201596067652ea26bfc28d6
Mon Jul 11 21:13:44 2016 +0200 ; readdepth ; https://github.com/lindenb/jvarkit/commit/e12fae61c73485adcc95ab4be7315f970cffe6bb
Mon Jul 11 16:10:20 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/f9925f9b6eeb54b7bf98036670a2f8802427f1d5
Mon Jul 11 12:34:46 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/af2e43a15f876d82aa659bc254ec8d0c22565606
Sun Jul 10 13:40:10 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/e3c1bccc2868126750ce88174886751dc18b21cf
Fri Jul 8 17:32:51 2016 +0200 ; html ; https://github.com/lindenb/jvarkit/commit/15ff9478b449d7deb4ac0a037d0fe9f643639708
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **forkvcf** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)





### Example



```
(...)
```







