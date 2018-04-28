package com.helphero.util.hhc.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.helphero.util.hhc.HelpHeroConverter;
import com.helphero.util.hhc.dom.processing.PartitionException;
import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.helper.CommandLineHelper;
import com.helphero.util.hhc.processing.xslmanagers.IXslInstanceManager;
import com.helphero.util.hhc.processing.xslmanagers.IXslInstanceManagerFactory;
import com.helphero.util.hhc.processing.xslmanagers.XslInstanceManagerFactoryImpl;
import com.helphero.util.hhc.rule.RuleSetManager;
import com.helphero.util.hhc.util.ConfigHelper;
import com.helphero.util.hhc.util.FileHelper;

/**
 * The file converter class manages the overall process of converting a specific input document through each of the conversion stages.
 * It is runnable to allow the application level ThreadPoolExecutor to run multiple document conversion threads simultaneously.
 * 
 * @author jcharles
 *
 */
public class FileConverter implements Runnable {
	static Logger logger = Logger.getLogger(FileConverter.class);
	private static IPreProcessorFactory preProcessorFactory = new PreProcessorFactoryImpl();
	private static IPostProcessorFactory postProcessorFactory = new PostProcessorFactoryImpl();
	private static IXslInstanceManagerFactory xslInstanceManagerFactory = new XslInstanceManagerFactoryImpl();
	
	private CommandLineHelper cmdLineHelper;
	private ConfigHelper configHelper;
	private File inputFile;
	private IDocumentPreProcessor templatePreProcessor;
	private RuleSetManager ruleSetManager;
	private boolean debug;
	
	/**
	 * Constructor
	 */
	public FileConverter() {
	}

