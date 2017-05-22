# PcrClipReads


## Usage

```
Usage: pcrclipreads [options] Files
  Options:
    --bamcompression
      Compression Level.
      Default: 5
    -B, --bed
      Bed file containing non-overlapping PCR fragments
    -flag, --flag
      Only run on reads having sam flag (flag). -1 = all reads. (as 
      https://github.com/lindenb/jvarkit/issues/43) 
      Default: -1
    -h, --help
      print help and exit
    -largest, --largest
      see if a read overlaps two bed intervals use the bed region sharing the 
      longest sequence with a read. see 
      https://github.com/lindenb/jvarkit/issues/44 
      Default: false
    -o, --output
      Output file. Optional . Default: stdout
    -pr, --programId
      add a program ID to the clipped SAM records
      Default: false
    --samoutputformat
      Sam output format.
      Default: TypeImpl{name='SAM', fileExtension='sam', indexExtension='null'}
    --version
      print version and exit

```


## Description

Soft clip bam files based on PCR target regions https://www.biostars.org/p/147136/

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
$ make pcrclipreads
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/pcr/PcrClipReads.java
](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/pcr/PcrClipReads.java
)
## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **pcrclipreads** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)




 Soft clip BAM files based on PCR target regions https://www.biostars.org/p/147136/


 *  mapping quality is set to zero if a read on strand - overlap the 5' side of the PCR fragment
 *  mapping quality is set to zero if a read on strand + overlap the 3' side of the PCR fragment
 *  mapping quality is set to zero if no PCR fragment is found


after processing the BAM file should be sorted, processed with samtools calmd and picard fixmate




### Example





```

echo  "seq2\t1100\t1200" > test.bed
java -jar dist-1.133/pcrclipreads.jar -B test.bed -b  samtools-0.1.19/examples/ex1.bam  |\
	samtools  view -q 1 -F 4 -bu  -  |\
	samtools  sort - clipped && samtools index clipped.bam

samtools tview -p seq2:1100  clipped.bam  samtools-0.1.19/examples/ex1.fa


```




### output


![img](http://i.imgur.com/bjDEnMW.jpg)




```

    1091      1101      1111      1121      1131      1141      1151      1161      1171      1181      1191
AAACAAAGGAGGTCATCATACAATGATAAAAAGATCAATTCAGCAAGAAGATATAACCATCCTACTAAATACATATGCACCTAACACAAGACTACCCAGATTCATAAAACAAATNNNNN
              ...................................                               ..................................
              ,,,                                                               ..................................
              ,,,,,                                                              .................................
              ,,,,,,,,,,,                                                        .............................N...
              ,,,,,,,,                                                             ...............................
              ,,g,,,,,,,,,,,,,                                                        ............................
              ,,,,,,,,,,,,,,,,,,,,                                                    ............................
              ,,,,,,,,,,,,,,,,,,,                                                       ..........................
              ,,,,,,,,,,,,,,,,,,,,,,                                                    ..........................
              ,,,,,,,,,,,,,,,,,,,,,,,                                                       ......................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ..................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ..................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                       .................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                       ................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ...............
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                         ............
              ,,,,,,,,,,,,a,,,,,,,,,,,,,,,,,,,                                                             .......
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                            ......
              ,,a,,,a,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                              ....
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                             ....
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                                .
                                                                                                                 .

```





### See also


 *  https://www.biostars.org/p/147136/
 *  https://www.biostars.org/p/178308






## Motivation

 Soft clip BAM files based on PCR target regions https://www.biostars.org/p/147136/

* mapping quality is set to zero if a read on strand - overlap the 5' side of the PCR fragment
* mapping quality is set to zero if a read on strand + overlap the 3' side of the PCR fragment
* mapping quality is set to zero if no PCR fragment is found
* after processing the BAM file should be sorted, processed with samtools calmd and picard fixmate


## Example


```bash
echo  "seq2\t1100\t1200" > test.bed
java -jar dist-1.133/pcrclipreads.jar -B test.bed -b  samtools-0.1.19/examples/ex1.bam  |\
	samtools  view -q 1 -F 4 -bu  -  |\
	samtools  sort - clipped && samtools index clipped.bam

samtools tview -p seq2:1100  clipped.bam  samtools-0.1.19/examples/ex1.fa
```
output:

![http://i.imgur.com/bjDEnMW.jpg](http://i.imgur.com/bjDEnMW.jpg)

```
    1091      1101      1111      1121      1131      1141      1151      1161      1171      1181      1191
AAACAAAGGAGGTCATCATACAATGATAAAAAGATCAATTCAGCAAGAAGATATAACCATCCTACTAAATACATATGCACCTAACACAAGACTACCCAGATTCATAAAACAAATNNNNN
              ...................................                               ..................................
              ,,,                                                               ..................................
              ,,,,,                                                              .................................
              ,,,,,,,,,,,                                                        .............................N...
              ,,,,,,,,                                                             ...............................
              ,,g,,,,,,,,,,,,,                                                        ............................
              ,,,,,,,,,,,,,,,,,,,,                                                    ............................
              ,,,,,,,,,,,,,,,,,,,                                                       ..........................
              ,,,,,,,,,,,,,,,,,,,,,,                                                    ..........................
              ,,,,,,,,,,,,,,,,,,,,,,,                                                       ......................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ..................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ..................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                       .................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                       ................
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                        ...............
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                         ............
              ,,,,,,,,,,,,a,,,,,,,,,,,,,,,,,,,                                                             .......
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                            ......
              ,,a,,,a,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                              ....
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                             ....
              ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                                                                .
                                                                                                                 .
```




