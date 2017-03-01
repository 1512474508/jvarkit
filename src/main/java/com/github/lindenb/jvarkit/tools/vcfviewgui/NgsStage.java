/*
The MIT License (MIT)

Copyright (c) 2017 Pierre Lindenbaum

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
package com.github.lindenb.jvarkit.tools.vcfviewgui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.ChartFactory;
import com.github.lindenb.jvarkit.util.Hershey;
import com.github.lindenb.jvarkit.util.igv.IgvSocket;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.Locatable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Chart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

/**
 * Abstract Stage for Bam and VCF file
 * @author lindenb
 *
 */
public abstract class NgsStage<HEADERTYPE,ITEMTYPE extends Locatable> extends Stage {
    protected static final Logger LOG= Logger.getLogger("NgsStage");
	protected static final String JAVASCRIPT_TAB_KEY="JS";
	private static final int REFRESH_SECOND=Integer.parseInt(System.getProperty("jfxngs.refresh.seconds","5"));
    static final ExtensionFilter JS_EXTENSION_FILTER=new ExtensionFilter("Javascript Files", ".js",".javascript");

	/** owner Application */
    protected JfxNgs owner;
    /** src file */
	private final NgsFile<HEADERTYPE,ITEMTYPE> ngsFile;
	/** javascript filtering */
	protected final TextArea javascriptArea=new TextArea();
	/** draw the karyotype */
	protected final SeqDictionaryCanvas seqDictionaryCanvas;
	/** message stuff */
	protected final Label messageLabel=new Label();
	/** message stuff */
	protected final TextField gotoField=new TextField();
	/** menuBar */
	protected final MenuBar menuBar=new MenuBar();
	/** File Menu */
	protected final Menu fileMenu=new Menu("File");
	/** Stats Menu */
	protected final Menu statsMenu=new Menu("Stats");
	
    /** limit number of items */
	protected final Spinner<Integer> maxItemsLimitSpinner=
			new Spinner<>(0, 1000, 1);

	/** simple pair chromosome/pos */
	protected static class ContigPos
	implements Comparable<ContigPos>, Locatable
		{
		final String contig;
		final int position;
		ContigPos(final String contig,final int position) {
			this.contig=contig;
			this.position=position;
		}
		
		@Override
		public String getContig() {
			return this.contig;
			}
		@Override
		public int getStart() {
			return this.position;
			}
		@Override
		public int getEnd() {
			return this.position;
			}
		@Override
		public int compareTo(final ContigPos o) {
			int i=contig.compareTo(o.contig);
			if(i!=0) return i;
			return position-o.position;
			}
		@Override
		public String toString() {
			return this.contig+":"+this.position;
			}
		}
	
