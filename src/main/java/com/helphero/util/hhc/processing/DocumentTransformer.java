package com.helphero.util.hhc.processing;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.TransformationManager;
import com.helphero.util.hhc.helper.CommandLineHelper;
import com.helphero.util.hhc.rule.IRule;
import com.helphero.util.hhc.rule.ITask;
import com.helphero.util.hhc.rule.MatchProperties;
import com.helphero.util.hhc.rule.RuleSet;
import com.helphero.util.hhc.rule.RuleTarget;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.TaskJoinType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.rule.TaskPosition;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DateHelper;

/**
 * This class manages the application of business filtering and transformation rules to the intermediary common object model xHtml document.
 * It generates a new business filtered and transformed xHtml file suffixed with the name .biz.xhtml.
 * 
 * @author jcharles
 *
 */
public class DocumentTransformer {
	static Logger logger = Logger.getLogger(DocumentTransformer.class);
	
	private DocumentBuilder builder = null;
	private Document inputDom = null;
	private Document outputDom = null;
	private String inputFile;
	private String outputFile;
	private Partition topPartition = null;
	private int topPartitionId = -1;
	
	private RuleSet ruleSet = null;
	
	private boolean debug = false;

	/**
	 * Constructor: Setup all the plumbing to create the W3C Input DOM
	 */	
	public DocumentTransformer() {
	}
	
	/**
	 * Not used
	 * @throws ParserConfigurationException Exception thrown if the output DOM is set incorrectly
	 */
	public void createOutputDom() throws ParserConfigurationException 
	{
		// Create a new DocumentBuilderFactory
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	
	    outputDom = builder.newDocument(); 		
	}
	
	/**
	 * Load the input file name and create an Document Builder instance 
	 * @throws ParserConfigurationException Exception thrown if the intermediary input file fails to load 
	 */
	public void loadAsXml() throws ParserConfigurationException
	{
		loadAsXml(this.getInputFile());
	}
	
	/**
	 * Load the input file name and create an Document Builder instance
	 * @param inputFile Input file name
	 * @throws ParserConfigurationException Exception thrown if the intermediary input file fails to load
	 */
	public void loadAsXml(String inputFile) throws ParserConfigurationException
	{	
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
	         builder = factory.newDocumentBuilder();
	         
		     // Create a new W3C document from the input source
	         setInputDom(inputFile);
	      } catch (Exception ex) {
	         ex.printStackTrace();
	      }	
	}
	
	/**
	 * Save the transformed InputDom file to an output xml file.
	 */
	public void saveAsXml()
	{
		saveAsXml(this.getOutputFile());
	}
	
	/**
	 * Save the transformed InputDom file to an output xml file. 
	 * @param outputFile Output file name
	 */
	public void saveAsXml(String outputFile)
	{
		try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            // send DOM to file
            FileOutputStream fos = new FileOutputStream(outputFile);
            transformer.transform(new DOMSource(inputDom), new StreamResult(fos));
            
            fos.flush();
            fos.close();

        } catch (TransformerException te) {
            logger.error("Failed to convert "+inputDom.getDocumentURI()+" to "+outputFile, te);
        } catch (IOException ioe) {
            logger.error("Failed to convert "+inputDom.getDocumentURI()+" to "+outputFile, ioe);
        }
	}
	
	/**
	 * Set the input DOM
	 * @param file - Input file name
	 * @throws SAXException Exception thrown parsing the input file
	 * @throws IOException General exception thrown while processing the input file
	 */
	private void setInputDom(String file) throws SAXException, IOException
	{
        if (ruleSet.isDebug()) logger.info("Document Processor: Creating Input DOM from file: "+file+": "+DateHelper.getFormattedDateTimeNow());
        inputDom = builder.parse(file);
        if (ruleSet.isDebug()) logger.info("Document Processor: Created Input File DOM: "+DateHelper.getFormattedDateTimeNow());
	}
	
	/**
	 * Get the input DOM
	 * @return Input W3C Dom
	 */
	private Document getInputDom()
	{
		return inputDom;
	}
	
	/**
	 * Apply all the transformation rules to the input document and save as an xHtml document
	 * @throws Exception General exception thrown if any other issue occurs during the execution of the transform 
	 * @throws XPathExpressionException Exception thrown if an invalid XPath is used
	 * @throws TransformException Exception thrown if a problem occurs while executing the transform
	 */
	public void process() throws Exception, XPathExpressionException, TransformException
	{
		logger.info("=== Document Processor: process()");
		
		if (ruleSet.isDebug())
		{
			for (IRule rule : ruleSet.getRules())
			{
				RuleType type = (RuleType) rule.getType();
				RuleTarget target = (RuleTarget) rule.getTarget();
				
				logger.info("\t Rule: Type="+type.name()+":Target="+target.name());
				
				for (ITask task : rule.getTasks())
				{
					TaskType taskType = (TaskType)task.getType();
					String xPathSrc = task.getSource();
					TaskJoinType taskJoinType = (TaskJoinType)task.getJoinType();
					TaskPosition position = (TaskPosition)task.getPosition();
					boolean keepIntroNodes = task.getKeepIntroNodes();
					String title = task.getIntroPartitionTitle();
					
					MatchProperties matchProps = (MatchProperties)task.getMatchProperties();
					
					if (matchProps != null)
					{				
						TaskMatchType taskMatchType = (TaskMatchType)matchProps.getType();
						String taskMatchPattern = matchProps.getPattern();
						String taskReplaceWith = matchProps.getReplaceWith();
						
						logger.info("\t\t Task: Type="+taskType.name()+": Src="+xPathSrc+": Join="+taskJoinType.name()+": Position="+position.name());
						logger.info("\t\t\t Match: Type="+taskMatchType.name()+": Pattern="+taskMatchPattern+": ReplaceWith="+taskReplaceWith+": AttrName="+matchProps.getAttributeName()+": AttrVal="+matchProps.getAttributeValue()+": ExprType="+matchProps.getExprType()+": ExprValue="+matchProps.getExprValue());
					}

					else
						logger.info("\t\t Task: Type="+taskType.name()+": Intro Partition Title="+title+": Keep Intro Nodes="+keepIntroNodes);
				}
			}
		}
		
		TransformationManager transformer = new TransformationManager(ruleSet, inputDom);
		
		transformer.process();
		
		// Top level partition information is needed for the final stage transformation to SupportPoint xml
		this.setTopPartition(transformer.getTopPartition());
		this.setTopPartitionId(transformer.getTopPartitionId());
		
		// Save as the business filtered and transformed xhtml
	    this.saveAsXml();
	}

	/**
	 * Private member variable RuleSet Getter and Setters methods.
	 * Get the rule set
	 * @return RuleSet
	 */
	public RuleSet getRuleSet() {
		return ruleSet;
	}

	/**
	 * Set the rule set.
	 * @param ruleSet RuleSet
	 */
	public void setRuleSet(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	/**
	 * Get the input file being processed.
	 * @return inputFile Input file name
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * Set the input file to be processed.
	 * @param inputFile Input file name
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Get the processed output file name
	 * @return outputFile Output file name
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Set the processed output file name
	 * @param outputFile Output file name
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
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
	 * @param topPartitionId Top partition id
	 */
	public void setTopPartitionId(int topPartitionId) {
		this.topPartitionId = topPartitionId;
	}
}
