# VCFTabixml

 annotate a value from a vcf+xml file


## Usage

```
Usage: vcftabixml [options] Files
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
  * -B
       BED file indexed with tabix. The 4th column *is* a XML string.)
  * -F
      file containing extra INFO headers line to add version: 4.1
  * -xsl
      x xslt-stylesheet. REQUIRED. Should produce a valid set of INFO field.

```


## Keywords

 * vcf
 * xml


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
$ make vcftabixml
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

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcftabixml/VCFTabixml.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/vcftabixml/VCFTabixml.java)


<details>
<summary>Git History</summary>

```
Mon May 15 10:41:51 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/c13a658b2ed3bc5dd6ade57190e1dab05bf70612
Tue Apr 18 17:44:13 2017 +0200 ; javacc + samfilter ; https://github.com/lindenb/jvarkit/commit/695e7cb606ba96feeeabbd2a359aacd38cf36ae0
Mon Jun 8 17:24:41 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/f9a941d604f378ff40a32666c8381cb2450c7cfa
Fri Jun 5 12:42:21 2015 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/cc909f9f4ceea181bb65e4203e3fdbde176c6f2f
Mon Jun 1 15:27:11 2015 +0200 ; change getChrom() to getContig() ; https://github.com/lindenb/jvarkit/commit/5abd60afcdc2d5160164ae6e18087abf66d8fcfe
Mon May 12 14:06:30 2014 +0200 ; continue moving to htsjdk ; https://github.com/lindenb/jvarkit/commit/011f098b6402da9e204026ee33f3f89d5e0e0355
Mon May 12 10:28:28 2014 +0200 ; first sed on files ; https://github.com/lindenb/jvarkit/commit/79ae202e237f53b7edb94f4326fee79b2f71b8e8
Mon Oct 21 11:56:13 2013 +0200 ; Merge branch 'master' of https://github.com/lindenb/jvarkit ; https://github.com/lindenb/jvarkit/commit/e42d75062273a78b07c4a2efe3647aa9e36853cb
Fri Oct 11 15:39:02 2013 +0200 ; picard v.100: deletion of VcfIterator :-( ; https://github.com/lindenb/jvarkit/commit/e88fab449b04aed40c2ff7f9d0cf8c8b6ab14a31
Wed Oct 9 17:36:01 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/05b7b3b975cb3c2b0dc8aa674a5fa26e10778bb2
Fri Oct 4 17:21:55 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/33910ef83d0c7ff92d6506173c4f76524d0bed30
Wed Sep 18 17:39:34 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/7713393b28bc5004c6d8047a24bb9b84d2294fef
Wed Sep 18 05:09:21 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/500379da9ebbe900bce535de7bccc272e632b70d
Tue Sep 17 18:57:12 2013 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/47456854eb086ecafa8760b5bfd51a5e20a90376
Fri Sep 6 15:11:11 2013 +0200 ; moved code for latest version of picard (1.97). Using VCFIterator instead of ASciiLineReader ; https://github.com/lindenb/jvarkit/commit/810877c10406a017fd5a31dacff7e8401089d429
Fri Jul 26 12:42:54 2013 +0200 ; vcfrabixml ; https://github.com/lindenb/jvarkit/commit/6ef20a70315a073f63dd495619a9a2f36b62c56d
Sun Jul 21 14:17:59 2013 +0200 ; vcf trios, added git HASH in METAINF/Manifest ; https://github.com/lindenb/jvarkit/commit/1854d3695563b91471861164f5e8903042493470
Fri Jul 19 18:23:32 2013 +0200 ; code for fixvcf ; https://github.com/lindenb/jvarkit/commit/99cb799aac12c5b6217e830f403e0e8ce5cf7bf6
Tue Jul 16 19:56:40 2013 +0200 ; disease ontology ; https://github.com/lindenb/jvarkit/commit/3ca2d21908b547d021a7a58e56693956f395a167
Mon May 6 18:56:46 2013 +0200 ; moving to git ; https://github.com/lindenb/jvarkit/commit/55158d13f0950f16c4a3cc3edb92a87905346ee1
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcftabixml** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)



4th column of the BED indexed with TABIX is a XML string.
It will be processed with the xslt-stylesheet and should procuce a xml result <properties><entry key='key1'>value1</property><property key='key2'>values1</property></properies>
INFO fields. Carriage returns will be removed." 
Parameters to be passed to the stylesheet: vcfchrom (string) vcfpos(int) vcfref(string) vcfalt(string). 


## Example

The header of the tabix-index BED+XML file:
```
1	69427	69428	<snpList><positionString>1:69428</positionString><chrPosition>69428</chrPosition><alleles>G/T</alleles><ua..
1	69475	69476	<snpList><positionString>1:69476</positionString><chrPosition>69476</chrPosition><alleles>C/T</alleles><ua..
1	69495	69496	<snpList><positionString>1:69496</positionString><chrPosition>69496</chrPosition><alleles>A/G</alleles><ua..
1	69510	69511	<snpList><positionString>1:69511</positionString><chrPosition>69511</chrPosition><alleles>G/A</alleles><ua...
1	69589	69590	<snpList><positionString>1:69590</positionString><chrPosition>69590</chrPosition><alleles>A/T</alleles><ua...
1	69593	69594	<snpList><positionString>1:69594</positionString><chrPosition>69594</chrPosition><alleles>C/T</alleles><ua..
1	69619	69620	<snpList><positionString>1:69620</positionString><chrPosition>69620</chrPosition><alleles>T/TA</alleles><u...
1	69744	69745	<snpList><positionString>1:69745</positionString><chrPosition>69745</chrPosition><alleles>CA/C</alleles><u..
1	69760	69761	<snpList><positionString>1:69761</positionString><chrPosition>69761</chrPosition><alleles>T/A</alleles><ua...
1	801942	801943	<snpList><positionString>1:801943</positionString><chrPosition>801943</chrPosition><alleles>T/C</alleles...
(...)
```
The content of the tags.txt file:
```
##INFO=<ID=EVS_AAMAF,Number=1,Type=String,Description="EVS_AAMAF from EVS">
##INFO=<ID=EVS_AVGSAMPLEREADDEPTH,Number=1,Type=String,Description="EVS_AVGSAMPLEREADDEPTH from EVS">
##INFO=<ID=EVS_CLINICALLINK,Number=1,Type=String,Description="EVS_CLINICALLINK from EVS">
##INFO=<ID=EVS_CONFLICTALT,Number=1,Type=String,Description="EVS_CONFLICTALT from EVS">
##INFO=<ID=EVS_CONSERVATIONSCOREGERP,Number=1,Type=String,Description="EVS_CONSERVATIONSCOREGERP from EVS">
##INFO=<ID=EVS_CONSERVATIONSCORE,Number=1,Type=String,Description="EVS_CONSERVATIONSCORE from EVS">
##INFO=<ID=EVS_GENELIST,Number=1,Type=String,Description="EVS_GENELIST from EVS">
##INFO=<ID=EVS_GWASPUBMEDIDS,Number=1,Type=String,Description="EVS_GWASPUBMEDIDS from EVS">
##INFO=<ID=EVS_ONEXOMECHIP,Number=1,Type=String,Description="EVS_ONEXOMECHIP from EVS">
##INFO=<ID=EVS_RSIDS,Number=1,Type=String,Description="EVS_RSIDS from EVS">
##INFO=<ID=EVS_TOTALMAF,Number=1,Type=String,Description="EVS_TOTALMAF from EVS">
##INFO=<ID=EVS_UAMAF,Number=1,Type=String,Description="EVS_UAMAF from EVS">
```
The XSLT file:

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 
<xsl:output method="xml"/>
<xsl:param name="vcfchrom"/>
<xsl:param name="vcfpos"/>
<xsl:param name="vcfref"/>
<xsl:param name="vcfalt"/>
<xsl:template match="/">
<properties>
<xsl:apply-templates select="evsData|snpList"/>
</properties>
</xsl:template>
<xsl:template match="evsData">
<xsl:a* 
 * @author lindenb
 *pply-templates select="snpList"/>
</xsl:template>
<xsl:template match="snpList">
<xsl:choose>
 <xsl:when test="chromosome=$vcfchrom and chrPosition=$vcfpos and refAllele=$vcfref">
<xsl:apply-templates select="clinicalLink|rsIds|uaMAF|aaMAF|totalMAF|avgSampleReadDepth|geneList|conservationScore|conservationScoreGERP|gwasPubmedIds|onExomeChip|gwasPubmedIds"/>
  <xsl:if test="altAlleles!=$vcfalt">
  	<entry key="EVS_CONFLICTALT">
  		<xsl:value-of select="altAlleles"/>
  	</entry>		
  	</xsl:if>
  </xsl:when>
  <xsl:otherwise/>
</xsl:choose>
</xsl:template>
<xsl:template match="clinicalLink|rsIds|uaMAF|aaMAF|totalMAF|avgSampleReadDepth|geneList|conservationScore|conservationScoreGERP|onExomeChip|gwasPubmedIds">
<xsl:if test="string-length(normalize-space(text()))&gt;0">
<entry>
<xsl:attribute name="key"><xsl:value-of select="concat('EVS_',translate(name(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'))"/></xsl:attribute>
<xsl:value-of select="text()"/>
</entry>
</xsl:if>
</xsl:template>
</xsl:stylesheet>
```

