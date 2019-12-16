# BamWithoutBai

![Last commit](https://img.shields.io/github/last-commit/lindenb/jvarkit.png)

Query a Remote BAM without bai


## Usage

```
Usage: bamwithoutbai [options] Files
  Options:
    --bamcompression
      Compression Level.
      Default: 5
    --debug
      Enable debugging.
      Default: false
    -h, --help
      print help and exit
    --helpFormat
      What kind of help. One of [usage,markdown,xml].
  * -r, --region, --interval
      An interval as the following syntax : "chrom:start-end" or 
      "chrom:middle+extend"  or "chrom:start-end+extend" or 
      "chrom:start-end+extend-percent%".A program might use a Reference 
      sequence to fix the chromosome name (e.g: 1->chr1)
      Default: <empty string>
    -o, --output
      Output file. Optional . Default: stdout
    --repeat
      Max dichotomy repeat to perform during binary search.
      Default: 20
    --samoutputformat
      Sam output format.
      Default: SAM
      Possible Values: [BAM, SAM, CRAM]
    --version
      print version and exit

```


## Keywords

 * bam
 * sam
 * bai
 * remote


## Compilation

### Requirements / Dependencies

* java [compiler SDK 11](https://jdk.java.net/11/). Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ ./gradlew bamwithoutbai
```

The java jar file will be installed in the `dist` directory.


## Creation Date

20191213

## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/nobai/BamWithoutBai.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/nobai/BamWithoutBai.java)


## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **bamwithoutbai** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


Motivation: query a remote bam without bai (e.g: Encode bams - last checked 2019-12-16)

The idea is to run a **binary search** on the remote BAM, scanning for the BGZF blocks and the Bam Record.

This tool uses a class from the https://github.com/disq-bio/disq project.

This tool expect 'small' reads. Long reads may fail.

## Acknowledgement

Louis Bergelson and John Marshall for their useful suggestions 

## Example


$ java  -jar dist/bamwithoutbai.jar  -r "chr3:38548061-38649667" \
 'https://www.encodeproject.org/files/ENCFF741DEO/@@download/ENCFF741DEO.bam' -o jeter.bam https://www.encodeproject.org/files/ENCFF741DEO/@@download/ENCFF741DEO.bam

$ samtools view jeter.bam 
D2FC08P1:268:C3NPCACXX:8:1111:11204:70951	99	chr3	38548254	255	1S99M1S	=	38548442	289	CCACCTCCCACCCCCAACCACACCGTCTGCAGCCAGCCCCAGGCACCTGTCTCAAAGCTCCCGGGCTGTCCACACACACAAAAACCACAGTCTCCTTCCGC	@@@FFFFFFHHGHJIGIIJJIJJIGIIJGHGGHIIHHIIJJJGIIJJDGCHGHHHFEDFFFDCBDDBBBCDDDDDDDDDDB?BBDDBD@?>CCCCC@CC##	NH:i:1	HI:i:1	AS:i:198	NM:i:0	MD:Z:99
D2FC08P1:268:C3NPCACXX:8:1216:18650:7210	99	chr3	38548254	255	1S100M	=	38548442	289	CCACCTCCCACCCCCAACCACACCGTCTGCAGCCAGCCCCAGGCACCTGTCTCAAAGCTCCCGGGCTGTCCACACACACAAAAACCACAGTCTCCTTCCGG	CCCFFFFFHHHDHIJJJJJJIJJJJHHIJIJIJIJIIJIGIIJIIJJJJHHGHFHFFFFFFDDDBDDBBDCDDDDDDBBDDBDDDDDDDDCDDDDDDDD##	NH:i:1	HI:i:1	AS:i:199	NM:i:0	MD:Z:100
(...)
D2FC08P1:268:C3NPCACXX:8:1303:15665:9279	147	chr3	38649573	255	101M	=	38633218	-16456	TTGGCGCGGACTCGGCTCGGCGCGGGGCTCGGGGCACTGGGCGCAGGCTCAGCGGCCCCGGGGGAGCGATCCCTGCATCCTACGGGCGCCGCCGCCGTCTC	<9DDBB@C<2BB@DBDDBDBDDDDDBDB9BDDDDDCDDDDDDDDDDDDDDDDDDDDDDBDDDDDBBBDDDDDDDDDDDDDDBDDDDFJHHHHHFFFFFCCC	NH:i:1	HI:i:1	AS:i:196	NM:i:0	MD:Z:101
D2FC08P1:268:C3NPCACXX:8:2312:16447:12679	147	chr3	38649596	255	23S78M	=	38633222	-16452	GGGGCGGGGCCCGGGGGGGGGGGGGGGCGGGGGGCGCGGGGCGCAGCCTCGGCGCCCCGGGGGGAGCGATCCCTGCATCCTACGGGCGCCGCCGCCGTCTC	###################################################################################B>6<6F?DFFDDDDD@@@	NH:i:1	HI:i:1	AS:i:155	NM:i:8	MD:Z:5T0C5A1T8G3A3G3C42



## See also

 * lbergelson on github: https://github.com/samtools/htsjdk/issues/1445#issuecomment-565599459
 * https://twitter.com/yokofakun/status/1202681859051859969
 * https://twitter.com/jomarnz/status/1205532441353560066

