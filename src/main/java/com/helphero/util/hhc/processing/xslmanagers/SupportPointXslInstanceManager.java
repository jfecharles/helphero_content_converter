package com.helphero.util.hhc.processing.xslmanagers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.dom.processing.PartitionType;
import com.helphero.util.hhc.processing.FileConverter;
import com.helphero.util.hhc.util.FileHelper;

public class SupportPointXslInstanceManager implements IXslInstanceManager {
	static Logger logger = Logger.getLogger(FileConverter.class);
	private File inputFile;						// Document to be processed
	private String spsXsl; 						// Original template XSL
	private String spsTreeXsl; 					// Common XSL file used to parse the business rules filtered XHTML file.
												// The output of this is a document specific xml folder tree file.
												// This document is referenced in the document specific xsl file allowing the tree nodes
												// to be directly included into the final output SupportPoint document specific xml.	
	private String spsXslInstance; 				// Document instance specific XSL file
	private String spsXmlOutputTreeInstance; 	// Document specific XML Output tree file referenced inside the XSL document instance file
	private String spsXslVarInstance;			// Document specific XSL vars file that is referenced inside the XSL document instance file.
	
	private Partition topPartition;
	private int topPartitionId;
	
	private String docType;
	private String dbVersion;
	private int authorId;
	private int langId;
	private String nestedTablePadChar;
	private boolean debug = true;

	public SupportPointXslInstanceManager() {
	}
	
	/**
	 * Get the input file
	 * @return File - inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * Set the input file
	 * @param inputFile Input File 
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	/**
	 * Get the final post-processing conversion stage XSLT transformation file.
	 * @return spsXsl Final post-processing conversion stage XSLT transformation file
	 */
	public String getSpsXsl() {
		return spsXsl;
	}

	/**
	 * Set the final post-processing conversion stage XSLT transformation file.
	 * @param spsXsl Final post-processing conversion stage XSLT transformation file
	 */
	public void setSpsXsl(String spsXsl) {
		this.spsXsl = spsXsl;
	}

	/**
	 * Get the final post-processing conversion stage XSLT tree transformation file. 
	 * @return spsTreeXsl Final post-processing conversion stage XSLT tree transformation file
	 */
	public String getSpsTreeXsl() {
		return spsTreeXsl;
	}

	/**
	 * Set the final post-processing conversion stage XSLT tree transformation file.
	 * @param spsTreeXsl Final post-processing conversion stage XSLT tree transformation file
	 */
	public void setSpsTreeXsl(String spsTreeXsl) {
		this.spsTreeXsl = spsTreeXsl;
	}
	
	/**
	 * Get the document specific xsl instance file name.
	 * @return String
	 */
	public String getSpsXslInstance() {
		return spsXslInstance;
	}

	/**
	 * Set the document specific xsl instance file name.
	 * @param spsXslInstance Input document specific xsl instance file name
	 */
	public void setSpsXslInstance(String spsXslInstance) {
		this.spsXslInstance = spsXslInstance;
	}

	/**
	 * Get the document specific xsl variable instance file name.
	 * @return spsXslVarInstance document specific xsl instance file name
	 */
	public String getSpsXslVarInstance() {
		return spsXslVarInstance;
	}

	/**
	 * Set the document specific xsl variable instance file name.
	 * @param spsXslVarInstance Document specific xsl variable instance file name.
	 */
	public void setSpsXslVarInstance(String spsXslVarInstance) {
		this.spsXslVarInstance = spsXslVarInstance;
	}
	
