/*
The MIT License (MIT)

Copyright (c) 2019 Pierre Lindenbaum

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

*/
package com.github.lindenb.jvarkit.tools.burden;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.jfx.JFXChartExporter;
import com.github.lindenb.jvarkit.util.Pedigree;
import com.github.lindenb.jvarkit.util.jcommander.JfxLauncher;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.AFExtractorFactory;
import com.github.lindenb.jvarkit.util.vcf.AFExtractorFactory.AFExtractor;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
BEGIN_DOC

### Example

```
java -jar dist/casectrljfx.jar --pedigree  mutations.ped mutations.vcf
```
### See also:

  * https://twitter.com/yokofakun/status/860495863633805312

## screenshot

![screenshot](https://pbs.twimg.com/media/C_EYa54W0AAopkl.jpg)


END_DOC
 */
@Program(
	name="casectrljfx",
	description="display jfx chart of case/control maf from a VCF and a pedigree",
	keywords={"vcf","pedigree","case","control","visualization","jfx","chart","maf"}
	)
public class CaseControlJfx extends JfxLauncher {
	
	private static final Logger LOG = Logger.build(CaseControlJfx.class).make();

	private enum VariantPartitionType{
		chromosome,
		variantType,
		autosomes,
		qual,
		vqslod,
		typeFilter,
		distance,
		n_alts
		}
	
	private static interface VariantPartition
		{
		public List<XYChart.Series<Number,Number>> getSeries();
		public void add(VariantContext vc,Pedigree ped, XYChart.Data<Number,Number> data);
		}
	
	private static class VariantTypePartition implements VariantPartition
		{
		final List<XYChart.Series<Number,Number>> series = new ArrayList<>( VariantContext.Type.values().length);
		VariantTypePartition() 
			{
			for(final VariantContext.Type t:VariantContext.Type.values()) {
				final XYChart.Series<Number,Number> serie = new XYChart.Series<>();
				serie.setName(t.name());
				this.series.add(serie);
				}
			}
		@Override
		public List<XYChart.Series<Number,Number>> getSeries()
			{
			return this.series;
			}
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			final VariantContext.Type t= vc.getType();
			this.series.get(t.ordinal()).getData().add(data);
			}
		}
	
	private static abstract class MapPartition implements VariantPartition
		{
		final Map<String,XYChart.Series<Number,Number>> series = new TreeMap<>();
		MapPartition() 
			{
			}
		
		protected XYChart.Series<Number,Number> get(String key) {
			XYChart.Series<Number,Number> S = this.series.get(key);
			if(S==null)
				{
				S = new XYChart.Series<>();
				S.setName(key);
				this.series.put(key,S);
				}
			return S;
			}
		@Override
		public List<XYChart.Series<Number,Number>> getSeries()
			{
			return new ArrayList<>(this.series.values());
			}
	
		}


	private static class ChromosomePartition extends MapPartition
		{
		ChromosomePartition() 
			{
			}
		
		protected String normalizeContig(final String str) {
			return str;
			}
		
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			final String str = normalizeContig(vc.getContig());
			if(str==null || str.isEmpty()) return;
			this.get(str).getData().add(data);
			}
		}
	
	private static class SexualContigPartition extends ChromosomePartition
		{
		protected String normalizeContig(final String str) {
			if( str.equalsIgnoreCase("chrX") || str.equalsIgnoreCase("X") || 
				str.equalsIgnoreCase("chrY") || str.equalsIgnoreCase("Y")
				) return str; 
			return "Autosome";
			}
		}

	private static class QualPartition extends MapPartition
		{
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			final String str ;
			if(vc.hasLog10PError())
				{		
				int qual=(int)vc.getPhredScaledQual();
				if(qual <10) str="<10";
				else if(qual <20) str="<20";
				else if(qual <30) str="<30";
				else if(qual <40) str="<40";
				else if(qual <50) str="<50";
				else if(qual <100) str="<100";
				else str=">=100";
				}
			else
				{
				str="N/A";
				}
			this.get(str).getData().add(data);
			}
		}
	private static class VQSLODPartition extends MapPartition
		{
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			if(!vc.hasAttribute("VQSLOD")) return;
			double vqslod=vc.getAttributeAsDouble("VQSLOD",Double.NaN);
			if(Double.isNaN(vqslod)) return;
			final int WINDOW=5;
			int norm=WINDOW*(int)(vqslod/((double)WINDOW));
			String str=String.valueOf(norm);
			
			this.get(str).getData().add(data);
			}
		}

	private static class NAltsPartition extends MapPartition
		{
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			this.get(String.valueOf(vc.getAlternateAlleles().size())).getData().add(data);
			}
		}
	
	private static class DisanceToDiagonalPartiton extends MapPartition
		{
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			
			double x1= data.getXValue().doubleValue();
			double y1= data.getYValue().doubleValue();
			// use 'mirror' point on diagonal
			double x2 = y1;
			double y2 = x1;
			//distance to diagonal is half distance between (x1,y1) and (x2,y2)
			double distance= Math.sqrt( Math.pow((x1-x2),2)+ Math.pow(y1-y2,2))/2.0;
			final double WINDOW=10.0;
			double norm= Math.abs(((int)(distance*WINDOW))/(double)WINDOW);
			this.get(String.valueOf(norm)).getData().add(data);
			}
		}
	
	private static class TypeAndFilterPartiton extends MapPartition
		{
		@Override
		public void add(VariantContext vc,Pedigree ped,XYChart.Data<Number,Number> data){
			if(vc==null) return;
			
			final String str=vc.getType().name()+(vc.isFiltered()?" FILTERED":" PASS");
			
			this.get(str).getData().add(data);
			}
		}
	
	
	private enum SelectSamples { all,males,females};
	
	
		@Parameter(names={"-p","--ped","--pedigree"},description="Pedigree File. If not defined, try to use the pedigree inserted in the VCF header.")
		File pedigreeFile = null;
		@Parameter(names={"-partition","--partition"},description="partition type. How series are built. For example 'variantType' will produces some series for INDEL, SNP, etc... ")
		VariantPartitionType partitionType=VariantPartitionType.variantType;
		@Parameter(names={"-nchr","--nocallishomref"},description="treat no call as HomRef")
		boolean no_call_is_homref=false;
		@Parameter(names={"-filter","--filter"},description="Ignore FILTERed variants")
		boolean ignore_ctx_filtered=false;
		@Parameter(names={"-gtfilter","--genotypefilter"},description="Ignore FILTERed Genotypes")
		boolean ignore_gt_filtered=false;
		@Parameter(names={"--legendside"},description="Legend side")
		Side legendSide = Side.RIGHT;
		@Parameter(names={"--tooltip"},description="add mouse Tooltip the point (requires more memory)")
		boolean add_tooltip = false;
		@Parameter(names={"--limit"},description="Limit to 'N' variants. negative==no limit; All point are loaded in memory. The more variants you have, the more your need memory")
		int limit_to_N_variants = -1;
		@Parameter(names={"--sex"},description="Select/Filter samples on their gender.")
		SelectSamples selectSamples=SelectSamples.all;
		@Parameter(names={"--title"},description="Default title for the graph")
		String userTitle=null;
		@Parameter(names={"--opacity"},description="Point opaciy [0-1]")
		double dataOpacity=0.4;
		@Parameter(names={"-o","--out"},description="Save the image in a file and then exit.")
		File outputFile=null;
		@Parameter(names={"-mafTag","--mafTag"},description=
				"[20180905] Do not calculate MAF for controls, but use this tag to get Controls' MAF. " +
				AFExtractorFactory.OPT_DESC
				)
		String controlFields =null;
		
		public CaseControlJfx()
			{
			}
		
		@Override
		public int doWork(final Stage primaryStage,final List<String> args) {
			final VariantPartition partition;
			Pedigree pedigree = null;
			VcfIterator in = null;
			try {
				
				switch(this.partitionType)
					{
					case variantType: partition = new VariantTypePartition(); break;
					case chromosome: partition = new ChromosomePartition(); break;
					case autosomes: partition = new SexualContigPartition(); break;
					case qual : partition = new QualPartition();break;
					case vqslod : partition = new VQSLODPartition();break;
					case typeFilter : partition = new TypeAndFilterPartiton();break;
					case distance : partition = new DisanceToDiagonalPartiton(); break;
					case n_alts : partition = new NAltsPartition(); break;
					default: throw new IllegalStateException(this.partitionType.name());
					}
				
				if(args.isEmpty())
					{
					in = VCFUtils.createVcfIteratorStdin();
					primaryStage.setTitle(CaseControlJfx.class.getSimpleName());
					}
				else if(args.size()==1)
					{
					in = VCFUtils.createVcfIterator(args.get(0));
					primaryStage.setTitle(args.get(0));
					}
				else
					{
					LOG.error("Illegal Number of arguments: " + args);
					return -1;
					}
				if(this.pedigreeFile!=null)
					{
					pedigree = Pedigree.newParser().parse(this.pedigreeFile);
					}
				else
					{
					pedigree = Pedigree.newParser().parse(in.getHeader());
					}
				final AFExtractor controlAFExtractor;
				if(!StringUtil.isBlank(this.controlFields))
					{
					final List<AFExtractor> extractors = new AFExtractorFactory().parseFieldExtractors(this.controlFields);
					if(extractors.size()!=1) {
						LOG.error("extractor list should have size==1 . got "+extractors);
						return -1;
						}
					controlAFExtractor = extractors.get(0);
					if(!controlAFExtractor.validateHeader(in.getHeader())) {
						LOG.error("Invalid : "+controlAFExtractor);
						return -1;
						}
					}
				else
					{
					controlAFExtractor = null;
					}
				
				int count = 0;
				final SAMSequenceDictionaryProgress progress = new SAMSequenceDictionaryProgress(in.getHeader());
				while(in.hasNext() && (this.limit_to_N_variants<0 || count<this.limit_to_N_variants)) 
					{
					final VariantContext ctx=progress.watch(in.next());
					
					if(this.ignore_ctx_filtered && ctx.isFiltered()) continue;
					
					++count;
					
					final List<Allele> alternates = ctx.getAlternateAlleles();
					for(int alt_idx=0;alt_idx < alternates.size();++alt_idx) {
						final Allele alt = alternates.get(alt_idx);
						final Double mafs[]={null,null};
						
						for(int i=0;i< 2;++i)
							{
							if(i==1 && controlAFExtractor!=null)
								{
								final List<Double> dvals = controlAFExtractor.parse(ctx);
								if(alt_idx< dvals.size() && dvals.get(alt_idx)!=null) {	
								    final double d= dvals.get(alt_idx);
									if(!Double.isNaN(d) && d>=0 && d<=1.0) mafs[1]=d;
									}
								}
							else
								{
								final MafCalculator mafCalculator = new MafCalculator(alt, ctx.getContig());
								mafCalculator.setNoCallIsHomRef(no_call_is_homref);
								for(Pedigree.Person person: (i==0?pedigree.getAffected():pedigree.getUnaffected()))
									{
									if(selectSamples.equals(SelectSamples.males) && !person.isMale()) continue;
									if(selectSamples.equals(SelectSamples.females) && !person.isFemale()) continue;
									
									final Genotype genotype = ctx.getGenotype(person.getId());
									if(genotype==null) continue;
									if(ignore_gt_filtered && genotype.isFiltered()) continue;
									mafCalculator.add(genotype, person.isMale());
									}
								if(!mafCalculator.isEmpty())
									{
									mafs[i]=mafCalculator.getMaf();
									}
								}
							}
						if(mafs[0]==null || mafs[1]==null) continue;
						final XYChart.Data<Number,Number> data = new  XYChart.Data<Number,Number>(mafs[0],mafs[1]);
						if(this.add_tooltip && this.outputFile==null)
							{
							data.setExtraValue(ctx.getContig()+":"+ctx.getStart());
							}
						partition.add(ctx,pedigree,data);
						}
					}
				progress.finish();
				in.close();in=null;
				}
			catch(final Exception err)
				{	
				LOG.error(err);
				return -1;
				}
			finally {
				CloserUtil.close(in);
				}
			
	        final NumberAxis xAxis = new NumberAxis(0.0,1.0,0.1);
	        xAxis.setLabel("Cases");
	        
	        final NumberAxis yAxis = new NumberAxis(0.0,1.0,0.1);
	        yAxis.setLabel("Controls"+(StringUtil.isBlank(this.controlFields)?"":"["+this.controlFields+"]"));
	        final ScatterChart<Number, Number>   chart =  new ScatterChart<>(xAxis,yAxis);
	        for(final XYChart.Series<Number,Number> series:partition.getSeries())
		        {
				chart.getData().add(series);
		        }
			String title="Case/Control";
			if(!args.isEmpty())
				{
				title= args.get(0);
				int slash=title.lastIndexOf("/");
				if(slash!=-1) title=title.substring(slash+1);
				if(title.endsWith(".vcf.gz")) title=title.substring(0, title.length()-7);
				if(title.endsWith(".vcf")) title=title.substring(0, title.length()-4);
				}
			if(userTitle!=null) title=userTitle;
			chart.setTitle(title);
			chart.setAnimated(false);
			chart.setLegendSide(this.legendSide);
			
			
		    final VBox root = new VBox();    
		    
		    MenuBar menuBar = new MenuBar();
		    Menu menu = new Menu("File");
		    MenuItem item=new MenuItem("Save image as (.png,.jpg,.R)...");
		    item.setOnAction(AE->{doMenuSave(chart);});
		    menu.getItems().add(item);
		    menu.getItems().add(new SeparatorMenuItem());
		    item=new MenuItem("Quit");
		    item.setOnAction(AE->{Platform.exit();});
		    menu.getItems().add(item);
		    menuBar.getMenus().add(menu);
		    root.getChildren().add(menuBar);
		    
			final BorderPane contentPane=new BorderPane();
			
			contentPane.setCenter(chart);
			
			root.getChildren().add(contentPane);
			Rectangle2D screen=Screen.getPrimary().getVisualBounds();
			double minw = Math.max(Math.min(screen.getWidth(),screen.getHeight())-50,50);
	        chart.setPrefSize(minw, minw);
	        final  Scene scene= new Scene(root,minw,minw);
			primaryStage.setScene(scene);
			
			if(this.outputFile!=null)
	        	{
	        	 primaryStage.setOnShown(WE->{
	        		 LOG.info("saving as "+this.outputFile);
	        		 try
	        		 	{
	        			saveImageAs(chart,this.outputFile);
	        		 	}
	        		 catch(IOException err)
	        		 	{
	        			LOG.error(err);
	        			System.exit(-1);
	        		 	}
	        		 Platform.exit();
	        	 });
	        	}
			
	        primaryStage.show();	
	        
	       
	        
	        if(this.outputFile==null)
		        {
	        	//http://stackoverflow.com/questions/14117867
		        for(final XYChart.Series<Number,Number> series:partition.getSeries())
			        {
		        	for (XYChart.Data<Number, Number> d : series.getData()) {
		        		if(dataOpacity>=0 && dataOpacity<1.0)
		        			{
		        			d.getNode().setStyle(d.getNode().getStyle()+"-fx-opacity:0.3;");
		        			}
		        		if(this.add_tooltip) {
	        			 	final Tooltip tooltip = new Tooltip();
	        	            tooltip.setText(
			                        String.format("%s (%f / %f)",
			                        		String.valueOf(d.getExtraValue()),
			                                d.getXValue().doubleValue(), 
			                                d.getYValue().doubleValue()));
	        	            Tooltip.install(d.getNode(), tooltip);
		        		}
		            }
			        }
		        }
	        return 0;
	        }
		private void doMenuSave(final ScatterChart<Number, Number>   chart)
			{
			final FileChooser fc = new FileChooser();
			final File file = fc.showSaveDialog(null);
	    	if(file==null) return;
	    	try {
	    		saveImageAs(chart,file);
	        } catch (final IOException e) {
	            super.displayAlert(e);
	        	}
			}
		
		 private void saveImageAs(final ScatterChart<Number, Number>   chart,final File file)
		 	throws IOException
		 	{
			 PrintWriter pw = null;
			 try {
				if(file.getName().endsWith(".R"))
					{
					pw = new PrintWriter(file);
					final JFXChartExporter exporter=new JFXChartExporter(pw);
					exporter.exportToR(chart);
					pw.flush();
					pw.close();
					}
				else
					{
		    		final WritableImage image = chart.snapshot(new SnapshotParameters(), null);
		    		String format="png";
		    		if(file.getName().toLowerCase().endsWith(".jpg") ||file.getName().toLowerCase().endsWith(".jpeg") )
		    			{
		    			format="jpg";
		    			}
		            ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);
				 	}
			 	}
			 finally
			 	{
				CloserUtil.close(pw); 
			 	}
		 	}
		
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	
	
}
