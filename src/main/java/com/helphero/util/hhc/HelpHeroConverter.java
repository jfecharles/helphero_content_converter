package com.helphero.util.hhc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.ParseException;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.dom.processing.PartitionException;
import com.helphero.util.hhc.helper.CommandLineHelper;
import com.helphero.util.hhc.processing.DocumentPostProcessor;
import com.helphero.util.hhc.processing.DocumentTransformer;
import com.helphero.util.hhc.processing.FileConverter;
import com.helphero.util.hhc.processing.IDocumentPreProcessor;
import com.helphero.util.hhc.processing.IPreProcessorFactory;
import com.helphero.util.hhc.processing.PreProcessorFactoryImpl;
import com.helphero.util.hhc.processing.SupportedDocType;
import com.helphero.util.hhc.processing.XslInstanceManager;
import com.helphero.util.hhc.rule.RuleSetManager;
import com.helphero.util.hhc.util.ConfigHelper;
import com.helphero.util.hhc.util.FileHelper;

/**
 * The main class and entry point for the Generic Converter application.
 * 
 * This class takes care of the initialization of the application including:
 * - Processing of command line arguments
 * - Initializing resources settings
 * - Instantiating the Rule Set Manager
 * - Descending through the target folder structure and kicking of the conversion all target files. 
 *
 */
public class HelpHeroConverter 
{
	private static IPreProcessorFactory preProcessorFactory = new PreProcessorFactoryImpl();
	static Logger logger = Logger.getLogger(HelpHeroConverter.class);

    public static void main( String[] args ) throws Exception
    {
    	String applicationName = "HelpHeroConverter";
	    boolean debug = true;
	    int docId;
	    int folderId;
	    
        try {
    		CommandLineHelper cmdLineHelper = new CommandLineHelper(args);
    		
    		// Validate the Command line options
    		if (cmdLineHelper.validate())
    			System.exit(0);
    		
    		// Process the Command line options
    		cmdLineHelper.process();
    		
	        docId = cmdLineHelper.getDocId();
	        folderId = cmdLineHelper.getFolderId();        
	        
	        // Setup log4j parameters either from the command line or using default values. The log4j setup code MUST execute in the main thread.
	        if (System.getProperty("log4j.configuration") != null && System.getProperty("log4j.log_file") != null)
	        {
	        	PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
	        	logger.info("Setting log4j.properties location="+System.getProperty("log4j.configuration")+" and log4j logfile location="+System.getProperty("log4j.log_file"));
	        }
	        else
	        {
		        // Set default log4j settings for the location of the configuration file and the log file location
	        	File log4jPropsFile = null;
	        	
	        	URL url = Thread.currentThread().getContextClassLoader().getResource("resources/log4j.properties");
	        	
		    	if (url != null) log4jPropsFile = new File(url.getFile());
		    	
		    	if (log4jPropsFile == null || !log4jPropsFile.exists()) {
		    		log4jPropsFile = new File(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"log4j.properties");
		    	}
		    	
			    if (log4jPropsFile.exists()) {
		
			    	// Set a default log4j log file location if not specified on the command line.
			    	if (System.getProperty("log4j.log_file") == null) {
			    		System.setProperty("log4j.log_file", System.getProperty("user.dir")+File.separator+"pgc.log");
			    	}
		
			    	PropertyConfigurator.configure(log4jPropsFile.getPath());		        	
		        	System.setProperty("log4j.configuration", log4jPropsFile.getPath());		
		        	PropertyConfigurator.configure(System.getProperties());
		        	logger.info("Setting log4j.properties location="+log4jPropsFile.getPath()+" and log4j logfile location="+System.getProperty("log4j.log_file"));
		        } 
	        }
	        
	        // Attempt to disable the docx4j logging
	        Docx4jProperties.getProperties().setProperty("docx4j.Log4j.Configurator.disabled", "true");
	        org.docx4j.Docx4jProperties.setProperty("docx4j.Log4j.Configurator.disabled", "true");
	        PropertyConfigurator.configure(System.getProperties());	            
	        
	        ConfigHelper configHelper = new ConfigHelper();
	        configHelper.process();
	        
    		RuleSetManager ruleSetManager = new RuleSetManager(cmdLineHelper.getRules());  		
    		ruleSetManager.parse();
    		
    		// Pick up the debug setting from the rle.xml file.
    		debug = ruleSetManager.isDebug();
    		    		
    		IDocumentPreProcessor templatePreProcessor = null;
    		if (cmdLineHelper.isUseTemplate())
    		{
    			templatePreProcessor = preProcessorFactory.newInstance(SupportedDocType.DOTX);
    		}
    		
     		logger.info("\tTemplate File="+cmdLineHelper.getTemplateFile()+": Wildcard="+cmdLineHelper.getWildcard());

    		if (debug) {
    			logger.info("\tTemplate File="+cmdLineHelper.getTemplateFile()+": Wildcard="+cmdLineHelper.getWildcard());
    			if (templatePreProcessor == null) 
    				logger.info("\tTemplate File="+cmdLineHelper.getTemplateFile()+" is null");
    		}
    		
    		try {
				if (cmdLineHelper.isUseTemplate() && templatePreProcessor != null)
					templatePreProcessor.setInputFile(cmdLineHelper.getTemplateFile());
				
		        // Iterate through the remaining command line arguments containing the list of files or directories specified on the command line
		        List<File> files = FileHelper.getExpandedFileList(cmdLineHelper.getArgs(), cmdLineHelper.getWildcard());
		        
		        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cmdLineHelper.getThreadCount());
		        
		        Iterator<File> iter = files.iterator();
		        while (iter.hasNext())
		        {
		        	File f = (File)iter.next();
		        	
		        	FileConverter fileConverter = new FileConverter();
		        	fileConverter.setInputFile(f);
		        	fileConverter.setDebug(debug);
		        	fileConverter.setCmdLineHelper(cmdLineHelper);
		        	fileConverter.setConfigHelper(configHelper);
		        	fileConverter.setRuleSetManager(ruleSetManager);
		        	fileConverter.setTemplatePreProcessor(templatePreProcessor);
		        	
		        	executor.execute(fileConverter);		        	
		        }
		        
		        executor.shutdown();
		        
    		} catch (RuntimeException e1) {
    			e1.printStackTrace();
    		} catch (Docx4JException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   		
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }   
}
