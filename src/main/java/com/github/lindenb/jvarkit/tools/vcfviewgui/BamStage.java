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
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.CompiledScript;
import javax.script.ScriptException;

import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.BasesPerPositionChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.ChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.CigarOpPerPositionChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.GCPercentChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.MapqChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.QualityPerPositionChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.ReadChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.ReadLengthChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.ReadQualityChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.SamFlagsChartFactory;
import com.github.lindenb.jvarkit.tools.vcfviewgui.chart.VariantContextChartFactory;
import com.github.lindenb.jvarkit.util.Hershey;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMFlag;
import htsjdk.samtools.SAMProgramRecord;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMTagUtil;
import htsjdk.samtools.SAMTextHeaderCodec;
import htsjdk.samtools.SAMUtils;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SAMRecord.SAMTagAndValue;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.SequenceUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.Chart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;

@SuppressWarnings("unused")
public class BamStage extends NgsStage<SAMFileHeader,SAMRecord> {
    static final String SPINNER_VALUE_KEY="bam.spinner.value";
    static final int DEFAULT_BAM_RECORDS_COUNT= 1000;
    static final ExtensionFilter EXTENSION_FILTER=new ExtensionFilter("Bam Files", ".bam");
    private static final Logger LOG= Logger.getLogger("BamStage");
    /** shor-Read oriented chart-factories */
    private static final List<Supplier<ChartFactory<SAMRecord>>> READ_CHART_LIST=Arrays.asList(
    		()->new BasesPerPositionChartFactory(),
    		()->new QualityPerPositionChartFactory(),
    		()->new GCPercentChartFactory(),
    		()->new ReadLengthChartFactory(),
    		()->new SamFlagsChartFactory(),
    		()->new MapqChartFactory(),
    		()->new ReadQualityChartFactory(),
    		()->new CigarOpPerPositionChartFactory()
    		);
    
    /** Misc BAM tools that will be injected in the nashorn context */
    public static class BamTools
    	{
    	public String reverseComplement(final String sequenceData)
    		{
    		return SequenceUtil.reverseComplement(sequenceData);
    		}
    	
    	}
    
    /** specialized javascript filter for BAM file */
    private static class BamJavascripFilter
    	extends JavascriptFilter<SAMFileHeader,SAMRecord>
		{
		protected BamJavascripFilter(
				final SAMFileHeader header,
				final Optional<CompiledScript> compiledScript
				)
			{
			super(header,compiledScript);
			super.bindings.put("tools",new BamTools());
			}
		@Override public SAMRecord eval(final SAMRecord rec)
			{
			if(!super.compiledScript.isPresent()) return rec;
			super.bindings.put("record", rec);
			return super.accept()?rec:null;
			}
		}

    
    private class ReadQualityStage
		extends AbstractQualityStage
			{
    		private class ScanQualThread extends ScanThread
    			{
    			private final Predicate<SAMRecord> flagFilters;
        		ScanQualThread(
        				final ChartFactory<SAMRecord> factory,
        				final NgsFile<SAMFileHeader, SAMRecord> bamReader,
        				final Optional<CompiledScript> compiledScript,
        				final Predicate<SAMRecord> flagFilters
        				)
    				{
    				super(factory,bamReader,compiledScript,flagFilters);
    				this.flagFilters=flagFilters;
    				}
        		
    			@Override
    			public void run() {
    				CloseableIterator<SAMRecord> samIter=null;
    				try 
    					{
	    				BamJavascripFilter bamJavascripFilter=null;
	    				
    					samIter = super.ngsReader.iterator();
    					
    					if(this.compiledScript!=null)
    						{
    						bamJavascripFilter=new BamJavascripFilter(
    								ngsReader.getHeader(),
    								compiledScript);
    						}	
    					
    					while(!kill_flag && samIter.hasNext())
    						{
    						final SAMRecord rec=samIter.next();
    						
    						nItems++;
    						if(!flagFilters.test(rec)) continue;
    						if(bamJavascripFilter!=null)
    							{
    							if(bamJavascripFilter.eval(rec)==null) continue;
    							}
    						this.factory.visit(rec);
    						update();
    						}
    					samIter.close();
    					super.ngsReader.close();
    					
    					if(bamJavascripFilter!=null && bamJavascripFilter.encounteredException.isPresent())
    						{
    						this.encounteredException=bamJavascripFilter.encounteredException;
    						}
    					super.atEnd();
	    				}
    				catch(final Throwable err)
    					{
    					super.onError(err);
    					}
    				finally
    					{
    					CloserUtil.close(samIter);
    					CloserUtil.close(super.ngsReader);
    					}
    				}
    			
    			}
    		
	    	ReadQualityStage(
	    			final ChartFactory<SAMRecord> factory,
	    			final BamFile bamReader,
	    			final Optional<CompiledScript> compiledScript,
	    			final Predicate<SAMRecord> filters)
	    		{
	    		super(factory,bamReader,compiledScript,filters);
	    		}
	    	@Override
	    	protected NgsStage<SAMFileHeader, SAMRecord>.AbstractQualityStage.ScanThread createThread(
	    		ChartFactory<SAMRecord> factory, NgsFile<SAMFileHeader, SAMRecord> bamReader,
	    		Optional<CompiledScript> compiledScript, Predicate<SAMRecord> otherFilters) {
				return new ScanQualThread(factory,bamReader,compiledScript,otherFilters);
	    		}
	    	
			}
    
	/** describe the state of a SamFlag */
    private static class SamFlagRow
		{
    	private final SAMRecord record;
    	private final SAMFlag flag ;
    	SamFlagRow(final SAMRecord record,final SAMFlag flag)
    		{
    		this.record=record;
    		this.flag=flag;
    		}
		}
    
