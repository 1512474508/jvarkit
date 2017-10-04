# BWAMemDigest




## Usage

```
Usage: bwamemdigest [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    --version
      print version and exit
    -B
      BED of Regions to ignore.
    -x
       Extend BED Regions to ignore by 'x' bases.
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
$ make bwamemdigest
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/mem/BWAMemDigest.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/mem/BWAMemDigest.java)


<details>
<summary>Git History</summary>

```
Fri Jun 2 16:31:30 2017 +0200 ; circos / lumpy ; https://github.com/lindenb/jvarkit/commit/7bddffca3899196e568fb5e1a479300c0038f74f
Wed May 3 08:14:25 2017 +0200 ; remove GetOpt ; https://github.com/lindenb/jvarkit/commit/32056bc2b0c9a20b7ae1c2216151885378bf2ab8
Fri May 23 15:00:53 2014 +0200 ; cont moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/81f98e337322928b07dfcb7a4045ba2464b7afa7
Mon May 12 14:06:30 2014 +0200 ; continue moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/011f098b6402da9e204026ee33f3f89d5e0e0355
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Mon Mar 10 13:23:56 2014 +0100 ; SAM specification changed; supplementary align now defined as 'SA' ; https://github.com/lindenb/jvarkit/commit/0abcdb58cb641efc661a32a6fa5a36e150457dee
Wed Oct 23 16:07:39 2013 +0200 ; vcf2xml ; https://github.com/lindenb/jvarkit/commit/45be5f2d65b1bef86e8a156b5765e49d5019ff6e
Wed Oct 23 08:52:16 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/849c620dfd4a4353a7fc9f52fd9a8e6f5cc83fc7
Mon Oct 21 11:25:40 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/5712fed9238aad4e286ab59a983927afcac7c8be
Fri Oct 11 15:39:02 2013 +0200 ; picard v.100: deletion of VcfIterator :-( ; https://github.com/lindenb/jvarkit/commit/e88fab449b04aed40c2ff7f9d0cf8c8b6ab14a31
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **bwamemdigest** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

package/bwa/bwa-0.7.4/bwa mem \
 -M  ~/tmp/DATASANGER/hg18/chr1.fa  \
 ~/tmp/DATASANGER/4368_1_1.fastq.gz  ~/tmp/DATASANGER/4368_1_2.fastq.gz 2> /dev/null | 
 java -jar dist/bwamemdigest.jar -B <(curl -s "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/gap.txt.gz" | gunzip -c | cut -f2,3,4 ) -x 500  | tee /dev/tty | gzip --best > /tmp/jeter.mem.bed.gz



