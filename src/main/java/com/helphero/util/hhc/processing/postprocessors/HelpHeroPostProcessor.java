package com.helphero.util.hhc.processing.postprocessors;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.lib.Validation;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessStarter;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.processing.DocumentPostProcessor;
import com.helphero.util.hhc.processing.IDocumentPostProcessor;
import com.helphero.util.hhc.util.ConfigHelper;
import com.helphero.util.hhc.util.DateHelper;

public class HelpHeroPostProcessor implements IDocumentPostProcessor {
	static Logger logger = Logger.getLogger(HelpHeroPostProcessor.class);
	
	private String inputFile;
	private String outputFile;
	private String xslFile;
	private String xslTreeFile;
	private String metadataOutfile;
	private boolean debug = false;
	private boolean useSaxon9 = true;
	private String preProcessedOutfileSuffix = ".presps.xml";
	private String preProcessedOutfile;
	private String finalOutfileSuffix = ".sps.xml";
	private String finalOutfile;
	private String tempTreeOutputFile;
	private String xslTreeInstanceFile;
	private ConfigHelper configHelper;

	public HelpHeroPostProcessor() {
	}
	
	public HelpHeroPostProcessor(String inputXhtmlFile, String xslFile)
	{
		this.setInputFile(inputXhtmlFile);
		this.setXslFile(xslFile);
	}