Running:

```bash
java -jar dist/vcftabixml.jar \
	IN=input.vcf.gz \
	XSL=evs2vcf.xsl \
	BEDFILE=evs.data.gz \
	TAGS=tags.txt | grep -v "##" | head

#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	U0925
A	613326	.	G	A	182	.	AC1=1;AF1=0.5;DP=327;DP4=0,147,0,178;FQ=163;MQ=47;PV4=1,0.21,1.2e-35,1;RPB=6.999638e+00;VDB=8.105711e-05	GT:PL:DP:GQ	0/1:212,0,190:325:99
A	614565	.	A	G	222	.	AC1=2;AF1=1;DP=910;DP4=0,1,71,836;EVS_AAMAF=23.2659;EVS_AVGSAMPLEREADDEPTH=70;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.5;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs26;EVS_TOTALMAF=30.5519;EVS_UAMAF=33.7209;FQ=-282;MQ=41;PV4=1,0.26,0.032,1;RPB=1.705346e+00;VDB=1.656917e-35	GT:PL:DP:GQ	1/1:255,255,0:908:99
A	614379	.	C	T	225	.	AC1=1;AF1=0.5;DP=979;DP4=33,440,37,456;EVS_AAMAF=2.8179;EVS_AVGSAMPLEREADDEPTH=59;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-4.7;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs249;EVS_TOTALMAF=8.8261;EVS_UAMAF=11.4393;FQ=225;MQ=42;PV4=0.8,1,3.8e-152,1;RPB=1.317662e+01;VDB=3.857882e-21	GT:PL:DP:GQ	0/1:255,0,255:966:99
A	614565	.	T	G	222	.	AC1=2;AF1=1;DP=209;DP4=0,0,0,187;FQ=-282;MQ=46;VDB=2.410569e-12	GT:PL:DP:GQ	1/1:255,255,0:187:99
A	614953	.	C	T	225	.	AC1=1;AF1=0.5;DP=810;DP4=197,214,194,195;EVS_AAMAF=2.4603;EVS_AVGSAMPLEREADDEPTH=45;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.1;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs733;EVS_TOTALMAF=7.0506;EVS_UAMAF=9.4205;FQ=225;MQ=47;PV4=0.62,1,9.2e-58,0.015;RPB=4.560133e+00;VDB=6.880316e-03	GT:PL:DP:GQ	0/1:255,0,255:800:99
A	614922	.	G	A	130	.	AC1=1;AF1=0.5;DP=183;DP4=0,94,0,86;EVS_AAMAF=2.3053;EVS_AVGSAMPLEREADDEPTH=43;EVS_CLINICALLINK=unknown;EVS_CONSERVATIONSCORE=0.0;EVS_CONSERVATIONSCOREGERP=-3.0;EVS_GENELIST=KCNAB2;EVS_GWASPUBMEDIDS=unknown;EVS_ONEXOMECHIP=false;EVS_RSIDS=rs202;EVS_TOTALMAF=7.1215;EVS_UAMAF=9.6386;FQ=133;MQ=44;PV4=1,0.0065,3.7e-51,1;RPB=3.500959e+00;VDB=9.915205e-29	GT:PL:DP:GQ	0/1:160,0,184:180:99
A	614986	.	G	C	188	.	AC1=2;AF1=1;DP=176;DP4=0,0,0,175;FQ=-282;MQ=46;VDB=0.000000e+00	GT:PL:DP:GQ	1/1:221,255,0:175:99
A	615009	.	T	A	125	.	AC1=1;AF1=0.5;DP=103;DP4=45,0,56,0;FQ=120;MQ=45;PV4=1,0.14,2.8e-19,1;RPB=1.520268e+00;VDB=1.539079e-06	GT:PL:DP:GQ	0/1:155,0,148:101:99
A	615037	.	C	T	161	.	AC1=1;AF1=0.5;DP=353;DP4=0,164,0,165;FQ=110;MQ=48;PV4=1,1,1.1e-23,1;RPB=5.549816e+00;VDB=1.486773e-11	GT:PL:DP:GQ	0/1:191,0,137:329:99
```


