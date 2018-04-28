package com.helphero.util.hhc.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PowerPointPreProcessor implements IDocumentPreProcessor {
	private OpcPackage opcPackage;
	private String outputFile;
	private boolean useSaxon9 = true;
	
	private static Logger logger = LoggerFactory.getLogger(PowerPointPreProcessor.class);	

	public PowerPointPreProcessor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Set the input file to be processed
	 * @param file Input file name
	 */
	public void setInputFile(String file) throws Docx4JException {
		setOpcPackage(OpcPackage.load(new File(file)));	
	}
	
	/**
	 * Set the specified input file to be processed.
	 * @param file Input File
	 */
	public void setInputFile(File file) throws Docx4JException {
		setOpcPackage(OpcPackage.load(file));
	}	

	/**
	 * Get the output file name.
	 * @return outputFile Output file name
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Set the output file name.
	 * @param outputFile Output file name
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}	

	/**
	 * Convenience method to convert an input file to xHtml output file.
	 */
	public void toXhtml() {
		toOutputFormat(this.getOutputFile(),Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
	
	/**
	 * Convenience method to convert an input file to a specified xHtml output file.
	 * @param outfile xHtml output file name
	 */
	public void toXhtml(String outfile) {
		toOutputFormat(outfile,Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Convenience method to convert an input file to an Html output file.
	 */
	public void toHtml() {
		toOutputFormat(this.getOutputFile(), Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Convenience method to convert an input file to a specified Html output file.
	 * @param outfile xHtml output file name
	 */
	public void toHtml(String outfile) {
		toOutputFormat(outfile, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
	
	private void toOutputFormat(String outfile, int format) {
		// It is mandatory to define the html settings
    	HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
    	
    	String inputFilePath = outfile.contains(File.separator) ? outfile.substring(0,outfile.lastIndexOf(File.separator)) : ".";
    	
    	htmlSettings.setImageDirPath(inputFilePath + File.separator + "_files");
    	htmlSettings.setImageTargetUri(inputFilePath + File.separator + "_files");

    	htmlSettings.setWmlPackage((PresentationMLPackage)opcPackage);
    	
    	// PresentationMLPackage presentationMLPackage = (PresentationMLPackage)opcPackage;
    	// MainPresentationPart mainPresentationPart = presentationMLPackage.getMainPresentationPart();
 
    	String propName = "docx4j.Convert.Out.HTML.OutputMethodXML";
    	switch (format) {
    	case Docx4J.FLAG_EXPORT_PREFER_XSL:
    		propName = "docx4j.Convert.Out.HTML.OutputMethodXML";
    		break;
    	case Docx4J.FLAG_SAVE_FLAT_XML:
    	}
    	
    	OutputStream os = null;
		try {
			os = new FileOutputStream(outfile);
			
	    	// Save output as XHTML
	    	Docx4jProperties.setProperty(propName, true);
	    	
	    	// Export the document using an xslt transformation
	    	try {
				Docx4J.toHTML(htmlSettings, os, format);
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}

	public void injectCustomStylesFromTemplate(OpcPackage template) {
	}

	public OpcPackage getOpcPackage() {
		return opcPackage;
	}

	public void setOpcPackage(OpcPackage opcPackage) {
		this.opcPackage = opcPackage;
	}

	public boolean isUseSaxon9() {
		return useSaxon9;
	}

	public void setUseSaxon9(boolean useSaxon9) {
		this.useSaxon9 = useSaxon9;
	}

}
