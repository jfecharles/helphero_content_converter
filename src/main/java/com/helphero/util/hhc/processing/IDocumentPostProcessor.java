package com.helphero.util.hhc.processing;

import javax.xml.transform.TransformerConfigurationException;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.util.ConfigHelper;

/**
 * Interface describing the key methods of the Post Processing (final) stage of document conversion.
 *  
 * @author jcharles
 *
 */
public interface IDocumentPostProcessor {
	/**
	 * Execute the post processor
	 * @throws TransformException Thrown if an error occurs while executing the transform
	 */
	public void process() throws TransformException;	
	
	/**
	 * Set the input file to be processed.
	 * @param inputXhtmlFile Input XHtml file
	 */
	public void setInputFile(String inputXhtmlFile);
	
	/**
	 * Input File to be processed
	 * @return String Input File to be processed
	 */
	public String getInputFile();
	
	/**
	 * Set the output file for this stage of processing.
	 * @param inputXhtmlFile Output xHtml file
	 */
	public void setOutputFile(String inputXhtmlFile);
	
	/**
	 * Set the SupportPoint XSLT file for transforming from the business filtered and transformed xHtml file to the final stage output file. 
	 * @param xslFile SupportPoint XSLT file for transforming from the business filtered and transformed xHtml file to the final stage output file
	 */
	public void setXslFile(String xslFile);	
	
	/**
	 * Enable/disable internal debugging for the Document Post Processor.
	 * @param debug Flag to enable or disable internal debugging
	 */
	public void setDebug(boolean debug);
	
	/**
	 * The following methods are applicable to the SupportPoint XML format only
	 * Set the path to and including the XSLT tree file that is used determine folder and document hierarchy.    
	 * @param spsTreeXsl The SupportPoint XSTL folder hierarchy transform file
	 */
	public void setXslTreeFile(String spsTreeXsl);
	
	/**
	 * The SupportPoint XSLT transformed output file. This file requires an additional stage of processing 
	 * set by using the final output file.
	 * @param spsPreProcessedOutfile The SupportPoint XSLT transformed output file.
	 */
	public void setPreProcessedOutfile(String spsPreProcessedOutfile);
	
	/**
	 * Get the SupportPoint XSLT transformed output file
	 * @return String The SupportPoint XSLT transformed output file
	 */
	public String getPreProcessedOutfile();
	
	/**
	 * The final stage file. This is used to transform to SupportPoint final output XML.
	 * @param finalOutfile The final stage SupportPoint XML file
	 */
	public void setFinalOutfile(String finalOutfile);
	
	/**
	 * Get the final stage output file.
	 * @return String The final stage output file.
	 */
	public String getFinalOutfile();
	
	/**
	 * The generated XML folder hierarchy file. This is referenced by the instance
	 * specific XSLT transform file to include folder hierarchy information in the final output 
	 * of SupportPoint documents.
	 * @param treeOutputFile The generated XML folder hierarchy file. 
	 */
	public void setTempTreeOutputFile(String treeOutputFile);
	
	/**
	 * Document specific XSLT tree instance transform file. Used to create SupportPoint XML documemts.
	 * @param treeXslInstanceFile Document specific XSLT tree instance transform file.
	 */
	public void setXslTreeInstanceFile(String treeXslInstanceFile);
	
	/**
	 * 
	 * @param configHelper Configuration Helper instance. Used specify settings specific for any sanitization performed during the conversion to the final stage. 
	 */
	public void setConfigHelper(ConfigHelper configHelper);
}
