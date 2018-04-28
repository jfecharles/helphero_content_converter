package com.helphero.util.hhc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.docx4j.Docx4jProperties;

/**
 * Convenience class to manage properties stored in a APPDATA\pgc\config.properties file.
 * 
 * This properties file is not currently used but could be used by an installer at a later point in time.
 * 
 *  The deduce properties method is used to determine the path to the ImageMagick converter program.
 * 
 * @author jcharles
 *
 */
public class ConfigHelper {
	static Logger logger = Logger.getLogger(ConfigHelper.class);
	private Properties prop = new Properties();

	public ConfigHelper() {
	}
	
	/**
	 * Main process method to load properties from a config.properties file in the APPDATA path and 
	 * deduce the path to the ImageMagick 
	 */
	public void process()
	{
		// Load the config.properties
		boolean found = false;
		
		try {
			found = this.load();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			found = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			found = false;
		}
		
		if (!found) this.deduceProperties();		        
	}
	
	/*
	 * Load the up config.properties from the users APPDATA\pgc\config.properties file if found
	 * @return boolean
	 */
	private boolean load() throws IOException, FileNotFoundException {
		// Find the location of the config.properties file
		File appData = new File(System.getenv("APPDATA"));
		boolean found = false;
		
		if (appData.exists())
		{
			File propsDir = new File(appData,"pgc");
			
			if (propsDir.exists())
			{
				File props = new File(propsDir,"config.properties");
				FileInputStream fis = new FileInputStream(props);
				
				prop.load(fis);	
				found = true;
			}
		}	
		return found;
	}
	
	/**
	 * Deduce important properties from alternate sources
	 */
	public void deduceProperties() {		
		// Deduce the path ImageMagick has been installed in
		logger.info("\tFile config.properties non-existant. Deducing ImageMagick location from the PATH.");
		String fullPath = System.getenv("PATH");		
		if (fullPath != null) {
			String[] paths = fullPath.split(File.pathSeparator);
			for (String path : paths) {
				if (path.contains("ImageMagick"))
				{
					setProperty("ImageMagickPath", path);
					break;
				}
			}
		}
	}
	
	/**
	 * Main getter method to retrieve the value for specific key
	 * @param key - Retrieve a property using this key value
	 * @return String - Property string value
	 */
	public String getProperty(String key)
	{
		return prop.getProperty(key);
	}
	
	/**
	 * Main setter method to set a property key, value pair.
	 * @param key - The property key
	 * @param value - The property value
	 */
	public void setProperty(String key, String value)
	{
		prop.setProperty(key, value);
	}
}
