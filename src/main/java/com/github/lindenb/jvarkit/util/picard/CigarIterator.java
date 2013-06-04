package com.github.lindenb.jvarkit.util.picard;

import net.sf.picard.reference.ReferenceSequenceFile;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

public class CigarIterator
	{
	private SAMRecord record;
	private int refPos=0;
	private int readPos=0;
	private int displayRefPos=0;
	private int displayReadPos=0;
	private int cigardElementIndex=0;
	private int indexInCigarElement=-1;
	private ReferenceSequenceFile referenceSequenceFile;
	
	
	public boolean next()
		{
		if(getSAMRecord().getReadUnmappedFlag()) return false;
		if(getSAMRecord().getCigar()==null) return false;
		for(;;)
			{
			if(this.cigardElementIndex >= getSAMRecord().getCigar().getCigarElements().size())
				{
				return false;
				}
			
			this.indexInCigarElement++;
			final CigarElement ce=  getSAMRecord().getCigar().getCigarElement(this.cigardElementIndex);
			if(this.indexInCigarElement>= ce.getLength() )
				{
				this.cigardElementIndex++;
				this.indexInCigarElement=-1;
				continue;
				}
			displayRefPos=-1;
			displayReadPos=-1;
			switch(ce.getOperator())
				{
				case I:
				case S:
					{
					this.displayReadPos= this.readPos;
					this.readPos++;
					break;
					}
				case D:
				case N:
				case P:
					{
					this.displayRefPos= this.refPos;
					this.refPos++;
					break;
					}
				case M:
				case EQ:
				case X:
					{
					this.displayReadPos= this.readPos;
					this.displayRefPos= this.refPos;
					this.readPos++;
					this.refPos++;
					break;
					}
				default: throw new IllegalStateException("Doesn't know how to handle cigar operator:"+ce.getOperator());
				}
			return true;
			}
		}
	
	public void reset()
		{
		this.readPos=0;
		this.refPos=record.getUnclippedStart();
		this.displayRefPos=-1;
		this.displayReadPos=-1;
		this.cigardElementIndex=0;
		this.indexInCigarElement=-1;
		}
	
	public SAMRecord getSAMRecord()
		{
		return record;
		}
	
	public int getReferencePosition()
		{
		return displayRefPos;
		}
	
	public int getReadPosition()
		{
		return displayReadPos;
		}
	
	
	
	public Character getReadBase()
		{
		return this.displayReadPos==-1?null:(char)getSAMRecord().getReadBases()[this.displayReadPos];
		}
	
	public Integer getReadQual()
		{
		return this.displayReadPos==-1?null:(int)getSAMRecord().getBaseQualities()[this.displayReadPos];
		}
	
	public Character getReferenceBase()
		{
		if(this.referenceSequenceFile==null || this.displayRefPos==-1)
			{
			return null;
			}
		if(getSAMRecord().getReadUnmappedFlag()) return null;
		String tid=getSAMRecord().getReferenceName();
		if(tid==null) return null;
		return (char)this.referenceSequenceFile.getSubsequenceAt(
				tid, this.displayRefPos,this.displayRefPos
				).getBases()[0];
		}
	
	public CigarElement getCigarElement()
		{
		return getSAMRecord().getCigar().getCigarElement(cigardElementIndex);
		}
	
	public CigarOperator getCigarOperator()
		{	
		return getCigarElement().getOperator();
		}
	
	
	
	static public CigarIterator create(
			SAMRecord record,
			ReferenceSequenceFile ref
			)
		{
		CigarIterator iter=new CigarIterator();
		iter.record=record;
		iter.refPos=record.getUnclippedStart();
		iter.referenceSequenceFile=ref;
		return iter;
		}
	
	}
