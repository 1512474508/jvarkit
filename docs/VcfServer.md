# VcfServer

Web Server displaying VCF file. A web interface for vcf2table


## Usage

```
Usage: vcfserver [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -jexl, --jexl
      Use/Show JEXL filter instead of Javascript filter (which is not 
      filesystem-safe). 
      Default: false
    -p, --ped, --pedigree
      Optional Pedigree file:A pedigree is a text file delimited with tabs. No 
      header. Columns are (1) Family (2) Individual-ID (3) Father Id or '0' 
      (4) Mother Id or '0' (5) Sex : 1 male/2 female / 0 unknown (6) Status : 
      0 unaffected, 1 affected,-9 unknown
    -P, --port, -port
      Server listening port
      Default: 8080
    -timeout, --timeout
      query timeout in seconds
      Default: 60
    --version
      print version and exit

```


## Keywords

 * vcf
 * table
 * visualization
 * server
 * web


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
$ make vcfserver
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfserver/VcfServer.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfserver/VcfServer.java)


<details>
<summary>Git History</summary>

```
Sat Oct 28 18:13:58 2017 +0200 ; vcf server: description ; https://github.com/lindenb/jvarkit/commit/7abb1d5513bdd8f9f249a290b418b75b3a237771
Sat Oct 28 18:04:34 2017 +0200 ; vcf server ; https://github.com/lindenb/jvarkit/commit/1ba12c148566055ec896ddec9b7c2d7674cf8236
Fri Oct 27 23:58:05 2017 +0200 ; javascript for vcfserver ; https://github.com/lindenb/jvarkit/commit/2fbecaea195213025f9d860e7ba121887f247d2c
Fri Oct 27 19:58:25 2017 +0200 ; text output for server ; https://github.com/lindenb/jvarkit/commit/b146971af0577da23b25d84338d608325f9432c2
Fri Oct 27 18:13:18 2017 +0200 ; cont vcf server ; https://github.com/lindenb/jvarkit/commit/abc4d04da94e86f7d4955e24ffdec9632afd7bdc
Fri Oct 27 15:15:11 2017 +0200 ; adding vcf server and https://www.biostars.org/p/279942/#280255 ; https://github.com/lindenb/jvarkit/commit/3eabba0b8c06b88f90193f958e47a725d105216a
Fri Oct 27 13:05:17 2017 +0200 ; starting vcf server ; https://github.com/lindenb/jvarkit/commit/7a514c92bc44037f3f61538dfd1bf0147ac353af
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfserver** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Input

Input is a set of indexed VCF file (tabix or tribble) or a file containing the path to the VCFs.


## Example 

```
$ java -jar dist/vcfserver.jar input.vcf.gz

2017-10-27 23:53:04.140:INFO::main: Logging initialized @510ms
[INFO][VcfServer]Starting com.github.lindenb.jvarkit.tools.vcfserver.VcfServer on http://localhost:8080
2017-10-27 23:53:04.223:INFO:oejs.Server:main: jetty-9.3.7.v20160115
2017-10-27 23:53:04.336:INFO:oejs.ServerConnector:main: Started ServerConnector@9a8472{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
2017-10-27 23:53:04.337:INFO:oejs.Server:main: Started @717ms

```



