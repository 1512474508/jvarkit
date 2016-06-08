/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


History:
* 2014 creation
* 2015 moving to knime

*/
package com.github.lindenb.jvarkit.tools.misc;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


public class SplitVcf
	extends AbstractSplitVcf
	{
	private static final org.slf4j.Logger LOG = com.github.lindenb.jvarkit.util.log.Logging.getLog(SplitVcf.class);

	private final static String REPLACE_GROUPID="__GROUPID__";
	private final java.util.Map<String,SplitGroup> name2group=new java.util.HashMap<>();
	private final IntervalTreeMap<Set<SplitGroup>> interval2group = new IntervalTreeMap<>();
	private SplitGroup underminedGroup=null;

	
	/** spit group */
	private class SplitGroup implements Closeable
		{
		final String groupName;
		VCFHeader header=null;
		VariantContextWriter _writer;
		
		SplitGroup(final String groupName)
			{
			this.groupName=groupName;
			}
		

		@Override
		public void close() {
			if(_writer!=null) CloserUtil.close(_writer);
			_writer=null;
			
		}
		@Override
		public int hashCode() {
			return this.groupName.hashCode();
			}
		
		@Override
		public boolean equals(Object obj) {
			if(obj==null) return false;
			if(obj == this) return true;
			return  this.groupName.equals(((SplitGroup)obj).groupName);
			}

		
		public File getFile()
			{
			return new File(
					SplitVcf.this.getOutputFile().getParentFile(),
					SplitVcf.this.getOutputFile().getName().replaceAll(
							SplitVcf.REPLACE_GROUPID,
							this.groupName
							));
			}
		
		public void open(final VCFHeader src)
			{	
	
			final File fileout=getFile();
			LOG.info("opening VCF file \""+fileout+"\" for writing");
			final File parent=fileout.getParentFile();
			if(parent!=null) {
				parent.mkdirs();
			}
	
			this.header= new VCFHeader(src);
			this.header.addMetaDataLine(new VCFHeaderLine("SplitVcf.GroupName", this.groupName));
			try {
				this._writer = VCFUtils.createVariantContextWriter(fileout);
			} catch (IOException e) {
				throw new RuntimeIOException(e);
			}
			this._writer.writeHeader(this.header);
			}
		
		@Override
		public String toString() {
			return groupName;
			}
		}
	
	
		
	public SplitVcf()
		{
		}
	
	
	
	private Set<SplitGroup> getGroupsFromInterval(final Interval interval) {
		final Set<SplitGroup> groups= new HashSet<>();
		for(final Set<SplitGroup> G:this.interval2group.getOverlapping(interval))
			{
			groups.addAll(G);
			}
		if(groups.isEmpty()) return Collections.emptySet();
		if(!super.multiIntervalEnabled && groups.size()!=1) {
			throw new IllegalStateException("got two group for uniq interval "+interval+" ? "+groups);
		}
		return groups;
		}
	
	
	private void putInterval(final String groupName,final Interval interval)
		{
		Collection<SplitGroup> splitgroups = this.getGroupsFromInterval(interval);
		if(!super.multiIntervalEnabled)
			{
			for(final SplitGroup splitgroup:splitgroups)
				{
				if(!splitgroup.groupName.equals(groupName))
					{
					throw new IllegalArgumentException(
							"chrom "+interval+" already used in "+splitgroup.groupName+
							". use option  -"+ OPTION_MULTIINTERVALENABLED+" to enable multiple groups/interval");
					}
				}
			}
		
		SplitGroup splitgroup = this.name2group.get(groupName);
			
		if(splitgroup==null)
			{
			splitgroup = new SplitGroup(groupName);
			this.name2group.put(groupName,splitgroup);
			}
		Set<SplitGroup> L = this.interval2group.get(interval);
		if(L==null) {
			L=new HashSet<>();
			L.add(splitgroup);
		}
		this.interval2group.put(interval, L);
		}
				
	@Override
	protected Collection<Throwable> call(final String inputName) throws Exception {
		if (getOutputFile()==null || !getOutputFile().getName().contains(REPLACE_GROUPID)) {
			return wrapException("Output file pattern undefined or doesn't contain " + REPLACE_GROUPID + " : "
					+ this.getOutputFile());
		}
		if (!(getOutputFile().getName().endsWith(".vcf") || getOutputFile().getName().endsWith(".vcf.gz"))) {
			return wrapException("output file must end with '.vcf' or '.vcf.gz'");
		}
		BufferedReader r=null;
		VcfIterator in =null;
		try 
			{
			in = openVcfIterator(inputName);
			final SAMSequenceDictionary samSequenceDictionary = in.getHeader().getSequenceDictionary();
			if(samSequenceDictionary==null) {
				return wrapException("samSequenceDictionary missing in input VCF.");
			}
			
			
			this.underminedGroup = new SplitGroup(UNDERTERMINED_NAME);
			this.name2group.put(UNDERTERMINED_NAME, this.underminedGroup);
	
			if (super.chromGroupFile != null)
				{
				r = IOUtils.openFileForBufferedReading(super.chromGroupFile);
				String line;
				while ((line = r.readLine()) != null) {
					if (line.isEmpty() || line.startsWith("#"))
						continue;
					final String tokens[] = line.split("[ \t,]+");
					final String groupName = tokens[0].trim();
					if (groupName.isEmpty())
						return wrapException("Empty group name in " + line);
					if (this.UNDERTERMINED_NAME.equals(groupName))
						return wrapException("Group cannot be named " + UNDERTERMINED_NAME);
					if (this.name2group.containsKey(groupName))
						return wrapException("Group defined twice " + groupName);
					for (int i = 1; i < tokens.length; i++) {
						String sequence;
						int start;
						int end;
						final String segment = tokens[i].trim();
	
						if (segment.isEmpty())
							continue;
	
						int colon = segment.indexOf(':');
						if (colon == -1) {
							final SAMSequenceRecord ssr = samSequenceDictionary.getSequence(segment);
							if (ssr == null) {
								return wrapException("Unknown chromosome , not in dict \"" + segment + "\"");
							}
							sequence = segment;
							start = 1;
							end = ssr.getSequenceLength();
						} else {
							int hyphen = segment.indexOf('-', colon);
							if (hyphen == -1)
								return wrapException("Bad segment:" + segment);
							sequence = segment.substring(0, colon);
							if (samSequenceDictionary.getSequence(sequence) == null)
								return wrapException("Unknown chromosome , not in dict " + segment);
	
							start = Integer.parseInt(segment.substring(colon + 1, hyphen));
							end = Integer.parseInt(segment.substring(hyphen + 1));
						}
	
						final Interval interval = new Interval(sequence, start, end);
						this.putInterval(groupName, interval);
					}
				}
				r.close();r=null;
			} else {
				LOG.info("creating default split interval");
				for (final SAMSequenceRecord seq : samSequenceDictionary.getSequences()) {
					final String groupName = seq.getSequenceName();
					final Interval interval = new Interval(groupName, 1, seq.getSequenceLength());
					this.putInterval(groupName, interval);
				}
			}
			
			/* open all output vcf */
			for (final SplitGroup g : this.name2group.values()) {
				g.open(in.getHeader());
			}

			/* loop over vcf variations */
			final SAMSequenceDictionaryProgress progress = new SAMSequenceDictionaryProgress(samSequenceDictionary);
			while (in.hasNext()) {
				final VariantContext record = progress.watch(in.next());
				final Interval interval = new Interval(record.getContig(), record.getStart(), record.getEnd());
				Set<SplitGroup> splitGroups = this.getGroupsFromInterval(interval);
				
				
				if (splitGroups.isEmpty()){
					splitGroups = Collections.singleton(this.underminedGroup);
				}
			
				
				for(final SplitGroup splitGroup:splitGroups)
					{
					splitGroup._writer.add(record);
					}
			}
	
			progress.finish();
			in.close();
	
			for (final SplitGroup g : this.name2group.values()) {
				g.close();
			}
			return RETURN_OK;
			}
		catch(Exception err) {
			for (final SplitGroup g : this.name2group.values()) {
				CloserUtil.close(g);
				if(in!=null) g.getFile().delete();
			}
			return wrapException(err);
		} finally {
			for (final SplitGroup g : this.name2group.values()) {
				CloserUtil.close(g);
			}
			CloserUtil.close(r);
			CloserUtil.close(in);
			this.name2group.clear();
			this.interval2group.clear();
		}
	}
	 	
	
	public static void main(String[] args)
		{
		new SplitVcf().instanceMainWithExit(args);
		}

	}