	/**
	 * This method creates the document instance specific xslt vars file. This file is referenced in the main document instance specific 
	 * xslt conversion file to provide document instance specific values for as part of the final stage conversion.
	 *  
	 * @throws IOException Throws IOException if issues arise generating the document specific XSLT vars file
	 */
	public void createXslVarInstanceFile() throws IOException
	{
		boolean useNestedTableIndenting = true;
		
		if (spsXsl != null)
		{
			if (this.getDbVersion() == null)
	        	this.setDbVersion("10.0.2");
	        
	        if (this.getDocType() == null)
	        	this.setDocType("procedure");	        
	        
	        StringBuffer sb = new StringBuffer();
	        sb.append("<xsl:stylesheet version=\"2.0\""); 
	        sb.append("\n\txmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"");
	        sb.append("\n\txmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
	        sb.append("\n\txmlns=\"http://www.w3.org/1999/xhtml\"");
	        sb.append("\n\txmlns:xhtml=\"http://www.w3.org/1999/xhtml\"");
	        sb.append("\n\txpath-default-namespace=\"http://www.w3.org/1999/xhtml\"");
	        sb.append("\n\txmlns:uuid=\"java:java.util.UUID\"");
	        sb.append("\n\texclude-result-prefixes=\"xhtml xsl xs\">");	        
	        sb.append("\n");
	        	        
	        sb.append("\n\t<xsl:variable name=\"top_partition_type\" select=\"'");
	        if (this.getTopPartition().getType() == PartitionType.FOLDER)
	        	sb.append("shelf");
	        else if (this.getTopPartition().getType() == PartitionType.DOCUMENT)
	        	sb.append("document");	        	
	        sb.append("'\"/>");
	        
	        sb.append("\n\t<xsl:variable name=\"top_partition_id\" select=\"'");
	        sb.append(this.getTopPartitionId());
	        sb.append("'\"/>");
	        	
	        sb.append("\n\t<xsl:variable name=\"author_id\" select=\"'");
	        sb.append(this.getAuthorId());
	        sb.append("'\"/>");
	
	        sb.append("\n\t<xsl:variable name=\"lang_id\" select=\"'");
	        sb.append(this.getLangId());
	        sb.append("'\"/>");
	        	
	        sb.append("\n\t<xsl:variable name=\"revision\" select=\"'1'\"/>");
	        
	        sb.append("\n\t<xsl:variable name=\"doc_type\" select=\"'");
	        sb.append(this.getDocType());
	        sb.append("'\"/>");
	        
	        sb.append("\n\t<xsl:variable name=\"db_id\" select=\"'");
	        sb.append(UUID.randomUUID());
	        sb.append("'\"/>");
	
	        sb.append("\n\t<xsl:variable name=\"db_version\" select=\"'");
	        sb.append(this.getDbVersion());
	        sb.append("'\"/>");
	
	        sb.append("\n\t<xsl:variable name=\"use_nested_table_indenting\" select=\"'");
	        sb.append(useNestedTableIndenting ? "1" : "0");
	        sb.append("'\"/>");
	        
	        sb.append("\n\t<xsl:variable name=\"nested_table_padding_character\" select=\"'");
	        sb.append(this.getNestedTablePadChar());
	        sb.append("'\"/>");
	        
	        sb.append("\n");
	        
	        sb.append("\n</xsl:stylesheet>");
	        
			// Write out all the dynamic xsl parameters to xsl import file. Typically named "sps.v1.0.vars.xsl"
	        FileWriter fw;
			fw = new FileWriter(this.getSpsXslVarInstance());
				
			fw.write(sb.toString());
			fw.flush();
			fw.close();
		}
	}
	
	/**
	 * This method replaces the reference to the xsl vars file in the document instance specific xsl file with the instance specific xsl vars file.
	 *  
	 * @throws IOException Exception thrown if an error occurs processing the XSLT document specific instance file
	 */
	public void processXslInstanceFile() throws IOException { 
	      BufferedReader br = null;
	      PrintWriter pw = null;
	      
	      try {
	    	  if (this.getSpsXsl().contains("resources/")) {

	    		  File spsXslFile = null;
	    		  
	    		  // The spsXsl file was not supplied as a command line argument and will be retrieved from the CLASSPATH in the application bundle.	
	    		  URL url = this.getClass().getClassLoader().getResource(this.getSpsXsl());
	    		  
	    		  // load the resource from the CLASSPATH
	    		  if (url != null) 
	    			  spsXslFile = new File(this.getClass().getClassLoader().getResource(this.getSpsXsl()).getFile());
	    		  
	    		  // If the resource cannot be extracted from the CLASSPATH load from the resources folder
	    		  if (spsXslFile != null && spsXslFile.exists()) {
	    			  br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(this.getSpsXsl())));
	    		  }
	    		  else
	    		  {
	    			// Extracting the SupportPoint xsl file as a resource from the CLASSPATH failed so pick it up from the resources {current-working-directory}/resources folder  
	    			spsXslFile = new File(System.getProperty("user.dir")+File.separator+this.getSpsXsl().replace("/", File.separator));
	      			
	      			if (spsXslFile != null && spsXslFile.exists())
	      			{
	      				br = new BufferedReader(new FileReader( spsXslFile ));
	      			}
	      			else {
	      				logger.error("Failed to copy input file from "+spsXslFile.getPath()+" to "+this.getSpsXslInstance());
	      				throw new IOException("Failed to copy input file from "+spsXslFile.getPath()+" to "+this.getSpsXslInstance());
	      			}
	    		  }
	    	  } else {
	    		  br = new BufferedReader(new FileReader( this.getSpsXsl() ));
	    	  }

	    	  pw =  new PrintWriter(new FileWriter( this.getSpsXslInstance() ));

	          String line;
	          while ((line = br.readLine()) != null) {
	        	  if (line.contains("output.tree.xml"))
						line = line.replace("output.tree.xml", this.getInputFile().getName() + ".output.tree.xml");
	        	  if (line.contains("sps.v1.0.vars.xsl"))
						line = line.replace("sps.v1.0.vars.xsl", FileHelper.getName(this.getSpsXslVarInstance()));

	              pw.println(line);
	          }

	          br.close();
	          pw.close();
	      } catch (Exception e) {
	    	  throw new IOException(e);
	      }
	}
	
