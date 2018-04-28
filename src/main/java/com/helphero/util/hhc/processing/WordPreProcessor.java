package com.helphero.util.hhc.processing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.TraversalUtil.Callback;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTGraphicalObjectFrameLocking;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTNonVisualPictureProperties;
import org.docx4j.dml.CTOfficeArtExtension;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTPoint2D;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTStretchInfoProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.Theme;
import org.docx4j.dml.picture.CTPictureNonVisual;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTEffectExtent;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.OleObjectBinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblLook;
import org.docx4j.wml.CTTblPPr;
import org.docx4j.wml.Fonts;
import org.docx4j.wml.Styles;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTTblPrBase.TblStyleColBandSize;
import org.docx4j.wml.CTTblPrChange;
import org.docx4j.wml.Jc;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.Pict;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblWidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microsoft docx format pre-processor class to convert docx files to various target formats i.e. xHtml, Html
 * 
 * In addition this class provides a number of diagnostic methods to dump out parts of documents and prnt on the console.
 * 
 * @author jcharles
 */
public class WordPreProcessor extends OpcPackage implements IDocumentPreProcessor {
	private OpcPackage opcPackage;
	private String outputFile;
	private MainDocumentPart wordTemplateDocPart = null;
	private boolean debug = false;
	private boolean useSaxon9 = true;
	
	private static Logger logger = LoggerFactory.getLogger(WordPreProcessor.class);						

	/**
	 * Constructor
	 */
	public WordPreProcessor() {
	}
	
	/**
	 * Constructor specifying input file 
	 * @param file Input File 
	 * @throws Docx4JException Thrown if an error occurs converting the input document to the intermediary xHtml document
	 */
	public WordPreProcessor(File file) throws Docx4JException {
		setOpcPackage(OpcPackage.load(file));		
	}
	
	/**
	 * Convenience method to convert an input document to docx.
	 * @param outfile Output file name
	 */
	public void toDocx(String outfile)
	{
    	WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)opcPackage;
		