	/**
	 * The run method started by the thread pool executor. It handles all the conversion stages in a thread safe manor.
	 */
	public void run() {
		// TODO Auto-generated method stub	
    	String inputFilePath = this.getInputFile().getAbsolutePath();
    	File f = this.getInputFile();
    	
    	String folderPath = "";
    	String target = "roles.dtd";
    	
    	if (f.getPath().contains(File.separator))
    	{
    		folderPath = f.getPath().substring(0, f.getPath().lastIndexOf(File.separator));
    		target = folderPath+File.separator+"roles.dtd";
    	}
    	
    	File targetRolesDtd = new File(target);
    	
    	if (!targetRolesDtd.exists()) {
    		
    		// Extract the roles.dtd as a resource from from the class path
    		File srcRolesDtd = null;
    		URL url = this.getClass().getClassLoader().getResource("resources/roles.dtd");
		  
  		  	// load the resource from the CLASSPATH
  		  	if (url != null) 
  		  		srcRolesDtd = new File(url.getFile());

    		if (srcRolesDtd != null && srcRolesDtd.exists())
    		{
	    		try {
					FileHelper.copyFile(srcRolesDtd, targetRolesDtd);
					logger.info("Copied resource file roles.dtd as a resource in the CLASSPATH to "+target);
				} catch (IOException ex) {
					logger.error("Failed to copy roles.dtd file as a resource in the CLASSPATH to "+target, ex);
					// Show stopper for all threads in the ThreadPoolExecutor
					throw new RuntimeException("Failed to copy roles.dtd file as a resource in the CLASSPATH to "+target, ex);
				}
    		}
    		else
    		{
    			// If extracting from the class path fails copy from the current working directory ./resources sub-directory.
    			srcRolesDtd = new File(System.getProperty("user.dir")+File.separator+"resources"+File.separator+"roles.dtd");
    			
    			if (srcRolesDtd.exists()) 
    			{
    	    		try {
    					FileHelper.copyFile(srcRolesDtd, targetRolesDtd);
    					logger.info("Copied resource file=roles.dtd to "+target);
    				} catch (IOException ex) {
    					logger.error("Failed to process input roles.dtd file : ", ex);
    					// Show stopper for all threads in the ThreadPoolExecutor    					
    					throw new RuntimeException("Failed to process input roles.dtd file : ", ex);
    				}
    			}
    			else 
    			{
    				logger.error("Non-existant roles.dtd file in "+srcRolesDtd.getPath()+" : Failed to copy input roles.dtd file to "+target);
					// Show stopper for all threads in the ThreadPoolExecutor
    				throw new RuntimeException("Non-existant roles.dtd file in "+srcRolesDtd.getPath()+" : Failed to copy input roles.dtd file to "+target);
    			}
    		}
    	}
    	
    	// 1. Pre-process the source docx or pptx or slsx or pdf or ... input file
    	logger.info("\tProcessing File="+f.getAbsolutePath());
    	System.out.println("\tProcessing File="+f.getAbsolutePath());  	
    	
    	IDocumentPreProcessor docPreProcessor = preProcessorFactory.newInstance(cmdLineHelper.getDocTypeFromWildcard());
    	try {
			docPreProcessor.setInputFile(f);
			
	    	docPreProcessor.setOutputFile(inputFilePath + ".xhtml");		        	
	    	
	    	// 2. Inject custom styles if a custom style template is supplied
	    	if (cmdLineHelper.isUseTemplate() && this.getTemplatePreProcessor() != null) 
	    	{
	    		logger.info("\tInjecting custom styles from file "+cmdLineHelper.getTemplateFile());
	    		docPreProcessor.injectCustomStylesFromTemplate(this.getTemplatePreProcessor().getOpcPackage());
	    	}
	    	
	    	docPreProcessor.toXhtml(); // Persist the pre-processed file to the intermediary common object format xhtml file
	    	logger.info("\tStage 1 and 2 Convert input file " + f.getAbsolutePath() + " to xhtml File="+docPreProcessor.getOutputFile());
	    	
	    	// 3. Perform the business rules driven transformations and filtering on the common object intermediary xhtml file.
	    	DocumentTransformer docTransformer = new DocumentTransformer();
	    	docTransformer.setRuleSet(ruleSetManager.getRuleSet());
	    	docTransformer.setInputFile(inputFilePath + ".xhtml");
	    	docTransformer.setOutputFile(inputFilePath + ".biz.xhtml");
	    	try {
	    		docTransformer.loadAsXml();	
	    		
	    		docTransformer.process();
			
	    		docTransformer.saveAsXml();
	        	logger.info("\tStage 3 Performed business rules transformation File="+docTransformer.getOutputFile()+":TopPartition="+docTransformer.getTopPartition());
	        	 					
	        	// Setup all the document specific XSL instance files and their dependent references.
	        	// This ensures XSL processing is thread safe enabling concurrent document conversion.
	        	IXslInstanceManager xslHelper = xslInstanceManagerFactory.newInstance(cmdLineHelper.getSupportedTargetType());
	        	xslHelper.setInputFile(f);
	        	xslHelper.setDbVersion(cmdLineHelper.getDbVersion());
	        	xslHelper.setDocType(cmdLineHelper.getDocType());
	        	xslHelper.setLangId(cmdLineHelper.getLangId());
	        	xslHelper.setNestedTablePadChar(cmdLineHelper.getNestedTablePadChar());
	        	xslHelper.setSpsXsl(cmdLineHelper.getSpsXsl());
	        	xslHelper.setSpsTreeXsl(cmdLineHelper.getSpsTreeXsl());
	        	xslHelper.setTopPartition(docTransformer.getTopPartition());
	        	xslHelper.setTopPartitionId(docTransformer.getTopPartitionId());
	        	if (cmdLineHelper.getSupportedTargetType() == SupportedOutputDocType.HHV1) 
	        	{
		        	// Additional parameters need for the Help Hero V1 JSON output
	        		xslHelper.setOrganisation(cmdLineHelper.getOrganisation());
	        		xslHelper.setCountry(cmdLineHelper.getCountry());
	        		xslHelper.setLanguage(cmdLineHelper.getLanguage());	        	
	        	}
	        	xslHelper.process();
	        	logger.info("\tStage 4 Generated all the document specific Xsl files and dependencies.");

	        	// 4. Post-process the business rules filtered and transformed xhtml file and generate the final target format i.e. SupportPoint XML file.
	        	IDocumentPostProcessor postProcessor = postProcessorFactory.newInstance(cmdLineHelper.getSupportedTargetType());
	        	postProcessor.setDebug(debug);
	        	postProcessor.setInputFile(inputFilePath + ".biz.xhtml");
	        	postProcessor.setXslTreeFile(cmdLineHelper.getSpsTreeXsl());
	        	postProcessor.setXslFile(xslHelper.getSpsXslInstance());
	        	postProcessor.setPreProcessedOutfile(inputFilePath + ".presps.xml");
	        	postProcessor.setFinalOutfile(inputFilePath + ".sps.xml");		        	
	        	postProcessor.setTempTreeOutputFile(xslHelper.getSpsXmlOutputTreeInstance());
	        	postProcessor.setXslTreeInstanceFile(inputFilePath + ".sps.tree.xsl");
	        	postProcessor.setConfigHelper(configHelper);
	        	postProcessor.process();		        	
	        	logger.info("\tStage 5 Completed Pre and Post Processing of Input file="+postProcessor.getInputFile()+" PreProcessed Output File="+postProcessor.getPreProcessedOutfile());
	        	
	        	System.out.println("\tDone: Completed converting input document ["+f.getAbsolutePath()+"] to " + postProcessor.getFinalOutfile());
	        	logger.info("\tDone: Completed converting input document ["+f.getAbsolutePath()+"] to " + postProcessor.getFinalOutfile());
	    	} catch (ParserConfigurationException ex)
	    	{
	    		logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);
	    		System.err.println("Failed to process input file :" +f.getAbsolutePath());
	    	} catch (XPathExpressionException ex)
	    	{
	    		logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);
	    		System.err.println("Failed to process input file :" +f.getAbsolutePath());
	    	} catch (TransformerConfigurationException ex)
	    	{
	    		logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);
	    		System.err.println("Failed to process input file :" +f.getAbsolutePath());
	    	} catch (TransformException ex) {
	    		logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);
	    		System.err.println("Failed to process input file :" +f.getAbsolutePath());
			} catch (Exception ex) {
				logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);	
				System.err.println("Failed to process input file :" +f.getAbsolutePath());
			}	
		} catch (Docx4JException ex) {
			logger.error("Failed to process input file :" +f.getAbsolutePath(), ex);
			System.err.println("Failed to process input file :" +f.getAbsolutePath());
		}
    }				

	/**
	 * Get the command line helper instance
	 * @return CommandLineHelper
	 */
	public CommandLineHelper getCmdLineHelper() {
		return cmdLineHelper;
	}

	/**
	 * Set the command line helper instance
	 * @param cmdLineHelper CommandLineHelper
	 */
	public void setCmdLineHelper(CommandLineHelper cmdLineHelper) {
		this.cmdLineHelper = cmdLineHelper;
	}
	
	/**
	 * Set the application config helper instance
	 * @param configHelper ConfigHelper
	 */
	public void setConfigHelper(ConfigHelper configHelper) {
		this.configHelper = configHelper;
	}

	/** 
	 * Get the input file
	 * @return inputFile Input file name
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * Set the input file
	 * @param inputFile Input file name
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	public boolean getDebug() {
		return this.debug;
	}
	
	public void setDebug(boolean debug){
		this.debug = debug;
	}

	/**
	 * Get the template pre-processor instance
	 * @return templatePreProcessor IDocumentPreProcessor implementation instance 
	 */
	public IDocumentPreProcessor getTemplatePreProcessor() {
		return templatePreProcessor;
	}

	/**
	 * Set the template pre-processor instance
	 * @param templatePreProcessor IDocumentPreProcessor implementation instance
	 */
	public void setTemplatePreProcessor(IDocumentPreProcessor templatePreProcessor) {
		this.templatePreProcessor = templatePreProcessor;
	}
	
	/**
	 * Get the rule set manager instance
	 * @return ruleSetManager RuleSetManager
	 */
	public RuleSetManager getRuleSetManager()
	{
		return this.ruleSetManager;
	}
	
	/**
	 * Set the rule set manager instance
	 * @param ruleSetManager RuleSetManager
	 */
	public void setRuleSetManager(RuleSetManager ruleSetManager)
	{
		this.ruleSetManager = ruleSetManager;
	}
}
