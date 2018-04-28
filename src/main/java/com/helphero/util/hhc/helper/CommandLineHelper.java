package com.helphero.util.hhc.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.processing.SupportedDocType;
import com.helphero.util.hhc.processing.SupportedOutputDocType;
import com.helphero.util.hhc.util.FileHelper;

/**
 * This helper class processes all the command line arguments. It provides the following:
 * 1. Command line argument validation
 * 2. Command line argument usage reporting allowing the user to see what the command line options are and their meaning.
 * 
 * @author jcharles
 */
public class CommandLineHelper {	
	private int defaultDocId = 1000;
	private int defaultFolderId = 10000;
	private int defaultLangId = 32;
	private String defaultDocType = "procedure";
	private String defaultWildcard = "\\*.docx";
	private String defaultSpsXsl = "resources/sps.v1.0.xsl";
	private String defaultSpsTreeXsl = "resources/sps-tree.v1.0.xsl";
	
	private String templateFile = null;
	private boolean useTemplate = false;
	private String rulesFile = "rules.xml";
	private boolean useNestedTableIndenting = false;
	private String nestedTablePadChar = " ";
	private String dbVersion;
	private String docType;
	private int docId = defaultDocId;
	private int langId = defaultLangId;
	private int authorId;
	private int threadCount = 4;
	private int folderId = defaultFolderId;
	private String wildcard = "\\*.docx";
	private String templateType = ".dotx";
	private String spsXsl;
	private String spsTreeXsl;
	private String xslVarFile;
	private String debugOptions;
	private String log4jProps;
	
	private String targetVersion = "1";
	private String targetType = "hh";
	private String organisation;
	private String country = "au";			// 2-letter country code
	private String language = "en";			// 2-letter language code
	
	private CommandLine cmdLineValues = null;

	/**
	 * Constructor
	 */
	public CommandLineHelper() {
	}

	/**
	 * Constructor
	 * @param args - Command line arguments
	 * @throws ParseException - An exception that is thrown when a parsing error has occurred
	 */
	public CommandLineHelper(String[] args) throws ParseException {
		
		CommandLineParser parser = new BasicParser();
		
		Options options = getOptions();

		try {
			cmdLineValues = parser.parse( options, args );
		} catch (ParseException ex)
		{
			System.err.println("Commandline Parse Error: "+ex.getMessage());
			this.usage();
		}

		// Validate the Command line options
		if (this.validate())
			System.exit(0);
		
		// Process the Command line options
		this.process();
	}

	/**
	 * This getter method defines all the program command line options returning them in a convenient Options object.
	 * @return Options
	 */
	public Options getOptions()
	{
		// Create the command line options object
		Options options = new Options();
		
		// No argument options
		options.addOption("h", "help", false, "Usage: [Options] [FILE List]");
		options.addOption("d", "debug", false, "Enable debugging");
		options.addOption("r", "recurse", false, "Recursively descend through sub-directory matching files against the wildcard");
		options.addOption("n", "usenestedtableindenting", false, "Use nested table indenting. Default is true.");
		options.addOption("V", "version", false, "Product version information.");

		// Command line options requiring an argument
		options.addOption("r","rules",true, "Load an XML based property file.");
		options.addOption("p", "nestedtablepadchar", true, "Nested table padding character. Default char is space.");
		options.addOption("v", "dbversion", true, "Database version. No default value.");
		options.addOption("o", "doctype", true, "Document types. Valid values are: procedure[top tabs], policy[left tabs], process[side window]. Default value is \"procedure\".");
		options.addOption("i", "docid", true, "First document id. Default value is 1000.");
		options.addOption("l", "langid", true, "Language id. Default value is 32.");
		options.addOption("a", "authorid", true, "Author id. No default value.");
		options.addOption("f", "folderid", true, "First folder id. Default value is 10000.");		
		options.addOption("w", "wildcard", true, "Use the specified wildcard instead of the default *.docx");
		options.addOption("t", "template", true, "The name of the template file i.e. dotx, docx, pptx, potx");
		options.addOption("s", "spsxsl", true, "SupportPoint version specific XSLT transformation and filtering file");
		options.addOption("e", "treexsl", true, "SupportPoint version specific XSLT tree structure extraction file");
		options.addOption("C", "log4jprops", true, "Used to set the log4j.configuration setting. This is used by the log4j libraries to locate the log4j.properties configuration file.");
		options.addOption("L", "log_file", true, "Sets the name and location for log4j log files.");
		options.addOption("h", "threads", true, "Sets the maximum number of simultaneous document conversion threads. The default is 4.");
		
		options.addOption("g", "org", true, "The organisation providing the documents. No default.");
		options.addOption("tt", "targettype", true, "The output target format. The default is hh.");
		options.addOption("tv", "targetversion", true, "The output target version. The default is 1.");
		
		return options;
	}
	
