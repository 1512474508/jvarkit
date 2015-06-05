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

*/
package com.github.lindenb.jvarkit.tools.misc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;

import com.github.lindenb.jvarkit.util.AbstractCommandLineProgram;
import com.github.lindenb.jvarkit.util.Hershey;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

public class VcfToHilbert extends AbstractCommandLineProgram
	{
	/** graphics context */ 
	private Graphics2D g;
	/** dictionary */
	private SAMSequenceDictionary dict;
    /** level of recursion */
    private int recursionLevel=6;
    /** with/height of the final picture */
    private int imageWidth=1000;
    private double sampleWidth=0;
    private double genomicSizePerCurveUnit=0L;

    
    private abstract class HilbertSegmentHandler
    	{
        /** last index of the position in the genome */
    	double prev_base=0;
        /** size (in pb) of an edge */
        private final double d_base=VcfToHilbert.this.genomicSizePerCurveUnit;
    	/** size of an edge */
        private double dist=-1;
        /** last time we plot a segment, point at end */
        private Point2D.Double prevPoint;
        
        
        protected HilbertSegmentHandler()
        	{
        	this.dist = VcfToHilbert.this.sampleWidth;
        	for (int i= VcfToHilbert.this.recursionLevel; i>0; i--)
	            {
	            this.dist /= 2.0;
	            }
        	this.prevPoint=new Point2D.Double(
        			this.dist/2.0,
        			this.dist/2.0
        			);
            this.prev_base=0L;
        	}
        
        abstract void segment(
        		Point2D.Double beg,
        		Point2D.Double end,
        		long chromStart,
        		long chromEnd
        		);
        
        void run()
        	{
        	this.HilbertU(recursionLevel);
        	}	
        
        private void lineRel(double deltaX, double deltaY)
        	{
        	Point2D.Double point=new Point2D.Double(
        		this.prevPoint.x + deltaX,
        		this.prevPoint.y + deltaY
        		);
            long chromStart=(long)prev_base;
            long chromEnd=(long)(chromStart+ d_base);
            segment(prevPoint, point, chromStart, chromEnd);
            this.prevPoint= point;
            this.prev_base= chromEnd;;
        	}
        
     // Make U-shaped curve at this scale:
        private void HilbertU(int level)
            {
            if (level <= 0) return;
            HilbertD(level-1);    this.lineRel(0, dist);
            HilbertU(level-1);    this.lineRel(dist, 0);
            HilbertU(level-1);    this.lineRel(0, -dist);
            HilbertC(level-1);
           
            }
     
        // Make D-shaped (really "]" shaped) curve at this scale:
        private void HilbertD(int level)
        	{
        	if (level <= 0) return;
            HilbertU(level-1);    this.lineRel(dist, 0);
            HilbertD(level-1);    this.lineRel(0, dist);
            HilbertD(level-1);    this.lineRel(-dist, 0);
            HilbertA(level-1);
    		}
     
        // Make C-shaped (really "[" shaped) curve at this scale:
        private void HilbertC(int level)
        	{
        	if (level <= 0) return;
            HilbertA(level-1);    this.lineRel(-dist, 0);
            HilbertC(level-1);    this.lineRel(0, -dist);
            HilbertC(level-1);    this.lineRel(dist, 0);
            HilbertU(level-1);
        	}
     
        // Make A-shaped (really "⊓" shaped) curve at this scale:
        private void HilbertA(int level) {
        	if (level <= 0) return;
            HilbertC(level-1);    this.lineRel(0, -dist);
            HilbertA(level-1);    this.lineRel(-dist, 0);
            HilbertA(level-1);    this.lineRel(0, dist);
            HilbertD(level-1);
        	}
    	}	
    private class EvalCurve extends HilbertSegmentHandler
    	{
    
    	long count=0;
    	@Override
    	void segment(Double beg, Double end, long chromStart, long chromEnd) {
    		count++;
    		}
    	}
    
    private class PaintSegment extends HilbertSegmentHandler
    	{
    	long genomicStart;
    	long genomicEnd;
    	@Override
    	void segment(
         		final Point2D.Double beg,
         		final Point2D.Double end,
         		final long chromStart,
         		final long chromEnd
         		)
    		{
    		if(this.genomicStart > chromEnd) return;
    		if(this.genomicEnd < chromStart) return;
    		
    		long p0= Math.max(this.genomicStart,chromStart);
    		long p1= Math.min(this.genomicEnd,chromEnd);
    		double segsize=chromEnd-chromStart;
    		if(segsize==0) segsize=1.0;
    		
    		double r0= (p0-(double)chromStart)/segsize;
    		double r1= (p1-(double)chromStart)/segsize;
    		
    		double x0= beg.x + (end.x - beg.x)*r0;
    		double y0= beg.y + (end.y - beg.y)*r0;
    		double x1= beg.x + (end.x - beg.x)*r1;
    		double y1= beg.y + (end.y - beg.y)*r1;
    		    		
    		g.draw(new Line2D.Double(x0,y0,x1,y1));
    		}
    	}
    
    private class PaintVariant extends HilbertSegmentHandler
		{
		VariantContext ctx;
		private long genomic_index;
		private int sample_x;
		private int sample_y;
		PaintVariant(VariantContext ctx,int sample_x,int sample_y)
			{
			this.ctx=ctx;
			this.sample_x=sample_x;
			this.sample_y=sample_y;
			for(SAMSequenceRecord ssr:VcfToHilbert.this.dict.getSequences())
				{
				if(ssr.getSequenceName().equals(ctx.getContig()))
					{
					genomic_index+=ctx.getStart();
					break;
					}
				else
					{
					genomic_index+=ssr.getSequenceLength();
					}
				}
			}
		private void mul(double x,double y)
			{
			g.draw(new Line2D.Double(x-5, y-5, x+5, y+5));
			g.draw(new Line2D.Double(x+5, y-5, x-5, y+5));
			}
		private void cross(double x,double y)
			{
			g.draw(new Line2D.Double(x-5, y, x+5, y));
			g.draw(new Line2D.Double(x, y-5, x, y+5));
			}
		private void circle(double x,double y)
			{
			g.fill(new Ellipse2D.Double(x-3, y-3, 6, 6));
			}
		
		@Override
		void segment(
	     		final Point2D.Double beg,
	     		final Point2D.Double end,
	     		final long chromStart,
	     		final long chromEnd
	     		)
			{
			if(genomic_index > chromEnd) return;
			if(genomic_index < chromStart) return;
			
			double segsize=chromEnd-chromStart;
			if(segsize==0) segsize=1.0;
			double r0= (genomic_index-(double)chromStart)/segsize;
			double x = beg.x + (end.x - beg.x)*r0;
			double y = beg.y + (end.y - beg.y)*r0;
			
			Genotype gx=this.ctx.getGenotype(sample_x);
			
	    	Composite composite=g.getComposite();
	    	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

			if(this.sample_x == this.sample_y 
				)
				{
				if( gx.isCalled() &&
					!gx.isHomRef())
					{
					boolean uniq_in_context=true;
					for(int i=0;i< ctx.getNSamples();++i)
						{
						if(i==this.sample_x) continue;
						Genotype other= ctx.getGenotype(i);
						if(other.sameGenotype(gx))
							{
							uniq_in_context=false;
							break;
							}
						}
					g.setColor(uniq_in_context?Color.RED:Color.BLACK);
					if(gx.isHet())
						{
						cross(x,y);
						}
					else
						{
						circle(x,y);
						}	
					}
				}
			else
				{
				Genotype gy = this.ctx.getGenotype(sample_y);
				if( gx.isCalled() &&
					gy.isCalled())
					{
					if(!(gx.sameGenotype(gy) && gx.isHomRef())) 
						{
						boolean x_uniq_in_context=true;
						boolean y_uniq_in_context=true;
						for(int i=0; i< ctx.getNSamples();++i)
							{
							if(i==this.sample_x || i==this.sample_y) continue;
							Genotype other= ctx.getGenotype(i);
							if(other.sameGenotype(gx))
								{
								x_uniq_in_context=false;
								}
							if(other.sameGenotype(gy))
								{
								y_uniq_in_context=false;
								}
							}
						g.setColor(Color.GRAY);
						if(y_uniq_in_context && x_uniq_in_context && gx.sameGenotype(gy))
							{
							g.setColor(Color.RED);
							circle(x,y);
							}
						else if(y_uniq_in_context && x_uniq_in_context)
							{
							g.setColor(Color.ORANGE);
							mul(x,y);
							}
						else if(gx.sameGenotype(gy))
							{ 
							g.setColor(Color.GRAY);
							cross(x,y);
							}
						else
							{
							g.setColor(Color.LIGHT_GRAY);
							cross(x,y);
							}
						}
					else
						{
						// both HOM-REF, ignore
						}
					
					}
				else if(gx.isCalled())
					{
					g.setColor(Color.LIGHT_GRAY);
					cross(x,y);
					}
				}
			//g.draw(new Line2D.Double(x0,y0,x1,y1));
	
			g.setComposite(composite);
			}
		}
    
	@Override
	public String getProgramDescription() {
		return "Plot a Hilbert Curve from a VCF file.";
		}
	
	@Override
	public void printOptions(java.io.PrintStream out)
		{
		out.println(" -o (file.png) output image name . Required");
		out.println(" -w (int) image width . Default: "+this.imageWidth);
		super.printOptions(out);
		}
	
	

    
    private void paintReference()
    	{
    	int n_colors=this.dict.getSequences().size();
    	ArrayList<Color> colors=new ArrayList<Color>(n_colors);
    	for(int i=0;i<n_colors;++i)
    		{
    		float gray=(i/(float)n_colors)*0.6f;
    		Color c=new Color( gray, gray, i%2==0?gray:1f-gray);
    		if(i%2==0)
    			{
    			colors.add(c);
    			}
    		else
    			{
    			colors.add(0,c);
    			}
    		}
    	Stroke oldStroke= g.getStroke();
    	Composite composite=g.getComposite();
    	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
    	long pos=0L;
    	for(int tid=0;tid<this.dict.getSequences().size();++tid)
    		{
    		g.setStroke(new BasicStroke(0.5f));
    		SAMSequenceRecord ssr= this.dict.getSequence(tid);
    		g.setColor(colors.get(tid%colors.size()));
    		
    		PaintSegment paintSeg=new PaintSegment();
        	paintSeg.genomicStart = pos;
        	paintSeg.genomicEnd   = pos+ssr.getSequenceLength();
        	paintSeg.run();
    		
    		pos+=ssr.getSequenceLength();
    		}
    	g.setComposite(composite);
    	 g.setStroke(oldStroke);
    	}
    
	@Override
	public int doWork(String[] args)
		{
		File imgOut=null;
		com.github.lindenb.jvarkit.util.cli.GetOpt opt=new com.github.lindenb.jvarkit.util.cli.GetOpt();
		int c;
		while((c=opt.getopt(args,getGetOptDefault()+"o:w:"))!=-1)
			{
			switch(c)
				{
				case 'o': imgOut=new File(opt.getOptArg());break;
				case 'w': this.imageWidth = Integer.parseInt(opt.getOptArg());break;

				default:
					{
					switch(handleOtherOptions(c, opt,args))
						{
						case EXIT_FAILURE: return -1;
						case EXIT_SUCCESS: return 0;
						default:break;
						}
					}
				}
			}
		if(imgOut==null)
			{
			error("output image file not defined");
			return -1;
			}
		
		if(this.imageWidth<1)
			{
			error("Bad image size:" +this.imageWidth);
			return -1;
			}
		VcfIterator iter=null;
		try
			{
			
			if(opt.getOptInd()==args.length)
				{
				info("Reading from stdin");
				iter=VCFUtils.createVcfIteratorStdin();
				}
			else if(opt.getOptInd()+1==args.length)
				{
				String filename=args[opt.getOptInd()];
				info("Reading from "+filename);
				iter=VCFUtils.createVcfIterator(filename);
				}
			else
				{
				error(getMessageBundle("illegal.number.of.arguments"));
				return -1;
				}
			VCFHeader header=iter.getHeader();
			this.dict = header.getSequenceDictionary();
			if(this.dict == null)
				{
				error(getMessageBundle("file.is.missing.dict"));
				return -1;
				}
			List<String> samples=header.getSampleNamesInOrder();
			if(samples.isEmpty())
				{
				error(getMessageBundle("no.sample.in.vcf"));
				return -1;
				}
			info("N-Samples:"+samples.size());
			double marginWidth = (this.imageWidth-2)*0.05;
			this.sampleWidth= ((this.imageWidth-2)-marginWidth)/samples.size();
			info("sample Width:"+sampleWidth);
			BufferedImage img = new BufferedImage(this.imageWidth, this.imageWidth, BufferedImage.TYPE_INT_RGB);
			this.g = (Graphics2D)img.getGraphics();
			this.g.setColor(Color.WHITE);
			this.g.fillRect(0, 0, imageWidth, imageWidth);
			
			g.setColor(Color.BLACK);
			Hershey hershey =new Hershey();

			EvalCurve evalCurve=new EvalCurve();
			evalCurve.run();
			this.genomicSizePerCurveUnit = ((double)dict.getReferenceLength()/(double)(evalCurve.count));
			if(this.genomicSizePerCurveUnit<1) this.genomicSizePerCurveUnit=1;
			info("genomicSizePerCurveUnit:"+genomicSizePerCurveUnit);
			
			for(int x=0;x< samples.size();++x)
				{
				String samplex =  samples.get(x);
				
				double labelHeight=marginWidth;
				if(labelHeight>50) labelHeight=50;
				
				g.setColor(Color.BLACK);
				hershey.paint(g,
						samplex,
						marginWidth + x*sampleWidth,
						marginWidth - labelHeight,
						sampleWidth*0.9,
						labelHeight*0.9
						);
				
        		AffineTransform old=g.getTransform();
        		AffineTransform tr= AffineTransform.getTranslateInstance(
        				marginWidth ,
        				marginWidth + x*sampleWidth
        				);
        		tr.rotate(Math.PI/2);
        		g.setTransform(tr);
        		hershey.paint(g,
        				samplex,
						0.0,
						0.0,
						sampleWidth*0.9,
						labelHeight*0.9
						);        		//g.drawString(this.tabixFile.getFile().getName(),0,0);
        		g.setTransform(old);
				
				
				double tx = marginWidth+x*sampleWidth;
				for(int y=0;y< samples.size();++y)
					{
					double ty = marginWidth+y*sampleWidth;
					g.translate(tx, ty);
					g.setColor(Color.BLUE);
					g.draw(new Rectangle2D.Double(0, 0, sampleWidth, sampleWidth));
					//paint each chromosome
					paintReference();	
					g.translate(-tx, -ty);
					}
				}
			info("genomicSizePerCurveUnit:"+(long)genomicSizePerCurveUnit*evalCurve.count+" "+dict.getReferenceLength()+" count="+evalCurve.count);
			info("Scanning variants");
			SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(this.dict);
			while(iter.hasNext())
				{
				VariantContext var=iter.next();
				progress.watch(var.getContig(), var.getStart());
				for(int x=0;x< samples.size();++x)
					{
					String samplex =  samples.get(x);
					Genotype gx = var.getGenotype(samplex);
					double tx = marginWidth+x*sampleWidth;

					for(int y=0;y< samples.size();++y)
						{
						String sampley =  samples.get(y);
						Genotype gy = var.getGenotype(sampley);
						if(gx.isHomRef() && gy.isHomRef()) continue;
						double ty = marginWidth+y*sampleWidth;
						g.translate(tx, ty);
						PaintVariant paint=new PaintVariant(var, x, y);
						paint.run();
						g.translate(-tx, -ty);
						}
					}
				}
			progress.finish();
			this.g.dispose();
			
			//save file
			info("saving "+imgOut);
			if(imgOut.getName().toLowerCase().endsWith(".png"))
				{
				ImageIO.write(img, "PNG", imgOut);
				}
			else
				{
				ImageIO.write(img, "JPG", imgOut);
				}
			return 0;
			}
		catch(Exception err)
			{
			error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(iter);
			}
		}
	public static void main(String[] args) {
		new VcfToHilbert().instanceMainWithExit(args);
	}
}
