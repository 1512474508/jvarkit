# Biostar95652

Drawing a schematic genomic context tree.


## Usage

```
Usage: biostar95652 [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --ncbi-api-key
      NCBI API Key see https://ncbiinsights.ncbi.nlm.nih.gov/2017/11/02/new-api-keys-for-the-e-utilities/ 
      .If undefined, it will try to get in that order:  1) environment 
      variable ${NCBI_API_KEY} ;  2) the jvm property "ncbi.api.key" ;	3) A 
      java property file ${HOME}/.ncbi.properties and key api_key
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * genbank
 * svg
 * tree
 * evolution



## See also in Biostars

 * [https://www.biostars.org/p/95652](https://www.biostars.org/p/95652)


## Compilation

### Requirements / Dependencies

* java compiler SDK 1.8 http://www.oracle.com/technetwork/java/index.html (**NOT the old java 1.7 or 1.6**) and avoid OpenJdk, use the java from Oracle. Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make biostar95652
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar95652.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar95652.java)


<details>
<summary>Git History</summary>

```
Thu Nov 2 19:54:56 2017 +0100 ; added NCBI API key ; https://github.com/lindenb/jvarkit/commit/fa13648014a42cd307b25f8661385e9f62d42bea
Wed Jun 28 17:33:30 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/3c252f19e5cad0ec87d250a5b9884b6f2d6fe856
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Thu May 11 16:20:27 2017 +0200 ; move to jcommander ; https://github.com/lindenb/jvarkit/commit/15b6fabdbdd7ce0d1e20ca51e1c1a9db8574a59e
Fri Apr 7 16:35:31 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/54c5a476e62e021ad18e7fd0d84bf9e5396c8c96
Thu Jul 28 09:48:29 2016 +0200 ; NCBI moved API to https ; https://github.com/lindenb/jvarkit/commit/d207e023a06d2ae7afd2e05d2f1369b8a713974b
Sat Mar 22 16:38:35 2014 +0100 ; Biostar95652 ; https://github.com/lindenb/jvarkit/commit/8a15bae59af5d5afca27234fae71fb3dcd26ad87
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **biostar95652** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

```bash
$ java -jar dist/biostar95652.jar \
   NP_077719.2 \
   XP_513697.3 XP_001114248.1 \
   XP_540266.3 XP_002686160.2 \
   NP_035058.2 NP_077334.1 \
   NP_001238962.1 NP_001108566.1 > result.svg
```

Result:

![Hosted by imgur.com](http://i.imgur.com/SYn6IAal.png)


