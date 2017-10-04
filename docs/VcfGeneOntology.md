# VcfGeneOntology

Find the GO terms for VCF annotated with SNPEFF or VEP


## Usage

```
Usage: vcfgo [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      Output file. Optional . Default: stdout
    --version
      print version and exit
  * -A
      (goa input url)
      Default: http://cvsweb.geneontology.org/cgi-bin/cvsweb.cgi/go/gene-associations/gene_association.goa_human.gz?rev=HEAD
    -C
      (Go:Term) Add children to the list of go term to be filtered. Can be 
      used multiple times.
      Default: []
    -F
       if -C is used, don't remove the variant but set the filter
  * -G
      (go  url)
      Default: http://archive.geneontology.org/latest-termdb/go_daily-termdb.rdf-xml.gz
    -T
      INFO tag.
      Default: GOA
    -r
      remove variant if no GO term is found associated to variant
      Default: false
    -v
      inverse filter if -C is used
      Default: false

```


## Keywords

 * vcf
 * go


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
$ make vcfgo
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfgo/VcfGeneOntology.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcfgo/VcfGeneOntology.java)


<details>
<summary>Git History</summary>

```
Tue Jun 6 18:06:17 2017 +0200 ; postponed vcf ; https://github.com/lindenb/jvarkit/commit/bcd52318caf3cd76ce8662485ffaacaabde97caf
Sun Jun 4 21:53:22 2017 +0200 ; writing bcf ; https://github.com/lindenb/jvarkit/commit/784fdac37cd7e6eca04e35d0a3ddad8637826b4a
Wed May 17 14:09:36 2017 +0200 ; fix typo bioalcidae ; https://github.com/lindenb/jvarkit/commit/9db2344e7ce840df02c5a7b4e2a91d6f1a5f2e8d
Mon May 15 17:17:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/fc77d9c9088e4bc4c0033948eafb0d8e592f13fe
Fri Apr 21 18:16:07 2017 +0200 ; scan sv ; https://github.com/lindenb/jvarkit/commit/49b99018811ea6a624e3df556627ebdbf3f16eab
Wed Feb 22 19:07:03 2017 +0100 ; refactor prediction parsers ; https://github.com/lindenb/jvarkit/commit/dc7f7797c60d63cd09d3b7712fb81033cd7022cb
Mon Feb 23 16:53:54 2015 +0100 ; Moving code from Ant to Make. NO MAVEN, NO ANT, NO NO NO #tweet ; https://github.com/lindenb/jvarkit/commit/bd5eb283b4e86b42d05a260d87e137930faef4a6
Thu Feb 12 11:21:26 2015 +0100 ; new version of VCF-GeneOntology (annotation+filter) #tweet ; https://github.com/lindenb/jvarkit/commit/6087f6b4718a7bd1e10685f62a92217ed525ce39
Mon May 12 14:06:30 2014 +0200 ; continue moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/011f098b6402da9e204026ee33f3f89d5e0e0355
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Fri Oct 11 15:39:02 2013 +0200 ; picard v.100: deletion of VcfIterator :-( ; https://github.com/lindenb/jvarkit/commit/e88fab449b04aed40c2ff7f9d0cf8c8b6ab14a31
Fri Sep 6 15:11:11 2013 +0200 ; moved code for latest version of picard (1.97). Using VCFIterator instead of ASciiLineReader ; https://github.com/lindenb/jvarkit/commit/810877c10406a017fd5a31dacff7e8401089d429
Tue Jul 16 22:55:41 2013 +0200 ; now using biomart instead of VEP annotations ; https://github.com/lindenb/jvarkit/commit/3833b7038fe2b766bd4b93ac45b990b4cb28131b
Mon Jul 15 12:55:01 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/982384a4b0b74e7c7574f3c897447d7816f0f458
Fri Jul 12 22:10:48 2013 +0200 ; go tools. vcf tools reads from URL ; https://github.com/lindenb/jvarkit/commit/0e81faa156c2ec00d680795ca454a5c9356985a5
Fri Jul 12 19:02:51 2013 +0200 ; annotion go & test if bzip or bgzip in IOutils ; https://github.com/lindenb/jvarkit/commit/c338cd7fc1e22474d97f4e15c58e7ff594611606
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfgo** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example


```make
INPUT=${HOME}/test.vcf.gz
VCFGO=java -jar dist/vcfgo.jar -A  gene_association.goa_human.gz -G go_daily-termdb.rdf-xml.gz 
DEPS=dist/vcfgo.jar gene_association.goa_human.gz go_daily-termdb.rdf-xml.gz 