		try {
			wordMLPackage.save(new File(outfile));
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	/**
	 * Private method to dump out the content of an output file.
	 * @param outfile Output file
	 * @param format Docx4j format options
	 */
	private void toOutputFormat(String outfile, int format) {
		// It is mandatory to define the html settings
    	HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
    	
    	String inputFilePath = outfile.contains(File.separator) ? outfile.substring(0,outfile.lastIndexOf(File.separator)) : ".";
    	
    	htmlSettings.setImageDirPath(inputFilePath + File.separator + "_files");
    	htmlSettings.setImageTargetUri(inputFilePath + File.separator + "_files");

    	htmlSettings.setWmlPackage((WordprocessingMLPackage)opcPackage);

    	/*
    	To save the document as flat XML - Very little difference!!!

    	OutputStream flatos = new FileOutputStream(inputFilePath + ".flat.xhtml");
    	htmlSettings.setImageDirPath(inputFilePath + "_flatxml_files");
    	htmlSettings.setImageTargetUri(inputFilePath.substring(inputFilePath.lastIndexOf("/")+1)
    			+ "_flatxml_files");
    	
    	Docx4J.toHTML(htmlSettings, flatos, Docx4J.FLAG_SAVE_FLAT_XML);
    	*/
    	
    	/*
    	To save as xhtml with custom styling
    	suffix = ".cstyle.xhtml";
    	if (useTemplate) suffix = ".ocstyle.xhtml";
    	OutputStream flatos = new FileOutputStream(inputFilePath + suffix);
    	htmlSettings.setImageDirPath(inputFilePath + "_cstyle_files");
    	htmlSettings.setImageTargetUri(inputFilePath.substring(inputFilePath.lastIndexOf("/")+1)
    			+ "_cstyle_files");
    	// Setup base styling
      	String userCSS = "html, body, div, span, h1, h2, h3, h4, h5, h6, p, a, img,  ol, ul, li, table, caption, tbody, tfoot, thead, tr, th, td " +
    			"{ margin: 0; padding: 0; border: 0;}" +
    			"body {line-height: 1;} ";
    	htmlSettings.setUserCSS(userCSS);
    	
    	Docx4J.toHTML(htmlSettings, flatos, Docx4J.FLAG_EXPORT_PREFER_XSL);
    	*/	
 
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
		WordprocessingMLPackage wordTemplateMLPackage = (WordprocessingMLPackage)template;
		
		wordTemplateDocPart = wordTemplateMLPackage.getMainDocumentPart();
		
		// Extract the Themes, Styles and fonts from the template document

		// Styles Part
    	StyleDefinitionsPart templateStyleDefPart = wordTemplateDocPart.getStyleDefinitionsPart();
    	Styles templateStyle = templateStyleDefPart.getJaxbElement();
    	Styles templateStyles = templateStyleDefPart.getContents();

    	// Themes part
    	ThemePart templateThemePart = wordTemplateDocPart.getThemePart();
    	Theme templateTheme = templateThemePart.getJaxbElement();
    	
    	// String sTemplateStyle = XmlUtils.marshaltoString(templateStyle, true);

    	// FontTable part
    	FontTablePart templateFontPart = wordTemplateDocPart.getFontTablePart();
    	Fonts templateFonts = templateFontPart.getJaxbElement();
    	
    	WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)opcPackage;
    	
		// Extract the style, theme and fonts parts from the existing document
		MainDocumentPart wordDocPart = wordMLPackage.getMainDocumentPart();
		StyleDefinitionsPart wordStyleDefsPart = wordDocPart.getStyleDefinitionsPart();
		ThemePart wordThemePart = wordDocPart.getThemePart();
		FontTablePart wordFontTablePart = wordDocPart.getFontTablePart();
		
		Styles wordStyles = wordStyleDefsPart.getContents();
		if (debug) System.out.println("======= Start Word Styles ===============");
		for (org.docx4j.wml.Style s : wordStyles.getStyle())
		{
			if (debug) System.out.println("\t\tStyle with name=" + s.getName().getVal() + ", id='" + s.getStyleId() + "' type=" + s.getType() + " style");
			boolean found = false;
			Iterator<org.docx4j.wml.Style> iterator = templateStyle.getStyle().iterator();
			while (iterator.hasNext())
			{
				org.docx4j.wml.Style ts = iterator.next();
				if (s.getStyleId().equals(ts.getStyleId()))
				{
					found = true;
					break;
				}
			}
			// Add any styles to the template contained in the input document not in the template
			if (!found)
			{
				if (debug) System.out.println("\t\t\tStyle with name=" + s.getName().getVal() + ", id='" + s.getStyleId() + "' type=" + s.getType() + " style - DOES NOT EXIST IN THE Template File");
				// Add the missing style to the template style
				templateStyle.getStyle().add(s);
			}
		}
		if (debug) System.out.println("======= End Word Styles ===============");

		// Override the current document with the styles of the template document.
		wordStyleDefsPart.setJaxbElement(templateStyle);
		wordThemePart.setJaxbElement(templateTheme);
		wordFontTablePart.setJaxbElement(templateFonts);
	}
	
	public OpcPackage getOpcPackage() {
		return opcPackage;
	}

	public void setOpcPackage(OpcPackage opcPackage) {
		this.opcPackage = opcPackage;
	}

	/**
	 * Set the input file to be processed.
	 */
	public void setInputFile(String file) throws Docx4JException {
		setOpcPackage(OpcPackage.load(new File(file)));	
	}

	/**
	 * Set the specified input file to be processed.
	 * @param file Input file name
	 */
	public void setInputFile(File file) throws Docx4JException {
		setOpcPackage(OpcPackage.load(file));
	}	

	/**
	 * Get the main part of the Word template file used to inject custom styles and override existing styles.
	 * @return wordTemplateDocPart Main part of the Word template 
	 */
	public MainDocumentPart getWordTemplateDocPart() {
		return wordTemplateDocPart;
	}