	/**
	 * @return String[] - A list of the remaining command line arguments.
	 */
	public String[] getArgs()
	{
		return cmdLineValues.getArgs();
	}
	
	/**
	 * This method checks the validity of command line arguments and assigns default values if not set.
	 * 
	 * @return boolean
	 */
	public boolean validate()
	{
		boolean error = false;
		
		if (cmdLineValues.hasOption("version"))
		{
			String sVersion = this.getClass().getPackage().getImplementationVersion();
			String sTitle = this.getClass().getPackage().getImplementationTitle();
			String sVendor = this.getClass().getPackage().getImplementationVendor();
					
			StringBuilder sb = new StringBuilder();
			if (sVersion != null)
				sb.append("Version: "+sVersion);
			if (sTitle != null && sTitle.length() > 0)
				sb.append("\nTitle: "+sTitle);
			if (sVendor != null && sVendor.length() > 0)
				sb.append("\nVendor: "+sVendor);
				
			if (sb.length() > 0) 
				System.out.println(sb.toString());
			
			System.exit(0);
		}
		
		if (!cmdLineValues.hasOption("rules"))
			setRules(cmdLineValues.getOptionValue("rules"));
		else
		{
			error = validateFile("rules","xml", true);
		}
	
		if (!cmdLineValues.hasOption("dbversion"))
		{
			System.err.println("<<< No database version specified.>>>");
			error = true;
		}
		
		if (!cmdLineValues.hasOption("doctype"))
		{
			setDocType(defaultDocType);
		}
		
		if (!cmdLineValues.hasOption("docid"))
		{
			setDocId(defaultDocId);
		}
		
		if (!cmdLineValues.hasOption("langid"))
		{
			setLangId(defaultLangId);
		}
		
		if (!cmdLineValues.hasOption("authorid"))
		{
			System.err.println("Warning: No authorId specified.>>>");
			error = true;
		}
		
		if (!cmdLineValues.hasOption("folderid"))
		{
			this.setFolderId(defaultFolderId);
		}
		
		if (!cmdLineValues.hasOption("wildcard"))
		{
			setWildcard(defaultWildcard);
		}
		
		if (cmdLineValues.hasOption("template"))
		{
			this.setUseTemplate(true);
			error = validateFile("template","dotx", true);	
		}
			
		if (!cmdLineValues.hasOption("spsxsl"))
		{
			System.out.println("No SupportPoint final stage xsl transformation file specified. Setting the default value to internally packaged \""+defaultSpsXsl+"\".");
			setSpsXsl(defaultSpsXsl);
		}
		
		if (!cmdLineValues.hasOption("treexsl"))
		{
			System.out.println("No SupportPoint final stage tree extraction xsl transformation file specified. Setting the default value to to internally packaged \""+defaultSpsXsl+"\".");
			setSpsTreeXsl(defaultSpsTreeXsl);
		}
		
		if (!cmdLineValues.hasOption("threads")) 
		{
			setThreadCount(4);
		}
		
		if (cmdLineValues.hasOption("targettype") && cmdLineValues.hasOption("targetversion") && !cmdLineValues.hasOption("org"))
		{
			System.err.println("<<< No organisation specified.>>>");
			error = true;			
		}
		
		try {
			SupportedOutputDocType type = SupportedOutputDocType.valueOf(this.getTargetType().toUpperCase()+"V"+this.getTargetVersion());
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid TargetType="+this.getTargetType()+" and Version="+this.getTargetVersion()+" combination.");
			System.err.println("\tValid TargetTypes are [sps, hh] and valid TargetVersions are [ 1 ]. ");
			error = true;
		}
			
		if (error) usage();
		
		return error;
	}
	
