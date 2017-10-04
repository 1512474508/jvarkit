# Biostar130456

Individual VCF files from main VCF file


## Usage

```
Usage: biostar130456 [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -z, --homref
      remove homzygote REF/REF
      Default: false
    -o, --output
      Output file. Optional . Default: stdout
  * -p, --pattern
      output file pattern. Must contain the word __SAMPLE__
    -x, --uncalled
      remove uncalled genotypes
      Default: false
    --version
      print version and exit

```


## See also in Biostars

 * [https://www.biostars.org/p/130456](https://www.biostars.org/p/130456)


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
$ make biostar130456
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar130456.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar130456.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Fri May 12 18:07:46 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/ca96bce803826964a65de33455e5231ffa6ea9bd
Thu Apr 6 18:34:56 2017 +0200 ; moving to jcommander ; https://github.com/lindenb/jvarkit/commit/883b4ba4b693661663694256f16b137e371147fa
Wed Mar 30 17:45:00 2016 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/ef77dce3c82c470017916555304df6a470fbdad4
Thu Nov 26 17:41:15 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/899c60335614350d463be66ec21e994b34dc55be
Mon Oct 5 16:45:29 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/49db236991055dbd990d55732a3c312b8204d508
Mon Jun 8 17:24:41 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/f9a941d604f378ff40a32666c8381cb2450c7cfa
Tue Feb 10 21:55:45 2015 +0100 ; biostar130456 split multi vcf #tweet ; https://github.com/lindenb/jvarkit/commit/06008a80769b17d7b72c31a93eb5dacb180e1b69
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **biostar130456** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

```
bash
$   curl -sL "https://raw.githubusercontent.com/arq5x/bedtools2/bc2f97d565c36a82c1a0b12f570fed4398001e5f/test/map/test.vcf" |\
    java -jar dist/biostar130456.jar -x -z -p "sample.__SAMPLE__.vcf.gz" 
sample.NA00003.vcf.gz
sample.NA00001.vcf.gz
sample.NA00002.vcf.gz

$ gunzip -c sample.NA00003.vcf.gz
(...)
##source=myImputationProgramV3.1
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	NA00003
chr1	10	rs6054257	G	A	29	PASS	AF=0.5;DB;DP=14;H2;NS=3	GT:DP:GQ:HQ	1/1:5:43
chr1	20	rs6040355	A	G,T	67	PASS	AA=T;AF=0.333,0.667;DB;DP=10;NS=2	GT:DP:GQ	2/2:4:35
chr1	130	microsat1	GTC	G,GTCT	50	PASS	AA=G;DP=9;NS=3	GT:DP:GQ	1/1:3:40
chr2	130	microsat1	GTC	G,GTCT	50	PASS	AA=G;DP=9;NS=3	GT:DP:GQ	1/1:3:40
```

## See also

 * GATK SelectVariants with option -sn 


