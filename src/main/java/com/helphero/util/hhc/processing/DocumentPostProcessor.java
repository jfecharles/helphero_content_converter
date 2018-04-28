package com.helphero.util.hhc.processing;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.saxon.lib.Validation;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.util.ConfigHelper;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class manages all the details to convert a business filtered and transformed common object model (xHtml) document instance
 * to the final SupportPoint importable xml output file.
 * 
 * @author jcharles
 */
public class DocumentPostProcessor implements IDocumentPostProcessor {
	static Logger logger = Logger.getLogger(DocumentPostProcessor.class);
	
	private String inputFile;
	private String outputFile;
	private String xslFile;
	private String xslTreeFile;
	private boolean debug = false;
	private boolean useSaxon9 = true;
	private String preProcessedOutfileSuffix = ".presps.xml";
	private String preProcessedOutfile;
	private String finalOutfileSuffix = ".sps.xml";
	private String finalOutfile;
	private String tempTreeOutputFile;
	private String xslTreeInstanceFile;
	private ConfigHelper configHelper;

	public DocumentPostProcessor() {
	}
	
	/**
	 * Constructor
	 * @param inputXhtmlFile Input xHtml file
	 * @param xslFile XSLT transformation file
	 */
	public DocumentPostProcessor(String inputXhtmlFile, String xslFile) {
		this.setInputFile(inputXhtmlFile);
		this.setXslFile(xslFile);
		this.setPreProcessedOutfile(inputXhtmlFile + this.getPreProcessedOutfileSuffix());
		this.setFinalOutfile(inputXhtmlFile + this.getFinalOutfileSuffix());
		this.setTempTreeOutputFile(inputXhtmlFile.substring(0, inputXhtmlFile.lastIndexOf('/')) + "/" + "output.tree.xml");
		// This is only ever used if the user does not specify an sps tree xsl on the command line
		this.setXslTreeInstanceFile(inputXhtmlFile.substring(0, inputXhtmlFile.lastIndexOf('/')) + "/" + "sps.tree.xsl");
	}
	
