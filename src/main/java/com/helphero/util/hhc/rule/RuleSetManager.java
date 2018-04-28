package com.helphero.util.hhc.rule;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class manages the loading, parsing and housing of a set of rules. 
 * The rules are loaded from the user supplied rules.xml rules template file.
 * 
 * @author jcharles
 */
public class RuleSetManager {
	static Logger logger = Logger.getLogger(RuleSetManager.class);	
	private RuleSet ruleSet = new RuleSet();
	private String rulesFile = null;
	private Document document = null;
	private boolean debug = false;

	/**
	 * Constructor
	 */
	public RuleSetManager() {
	}
	
	/**
	 * Constructor
	 * @param rulesFile - String
	 * @throws ParserConfigurationException Exception thrown attempting to create a parser instance to process the rules.xml rules template file
	 * @throws SAXException Exception thrown attempting to parse the rules.xml rules template file
	 * @throws IOException General exception thrown attempting to process the rules.xml rules template file
	 */
	public RuleSetManager(String rulesFile) throws ParserConfigurationException, SAXException, IOException {
		this.rulesFile = rulesFile;
		
		loadAsXml(rulesFile);
	}
	
	/**
	 * Load all the document conversion rules 
	 * @param rulesFile Rules file
	 * @throws ParserConfigurationException Thrown if an error occurs creating the parser instance 
	 * @throws IOException General exception thrown if any other issue occurs while loading anf parsing the rules.xml rules template file
	 * @throws SAXException Thrown if an error occurs parsing the rules.xml rules template file
	 */
	public void loadAsXml(String rulesFile) throws ParserConfigurationException, SAXException, IOException {
		this.rulesFile = rulesFile;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// At this stage
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);

