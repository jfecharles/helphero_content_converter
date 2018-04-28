package com.helphero.util.hhc.processing.xslmanagers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.dom.processing.PartitionType;
import com.helphero.util.hhc.util.FileHelper;

public class HelpHeroXslInstanceManager extends SupportPointXslInstanceManager {
	private String organisation;
	private String country;
	private String language;
	
	/**
	 * Get the org name
	 * 
	 * @return organisation The org name
	 */
	public String getOrganisation() {
		return organisation;
	}
	
	/**
	 * Set the org name used in the xslt Help Hero vars file 
	 * 
	 * @param organisation
	 */
	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}
	
	/**
	 * Get the 2 letter country code
	 * 
	 * @return country Get the 2-letter country code
	 */
	public String getCountry() {
		return country;
	}
	
	/**
	 * Set the 2-letter country code
	 * 
	 * @param country Set the 2-letter country code
	 */
	public void setCountry(String country) {
		this.country = country;
	}
	
	/**
	 * Get the 2-letter language code
	 * @return language Get the 2-letter language code
	 */
	public String getLanguage() {
		return language;
	}
	
	/**
	 * Set the 2-letter country code
	 * 
	 * @param language Set the 2-letter country code
	 */
	public void setLanguage(String language) {
		this.language = language;
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
		
		if (this.getSpsXsl() != null)
		{
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
	        	        	        	
	        Path pathAbsolute = Paths.get(this.getInputFile().getAbsolutePath());
	        Path pathBase = Paths.get(System.getProperty("user.dir"));
	        Path pathRelative = pathBase.relativize(pathAbsolute);

	        sb.append("\n\t<xsl:variable name=\"orig_input_file\" select=\"'");
	        sb.append(pathRelative.toString().replaceAll("\\\\", "\\\\\\\\"));	        

	        sb.append("'\"/>");

	        sb.append("\n\t<xsl:variable name=\"author_id\" select=\"'");
	        sb.append(this.getAuthorId());
	        sb.append("'\"/>");

	        sb.append("\n\t<xsl:variable name=\"org\" select=\"'");
	        sb.append(this.getOrganisation());
	        sb.append("'\"/>");

	        sb.append("\n\t<xsl:variable name=\"lang\" select=\"'");
	        sb.append(this.getLanguage());
	        sb.append("'\"/>");
	        	
	        sb.append("\n\t<xsl:variable name=\"country\" select=\"'");
	        sb.append(this.getCountry());
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
	        	  if (line.contains("hh.v1.0.vars.xsl"))
						line = line.replace("hh.v1.0.vars.xsl", FileHelper.getName(this.getSpsXslVarInstance()));

	              pw.println(line);
	          }

	          br.close();
	          pw.close();
	      } catch (Exception e) {
	    	  throw new IOException(e);
	      }
	}
}