	/**
	 * Process the command line options assigning the values to the various private member variables via public setters. 
	 */
	public void process()
	{
		// Assign all the values to the various options. default values are assigned during the validation phase.
		if (cmdLineValues.hasOption("help"))
			this.usage();
		
		if (cmdLineValues.hasOption("rules"))
			setRules(cmdLineValues.getOptionValue("rules"));
	
		if (cmdLineValues.hasOption("usenestedtableindenting"))
			setUseNestedTableIndenting(CommandLineHelper.booleanValue(cmdLineValues.getOptionValue("usenestedtableindenting"),false));
		else
			setUseNestedTableIndenting(true);
		
		if (cmdLineValues.hasOption("template")) 
			setTemplateFile(cmdLineValues.getOptionValue("template"));
		
		if (cmdLineValues.hasOption("nestedtablepadchar"))
			setNestedTablePadChar(cmdLineValues.getOptionValue("nestedtablepadchar"));
		
		if (cmdLineValues.hasOption("dbversion"))
			setDbVersion(cmdLineValues.getOptionValue("dbversion"));
		
		if (cmdLineValues.hasOption("doctype"))
			setDocType(cmdLineValues.getOptionValue("doctype"));
		
		if (cmdLineValues.hasOption("docid"))
			setDocId(CommandLineHelper.intValue(cmdLineValues.getOptionValue("docid"), 1000));
		
		if (cmdLineValues.hasOption("langid"))
			setLangId(CommandLineHelper.intValue(cmdLineValues.getOptionValue("langid"), 32));
		
		if (cmdLineValues.hasOption("authorid"))
			setAuthorId(CommandLineHelper.intValue(cmdLineValues.getOptionValue("authorid"), 1234));
		
		if (cmdLineValues.hasOption("folderid"))
			setFolderId(CommandLineHelper.intValue(cmdLineValues.getOptionValue("folderid"), 10000));
		
		if (cmdLineValues.hasOption("threads"))
			setThreadCount(CommandLineHelper.intValue(cmdLineValues.getOptionValue("threads"), 4));
		
		if (cmdLineValues.hasOption("wildcard"))
			setWildcard(cmdLineValues.getOptionValue("wildcard"));
		
		if (cmdLineValues.hasOption("template"))
			setTemplateType(cmdLineValues.getOptionValue("template"));
		
		if (cmdLineValues.hasOption("spsxsl"))
			setSpsXsl(cmdLineValues.getOptionValue("spsxsl"));
		
		if (cmdLineValues.hasOption("treexsl"))
			setSpsTreeXsl(cmdLineValues.getOptionValue("treexsl"));
		
		if (cmdLineValues.hasOption("log4jprops") && cmdLineValues.getOptionValue("log4jprops") != null) {
			System.setProperty("log4j.configuration", cmdLineValues.getOptionValue("log4jprops"));
			if (cmdLineValues.hasOption("debug")) System.out.println("log4j.configuration="+cmdLineValues.getOptionValue("log4jprops"));					 
		}
		
		if (cmdLineValues.hasOption("log_file") && cmdLineValues.getOptionValue("log_file") != null) {
			System.setProperty("log4j.log_file", cmdLineValues.getOptionValue("log_file"));
			if (cmdLineValues.hasOption("debug")) System.out.println("log4j.log_file="+cmdLineValues.getOptionValue("log_file"));					 
		}
		
		if (cmdLineValues.hasOption("org"))
			setOrganisation(cmdLineValues.getOptionValue("org"));
		
		if (cmdLineValues.hasOption("targettype"))
			setTargetType(cmdLineValues.getOptionValue("targettype"));

		if (cmdLineValues.hasOption("targetversion"))
			setTargetVersion(cmdLineValues.getOptionValue("targetversion"));
	}
	
