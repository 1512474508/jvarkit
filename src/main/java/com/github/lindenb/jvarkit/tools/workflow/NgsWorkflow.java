package com.github.lindenb.jvarkit.tools.workflow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;

public class NgsWorkflow extends AbstractNgsWorkflow
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(NgsWorkflow.class);
	
	private enum RefSplitType {WHOLE_GENOME,WHOLE_CONTIG,INTERVAL};
	
	private abstract class RefSplit
		{
		abstract RefSplitType getType();
		abstract String getToken();
		}
	
	private class NoSplit extends RefSplit
		{
		@Override RefSplitType getType() { return RefSplitType.WHOLE_GENOME;}
		@Override String getToken() { return "";}
		}
	
	private class ContigSplit extends RefSplit
		{
		final String contig;
		ContigSplit(final String contig) {this.contig=contig;}
		public String getContig() {return contig;}
		@Override RefSplitType getType() { return RefSplitType.WHOLE_CONTIG;}
		@Override String getToken() { return "."+getContig();}
		}

	
	private class IntervalSplit extends RefSplit
		{
		final Interval interval;
		IntervalSplit(final Interval interval) {this.interval=interval;
			if(interval.getStart()>=interval.getEnd()) throw new IllegalArgumentException(interval.toString());
			}
		public Interval getInterval() {return interval;}
		@Override RefSplitType getType() { return RefSplitType.INTERVAL;}
		@Override String getToken() { return "."+getInterval().getContig()+"_"+getInterval().getStart()+"_"+getInterval().getEnd();}
		}
		
	private static interface PropertyKey
		{
		public String getKey();
		public String getDescription();
		public String getDefaultValue();
		}
	
	private static class PropertyKeyBuilder
		{
		private final PropertyKeyImpl prop;
		PropertyKeyBuilder(final String key)
			{
			if(NgsWorkflow.name2propertyKey.containsKey(key))
				{
				throw new IllegalStateException(key);
				}
			this.prop = new PropertyKeyImpl(key);
			}
		PropertyKeyBuilder description(final String s) { this.prop.description=s;return this;}
		PropertyKeyBuilder def(final String s) { this.prop.def=s;return this;}
		PropertyKeyBuilder def(final boolean b) {return def(String.valueOf(b));}
		PropertyKeyBuilder def(final int b) {return def(String.valueOf(b));}
		
		PropertyKey build() {
			NgsWorkflow.name2propertyKey.put(this.prop.key,this.prop);
			return this.prop;
			}
		private  static class PropertyKeyImpl implements PropertyKey
			{
			final String key;
			String description="No Description available";
			String def;
			PropertyKeyImpl(final String key) {
				this.key=key;
				
			}
			
			public @Override String getKey() { return this.key;}
			public @Override String getDescription() { return this.description;}
			public @Override String getDefaultValue() { return this.def;}
			
			@Override
			public int hashCode() {
				return key.hashCode();
				}
			@Override
			public boolean equals(final Object obj) {
				if(obj==this) return true;
				if(obj==null || !(obj instanceof PropertyKeyImpl)) return false;
				return this.key.equals(PropertyKeyImpl.class.cast(obj).key);
				}
			@Override
			public String toString() {
				return this.key.toString();
				}
			}
		
		}
	
	private static final Map<String,PropertyKey> name2propertyKey=new HashMap<>();
	private static PropertyKeyBuilder key(final String key) { return new PropertyKeyBuilder(key);}
	
	
	private static final PropertyKey PROP_PROJECT_OUTPUT_DIRECTORY = key("output.directory").
			description("Project output directory").
			build();
	
	private static final PropertyKey PROP_TMP_PREFIX_TOKEN = key("tmp.prefix").
			description("Temporary Files prefix").
			def("__DELETE__").
			build();
	
	private static final PropertyKey PROP_FILES_PREFIX_TOKEN = key("prefix").
		description("Files prefix").
		build();
	
	private static final PropertyKey PROP_CUTADAPT_ADAPTER5 = key("cutadapt.adapter5").
		description("Cutadapt 5' adapter").
		def("CTACACTCTTTCCCTACACGACGCTCTTCCGATCT").
		build();
	
	private static final PropertyKey PROP_CUTADAPT_ADAPTER3 = key("cutadapt.adapter3").
		description("Cutadapt 3' adapter").
		def("GATCGGAAGAGCACACGTCTGAACTCCAGTCAC").
		build();
	
	private static final PropertyKey PROP_BWA_MEM_NTHREADS = key("bwa.mem.nthreads").
			description("Number of threads for bwa mem").
			def("1").
			build();
	
	private static final PropertyKey PROP_SAMTOOLS_SORT_NTHREADS = key("samtools.sort.nthreads").
			description("Number of threads for samtools sort").
			def(1).
			build();

	private static final PropertyKey PROP_PICARD_MARKDUP_REMOVES_DUPLICATES = key("markdups.removes.duplicates").
			description("picard MarkDuplicate will remove the dups").
			def(false).
			build();
	
	private static final PropertyKey PROP_CAPTURE_EXTEND = key("capture.extend").
			description("How to extend the capture bed").
			def(1000).
			build();

	private static final PropertyKey PROP_DEFAULT_COMPRESSION_LEVEL = key("compression.level").
			description("Compression level").
			def(9).
			build();
	
	private static final PropertyKey PROP_DISABLE_RECALIBRATION = key("disable.recalibration").
			description("Disable GATK Recalibration").
			def(false).
			build();
	private static final PropertyKey PROP_DISABLE_REALIGN = key("disable.realign").
			description("Disable GATK RealignAroundIndel").
			def(false).
			build();
	private static final PropertyKey PROP_DISABLE_MARKDUP = key("disable.markdup").
			description("Disable picard MarkDups").
			def(false).
			build();
	private static final PropertyKey PROP_PROJECT_IS_HALOPLEX = key("haloplex").
			description("Project is Haloplex").
			def(false).
			build();
	private static final PropertyKey PROP_DISABLE_CUTADAPT = key("disable.cutadapt").
			description("Disable cutadapt").
			def(false).
			build();
	private static final PropertyKey PROP_GATK_HAPCALLER_NCT = key("gatk.hapcaller.nct").
			description("Hapcaller -nct").
			def("1").
			build();

	private RefSplit parseRefSplitFromStr(final String s)
		{
		int colon= s.indexOf(":");
		if(colon!=-1)
			{
			int hyphen = s.lastIndexOf('-', colon+1);
			if(hyphen<0) throw new IllegalArgumentException("Illegal array item "+s);
			final Interval interval = new Interval(
					s.substring(0,colon),
					Integer.parseInt(s.substring(colon+1,hyphen)),
					Integer.parseInt(s.substring(hyphen+1))
					);
			return new IntervalSplit(interval);
			}
		else
			{
			return new ContigSplit(s);
			}
		}
	
	private List<RefSplit> parseRefSplitList(final JsonElement root) throws IOException
		{
		final List<RefSplit> splits=new ArrayList<>();
		if(root.isJsonObject())
			{
			JsonObject ob = root.getAsJsonObject();
			final Interval interval = new Interval(
					ob.get("chrom").getAsString(),
					ob.get("start").getAsInt(),
					ob.get("end").getAsInt()
					);
			return Collections.singletonList(new IntervalSplit(interval));
			}
		else if(root.isJsonArray())
			{
			final JsonArray array=root.getAsJsonArray();
			if(array.size()==0) throw new IOException("Empty array");
			for(int i=0; i< array.size();++i) {
				JsonElement item = array.get(i);
				if(item.isJsonPrimitive())
					{
					splits.add(parseRefSplitFromStr(item.getAsString()));
					}
				else if(item.isJsonObject())
					{
					JsonObject ob = item.getAsJsonObject();
					final Interval interval = new Interval(
							ob.get("chrom").getAsString(),
							ob.get("start").getAsInt(),
							ob.get("end").getAsInt()
							);
					splits.add(new IntervalSplit(interval));
					}
				else
					{
					 throw new IOException("Illegal array item "+item);
					}
				}
			return splits;
			}
		else if(root.isJsonPrimitive())
			{
			final String str=root.getAsString();
			if(str.endsWith(".bed"))
				{
				splits.addAll(Files.readAllLines(Paths.get(str)).stream().
					filter(L->!L.startsWith("browser")).
					filter(L->!L.startsWith("track")).
					filter(L->!L.startsWith("#")).
					filter(L->!L.trim().isEmpty()).
					map(L->L.split("[\t]")).
					map(T->new Interval(T[0],1+Integer.parseInt(T[1]),Integer.parseInt(T[2]))).
					map(I->new IntervalSplit(I)).
					collect(Collectors.toList())
					);
				return splits;
				}
			else if(str.endsWith(".json"))
				{
				splits.addAll(parseRefSplitList(readJsonFile(new File(str))));
				return splits;
				}
			else if(str.endsWith(".intervals"))//gatk style
				{
				splits.addAll(Files.readAllLines(Paths.get(str)).stream().
						filter(L->!L.startsWith("#")).
						filter(L->!L.trim().isEmpty()).
						map(L-> parseRefSplitFromStr(L)).
						collect(Collectors.toList())
						);
				return splits;
				}
			else if(str.equals("genome_wide"))
				{
				return Collections.singletonList(new NoSplit());
				}
			else if(str.equals("by_chromosome"))
				{
				for(int i=1;i<=22;++i) splits.add(new ContigSplit(String.valueOf(i)));
				splits.add(new ContigSplit("X"));
				splits.add(new ContigSplit("Y"));
				return splits;
				}
			else 
				{
				return Collections.singletonList(parseRefSplitFromStr(str));
				}
			}
		else
			{
			throw new IOException("illegal refSplit "+root);
			}
		}
	
	private abstract class HasAttributes
		{
		private final Map<PropertyKey,String> _att=new HashMap<>();
		
		protected HasAttributes(final JsonElement root) throws IOException
			{
			if(root!=null && root.isJsonObject())
				{
				for(final PropertyKey prop:NgsWorkflow.name2propertyKey.values())
					{
					if(!root.getAsJsonObject().has(prop.getKey())) continue;
					this._att.put(prop, root.getAsJsonObject().get(prop.getKey()).getAsString());
					}
				}
			}
		
		public abstract HasAttributes getParent();
		public Map<PropertyKey,String> getAttributes() { return _att;}
		
		public String getAttribute(final PropertyKey key,final String def)
			{
			if(key==null) throw new NullPointerException("key is null");
			HasAttributes curr=this;
			while(curr!=null)
				{
				if(curr.getAttributes().containsKey(key))
					{
					return curr.getAttributes().get(key);
					}
				curr=curr.getParent();
				}
			if(def!=null) return def;
			if(key.getDefaultValue()!=null) return key.getDefaultValue();
			throw new RuntimeException("key "+key+" is not defined for "+this.toString());
			}
		
		public String getAttribute(final PropertyKey key)
			{
			return getAttribute(key,null);
			}
		
		public boolean isAttributeSet(final PropertyKey key,final Boolean def)
			{
			final String s= getAttribute(key,(def==null?null:def.booleanValue()?"true":"false"));
			if(s==null) throw new RuntimeException("key "+key+" is not defined for "+this.toString());
			if(s.equals("1") || s.equals("true") || s.equals("yes") || s.equals("T") || s.equals("Y")) return true;
			if(s.equals("0") || s.equals("false") || s.equals("no") || s.equals("F") || s.equals("N")) return false;
			throw new RuntimeException("bad boolean value for "+key+" : "+s);
			}
		
		public boolean isAttributeSet(final PropertyKey key)
			{
			return isAttributeSet(key,null);
			}
		
		public String getTmpPrefixToken()
			{
			return getAttribute(PROP_TMP_PREFIX_TOKEN);
			}
		public String getFilePrefix()
			{
			return getAttribute(PROP_FILES_PREFIX_TOKEN);
			}
		public String getTmpPrefix()
			{
			return getTmpPrefixToken()+getFilePrefix();
			}
		@Override
		public abstract String toString();
		
		protected String rulePrefix()
			{
			return "\tmkdir -p $(dir $@) && [[ ! -f \"${OUT}/STOP\" ]]  ";
			}

		}
	
	
	/** describe a NGS project */
	private class Project extends HasAttributes
		{
		private final String name;
		private final String description;
		private final List<Sample> _samples=new ArrayList<>();
		private final Optional<Capture> capture;
		private final Pedigree pedigree;
		Project(final JsonElement root) throws IOException
			{
			super(root);
			if(!root.isJsonObject()) throw new IOException("project json is not object");
			final JsonObject json = root.getAsJsonObject();
			
			if(!json.has("name")) {
				throw new IOException("project name missing");
			}
			this.name = json.get("name").getAsString();
			if(!json.has("description")) {
				this.description=this.name;
			} else
			{
				this.description = json.get("description").getAsString();
			}
			
			if(json.has("capture")) {
				final Capture c=new Capture(this,json.get("capture"));
				capture = Optional.of(c);
				}
			else
				{
				this.capture =Optional.empty();
				}
			
			if(json.has("pedigree")) {
				this.pedigree = new Pedigree(this,json.get("pedigree"));
				}
			else
				{
				this.pedigree = new Pedigree(this,new JsonObject());
				}
			
			final Set<String> seen=new HashSet<>(); 
			if(json.has("samples")) {
				for(final JsonElement samplejson :json.get("samples").getAsJsonArray())
					{
					Sample sample =new Sample(this,samplejson.getAsJsonObject());
					this._samples.add(sample);
					if(seen.contains(sample.getName()))
						{
						throw new IOException("Sample "+sample+" defined twice");
						}
					seen.add(sample.getName());
					}
				}
			else
				{
				LOG.warn("No 'samples' under project");
				}
			}
		
		public String getName() {
			return name;
			}
		
		public String getDescription() {
			return description;
		}
		
		public Pedigree getPedigree() {
			return pedigree;
		}
		
		public List<Sample> getSamples() { return this._samples;}
		@Override  public HasAttributes getParent() { return null;}
		
		public  String getOutputDirectory() {
			return getAttribute(PROP_PROJECT_OUTPUT_DIRECTORY);
			}
		public  String getSamplesDirectory() {
			return "${OUT}/Samples";
			}
		public  String getBedDirectory() {
			return "${OUT}/BED";
			}
		
		public  String getVcfDirectory() {
			return "${OUT}/VCF";
			}
		

		
		boolean hasCapture()
			{
			return this.capture.isPresent();
			}
		public Capture getCapture()
			{
			return this.capture.get();
			}
		
		public  String getHapCallerGenotypedVcf() {
			return getVcfDirectory()+"/"+getTmpPrefix()+"Genotyped.vcf.gz";
			}

		
		public List<RefSplit> getHaplotypeCallerSplits() {
			final List<RefSplit> chroms=new ArrayList<>(25);
			for(int i=1;i<=22;++i) chroms.add(new ContigSplit(String.valueOf(i)));
			chroms.add(new ContigSplit("X"));
			chroms.add(new ContigSplit("Y"));
			return chroms;
			}
		
		public StringBuilder haplotypeCaller() {
			final StringBuilder w=new StringBuilder();
			final List<String> vcfParts=new ArrayList<>();
			final List<RefSplit> refSplits = getHaplotypeCallerSplits();
			if(refSplits.isEmpty()) throw new IllegalArgumentException();
			
			for(final RefSplit split: refSplits)
				{
				final String vcfPart;
				switch(split.getType()) {
					case WHOLE_GENOME:  vcfPart= getHapCallerGenotypedVcf();break;
					default:  vcfPart= "$(addsuffix "+split.getToken()+".vcf.gz,"+getHapCallerGenotypedVcf()+")";
					}
				
				vcfParts.add(vcfPart);
				
				w.append(vcfPart).append(":").append(getFinalBamList());
				w.append(" ").append(getPedigree().getPedFilename());
				if( this.hasCapture())
	            	{
					w.append(" ").append(getCapture().getExtendedFilename());
	            	}
				w.append("\n");
				w.append(rulePrefix()+" && ");
				
				if( this.hasCapture())
	            	{
					switch(split.getType())
						{
						case WHOLE_GENOME: break;
						case INTERVAL:{
								IntervalSplit tmp= IntervalSplit.class.cast(split);
								w.append(" awk -F '\t' '($$1==\""+tmp.getInterval().getContig()+" && !("+ 
										tmp.getInterval().getEnd()+" < int($$2) || int($$3) <"+
										tmp.getInterval().getStart()+")' "+getCapture().getExtendedFilename()+" > $(addsuffix .bed,$@) && "); 
								break;
								}
						case WHOLE_CONTIG:
								{
								ContigSplit tmp= ContigSplit.class.cast(split);
								w.append(" awk -F '\t' '($$1==\""+tmp.getContig()+"\")' "+getCapture().getExtendedFilename()+" > $(addsuffix .bed,$@) && ");
								break;
								}
						default: throw new IllegalStateException();
						}
	            	}
				
				w.append(" $(java.exe)  -XX:ParallelGCThreads=5 -Xmx2g  -Djava.io.tmpdir=$(dir $@) -jar $(gatk.jar) -T HaplotypeCaller ");
				w.append("	-R $(REF) ");
				w.append("	--validation_strictness LENIENT ");
				w.append("	-I $< -o \"$(addsuffix .tmp.vcf.gz,$@)\" ");
				//w.print("	--num_cpu_threads_per_data_thread "+  this.getIntProperty("base-recalibrator-nct",1));
				w.append("	-l INFO ");
				w.append("	-nct ").append(getAttribute(PROP_GATK_HAPCALLER_NCT));
				
				w.append("	--dbsnp \"$(gatk.bundle.dbsnp.vcf)\" ");
				w.append(" $(foreach A, PossibleDeNovo AS_FisherStrand AlleleBalance AlleleBalanceBySample BaseCountsBySample GCContent ClippingRankSumTest , --annotation ${A} ) ");
				w.append(" --pedigree ").append(getPedigree().getPedFilename());
				if( this.hasCapture())
		            {
					switch(split.getType())
						{
						case WHOLE_GENOME: 	w.append(" -L:"+getCapture().getName()+",BED "+getCapture().getExtendedFilename());
						case INTERVAL: //through...
						case WHOLE_CONTIG: w.append(" -L $(addsuffix .bed,$@) ");break;
						default: throw new IllegalStateException();
						}
		            }
				else
					{
					switch(split.getType())
						{
						case WHOLE_GENOME: /* nothing */ break;
						case INTERVAL: {
							IntervalSplit tmp= IntervalSplit.class.cast(split);
							w.append(" -L \""+tmp.getInterval().getContig()+":"+ tmp.getInterval().getStart()+"-"+tmp.getInterval().getEnd()+"\" ");
							break;
							}
						case WHOLE_CONTIG: w.append(" -L ").append(ContigSplit.class.cast(split).getContig());break;
						default: throw new IllegalStateException();
						}					
					}
				w.append(" && mv --verbose \"$(addsuffix .tmp.vcf.gz,$@)\" \"$@\" ");
				w.append(" && mv --verbose \"$(addsuffix .tmp.vcf.gz.tbi,$@)\" \"$(addsuffix .tbi,$@)\" ");
				
				if( this.hasCapture())
	            	{
					w.append(" && rm --verbose \"$(addsuffix .bed,$@)\" ");
	            	}
				w.append("\n");
				}
			
			if(vcfParts.size()>1) {
				w.append(getHapCallerGenotypedVcf()).append(":");
				for(final String vcfPart:vcfParts) {
					w.append(" \\\n\t").append(vcfPart);
					}
				w.append("\n");
				
				w.append(rulePrefix()+" && ${java.exe}   -Djava.io.tmpdir=$(dir $@)  -jar ${gatk.jar}  -T CombineVariants -R $(REF) "
						+ " -o $(addsuffix .tmp.vcf.gz,$@) -genotypeMergeOptions UNSORTED "
						);
				for(int k=0;k<vcfParts.size();++k) {
					w.append(" --variant:v").append(k).append(" ").append(vcfParts.get(k)).append(" ");
					}

				w.append(" && mv --verbose \"$(addsuffix .tmp.vcf.gz,$@)\" \"$@\" ");
				w.append(" && mv --verbose \"$(addsuffix .tmp.vcf.gz.tbi,$@)\" \"$(addsuffix .tbi,$@)\" ");
				w.append("\n");
				}
			
			return w;
			}
		

		String getFinalBamList() {
			return getSamplesDirectory()+"/"+getTmpPrefix()+"final.bam.list";
			}
		
		void bamList(final PrintWriter w) {
			w.println(getFinalBamList()+":"+this.getSamples().stream().map(S->S.getFinalBamBai()).collect(Collectors.joining(" \\\n\t")));
			w.print(rulePrefix()+ " && rm -f $@ $(addsuffix .tmp,$@) ");
			this.getSamples().stream().forEach(S->{
				w.print(" && echo \""+S.getFinalBam()+"\" >> $(addsuffix .tmp,$@) ");
				});
			w.println("&& mv --verbose $(addsuffix .tmp,$@) $@");
			}
		
		@Override
		public String toString() {
			return getName();
			}
		}
	
	/** describe a Pedigree capture */
	private class Pedigree extends HasAttributes
		{
		private final Project project;
		private final String pedFilename;
		private final boolean providedByUser;
		Pedigree( final Project project,final JsonElement root) throws IOException
			{
			super(root);
			this.project=project;
			if(root!=null && root.isJsonObject() && root.getAsJsonObject().has("ped"))
				{
				final JsonObject json=root.getAsJsonObject();
				this.pedFilename=json.get("ped").getAsString();
				this.providedByUser = true;
				}
			else if(root!=null && root.isJsonPrimitive())
				{
				this.pedFilename=root.getAsString();
				this.providedByUser = true;
				}
			else
				{
				this.providedByUser = false;
				this.pedFilename = project.getOutputDirectory()+"/PED/"+getTmpPrefix()+"pedigree.ped";
				}
			}
		public boolean isProvidedByUser() {
			return providedByUser;
			}
		public String getPedFilename() {
			return pedFilename;
			}
		public Project getProject() {
			return project;
			}
		@Override
		public Project getParent() {
			return getProject();
			}
		void build(final PrintWriter w)
			{
			if(isProvidedByUser()) return;
			w.println(getPedFilename()+":");
			w.println(rulePrefix()+" && rm --verbose -f $@ $(addsuffix .tmp.ped,$@)");
			for(final Sample sample: getProject().getSamples())
				{
				w.print("\techo '");
				w.print(String.join("\t",sample.getFamilyId(),sample.getName(),sample.getFatherId(),sample.getMotherId(),sample.getSex(),sample.getStatus()));
				w.println("' >> $(addsuffix .tmp.ped,$@)");
				}
			w.println("\tmv  --verbose $(addsuffix .tmp.ped,$@) $@");
			}
		
		@Override
		public String toString() {
			return getPedFilename();
			}
		}
	
	/** describe a BED capture */
	private class Capture extends HasAttributes
		{
		private final Project project;
		private Integer extend=null;
		private String name="capture";
		private final String bed;
		
		Capture( final Project project,final JsonElement root) throws IOException
			{
			super(root);
			this.project=project;
			if(root.isJsonObject())
				{
				final JsonObject json=root.getAsJsonObject();
				this.bed=json.get("bed").getAsString();
				if(json.has("extend"))
					{
					this.extend=json.get("extend").getAsInt();
					}
				if(json.has("name"))
					{
					this.name=json.get("name").getAsString();
					}
				}
			else if(root.isJsonPrimitive())
				{
				bed=root.getAsString();
				}
			else
				{
				throw new IOException("bad capture");
				}
			}
		
		public String getBedFilename()
			{
			return this.bed;
			}
		public String getName() {
			return name;
			}
		public String getNormalizedFilename()
			{
			return getProject().getBedDirectory()+"/"+getName()+".normalized.bed";
			}
		
		public String getExtendedFilename()
			{
			return getProject().getBedDirectory()+"/"+getName()+".extended"+getExtend()+".bed";
			}
		
		public int getExtend()
			{
			if(this.extend!=null) return this.extend;
			return Integer.parseInt(getAttribute(PROP_CAPTURE_EXTEND));
			}
		
		public Project getProject() {
			return project;
			}
		@Override
		public HasAttributes getParent() {
			return getProject();
			}
		
		
		void prepareCapture(final PrintWriter w)
		{
			w.println(getExtendedFilename()+": "+getNormalizedFilename()+" $(addsuffix .fai,$(REF))");
			w.print("\tmkdir -p $(dir $@) &&");
			w.print(" $(bedtools.exe) slop -b "+getExtend()+" -g $(addsuffix .fai,$(REF)) -i $< |");
			w.print(" LC_ALL=C sort -t '\t' -k1,1 -k2,2n -k3,3n |");
			w.print(" $(bedtools.exe) merge  -d  "+getExtend()+" |");
			w.print(" $(bedtools.exe) sort -faidx $(addsuffix .fai,$(REF)) > $(addsuffix .tmp.bed,$@) && ");
			w.print(" mv --verbose $(addsuffix .tmp.bed,$@) $@");
			w.println();

			w.println(getNormalizedFilename()+": "+getBedFilename()+" $(addsuffix .fai,$(REF))");
			w.print("\tmkdir -p $(dir $@) && ");
			w.print("	tr -d '\\r' < $< | grep -v '^browser'  | grep -v '^track' | cut -f1,2,3 | sed 's/^chr//' | LC_ALL=C sort -t '\t' -k1,1 -k2,2n -k3,3n | ");
			w.print("	$(bedtools.exe) merge |");
			w.print("   $(bedtools.exe) sort -faidx $(addsuffix .fai,$(REF)) > $(addsuffix .tmp.bed,$@) && ");
			w.print("	mv --verbose $(addsuffix .tmp.bed,$@) $@");
			w.println();

			}
		@Override
		public String toString() {
			return getBedFilename();
			}
		}
	
	private class Sample extends HasAttributes
		{
		private final Project project;
		private final String name;
		private final List<PairedFastq> pairs;
		private final String finalBam;
		private final String urer_g_vcf;
		Sample( final Project project,final JsonObject root) throws IOException
			{
			super(root);
			if(!root.isJsonObject()) throw new IOException("sample json is not object");
			final JsonObject json = root.getAsJsonObject();
	
			this.project = project;
			
			if(!json.has("name")) throw new IOException("@name missing in sample");
			this.name= json.get("name").getAsString();
			if(json.has("bam")) {
				this.pairs = Collections.emptyList();
				this.finalBam=json.get("bam").getAsString();
				}
			else if(json.has("fastqs"))
				{
				this.finalBam=null;
				this.pairs = new ArrayList<>();
				final JsonElement fastqs = json.get("fastqs");
				if(fastqs.isJsonPrimitive()) {
					final File dir=new File(fastqs.getAsString());
					if(!(dir.exists() && dir.isDirectory())) throw new IOException("not a directory "+dir );
					final File inside[]=dir.listFiles(F->F.exists() && F.getName().contains("_R1_") && F.getName().endsWith(".fastq.gz"));
					if(inside==null || inside.length==0) throw new IOException("No file under "+dir);
					for(final File insideFileR1:inside)
						{
						final JsonArray twofastqs=new JsonArray();
						twofastqs.add(new JsonPrimitive(insideFileR1.getAbsolutePath()));
						final File insideFileR2=new File(dir,insideFileR1.getName().replaceAll("_R1_", "_R2_"));
						if(!insideFileR2.exists()) throw new IOException("Cannot find "+insideFileR2);
						twofastqs.add(new JsonPrimitive(insideFileR2.getAbsolutePath()));
						final PairedFastq pair=new PairedFastq(this, this.pairs.size(), twofastqs);
						this.pairs.add(pair);
						}
					}
				else if(fastqs.isJsonArray())
					{
					for(final JsonElement pairjson : fastqs.getAsJsonArray())
						{
						final PairedFastq pair =new PairedFastq(this,this.pairs.size(),pairjson);
						this.pairs.add(pair);
						}
					}
				else
					{
					throw new IOException("bad fastq");
					}
				}
			else
				{
				this.finalBam=null;
				this.pairs = Collections.emptyList();
				}
			
			if(json.has("gvcf"))
				{
				this.urer_g_vcf=json.get("gvcf").getAsString();
				}
			else
				{
				this.urer_g_vcf=null;
				}
			}
		
		public String getFamilyId() { return "PEDIGREE";}
		public String getFatherId() { return "0";}
		public String getMotherId() { return "0";}
		public String getSex() { return "0";}
		public String getStatus() { return "9";}
		
		
		public Project getProject() { return this.project;}
		public List<PairedFastq> getPairs() { return this.pairs;}
		public String getName() { return this.name;}
		public boolean isHaloplex() { return isAttributeSet(PROP_PROJECT_IS_HALOPLEX, false);}
		public boolean isBamAlreadyProvided() { return this.finalBam!=null;}
		
		@Override public Project getParent() { return getProject();}
		
		public String getDirectory() {
			return getProject().getSamplesDirectory()+"/"+getName();
			}
		
		public String getMergedBam() {
			return getDirectory()+"/"+getTmpPrefix()+getName()+".merged.bam";
			}
		public String getMergedBamBai() {
			return "$(call  picardbai,"+getMergedBam()+")";
			}
		
		public boolean isMarkupDisabled() {
			return this.finalBam!=null || isHaloplex() || isAttributeSet(PROP_DISABLE_MARKDUP, false);
			}
		
		public String getMarkdupBam() {
			if(isMarkupDisabled()) {
				return getMergedBam();
				}
			else
				{
				return getDirectory()+"/"+getTmpPrefix()+getName()+".markdup.bam";
				}
			}
		public String getMarkdupBamBai() {
			return "$(call picardbai,"+getMarkdupBam()+")";
			}

		public boolean isRealignDisabled() {
			return  isBamAlreadyProvided() || isAttributeSet(PROP_DISABLE_REALIGN, false);
			}

		
		public String getRealignBam() {
			if(isRealignDisabled()) {
				return getMarkdupBam();
				}
			else
				{
				return getDirectory()+"/"+getTmpPrefix()+getName()+".realign.bam";
				}
			}
		public String getRealignBamBai() {
			return "$(call picardbai,"+getRealignBam()+")";
			}

		
		public boolean isRecalibrationDisabled()
			{
			return isBamAlreadyProvided() || isAttributeSet(PROP_DISABLE_RECALIBRATION, false);
			}
		
		
		public String getRecalBam() {
			if(isRecalibrationDisabled()) {
				return getRealignBam();
				}
			else
				{
				return getDirectory()+"/"+getTmpPrefix()+getName()+".recal.bam";
				}
			}
		public String getRecalBamBai() {
			return "$(call picardbai,"+getRecalBam()+")";
			}
		
		public String getFinalBam() {
			if(isBamAlreadyProvided()) {
				return this.finalBam;
				}
			else
				{
				return getDirectory()+"/"+getFilePrefix()+getName()+".final.bam";
				}
			}
		public String getFinalBamBai() {
			return "$(call picardbai,"+getFinalBam()+")";
			}
		
		public String mergeSortedBams()
			{
			if(isBamAlreadyProvided()) return "";
			return new StringBuilder().
				append(getMergedBamBai()+" : "+getMergedBam()).append("\n\ttouch -c $@\n").
				
				append(getMergedBam()+" : "+getPairs().stream().map(P->P.getSortedFilename()).collect(Collectors.joining(" "))+"\n").
				append(rulePrefix()+" && ").
				append("$(java.exe)  -Djava.io.tmpdir=$(dir $@) -jar \"$(picard.jar)\" MergeSamFiles O=$(addsuffix .tmp.bam,$@) ").
				append(" SO=coordinate AS=true CREATE_INDEX=true  COMPRESSION_LEVEL=").append(getAttribute(PROP_DEFAULT_COMPRESSION_LEVEL)).
				append(" VALIDATION_STRINGENCY=SILENT USE_THREADING=true VERBOSITY=INFO TMP_DIR=$(dir $@) COMMENT=\"Merged from $(words $^) files.\" ").
				append("$(foreach B,$(filter %.bam,$^), I=$(B) ) && ").
				append("mv --verbose \"$(addsuffix .tmp.bam,$@)\" \"$@\" ").
				append("\n").
				toString();
			}
		
		public String markDuplicates()
			{
			if(this.isMarkupDisabled()) return "";
			
			return new StringBuilder().
					append(getMarkdupBamBai()+" : "+getMarkdupBam()+"\n").
					append("\ttouch -c $@").
					append("\n").
					append(getMarkdupBam()+" : "+getMergedBam()+"\n").
					append(rulePrefix()+" && ").
					append("$(java.exe) -Xmx3g -Djava.io.tmpdir=$(dir $@) -jar \"$(picard.jar)\" MarkDuplicates I=$< O=$(addsuffix .tmp.bam,$@) M=$(addsuffix .metrics,$@) ").
					append(" REMOVE_DUPLICATES=").
					append(isAttributeSet(PROP_PICARD_MARKDUP_REMOVES_DUPLICATES)?"true":"false").
					append(" AS=true CREATE_INDEX=true  COMPRESSION_LEVEL=").
					append(getAttribute(PROP_DEFAULT_COMPRESSION_LEVEL)).
					append(" VALIDATION_STRINGENCY=SILENT VERBOSITY=INFO TMP_DIR=$(dir $@)  ").
					append(" && mv --verbose \"$(addsuffix .tmp.bam,$@)\" \"$@\" ").
					append(" && mv --verbose \"$(addsuffix .tmp.bai,$@)\" \""+getMarkdupBamBai()+"\"").
					append("\n").
					toString();
			}
		
		
		public String finalBam()
			{
			if(this.isBamAlreadyProvided()) return "";
			StringBuilder sb=new StringBuilder().
				append(getFinalBamBai()+":"+ this.getFinalBam()).
				append("\n").
				append(rulePrefix()).
				append(" && ${samtools.exe} index $< $@").
				append("\n")
				;
			sb.append(getFinalBam()+":"+ this.getRecalBam()).
				append("\n").
				append(rulePrefix()).
				append(" && cp --verbose $< $@").
				append("\n")
				;
			
			return sb.toString();
			}
		
		public String recalibrateBam()
		{
			
			if(this.isRecalibrationDisabled()) return "";

			final StringWriter sw=new StringWriter();
			final PrintWriter w= new PrintWriter(sw);

			w.println( getRecalBamBai() + " : " + this.getRecalBam());
			w.println("\ttouch -c $@");
			w.println();
		
		

			w.print(this.getRecalBam()+"  : "+this.getRealignBam()+" "+this.getRealignBamBai());
			if( this.getProject().hasCapture())
				{
				w.print(" " +getProject().getCapture().getExtendedFilename());
				}
			w.println();
			/* first call BaseRecalibrator */
			w.print("\tmkdir -p $(dir $@) && ");
			w.print(" $(java.exe)  -XX:ParallelGCThreads=5 -Xmx2g  -Djava.io.tmpdir=$(dir $@) -jar $(gatk.jar) -T BaseRecalibrator ");
			w.print("	-R $(REF) ");
			w.print("	--validation_strictness LENIENT ");
			w.print("	-I $< ");
			//w.print("	--num_cpu_threads_per_data_thread "+  this.getIntProperty("base-recalibrator-nct",1));
			w.print("	-l INFO ");
			w.print("	-o  $(addsuffix .grp,$@) ");
			w.print("	-knownSites:dbsnp138,VCF \"$(gatk.bundle.dbsnp.vcf)\" ");
			if( this.getProject().hasCapture())
                {
				w.print(" -L:"+getProject().getCapture().getName()+",BED "+getProject().getCapture().getExtendedFilename());
                }
			w.print("	-cov QualityScoreCovariate ");
			w.print("	-cov CycleCovariate ");
			w.print("	-cov ContextCovariate ");
			
			/* then call PrintReads */
			w.print(" && $(java.exe)  -XX:ParallelGCThreads=5 -Xmx4g  -Djava.io.tmpdir=$(dir $@) -jar $(gatk.jar) -T PrintReads ");
			w.print("	-R $(REF) ");
			w.print(" --bam_compression "+getAttribute(PROP_DEFAULT_COMPRESSION_LEVEL));
			w.print("	-BQSR  $(addsuffix .grp,$@) ");
			
			
			w.print(" --disable_indel_quals ");
				
			w.print("	-I $< ");
			w.print("	-o $(addsuffix .tmp.bam,$@) ");
			w.print("	--validation_strictness LENIENT ");
			w.print("	-l INFO && ");
			w.print("    mv --verbose \"$(addsuffix .tmp.bam,$@)\" \"$@\" && ");
			w.print("    mv --verbose \"$(call picardbai,$(addsuffix .tmp.bam,$@))\" \""+getRecalBamBai()+"\" && ");
			w.print("    sleep 2 && touch -c \""+getRecalBamBai()+"\" && ");
			w.print("  rm --verbose -f  $(addsuffix .grp,$@)");
			w.println();
			w.println();
			
			return sw.toString();
		}
		
		public String realignAroundIndels()
			{
			if(isRealignDisabled()) return "";
			
			StringWriter sw=new StringWriter();
			PrintWriter w= new PrintWriter(sw);
			
			w.append(getRealignBamBai()+" : "+getRealignBam()+"\n").
				append("\ttouch -c $@").
				append("\n")
				;
			
			/* first call RealignerTargetCreator */
			w.print(this.getRealignBam());
			w.print(" : ");
			w.print(this.getMarkdupBam());
			if( this.getProject().hasCapture())
				{
				w.print(" " +getProject().getCapture().getExtendedFilename());
				}
			w.println();
			w.print("\tmkdir -p $(dir $@ ) && ");
			w.print("$(java.exe)  -XX:ParallelGCThreads=2  -Xmx2g  -Djava.io.tmpdir=$(dir $@) -jar $(gatk.jar) -T RealignerTargetCreator ");
			w.print(" -R $(REF)  ");
			w.print(" --num_threads 1");
			if( this.getProject().hasCapture())
				{
				w.print(" -L:"+getProject().getCapture().getName()+",BED "+getProject().getCapture().getExtendedFilename());
				}
			w.print(" -I $(filter %.bam,$^) ");
			w.print(" -o $(addsuffix .intervals,$@) ");
			w.print(" --known:onekindels,VCF \"$(gatk.bundle.onek.indels.vcf)\" ");
			w.print(" --known:millsindels,VCF \"$(gatk.bundle.mills.indels.vcf)\"  ");
			w.print(" -S SILENT ");
			/* then call IndelRealigner */
			w.print("  && $(java.exe)  -XX:ParallelGCThreads=5 -Xmx2g  -Djava.io.tmpdir=$(dir $@) -jar $(gatk.jar) -T IndelRealigner ");
			w.print("  -R $(REF) ");
			w.print(" --bam_compression "+ getAttribute(PROP_DEFAULT_COMPRESSION_LEVEL));
			w.print("  -I $(filter %.bam,$^) "); 
			w.print("  -o $(addsuffix .tmp.bam,$@)  ");
			w.print("  -targetIntervals  \"$(addsuffix .intervals,$@)\" ");
			w.print("  -known:onekindels,VCF \"$(gatk.bundle.onek.indels.vcf)\"   ");
			w.print("  -known:millsindels,VCF \"$(gatk.bundle.mills.indels.vcf)\"  ");
			w.print("  -S SILENT &&  ");
			w.print("mv --verbose \"$(addsuffix .tmp.bam,$@)\" \"$@\" &&  ");
			w.print("mv --verbose  \"$(call picardbai,$(addsuffix .tmp.bam,$@))\" \""+ getRealignBamBai() +"\" &&  ");
			w.print("rm --verbose -f \"$(addsuffix .intervals,$@)\" ");
			w.println();
			w.println();

			return sw.toString();
			}
		
		@Override
		public String toString() {
			return getName();
			}
		}
	
	
	
	private class PairedFastq  extends HasAttributes
		{
		private final List<Fastq> fastqs;
		private final int indexInSample;
		private final Sample _sample;
	
		PairedFastq( final Sample sample,int indexInSample,final JsonElement root) throws IOException
			{
			super(root);
			this.indexInSample=indexInSample;
			this.fastqs = new ArrayList<>();
			this._sample=sample;
			if(root.isJsonObject())
				{
				final JsonObject json=root.getAsJsonObject();
				if(json.has("fastqs"))
					{
					for(final JsonElement fqjson :json.get("fastqs").getAsJsonArray())
						{		
						Fastq fq =new Fastq(this,fqjson);
						this.fastqs.add(fq);
						}
					}
				}
			else if(root.isJsonArray())
				{
				for(final JsonElement fqjson :root.getAsJsonArray())
					{		
					Fastq fq =new Fastq(this,fqjson);
					this.fastqs.add(fq);
					}
				}
			else if(root.isJsonPrimitive())
				{
				Fastq fq =new Fastq(this,root);
				this.fastqs.add(fq);
				}
			else throw new IOException("cannot decore paired for "+root);
			}
		
		public Sample getSample() { return this._sample;}
		public Project getProject() { return getSample().getProject();}
		
		@Override
		public final Sample getParent() { return getSample(); }
	
		public Fastq get(int i) { return this.fastqs.get(i);}
		public int getIndex() { return indexInSample;}
		
		
		public  String getDirectory() {
			return getSample().getDirectory()+"/p"+getIndex();
			}
		
		public String getSortedFilename()
		 	{
			if(getSample().getPairs().size()==1)
				{
				return getSample().getMergedBam();
				}
			else
				{
				return getDirectory()+"/"+getTmpPrefix()+getSample().getName()+".sorted.bam";
				}
			}
		private boolean isUsingCutadapt()
			{
			return isAttributeSet(PROP_DISABLE_CUTADAPT, false)==false;
			}
		
		private Integer getLane() {
			for(int i=0;i< this.fastqs.size();++i)
				{
				String fname= this.fastqs.get(i).getFilename();
				if(!fname.endsWith(".fastq.gz")) continue;
				String tokens[]=fname.split("[_]");
				for(int j=0;j+1< tokens.length;++j)
					{
					if(tokens[j].matches("L[0-9]+") && tokens[j+1].equals("R"+(i+1)))
						{
						return Integer.parseInt(tokens[j].substring(1).trim());
						}
					}
				
				}
			return null;
			}
		
		public  String bwamem()
			{
			if(getSample().isBamAlreadyProvided()) return "";
			final String inputs[]=new String[2];

			final StringBuilder sb= new StringBuilder().
				append(this.getSortedFilename()).
				append(":").
				append(this.get(0).getFilename());
			if(this.fastqs.size()>1)
				{
				sb.append(" ").
				append(this.get(1).getFilename());
				inputs[0]=this.get(0).getFilename();
				inputs[1]=this.get(1).getFilename();
				}
			sb.append("\n").
				append(rulePrefix());
			
			if(this.fastqs.size()==1 && this.fastqs.get(0).isBam())
				{
				inputs[0]="$(addsuffix .bam2fq.R1.fq.gz,$@)";
				inputs[1]="$(addsuffix .bam2fq.R2.fq.gz,$@)";

				sb.append(" && $(java.exe)  -Djava.io.tmpdir=$(dir $@) -jar \"$(picard.jar)\" SamToFastq ").
					append(" I=$<  FASTQ=").append(inputs[0]).
					append(" SECOND_END_FASTQ=").append(inputs[1]).
					append(" UNPAIRED_FASTQ=$(addsuffix .bam2fq.unpaired.fq.gz,$@) ").
					append(" VALIDATION_STRINGENCY=SILENT VERBOSITY=INFO TMP_DIR=$(dir $@) ").
					append(" && rm --verbose $(addsuffix .bam2fq.unpaired.fq.gz,$@) ")
					;
				}
			
			String fq1;
			String fq2;
			if(isUsingCutadapt())
				{
				fq1="$(addsuffix .tmp.R1.fq,$@)";
				fq2="$(addsuffix .tmp.R2.fq,$@)";
				for(int side=0;side<2;++side)
					{
					sb.append(" && ${cutadapt.exe} -a ").
					append(getAttribute(side==0?PROP_CUTADAPT_ADAPTER5:PROP_CUTADAPT_ADAPTER3)).
					append(" ").
					append(inputs[side]).
					append(" -o $(addsuffix .tmp.fq,$@) > /dev/null ").
					append("&& awk '{if(NR%4==2 && length($$0)==0) { printf(\"N\\n\");} else if(NR%4==0 && length($$0)==0) { printf(\"#\\n\");} else {print;}}' ").
					append("$(addsuffix .tmp.fq,$@) > ").
					append(side==0?fq1:fq2).
					append(" && rm   --verbose $(addsuffix .tmp.fq,$@) ");
					}
				}
			else
				{
				fq1=inputs[0];
				fq2=inputs[1];
				}
			final Integer laneIndex= getLane();
			
			
			final String bwaRef="$(dir $(REF))index-bwa-0.7.12/$(notdir $(REF))";

			
			sb.append(" && $(bwa.exe) mem -t ").
				append(getAttribute(PROP_BWA_MEM_NTHREADS)).
				append(" -M -H '@CO\\tDate:"
					+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
				if(getProject().hasCapture())
					{	
					sb.append(" Capture was ").
						append(getProject().getCapture().getName()).
						append(" : ").
						append(getProject().getCapture().getBedFilename()).
						append(" ")
						;
					}
				sb.append(" Alignment of $(notdir $^) for project ").
					append(getProject().getName()+":"+getProject().getDescription()+"'").
				append(" -R '@RG\\tID:"+ getSample().getName()).
				append(laneIndex==null?"":"_L"+laneIndex).
				append("\\tLB:"+getSample()+"\\tSM:"+getSample() +"\\tPL:illumina\\tCN:Nantes' \""+bwaRef+"\" "+fq1+" "+fq2+"  |").
				append("$(samtools.exe) sort --reference \"$(REF)\" -l ").
				append(getAttribute(PROP_DEFAULT_COMPRESSION_LEVEL)).
				append(" -@ ").
				append(getAttribute(PROP_SAMTOOLS_SORT_NTHREADS)).
				append(" -O bam -o $(addsuffix .tmp.bam,$@) -T  $(dir $@)"+getTmpPrefixToken()+getSample().getName()+".tmp_sort_"+getIndex()+" - && ").
				append("mv --verbose \"$(addsuffix .tmp.bam,$@)\" \"$@\"")
				;
			if(isUsingCutadapt())
				{
				sb.append(" && rm --verbose -f "+fq1+" "+fq2);
				}
			if(this.fastqs.size()==1 && this.fastqs.get(0).isBam())
				{
				sb.append(" && rm --verbose -f  "+inputs[0]+" "+inputs[1]);
				}
			
			return sb.append("\n").
				toString();
			}
		
		@Override
		public String toString() {
			return getSample().getName()+".fastqs["+this.getIndex()+"]";
			}
		}
	
	private class Fastq extends HasAttributes
		{
		private final PairedFastq pair;
		private final String filename;
		Fastq( final PairedFastq pair,final JsonElement e ) throws IOException {
			super(e);
			this.pair=pair;
			filename= e.getAsString();
			}
		public String getFilename() {
			return filename;
			}
		public boolean isBam() {
			return getFilename().endsWith(".bam");
			}
		public boolean isFastq() {
			final String s=getFilename().toLowerCase();
			return s.endsWith(".fq") || s.endsWith(".fq.gz") || s.endsWith(".fastq") || s.endsWith(".fastq.gz");
			}
		@Override
		public String toString() {
			return getFilename();
			}
	
		public PairedFastq getPair() {
			return pair;
			}
		
		@Override
		public PairedFastq getParent() {
			return getPair();
			}
		}
	
	
	private PrintWriter out = new PrintWriter(System.out);
	
	public void execute(final Project project)
		{
		out.println("include ../../config/config.mk");
		out.println("OUT="+project.getOutputDirectory());
		
		
		out.println("# 1 : bam name");
		out.println("define bam_and_picardbai");
		out.println("$(1) $(call picardbai,$(1))");
		out.println("endef");
		
		out.println("#1 : bam filename");
		out.println("define picardbai");
		out.println("$(patsubst %.bam,%.bai,$(1))");
		out.println("endef");

		
		out.println("gatk.bundle.dir=$(dir $(REF))");
		out.println("gatk.bundle.dbsnp.vcf=$(gatk.bundle.dir)dbsnp_138.b37.vcf");
		out.println("gatk.bundle.onek.indels.vcf=$(gatk.bundle.dir)1000G_phase1.indels.b37.vcf");
		out.println("gatk.bundle.onek.snp.vcf=$(gatk.bundle.dir)1000G_phase1.snps.high_confidence.b37.vcf");
		out.println("gatk.bundle.mills.indels.vcf=$(gatk.bundle.dir)Mills_and_1000G_gold_standard.indels.b37.vcf");
		out.println("gatk.bundle.hapmap.vcf=$(gatk.bundle.dir)hapmap_3.3.b37.vcf");
		out.println("exac.vcf?=/commun/data/pubdb/broadinstitute.org/exac/0.3/ExAC.r0.3.sites.vep.vcf.gz");
		
		out.println("all:"+project.getHapCallerGenotypedVcf());
		
		out.println("all_final_bam:"+project.getSamples().stream().
				map(S->S.getFinalBamBai()).
				collect(Collectors.joining(" \\\n\t"))
				);
		
		out.println("all_recal_bam:"+project.getSamples().stream().
				filter(S->!S.isBamAlreadyProvided()).
				map(S->S.getRecalBamBai()).
				collect(Collectors.joining(" \\\n\t"))
				);
		
		out.println("all_realigned_bam:"+project.getSamples().stream().
				filter(S->!S.isBamAlreadyProvided()).
				map(S->S.getRealignBamBai()).
				collect(Collectors.joining(" \\\n\t"))
				);
		
		out.println("all_markdup_bam:"+project.getSamples().stream().
				filter(S->!S.isBamAlreadyProvided()).
				map(S->S.getMarkdupBamBai()).
				collect(Collectors.joining(" \\\n\t"))
				);
		
		out.println("all_merged_bam:"+project.getSamples().stream().
				filter(S->!S.isBamAlreadyProvided()).
				map(S->S.getMergedBamBai()).
				collect(Collectors.joining(" \\\n\t"))
				);
		
		for(final Sample sample: project.getSamples())
			{
			if(sample.getPairs().size()==1)
				{	
				out.println(sample.getPairs().get(0).bwamem());
				
				}
			else if(sample.getPairs().size()>1)
				{
				sample.getPairs().forEach(P->{out.println(P.bwamem());});
				out.println(sample.mergeSortedBams());
				}
			
			out.println(sample.markDuplicates());
			out.println(sample.realignAroundIndels());
			out.println(sample.recalibrateBam());
			out.println(sample.finalBam());
			}
		if(project.hasCapture())
			{
			project.getCapture().prepareCapture(out);
			}
		project.getPedigree().build(out);
		project.bamList(out);
		out.println(project.haplotypeCaller());
		
		out.println();
		out.flush();
		}
	
	private static JsonElement readJsonFile(final File jsonFile) throws IOException {
		IOUtil.assertFileIsReadable(jsonFile);
		FileReader r=new FileReader(jsonFile);
		JsonParser jsparser=new JsonParser();
		JsonElement root=jsparser.parse(r);
		r.close();
		return root;
		}
	
	@Override
	public Collection<Throwable> call() throws Exception {
		if(super.dumpAttributes)
			{
			for(final PropertyKey prop:NgsWorkflow.name2propertyKey.values())
				{
				System.out.print("\""+prop.getKey()+"\"\t"+prop.getDescription());
				if(prop.getDefaultValue()!=null && !prop.getDefaultValue().isEmpty())
					{
					System.out.print("\t\""+prop.getDefaultValue()+"\"");
					}
				System.out.println();
				}
			return RETURN_OK;
			}
		try
			{
			final List<String> args=this.getInputFiles();
			if(args.size()!=1) return wrapException("exepcted one and only one json file as input");
			final File jsonFile=new File(args.get(0));
			JsonElement root=readJsonFile(jsonFile);
			Project proj=new Project(root);
			execute(proj);
			out.flush();
			return RETURN_OK;
			}
		catch(final Exception err)
			{
			return wrapException(err);
			}
		finally
			{
			}
		}
	
	public static void main(String[] args) {
		new NgsWorkflow().instanceMainWithExit(args);
	}
	
}