    /** describe the base of a read in the cigar context */
    private static class CigarAndBase
		{
    	private final String ref;
    	private final CigarOperator op;
    	private final Integer posInRead ;
    	private final Integer posInRef ;
    	private final Integer count;
    	private final Byte base ;
    	CigarAndBase(final String ref,final CigarOperator op,final Integer posInRead,final Integer posInRef,Integer count,Byte base)
    		{
    		this.ref=ref;
    		this.op=op;
    		this.posInRead = posInRead;
    		this.posInRef = posInRef;
    		this.base = base;
    		this.count=count;
    		}
		}
    /** describe 'SA' field in a READ */
    private static class SuplementaryAlign
    	{
    	private static final Pattern SEMICOLON_PAT = Pattern.compile("[;]");
   	 	private static final Pattern COMMA_PAT = Pattern.compile("[,]");    	 
    	final String contig;
    	final Integer pos;
    	final String strand;
    	final String cigar;
    	final Integer mapq;
    	final String nm;
    	private SuplementaryAlign(final String commaStrs[])
    		{
    		this.contig= commaStrs[0];
    		int  n=-1;
    		 try {
    			 n = Integer.parseInt(commaStrs[1]);
             } catch( final NumberFormatException err ) {
            	 n=-1;
             	}
    		this.pos=(n==-1?null:n);
    		this.strand=commaStrs[2];
    		this.cigar=commaStrs[3];
    		 try {
    			 n = Integer.parseInt(commaStrs[4]);
             } catch( final NumberFormatException err ) {
            	 n=-1;
             	}
    		 this.mapq=(n==-1?null:n);
    		 this.nm=commaStrs[5];
    		}
    	//TODO upgrade htsjdk and use SAMUtils
    	static List<SuplementaryAlign> fromSamRecord(final SAMRecord record) {
    		if(record==null)  return Collections.emptyList();
    		final Object saValue = record.getAttribute( SAMTagUtil.getSingleton().SA );
    		if( saValue == null || !(saValue instanceof String) ) return Collections.emptyList();
    		final String semiColonStrs[] = SEMICOLON_PAT.split((String)saValue);
    		final List<SuplementaryAlign> alignments = new ArrayList<>( semiColonStrs.length );
    		 for(int i=0; i< semiColonStrs.length;++i  ) {
    	            final String semiColonStr = semiColonStrs[i];
    	            /* ignore empty string */
    	            if( semiColonStr.isEmpty() ) continue;
    	            final String commaStrs[] = COMMA_PAT.split(semiColonStr);
    	            if( commaStrs.length != 6 ) continue;
    	            alignments.add(new SuplementaryAlign(commaStrs));
    		 	}
    		return alignments;
    		}
    	}
    
    private static class Pileup extends ContigPos
    	{
    	
    	final int count[]=new int[5];
    	final StringBuilder seq=new StringBuilder();
    	final StringBuilder qual=new StringBuilder();
    	final StringBuilder operators=new StringBuilder();
    	
    	Pileup(final String contig,final int position) {
			super(contig,position);
			Arrays.fill(count,0);
			}
    	void watch(char base,char q,CigarOperator op)
    		{
    		seq.append(base);
    		switch(Character.toUpperCase(base))
    			{
    			case 'A': count[0]++;break;
    			case 'T': count[1]++;break;
    			case 'G': count[2]++;break;
    			case 'C': count[3]++;break;
    			case '<':case '>': case '-':break;
    			default: count[4]++;break;
    			}
    		qual.append(q);
    		this.operators.append((char)CigarOperator.enumToCharacter(op));
    		}
    	int depth() { return count[0]+count[1]+count[2]+count[3];}
    	}
	
    private final TableView<SAMRecord> recordTable;
    private final TableView<SamFlagRow> flagsTable;
    private final TableView<SAMTagAndValue> metaDataTable;
    private final TableView<CigarAndBase> cigarTable;
    /** table of supplementary alignments */
    private final TableView<SuplementaryAlign> suplAlignTable;
    private final TableView<Pileup> pileupTable;
    private final Map<SAMFlag,CheckMenuItem> flag2filterInMenuItem=new HashMap<>();
    private final Map<SAMFlag,CheckMenuItem> flag2filterOutMenuItem=new HashMap<>();
    private final Canvas canvas = new Canvas(900, 600);
    private final ScrollBar canvasScrollV = new ScrollBar();
    private final CheckBox canvasShowReadName = new CheckBox("Show Read Name");
    