	/* pane used to draw the karyotype of the reference */
	protected class SeqDictionaryCanvas
		extends BorderPane
		{
		private final SAMSequenceDictionary dict;
		private final double refLength;
		private ContigPos itemsStart=null;
		private ContigPos itemsEnd=null;
		private Interval selectInterval=null;
		private final Map<String,Long> chrom2start;
		private final Canvas canvas;
	    private final Tooltip mousePositionToolTip = new Tooltip("dddd");
    	private java.text.DecimalFormat niceIntFormat = new java.text.DecimalFormat("###,###");

		SeqDictionaryCanvas(final SAMSequenceDictionary dict) {
			this.dict = dict;
			this.chrom2start=new HashMap<>(dict.size());
			this.refLength = dict.getReferenceLength();
			this.canvas = new Canvas(500,25);
			
			this.setCenter(this.canvas);
			this.setPrefWidth(Double.MAX_VALUE);
			this.setPrefWidth(25.0);
            long p=0L;
            for(final SAMSequenceRecord ssr: dict.getSequences())
            	{
            	this.chrom2start.put(ssr.getSequenceName(),p);
            	p+=ssr.getSequenceLength();
            	}
            this.canvas.setOnMouseClicked(AE->{
            	if(AE.getClickCount()<2 || this.canvas.getWidth()==0) return;
            	final ContigPos cp = pixel2base(AE.getX());
            	if(cp==null) return;
            	NgsStage.this.gotoField.setText(cp.contig+":"+cp.position);
                NgsStage.this.reloadData();		
            	});
            Tooltip.install(this.canvas,this.mousePositionToolTip);
            this.canvas.setOnMouseMoved(AE->{
           		final ContigPos cp = pixel2base(AE.getX());
           		mousePositionToolTip.setText(cp==null?"":cp.contig+":"+this.niceIntFormat.format(cp.position));});
			}
		
		private ContigPos pixel2base(final double pixx)
			{
			final long x= (long)((pixx/this.canvas.getWidth())*this.refLength);
			long p1=0L;
			for(final SAMSequenceRecord ssr: this.dict.getSequences())
	        	{
	        	if(p1<=x && x<=p1+ssr.getSequenceLength()) {
	        		return new ContigPos(ssr.getSequenceName(),(int)(x-p1));
	        		}
	        	p1+=ssr.getSequenceLength();
	        	}
			return null;
			}
		
		public void setItemsInterval(final ContigPos start,final ContigPos end) {
			this.itemsStart = start;
			this.itemsEnd = end;
			draw();
			}
		public void setSelectInterval(final Interval selectInterval) {
			this.selectInterval = selectInterval;
			draw();
			}
		// http://stackoverflow.com/questions/31761361/
        @Override
        protected void layoutChildren() {
        	final double x = snappedLeftInset();
            final double y = snappedTopInset();
            final double w = snapSize(getWidth()) - x - snappedRightInset();
            final double h = snapSize(getHeight()) - y - snappedBottomInset();
            canvas.setLayoutX(x);
            canvas.setLayoutY(y);
            canvas.setWidth(w);
            canvas.setHeight(h);
            draw();
        }
		private double base2pixel(final String contig,final int v) {
			final Long n=this.chrom2start.get(contig);
			if(n==null) return -9999.99;
			return ((double)(n+v)/this.refLength)*this.canvas.getWidth();
			}
		
		public void draw() {
            final double boundrec=5.0;
			final double width = canvas.getWidth();
			final double height = canvas.getHeight();
			if(width<=1.0 || height<=1.0) return;
            final GraphicsContext gc = this.canvas.getGraphicsContext2D();
            gc.setGlobalAlpha(1.0);
            gc.clearRect(0, 0, width, height);
            final Paint p1= new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE,
            		new Stop(0.0, Color.DARKGRAY),
            		new Stop(0.5, Color.WHITE),
            		new Stop(1.0,Color.DARKGRAY)
            		);
            final Paint p2= new LinearGradient(0, 0, 0, 1.0, true, CycleMethod.NO_CYCLE,
            		new Stop(0.0, Color.DARKSLATEBLUE),
            		new Stop(0.5, Color.WHITE),
            		new Stop(1.0,Color.DARKSLATEBLUE)
            		);
            final Hershey hershey = new Hershey();
            /* paint each chrom */
            for(int i=0;i< this.dict.size();++i)
            	{
            	final SAMSequenceRecord ssr = this.dict.getSequence(i);
            	final double x0 = base2pixel(ssr.getSequenceName(),1);
            	final double x1 =  base2pixel(ssr.getSequenceName(),ssr.getSequenceLength());
            	double labelh = height-5;
            	double labelw=Math.min(ssr.getSequenceName().length()*labelh,(x1-x0));
            	
            	gc.setLineWidth(0.5);
            	gc.setFill(i%2==0?p1:p2);
            	gc.fillRoundRect(x0, 1, (x1-x0), height-2, boundrec, boundrec);
            	gc.setStroke(Color.BLACK);
            	gc.strokeRoundRect(x0, 1, (x1-x0), height-2, boundrec, boundrec);
            	if(labelh>3 && labelw>3) {
	            	gc.setStroke(Color.BLACK);
	            	gc.setLineWidth(1.0);
	            	hershey.paint(gc, ssr.getSequenceName(), x0+(x1-x0)/2.0-labelw/2.0,2.0, labelw, labelh);
	            	}
            	}
            /** draw current region of items */
            if(this.itemsStart!=null && this.itemsEnd!=null) {
            	final double x0 = base2pixel(this.itemsStart.contig,this.itemsStart.position);
            	final double x1 =  base2pixel(this.itemsEnd.contig,this.itemsEnd.position);
            	gc.setGlobalAlpha(0.8);
            	gc.setFill(Color.ORANGE);
            	gc.fillRoundRect(x0, 1, (x1-x0)+1.0, height-2, 1, 1);
            	}
            /* draw current selected item */
            if(selectInterval!=null) {
            	final double x0 = base2pixel(selectInterval.getContig(),selectInterval.getStart());
            	final double x1 =  base2pixel(selectInterval.getContig(),selectInterval.getEnd());
            	gc.setGlobalAlpha(0.8);
            	gc.setFill(Color.RED);
            	gc.fillRoundRect(x0, 1, (x1-x0)+1.0, height-2, 1, 1);
            	}
			}

		}
	
	/** javascript filter used to filter a NgsFile*/
    protected static abstract class JavascriptFilter<HEADER,DATATYPE> {
    	/** the compiled script */
		protected final Optional<CompiledScript> compiledScript;
		/** bindings that will be injected in the script */
		protected final SimpleBindings bindings=new SimpleBindings();
		Optional<Throwable> encounteredException=Optional.empty();
		protected JavascriptFilter(
				final HEADER header,
				final Optional<CompiledScript> compiledScript)
			{
			this.bindings.put("header", header);
			this.compiledScript=compiledScript;
			}
		public abstract DATATYPE eval(DATATYPE v);
		
		 /** called by javascript filters */
	    protected boolean accept()
			{
	    	if(!this.compiledScript.isPresent()) return true;
			final Object result;
			try  {
				result = this.compiledScript.get().eval(this.bindings);
			} catch(final ScriptException err)
			{
				if(!this.encounteredException.isPresent())
					{
					LOG.severe(err.getMessage());
					err.printStackTrace();
					this.encounteredException = Optional.of(err);
					}
				return false;
			}
			
			if(result==null) return false;;
			if(result instanceof Boolean)
				{
				if(Boolean.FALSE.equals(result)) return false;
				}
			else if(result instanceof Number)
				{
				if(((Number)result).intValue()!=1) return false;
				}
			else
				{
				if(!this.encounteredException.isPresent())
					{
					final String err="Script returned something that is not a boolean or a number:"+result.getClass();
					LOG.warning(err);
					this.encounteredException = Optional.of(new ScriptException(err));
					}
				return false;
				}
			return true;
			}
		}

	
	protected abstract class AbstractQualityStage
		extends Stage
		{
		protected abstract class ScanThread 
			extends Thread
			{
			/** number of items scanned so far */
			protected long nItems=0L;
			/** file source */
			protected final NgsFile<HEADERTYPE, ITEMTYPE> ngsReader;
			/** script for filtering */
			protected final Optional<CompiledScript> compiledScript;
			/** should we stop the scanning */
			protected volatile boolean kill_flag=false;
			/** time of last refresh */
    		protected long lastRefresh =System.currentTimeMillis();
    		/** active chart factory */
    		protected final ChartFactory<HEADERTYPE,ITEMTYPE> factory;
    		/**  other filters */
    		protected final Predicate<ITEMTYPE> otherFilters;
    		/** last seen exception */
    		protected Optional<Throwable> encounteredException=Optional.empty();

    		ScanThread(
    				final ChartFactory<HEADERTYPE,ITEMTYPE> factory,
    				final NgsFile<HEADERTYPE, ITEMTYPE> ngsReader,
    				final Optional<CompiledScript> compiledScript,
    				final Predicate<ITEMTYPE> otherFilters)
				{
				this.ngsReader=ngsReader;
				this.compiledScript = compiledScript;
				this.factory=factory;
				this.otherFilters= otherFilters;
				}
            
    		
            /** called at end */
            protected void atEnd() {
            	CloserUtil.close(this.ngsReader);
            	if(kill_flag) {
					LOG.warning("Thread was killed");
					}
				else
    				{
					/** repaint for the last time */
    				repaint();
    				Platform.runLater(new Runnable() {
        				 @Override
        				public void run() {
        					 AbstractQualityStage.this.countItemsLabel.setText(
        						"Done... Number of items: "+nItems+ (kill_flag?" [KILLED]":""));
        					 if(encounteredException.isPresent())
        					 	{
        						JfxNgs.showExceptionDialog(NgsStage.this, encounteredException.get());
        					 	}
        				 }
        			 	});
    				}
            	}
            protected void repaint()
				{
				final Chart chart= this.factory.build();
				 Platform.runLater(new Runnable() {
					 @Override
					public void run() {
						AbstractQualityStage.this.contentPane.setCenter(chart);
						AbstractQualityStage.this.countItemsLabel.setText("Running... Number of items: "+nItems);
					 	}
				 	});
				}
            protected void update()
				{
				long now  =System.currentTimeMillis(); 
				if( kill_flag || now - lastRefresh < REFRESH_SECOND*1000) return ;//5 seconds;
				lastRefresh = now;
				repaint();
				}
            protected void onError(final Throwable err)
            	{
            	CloserUtil.close(this.ngsReader);
            	LOG.severe(err.getMessage());
				Platform.runLater(new Runnable() {
    				 @Override
    				public void run() {
    					 AbstractQualityStage.this.countItemsLabel.setText(
    						"ERROR "+err.getMessage());
    				 	}
    			 	});
            	}
            
			}
		
		protected final ScanThread thread;
		protected final BorderPane contentPane=new BorderPane();
		protected final Label countItemsLabel=new Label();

		protected AbstractQualityStage(
				final ChartFactory<HEADERTYPE,ITEMTYPE> factory,
				final NgsFile<HEADERTYPE,ITEMTYPE> ngsReader,
				final Optional<CompiledScript> compiledScript,
				final Predicate<ITEMTYPE> otherFilters
				)
			{
			this.setTitle(factory.getName()+" : "+ngsReader.getSource());
			this.thread = createThread(factory,ngsReader,compiledScript,otherFilters);
	    	this.addEventHandler(
	    			WindowEvent.WINDOW_CLOSE_REQUEST ,new EventHandler<WindowEvent>() {
	                    @Override
	                    public void handle(WindowEvent event) {
	                    	kill();
	                        }
	                    });

        	this.addEventHandler(
        			WindowEvent.WINDOW_SHOWN ,new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                        	thread.start();
                            }
                        });
        	this.contentPane.setCenter(factory.build());
        	this.contentPane.setBottom(this.countItemsLabel);
        	final Scene scene=new Scene(this.contentPane,1000,500);
        	this.setScene(scene);
			}
		/** create the thread that will scan the file in the background */
		protected abstract ScanThread createThread(
				final ChartFactory<HEADERTYPE,ITEMTYPE> factory,
				final NgsFile<HEADERTYPE, ITEMTYPE> ngsReader,
				final Optional<CompiledScript> compiledScript,
				final Predicate<ITEMTYPE> otherFilters
				);
		@Override
    	protected void finalize() throws Throwable {
    		kill();
	    	super.finalize();
	    	}
    	void kill()
    		{
    		thread.kill_flag=true;
    		}
    	}
	
    protected NgsStage(
    		final JfxNgs owner,
    		final NgsFile<HEADERTYPE,ITEMTYPE> ngsFile
    		) throws IOException
    	{
    	this.owner= owner;
    	this.ngsFile= ngsFile;
    	this.setTitle(this.ngsFile.getSource());
    	this.maxItemsLimitSpinner.setEditable(true);
    	this.maxItemsLimitSpinner.setTooltip(new Tooltip(
    			"The whole file is NOT loaded, only a subset of data will be read."));
    	
    	if(ngsFile.getSequenceDictionary()==null)
    		{
    		throw new IOException("There is no associated dictionary for "+ngsFile);
    		}
    	this.seqDictionaryCanvas = new  SeqDictionaryCanvas(ngsFile.getSequenceDictionary());
    	
    	if(!this.owner.javascriptCompiler.isPresent()) {
    		this.javascriptArea.setEditable(false);
    		this.javascriptArea.setPromptText("Javascript engine is not available");
    	} else
    		{
    		this.javascriptArea.setPromptText("Use this area to create a javascript-bases filter to ignore some items");
    		}
    	
    	
    	this.addEventHandler(
    			WindowEvent.WINDOW_SHOWING ,WE-> {
                        owner.registerStage(NgsStage.this);
                        NgsStage.this.reloadData();   
                    });
    	this.addEventHandler(
    			WindowEvent.WINDOW_CLOSE_REQUEST ,WE->{
                    	NgsStage.this.ngsFile.close();
                    	owner.unregisterStage(NgsStage.this);
                    });
       
        this.gotoField.setPrefColumnCount(25);
        this.gotoField.setEditable(true);
        
        
        this.fileMenu.getItems().addAll(owner.createCommonMenuItems(this));
        this.fileMenu.getItems().add(JfxNgs.createMenuItem("Save filtered data as...", ()->doMenuSaveAs()));

    	
        this.menuBar.getMenus().add(this.fileMenu);
    	this.menuBar.getMenus().add(this.statsMenu);
    	this.menuBar.getMenus().add(this.createJavascriptSnippetMenu());
    	}
    
    @Override
    protected void finalize() throws Throwable {
    	this.ngsFile.close();
    	super.finalize();
    	}
    
    /** path to java script snippets */
    protected abstract String getSnippetResourcePath();
    
    /** generate a set of common button for handling javascript */
    protected List<Button> makeJavascriptButtons()
    	{
    	List<Button> buttons=new ArrayList<>();
    	if(!owner.javascriptCompiler.isPresent()) return buttons;
    	Button button= setTooltip( new Button("Save as..."),
    			"Save the Script below in a text file"
    			);
    	button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser fc= owner.newFileChooser();
				fc.setSelectedExtensionFilter(JS_EXTENSION_FILTER);
				File js=owner.updateLastDir(fc.showSaveDialog(NgsStage.this));
				if(js==null) return;
				PrintWriter pw=null;
				try 
					{
					pw=new PrintWriter(js);
					pw.write(javascriptArea.getText());
					pw.flush();
					pw.close();
					}
				catch(final Exception err)
					{
					JfxNgs.showExceptionDialog(NgsStage.this, err);
					}
				finally
					{
					CloserUtil.close(pw);
					}
			}
			});
    	buttons.add(button);
    	
    	button= setTooltip(new Button("Open..."),
    			"Open a javascript file  that will be used to filter the data");
    	button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final FileChooser fc= owner.newFileChooser();
				fc.setSelectedExtensionFilter(JS_EXTENSION_FILTER);
				final File js=owner.updateLastDir(fc.showOpenDialog(NgsStage.this));
				if(js==null) return;
				InputStream in=null;
				try 
					{
					in= new FileInputStream(js);
					javascriptArea.setText(IOUtil.readFully(in));
					in.close();
					}
				catch(final Exception err)
					{
					JfxNgs.showExceptionDialog(NgsStage.this, err);
					}
				finally
					{
					CloserUtil.close(in);
					}
			}
			});
    	buttons.add(button);
    	
    	button=setTooltip(
    			new Button("Validate"),
    			"Validate the Javascript syntax of the script below"
    			);
    	button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final String script=javascriptArea.getText();
				try 
					{
					owner.javascriptCompiler.get().compile(script);
					final Alert alert=new Alert(AlertType.CONFIRMATION);
					alert.setAlertType(AlertType.CONFIRMATION);
					alert.setTitle("OK");
					alert.setContentText("OK. Script is compilable.");
					alert.showAndWait();
					}
				catch(final Exception err)
					{
					JfxNgs.showExceptionDialog(NgsStage.this, err);
					}
			}
			});
    	buttons.add(button);
   	
    	
    	return buttons;
    	}
    
    /** generate the javascript Menu, containing the snippets.
     * the snippet are stored as a xml file in the jar file
     * @return
     */
    private Menu createJavascriptSnippetMenu() {
    	final Menu menu=new Menu("Snippets");
    	final String rsrc = getSnippetResourcePath();
    	if(rsrc!=null && !rsrc.isEmpty()) {
    		LOG.info("Loading javascript snippets "+rsrc);
    		InputStream in=null;
    		XMLEventReader r=null;
    		try
    			{
    			in = getClass().getResourceAsStream(rsrc);
    			if(in!=null)
    				{
    				final XMLInputFactory xif=XMLInputFactory.newFactory();
    				xif.setProperty(XMLInputFactory.IS_COALESCING,Boolean.TRUE);
    				r=xif.createXMLEventReader(in);
    				final QName labelAtt=new QName("label");
    				final QName nameAtt=new QName("name");
    				while(r.hasNext())
    					{
    					XMLEvent evt=r.nextEvent();
    					if(!evt.isStartElement() ) continue;
    					StartElement start=evt.asStartElement();
    					if(!start.getName().getLocalPart().equals("code")) continue;
    					Attribute attLabel=start.getAttributeByName(labelAtt);
    					if(attLabel==null) attLabel=start.getAttributeByName(nameAtt);
    					if(attLabel!=null && r.hasNext() && r.peek().isCharacters())
    						{
        					final MenuItem item=new MenuItem(attLabel.getValue());
    						final String code= r.nextEvent().asCharacters().getData();

    						item.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									NgsStage.this.javascriptArea.setText(code);
									Parent parent=NgsStage.this.javascriptArea;
									while(parent!=null)
										{
										if(parent instanceof TabPane)
											{
											List<Tab> tabs=((TabPane)parent).getTabs();
											for(int x=0;x<tabs.size();++x)
												{
												if(tabs.get(x).getText().equals(JAVASCRIPT_TAB_KEY))
													{
													((TabPane)parent).getSelectionModel().select(tabs.get(x));
													break;
													}
												}
											break;
											}
										parent=parent.getParent();
										}
									
									
								}
							});
    						menu.getItems().add(item);
    						}
    					}
    				}
    			else
    				{
    				LOG.warning("Cannot read snippets "+rsrc);
    				}
    			}
    		catch(Exception err)
    			{
    			LOG.warning(err.getMessage());
    			}
    		finally
    			{
    			CloserUtil.close(r);
    			CloserUtil.close(in);
    			}
    		}
    	else
    		{
    		LOG.warning("No snippets defined for "+getClass());
    		}
    	return menu;
    	}
    
    /** send those command to IGV */
    protected void openInIgv(final List<String> commands)
    	{
    	if(commands==null || commands.isEmpty()) return;
    	@SuppressWarnings("resource")
		final IgvSocket socket=new IgvSocket();
    	final Runnable r=socket.buildRunnable(commands);
    	new Thread(r).start();
    	}

    /** create a MenuItem saving a table */
    protected <T> MenuItem menuForSavingTable(final String tableName,TableView<T> table)
    	{
    	final MenuItem item=new MenuItem("Save table '"+tableName+"' as ...");
    	item.setOnAction(new EventHandler<ActionEvent>()
			{
			@Override
			public void handle(final ActionEvent event)
				{
				final FileChooser fc= owner.newFileChooser();
				fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TSV","tsv"));
				final File fout= owner.updateLastDir(fc.showSaveDialog(NgsStage.this));
				if(fout==null) return ;
				PrintWriter out=null;
				try
					{
					out = new PrintWriter(fout);
					for(int x=0;x< table.getColumns().size();++x)
						{
						out.print(x==0?"#":"\t");
						out.print(table.getColumns().get(x).getText());
						}
					out.println();
					for(int y=0;y< table.getItems().size();++y)
						{
						final T row=table.getItems().get(y);
						for(int x=0;x< table.getColumns().size();++x)
							{
							if(x>0) out.print("\t");
							out.print(table.getColumns().get(x).getCellObservableValue(row).getValue());
							}
						out.println();
						}
					out.flush();
					}
				catch(Exception err)
					{
					err.printStackTrace();
					JfxNgs.showExceptionDialog(NgsStage.this, err);
					}
				finally
					{
					CloserUtil.close(out);
					}
				}
			});
    	return item;
    	}
    
    /** send a goto command to IGV */
    protected void openInIgv(final Locatable feature)
    	{
    	if(feature==null) {
    		updateStatusBar(AlertType.WARNING,"No Feature was selected");
    		return;
    	}
    	openInIgv(
    			Collections.singletonList("goto "+feature.getContig()+":"+feature.getStart()+"-"+feature.getEnd())
    			);
    	}
    
    abstract void openInIgv();
    /** reload all data */
    abstract void reloadData();
    
    /** show stats */
    protected abstract void doMenuShowWholeStats(final ChartFactory<HEADERTYPE,ITEMTYPE> factory);
    /** show stats for whole file */
    protected final <T> void doMenuShowLocalStats(final ChartFactory<HEADERTYPE,T> factory,final Supplier<List<T>> data)
    	{
    	LOG.info("creating chart "+factory.getName());
    	factory.setHeader(getNgsFile().getHeader());
    	factory.setPedigree(getPedigree());
    	final List<T> L=data.get();
    	LOG.info("creating n items "+L.size());

    	for(final T o:L) factory.visit(o);
		final Chart chart=factory.build();
    	final BorderPane contentPane=new BorderPane(chart);
    	contentPane.setPadding(new Insets(10));
    	final Stage dialog = new Stage();
    	dialog.initOwner(this);
    	dialog.setTitle(factory.getName());
    	contentPane.setTop(new Label("Data for "+this.ngsFile.getSource()));
       	dialog.setScene(new Scene(contentPane));
    	contentPane.setBottom(new Label("Number of items: "+L.size()));
    	LOG.info("Showing chart");
    	dialog.show();
    	}
    
    protected void updateStatusBar(final AlertType type,final Object o)
    	{
    	final Color textColor;
    	switch(type)
    		{
    		case CONFIRMATION: textColor=Color.BLUE; break;
    		case ERROR: textColor=Color.RED;break;
    		case INFORMATION: textColor=Color.GREEN;break;
    		case NONE: textColor=Color.BLACK; break;
    		case WARNING: textColor=Color.ORANGE;break;
    		default: textColor=Color.BLACK; break;
    		}
    	this.messageLabel.setTextFill(textColor);
    	if(o==null) {
    		this.messageLabel.setText("");
    		}
    	else if(o instanceof Throwable )
    		{
    		this.messageLabel.setText(String.valueOf(Throwable.class.cast(o).getMessage()));
    		}
    	else
    		{
    		this.messageLabel.setText(String.valueOf(o));
    		}
    	}
    
    protected <T,R> TableColumn<T,R> makeColumn(final String tag,final Function<T,R> supplier)
    	{
        final TableColumn<T,R>  col = new TableColumn<>(tag);
	        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<T,R>, ObservableValue<R>>() {				
				@Override
				public ObservableValue<R> call(CellDataFeatures<T, R> param) {
					return new ReadOnlyObjectWrapper<R>(supplier.apply(param.getValue()));
					}
				});
	        return col;
    	}
   
    
    /** build a table view for a Dictionary */
    protected Tab buildDictTab(final SAMSequenceDictionary dict)
        {
    	
		/* build dict Table */
		final TableView<SAMSequenceRecord> table=new TableView<>(
				dict==null?
				FXCollections.observableArrayList():
				FXCollections.observableArrayList(dict.getSequences())
				);
		
        table.getColumns().add(makeColumn("Name",SSR->SSR.getSequenceName()));
        table.getColumns().add(formatIntegerColumn(makeColumn("Length",SSR->SSR.getSequenceLength())));
        
        final Set<String> all_attributes=new HashSet<>();
        
       for(final SAMSequenceRecord ssr:dict.getSequences())
       		{
        	all_attributes.addAll(ssr.getAttributes().stream().
		        map(A->A.getKey()).
		        filter(S->!(S.equals("Name") || S.equals("Length"))).
		        collect(Collectors.toSet())
		        );
       		}
        for(final String key:all_attributes)
	        {
            if(dict.getSequences().stream().filter(S->S.getSpecies()!=null && !S.getSpecies().trim().isEmpty()).findAny().isPresent()) {
            	table.getColumns().add(makeColumn(key,SSR->SSR.getAttribute(key)));
            	}
	        }
        final Tab tab=new Tab("Dict", table);
        tab.setClosable(false);
        table.setPlaceholder(new Label("No Dictionary."));
        return tab;
        }
    /** save filtered Data As ... */
    protected abstract void doMenuSaveAs();
    
    
    /** called by main stage: set location box content and call reloadData */
    protected void moveTo(final String s)
    	{
    	this.gotoField.setText(s);
    	this.reloadData();
    	}
    
    private int parsePosition(String num) throws NumberFormatException
    	{
    	if( num==null || num.trim().isEmpty()) throw new NumberFormatException("bad number \""+num+"\"");
    	num = num.replace(",", "").trim();
    	if(num.toLowerCase().endsWith("bp"))
    		{
    		return Integer.parseInt(num.substring(0,num.length()-2))*1;
    		}
    	else if(num.toLowerCase().endsWith("kb"))
			{
			return Integer.parseInt(num.substring(0,num.length()-2))*1000;
			}
    	else if(num.toLowerCase().endsWith("mb"))
			{
			return Integer.parseInt(num.substring(0,num.length()-2))*1000000;
			}
    	else
    		{
    		return Integer.parseInt(num);
    		}
    	}
    
    protected Interval parseInterval(final String location)
    	{
    	final SAMSequenceDictionary dict=this.ngsFile.getSequenceDictionary();
		final String contig;
		int colon =location.indexOf(":");
		if(colon==-1)
			{
			contig=location.trim();
			}
		else
			{
			contig=location.substring(0,colon).trim();
			}
		
		SAMSequenceRecord ssr= dict.getSequence(contig);
		if(ssr==null && !contig.startsWith("chr"))
			{
			ssr= dict.getSequence("chr"+contig);
			}
		if(ssr==null && contig.startsWith("chr"))
			{
			ssr= dict.getSequence( contig.substring(3));
			}
		if(ssr==null)
			{
			updateStatusBar(AlertType.WARNING, "Cannot find contig in dictionary: "+location);
			return null;
			}
		
		if(colon!=-1)
			{
			final int hyphen = location.indexOf('-');
			final int plus = location.indexOf('+');
			
			if(hyphen!=-1 && plus!=-1) {
				updateStatusBar(AlertType.WARNING, "both '+' and '-' in "+location);
				return null;
				}
			
			Integer start=null,end=null;
			
			if(hyphen==-1 && plus==-1)
				{
				try {
					start= Math.max(0, parsePosition(location.substring(colon+1)));
					end=ssr.getSequenceLength();
					}
				catch(final NumberFormatException err ) {
					start=null;
					LOG.warning(location);
					updateStatusBar(AlertType.WARNING, "Bad Start in : "+location);
					return null;
					}
				}
			else
				{
				int delimidx=(hyphen==-1?plus:hyphen);
				try {
					start= Math.max(0, parsePosition(location.substring(colon+1,delimidx)));
					int num2= Math.min(
							parsePosition(location.substring(delimidx+1)),
							ssr.getSequenceLength()
							);
					if(plus!=-1) {
						int center = start;
						start=Math.max(center-num2,0);
						end = Math.min(center+num2,ssr.getSequenceLength());
						}
					else
						{
						end=num2;
						}
					}
				catch(final NumberFormatException err )
					{
					start=null;end=null;
					LOG.warning(location);
					updateStatusBar(AlertType.WARNING, "Bad Start/End in : "+location);
					return null;
					}
				}
			if(start!=null && end!=null && start.compareTo(end)<=0)
				{
				return new Interval(ssr.getSequenceName(), start, end);
				}
			else
				{
				return null;
				}
			}
		else
			{
			return new Interval(ssr.getSequenceName(), 0, ssr.getSequenceLength());
			}
    	}
    
    protected <T extends  javafx.scene.control.Control> T setTooltip(final T control, final String text)
    	{
    	if(text!=null && !text.trim().isEmpty())
    		{
    		control.setTooltip(new Tooltip(text));
    		}
    	return control;
    	}
    /** create a button opening the current item in Broad/IGV */
    protected Button createIgvButton() {
        final Button igvButton =setTooltip(new Button("IGV"),
        		"Open the current selected item in Broad/Integrative Genome Viewer"
        		);
        igvButton.setOnAction(AE->openInIgv());
		return igvButton;
        }
    
    static <T>  TableColumn<T, Integer> formatIntegerColumn(final TableColumn<T, Integer> tc) {
    	tc.setCellFactory(tv ->new TableCell<T,Integer>() {
    	private java.text.DecimalFormat niceIntFormat = new java.text.DecimalFormat("###,###");
	    @Override
	    protected void updateItem(final Integer pos, boolean empty) {
	        super.updateItem(pos, empty);
	        setText(null);
	        if(pos==null)
	        	{
	        	setText(null);
	            setGraphic(null);
	        	}
	        else
	        	{
	        	setText(niceIntFormat.format(pos));
	        	 setGraphic(null);
	        	}
	        	}
	    	});
    	return tc;
    	}
    
    /** returns  the underlying instance of NgsFile (BamFile, VcfFile...) */
    protected NgsFile<HEADERTYPE, ITEMTYPE> getNgsFile() {
		return ngsFile;
	}
    
    /** returns the pedigree associated to this file */
    public PedFile getPedigree() {
    	return PedFile.getEmptyInstance();
    }
    
    /** returns the currently selected item , used for contextual menus */
    protected abstract Optional<ITEMTYPE> getCurrentSelectedItem(); 
    
    /** build context menu for current selected locatable */
    protected List<MenuItem> buildItemsForContextMenu() {
    	final List<MenuItem> L=new ArrayList<>();
    	MenuItem menuItem;
    	
    	menuItem=new MenuItem("Copy Interval in Clipboard");
		menuItem.setOnAction(AE->{
			Optional<ITEMTYPE> sel=getCurrentSelectedItem();
			if(!sel.isPresent() || sel.get().getContig()==null) return;
			final Clipboard clipboard = Clipboard.getSystemClipboard();
		    final ClipboardContent content = new ClipboardContent();
		    content.putString(sel.get().getContig()+":"+sel.get().getStart()+"-"+sel.get().getEnd());
		    clipboard.setContent(content);
		});

		L.add(menuItem);
    	
    	for(final String build:new String[]{"hg38","hg19","hg18"})
    		{
    		menuItem=new MenuItem("Open in UCSC "+build+" ... ");
    		menuItem.setOnAction(AE->{
    			final Optional<ITEMTYPE> sel=getCurrentSelectedItem();
    			if(!sel.isPresent() || sel.get().getContig()==null) return;
    			NgsStage.this.owner.getHostServices().showDocument(
    				"http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position="+
    						JfxNgs.ContigToUCSC.apply(sel.get().getContig())+"%3A"+sel.get().getStart()+"-"+sel.get().getEnd()
    					);
    		});
    		L.add(menuItem);
    		}
    	for(final String build:new String[]{"grch37.Homo_sapiens","www.Homo_sapiens","www.Rattus_norvegicus"})
			{
    		int dot=build.indexOf('.');
    		final String host=build.substring(0,dot);
    		final String org=build.substring(dot+1);
    		menuItem=new MenuItem("Open in Ensembl "+org+(host.equals("www")?"":"["+host+"]")+" ... ");
    		menuItem.setOnAction(AE->{
			
    		final Optional<ITEMTYPE> sel=getCurrentSelectedItem();
			if(!sel.isPresent() || sel.get().getContig()==null) return;
			
			NgsStage.this.owner.getHostServices().showDocument(
				"http://"+host +".ensembl.org/"+ org+"/Location/View?r="+
						JfxNgs.ContigToEnseml.apply(sel.get().getContig())+"%3A"+sel.get().getStart()+"-"+sel.get().getEnd()
					);
				});
			L.add(menuItem);
			}
    	
	    	for(final String database : new String[]{"Exac","gnomAD"})
	    	{
	    	menuItem=new MenuItem("Open Region in "+database+" ... ");
			menuItem.setOnAction(AE->{
				final Optional<ITEMTYPE> sel=getCurrentSelectedItem();
				if(!sel.isPresent() || sel.get().getContig()==null) return;
				NgsStage.this.owner.getHostServices().showDocument(
					"http://"+database.toLowerCase()+".broadinstitute.org/region/"+
							JfxNgs.ContigToEnseml.apply(sel.get().getContig())+"-"+sel.get().getStart()+"-"+sel.get().getEnd()
						);
			});
			L.add(menuItem);
	    	}
    	
    	return L;
    	}
}