	public void process() throws TransformException
	{
		TransformerFactory tFactory = null;
		
    	// Apply an Xslt transformation to the input file
    	if (this.getXslFile() != null)
    	{
			try {
	
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
		
		    		Transformer transformer = tFactory.newTransformer(new StreamSource(this.getXslFile()));
		    		
		    		logger.info("Created transformer using XslFile="+this.getXslFile());
						
					try {
							transformer.transform(new StreamSource(this.getInputFile()), new StreamResult(new FileOutputStream(this.getOutputFile())));
							
							logger.info("Applied xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getOutputFile());
							
							this.sanitize();
							
					} catch (FileNotFoundException e) {
							throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getOutputFile(),e);
					} catch (IOException e) {
							throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getOutputFile(),e);					
					} catch (TransformerException e) {
							throw new TransformException("Failed to apply xslt transform " + this.getXslFile() + " to input file " + this.getInputFile() + " and outputing to " + this.getOutputFile(),e);					
					} catch (ParserConfigurationException e) {
						throw new TransformException("Failed to parse metadata output file " + this.metadataOutfile, e);					
					} catch (XPathExpressionException e) {
						throw new TransformException("Failed to parse metadata output file " + this.metadataOutfile, e);					
					} 
	    	} catch (TransformerConfigurationException e) {
					throw new TransformException("Failed to apply xslt transforms to input file " + this.getInputFile(), e);
			}
    	}
    	else
    	{
    		throw new TransformException("Failed to apply xslt transform to input file "+ this.getInputFile() + " because the xslt instance file does not exist");
    	}
	}
	
	private String getMetadataOutputFile()
	{
		return metadataOutfile;
	}
	
	private void sanitize() throws IOException, FileNotFoundException, TransformException, ParserConfigurationException, XPathExpressionException
	{	
		// Determine the file system path to images
		String pathToDocs = this.getInputFile().substring(0, this.getInputFile().lastIndexOf("\\"));
		logger.info("HelpHeroPostProcessor:sanitize: path to docs="+pathToDocs);
		
		String pathToImages = pathToDocs + File.separator + "_files";

		HashMap<String, HelpHeroImageInfo> imgMap = new HashMap<String, HelpHeroImageInfo>();
		HashMap<String, HelpHeroExdataInfo> exdataMap = new HashMap<String, HelpHeroExdataInfo>();
		
		List<String> peers = new ArrayList<String>();
		
		logger.info("Loading JSON Metadata file="+this.getMetadataOutputFile());
		
		Document metaDoc = loadAsXml(getMetadataOutputFile());
			
		XPath xPath =  XPathFactory.newInstance().newXPath();
			
		NodeList documents = (NodeList)xPath.compile("//document").evaluate(metaDoc, XPathConstants.NODESET);
		
		// Build a list of all peer documents to be added to the relationships peers entry
		for (int i = 0; i < documents.getLength(); i++) 
		{ 
			Node document = (Node)documents.item(i);
			
			String jsonDocName = document.getTextContent();
			
			Path path = Paths.get(jsonDocName);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			
			String pattern = "(\"self\"[ ]{0,}:[ ]{0,}\"[/].*.json\")";
			
			Pattern p = Pattern.compile(pattern);
			
			Matcher matcher = p.matcher(content);
			
			int sz = peers.size() + 1;
			while (matcher.find()) {
				String sPeer = "\"peer" + sz + "\"";
				String peer = matcher.group(1).replaceFirst("\"self\"", sPeer);
				peers.add(peer);
			}
		}
		
		for (int i = 0; i < documents.getLength(); i++) 
		{        	
			Node document = (Node)documents.item(i);
			
			// Extract the individual json documents from the output metadata file
			String jsonDocName = document.getTextContent();
			
			logger.info("Cleaning json for file="+jsonDocName);
		
			Path path = Paths.get(jsonDocName);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path), charset);
			
			// Clean up each json document file so ensure it is properly formed json
			// 1. Fix the single line split problem. 
			String pattern = "(\"htmltext\": \"[^\"]*)\n([^\"}]*)(\")";
			String replacement = "$1 $2$3";
			content = content.replaceAll(pattern, replacement);
			
			// 2. Fix the multi-line split problem
			pattern = "(\"htmltext\": \"[^\"]*)\n((.*)(\n))(([^\"}]*)(\"))";
			replacement = "$1 $3 $5";
			content = content.replaceAll(pattern, replacement);
			
			// 3. Repeat fix the single line split problem.
			pattern = "(\"htmltext\": \"[^\"]*)\n([^\"}]*)(\")";
			replacement = "$1 $2$3";
			content = content.replaceAll(pattern, replacement);
			
			// 4. Fix the Name line split problem
			pattern = "(\"Name\" : \"[^\"]*)\n([^\"}]*)(\",)";
			replacement = "$1 $2$3";
			content = content.replaceAll(pattern, replacement);
			
			// 5. Remove duplicate whitespace with a single whitespace
			pattern = "\\s{2,}";
			replacement = " ";
			content = content.replaceAll(pattern, replacement);
			
			// 6. Remove all trailing tabs and whitespace
			pattern = "\\s{1,}\"";
			replacement = "\"";
			content = content.replaceAll(pattern, replacement);
			
			// 7. Remove all blank lines
			content = content.replaceAll("(?m)^\\s", "");
			content = content.replaceAll("\\r\\n|\\r|\\n", " ");
			
			content = content.replaceAll("[ ]{0,1}xmlns=\"[^\"]{1,}\"", "");
			content = content.replaceAll("[ ]{0,1}xmlns:uuid=\"[^\"]{1,}\"", "");

			
			// Extract a unique list of images references from the json documents 
			pattern = "(\\[img width=[^\\]]*src='[^']*'\\])";
			
			Pattern p = Pattern.compile(pattern);
			
			Matcher matcher = p.matcher(content);
			
			while (matcher.find()) {
				String imgSrc = matcher.group(1);
				
				String srcRef = imgSrc.replaceAll(".*src='[/]([^']*).b64']", "$1");
				logger.info("img srcRef="+srcRef);
				
				if (!imgMap.containsKey(srcRef)) {
					HelpHeroImageInfo hhImgInfo = new HelpHeroImageInfo();

					hhImgInfo.setId(srcRef.replaceAll(".*[/]([^/]*)$","$1"));
					hhImgInfo.setOrganisation(srcRef.replaceAll("([^/]*)[/].*$","$1"));
					hhImgInfo.setSrcRef(srcRef);
					hhImgInfo.setFileRef(pathToImages + File.separator + hhImgInfo.getId());
					hhImgInfo.setHeight(imgSrc.replaceAll("^.*height='([^']*?)'.*$", "$1"));
					hhImgInfo.setWidth(imgSrc.replaceAll("^.*width='([^']*?)'.*$", "$1"));
					
					imgMap.put(srcRef, hhImgInfo);
					logger.info("Adding img="+hhImgInfo.getId()+ " to image HashMap");
				}
			}	
			
			pattern = "\"self\"[ ]{0,}:[ ]{0,}\"([/]([^/]*)[/].*[/]([^/]*).b64)\"";
			
			p = Pattern.compile(pattern);
			
			matcher = p.matcher(content);
				
			while (matcher.find()) {
				String exdataSrcRef = matcher.group(1);
				String org = matcher.group(2);
				String exdataId = matcher.group(3);
				
				if (!exdataMap.containsKey(exdataId)) {
					HelpHeroExdataInfo hhExdataInfo = new HelpHeroExdataInfo();

					hhExdataInfo.setId(exdataId);
					hhExdataInfo.setOrganisation(org);
					hhExdataInfo.setSrcRef(exdataSrcRef);
					hhExdataInfo.setFileRef(pathToImages + File.separator + hhExdataInfo.getId());
					
					exdataMap.put(exdataId, hhExdataInfo);
					logger.info("Adding exdata="+hhExdataInfo.getId()+ " to exdata HashMap");
				}
			}	
			
			// Add all the peer references
			pattern = "(\"self\"[ ]{0,}:[ ]{0,}\"[/].*.json\")";

			p = Pattern.compile(pattern);

			matcher = p.matcher(content);
			
			// Add the peer documents to the relationship block
			logger.info("Adding all peer references to document="+jsonDocName);
			if (matcher.find()) {
				String self = matcher.group(1);
				
				StringBuffer peerBuf = new StringBuffer();
				
				peerBuf.append(self);
				
				for (int j = 0; j < peers.size(); j++) {
					if (j != i) {
						peerBuf.append(", ");
						peerBuf.append(peers.get(j));
					}
				}
				
				content = content.replaceAll(self, peerBuf.toString());
				logger.info("Replacing peer="+self+" with "+peerBuf.toString());
			}
			
			// Write the cleaned up json to the json file
			Files.write(path, content.getBytes(charset));
		}
		
		// Create all the JSON Image files
		for (String key : imgMap.keySet()) {
			this.createJsonImage(imgMap.get(key));
		}
		
		// Create all the JSON External data files
		for (String key : exdataMap.keySet()) {
			this.createJsonExdata(exdataMap.get(key), pathToImages);
		}
	}
	
	/**
	 * Load the input file name and create an Document Builder instance
	 * @param inputFile Input file name
	 * @throws ParserConfigurationException Exception thrown if the intermediary input file fails to load
	 */
	private Document loadAsXml(String inputFile) throws ParserConfigurationException
	{	
		Document document = null;
		// Create a new DocumentBuilderFactory
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    
     	// At this stage
 		factory.setNamespaceAware(false);
 		factory.setValidating(false);
 		factory.setSchema(null);
 		// Disable any DTD validation
 		factory.setFeature("http://xml.org/sax/features/namespaces", false);
 		factory.setFeature("http://xml.org/sax/features/validation", false);
 		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
 		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 		
	    try {
	         // Use the factory to create a DocumentBuilder instance
	         DocumentBuilder builder = factory.newDocumentBuilder();
	         
		     // Create a new W3C document from the input source
	         document = builder.parse(inputFile);
	      } catch (Exception ex) {
	         ex.printStackTrace();
	      }	
	    
	    return document;
	}
	
	private void createJsonImage(HelpHeroImageInfo hhImgInfo)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		logger.info("Create JSON file for image file="+hhImgInfo.getFileRef());
        try {
			BufferedImage imgBuf = ImageIO.read(new File(hhImgInfo.getFileRef()));
			
	        if (!hhImgInfo.getFileRef().toLowerCase().endsWith("emf") || imgBuf != null) {
	        	// This handles the basic jpeg, png, bmp, gif and wbmp image formats converting them to png
		        try {
		            ImageIO.write(imgBuf, "png", bos);
		            byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());

		            bos.close();
		            
		            hhImgInfo.setData(new String(imageBytes));
		            
		        } catch (IOException e) {
		            // Attempt to convert using the img4java library wrapper around ImageMagick
		        	logger.info("Failed to convert image using ImageIO. Will attempt to convert using img4java library wrapper for ImageMagick", e);

					try {
			        	String pngImgDest = this.convertImageUsingImageMagickWrapper(hhImgInfo.getFileRef());
			        	
						imgBuf = ImageIO.read(new File(pngImgDest));
						bos = new ByteArrayOutputStream();

				        ImageIO.write(imgBuf, "png", bos);
				        byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());
	
				        bos.close();
				            
			            hhImgInfo.setData(new String(imageBytes));
			            				            
				    } catch (IM4JavaException e1) {
				        logger.error("Failed to convert image using img4java library", e1);
				            
				        hhImgInfo.setData("");
				            
				    } catch (InterruptedException e1) {
				        logger.error("Failed to convert image using img4java library", e1);
				            
				        hhImgInfo.setData("");
				     								        
				    } catch (IOException e1) {
				        logger.error("Failed to convert image using img4java library", e1);
				            
				        hhImgInfo.setData("");
				            
				    }
		        }					        
	        } 
	        else
	        {
	        	// This handles Vector Graphics image formats converting them to png using the img4java wrapper library around ImageMagick
	        	logger.info("\t\tInput Image File "+hhImgInfo.getFileRef());
	        	 
				try {
		        	String pngImgDest = this.convertImageUsingImageMagickWrapper(hhImgInfo.getFileRef());
		        	
					imgBuf = ImageIO.read(new File(pngImgDest));
					bos = new ByteArrayOutputStream();

			        ImageIO.write(imgBuf, "png", bos);
			        byte[] imageBytes = Base64.encodeBase64(bos.toByteArray());

			        bos.close();
			            
			        hhImgInfo.setData(new String(imageBytes));
			            
				} catch (IM4JavaException e) {
			        logger.error("Failed to convert image using img4java library", e);							            
			        hhImgInfo.setData("");

				} catch (InterruptedException e) {
			        logger.error("Failed to convert image using img4java library", e);							            
			        hhImgInfo.setData("");
									
			    } catch (IOException e) {
			        logger.error("Failed to convert image using img4java library", e);							            
			        hhImgInfo.setData("");
			    }
	        }
	        
	        String outfile = hhImgInfo.getOrganisation() + File.separator + "images" + File.separator + hhImgInfo.getId() + ".json";
	        
	        File imagesDir = new File(hhImgInfo.getOrganisation() + File.separator + "images");
	        
	        if (!imagesDir.exists()) {
	        	if (imagesDir.mkdir())
	        		logger.info("Created directory: "+imagesDir);
	        }	        

	        FileWriter writer = new FileWriter(outfile);
	        
	        writer.write(this.getImageJson(hhImgInfo));
	        writer.flush();
	        writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createJsonExdata(HelpHeroExdataInfo exdataInfo, String pathToImages)
	{
		
		logger.info("Create JSON file for exdata file="+exdataInfo.getFileRef());
		String jsonData = this.getExdataJson(exdataInfo, pathToImages);	
		
		// Relative path. Relative to where the converter is executed
		String jsonFile = exdataInfo.getOrganisation() + File.separator + "exdata" + File.separator + exdataInfo.getId()+".json";		
        
        File exdataDir = new File(exdataInfo.getOrganisation() + File.separator + "exdata");
        
        if (!exdataDir.exists()) {
        	if (exdataDir.mkdir())
        		logger.info("Created directory: "+exdataDir);
        }	
		
		// Write to the json file
        FileWriter writer;
		try {
			writer = new FileWriter(jsonFile);
			
	        writer.write(jsonData);
	        writer.flush();
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}        
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
    	ProcessStarter.setGlobalSearchPath(configHelper.getProperty("ImageMagickPath"));
    	
    	// create command
    	ConvertCmd cmd = new ConvertCmd();
    	
    	// create the operation, add images and operators/options
    	IMOperation op = new IMOperation();
    	String imgSrc1 = imgSrc.replaceAll("/", "\\\\");
    	
    	op.addImage(imgSrc1); // source file
    	    	
    	String pngImgDest = imgSrc1.substring(0, imgSrc1.lastIndexOf(".")) + ".png";
    	
    	op.addImage(pngImgDest); // destination file
    	
    	// execute the operation
		cmd.run(op);				
    	
		return pngImgDest;
	}

	
	private String getImageJson(HelpHeroImageInfo hhImgInfo)
	{
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\n");
        sb.append("\t\"id\": \""+hhImgInfo.getId()+"\",");
        sb.append("\n");
        sb.append("\t\"width\": \""+hhImgInfo.getWidth()+"\",");
        sb.append("\n");
        sb.append("\t\"height\": \""+hhImgInfo.getHeight()+"\",");
        sb.append("\n");
        sb.append("\t\"src\": \""+hhImgInfo.getData()+"\"");
        sb.append("\n");
        sb.append("}");
        
        return sb.toString();
	}
	
	private String getExdataJson(HelpHeroExdataInfo hhExdataInfo, String pathToImages)
	{
    	File file = new File(pathToImages + File.separator + hhExdataInfo.getId());
    	
    	FileInputStream fis = null;
    	
    	Throwable throwable = null;
        String encodedMsg = null;
        try {
            fis = new FileInputStream(file);
            
            byte[] bytes = new byte[(int)file.length()];
            
            fis.read(bytes);
            
            encodedMsg = new String(Base64.encodeBase64(bytes));            
            
            fis.close();
            		                
        } catch (FileNotFoundException e) {
	        logger.error("Failed to convert exdata file="+file.getPath(), e);
            
	        encodedMsg = "";
	        
	        throwable = e;
	        
        } catch (IOException e) {
	        logger.error("Failed to convert exdata file="+file.getPath(), e);
	        
	        encodedMsg = "";
	        
	        throwable = e;
            
        } finally {
        	if (throwable != null)
        	{
        		try {
        			fis.close();
        		} catch (IOException e) {
        			logger.warn("Failed to convert exdata file="+file.getPath(), e);
        		}
        	}
        }
        
        hhExdataInfo.setData(new String(encodedMsg));

        StringBuffer sb = new StringBuffer();
        
        sb.append("{");
        sb.append("\n");
        sb.append("\t\"id\": \""+hhExdataInfo.getId()+"\",");
        sb.append("\n");
        sb.append("\t\"type\": \"external\",");
        sb.append("\n");
        sb.append("\t\"data\": \""+hhExdataInfo.getData()+"\"");
        sb.append("\n");
        sb.append("}");
        
        return sb.toString();
	}

	public void setInputFile(String inputXhtmlFile) {
		this.inputFile = inputXhtmlFile;
		
		if (inputXhtmlFile.endsWith(".biz.xhtml"))
		{
			this.metadataOutfile = inputXhtmlFile.substring(0, inputXhtmlFile.indexOf(".biz.xhtml")) + ".meta.xml";
		}
		else
		{
			this.metadataOutfile = inputXhtmlFile + ".meta.xml";
		}
	}

	public String getInputFile() {
		return this.inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public String getOutputFile()
	{
		return this.outputFile;
	}

	public void setXslFile(String xslFile) {
		this.xslFile = xslFile;
	}
	
	public String getXslFile() {
		return this.xslFile;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setConfigHelper(ConfigHelper configHelper) {
		this.configHelper = configHelper;
	}

	/**
	 * Not used
	 */
	public void setXslTreeFile(String spsTreeXsl) {
	}

	/**
	 * Not used
	 */
	public void setPreProcessedOutfile(String spsPreProcessedOutfile) {
		this.setOutputFile(spsPreProcessedOutfile);
	}

	/**
	 * Not used
	 */
	public String getPreProcessedOutfile() {
		return getOutputFile();
	}

	/**
	 * Not used
	 */
	public void setFinalOutfile(String finalOutfile) {
		// TODO Auto-generated method stub
	}

	/**
	 * Not used
	 */
	public String getFinalOutfile() {
		return null;
	}

	/**
	 * Not used
	 */
	public void setTempTreeOutputFile(String treeOutputFile) {
	}

	/**
	 * Not used
	 */
	public void setXslTreeInstanceFile(String treeXslInstanceFile) {
	}
}