    BamStage(final JfxNgs owner,
    		final BamFile bamFile
    		) throws IOException
    	{
    	super(owner,bamFile);
               
        /** Build menu for SAM Flags */
        for(final SAMFlag flg:SAMFlag.values())
        	{
        	this.flag2filterInMenuItem.put(flg,new CheckMenuItem("Filter In "+flg.name()));
        	this.flag2filterOutMenuItem.put(flg,new CheckMenuItem("Filter Out "+flg.name()));
        	}
        
        
        final VBox vbox1 = new VBox();
        vbox1.getChildren().add(super.menuBar);
        
        FlowPane top1= new FlowPane(5,5);
        top1.setPadding(new Insets(10, 10, 10, 10));
        vbox1.getChildren().add(top1);
        top1.getChildren().add(new Label("GoTo:"));
        top1.getChildren().add(this.gotoField);
        final Button gotoButton=new Button("Go");
        gotoButton.setOnAction(new EventHandler<ActionEvent>()
			{
			@Override
			public void handle(ActionEvent event)
				{
				reloadData();
				}
			});
        
        int number_of_items_in_spinner;
        try {
        	number_of_items_in_spinner= owner.preferences.getInt(SPINNER_VALUE_KEY, DEFAULT_BAM_RECORDS_COUNT);
			if(number_of_items_in_spinner<0) number_of_items_in_spinner=0;
			if(number_of_items_in_spinner>100000) number_of_items_in_spinner=100000;
        	}
        catch(Exception err)
        	{
        	number_of_items_in_spinner = DEFAULT_BAM_RECORDS_COUNT;
        	}
        
        super.maxItemsLimitSpinner.setValueFactory(
        		new SpinnerValueFactory.IntegerSpinnerValueFactory(0,100000,
        				number_of_items_in_spinner
        				));
        				
        top1.getChildren().add(gotoButton);
        top1.getChildren().add(new Separator(Orientation.VERTICAL));
        top1.getChildren().add(new Label("Limit:"));
        top1.getChildren().add(super.maxItemsLimitSpinner);
        top1.getChildren().add(new Separator(Orientation.VERTICAL));
        List<CheckMenuItem> menuFlags=new ArrayList<>(flag2filterOutMenuItem.values());
        menuFlags.addAll(flag2filterInMenuItem.values());
        CheckMenuItem tmp[]=new CheckMenuItem[menuFlags.size()];
        menuFlags.toArray(tmp);
        top1.getChildren().add(new MenuBar(new Menu("Flags",null,tmp)));
        super.gotoField.setOnAction(AE->reloadData());
      
        top1.getChildren().add(createIgvButton());
       
        
        
        TabPane tabbedPane = new TabPane();
        tabbedPane.setPadding(new Insets(10, 10, 10, 10));
        Tab tab= new Tab("Reads");
        tab.setClosable(false);
        tabbedPane.getTabs().add(tab);
        
        this.recordTable = makeRecordTable();
        /** create columns */
        
        
        
        

        
        //ScrollPane scroll = new ScrollPane(this.recordTable);
        //scroll.setFitToHeight(true);
        //scroll.setFitToWidth(true);
        
        BorderPane borderPane1=new BorderPane(this.recordTable);
        borderPane1.setPadding(new Insets(5));
        SplitPane split1= new SplitPane();
        split1.setOrientation(Orientation.VERTICAL);
        split1.getItems().add(borderPane1);
        
       
        SplitPane split2= new SplitPane();
        split1.getItems().add(split2);
        split2.setOrientation(Orientation.HORIZONTAL);
        
        /* define SAM Flag table */
        this.flagsTable= createSamFlagTable();
       
        
        //scroll.setFitToHeight(true);
        split2.getItems().add(this.flagsTable); 
        
        /* define Meta Data table */
        this.metaDataTable = createMetaDataTable();
        split2.getItems().add(this.metaDataTable); 

        
        /* build the cigar table */
        this.cigarTable = createCigarTable();
        this.suplAlignTable = createSuplAlignTable();
        final javafx.scene.control.TabPane tabpane2=new TabPane();
        tabpane2.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabpane2.getTabs().add( new Tab("Cigar",  this.cigarTable ));
        tabpane2.getTabs().add( new Tab("SA",  this.suplAlignTable ));
        
        split2.getItems().add(tabpane2); 

        
        /* when a read is selected update the flagsTable and metaDataTable */
        this.recordTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection==null)
            	{
        		seqDictionaryCanvas.setSelectInterval(null);
            	flagsTable.getItems().clear();
            	metaDataTable.getItems().clear();
            	cigarTable.getItems().clear();
            	suplAlignTable.getItems().clear();
            	}
            else
            	{
        		super.seqDictionaryCanvas.setSelectInterval(new Interval(newSelection.getContig(), newSelection.getStart(), newSelection.getEnd()));

            	final List<SamFlagRow> L=new ArrayList<>();
            	for(final SAMFlag flag: SAMFlag.values())
            		{
            		L.add(new SamFlagRow(newSelection,flag));
            		}
            	flagsTable.getItems().setAll(L);
            	
            	/* update meta data */
            	metaDataTable.getItems().setAll(newSelection.getAttributes());
            	/* update suppl align table */
            	suplAlignTable.getItems().setAll(SuplementaryAlign.fromSamRecord(newSelection));
            	
            	if(!newSelection.getReadUnmappedFlag() && newSelection.getCigar()!=null)
            		{
            		final List<CigarAndBase> M = new ArrayList<>();
            		int posInRead=0;
            		int posInRef=newSelection.getUnclippedStart();
            		final byte readString[] = newSelection.getReadBases();
            		for(CigarElement ce: newSelection.getCigar())
            			{
            			final CigarOperator op= ce.getOperator();
            			
            			switch(op)
            				{
            				case H: case D: case N:
            					{
            					M.add(new CigarAndBase(newSelection.getReferenceName(), op, null, posInRef,ce.getLength(),null));
            					posInRef+=ce.getLength();
            					break;
            					}
            				case P:
            					{
            					M.add(new CigarAndBase(newSelection.getReferenceName(), op, null, null,ce.getLength(),null));
            					break;
            					}
            				case I: 
            					{
            					for(int i=0;i< ce.getLength();++i)
    	                			{
                					M.add(new CigarAndBase(null, op, posInRead, null,1,
                							readString==null || posInRead>=readString.length?null:readString[posInRead]
                							));
                					posInRead++;
    	                			}
            					break;
            					}
            				case M: case X: case EQ: case S: 
            					{
            					for(int i=0;i< ce.getLength();++i)
    	                			{
                					M.add(new CigarAndBase(newSelection.getReferenceName(), op, posInRead, posInRef,1,
                							readString==null || posInRead>=readString.length?null:readString[posInRead]
                							));
                					posInRead++;
                					posInRef++;
    	                			}
            					break;
            					}
            				}
                		}
            			
            		cigarTable.getItems().setAll(M);
            		}
            	else
            		{
            		cigarTable.getItems().clear();
            		}
            	}
        });

        split2.setDividerPositions(0.1f, 0.6f, 0.9f);
        tab.setContent(split1);
       
        this.pileupTable = createPileupTable();
        tab=new Tab("Pileup",this.pileupTable);
        tab.setClosable(false);
        tabbedPane.getTabs().add(tab);

        
        tab=new Tab("Header");
        tab.setClosable(false);
        tabbedPane.getTabs().add(tab);
        final StringWriter headerTextBuffer = new StringWriter();
        new SAMTextHeaderCodec().encode(headerTextBuffer, bamFile.getHeader());
        final TextArea textAreaHeader =new TextArea(headerTextBuffer.toString());
        textAreaHeader.setEditable(false);
        
        ScrollPane scroll = new ScrollPane(textAreaHeader);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        
        tabbedPane.getTabs().add(buildDictTab(  bamFile.getHeader().getSequenceDictionary()));
        tabbedPane.getTabs().add(createReadGroupPane(bamFile.getHeader()));
        tabbedPane.getTabs().add(createProgramRecordPane(bamFile.getHeader()));
        tabbedPane.getTabs().add(buildJavascriptPane());
        
        /* CANVAS STUFFF */
        final BorderPane canvasPane = new BorderPane(this.canvas);
        this.canvasScrollV.setOrientation(Orientation.VERTICAL);
        canvasPane.setRight(this.canvasScrollV);
        final FlowPane canvasTop=new FlowPane(this.canvasShowReadName);
        canvasPane.setTop(canvasTop);
        
        this.canvasShowReadName.setOnAction(E->repaintCanvas());
        this.canvasScrollV.valueProperty().addListener(E->repaintCanvas());
        
        tab=new Tab("Canvas",canvasPane);
        tab.setClosable(false);
        tabbedPane.getTabs().add(tab);
        /* END CANVAS STUFF */
        
        vbox1.getChildren().add(super.seqDictionaryCanvas);
        vbox1.getChildren().add(tabbedPane);
        
        
        final FlowPane bottom=new FlowPane(super.messageLabel);
        vbox1.getChildren().add(bottom);
        
        final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        this.setScene(new Scene(vbox1,
        		primaryScreenBounds.getWidth()-200,
        		primaryScreenBounds.getHeight()-200)
        		);
        		

        super.fileMenu.getItems().addAll(
        		menuForSavingTable("SamRecord",this.recordTable),
        		menuForSavingTable("MetaData",this.metaDataTable),
        		menuForSavingTable("Flags",this.flagsTable),
        		menuForSavingTable("Pileup",this.pileupTable),
        		menuForSavingTable("Cigar",this.cigarTable)
        		);
        
        /* fill stats menu */
        final Supplier<List<SAMRecord>> variantsProvider=()->this.recordTable.getItems();
        
        for(final Supplier<ChartFactory<SAMRecord>> supplier: READ_CHART_LIST)
	        {
        	final ChartFactory<SAMRecord> factory = supplier.get();
        	final MenuItem menuItem=new MenuItem("Local "+factory.getName());
        	menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					doMenuShowLocalStats(factory, variantsProvider);
				}
			});
        	statsMenu.getItems().add(menuItem);
	        }
        super.statsMenu.getItems().add(new SeparatorMenuItem());
        for(final Supplier<ChartFactory<SAMRecord>> supplier: READ_CHART_LIST)
	        {
	    	final ChartFactory<SAMRecord> factory = supplier.get();
	    	final MenuItem menuItem=new MenuItem("Whole"+factory.getName());
	    	menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					doMenuShowWholeStats(factory);
				}
			});
	    	super.statsMenu.getItems().add(menuItem);
	        }
        
       

        
        this.addEventHandler(
    			WindowEvent.WINDOW_CLOSE_REQUEST ,new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(final WindowEvent event) {
                    owner.preferences.putInt(SPINNER_VALUE_KEY,maxItemsLimitSpinner.getValue().intValue());
                    }
                });
        
        
    	}

    private TableView<SAMRecord> makeRecordTable() {
    	final Font font1=new Font("Courier", 9);

    	final TableView<SAMRecord>  table = new TableView<SAMRecord>();
    	table.getColumns().add(makeColumn("Read-Name",REC->REC.getReadName()));
    	table.getColumns().add(makeColumn("Flag",REC->REC.getFlags()));
    	table.getColumns().add(makeColumn("Ref",REC->REC.getReferenceName()));
    	table.getColumns().add(formatIntegerColumn(makeColumn("UStart",REC->{
    		if(REC.getReadUnmappedFlag() || REC.getCigar()==null ) return null;
    		final int upos = REC.getUnclippedStart();
    		if(upos ==  REC.getAlignmentStart()) return null;
    		return upos;
    		})));
    	table.getColumns().add(formatIntegerColumn(makeColumn("Start",REC->{
			final int pos= REC.getAlignmentStart();
			if( pos == SAMRecord.NO_ALIGNMENT_START) return null;
			return pos;
			})));
    	table.getColumns().add(formatIntegerColumn(makeColumn("End",REC->{
	    	final int pos= REC.getAlignmentEnd();
			if( pos == SAMRecord.NO_ALIGNMENT_START) return null;
			return pos;
			})));
    	table.getColumns().add(formatIntegerColumn(makeColumn("UEnd",REC->{
			if(REC.getReadUnmappedFlag()  || REC.getCigar()==null) return null;
			final int upos = REC.getUnclippedEnd();
			if(upos ==  REC.getAlignmentEnd()) return null;
			return upos;
			})));
    	table.getColumns().add(makeColumn("MAPQ",REC->REC.getMappingQuality()));
    	
    	TableColumn<SAMRecord, Cigar> tc0 = makeColumn("CIGAR",REC->REC.getCigar());
    	
    	tc0.setCellFactory(tv -> new TableCell<SAMRecord, Cigar>() { 
    		final TextFlow textFlow = new TextFlow();
    		
    	    @Override
    	    protected void updateItem(final Cigar cigar, boolean empty) {
    	        super.updateItem(cigar, empty);
    	        setText(null);
    	        if(cigar==null || cigar.isEmpty())
    	        	{
    	        	//setText(null);
    	            setGraphic(null);
    	        	return;
    	        	}
    	        final List<Text> L=new ArrayList<>(cigar.numCigarElements());
    	        for(final CigarElement ce: cigar.getCigarElements()) {
    	        	final Text txt=new Text(String.valueOf(ce.getLength())+(char)CigarOperator.enumToCharacter(ce.getOperator()));
    	        	txt.setFont(font1);
    	        	switch(ce.getOperator())
    	        		{
    	        		case H:case S: txt.setStroke(Color.ORANGE);break;
    	        		case X:case N: case D: case I: txt.setStroke(Color.RED);break;
    	        		case M: txt.setStroke(Color.BLUE);break;
    	        		case EQ: txt.setStroke(Color.GREEN);break;
    	        		case P: txt.setStroke(Color.GRAY);break;
    	        		default: txt.setStroke(Color.BLACK);break;
    	        		}
    	        	L.add(txt);
    	        	}
    	        this.textFlow.setLineSpacing(0.1);
    	        this.textFlow.setMaxHeight(10);
    	        this.textFlow.setPrefHeight(10);
    	        this.textFlow.getChildren().setAll(L);
    	        this.setGraphic(textFlow);
    	    }
    	});
    	
    	table.getColumns().add(tc0);
    	
    	table.getColumns().add(formatIntegerColumn(makeColumn("LEN",REC-> !REC.getReadPairedFlag() || REC.getMateUnmappedFlag()?null:REC.getInferredInsertSize())));
    	table.getColumns().add(makeColumn("Mate-Ref",REC->!REC.getReadPairedFlag() || REC.getMateUnmappedFlag()?null:REC.getMateReferenceName()));
    	table.getColumns().add(formatIntegerColumn(makeColumn("Mate-Pos",REC-> !REC.getReadPairedFlag() || REC.getMateUnmappedFlag()?null: REC.getMateAlignmentStart())));
    	
    	
    	TableColumn<SAMRecord, String> tc = makeColumn("SEQ",REC->REC.getReadString());
    	
    	// http://stackoverflow.com/questions/42187987/
    	tc.setCellFactory(tv -> new TableCell<SAMRecord, String>() { 
    		final TextFlow textFlow = new TextFlow();
    		
    	    @Override
    	    protected void updateItem(final String item, boolean empty) {
    	        super.updateItem(item, empty);
    	        setText(null);
    	        if(item==null)
    	        	{
    	        	//setText(null);
    	            setGraphic(null);
    	        	return;
    	        	}
    	        final List<Text> L=new ArrayList<>(item.length());
    	        for(int i=0;i< item.length();++i) {
    	        	final Text txt=new Text(String.valueOf(item.charAt(i)));
    	        	txt.setFont(font1);
    	        	txt.setStroke(JfxNgs.BASE2COLOR.apply(item.charAt(i)));
    	        	L.add(txt);
    	        	}
    	        this.textFlow.setLineSpacing(0.1);
    	        this.textFlow.setMaxHeight(10);
    	        this.textFlow.setPrefHeight(10);
    	        this.textFlow.getChildren().setAll(L);
    	        this.setGraphic(textFlow);
    	    }
    	});
	    table.getColumns().add(tc);
	    
    	tc=makeColumn("QUAL",REC->REC.getBaseQualityString());
    	tc.setCellFactory(tv -> new TableCell<SAMRecord, String>() { 
    		final TextFlow textFlow = new TextFlow();
    	    @Override
    	    protected void updateItem(final String item, boolean empty) {
    	        super.updateItem(item, empty);
    	        setText(null);
    	        if(item==null)
    	        	{
    	        	//setText(null);
    	            setGraphic(null);
    	        	return;
    	        	}
    	        final List<Text> L=new ArrayList<>(item.length());
    	        for(int i=0;i< item.length();++i) {
    	        	char c=item.charAt(i);
    	        	int ch=(int)c;
    	        	double qual=93.0;
    	        	if (!(ch < 33 || ch > 126))
    	        		{
    	        		qual=SAMUtils.fastqToPhred(c);
    	        		}
    	        	final Text txt=new Text(String.valueOf(c));
    	        	txt.setFont(font1);
    	        	txt.setStroke(Color.gray(qual/93.0));
    	        	L.add(txt);
    	        	}
    	        this.textFlow.setLineSpacing(0.1);
    	        this.textFlow.setMaxHeight(10);
    	        this.textFlow.setPrefHeight(10);
    	        this.textFlow.getChildren().setAll(L);
    	        this.setGraphic(textFlow);
    	    }
    	});

    	
	    table.getColumns().add(tc);
	    
    	final short SA = SAMTagUtil.getSingleton().makeBinaryTag("SA");
    	table.getColumns().add(makeColumn("SA",REC->REC.getAttribute(SA)==null?null:"*"));
    	table.getColumns().add(makeColumn("NM",REC->REC.getIntegerAttribute("NM")));

	    
	    table.setPlaceholder(new Label("No Read."));
	    
	    
        final ContextMenu ctxMenu=new ContextMenu();
        ctxMenu.getItems().addAll(super.buildItemsForContextMenu());
        
        table.setContextMenu(ctxMenu);
	    
	    return table;
	    }
    
    
    /** get a stream of read we can display on canvas */
    private Stream<SAMRecord> getDisplayableSamRecordStream()
    	{
    	return this.recordTable.getItems().
    			stream().
    			filter(R->!R.getReadUnmappedFlag() && R.getCigar()!=null)
    			;
    	}
    
    
    /** repaint the canvas area */
    private void repaintCanvas()
    	{
    	final boolean showReadName = this.canvasShowReadName.isSelected();
    	final int baseSize=15;
    	final double canvaswidth= this.canvas.getWidth();
    	final double canvasheight= this.canvas.getHeight();
    	final GraphicsContext gc=this.canvas.getGraphicsContext2D();
    	gc.setFill(Color.WHITE);
    	gc.fillRect(0, 0, canvaswidth, canvasheight);
    	double y=baseSize*2;
    	final List<SAMRecord> records=getDisplayableSamRecordStream().collect(Collectors.toList());
    	if(records.isEmpty()) return;
 
    	final int recordStart=(int)this.canvasScrollV.getValue();
    	if(recordStart>=records.size()) return;
    	int recordIndex=recordStart;
    	final int chromStart=records.get(recordStart).getUnclippedStart();
    	final int chromLen=(int)(canvaswidth/baseSize);
    	if(chromLen==0) return;
    	
    	
    	Function<Integer,Double> position2pixel=new Function<Integer, Double>() {
			@Override
			public Double apply(Integer pos) {
				return ((pos-(double)chromStart)/(double)chromLen)*canvaswidth;
			}
		};
		
    	

		
		final Hershey hershey=new Hershey();
		
		hershey.paint(gc,records.get(recordStart).getContig(),1,0,baseSize*records.get(recordStart).getContig().length(),baseSize-2);
		
		for(int x=chromStart;x<chromStart+chromLen;++x)
			{
			double px=position2pixel.apply(x);
			gc.setStroke(x%10==0?Color.BLACK:Color.GRAY);
			gc.setLineWidth(x%10==0?5:0.5);
			gc.strokeLine(px, baseSize, px, canvasheight);
			if(x%10==0)
				{
				String s=String.valueOf(x);
				gc.setLineWidth(1.0);
				hershey.paint(gc,s,px,baseSize,baseSize*s.length(),baseSize-1);
				}
			}
		gc.setLineWidth(1);
    	while(y<canvasheight && recordIndex < records.size())
    		{
    		final SAMRecord rec=records.get(recordIndex);
    		if(!rec.getReferenceName().equals(records.get(recordStart).getReferenceName())) {
    			++recordIndex;
    			continue;
    			}
    		int baseIndex=0;
    		int refIndex=rec.getUnclippedStart();
    		final byte bases[]=rec.getReadBases();
    		
    		final Function<Integer,String> getBaseAt = new Function<Integer, String>() {
				@Override
				public String apply(Integer readPos) {
					char c;
					if(showReadName)
						{
						if(rec.getReadNameLength()<=readPos) return "";
						c= rec.getReadName().charAt(readPos);
						}
					else if(bases==null || bases.length<=readPos)
						{
						return "";
						}
					else
						{
						c=(char)bases[readPos];
						}	
					c=(rec.getReadNegativeStrandFlag()?
							Character.toLowerCase(c):
							Character.toUpperCase(c)
							);
					return String.valueOf(c);
				}
			};
			
    		final Function<Integer,Color> getColorAt = new Function<Integer, Color>() {
				public Color apply(final Integer readPos) {
					if(bases==null || bases.length<=readPos)
						{
						return Color.BLACK;
						}
					return JfxNgs.BASE2COLOR.apply((char)bases[readPos]);
				}
			};

			
			
			gc.setLineWidth(1.0);
    		//arrow end
			{
			double endpos=position2pixel.apply(rec.getReadNegativeStrandFlag()?rec.getUnclippedStart():rec.getUnclippedEnd()+1);
			double radius=baseSize/4.0;
			gc.setFill(Color.BLACK);
			gc.fillOval(
					endpos- radius,
    				y+baseSize/2.0 - radius,
    				radius*2 ,
    				radius*2
    				);
			}

    		
    		final Set<Integer> referenceEvents=new HashSet<>();
    		for(CigarElement ce:rec.getCigar()) {
    			switch(ce.getOperator())
    				{
    				case P: break;
    				case I: 
    					{
    					baseIndex+=ce.getLength();
    					referenceEvents.add(refIndex);
    					break;
    					}
    				case D: case N:
    					{
    					gc.setFill(Color.RED);
    					for(int x=0;x< ce.getLength();++x)
    						{
    						gc.fillRect(position2pixel.apply(refIndex),y,baseSize,baseSize-1);
    						refIndex++;
    						}
    					break;
    					}
    				case H:
    					{
						gc.setFill(Color.YELLOW);
    					for(int x=0;x< ce.getLength();++x)
    						{
    						gc.fillRect(position2pixel.apply(refIndex),y,baseSize,baseSize-1);
    						refIndex++;
    						}
    					break;
    					}
    				case S:
    					{
    					for(int x=0;x< ce.getLength();++x)
    						{
    						gc.setFill(Color.YELLOW);
    						gc.fillRect(position2pixel.apply(refIndex),y,baseSize,baseSize-1);
    						gc.setStroke(getColorAt.apply(baseIndex));
    						hershey.paint(gc,getBaseAt.apply(baseIndex), position2pixel.apply(refIndex),y,baseSize-1,baseSize-2);
    						refIndex++;
    						baseIndex++;
    						}
    					break;
    					}
    				case EQ:case X:case M:
						{
    					for(int x=0;x< ce.getLength();++x)
    						{
    						gc.setFill(ce.getOperator()==CigarOperator.X?Color.RED:Color.LIGHTGRAY);
    						gc.fillRect(position2pixel.apply(refIndex),y,baseSize,baseSize-1);
    						gc.setStroke(getColorAt.apply(baseIndex));
    						hershey.paint(gc,getBaseAt.apply(baseIndex), position2pixel.apply(refIndex),y,baseSize-1,baseSize-2);
    						refIndex++;
    						baseIndex++;
    						}
    					break;
    					}
    				
    				default:break;
    				}
    			if(refIndex> chromStart+chromLen) break;
    			}
    		
    		
    		gc.setStroke(Color.BLACK);
    		gc.strokeRect(
    				position2pixel.apply(rec.getUnclippedStart()),
    				y,
    				position2pixel.apply(rec.getUnclippedEnd()+1)-position2pixel.apply(rec.getUnclippedStart()) ,
    				baseSize-1
    				);
    		
    		for(Integer pos:referenceEvents)
    			{
    			double x=position2pixel.apply(pos);
    			gc.setStroke(Color.RED);
    			gc.setLineWidth(0.5);
    			gc.strokeLine(x, y, x, y+baseSize);
    			}
    		
    		recordIndex++;
    		y+=baseSize;
    		}
    	gc.setStroke(Color.BLACK);
		gc.rect(0,0,canvaswidth-1,canvasheight-1);
    	}
    
    private TableView<SamFlagRow> createSamFlagTable()
        {
        final TableView<SamFlagRow> table=new TableView<>();
    	table.getColumns().add(makeColumn("Flag", O->O.flag.name()));
    	table.getColumns().add(makeColumn("Status",param-> param.flag.isSet(param.record.getFlags())?"\u2611":"\u2610"));
    	table.setPlaceholder(new Label("No SAM flag."));
        return table;
        }        
    
    private TableView<SAMTagAndValue> createMetaDataTable()
        {
        final TableView<SAMTagAndValue> table=new TableView<>();
    	table.getColumns().add(makeColumn("Key", O->O.tag));
    	table.getColumns().add(makeColumn("Value", O->O.value));
    	table.setPlaceholder(new Label("No Meta-data."));
        return table;
        }
    
    
    private TableView<Pileup> createPileupTable()
    	{
    	final TableView<Pileup> table=new TableView<>();
    	table.getColumns().add(makeColumn("REF", O->O.contig));
    	table.getColumns().add(formatIntegerColumn(makeColumn("POS", O->O.position)));
    	table.getColumns().add(makeColumn("Depth", O->O.depth()));
    	table.getColumns().add(makeColumn("A", O->O.count[0]));
    	table.getColumns().add(makeColumn("T", O->O.count[1]));
    	table.getColumns().add(makeColumn("G", O->O.count[2]));
    	table.getColumns().add(makeColumn("C", O->O.count[3]));
    	table.getColumns().add(makeColumn("N", O->O.count[4]));
    	table.getColumns().add(makeColumn("Bases", O->O.seq.toString()));
    	table.getColumns().add(makeColumn("Qual", O->O.qual.toString()));
    	table.getColumns().add(makeColumn("Operators", O->O.operators.toString()));
    	table.setPlaceholder(new Label("No Pileup data."));
    	return table;
    	}
    /** supplementary align table ... */
    private TableView<SuplementaryAlign> createSuplAlignTable()
    {
    	final TableView<SuplementaryAlign> table=new TableView<>();
    	table.getColumns().add(makeColumn("REF", O->O.contig));
    	table.getColumns().add(formatIntegerColumn(makeColumn("POS", O->O.pos)));
    	table.getColumns().add(makeColumn("Strand", O->O.strand));
    	table.getColumns().add(makeColumn("NM", O->O.nm));
    	table.getColumns().add(makeColumn("MAPQ", O->O.mapq));
    	table.getColumns().add(makeColumn("Cigar", O->O.cigar));
    	table.setPlaceholder(new Label("No Read or no \"SA\" tag."));
        return table;
    }
    
    /** create the Table showing the bases in a CIGAR string */
    private TableView<CigarAndBase> createCigarTable() 
    	{
    	final TableView<CigarAndBase> table=new TableView<>();
    	table.getColumns().add(makeColumn("REF", O->O.ref));
    	table.getColumns().add(formatIntegerColumn(makeColumn("Read-Pos", O->O.posInRead)));
    	table.getColumns().add(formatIntegerColumn(makeColumn("Ref-Pos", O->O.posInRef)));
    	table.getColumns().add(makeColumn("OP",new Function<CigarAndBase,String>() {
    		@Override
    		public String apply(final CigarAndBase param) {
    			return param.op==null?null:param.op.name();
    		}
			} ));
    	table.getColumns().add(formatIntegerColumn(makeColumn("Len", O->O.count)));
    	table.getColumns().add(makeColumn("Read-Bases",new Function<CigarAndBase,String>() {
    		@Override
    		public String apply(final CigarAndBase param) {
    			return param.base==null?null:String.valueOf((char)param.base.intValue());
    		}
			} ));
    	
    	table.setPlaceholder(new Label("No Cigar Data."));
        return table;
    	}
    
    private Tab buildJavascriptPane()
		{
		final ScrollPane scroll=new ScrollPane(super.javascriptArea);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		final BorderPane pane=new BorderPane(scroll);
		pane.setPadding(new Insets(10));
		
		final FlowPane top=new FlowPane();
		top.getChildren().addAll(super.makeJavascriptButtons());
		pane.setTop(top);
		
		final Label helpLabel=new Label("The script injects:\n"+
				"* header ( htsjdk.samtools.SAMFileHeader )\n"+
				"* record ( htsjdk.samtools.SAMRecord )\n"+
				"The script should return a boolean: true (accept record) or false (reject record)"
				);
		helpLabel.setWrapText(true);
		pane.setBottom(helpLabel);
		
		final Tab tab=new Tab(JAVASCRIPT_TAB_KEY,pane);
		tab.setClosable(false);
		return tab;
		}
    
    private Tab createReadGroupPane(final SAMFileHeader header)
    	{
    	final TableView<SAMReadGroupRecord> table=new TableView<>(header==null?
    			FXCollections.observableArrayList():
    			FXCollections.observableArrayList(header.getReadGroups())
    			);
    	table.getColumns().add(makeColumn("ID", G->G.getId()));
    	table.getColumns().add(makeColumn("Sample", G->G.getSample()));
    	table.getColumns().add(makeColumn("Center", G->G.getSequencingCenter()));
    	table.getColumns().add(makeColumn("Platform", G->G.getPlatform()));
    	table.getColumns().add(makeColumn("PlatformModel", G->G.getPlatformModel()));
    	table.getColumns().add(makeColumn("PlatformUnit", G->G.getPlatformUnit()));
    	table.getColumns().add(makeColumn("MedianInsertSize", G->G.getPredictedMedianInsertSize()));
    	table.getColumns().add(makeColumn("Desc", G->G.getDescription()));
    	table.getColumns().add(makeColumn("PU", G->G.getPlatformUnit()));
    	table.getColumns().add(makeColumn("Lib", G->G.getLibrary()));
    	table.getColumns().add(makeColumn("Run-Date", G->G.getRunDate()));
    	
    	final Tab tab=new Tab("ReadGroups", table);
    	tab.setClosable(false);
    	
    	table.setPlaceholder(new Label("No BAM read-greoup."));
    	return tab;
    	}
    
    private Tab createProgramRecordPane(final SAMFileHeader header)
    	{
    	final TableView<SAMProgramRecord> table=new TableView<>(header==null?
    			FXCollections.observableArrayList():
    			FXCollections.observableArrayList(header.getProgramRecords())
    			);
    	table.getColumns().add(makeColumn("ID", G->G.getId()));
    	table.getColumns().add(makeColumn("PG-ID", G->G.getProgramGroupId()));
    	table.getColumns().add(makeColumn("Prev-PG-ID", G->G.getPreviousProgramGroupId()));
    	table.getColumns().add(makeColumn("Version", G->G.getProgramVersion()));
    	table.getColumns().add(makeColumn("Command", G->G.getCommandLine()));
        
    	final Tab tab=new Tab("PG", table);
    	tab.setClosable(false);
    	table.setPlaceholder(new Label("No Program-Group."));
    	return tab;
    	}
    
    /** build a Predicate for filtering on SAM FLAG using the checkboxes */
    private Predicate<SAMRecord> makeFlagPredicate()
    	{
    	java.util.function.Predicate<SAMRecord> recFilter= x -> true;
    	for(final SAMFlag flag: this.flag2filterInMenuItem.keySet())
			{
			CheckMenuItem cbox = this.flag2filterInMenuItem.get(flag);
			if(!cbox.isSelected()) continue;
			recFilter=recFilter.and(R-> flag.isSet(R.getFlags()));
			}
		for(final SAMFlag flag: this.flag2filterOutMenuItem.keySet())
			{
			CheckMenuItem cbox = this.flag2filterOutMenuItem.get(flag);
			if(!cbox.isSelected()) continue;
			recFilter=recFilter.and(R-> !flag.isSet(R.getFlags()));
			}
		return recFilter;
    	}
    
    @Override
    void reloadData() {
    	final int max_items= super.maxItemsLimitSpinner.getValue();
    	final List<SAMRecord> L= new ArrayList<SAMRecord>(max_items);
    	final String location = this.gotoField.getText().trim();
    	final CloseableIterator<SAMRecord> iter;
    	final java.util.function.Predicate<SAMRecord> recFilter=makeFlagPredicate();
    	
    	try {
    	if(location.isEmpty())
    		{
    		iter = this.getBamFile().iterator();
    		}
    	else if(location.equalsIgnoreCase("unmapped"))
    		{
    		iter = this.getBamFile().queryUnmapped();
    		}
    	else
    		{
    		final Interval interval=parseInterval(location);
    		if(interval==null)
    			{
    			iter=null;
    			}
    		else
    			{
    			iter= this.getBamFile().iterator(interval.getContig(),interval.getStart(), interval.getEnd());
    			}
    		}
    	} catch(final IOException err) {
    		err.printStackTrace();
    		JfxNgs.showExceptionDialog(this, err);
    		return;
    		}
    	Optional<BamJavascripFilter> bamjsfilter=Optional.empty();
    	if(this.owner.javascriptCompiler.isPresent() &&
    			!this.javascriptArea.getText().trim().isEmpty())
    		{
    		try
    			{
    			bamjsfilter=Optional.of(new BamJavascripFilter(
    					this.getBamFile().getHeader(),
    					Optional.of(this.owner.javascriptCompiler.get().compile(this.javascriptArea.getText()))
    					));
    			}
    		catch(final Exception err)
    			{
    			LOG.warning(err.getMessage());
    			updateStatusBar(AlertType.ERROR, err);
    			bamjsfilter=Optional.empty();
    			}
    		}
    	final Map<ContigPos,Pileup> pos2pileup=new TreeMap<>();
    	final Function<ContigPos,Pileup> getpileup=new  Function<ContigPos, Pileup>() {
			@Override
			public Pileup apply(ContigPos t) {
				Pileup p =pos2pileup.get(t);
				if(p==null) { p=new Pileup(t.contig,t.position);pos2pileup.put(t,p);}
				return p;
			}
		};
		
    	int count_items=0;
    	while(iter!=null && iter.hasNext() && count_items<max_items)
    		{
    		final SAMRecord rec = iter.next();
    		++count_items;
    		if(bamjsfilter.isPresent())
    			{
    			if(bamjsfilter.get().eval(rec)==null) continue;
    			}    		
    		if(!recFilter.test(rec)) continue;
    		L.add(rec);
    		
    		/* FILL pileup */
    		if(!rec.getReadUnmappedFlag() && rec.getCigar()!=null)
    			{
    			int refpos=rec.getUnclippedStart();
    			int readpos=0;
    			final byte bases[]=rec.getReadBases();
    			final byte quals[]=rec.getOriginalBaseQualities();
    			
    			/** function getting the ith base */
        		final Function<Integer,Character> getBaseAt = new Function<Integer, Character>() {
					@Override
					public Character apply(Integer readPos) {
						char c;
						if(bases==null || bases.length<=readPos)
							{
							return '?';
							}
						else
							{
							c=(char)bases[readPos];
							}	
						c=(rec.getReadNegativeStrandFlag()?
								Character.toLowerCase(c):
								Character.toUpperCase(c)
								);
						return c;
					}
				};
    			
				/** function getting the ith base quality */
        		final Function<Integer,Character> getQualAt = new Function<Integer, Character>() {
					@Override
					public Character apply(Integer readPos) {
						char c;
						if(quals==null || quals.length<=readPos)
							{
							return '#';
							}
						else
							{
							c=(char)quals[readPos];
							}	
						return c;
					}
				};

				
    			for(final CigarElement ce:rec.getCigar())
    				{
    				switch(ce.getOperator())
    					{
    					case P:break;
    					case N:case D:
    						{
    						refpos+=ce.getLength();
    						break;
    						}
    					case H:
    						{
							for(int i=0;i< ce.getLength();++i) {
    							final Pileup p = getpileup.apply(new ContigPos(rec.getContig(),refpos));
    							p.watch('-','#',ce.getOperator());
    							++refpos;
    							}
    						break;
    						}
    					case I:
    						{
    						for(int i=0;i< ce.getLength();++i) {
    						final Pileup p = getpileup.apply(new ContigPos(rec.getContig(),refpos));
    							p.watch('<',getQualAt.apply(readpos),ce.getOperator());
    							readpos++;
        						}
							break;
    						}
    					case S:
    						for(int i=0;i< ce.getLength();++i) {
    							final Pileup p = getpileup.apply(new ContigPos(rec.getContig(),refpos));
    							p.watch('-',getQualAt.apply(readpos),ce.getOperator());
    							++readpos;
    							++refpos;
    							}
    						break;
    					case EQ: case X: case M:
    						for(int i=0;i< ce.getLength();++i) {
    							final Pileup p = getpileup.apply(new ContigPos(rec.getContig(),refpos));
    							p.watch(getBaseAt.apply(readpos),getQualAt.apply(readpos),ce.getOperator());
    							++readpos;
    							++refpos;
    							}
    						break;
    					}
    				}
    			}
    		}
    	if(iter!=null) iter.close();
    	this.recordTable.getItems().setAll(L);
    	this.pileupTable.getItems().setAll(pos2pileup.values());
    	
    	
    	final int countDisplayable = (int)getDisplayableSamRecordStream().count();
    	this.canvasScrollV.setMin(0);
    	this.canvasScrollV.setMax(countDisplayable);
    	this.canvasScrollV.setValue(0);
    	
    	if(!this.recordTable.getItems().isEmpty())
    		{
    		super.seqDictionaryCanvas.setItemsInterval(
    				new ContigPos(this.recordTable.getItems().get(0).getContig(), this.recordTable.getItems().get(0).getStart()),
    				new ContigPos(this.recordTable.getItems().get(this.recordTable.getItems().size()-1).getContig(), this.recordTable.getItems().get(this.recordTable.getItems().size()-1).getEnd())
    				);
    		}
    	else
    		{
    		super.seqDictionaryCanvas.setItemsInterval(null,null);
    		}
    	
    	repaintCanvas();
    	}
    @Override
	void openInIgv() {
    	final SAMRecord ctx=this.recordTable.getSelectionModel().getSelectedItem();
    	if(ctx==null) {
    		updateStatusBar(AlertType.WARNING,"no Read selected");
    		return;
    		}
    	if(ctx.getReadUnmappedFlag())
    		{
    		updateStatusBar(AlertType.WARNING,"read is not mapped");
    		return;
    		}
    	openInIgv(ctx);
    	}
    
  


	@Override
	protected void doMenuShowWholeStats(final ChartFactory<?> factory) {
    	Optional<CompiledScript> compiledScript=Optional.empty();
    	if(this.owner.javascriptCompiler.isPresent() &&
    		!this.javascriptArea.getText().trim().isEmpty())
    		{
    		try
    			{
    			compiledScript =Optional.of(this.owner.javascriptCompiler.get().compile(this.javascriptArea.getText()));
    			}
    		catch(final Exception err)
    			{
    			JfxNgs.showExceptionDialog(this, err);
    			return;
    			}
    		}
    	BamFile reopened=null;
    	try 
	    	{
    		reopened=this.getBamFile().reOpen();
	    	@SuppressWarnings("unchecked")
			final ReadQualityStage qcstage=new ReadQualityStage(
	    			(ChartFactory<SAMRecord>)factory,
	    			reopened,
	    			compiledScript,
	    			makeFlagPredicate()
	    			);
	    	qcstage.show();
	    	}
    	catch(final IOException err)
    		{
    		CloserUtil.close(reopened);
    		JfxNgs.showExceptionDialog(this, err);
    		}
		
		}


	@Override
	protected void doMenuSaveAs() {
		final FileChooser fc= owner.newFileChooser();
    	fc.setSelectedExtensionFilter(EXTENSION_FILTER);
		final File saveAs= owner.updateLastDir(fc.showSaveDialog(this));
		if(saveAs==null) return;
		if(!saveAs.getName().endsWith(".bam"))
			{
			final Alert alert=new Alert(AlertType.ERROR, "Output should end with .bam", ButtonType.OK);
			alert.showAndWait();
			return;
			}
		
    	Optional<BamJavascripFilter> bamjsfilter=Optional.empty();
    	if(this.owner.javascriptCompiler.isPresent() && 
    			!this.javascriptArea.getText().trim().isEmpty())
    		{
    		try
    			{
    			bamjsfilter=Optional.of(
    					new BamJavascripFilter(this.getBamFile().getHeader(),
    							Optional.of(this.owner.javascriptCompiler.get().compile(this.javascriptArea.getText()))
    							));
    			}
    		catch(final Exception err)
    			{
    			JfxNgs.showExceptionDialog(this, err);
    			bamjsfilter=Optional.empty();
    			return;
    			}
    		}

    	final Predicate<SAMRecord> flagfilter=makeFlagPredicate();
		final SAMFileWriterFactory swf= new SAMFileWriterFactory();
		swf.setCreateIndex(true);
		CloseableIterator<SAMRecord> iter=null;
		SAMFileWriter w=null;
		try
			{
			final SAMFileHeader h2 =this.getBamFile().getHeader().clone();
			h2.addComment("Generated with JfxNgs. javascript was: "+
					this.javascriptArea.getText().trim().replaceAll("[\n\t\r ]+"," ")
					);
			
			w = swf.makeBAMWriter(h2, true, saveAs);
			
			iter= this.getBamFile().iterator();
			while(iter.hasNext())
				{
				final SAMRecord rec=iter.next();
				if(!flagfilter.test(rec)) continue;
				if(bamjsfilter.isPresent())
        			{
        			if(bamjsfilter.get().eval(rec)==null) continue;
        			}
				w.addAlignment(rec);
				}
			w.close();
			iter.close();
			}
		catch(Exception err)
			{
			JfxNgs.showExceptionDialog(this, err);
			return;
			}
		finally
			{
			CloserUtil.close(iter);
			CloserUtil.close(w);
			}    		
		}
    @Override
    protected String getSnippetResourcePath() {
    	return "/com/github/lindenb/jvarkit/tools/vcfviewgui/bam.snippets.xml";
    	}
    
    private BamFile getBamFile() {
    	return BamFile.class.cast(super.getNgsFile());
    }
    
    
    @Override
    protected Optional<SAMRecord> getCurrentSelectedItem() {
    	return Optional.ofNullable(this.recordTable.getSelectionModel().getSelectedItem());
    	}
    }