all: $(addsuffix .vcf,$(addprefix test,1 2 3 4 5 6 7))
	$(foreach F,$^,echo -e "Output of $F:"  && cat $F; )

test1.vcf: ${DEPS}
	${VCFGO} ${INPUT} |  grep -v "#" | grep GO | cut -f 7,8 | head -n 1 > $@

test2.vcf: ${DEPS}
	${VCFGO}  -T MYGOATAG ${INPUT} |  grep -v "#" | grep MYGOATAG | cut -f 7,8 | head -n 1 > $@
	
test3.vcf: ${DEPS}
	${VCFGO}  -C GO:0007283 ${INPUT} |  grep -v "#" | grep GO  | cut -f 7,8 | head -n 1 > $@
	
test4.vcf: ${DEPS}
	${VCFGO} -C GO:0007283 -v  ${INPUT}|  grep -v "#" |  grep GO | cut -f 7,8 | head -n 1 > $@

test5.vcf: ${DEPS}
	${VCFGO}  -C GO:0007283 -F GOFILTER ${INPUT}| grep -v "#" |   grep GOFILTER | cut -f 7,8 | head -n1 > $@
	
test6.vcf: ${DEPS}
	${VCFGO} -C GO:0007283 -F GOFILTER -v  ${INPUT}| grep -v "#" |   grep GOFILTER | cut -f 7,8 | head -n1 > $@	
	
test7.vcf: ${DEPS}
	${VCFGO} -r ${INPUT} | grep -v "#" | cut -f 7,8 | head -n1 > $@
		
	
gene_association.goa_human.gz :
	curl -o $@ "http://cvsweb.geneontology.org/cgi-bin/cvsweb.cgi/go/gene-associations/gene_association.goa_human.gz?rev=HEAD"

go_daily-termdb.rdf-xml.gz :
	curl -o $@ "http://archive.geneontology.org/latest-termdb/$@"

dist/vcfgo.jar:
	$(MAKE) vcfgo

```
output of GNU make:

Output of test1.vcf:

```
.	(...)GOA=AURKAIP1|GO:0070124&GO:0005739&GO:0005515&GO:0070125&GO:0045839&GO:0070126&GO:0032543&GO:0006996&GO:0005743&GO:0005654&GO:0005634&GO:0045862&GO:0043231;MQ=39	GT:PL:DP:GQ	1/1:35,3,0:1:4
```

Output of test2.vcf:

```
.	(...)MYGOATAG=AURKAIP1|GO:0070124&GO:0005739&GO:0005515&GO:0070125&GO:0045839&GO:0070126&GO:0032543&GO:0006996&GO:0005743&GO:0005654&GO:0005634&GO:0045862&GO:0043231
```

Output of test3.vcf:

```
.	(...)GOA=GJA9|GO:0007154&GO:0005922&GO:0016021,MYCBP|GO:0005739&GO:0005515&GO:0006355&GO:0005813&GO:0005737&GO:0003713&GO:0005634&GO:0007283&GO:0006351;MQ=46
```

Output of test4.vcf:
````
.	(...)GOA=AURKAIP1|GO:0070124&GO:0005739&GO:0005515&GO:0070125&GO:0045839&GO:0070126&GO:0032543&GO:0006996&GO:0005743&GO:0005654&GO:0005634&GO:0045862&GO:0043231
```

Output of test5.vcf:

```
GOFILTER	(...)GOA=AURKAIP1|GO:0070124&GO:0005739&GO:0005515&GO:0070125&GO:0045839&GO:0070126&GO:0032543&GO:0006996&GO:0005743&GO:0005654&GO:0005634&GO:0045862&GO:0043231
```

Output of test6.vcf:

```
GOFILTER	(...)GOA=GJA9|GO:0007154&GO:0005922&GO:0016021,MYCBP|GO:0005739&GO:0005515&GO:0006355&GO:0005813&GO:0005737&GO:0003713&GO:0005634&GO:0007283&GO:0006351
```

Output of test7.vcf:

```
.	(...)GOA=AURKAIP1|GO:0070124&GO:0005739&GO:0005515&GO:0070125&GO:0045839&GO:0070126&GO:0032543&GO:0006996&GO:0005743&GO:0005654&GO:0005634&GO:0045862&GO:0043231
```

