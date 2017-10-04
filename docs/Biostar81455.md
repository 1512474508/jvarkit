# Biostar81455

Defining precisely the genomic context based on a position .


## Usage

```
Usage: biostar81455 [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
  * -KG, --knownGene
      UCSC knownGene URI. Beware chromosome names are formatted the same as 
      your REFERENCE. A typical KnownGene file is 
      http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz 
      .If you only have a gff file, you can try to generate a knownGene file 
      with [http://lindenb.github.io/jvarkit/Gff2KnownGene.html](http://lindenb.github.io/jvarkit/Gff2KnownGene.html)
      Default: http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * bed
 * gene
 * knownGene
 * ucsc



## See also in Biostars

 * [https://www.biostars.org/p/81455](https://www.biostars.org/p/81455)


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
$ make biostar81455
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar81455.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/biostar/Biostar81455.java)


<details>
<summary>Git History</summary>

```
Wed May 24 17:27:28 2017 +0200 ; lowres bam2raster & fix doc ; https://github.com/lindenb/jvarkit/commit/6edcfd661827927b541e7267195c762e916482a0
Sun May 21 20:02:10 2017 +0200 ; instanceMain -> instanceMainWithExit ; https://github.com/lindenb/jvarkit/commit/4fa41d198fe7e063c92bdedc333cbcdd2b8240aa
Fri May 19 15:26:44 2017 +0200 ; vcf stats ; https://github.com/lindenb/jvarkit/commit/965f6bb8151a7748a4d15bc7520468492da9c40b
Mon May 15 12:10:21 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/b4895dd40d1c34f345cd2807f7a81395ba27e8ee
Fri May 12 18:07:46 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/ca96bce803826964a65de33455e5231ffa6ea9bd
Thu May 11 16:20:27 2017 +0200 ; move to jcommander ; https://github.com/lindenb/jvarkit/commit/15b6fabdbdd7ce0d1e20ca51e1c1a9db8574a59e
Wed Apr 19 10:40:28 2017 +0200 ; rm-xml ; https://github.com/lindenb/jvarkit/commit/971b090382a1b0b96e250030a5c8e7be500593b7
Tue May 31 18:35:56 2016 +0200 ; fix interval maps ; https://github.com/lindenb/jvarkit/commit/f6afe341076b9da7c63c33b64c96d15d6cc7b596
Mon Dec 14 17:18:02 2015 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/9b271459821d8061aa07e98bc7f30232597f47c9
Mon May 12 14:06:30 2014 +0200 ; continue moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/011f098b6402da9e204026ee33f3f89d5e0e0355
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Fri Oct 11 15:39:02 2013 +0200 ; picard v.100: deletion of VcfIterator :-( ; https://github.com/lindenb/jvarkit/commit/e88fab449b04aed40c2ff7f9d0cf8c8b6ab14a31
Wed Sep 18 17:39:34 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/7713393b28bc5004c6d8047a24bb9b84d2294fef
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **biostar81455** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example
```bash
echo -e "chr22\t41258261\nchr22\t52000000\nchr22\t0" |\
	java   dist/biostar81455.jar 

chr22	41258261	uc003azg.2	41253084	41258785	POSITIVE	Exon 2	41257621	41258785	0
chr22	41258261	uc011aox.2	41253084	41305239	POSITIVE	Exon 1	41253084	41253249	-5012
chr22	41258261	uc003azi.3	41253084	41328823	POSITIVE	Exon 1	41253084	41253249	-5012
chr22	41258261	uc003azj.3	41255553	41258130	NEGATIVE	Exon 1	41255553	41258130	-131
chr22	41258261	uc010gyh.1	41258260	41282519	POSITIVE	Exon 1	41258260	41258683	0
chr22	41258261	uc011aoy.1	41258260	41363888	POSITIVE	Exon 1	41258260	41258683	0
chr22	52000000	uc011asd.2	51195513	51227614	POSITIVE	Exon 4	51227177	51227614	-772386
chr22	52000000	uc003bni.3	51195513	51238065	POSITIVE	Exon 4	51237082	51238065	-761935
chr22	52000000	uc011ase.1	51205919	51220775	NEGATIVE	Exon 1	51220615	51220775	-779225
chr22	52000000	uc003bnl.1	51205919	51222087	NEGATIVE	Exon 1	51221928	51222087	-777913
chr22	52000000	uc003bns.3	51222156	51238065	POSITIVE	Exon 3	51237082	51238065	-761935
chr22	52000000	uc003bnq.1	51222224	51227600	POSITIVE	Exon 4	51227322	51227600	-772400
chr22	52000000	uc003bnr.1	51222224	51227781	POSITIVE	Exon 4	51227319	51227781	-772219
chr22	52000000	uc010hbj.3	51222224	51238065	POSITIVE	Exon 3	51237082	51238065	-761935
chr22	0	uc002zks.4	16150259	16193004	NEGATIVE	Exon 8	16150259	16151821	16150259
chr22	0	uc002zkt.3	16162065	16172265	POSITIVE	Exon 1	16162065	16162388	16162065
chr22	0	uc002zku.3	16179617	16181004	NEGATIVE	Exon 1	16179617	16181004	16179617
chr22	0	uc002zkv.3	16187164	16193004	NEGATIVE	Exon 5	16187164	16187302	16187164	
```


