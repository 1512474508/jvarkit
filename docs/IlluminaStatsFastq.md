# IlluminaStatsFastq

Reads filenames from stdin: Count FASTQs in Illumina Result.


## Usage

```
Usage: ilmnfastqstats [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -o, --output
      Output zip file.
    --version
      print version and exit
    -X
      maximum number of DNA indexes to print. memory consuming if not 0.
      Default: 0

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
$ make ilmnfastqstats
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/IlluminaStatsFastq.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/IlluminaStatsFastq.java)


<details>
<summary>Git History</summary>

```
Mon May 29 12:33:45 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/870be8e90d7e98d947f73e67ef9965f12f351846
Mon Apr 10 17:44:58 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1a3303b52707e9ba8c9b913e0f82d2735698d24e
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Thu Nov 28 08:16:28 2013 +0100 ; cont ; https://github.com/lindenb/jvarkit/commit/d41deb4c340967592eb53e98101077ccbd84a3dd
Fri Nov 22 17:34:16 2013 +0100 ; my version of fastqreader ; https://github.com/lindenb/jvarkit/commit/caf819c6c165d251722ce24f4429d6106e50c2cc
Mon Nov 18 12:03:05 2013 +0100 ; fastq rev comp ; https://github.com/lindenb/jvarkit/commit/05dd403283d57ffe03e3043853f9ff159a888eb8
Fri Nov 8 11:49:28 2013 +0100 ; distinct bam dict ; https://github.com/lindenb/jvarkit/commit/6035b5fba30e26e67b4ea6a02cd75c24901caada
Thu Nov 7 13:54:08 2013 +0100 ; continue ; https://github.com/lindenb/jvarkit/commit/2a7db844ef92646208fb98090906fdb21163613d
Tue Nov 5 18:46:15 2013 +0100 ; cmd line engance ; https://github.com/lindenb/jvarkit/commit/9049f617cc965e0bc9ad6f533a8c517b0d774e55
Mon Nov 4 16:23:17 2013 +0100 ; moving to std cli program ; https://github.com/lindenb/jvarkit/commit/9ed1dc3f053d57d379862c9a14648f96a967ada7
Thu Oct 31 17:13:55 2013 +0100 ; stuff for comparing bams ; https://github.com/lindenb/jvarkit/commit/fc2598c96eaa7b3001aac99b2c8d6026f78facdd
Wed Oct 30 10:23:45 2013 +0100 ; readme ; https://github.com/lindenb/jvarkit/commit/be21dd464da52b172616225872109de03c8768d5
Tue Oct 29 15:05:23 2013 +0100 ; fix close ; https://github.com/lindenb/jvarkit/commit/b0385a1dd0adcbed909a4b7fda3b07afd544b9e1
Tue Oct 29 14:47:32 2013 +0100 ; illumina fastq stats ; https://github.com/lindenb/jvarkit/commit/38f3d198d5786e8eaa4e0ad7d678b45c2ae10e05
Mon Oct 21 11:25:40 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/5712fed9238aad4e286ab59a983927afcac7c8be
Fri Oct 11 15:39:02 2013 +0200 ; picard v.100: deletion of VcfIterator :-( ; https://github.com/lindenb/jvarkit/commit/e88fab449b04aed40c2ff7f9d0cf8c8b6ab14a31
Mon Sep 30 17:03:13 2013 +0200 ; VCFPoly-X added ; https://github.com/lindenb/jvarkit/commit/9c91e722648f8180b964d52579c326fe583b010c
Fri Sep 27 18:13:12 2013 +0200 ; cont fastq ; https://github.com/lindenb/jvarkit/commit/94e90aea48e4b5c1a08fb81b4871fe3f5d349590
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **ilmnfastqstats** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

 
## Output
the software generates a directory or a zip file.

it contains the following files:

* names.tsv : file path with sample, index, lane, side , split, file size
* counts.tsv:  file path,total reads, read_fail_filter, read_do_not_fail_filter
* histopos2qual:  file path, position, mean-qual, count bases (excluding read_fail_filter)
* histquals: file path, [quality[, count reads
* lengths: file path, length, count reads (excluding read_fail_filter)
* notfastq: problem with that fastq
* quals : file, mean-quality (excluding read_fail_filter)
* bases! file, A,T,G,C,N (excluding read_fail_filter)
* indexes most frequent indexes

## Example

``` bash
$ find dir1 dir2 -type f -name "*.fastq.gz" |\
   grep -v SAMPLE1234 |\
   java -jar dist/ilmnfastqstats.jar \
   O=OUTDIR

$ ls JETER 
bases.tsv
counts.tsv
histpos2qual.tsv
histquals.tsv
lengths.tsv
names.tsv
notfastq.tsv
quals.tsv

$ find dir1 dir2 -type f -name "*.fastq.gz" |\
   grep -v SAMPLE1234 |\
   java -jar dist/ilmnfastqstats.jar \
   O=OUTDIR.zip


$ unzip -t OUTDIR.zip 
Archive:  OUTDIR.zip
    testing: names.tsv                OK
    testing: counts.tsv               OK
    testing: quals.tsv                OK
    testing: notfastq.tsv             OK
    testing: histquals.tsv            OK
    testing: histpos2qual.tsv         OK
    testing: bases.tsv                OK
    testing: lengths.tsv              OK
```

 