	    DocumentBuilder db = dbf.newDocumentBuilder();
	    document = db.parse(rulesFile);
	}
	
	/**
	 * Retrieve a RuleSet instance.
	 * @return RuleSet
	 */
	public RuleSet getRuleSet() {
		return ruleSet;
	}
	
	/**
	 * Process all the rules.
	 */
	public void process() {		
	}
	
	/**
	 * Parse all the rules within a rules.xml input file.
	 * @throws Exception Thrown if an error occurs parsing the rules.xml rules template file
	 */
	public void parse() throws Exception {
		
		Node xRulesNode = document.getDocumentElement();
		Node xRulesAttribute = xRulesNode.getAttributes().getNamedItem("document_root");
		if (xRulesAttribute != null)
			ruleSet.setDocumentRootXPath(xRulesAttribute.getNodeValue());
		
		xRulesAttribute = xRulesNode.getAttributes().getNamedItem("debug");
		if (xRulesAttribute != null)
		{
			String sVal = xRulesAttribute.getNodeValue();
			if (sVal != null && (sVal.equals("1") || sVal.toUpperCase().equals("TRUE") || sVal.toUpperCase().equals("ON") || sVal.toUpperCase().equals("ENABLED") || sVal.toUpperCase().equals("T") || sVal.toUpperCase().equals("Y")))
			{
				this.debug = true;
				ruleSet.setDebug(true);
			}
		}
		
		if (debug) 
		{
			logger.info("=== RuleSet Manager: parse()");
			logger.info("\tRules document_root="+ruleSet.getDocumentRootXPath());
		}
		
		NodeList xRuleList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < xRuleList.getLength(); i++)
		{
			Node xRule = xRuleList.item(i);
			if (xRule instanceof Element && xRule.getNodeName().equalsIgnoreCase("rule")) {
				Rule rule = new Rule();

				// Set the Rule type attribute value
				Node xRuleAttribute = xRule.getAttributes().getNamedItem("type");
				if (xRuleAttribute != null) 
					rule.setType(xRuleAttribute.getNodeValue());
				else
					rule.setType(RuleType.NOT_SET);
				logger.info("\tRule Type="+xRuleAttribute.getNodeValue().toUpperCase());
				
				// Set the Rule sub type attribute value
				xRuleAttribute = xRule.getAttributes().getNamedItem("sub_type");
				if (xRuleAttribute != null) 
					rule.setSubType(xRuleAttribute.getNodeValue());
				else
					rule.setSubType(RuleSubType.NOT_SET);
				logger.info("\tRule SubType="+rule.getSubType().toString());
				
				// Set the Rule target attribute value
				xRuleAttribute = xRule.getAttributes().getNamedItem("target");
				if (xRuleAttribute != null) 
					rule.setTarget(xRuleAttribute.getNodeValue());
				else
					rule.setTarget(RuleTarget.NOT_SET);
				logger.info("\tRule target="+xRuleAttribute.getNodeValue().toUpperCase());
				
				// Set the Rule target attribute value
				xRuleAttribute = xRule.getAttributes().getNamedItem("target_type");
				if (xRuleAttribute != null) 
					rule.setTargetType(xRuleAttribute.getNodeValue());
				else
					rule.setTargetType(RuleTargetType.PROCEDURE);
				logger.info("\tRule target_type="+rule.getTargetType().toString());
				
				// Set the Rule id attribute value. This allows multiple rules of the same type with different tasks to be executed.
				xRuleAttribute = xRule.getAttributes().getNamedItem("id");
				if (xRuleAttribute != null) 
					rule.setId(xRuleAttribute.getNodeValue());
				logger.info("\tRule id="+rule.getId());
				
				xRuleAttribute = xRule.getAttributes().getNamedItem("disable");
				if (xRuleAttribute != null) {
					rule.setDisable(xRuleAttribute);
					logger.info("\tRule disable="+xRuleAttribute.getNodeValue().toUpperCase());
				}
				
				NodeList xTasksList = xRule.getChildNodes();
				for (int j = 0; j < xTasksList.getLength(); j++)
				{
					Node xTasks = xTasksList.item(j);
					
					if (xTasks instanceof Element && xTasks.getNodeName().equals("tasks"))
					{								
						NodeList xTaskList = xTasks.getChildNodes();
						for (int k = 0; k < xTaskList.getLength(); k++)
						{
							Node xTask = xTaskList.item(k);
													
							if (xTask instanceof Element && xTask.getNodeName().equalsIgnoreCase("task"))
							{
								Task task = new Task();
								
								// Set the Task type attribute value
								Node xTaskAttribute = xTask.getAttributes().getNamedItem("type");
								if (xTaskAttribute != null)
									task.setType(xTaskAttribute.getNodeValue());
								else
									task.setType(TaskType.NOT_SET);
								logger.info("\t\tTask type="+xTaskAttribute.getNodeValue().toUpperCase());
								
								// Setup all the html element parameters
								if (task.getType() == TaskType.ELEMENT) {
									HtmlElement elem = new HtmlElement();
									HtmlElementType elemType = HtmlElementType.P; // default value is paragrpah
									
									xTaskAttribute = xTask.getAttributes().getNamedItem("name");
									if (xTaskAttribute != null) {
										elemType.setType(xTaskAttribute.getNodeValue());
										logger.info("\t\tElement type or name="+elemType.getType()+" : Node Value="+xTaskAttribute.getNodeValue());
									}										

									elem.setType(elemType.getType());
									logger.info("\t\tElement type="+elem.getType());
									
									// Set the Task element id attribute value
									xTaskAttribute = xTask.getAttributes().getNamedItem("id");
									if (xTaskAttribute != null)
										elem.setId(xTaskAttribute.getNodeValue());
									logger.info("\t\tElement id="+elem.getId());

									// Set the Task element parent id attribute value
									xTaskAttribute = xTask.getAttributes().getNamedItem("parentid");
									if (xTaskAttribute != null)
										elem.setParentId(xTaskAttribute.getNodeValue());
									logger.info("\t\tElement parentid="+elem.getParentId());
									
									xTaskAttribute = xTask.getAttributes().getNamedItem("style");
									if (xTaskAttribute != null)
										elem.setStyleValue(xTaskAttribute.getNodeValue());
									logger.info("\t\tElement style="+elem.getStyleValue());

									xTaskAttribute = xTask.getAttributes().getNamedItem("class");
									if (xTaskAttribute != null)
										elem.setClassValue(xTaskAttribute.getNodeValue());
									logger.info("\t\tElement class="+elem.getClassValue());
									
									xTaskAttribute = xTask.getAttributes().getNamedItem("src");
									if (xTaskAttribute != null)
										elem.setSrc(xTaskAttribute.getNodeValue());
									logger.info("\t\tElement src="+elem.getSrc());
																	
									if (xTask.getTextContent() != null)
										elem.setValue(xTask.getTextContent());
									logger.info("\t\tElement value="+elem.getValue());
									
									task.setHtmlElement(elem);
								}
								
								// Set the Task src attribute value
								xTaskAttribute = xTask.getAttributes().getNamedItem("src");
								if (xTaskAttribute != null)
									task.setSource(xTaskAttribute.getNodeValue());
								logger.info("\t\tTask src="+task.getSource());
								
								// Set the Task join attribute value
								xTaskAttribute = xTask.getAttributes().getNamedItem("join");
								if (xTaskAttribute != null && xTaskAttribute.getNodeValue() != null)
								{
									task.setJoinType(xTaskAttribute.getNodeValue());
									logger.info("\t\tTask join="+xTaskAttribute.getNodeValue());
								}
								else
									task.setJoinType(TaskJoinType.NOT_SET);
								
								// Set the Task position attribute value
								xTaskAttribute = xTask.getAttributes().getNamedItem("position");
								if (xTaskAttribute != null && xTaskAttribute.getNodeValue() != null)
								{
									task.setPosition(xTaskAttribute.getNodeValue());
									logger.info("\t\tTask position="+xTaskAttribute.getNodeValue());
								}
								else
									task.setPosition(TaskPosition.NOT_SET);
								
								// Set the Task dependent type attribute value
								xTaskAttribute = xTask.getAttributes().getNamedItem("dependent");
								if (xTaskAttribute != null)
									task.setDependentType(xTaskAttribute.getNodeValue());
								else
									task.setDependentType(TaskType.NOT_SET);
								logger.info("\t\tTask dependent task type="+task.getDependentType());
								
								xTaskAttribute = xTask.getAttributes().getNamedItem("keep_intro_nodes");
								if (xTaskAttribute != null)
									task.setKeepIntroNodes(xTaskAttribute.getNodeValue());
								else
									task.setDependentType(TaskType.NOT_SET);
								logger.info("\t\tTask keep_intro_nodes="+task.getKeepIntroNodes());
								
								xTaskAttribute = xTask.getAttributes().getNamedItem("target_title");
								if (xTaskAttribute != null)
									task.setTargetTitle(xTaskAttribute.getNodeValue());
								logger.info("\t\tTarget Title="+task.getTargetTitle());								
								
								xTaskAttribute = xTask.getAttributes().getNamedItem("intro_partition_title");
								if (xTaskAttribute != null)
									task.setIntroPartitionTitle(xTaskAttribute.getNodeValue());
								else
									task.setDependentType(TaskType.NOT_SET);
								logger.info("\t\tTask intro_nodes_title="+task.getIntroPartitionTitle());
								
								NodeList xMatchList = xTask.getChildNodes();
								for (int m = 0; m < xMatchList.getLength(); m++)
								{
									Node xMatch = xMatchList.item(m);
									
									if (xMatch instanceof Element && xMatch.getNodeName().equalsIgnoreCase("match"))
									{
										MatchProperties match = new MatchProperties();
										
										// Set the Match attribute name
										Node xMatchAttribute = xMatch.getAttributes().getNamedItem("attr");
										if (xMatchAttribute != null)
											match.setAttributeName(xMatchAttribute.getNodeValue());
										logger.info("\t\t\tMatch AttrName="+match.getAttributeName());
										
										// Set the Match attribute name value
										xMatchAttribute = xMatch.getAttributes().getNamedItem("attr_value");
										if (xMatchAttribute != null)
											match.setAttributeValue(xMatchAttribute.getNodeValue());
										logger.info("\t\t\tMatch AttrValue="+match.getAttributeValue());
										
										// Set the Match type attribute value
										xMatchAttribute = xMatch.getAttributes().getNamedItem("type");
										if (xMatchAttribute != null)
											match.setType(xMatchAttribute.getNodeValue().toUpperCase());
										else
											match.setType(TaskMatchType.NOT_SET);
										logger.info("\t\t\tMatch Type="+match.getType());

										// Set the Match expression type value i.e. Any of: starts-with, startswith, starts_with, contains upper or lower case
										xMatchAttribute = xMatch.getAttributes().getNamedItem("expr");
										if (xMatchAttribute != null)
											match.setExprType(xMatchAttribute.getNodeValue().toUpperCase());
										logger.info("\t\t\tMatch Expr Type="+match.getExprType());
										
										// Set the Match expression type value
										xMatchAttribute = xMatch.getAttributes().getNamedItem("expr_value");
										if (xMatchAttribute != null)
											match.setExprValue(xMatchAttribute.getNodeValue());
										logger.info("\t\t\tMatch Expr Value="+match.getExprValue());										
										
										// Set the Match pattern attribute value
										xMatchAttribute = xMatch.getAttributes().getNamedItem("pattern");
										if (xMatchAttribute != null && xMatchAttribute.getNodeValue() != null)
										{
											match.setPattern(xMatchAttribute.getNodeValue());
											logger.info("\t\t\tMatch Pattern="+match.getPattern());
										}
										
										// Set the Match replace-with attribute value
										xMatchAttribute = xMatch.getAttributes().getNamedItem("replacewith");
										if (xMatchAttribute != null && xMatchAttribute.getNodeValue() != null)
										{
											match.setReplaceWith(xMatchAttribute.getNodeValue());
											logger.info("\t\t\tMatch ReplaceWith="+match.getReplaceWith());
										}
										
										task.setMatchProperties(match);
				
										break;
									}
								}
		
								rule.addTask(task);
							}
						} // end for TaskList
					} // end if xTasks
				} // end for TasksList
				
				rule.setDocumentRootXPath(ruleSet.getDocumentRootXPath());
				
				rule.hashTasks();
				
				ruleSet.add(rule);
				
			} // if rule
		} // end for RuleList
		
		ruleSet.hashRules();
	}
	
	/**
	 * Enable or disable internal debugging 
	 * @param debug - boolean
	 */
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}
	
	/**
	 * Is internal debugging enabled?
	 * @return boolean
	 */
	public boolean isDebug()
	{
		return this.debug;
	}
}
