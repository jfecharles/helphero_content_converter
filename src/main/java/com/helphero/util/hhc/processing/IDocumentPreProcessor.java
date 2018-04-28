package com.helphero.util.hhc.processing;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;

/**
 * Interface describing the key methods for the pre-processing stage of the document conversion process.
 * 
 * @author jcharles
 */
public interface IDocumentPreProcessor {
	
	/**
	 * Set the input file to be processed.
	 * @param file Input file name
	 * @throws Docx4JException Exception thrown if the input file is set incorrectly
	 */
	public void setInputFile(String file) throws Docx4JException;
	
	/**
	 * Set the input file to be processed.
	 * @param file Input file name
	 * @throws Docx4JException Exception thrown if the input file is set incorrectly
	 */
	public void setInputFile(File file) throws Docx4JException;
	
	/**
	 * Set the pre-processed output file name. 
	 * @param outputFile Output file name
	 */
	public void setOutputFile(String outputFile);
	
	/**
	 * Get the pre-processed output file name.
	 * @return String Output file name
	 */
	public String getOutputFile();
	
	/**
	 * Method to inform the pre-processor to convert the output to xHtml. 
	 */
	public void toXhtml();
	
	/**
	 * Method to inform the pre-processor to convert the output file to xHtml.
	 * @param outfile xHtml output file name
	 */
	public void toXhtml(String outfile);

	/**
	 * Method to inform the pre-processor to convert the output to Html.
	 */
	public void toHtml();
	
	/**
	 * Method to inform the pre-processor to convert the output file to Html.
	 * @param outfile Output file name
	 */
	public void toHtml(String outfile);

	/**
	 * Wrapper method to allow for the injection of a custom style template file
	 * @param template OpcPackage
	 */
	public void injectCustomStylesFromTemplate(OpcPackage template);
	
	/**
	 * Get the OpcPackage handler from the underlying docx4j library. 
	 * @return OpcPackage OpcPackage handler
	 */
	public OpcPackage getOpcPackage();
}