	/** 
	 * Private method to validate the existence of the various files that can be specified on the command line.
	 * @param option - String
	 * @param type - String
	 * @param hasFile - boolean
	 * @return boolean - Exists = true, Does not exist = false 
	 */
	private boolean validateFile(String option, String type, boolean hasFile)
	{
		boolean error = false;
		
		if (!cmdLineValues.hasOption(option))
		{
			System.err.println("<<<Error: No "+option+" file of type \""+type+"\" file specified>>>");
			error = true;
		}
		
		if (hasFile && !FileHelper.exists(cmdLineValues.getOptionValue(option)))
		{
			System.err.println("<<<Error: File "+cmdLineValues.getOptionValue(option)+" does not exist>>>");
			error = true;
		}
		
		return error;		
	}
	 
	/**
	 * Public getter/setters
	 * Set the name of the rules xml file.
	 * @param rulesFile - Name of the rule file
	 */
	public void setRules(String rulesFile)
	{
		this.rulesFile = rulesFile;
	}

	/**
	 * Get the name of the rules xml file.
	 * @return rulesFile - The name of the rule file
	 */
	public String getRules()
	{
		return this.rulesFile;
	}
	
	/**
	 * Get a boolean flag indicating if the nested table content is to be indented or not in the target format. 
	 * SupportPoint does not currently support nested tables so content need to be compacted into a single table depth. Indentation is used to 
	 * discriminate nested content. 1 indent for 1 level of nesting, 2 indents for 2 level of nesting etc.      
	 * @return boolean
	 */
	public boolean isUseNestedTableIndenting() {
		return useNestedTableIndenting;
	}

	/**
	 * Set a boolean flag indicating if the nested table content is to be indented or not in the target format. 
	 * @param useNestedTableIndenting - boolean
	 */
	public void setUseNestedTableIndenting(boolean useNestedTableIndenting) {
		this.useNestedTableIndenting = useNestedTableIndenting;
	}

	/**
	 * Get the database version. No default.
	 * @return String
	 */
	public String getDbVersion() {
		return dbVersion;
	}

	/**
	 * Set the database version.
	 * @param dbVersion - String defining the database version
	 */
	public void setDbVersion(String dbVersion) {
		this.dbVersion = dbVersion;
	}

	/**
	 * Get the character used for nested table content indentation.
	 * @return String - Defaults to a single whitespace
	 */
	public String getNestedTablePadChar() {
		return nestedTablePadChar;
	}

	/**
	 * Set the character used for nested table content indentation. Default is " ".
	 * @param nestedTablePadChar - The value of the nested table padding character
	 */
	public void setNestedTablePadChar(String nestedTablePadChar) {
		this.nestedTablePadChar = nestedTablePadChar;
	}

	/**
	 * Get the document type.
	 * @return String - Default is docx
	 */
	public String getDocType() {
		return docType;
	}

	/**
	 * Set the document type.
	 * @param docType - String 
	 */
	public void setDocType(String docType) {
		this.docType = docType;
	}

	/**
	 * Set the first document id. Default is 1000.
	 * @return int - The document id
	 */
	public int getDocId() {
		return docId;
	}

	/**
	 * Set the first document id. Default is 1000.
	 * @param docId - The document id
	 */
	public void setDocId(int docId) {
		this.docId = docId;
	}

	/**
	 * Get the language id. Default is 32.
	 * @return int - The language id. Default is 32.
	 */
	public int getLangId() {
		return langId;
	}

	/**
	 * Set the language id. Default is 32.
	 * @param langId - Language id
	 */
	public void setLangId(int langId) {
		this.langId = langId;
	}

	/**
	 * Get the author id. No default.
	 * @return int - The author id
	 */
	public int getAuthorId() {
		return authorId;
	}

	/**
	 * Set the author id. No default.
	 * @param authorId - The author id int value
	 */
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	/**
	 * Get the first folder id. Default is 10000. 
	 * @return int - Folder id
	 */ 
	public int getFolderId() {
		return folderId;
	}