	/**
	 * Set the main part of the Word template file used to inject custom styles and override existing styles.
	 * @param wordTemplateDocPart Main part of the Word template file used to inject custom styles and override existing styles
	 */
	public void setWordTemplateDocPart(MainDocumentPart wordTemplateDocPart) {
		this.wordTemplateDocPart = wordTemplateDocPart;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Is the Saxon9 XSLT being used? 
	 * @return useSaxon9 Is the Saxon9 XSLT parser used?
	 */
	public boolean isUseSaxon9() {
		return useSaxon9;
	}

	/**
	 * Force the saxon9 XSLT parser to be used.
	 * @param useSaxon9 Use the Saxon9 XSLT parser
	 */
	public void setUseSaxon9(boolean useSaxon9) {
		this.useSaxon9 = useSaxon9;
	}
		
	/**
	 * The following methods are for diagnostic purposes only.
	 * 
	 * Dump out the list of Content Types
	 * @param pkg OpcPackage
	 */
	public static void printContentTypes(OpcPackage pkg) {
    	System.out.println("Content Types:");

    	ContentTypeManager ctm = pkg.getContentTypeManager();

		System.out.println(ctm.toString());
    	System.out.println("========================================");
	}
	
	/**
	 * Static method to dump out the various parts of an Opc document.
	 * @param pkg OpcPackage
	 */
	public static void printDocumentParts(OpcPackage pkg)
	{
    	System.out.println("Document Parts:");
  			
		RelationshipsPart rp = pkg.getRelationshipsPart();

		StringBuilder sb = new StringBuilder();
		printInfo(rp, sb, "");
		traverseRelationships(pkg, rp, sb, "    ");
		
		System.out.println(sb.toString());
    	System.out.println("========================================");
	}

	/**
	 * Static method to print the contents of various parts of an Opc document.
	 * Used for diagnostic purposes only.
	 * @param p Part
	 * @param sb StringBuilder
	 * @param indent String
	 */
	public static void  printInfo(Part p, StringBuilder sb, String indent) {
		
		String relationshipType = "";
		if (p.getSourceRelationships().size()>0 ) {
			relationshipType = p.getSourceRelationships().get(0).getType();
		}
		
		sb.append("\n" + indent + "Part " + p.getPartName() + " [" + p.getClass().getName() + "] " + relationshipType );
		
		if (p instanceof JaxbXmlPart) {
			Object o = ((JaxbXmlPart)p).getJaxbElement();
			if (o instanceof javax.xml.bind.JAXBElement) {
				sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
			} else {
				sb.append(" containing:"  + o.getClass().getName() );
			}
		} else if (p instanceof DefaultXmlPart) {
			try {
				org.w3c.dom.Document doc = ((DefaultXmlPart)p).getDocument();
				Object o = XmlUtils.unmarshal(doc);
				if (o instanceof javax.xml.bind.JAXBElement) {
					sb.append(" containing JaxbElement:" + XmlUtils.JAXBElementDebug((JAXBElement)o) );
				} else {
					sb.append(" containing:"  + o.getClass().getName() );
				}				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		sb.append("\n content type: " + p.getContentType() + "\n");
		
		if (p instanceof OleObjectBinaryPart) {
			
			try {
				((OleObjectBinaryPart)p).viewFile(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (p instanceof VbaDataPart) {
			System.out.println( ((VbaDataPart)p).getXML() );
		}
		
	}
	
	/**
	 * This HashMap is intended to prevent loops.
	 */
	public static HashMap<Part, Part> handled = new HashMap<Part, Part>();
	
	/**
	 * Static method used to recurse down through the parts of a Word ML Package document.
	 * Used for diagnostic purposes only.
	 * @param wordMLPackage Word ML Package
	 * @param rp RelationshipsPart
	 * @param sb StringBuilder
	 * @param indent String
	 */
	public static void traverseRelationships(org.docx4j.openpackaging.packages.OpcPackage wordMLPackage, 
		RelationshipsPart rp, 
		StringBuilder sb, String indent) {
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
			logger.info("\nFor Relationship Id=" + r.getId() 
					+ " Source is " + rp.getSourceP().getPartName() 
					+ ", Target is " + r.getTarget() 
					+ " type " + r.getType() + "\n");
		
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				
				sb.append("\n" + indent + "external resource " + r.getTarget() 
						   + " of type " + r.getType() );
				continue;				
			}
			
			org.docx4j.openpackaging.parts.Part part = rp.getPart(r);
						
			
			printInfo(part, sb, indent);
			if (handled.get(part)!=null) {
				sb.append(" [additional reference] ");
				continue;
			}
			handled.put(part, part);
			if (((Base) part).getRelationshipsPart(false)==null) {
				// sb.append(".. no rels" );						
			} else {
				traverseRelationships(wordMLPackage, part.getRelationshipsPart(false), sb, indent + "    ");
			}
					
		}	
	}
	
	/**
	 * Static method used to print out the core document information. Used for diagnostic purposes only.
	 * @param pkg String
	 */
	public static void printCoreDocumentInfo(OpcPackage pkg)
	{
		WordprocessingMLPackage wordPkg = (WordprocessingMLPackage)pkg;
		final MainDocumentPart docPart = wordPkg.getMainDocumentPart();
		
		System.out.println("===================================");
		System.out.println("XML: "+docPart.getXML());
		System.out.println("===================================");
		
    	System.out.println("Core Document Info:");
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)docPart.getJaxbElement();
		Body body = wmlDocumentEl.getBody();
		
		// Recursively descend and display all the contents of a document
		new TraversalUtil(body,

				new Callback() {

					String indent = "";

					//@Override
					public List<Object> apply(Object o) {

						String text = "";
						String para = "";
						String run = "";
						
						// Process a paragraph and all its properties
						if (o instanceof org.docx4j.wml.P)
						{
							org.docx4j.wml.P p = (org.docx4j.wml.P)o;
							org.docx4j.wml.PPr pPr = p.getPPr();
							
							para = ((org.docx4j.wml.P) o).getRsidRDefault();
							para = para == null ? "" : para;
							System.out.print(indent + o.getClass().getName() + "  \""
    								+ para + "\"");
							if (pPr != null)
							{
								String align = null;
								String shadow = null;

								StringBuilder sb = new StringBuilder();
								
								// Vertical Alignment
								if (pPr.getTextAlignment() != null)
								{
									System.out.print(" valignment=\""+pPr.getTextAlignment().getVal()+"\"");
								}
								
								// Justification
								if (pPr.getJc() != null)
								{
									System.out.print(" halignment=\""+pPr.getJc().getVal().name()+"\"");
								}
								
								if (pPr.getShd() != null)
								{
									System.out.print(" shadow=\""+pPr.getShd().getColor()+"\"");
								}
								
								if (pPr.getInd() != null)
								{
									boolean prev = false;
									if (pPr.getInd().getFirstLine() != null)
									{
										sb.append("f:"+UnitsOfMeasurement.twipToPoint(pPr.getInd().getFirstLine().intValue()));
										prev = true;
									}
									if (pPr.getInd().getHanging() != null)
									{
										if (prev == true) sb.append(",");
										sb.append("h:"+UnitsOfMeasurement.twipToPoint(pPr.getInd().getHanging().intValue()));
										prev = true;
									}
									if (pPr.getInd().getLeft() != null)
									{
										if (prev == true) sb.append(",");
										sb.append("l:"+UnitsOfMeasurement.twipToPoint(pPr.getInd().getLeft().intValue()));
										prev = true;
									}
									if (pPr.getInd().getRight() != null)
									{
										if (prev == true) sb.append(",");
										sb.append("r:"+UnitsOfMeasurement.twipToPoint(pPr.getInd().getRight().intValue()));
										prev = true;
									}
									
									if (sb.length() > 0)
										System.out.print("  \"indent=" + sb.toString()+"\"");
								}
								
								// Include the properties for a list: Namely the indent level and the type of list
								// listnum = 1 : Bulleted list
								// listnum = 2 : Numbered list
								// listnum = 3 : Multi-level list
								if (pPr.getNumPr() != null)
								{
									BigInteger iLvl = pPr.getNumPr().getIlvl().getVal();
									BigInteger numId = pPr.getNumPr().getNumId().getVal();
									System.out.print("  indentlevel=\"" + iLvl.intValueExact()+"\" listnum=\""+numId.intValueExact()+"\"");
								}
								
								System.out.print("  " + pPr.getClass().getName());
								
								//if (align != null) System.out.print("  \"align=" + align + "\"");
								//if (shadow != null) System.out.print("  \"shadowcolor=" + shadow + "\"");
								

								if (pPr.getPStyle() != null)
									System.out.println("  \"style=" + pPr.getPStyle().getVal() + "\"");
								else
									System.out.println();
								
								// System.out.println("  " + pPr.getClass().getName() + "  \"style=" + pPr.getPStyle().getVal() + "\"");
							}
							else
							{
								System.out.println();
							}
						}
						
						// Process a run and all its properties
						if (o instanceof org.docx4j.wml.R)
						{
							org.docx4j.wml.R r = (org.docx4j.wml.R)o;
							org.docx4j.wml.RPr rPr = r.getRPr();
							
							run = ((org.docx4j.wml.R) o).getRsidRPr();
							run = run == null ? "" : run;
							StringBuilder sb = new StringBuilder();
							if (rPr != null)
							{
								if (rPr.getB() != null && rPr.getB().isVal())	{ 
									if (sb.length() == 0) sb.append("bold");
									else sb.append(",bold"); 
								}
								if (rPr.getI() != null && rPr.getI().isVal())	{ 
									if (sb.length() == 0) sb.append("italic");
									else sb.append(",italic"); 
								}
								if (rPr.getU() != null && rPr.getU().getVal().value() != null)	{ 
									if (sb.length() == 0) sb.append("underline:"+rPr.getU().getVal().value());
									else sb.append(",underline:"+rPr.getU().getVal().value()); 
								}
								if (rPr.getStrike() != null && rPr.getStrike().isVal())
								{
									if (rPr.getStrike().isVal())	{ 
										if (sb.length() == 0) sb.append("strike");
										else sb.append(",strike"); 
									}
								}
								if (rPr.getRtl() != null && rPr.getRtl().isVal())
								{
									if (rPr.getRtl().isVal())	{ 
										if (sb.length() == 0) sb.append("rtl");
										else sb.append(",rtl"); 
									}
								}
								if (rPr.getShd() != null) 
								{
									if (sb.length() > 0) sb.append(",");
									sb.append("shadow:"+rPr.getShd().getColor());
								}

								if (rPr.getRFonts() != null)
								{
									String font = "";
									if (rPr.getRFonts().getAscii() != null)
										font = rPr.getRFonts().getAscii();
									if (rPr.getRFonts().getCs() != null)
										font = rPr.getRFonts().getCs();
									if (rPr.getRFonts().getEastAsia() != null)
										font = rPr.getRFonts().getEastAsia();
									if (rPr.getRFonts().getHAnsi() != null)
										font = rPr.getRFonts().getHAnsi();
																
									if (sb.length() > 0) sb.append(",");
									sb.append("font:"+font);
									if (rPr.getSz() != null)
									{
										sb.append("-"+rPr.getSz().getVal().floatValue()/2);
									}
								}
								
								if (rPr.getColor() != null)
								{
									String color = rPr.getColor().getVal();
									if (sb.length() > 0) sb.append(",");
									sb.append("color:"+color);
									
								}
								if (rPr.getHighlight() != null)
								{
									String hlcolor = rPr.getHighlight().getVal();
									if (sb.length() > 0) sb.append(",");
									sb.append("hlcolor:"+hlcolor);
									
								}
								if (rPr.getVertAlign() != null)
								{
									String align = rPr.getVertAlign().getVal().value();
									if (sb.length() > 0) sb.append(",");
									sb.append("valign:"+align);
								}
								/*
								 * Full list of properties includes:
								 */
							}
							
							System.out.print(indent + o.getClass().getName() + "  \"" + run + "\"");
							if (rPr != null)
							{
								System.out.println("  " + rPr.getClass().getName() + "  \"style=" + sb.toString() + "\"");
							}
							else
							{
								System.out.println();
							}
						}
						
						if (o instanceof org.docx4j.wml.Text)
						{
							text = ((org.docx4j.wml.Text) o).getValue();
							System.out.println(indent + o.getClass().getName() + "  \""	+ text + "\"");
						}
						
						if (o instanceof org.docx4j.wml.Pict)
						{
							Pict pict = (org.docx4j.wml.Pict)o;
							System.out.println(indent + pict.getClass().getName() + "  \""	+ text + "\"");
						}
						
						// Process a table and all its properties
						if (o instanceof org.docx4j.wml.Tbl)
						{
							org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl)o;
							
							// Table grid properties
							TblGrid tblGrid = tbl.getTblGrid();
							
							org.docx4j.wml.TblPr tblPr = tbl.getTblPr();
							Jc jc = tblPr.getJc();
							// Justification enumeration values "left", "center", "right", "both", "mediumKashida", "distribute", "numTab", "highKashida", "lowKashida", "thaiDistribute"
							StringBuilder sb = new StringBuilder();
							if (jc != null && jc.getVal() != null)
								sb.append(" justification="+jc.getVal().name());
							
							// Bidi Visual Flag
							if (tblPr.getBidiVisual() != null)
								sb.append(" bidi="+tblPr.getBidiVisual().isVal());

							// Shadow properties
							CTShd shd = tblPr.getShd();
							if (shd != null)
							{
								String color = shd.getColor();
								String fill = shd.getFill();
								String themeColor = shd.getThemeColor().name();
								String themeFill = shd.getThemeFill().name();
								String themeFillShd = shd.getThemeFillShade();
								String themeFillTint = shd.getThemeFillTint();
								String themeTint = shd.getThemeTint();
							}
							
							// Table border Properties
							TblBorders brd = tblPr.getTblBorders();
							if (brd != null)
							{
								String bottom = brd.getBottom() != null && brd.getBottom().getVal() != null ? brd.getBottom().getVal().name() : "";
								String top = brd.getTop() != null && brd.getTop().getVal() != null ? brd.getTop().getVal().name() : "";
								String insideH = brd.getInsideH() != null && brd.getInsideH().getVal() != null ? brd.getInsideH().getVal().name() : "";
								String insideV = brd.getInsideV() != null && brd.getInsideV().getVal() != null ? brd.getInsideV().getVal().name() : "";
								String left = brd.getLeft() != null && brd.getLeft().getVal() != null ? brd.getLeft().getVal().name() : "";
								String right = brd.getRight() != null && brd.getRight().getVal() != null ? brd.getRight().getVal().name() : "";
							}
							
							// Table cell margins: left, right, top, bottom 
							CTTblCellMar cellMargin = tblPr.getTblCellMar();
							if (cellMargin != null)
							{
								int cellL = cellMargin.getLeft() != null ? cellMargin.getLeft().getW().intValueExact(): 0;
								int cellR = cellMargin.getRight() != null ? cellMargin.getRight().getW().intValueExact() : 0;
								int cellT = cellMargin.getTop() != null ? cellMargin.getTop().getW().intValueExact() : 0;
								int cellB = cellMargin.getBottom() != null ? cellMargin.getBottom().getW().intValueExact() : 0;
							}
							
							// Table cell spacing
							TblWidth cellSpac = tblPr.getTblCellSpacing();
							if (cellSpac != null)
							{
								int cellSpacing = cellSpac != null ? cellSpac.getW().intValueExact() : 0;
								String spacingType = cellSpac.getType() != null ? cellSpac.getType() : "";
							}
							
							// Table indent
							TblWidth tblIndent = tblPr.getTblInd();
							if (tblIndent != null)
							{
								int indW = tblIndent.getW().intValueExact();
								String indType = tblIndent.getType() != null ? tblIndent.getType() : "";
							}
						
							// Table layout
							if (tblPr.getTblLayout() != null)
								sb.append(" layout=\""+tblPr.getTblLayout().getType().name()+"\"");
							
							// Table look
							if (tblPr.getTblLook() != null)
							{
								sb.append(" look=\""+tblPr.getTblLook().getVal()+"\"");
								CTTblLook look = tblPr.getTblLook();
								look.getFirstColumn().name();
								look.getFirstRow().name();
								look.getLastRow().name();
								look.getNoHBand().name();
								look.getNoVBand().name();
							}
							
							// Table overlap
							if (tblPr.getTblOverlap() != null)
								sb.append("overlap=\""+tblPr.getTblOverlap().getVal()+"\"");
							
							// Base properties
							CTTblPPr tblpPr = tblPr.getTblpPr();
							if (tblpPr != null)
							{
								int x = tblpPr.getTblpX().intValue();
								String xspec = tblpPr.getTblpXSpec().name();
								int y= tblpPr.getTblpY().intValue();
								String yspec = tblpPr.getTblpYSpec().name();
								int topFrmTxt = tblpPr.getTopFromText().intValue();
								String vanchor = tblpPr.getVertAnchor().name();
							}
							
							// Change Properties
							CTTblPrChange prChg = tblPr.getTblPrChange();
							if (prChg != null)
							{
								String author = prChg.getAuthor() != null ? prChg.getAuthor() : "";
								int id = prChg.getId() != null ? prChg.getId().intValueExact() : 0;
							}
							
							// Table style properties
							TblStyle tblStyle = tblPr.getTblStyle();
							if (tblStyle != null)
							{
								String tstyle = tblStyle.getVal();
							}
							
							// Column band size
							int colBandSz = tblPr.getTblStyleColBandSize() != null ? tblPr.getTblStyleColBandSize().getVal().intValueExact() : -1;
							
							// Row band size
							int rowBandSz = tblPr.getTblStyleRowBandSize() != null ? tblPr.getTblStyleRowBandSize().getVal().intValueExact() : -1;

							// Table width
							int w  = tblPr.getTblW() != null ? tblPr.getTblW().getW().intValueExact() : -1;
							
							//System.out.println(indent +  tbl.getClass().getName() + sb.toString() );
							
							System.out.println(indent + tbl.getClass().getName());
						}
						
						if (o instanceof org.docx4j.wml.Tr)
						{
							org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr)o;
							System.out.println(indent + tr.getClass().getName());
						}
						
						if (o instanceof org.docx4j.wml.Tc)
						{
							org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc)o;
							System.out.println(indent + tc.getClass().getName());
						}
						
						if (o instanceof org.docx4j.wml.Drawing)
						{
							org.docx4j.wml.Drawing dwg = (org.docx4j.wml.Drawing)o;
							
							if ( dwg.getAnchorOrInline().get(0) instanceof Anchor ) {
								
					            System.out.println(indent + dwg.getClass().getName() + " ENCOUNTERED w:drawing/wp:anchor " );
					            // That's all for now...

					        } else if ( dwg.getAnchorOrInline().get(0) instanceof Inline ) {
					        	
					        	// Extract w:drawing/wp:inline/a:graphic/a:graphicData/pic:pic/pic:blipFill/a:blip/@r:embed

					            Inline inline = (Inline )dwg.getAnchorOrInline().get(0);
					            // Inline drawing properties: distT, distB, distL, distR
					            long distB = inline.getDistB();
					            long distL = inline.getDistL();
					            long distR = inline.getDistR();
					            long distT = inline.getDistT();
					            
					            // Drawing extents
					            CTPositiveSize2D exSz = inline.getExtent();
					            long cx = exSz.getCx();
					            long cy = exSz.getCy();
					            
					            // Drawing effect extents
					            CTEffectExtent effectExtent = inline.getEffectExtent();
					            long exB= effectExtent.getB();
					            long exL= effectExtent.getL();
					            long exR= effectExtent.getR();
					            long exT= effectExtent.getT();

					            // Non-Visual Graphics Frame Properties
					            CTNonVisualDrawingProps drPr = inline.getDocPr();
					            long id = drPr.getId();
					            String drName = drPr.getName();

					            // Non Visual Graphic Frame Lock Properties
					            CTGraphicalObjectFrameLocking nvFrLckPr = inline.getCNvGraphicFramePr().getGraphicFrameLocks();
					            boolean noChange = nvFrLckPr.isNoChangeAspect();
					            boolean noDrillDown = nvFrLckPr.isNoDrilldown();
					            boolean noGrp = nvFrLckPr.isNoGrp();
					            boolean noMove = nvFrLckPr.isNoMove();
					            boolean noResize = nvFrLckPr.isNoResize();
					            boolean noSelect = nvFrLckPr.isNoSelect();

					            // Graphic Data Properties
					            GraphicData grData = inline.getGraphic().getGraphicData();
					            String uri = grData.getUri();

					            // Picture Properties
					            Pic pic = inline.getGraphic().getGraphicData().getPic();
					            
					            // Non-Visual Drawing Properties
					            CTPictureNonVisual nvPicPr = pic.getNvPicPr();
					            long picId = nvPicPr.getCNvPr().getId();
					            String picName = nvPicPr.getCNvPr().getName();
					            // Non-Visual Picture Properties
					            CTNonVisualPictureProperties cNvPicPr = nvPicPr.getCNvPicPr();
					            
					            // Blip Fill Properties
					            CTBlipFillProperties blipFillPr = pic.getBlipFill();
					            CTBlip blipPr = blipFillPr.getBlip();
					            String blipState = blipPr.getCstate().name();
					            // Embedded Image Relationship Id
					            String relId = blipPr.getEmbed();
					            
					            // Office Art Extension List Properties
					            CTOfficeArtExtensionList extLst = blipPr.getExtLst();
					            Iterator<CTOfficeArtExtension> iter = extLst.getExt().iterator();
					            while (iter.hasNext()) {
					            	CTOfficeArtExtension oaExt = iter.next();
					            	String oaExtUri = oaExt.getUri();
					             }
					            
					            // Blip Fill Stretch Properties
					            CTStretchInfoProperties stretchPr = blipFillPr.getStretch();
					            int stretchB = stretchPr.getFillRect().getB();
					            int stretchL = stretchPr.getFillRect().getL();
					            int stretchR = stretchPr.getFillRect().getR();
					            int stretchT = stretchPr.getFillRect().getT();
					            
					            // Picture Shape Properties
					            CTTransform2D trans2DPr = pic.getSpPr().getXfrm();
					            CTPoint2D pt2d = trans2DPr.getOff();
					            // Shape offset
					            long shpOffX = pt2d.getX();
					            long shpOffY = pt2d.getY();
					            CTPositiveSize2D posSz2d = trans2DPr.getExt();
					            // Shape size
					            long shpCX = posSz2d.getCx();
					            long shpCY = posSz2d.getCy();
					            
					            CTPresetGeometry2D presetGeomPr = pic.getSpPr().getPrstGeom();
					            // Preset Geometry Name
					            String prst = presetGeomPr.getPrst().name();
					            
					            // Extract the picture raw data
					            BinaryPartAbstractImage imgPart = (BinaryPartAbstractImage)docPart.getRelationshipsPart().getPart(relId);
					            byte[] imgData = imgPart.getBytes();
					            // Test by writing the image to a file
					            ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(imgData);	
					            BufferedImage imgBuf = null;
								try {
									imgBuf = ImageIO.read(byteArrayIn);
									
						            // Transform the image to png
						            File saveToFile = new File("c:\\temp\\"+drName+".png");
						            ImageIO.write(imgBuf,"png",saveToFile);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					            
								/*
					            // Or write the image to a byte array as png raw data
					            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
					            try {
									ImageIO.write(imgBuf, "png", byteArrayOut);
						            byte[] rawPngBytes = byteArrayOut.toByteArray();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								*/
					            
					            /*
					            File saveToFile = new File("c:\\temp\\"+drName+".jpg");
								try {
									FileOutputStream fos = new FileOutputStream(saveToFile);
									fos.write(imgData);
									fos.flush();
									convertToPNG(bais, fos, 600);
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								*/
					            
						        System.out.println(indent + dwg.getClass().getName() + " id=\"" + nvPicPr.getCNvPr().getId() + "\" name=\""+nvPicPr.getCNvPr().getName() + "\"");
					        }
						}

						
						return null;
					}
					//@Override
					public boolean shouldTraverse(Object o) {
						return true;
					}

					// Depth first
					//@Override
					public void walkJAXBElements(Object parent) {

						indent += "    ";

						List children = getChildren(parent);
						if (children != null) {

							for (Object o : children) {

								// if its wrapped in javax.xml.bind.JAXBElement, get its
								// value
								o = XmlUtils.unwrap(o);

								this.apply(o);

								if (this.shouldTraverse(o)) {
									walkJAXBElements(o);
								}
							}
						}

						indent = indent.substring(0, indent.length() - 4);
					}

					// @Override
					public List<Object> getChildren(Object o) {
						return TraversalUtil.getChildrenImpl(o);
					}

				}

				);
    	System.out.println("========================================");
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
}
