package com.helphero.util.hhc.processing.xslmanagers;

import java.io.File;
import java.io.IOException;

import com.helphero.util.hhc.dom.processing.Partition;

public interface IXslInstanceManager {
	
	/**
	 * Get the input file
	 * @return File - inputFile
	 */
	public File getInputFile();

	/**
	 * Set the input file
	 * @param inputFile Input File 
	 */
	public void setInputFile(File inputFile);
	
	/**
	 * Get the final post-processing conversion stage XSLT transformation file.
	 * @return spsXsl Final post-processing conversion stage XSLT transformation file
	 */
	public String getSpsXsl();

	/**
	 * Set the final post-processing conversion stage XSLT transformation file.
	 * @param spsXsl Final post-processing conversion stage XSLT transformation file
	 */
	public void setSpsXsl(String spsXsl);

	/**
	 * Get the final post-processing conversion stage XSLT tree transformation file. 
	 * @return spsTreeXsl Final post-processing conversion stage XSLT tree transformation file
	 */
	public String getSpsTreeXsl();

	/**
	 * Set the final post-processing conversion stage XSLT tree transformation file.
	 * @param spsTreeXsl Final post-processing conversion stage XSLT tree transformation file
	 */
	public void setSpsTreeXsl(String spsTreeXsl);
	
	/**
	 * Get the document specific xsl instance file name.
	 * @return String
	 */
	public String getSpsXslInstance();

	/**
	 * Set the document specific xsl instance file name.
	 * @param spsXslInstance Input document specific xsl instance file name
	 */
	public void setSpsXslInstance(String spsXslInstance);

	/**
	 * Get the document specific xsl variable instance file name.
	 * @return spsXslVarInstance document specific xsl instance file name
	 */
	public String getSpsXslVarInstance();

	/**
	 * Set the document specific xsl variable instance file name.
	 * @param spsXslVarInstance Document specific xsl variable instance file name.
	 */
	public void setSpsXslVarInstance(String spsXslVarInstance);
	
	/**
	 * This method creates the document instance specific xslt vars file. This file is referenced in the main document instance specific 
	 * xslt conversion file to provide document instance specific values for as part of the final stage conversion.
	 *  
	 * @throws IOException Throws IOException if issues arise generating the document specific XSLT vars file
	 */
	public void createXslVarInstanceFile() throws IOException;
	
	/**
	 * This method replaces the reference to the xsl vars file in the document instance specific xsl file with the instance specific xsl vars file.
	 *  
	 * @throws IOException Exception thrown if an error occurs processing the XSLT document specific instance file
	 */
	public void processXslInstanceFile() throws IOException; 
	
	/**
	 * The main process method to createand process all the document instance specific xslt files required for the final stage document conversion. 
	 * @throws IOException Throws IOException if the XSLT transform fails to process the document specific xHtml document
	 */
	public void process() throws IOException;

	/**
	 * Get the top partition
	 * @return topPartition Top Partition
	 */
	public Partition getTopPartition();

	/**
	 * Set the top partition
	 * @param topPartition Top Partition
	 */
	public void setTopPartition(Partition topPartition);

	/**
	 * Get the top partition id
	 * @return topPartitionId Top Partition Id
	 */
	public int getTopPartitionId();

	/**
	 * Set the top partition id
	 * @param topPartitionId Top Partition Id
	 */
	public void setTopPartitionId(int topPartitionId);

	/**
	 * Get the document type
	 * @return docType Document type string
	 */
	public String getDocType();

	/**
	 * Set the document type
	 * @param docType Document type 
	 */
	public void setDocType(String docType);

	/**
	 * Get the database version
	 * @return dbVersion Database version
	 */
	public String getDbVersion();

	/**
	 * Set the database version
	 * @param dbVersion Database version
	 */
	public void setDbVersion(String dbVersion);

	/**
	 * Get the author id
	 * @return authorId Author Id
	 */
	public int getAuthorId();

	/**
	 * Set the author id
	 * @param authorId Author Id
	 */
	public void setAuthorId(int authorId);

	/**
	 * Get the language id. Defaults to 32.
	 * @return langId Language id
	 */
	public int getLangId();

	/**
	 * Set the language id.Defaults to 32.
	 * @param langId Language id
	 */
	public void setLangId(int langId);

	/**
	 * Get the nested table padding character used to indicate nested nested table content flattened in SupportPoint. Default is " ".
	 * @return nestedTablePadChar Nested table padding character
	 */
	public String getNestedTablePadChar();

	/**
	 * Set the nested table padding character used to indicate nested nested table content flattened in SupportPoint. Default is " ".
	 * @param nestedTablePadChar Nested table padding character
	 */
	public void setNestedTablePadChar(String nestedTablePadChar);

	/**
	 * Get the document specific xsl tree instance file name.
	 * @return spsXmlOutputTreeInstance Document specific xsl tree instance file name
	 */
	public String getSpsXmlOutputTreeInstance();

	/**
	 * Set the document specific xsl tree instance file name.
	 * @param spsOutputTreeInstance Document specific xsl tree instance file name
	 */
	public void setSpsXmlOutputTreeInstance(String spsOutputTreeInstance);
	
	// Help Hero specific parameters
	/**
	 * Get the 2-letter country code
	 * @return String Get the 2-letter country code
	 */
	public String getCountry();
	
	/**
	 * Set the 2-letter country code.
	 * 
	 * @param country Set the 2-letter country code
	 */
	public void setCountry(String country);
	
	/**
	 * Get the 2-letter language code
	 * @return String Get the 2-letter language code
	 */
	public String getLanguage();
	
	/**
	 * Set the 2-letter language code
	 * @param language Set the 2-letter language code
	 */
	public void setLanguage(String language);
	
	/**
	 * Get the organisation short name
	 * @return String Get the organisation short name
	 */
	public String getOrganisation();

	/**
	 * Set the organisation short name
	 * 
	 * @param organisation Set the organisation short name
	 */
	public void setOrganisation(String organisation);
}