	/**
	 * Set the first folder id. Default is 10000.
	 * @param folderId - Folder id int value
	 */
	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}

	/**
	 * Get the document wildcard. Default is "*.docx". 
	 * This is expanded to all documents in the current target folder and all descending folders.  
	 * @return String - Document wildcard ie. "*.docx"
	 */
	public String getWildcard() {
		return wildcard;
	}
	
	/**
	 * Get the Enumerated type of the document(s) to be transformed.
	 * @return SupportedDocType - Valid command types are: ".docx", ".pptx", ".xslx", ".xhtml", ".xml", ".pdf"
	 */
	public SupportedDocType getDocTypeFromWildcard()
	{
		SupportedDocType docType = SupportedDocType.DOCX;
		
		if (wildcard != null)
		{
			if (wildcard.toLowerCase().endsWith(".docx"))
				docType = SupportedDocType.DOCX;
			else if (wildcard.toLowerCase().endsWith(".pptx"))
				docType = SupportedDocType.PPTX;
			else if (wildcard.toLowerCase().endsWith(".pptx"))
				docType = SupportedDocType.PPTX;
			else if (wildcard.toLowerCase().endsWith(".xlsx"))
				docType = SupportedDocType.XLSX;
			else if (wildcard.toLowerCase().endsWith(".xhtml"))
				docType = SupportedDocType.XHTML;
			else if (wildcard.toLowerCase().endsWith(".pdf"))
				docType = SupportedDocType.PDF;
			else if (wildcard.toLowerCase().endsWith(".xml"))
				docType = SupportedDocType.XML;
			else
				docType = SupportedDocType.NOT_SET;
		}
		
		return docType;
	}

	/**
	 * Set the document wildcard. Default is "*.docx". 
	 * This is expanded to all documents in the current target folder and all descending folders.  
	 * @param wildcard - String describing the documenmt wildcard
	 */
	public void setWildcard(String wildcard) {
		this.wildcard = wildcard;
	}

	/**
	 * Get the template file type. Default is "dotx".
	 * This is used to override the styles in the document to be converted.
	 * @return String - Describes the template style document type. i.e. dotx 
	 */
	public String getTemplateType() {
		return templateType;
	}

	/**
	 * Set the template file type. Default is "dotx".
	 * This is used to override the styles in the document to be converted.
	 * @param templateType - The template document style i.e. dotx
	 */
	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	/**
	 * Get the name of the SupportPoint final stage transformation XSLT file. 
	 * If not specified the file sps.v1.0.xsl in the resources folder of the current working directory will be used.
	 * @return String - The final stage XSLT transformation file
	 */
	public String getSpsXsl() {
		return spsXsl;
	}

	/**
	 * Set the name of the SupportPoint final stage transformation XSLT file. 
	 * If not specified the file sps.v1.0.xsl in the resources folder of the current working directory will be used.
	 * @param spsXsl - The final stage XSLT transformation file
	 */
	public void setSpsXsl(String spsXsl) {
		this.spsXsl = spsXsl;
	}

	/**
	 * Get the style template file name. If not specified not styling overrides will be performed on the document to be converted.
	 * @return String - The style template file name and path
	 */
	public String getTemplateFile() {
		return templateFile;
	}

	/**
	 * Set the style template file name. If not specified not styling overrides will be performed on the document to be converted.
	 * @param templateFile - The style template file name and path
	 */	
	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}

	/**
	 * Returns a boolean flag to indicate if styling overrides will be implemented.
	 * @return boolean - Is a style template override file being used.
	 */
	public boolean isUseTemplate() {
		return useTemplate;
	}

	/**
	 * Set a flag to indicate if template driven style overrides will be performed.  
	 * @param useTemplate - boolean flag set to indicate a style template override file is used.
	 */
	public void setUseTemplate(boolean useTemplate) {
		this.useTemplate = useTemplate;
	}
	
	/**
	 * Not used.
	 * @return String
	 */
	public String getXslVarFile() {
		return this.xslVarFile;
	}

	/**
	 * This method prints a list of all command line options to the console.
	 */
	public void usage()
	{
		System.err.println("\nUsage: pgc");
		System.err.println("\tOptions:");
		Options options = this.getOptions();
		for (Option option : new ArrayList<Option>(options.getOptions()))
		{
			System.err.println("\t\tShortOpt="+option.getOpt()+":LongOpt="+option.getLongOpt()+":Description="+option.getDescription()+":Mandatory="+option.isRequired());
		}
		System.exit(0);
	}
	
	/**
	 * Private convenience method to test if a supplied string can be interpreted as a boolean. 
	 * @param s - "true", "t", "yes", "y", 1" all translate to true
	 * @param defaultValue - Default value if not set correctly.
	 * @return boolean
	 */
	private static boolean booleanValue(String s, boolean defaultValue)
	{
		boolean value = defaultValue;
		if (s != null)
		{
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("y") ||  s.equals("1"))
			{
				value = true;
			}
		}
		return value;
	}
	
	/**
	 * Private convenience method to test if a supplied string is an integer.
	 * 
	 * @param s  - The string housing the integer value
	 * @param defaultValue - Default value assigned if not set
	 * @return int
	 */
	private static int intValue(String s, int defaultValue)
	{
		int value = defaultValue;
		
		if (s != null)
		{
			try {
				value = Integer.parseInt(s);
			} catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	/**
	 * This method processes the wildcard specified on the command line. 
	 * If a wildcard is specified on the commandline any * characters must be prefixed with a '\' character. i.e. -w "\*.docx"
	 * Otherwise it will be expanded at the time the getOptionValue is issued. This will then retrieve a list of docx files in
	 * the workspace directory or sub-directories of the current user. 
	 * @param wc - Wild card value
	 * @return wildcard - New wild card value
	 */
	public static String getNewWildcard(String wc)
	{
		String wildcard = wc;
		if (wc.contains("\\")) {
    		wildcard = wc.replace("\\", "");
    	}
		return wildcard;
	}

	/**
	 * Get the path to the SupportPoint XSLT file used to derive the folder and document relationship tree structure.
	 * This is used for the final stage transformation of input documents to theie SupportPoint specific importable xml format file counterpart.
	 * If not specified this defaults to {current working directory}/resources/sps-tree.v1.0.xsl
	 * @return String - The Final stage SupportPoint XSLT transform file used to generate an output folder treeFinal stage SupportPoint XSLT transform file used to generate an output folder tree
	 */
	public String getSpsTreeXsl() {
		return spsTreeXsl;
	}

	/**
	 * Set the path to the SupportPoint XSLT file used to derive the folder and document relationship tree structure.
	 * This is used for the final stage transformation of input documents to theie SupportPoint specific importable xml format file counterpart.
	 * If not specified this defaults to {current working directory}/resources/sps-tree.v1.0.xsl
	 * @param spsTreeXsl - Set the 
	 */
	public void setSpsTreeXsl(String spsTreeXsl) {
		this.spsTreeXsl = spsTreeXsl;
	}

	/**
	 * Return the path to the log4j.properties configuration file.
	 * If not specified this defaults to {current working directory}/resources/log4j.properties
	 * @return String - The log4j.properties file location
	 */
	public String getLog4jProps() {
		return log4jProps;
	}

	/**
	 * Set the path to the log4j.properties configuration file.
	 * If not specified this defaults to {current working directory}/resources/log4j.properties
	 * @param log4jProps - Set the log4j.properties file location
	 */
	public void setLog4jProps(String log4jProps) {
		this.log4jProps = log4jProps;
	}

	/**
	 * Return the number of simultaneous threads used to convert input document. 8 threads means 8 simultaneous document conversions.
	 * Default is 4.
	 * @return int - The number of simultaneous file conversion threads to execute.
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Set the number of simultaneous threads used to convert input document. 8 threads means 8 simultaneous document conversions.
	 * The default is 4.
	 * @param threadCount - Sets the number of simultaneous file conversion threads to execute.
	 */
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public String getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}

	public String getTargetType() {
		return targetType;
	}
	
	public SupportedOutputDocType getSupportedTargetType()
	{
		SupportedOutputDocType type = SupportedOutputDocType.HHV1;
		
		if (getTargetType() != null && getTargetVersion() != null)
		{
			String sType = getTargetType().toUpperCase() + "V" + getTargetVersion();
			
			try {
				type = SupportedOutputDocType.valueOf(sType);
			} catch (IllegalArgumentException e) {
				type = SupportedOutputDocType.HHV1;
			}
		}
		
		return type;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getOrganisation() {
		return organisation;
	}

	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