	/**
	 * Main method to execute the final stage document specific conversion.
	 */
	public void process() throws TransformException {
		TransformerFactory tFactory = null;
		
    	// Apply an Xslt transformation to the input file
    	if (this.getXslFile() != null)
    	{
    		if (useSaxon9)
    		{
    			tFactory = new net.sf.saxon.TransformerFactoryImpl();

    			net.sf.saxon.Configuration fConfig = ((net.sf.saxon.TransformerFactoryImpl)tFactory).getConfiguration();
                //parse dtds and give warnings if asked
                fConfig.setValidation(false);
                fConfig.setValidationWarnings(false);
                fConfig.setSchemaValidationMode(Validation.STRIP);
    		}
    		else
    		{
	    		tFactory = TransformerFactory.newInstance();
    		}

    		Transformer transformer, treeTransformer = null;
			try {
				// If the XslTree file is not specified on the command line extract it from the CLASSPATH as a resource
				if (this.getXslTreeFile().contains("resources/")) {
					
					logger.info("Extracting resource file "+this.getXslTreeFile()+" from CLASSPATH and copying to "+this.getXslTreeInstanceFile());
					
					File destFile = new File(this.getXslTreeInstanceFile());
					File srcFile = null;
									
					if (!destFile.exists()) {
						
						URL url = this.getClass().getClassLoader().getResource(this.getXslTreeFile());
						
						if (url != null)
							srcFile = new File(url.getFile());	
						
						if (srcFile != null && srcFile.exists()) {
					
							try {
								FileUtils.copyFile(srcFile, destFile);
								logger.info("Extracted resource file "+this.getXslTreeFile()+" from CLASSPATH and copied to "+this.getXslTreeInstanceFile());
								treeTransformer = tFactory.newTransformer(new StreamSource(this.getXslTreeInstanceFile()));
							} catch (IOException ex) {
								String sMsg = "Failed to copy resource file "+this.getXslTreeFile()+" to "+this.getXslTreeInstanceFile();
								logger.error(sMsg+" : "+ex.getMessage());
								throw new TransformException(sMsg, ex);
							}
							
						} else {
							// Use the sps-tree.v*.xsl file in the {current-working-directory}/resources folder
							srcFile = new File(System.getProperty("user.dir")+File.separator+File.separator+this.getXslTreeFile().replace("/",File.separator));
							
							if (srcFile != null && srcFile.exists()) {
								try {
									FileUtils.copyFile(srcFile, destFile);
									logger.info("Extracted resource file "+this.getXslTreeFile()+" from the current working directory resources sub-directory and copied to "+this.getXslTreeInstanceFile());
								} catch (IOException ex) {
									String sMsg = "Failed to copy file "+System.getProperty("user.dir")+File.separator+"resources"+File.separator+this.getXslTreeFile()+" to "+this.getXslTreeInstanceFile();
									logger.error(sMsg+" : "+ex.getMessage());
									throw new TransformException(sMsg, ex);
								}
							}
							else
							{
								logger.error("Non-existant tree xsl file : "+srcFile.getPath());
								throw new TransformException("Non-existant tree xsl resource : "+srcFile.getPath());
							}
						}
					}
					
					treeTransformer = tFactory.newTransformer(new StreamSource(this.getXslTreeInstanceFile()));		
					
				} else {
					// Use the command line referenced tree xsl file. A instance specific tree xsl file is not required in the target folder.
					treeTransformer = tFactory.newTransformer(new StreamSource(this.getXslTreeFile()));
				}
				
				transformer = tFactory.newTransformer(new StreamSource(this.getXslFile()));
				
				try {
					treeTransformer.transform(new StreamSource(this.getInputFile()), new StreamResult(new FileOutputStream(this.getTempTreeOutputFile())));
					
					logger.info("Applied xslt tree transform " + this.getXslTreeFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getTempTreeOutputFile());
					
				} catch (FileNotFoundException e) {
					throw new TransformException("Failed to apply xslt tree transform " + this.getXslTreeFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getTempTreeOutputFile(), e);
				} catch (TransformerException e) {
					throw new TransformException("Failed to apply xslt tree transform " + this.getXslTreeFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getTempTreeOutputFile(), e);
				}
				
				File outfile = new File(this.getTempTreeOutputFile());
				if (!outfile.exists())
				{
					logger.info("Temporary Output Tree File "+ this.getTempTreeOutputFile() + " does not exist!");
				} 
				
				try {
					transformer.transform(new StreamSource(this.getInputFile()), new StreamResult(new FileOutputStream(this.getPreProcessedOutfile())));
					
					logger.info("Applied xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getPreProcessedOutfile());
					
					this.sanitize();
					
				} catch (FileNotFoundException e) {
					throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getPreProcessedOutfile(),e);
				} catch (IOException e) {
					throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getPreProcessedOutfile(),e);					
				} catch (TransformerException e) {
					throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getPreProcessedOutfile(),e);					
				}
			} catch (TransformerConfigurationException e) {
				throw new TransformException("Failed to apply xslt transforms to input file " + this.getInputFile(), e);
			}
    	}
	}
	
	/**
	 * Clean up the XSLT 2.0 generated xml to ensure the document is able to be imported into SupportPoint. 
	 * This includes the following:
	 * - Convert all less than and greater than to symbols
	 * - Inline all image element references inside the parent objects element. 
	 * - Filter out all xHtml namespace declarations
	 * - Remove all non-printable characters
	 * - Replace all less than @ less than and greater than @ greater then symbol references generated by the xls transformation with a single less than and greater than symbol. This was used to protect against double less than or greater than references in word documents.
	 * - Convert all "emf", "wmf" (vector graphic) image file references to png using the ImageMagick library.
	 * @throws IOException Exception thrown while processing final stage xml file
	 * @throws FileNotFoundException Exception thrown if the input file does not exist
	 */
	private void sanitize() throws IOException, FileNotFoundException
	{
		File inFile = new File(this.getPreProcessedOutfile());
		
		// Only process the input file if it exists
		if (inFile.exists())
		{		
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.getPreProcessedOutfile()), "UTF-8" ) );
				
				final StringBuilder contents = new StringBuilder();
				
				try {
					while(reader.ready()) {
						contents.append(reader.readLine());
					}
	
					reader.close();
					
					String stringContents = contents.toString();
					
					// Apply a non-greedy replacement of xml elements wrapped in a &lt;element&gt; wrapper
					// The wrapping of elements with &lt;@&lt; .... &gt;@&gt; is used to protect against 
					// word documents containing << ... >> strings. The following regex removes this from the xslt generated xml.
					stringContents = stringContents.replaceAll("&lt;@&lt;([^.]*?)&gt;@&gt;", "<$1>");
					
					// Remove the DOCTYPE reference
					if (stringContents.contains("<!DOCTYPE export  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"))
						stringContents = stringContents.replaceAll("<!DOCTYPE export  PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", "");
					
					// Remove the namespace "xmlns" from the export element
					if (stringContents.contains("<export xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:uuid=\"java:java.util.UUID\""))
						stringContents = stringContents.replaceAll("<export xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:uuid=\"java:java.util.UUID\"", "<export");
					
					// remove all the control characters but not carriage return, line-feed and tab
					// stringContents = stringContents.replaceAll("[\\p{Cc}&&[^\r\n\t]]", "");
					
					// remove all non-printable characters
					stringContents = stringContents.replaceAll("\\P{Print}", "");
					
					String patternString = "<(image|exdata) id=(\"[^\"]+?\") src=\"([^\"]+?)\" name=\"([^\"]+?)\"/>";
					Pattern pattern = Pattern.compile(patternString);
					
					Matcher matcher = pattern.matcher(stringContents);
					
					StringBuffer newBuf = new StringBuffer();
					
					// Iterate through the images
					int count = 0;
					int start = -1;
					int end = -1;
					int prevEnd = -1;
					while (matcher.find())
					{
						count++;
						
						String elemType = matcher.group(1);
						String elemId = matcher.group(2);
						String elemSrc = matcher.group(3);
						String elemName = matcher.group(4);
						
						start = matcher.start();
						end = matcher.end();
						
						if (count == 1)
						{
							// Copy the first segment if it is not an image declaration
							if (start > 0)
								newBuf.append(stringContents.substring(0, start));
						}
						else
						{
							// Copy all relevant contents between image declarations
							if (start > prevEnd)
								newBuf.append(stringContents.substring(prevEnd, start));
						}
						
				        if (elemType.equalsIgnoreCase("image")) {

					        ByteArrayOutputStream bos = new ByteArrayOutputStream();
					        BufferedImage imgBuf = ImageIO.read(new File(elemSrc));
		
					        if (!elemSrc.toLowerCase().endsWith("emf") || imgBuf != null) {
					        	// This handles the basic jpeg, png, bmp, gif and wbmp image formats converting them to png
						        try {
						            ImageIO.write(imgBuf, "png", bos);
						            byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());
			
						            bos.close();
						            
						            String encodedImg = new String(imageBytes);
						            
						            newBuf.append(this.getImageXml(elemId, elemName, encodedImg));
						            
						            prevEnd = end;
						            
						        } catch (IOException e) {
						            // Attempt to convert using the img4java library wrapper around ImageMagick
						        	logger.info("Failed to convert image using ImageIO. Will attempt to convert using img4java library wrapper for ImageMagick", e);

									try {
							        	String pngImgDest = this.convertImageUsingImageMagickWrapper(elemSrc);
							        	
										imgBuf = ImageIO.read(new File(pngImgDest));
										bos = new ByteArrayOutputStream();
	
								        ImageIO.write(imgBuf, "png", bos);
								        byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());
					
								        bos.close();
								            
								        String encodedImg = new String(imageBytes);
								            
								        newBuf.append(this.getImageXml(elemId, elemName, encodedImg));
								            
								        prevEnd = end;
								    } catch (IM4JavaException e1) {
								        logger.error("Failed to convert image using img4java library", e1);
								            
								        newBuf.append(this.getImageXml(elemId, elemName, ""));
								            
								        prevEnd = end;
								    } catch (InterruptedException e1) {
								        logger.error("Failed to convert image using img4java library", e1);
								            
								        newBuf.append(this.getImageXml(elemId, elemName, ""));
								            
								        prevEnd = end;								        
								    } catch (IOException e1) {
								        logger.error("Failed to convert image using img4java library", e1);
								            
								        newBuf.append(this.getImageXml(elemId, elemName, ""));
								            
								        prevEnd = end;
								    }
						        }					        
					        } 
					        else
					        {
					        	// This handles Vector Graphics image formats converting them to png using the img4java wrapper library around ImageMagick
					        	logger.info("\t\tInput Image File "+elemSrc);
					        	 
								try {
						        	String pngImgDest = this.convertImageUsingImageMagickWrapper(elemSrc);
						        	
									imgBuf = ImageIO.read(new File(pngImgDest));
									bos = new ByteArrayOutputStream();
	
							        ImageIO.write(imgBuf, "png", bos);
							        byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());
				
							        bos.close();
							            
							        String encodedImg = new String(imageBytes);
							            
							        newBuf.append(this.getImageXml(elemId, elemName, encodedImg));
							            
							        prevEnd = end;
								} catch (IM4JavaException e) {
							        logger.error("Failed to convert image using img4java library", e);							            
							        newBuf.append(this.getImageXml(elemId, elemName, ""));							            
							        prevEnd = end;
								} catch (InterruptedException e) {
							        logger.error("Failed to convert image using img4java library", e);							            
							        newBuf.append(this.getImageXml(elemId, elemName, ""));							            
							        prevEnd = end;									
							    } catch (IOException e) {
							        logger.error("Failed to convert image using img4java library", e);							            
							        newBuf.append(this.getImageXml(elemId, elemName, ""));							            
							        prevEnd = end;
							    }
					        }
				        
				        } else if (elemType.equalsIgnoreCase("exdata")) {
				        	
				        	// Process the external data files converting them to base64
				        	File file = new File(elemSrc);
				        	
				            String encodedMsg = null;
				            try {
				                FileInputStream fis = new FileInputStream(file);
				                
				                byte[] bytes = new byte[(int)file.length()];
				                
				                fis.read(bytes);
				                
				                encodedMsg = new String(Base64.encodeBase64(bytes));
				                
				                newBuf.append(this.getExdataXml(elemId, elemName, encodedMsg));
				                
				                fis.close();
				                
				                prevEnd = end;				                
				            } catch (FileNotFoundException e) {
						        logger.error("Failed to convert exdata id="+elemId, e);
				                
				                // If there are any errors just blank out the base64 encoded content for the external data 
				                newBuf.append(this.getExdataXml(elemId, elemName, ""));	
				                
				                prevEnd = end;
				            } catch (IOException e) {
						        logger.error("Failed to convert exdata id="+elemId, e);
				                
				                // If there are any errors just blank out the base64 encoded content for the external data
				                newBuf.append(this.getExdataXml(elemId, elemName, ""));	
				                
				                prevEnd = end;
				            }				        
				        }
				        
					} // while
					
					if (count > 0)
					{
						// Only copy the last segment if it is not an image declaration
						if (end < stringContents.length())
						{
							newBuf.append(stringContents.substring(end));
						}
					}
					else
					{
						// No image declarations
						newBuf.append(stringContents);
					}
					
					
					/*
					 * The following code manipulates the final XML output string moving any exdata elements 
					 * referenced in the business filtered .biz.xhtml file as <div class="Document" target_type="external" />
					 * to the <objects> element. This is done using regular expressions and string manipulation because SupportPoint XML
					 * is not well formed. 
					 */
					// Match on any exdata elements
					stringContents = newBuf.toString();
					patternString = "<exdata.*?</exdata>"; // Non-greedy match
					pattern = Pattern.compile(patternString);					
					matcher = pattern.matcher(stringContents);
					
					newBuf = new StringBuffer();
					StringBuffer exdataBuf = new StringBuffer();
					// Iterate through the external data references
					count = 0;
					start = -1;
					end = -1;
					prevEnd = -1;
					while (matcher.find())
					{
						count++;
											
						start = matcher.start();
						end = matcher.end();
						
						if (count == 1)
						{
							// Copy the first segment if it is not an image declaration
							if (start > 0)
								newBuf.append(stringContents.substring(0, start));
						}
						else
						{
							// Copy all relevant contents between image declarations
							if (start > prevEnd)
								newBuf.append(stringContents.substring(prevEnd, start));
						}
						
						// Concatenate all exdata elements into one string
						exdataBuf.append(stringContents.substring(start, end));
						prevEnd = end;
					}
					
					if (count > 0)
					{
						if (end < stringContents.length())
						{
							newBuf.append(stringContents.substring(end));
							
							if (exdataBuf.length() > 0) {

								stringContents = newBuf.toString();

								if (stringContents.contains("<objects>")) {
									// Add the exdata elements content to the correct place in the SPS XML output
									stringContents = stringContents.replace("<objects>", "<objects>"+exdataBuf.toString());
									
									newBuf = new StringBuffer();
									newBuf.append(stringContents);
								} else { // If there is no objects element then add one
									// Add the exdata elements content to the correct place in the SPS XML output
									stringContents = stringContents.replace("</export>", "<objects>"+exdataBuf.toString()+"</objects></export>");
									
									newBuf = new StringBuffer();
									newBuf.append(stringContents);
								}
							}
						}
					}
					else
					{
						// No external data declarations
						newBuf.append(stringContents);
					}
					
			        final BufferedWriter writer = new BufferedWriter(new FileWriter(this.getFinalOutfile()));
	
			        writer.write(newBuf.toString());
			        writer.close();
	
				} catch (IOException e) {
			        logger.error("Failed to write to file="+this.getFinalOutfile(), e);					
				}
				
			} catch (FileNotFoundException e) {
		        logger.error("Failed to read file="+this.getPreProcessedOutfile(), e);
			}
		}
	}
	
	/**
	 * Private convenience method to return a SupportPoint xml specific envelope for an image reference.  
	 * @param imgId Image id
	 * @param imgName Image name
	 * @param encodedImg Encoded image
	 * @return String SupportPoint XML element envelope for an image 
	 */
	private String getImageXml(String imgId, String imgName, String encodedImg)
	{
        StringBuffer sb = new StringBuffer();
        sb.append("<image id=");
        sb.append(imgId);
        sb.append(" encoding=\"base64\">");
        sb.append("<name>");
        sb.append(imgName);
        sb.append("</name>");
        sb.append(encodedImg);
        sb.append("</image>");
        
        return sb.toString();
	}
	
	/**
	 * Private convenience method to return a SupportPoint xml specific envelope for an external object reference.
	 * @param elemId Element id
	 * @param elemName Element name
	 * @param encodedSrc Encoded image data
	 * @return String SupportPoint XML element envelope for an external document
	 */
	private String getExdataXml(String elemId, String elemName, String encodedSrc)
	{
        StringBuffer sb = new StringBuffer();
        sb.append("<exdata id=");
        sb.append(elemId);
        sb.append(" encoding=\"base64\">");
        sb.append("<name>");
        sb.append(elemName);
        sb.append("</name>");
        sb.append(encodedSrc);
        sb.append("</exdata>");
        
        return sb.toString();
	}
	
	/**
	 * Private Method to convert EMF and WMF Vector Graphics images to png using the img4java library wrapper around 
	 * the ImageMagick convert executable. 
	 * If successful it returns the non-null full pathname of the png generated file.
	 * @param imgSrc Source location for the vector graphics image
	 * @return pngImgDest Converted png image destination 
	 * @throws IOException Exception thrown if an error occurs converting the vector graphics image to png
	 * @throws InterruptedException Exception thrown if the conversion process is interrupted 
	 * @throws IM4JavaException Exception thrown by the ImageMagick wrapper class if an error occurs calling the ImageMagick converter.exe to convert from a vector graphics image to a png
	 */
	private String convertImageUsingImageMagickWrapper(String imgSrc) throws IOException, InterruptedException, IM4JavaException
	{
		String encodedImg = "";
		
    	// String myPath="C:\\Program Files\\ImageMagick-7.0.1-Q16";
		String sImageMagickPath = configHelper.getProperty("ImageMagickPath");
		
    	ProcessStarter.setGlobalSearchPath(configHelper.getProperty("ImageMagickPath"));
    	
    	// create command
    	ConvertCmd cmd = new ConvertCmd();
    	
    	// create the operation, add images and operators/options
    	IMOperation op = new IMOperation();
    	String imgSrc1 = imgSrc.replaceAll("/", "\\\\");
    	
    	op.addImage(imgSrc1); // source file
    	    	
    	String pngImgDest = imgSrc1.substring(0, imgSrc1.lastIndexOf(".")) + ".png";
    	
    	op.addImage(pngImgDest); // destination file file
    	
    	// execute the operation
		cmd.run(op);				
    	
		return pngImgDest;
	}
	
	/**
	 * Get the input file name
	 * @return String
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * Set the input file name
	 * @param inputFile Input File
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Get the output file name
	 * @return outputFile Output File name
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Set the output file name
	 * @param outputFile Output file name
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Get the common xsl file name
	 * @return xslFile Common XSLT file name
	 */
	public String getXslFile() {
		return xslFile;
	}

	/**
	 * Set the common xsl fle name
	 * @param xslFile Common XSLT file name
	 */
	public void setXslFile(String xslFile) {
		this.xslFile = xslFile;
	}

	/**
	 * Get the pre-processed file suffix
	 * @return preProcessedOutfileSuffix Pre-Processed Output file name suffix
	 */
	public String getPreProcessedOutfileSuffix() {
		return preProcessedOutfileSuffix;
	}

	/**
	 * Set the pre-processed file suffix
	 * @param preProcessedOutfileSuffix Pre-Processed Output file name suffix
	 */
	public void setPreProcessedOutfileSuffix(String preProcessedOutfileSuffix) {
		this.preProcessedOutfileSuffix = preProcessedOutfileSuffix;
	}

	/**
	 * Get the final output file suffix
	 * @return finalOutfileSuffix Final output file name suffix
	 */
	public String getFinalOutfileSuffix() {
		return finalOutfileSuffix;
	}

	/**
	 * set the final output file suffix
	 * @param finalOutfileSuffix Final output file name suffix
	 */
	public void setFinalOutfileSuffix(String finalOutfileSuffix) {
		this.finalOutfileSuffix = finalOutfileSuffix;
	}

	/**
	 * Get the pre-processed output file name
	 * @return preProcessedOutfile Pre-processed output file name
	 */
	public String getPreProcessedOutfile() {
		return preProcessedOutfile;
	}

	/**
	 * Set the pre-processed output file name
	 * @param preProcessedOutfile Pre-processed output file name
	 */
	public void setPreProcessedOutfile(String preProcessedOutfile) {
		this.preProcessedOutfile = preProcessedOutfile;
	}

	/**
	 * Get the final output file name
	 * @return finalOutfile Final output file name
	 */
	public String getFinalOutfile() {
		return finalOutfile;
	}

	/**
	 * Set the final output file name
	 * @param finalOutfile Final output file name
	 */
	public void setFinalOutfile(String finalOutfile) {
		this.finalOutfile = finalOutfile;
	}

	/**
	 * Get the common xsl tree file name
	 * @return xslTreeFile Common xsl tree file name
	 */
	public String getXslTreeFile() {
		return xslTreeFile;
	}

	/**
	 * Set the common xsl tree file name
	 * @param xslTreeFile Common xsl tree file name
	 */
	public void setXslTreeFile(String xslTreeFile) {
		this.xslTreeFile = xslTreeFile;
	}
	
	/**
	 * Get the document instance specific temporary output tree file name. This is generated by applying the xslt tree file to the xsl generated output file
	 * as part of the document instance post processing stage. 
	 * @return tempTreeOutputFile Temporary output tree file name
	 */
	public String getTempTreeOutputFile()
	{
		return this.tempTreeOutputFile;
	}
	
	/**
	 * Set the document instance specific temporary output tree file name. This is generated by applying the xslt tree file to the xsl generated output file
	 * as part of the document instance post processing stage.
	 * @param tempTreeOutputFile Temporary output tree file name
	 */
	public void setTempTreeOutputFile(String tempTreeOutputFile) {
		this.tempTreeOutputFile = tempTreeOutputFile;
	}

	/** 
	 * Get the configuration helper object containing all ini paramter details
	 * @return configHelper configuration helper object containing all ini paramter details
	 */
	public ConfigHelper getConfigHelper() {
		return configHelper;
	}

	/**
	 * Set the configuration helper object containing all ini paramter details
	 * @param configHelper Configuration helper object containing all ini paramter details
	 */
	public void setConfigHelper(ConfigHelper configHelper) {
		this.configHelper = configHelper;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Get the document instance specific xslt tree file
	 * @return xslTreeInstanceFile Document instance specific xslt tree file
	 */
	public String getXslTreeInstanceFile() {
		return xslTreeInstanceFile;
	}

	/**
	 * Set the document instance specific xslt tree file
	 * @param xslTreeInstanceFile Document instance specific xslt tree file
	 */
	public void setXslTreeInstanceFile(String xslTreeInstanceFile) {
		this.xslTreeInstanceFile = xslTreeInstanceFile;
	}
}