	/**
	 * The main process method to createand process all the document instance specific xslt files required for the final stage document conversion. 
	 * @throws IOException Throws IOException if the XSLT transform fails to process the document specific xHtml document
	 */
	public void process() throws IOException
	{
		String base = getInputFile().getAbsolutePath();

		/* The spsXsl file is used to transform the input document.
		 * This file houses embedded document specific folder tree information and variables
		 * supplied in the SpsXmlOutputTreeInstance (XML inclusion in output) and SpsXslVarInstance 
		 * (as an XSL import) files.
		 */
		File spsXslFile = new File(this.getSpsXsl());		
		this.setSpsXslInstance(base + "." + spsXslFile.getName());
		
		this.setSpsXmlOutputTreeInstance(base + ".output.tree.xml");
		this.setSpsXslVarInstance(base + "." + spsXslFile.getName().replace(".xsl", "") + ".vars.xsl");
		
		this.createXslVarInstanceFile();
		this.processXslInstanceFile();
	}

	/**
	 * Get the top partition
	 * @return topPartition Top Partition
	 */
	public Partition getTopPartition() {
		return topPartition;
	}

	/**
	 * Set the top partition
	 * @param topPartition Top Partition
	 */
	public void setTopPartition(Partition topPartition) {
		this.topPartition = topPartition;
	}

	/**
	 * Get the top partition id
	 * @return topPartitionId Top Partition Id
	 */
	public int getTopPartitionId() {
		return topPartitionId;
	}

	/**
	 * Set the top partition id
	 * @param topPartitionId Top Partition Id
	 */
	public void setTopPartitionId(int topPartitionId) {
		this.topPartitionId = topPartitionId;
	}

	/**
	 * Get the document type
	 * @return docType Document type string
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * Set the document type
	 * @param docType Document type 
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}

	/**
	 * Get the database version
	 * @return dbVersion Database version
	 */
	public String getDbVersion() {
		return dbVersion;
	}

	/**
	 * Set the database version
	 * @param dbVersion Database version
	 */
	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}

	/**
	 * Get the author id
	 * @return authorId Author Id
	 */
	public int getAuthorId() {
		return authorId;
	}

	/**
	 * Set the author id
	 * @param authorId Author Id
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 * Get the language id. Defaults to 32.
	 * @return langId Language id
	 */
	public int getLangId() {
		return langId;
	}

	/**
	 * Set the language id.Defaults to 32.
	 * @param langId Language id
	 */
	public void setLangId(int langId) {
		this.langId = langId;
	}

	/**
	 * Get the nested table padding character used to indicate nested nested table content flattened in SupportPoint. Default is " ".
	 * @return nestedTablePadChar Nested table padding character
	 */
	public String getNestedTablePadChar() {
		return nestedTablePadChar;
	}

	/**
	 * Set the nested table padding character used to indicate nested nested table content flattened in SupportPoint. Default is " ".
	 * @param nestedTablePadChar Nested table padding character
	 */
	public void setNestedTablePadChar(String nestedTablePadChar) {
		this.nestedTablePadChar = nestedTablePadChar;
	}

	/**
	 * Get the document specific xsl tree instance file name.
	 * @return spsXmlOutputTreeInstance Document specific xsl tree instance file name
	 */
	public String getSpsXmlOutputTreeInstance() {
		return spsXmlOutputTreeInstance;
	}

	/**
	 * Set the document specific xsl tree instance file name.
	 * @param spsOutputTreeInstance Document specific xsl tree instance file name
	 */
	public void setSpsXmlOutputTreeInstance(String spsOutputTreeInstance) {
		this.spsXmlOutputTreeInstance = spsOutputTreeInstance;
	}

	// The following parameters are not relevant for SupportPoint documents - Yet
	public String getCountry() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCountry(String country) {
		// TODO Auto-generated method stub
		
	}

	public String getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLanguage(String language) {
		// TODO Auto-generated method stub
		
	}

	public String getOrganisation() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setOrganisation(String organisation) {
		// TODO Auto-generated method stub
		
	}
}
