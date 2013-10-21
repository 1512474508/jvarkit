package com.github.lindenb.jvarkit.util.picard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.picard.util.IntervalTree;
import net.sf.samtools.SAMSequenceDictionary;

public class SamSequenceRecordTreeMap<T>
	{
	private SAMSequenceDictionary dict=null;
	private List<IntervalTree<T>> chroms;
	public SamSequenceRecordTreeMap(SAMSequenceDictionary dict)
		{
		this.dict=dict;
		this.chroms=new ArrayList<IntervalTree<T>>(this.dict.size());
		}

	
	public SAMSequenceDictionary getSAMSequenceDictionary()
		{
		return dict;
		}
	
	protected IntervalTree<T> tree(int tid)
		{
		if(tid<0 || tid>=this.chroms.size()) return null;
		return this.chroms.get(tid);
		}
	
	public List<T> getOverlapping(String chrom,int start1,int end1)
		{
		return getOverlapping(getSAMSequenceDictionary().getSequenceIndex(chrom),start1,end1);
		}
	
    public List<T> getOverlapping(int tid,int start1,int end1)
    	{
        IntervalTree<T> tree=tree(tid);
        if(tree==null) return Collections.emptyList();
        List<T> L=new ArrayList<T>();
        for(Iterator<IntervalTree.Node<T>> iter = tree.overlappers(start1,end1);
        		iter.hasNext();
        		)
        		{
                L.add(iter.next().getValue());
        		}
       
        return L;
    	}
    
    public boolean containsOverlapping(String chrom,int start1,int end1)
		{
		return containsOverlapping(getSAMSequenceDictionary().getSequenceIndex(chrom),start1,end1);
		}

	public boolean  containsOverlapping(int tid,int start1,int end1)
		{
	    IntervalTree<T> tree=tree(tid);
	    return tree!=null && tree.overlappers(start1,end1).hasNext();
	 	}

	public void put(String chrom,int start1,int end1,T o)
		{
		put(getSAMSequenceDictionary().getSequenceIndex(chrom),start1,end1,o);
		}
	
	
	public void put(int tid,int start1,int end1,T o)
		{
		if(tid<0) return;
		IntervalTree<T> m;
		if(this.chroms.size()<=tid)
			{
			while(this.chroms.size()<tid) this.chroms.add(null);
			m=new IntervalTree<T>();
			this.chroms.add(m);
			}
		else 
			{
			m= this.chroms.get(tid);
			if(m==null)
				{
				m=new IntervalTree<T>();
				this.chroms.set(tid, m);
				}
			}
		m.put(start1, end1, o);
		}
	
	public boolean isEmpty()
		{
		return this.chroms.isEmpty();
		}
	}
