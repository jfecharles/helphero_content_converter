package com.helphero.util.hhc.dom.processing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.processing.FileConverter;
import com.helphero.util.hhc.rule.IRule;
import com.helphero.util.hhc.rule.ITaskExprType;
import com.helphero.util.hhc.rule.ITaskMatchType;
import com.helphero.util.hhc.rule.MatchProperties;
import com.helphero.util.hhc.rule.RuleTargetType;
import com.helphero.util.hhc.rule.Task;
import com.helphero.util.hhc.rule.TaskExprType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * Abstract class implementing partitioning executed under the umbrella of a transform.
 * This class provides all the methods to perform general xHtml common object model document partitioning.
 * For partition specific partitioning this class should be sub-classes and the appropriate methods overwritten.   
 * @author jcharles
 */
public abstract class Partitioner implements IPartitioner, ITransform {
	static Logger logger = Logger.getLogger(Partitioner.class);
	protected Document document = null;
	private XPath xPath =  null;
	private PartitionType parentContainerType = PartitionType.NOT_SET;
	private String parentContainerXPath = null;
	private String documentRootXPath = null;
	private ITaskExprType attrMatchExpr = TaskExprType.NOT_SET;
	private String matchingAttributeName = null;
	private String matchingAttributeValue = null;
	private String matchingElementXPath = null;
	private PartitionType partitionType = PartitionType.NOT_SET;
	private RuleTargetType targetType = RuleTargetType.NOT_SET;
	private Partition firstPartition = null;
	private int firstPartitionId = -1;
	private String introPartitionTitle = "Preliminaries";
	private boolean keepFirstElements = true;
	private boolean pruneEmptyChildren = false;
	private IRule rule;
	private IDevolutionCondition condition;
	private ITaskMatchType attributeMatchType;
	private String attributeMatchRegexPattern;
	
	// Title name match properties
	private boolean titleNameMatchCondition = false;
	private ITaskMatchType titleNameMatchType = TaskMatchType.NOT_SET;
	private ITaskExprType titleNameMatchExprType = TaskExprType.NOT_SET;
	private String titleNameMatchExprValue;
	private String titleNameMatchRegexPattern;
	// Title name regex replacement properties
	private boolean titleNameReplaceCondition = false;
	private String titleNameReplaceRegexPattern = null;
	private String titleNameReplaceWithRegexPattern = null;
	private int lastFolderId = 0;
	private boolean debug = false;

	public Partitioner() {
		// TODO Auto-generated constructor stub
		xPath =  XPathFactory.newInstance().newXPath();
	}
	
	/**
	 * A method for child classes to initialise parameters relevant to the type of child class.
	 */
	public abstract void initialise();
	
	/**
	 * Convenience method to return the XPath.
	 * 
	 * @return XPath
	 */
	protected XPath getXPathHandler()
	{
		return xPath;
	}
	
	/**
	 * Convenience method to derive the Matching Element XPath from parameters already set.
	 */
	protected void deriveMatchingElementXPath()
	{
		String pcXPath = getParentContainerXPath();
		String base = pcXPath.substring(0, pcXPath.indexOf("["));
		
		StringBuffer sb = new StringBuffer();
		sb.append(base);
		sb.append("[");
		
		if (getAttributeMatchExpression().equals(TaskExprType.STARTS_WITH))
		{
			sb.append("starts-with(");
		}
		else if (getAttributeMatchExpression().equals(TaskExprType.CONTAINS))
		{
			sb.append("contains(");
		}

		sb.append(getMatchingAttributeName());
		sb.append(",'");
		sb.append(getMatchingAttributeValue());
		sb.append("')]");
		
		/*
		 *  Should look like this:
		 *  setMatchingElementXPath("//body/div/div[starts-with(@class,'Heading1')]");
		 */
		setMatchingElementXPath(sb.toString());
	}
	
	/**
	 * This method sets the XPath to the elements that mark the partitions within the document.
	 * 
	 * @param sXPath - XPath String to the Partition Matching Elements
	 */
	public void setMatchingElementXPath(String sXPath)
	{
		this.matchingElementXPath = sXPath;
	}
	
	/**
	 * This method retrieves the XPath to the elements that mark the partitions within the document.
	 * 
	 * @return String The XPath to the matching element(s)
	 */
	public String getMatchingElementXPath()
	{
		return this.matchingElementXPath;
	}
	
	/**
	 * This method sets the name of the partition element attribute used to determine which elements are used as partitions.
	 *  
	 * @param name Name of the partition element attribute used to determine which elements are used as partitions
	 */
	public void setMatchingAttributeName(String name)
	{
		this.matchingAttributeName = name;
	}
	
	/**
	 * This method returns the name of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @return matchingAttributeName Matching attribute name
	 */
	public String getMatchingAttributeName()
	{
		return this.matchingAttributeName;
	}
	
	/**
	 * This method sets the value of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @param value Value of the partition element attribute used to determine which elements are used as partitions
	 */
	public void setMatchingAttributeValue(String value)
	{
		this.matchingAttributeValue = value;
	}
	
	/**
	 * This method gets the value of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @return matchingAttributeValue Matching Attribute Value
	 */
	public String getMatchingAttributeValue()
	{
		return this.matchingAttributeValue;
	}
	
	/**
	 * This method sets the type of expression match that is used on the element attribute used to determine which elements are used as partitions.
	 * 
	 * @param value Type of expression match that is used on the element attribute used to determine which elements are used as partitions
	 */
	public void setAttributeMatchExpression(ITaskExprType value)
	{
		this.attrMatchExpr = value;
	}
	
	/**
	 * This method gets the type of expression match that is used on the element attribute used to determine which elements are used as partitions.
	 * 
	 * @return attrMatchExpr Type of expression match that is used on the element attribute used to determine which elements are used as partitions
	 */
	public ITaskExprType getAttributeMatchExpression()
	{
		return attrMatchExpr;
	}
	
	/**
	 * This method provides the xPath to the node or nodes the content of which will be used for partitioning.
	 * Folder partitioning is applied to siblings of this node.
	 * For Document/Section/Task/etc. partitioning, this method provides the xPath to a node or nodes whose children will be
	 * used for partitioning.
	 *    
	 * @param sXPath Parent Container XPath
	 */
	public void setParentContainerXPath(String sXPath)
	{
		this.parentContainerXPath = sXPath;
	}
	
	/**
	 * Retrieve the xPath to the node or nodes whose children are used for partitioning.
	 * 
	 * @return parentContainerXPath XPath string to the node or nodes whose children are used for partitioning
	 */
	public String getParentContainerXPath()
	{
		return this.parentContainerXPath;
	}
	
	/**
	 * Set the xHtml xPath to the root document node
	 * @param sXPath XPath string to the root document node
	 */
	public void setDocumentRootXPath(String sXPath)
	{
		this.documentRootXPath = sXPath;
	}
	
	/**
	 * Get the xHtml xPath to the root document node
	 * @return documentRootXPath XPath string to the root document node
	 */
	public String getDocumentRootXPath()
	{
		return this.documentRootXPath;
	}
	
	/**
	 * This method returns a node or nodes whose siblings for folder partitioning or children for document/section/task partitioning 
	 * will be used to further partition a document.
	 * @return nodeList List of nodes matching the parent container XPath expression
	 * @throws XPathExpressionException Exception thrown if XPath compilation of the parent container XPath is invalid
	 */
	public NodeList getParentContainerNodes() throws XPathExpressionException
	{
		NodeList nodeList = (NodeList)xPath.compile(this.parentContainerXPath).evaluate(this.document, XPathConstants.NODE);
		
		return nodeList;
	}
	
	/**
	 * This method sets the type of parent container. The parent container determines 
	 * whether sibling (for folders) or child nodes (for document/section/tasks) are used to further partition a document.
	 * @param type Type of parent container used to further partition a document
	 */
	public void setParentContainerType(IPartitionType type)
	{
		this.parentContainerType = (PartitionType) type;
	}
	
	/**
	 * This method retrieves the type of parent container.
	 * 
	 * @return parentContainerType Type of parent container - IPartitionType implementation instance - PartitionType
	 */
	public IPartitionType getParentContainerType()
	{
		return parentContainerType;
	}
	
	/**
	 * Method to check if the document contains partitions of this type. Used by the folder partitioner and document partitioner.
	 * @return boolean Does this document contain partitions
	 */
	protected boolean hasPartitions()
	{
		boolean hasPartitions = false;
		XPath xPath = getXPathHandler();
		NodeList parentNodes;
		try {
			parentNodes = (NodeList)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			for (int i = 0; i < parentNodes.getLength(); i++) 
			{        	
				Node parentNode = (Node)parentNodes.item(i);
				
				if (parentNode instanceof Element)
				{					
					NodeList nodeList = parentNode.getChildNodes();
					
					Node xIntroDivNode = this.document.createElement("div");
					((Element)xIntroDivNode).setAttribute("class", this.getPartitionTypeAsString());
					((Element)xIntroDivNode).setAttribute("title", this.getIntroPartitionTitle());
							        
					for (int j = 0; j < nodeList.getLength(); j++) 
					{        	
			         	Node xNode = nodeList.item(j);
			         	if (xNode instanceof Element)
			         	{			         		
			         		String sClass = xNode.getAttributes().getNamedItem("class") != null ? xNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
				         	
				         	if (sClass != null)
				         	{				         		
				         		boolean match = false;
				         		switch ((TaskExprType)this.getAttributeMatchExpression())
				         		{
				         		case NOT_SET:
				         		case STARTS_WITH:
				         			match = sClass.startsWith(this.getMatchingAttributeValue());
				         			break;
				         		case CONTAINS:
				         			match = sClass.contains(this.getMatchingAttributeValue());
				         			break;
				         		}
				         		
				         		// Process the title name match condition if it exists
				         		String title = null;
				         		if (match)
				         		{
				         			title = DomUtils.extractTextChildren(xNode);
				         			
				         			if (this.isTitleNameMatchCondition())
				         			{
					         			if (title != null && this.getTitleNameMatchType() == TaskMatchType.EXPR)
					         			{
					         				switch ((TaskExprType)this.getTitleNameMatchExprType())
					         				{
					         					case STARTS_WITH:
					         						match = title.startsWith(this.getTitleNameMatchExprValue());
					         						break;
					         					case CONTAINS:
					         						match = title.contains(this.getTitleNameMatchExprValue());
					         						break;
					         					case NOT_SET:
					         						break;
					         					default:
					         						break;
					         				}
					         				
					         			} else if (this.getTitleNameMatchType() == TaskMatchType.REGEX)
					         			{
						         			Pattern r = Pattern.compile(this.getTitleNameMatchRegexPattern());			         			
						         			Matcher m = r.matcher(title);			         			
						         			match = m.matches();
					         			}
				         			}
				         		}
				         		
				         		if (match) hasPartitions = match;
				         	}	         	
			         	}
			        } // end for j
				} // end if parentNode
			} // end for i
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		logger.info(">>> " + this.getPartitionType().name() + " Partitioner: hasPartitions()="+hasPartitions);
		
		return hasPartitions;
	}
	/**
	 *  Do the partitioning of the document. 
	 */	
	public void partition() 
	{
		XPath xPath = getXPathHandler();
		NodeList parentNodes;
		try {
			parentNodes = (NodeList)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info(">>> " + this.getPartitionType().name() + " Partitioner: partition()");
			
			for (int i = 0; i < parentNodes.getLength(); i++) 
			{        	
				Node parentNode = (Node)parentNodes.item(i);
				
				if (parentNode instanceof Element)
				{
					String sParentClass = parentNode.getAttributes().getNamedItem("class") != null ? parentNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
					logger.info("\tParent Node Name=" + parentNode.getNodeName()+":class="+ sParentClass);
					
					NodeList nodeList = parentNode.getChildNodes();
					Node xDivNode = null;
					
					Node xIntroDivNode = this.document.createElement("div");
					((Element)xIntroDivNode).setAttribute("class", this.getPartitionTypeAsString());
					((Element)xIntroDivNode).setAttribute("title", this.getIntroPartitionTitle());
					
					boolean first = true;
					boolean keepFirst = this.getKeepFirstElements();
					boolean hasIntroNode = false;
			        
					for (int j = 0, pCount = 0; j < nodeList.getLength(); j++) 
					{        	
			         	Node xNode = nodeList.item(j);
			         	if (xNode instanceof Element)
			         	{
			         		if (first && keepFirst)
			         		{
			         			if (!hasIntroNode) {
				         			((Element)xIntroDivNode).setAttribute("id", Integer.toString(++pCount));
			         				DomUtils.insertBefore(xIntroDivNode, xNode);		         				
				         			j++;
				         			hasIntroNode = true;
			         			}
			         		}
			         		
			         		String sClass = xNode.getAttributes().getNamedItem("class") != null ? xNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
				         	boolean isTable = xNode.getNodeName().equalsIgnoreCase("table") ? true : false;
				         	String sId = isTable && xNode.getAttributes().getNamedItem("id") != null ? xNode.getAttributes().getNamedItem("id").getNodeValue() : "";
				         	
				         	if (sClass != null)
				         	{
				         		logger.info("\tNode Name=" + xNode.getNodeName()+":class="+ sClass);
				         		
				         		boolean match = false;
				         		switch ((TaskExprType)this.getAttributeMatchExpression())
				         		{
				         		case NOT_SET:
				         		case STARTS_WITH:
				         			match = sClass.startsWith(this.getMatchingAttributeValue());
				         			break;
				         		case CONTAINS:
				         			match = sClass.contains(this.getMatchingAttributeValue());
				         			break;
				         		}
				         		
				         		// Process the title name match condition if it exists
				         		String title = null;
				         		if (match)
				         		{
				         			title = DomUtils.extractTextChildren(xNode);
				         			
				         			if (this.isTitleNameMatchCondition())
				         			{
					         			if (title != null && this.getTitleNameMatchType() == TaskMatchType.EXPR)
					         			{
					         				switch ((TaskExprType)this.getTitleNameMatchExprType())
					         				{
					         					case STARTS_WITH:
					         						match = title.startsWith(this.getTitleNameMatchExprValue());
					         						break;
					         					case CONTAINS:
					         						match = title.contains(this.getTitleNameMatchExprValue());
					         						break;
					         					case NOT_SET:
					         						break;
					         					default:
					         						break;
					         				}
					         				
					         			} else if (this.getTitleNameMatchType() == TaskMatchType.REGEX)
					         			{
						         			Pattern r = Pattern.compile(this.getTitleNameMatchRegexPattern());			         			
						         			Matcher m = r.matcher(title);			         			
						         			match = m.matches();
					         			}
				         			}
				         		}
				         		
				         		if (match)
				         		{
				         			if (first)
				         			{
				         				Partition partition = new Partition();
				         				partition.setType(this.getPartitionType());
				         				partition.setsId(sId);
				         				this.setFirstPartition(partition);
				         			}

				         			first = false;
				         			
				         			if (this.isTitleNameReplaceCondition())
				         			{
				         				Pattern r = Pattern.compile(this.getTitleNameReplaceRegexPattern());			         			
					         			Matcher m = r.matcher(title);	
					         			title = m.replaceAll(this.getTitleNameReplaceWithRegexPattern());
				         			}
				         			
				         			xDivNode = this.document.createElement("div");
				         			((Element)xDivNode).setAttribute("class", getPartitionTypeAsString());
				         			((Element)xDivNode).setAttribute("title", title);
				         			((Element)xDivNode).setAttribute("id", Integer.toString(++pCount));
				         			
				         			DomUtils.insertBefore(xDivNode, xNode);
				         			DomUtils.removeElement((Element) xNode, false);	 
				         		}
				         		else
				         		{
				         			// Skip over all nodes leading up to the first Heading1 node
				         			if (!first && xDivNode != null)
				         			{
				         				xDivNode.appendChild(xNode);
				         				j--;
				         			}
				         			else if (first && keepFirst && xIntroDivNode != null)
				         			{
				         				xIntroDivNode.appendChild(xNode);
				         				j--;
				         			}
				         		}
				         	} else if (isTable && sId != null)
				         	{
				         		logger.info("\t\tNode Name=" + xNode.getNodeName()+":id="+ sId);
			         			// Skip over all nodes leading up to the first Heading1 node
			         			if (!first && xDivNode != null)
			         			{
			         				xDivNode.appendChild(xNode);
			         				j--;
			         			}
			         			else if (first && keepFirst && xIntroDivNode != null)
			         			{
			         				xIntroDivNode.appendChild(xNode);
			         				j--;
			         			}
				         	}	         	
			         	}
			        } // end for j
				} // end if parentNode
			} // end for i
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}	
	
	/**
	 * Set the document (DOM) to partition.
	 * @param doc Document DOM to partition
	 */
	public void setDocument(Document doc)
	{
		this.document = doc;
	}
	
	/**
	 * Get the document (DOM) to partition.
	 *  
	 * @return document Document DOM to partition
	 */
	public Document getDocument()
	{
		return this.document;
	}

	/**
	 * Get the partition type as a string
	 * @return type Type of partition expressed as a string
	 */
	public String getPartitionTypeAsString() {
		String type = null;
		
		switch (partitionType) {
		case FOLDER:
			type = "Folder";
			break;
		case DOCUMENT:
			type = "Document";
			break;
		case NOT_SET:
			type = "Document";
			break;
		case ROOT:
			type = "Document";
			break;
		case SECTION:
			type = "Section";
			break;
		case TASK:
			type = "Task";
			break;
		default:
			break;
		}
		return type;
	}

	/**
	 * Get the partition type
	 * @return partitionType Partition Type
	 */
	public PartitionType getPartitionType() {
		return partitionType;
	}

	/**
	 * Set the partition type
	 * @param partitionType Partition Type
	 */
	public void setPartitionType(PartitionType partitionType) {
		this.partitionType = partitionType;
	}
	
	/**
	 * Get the target type as a string
	 * 
	 * @return type Target type
	 */
	public String getTargetTypeAsString() {
		String type = null;
		
		switch (targetType) {
		case PROCEDURE:
			type = "procedure";
			break;
		case POLICY:
			type = "policy";
			break;
		case PROCESS:
			type = "process";
			break;
		case EXTERNAL:
			type = "external";
			break;
		case NOT_SET:
			type = "procedure";
			break;
		default:
			type = "procedure";
			break;
		}
		return type;
	}

	/**
	 * Get the rule target type
	 * @return targetType Rule Target Type
	 */
	public RuleTargetType getTargetType() {
		return targetType;
	}

	/**
	 * Set the rule target type
	 * @param targetType Rule Target Type
	 */	
	public void setTargetType(RuleTargetType targetType) {
		this.targetType = targetType;
	}
	
	/**
	 * All information between the start and the first partition node are placed in an "Preliminaries" partition so that no information in 
	 * the xHtml document is lost. This method sets the title for that partition. The default is {PartitionType} + " Preliminaries".
	 * @param title Introductory Partition Title 
	 */
	public void setIntroPartitionTitle(String title)
	{
		this.introPartitionTitle = title;
	}

	/**
	 * Get the introductory partition title.
	 * @return introPartitionTitle Introductory Partition Title 
	 */
	public String getIntroPartitionTitle()
	{
		return this.introPartitionTitle;
	}

	/**
	 * All of the child elements of the introductory partition can be preserved (true) or discarded (false). This setter method indicates
	 * whether the child content will be preserved. 
	 * @param keep Boolean flag to indicate if child element of the introductory partition will be preserved.
	 */
	public void setKeepFirstElements(boolean keep)
	{
		this.keepFirstElements = keep;
	}
	
	/**
	 * Get the boolean value indicating if the introductory partition child elements will be preserved or discarded.
	 * @return boolean
	 */
	public boolean getKeepFirstElements()
	{
		return this.keepFirstElements;
	}
	
	/**
	 * Do the partitioning. Placeholder to satisfy the transform interface run method allowing this method to be executed from a
	 * derived transform.
	 */
	public void run()
	{
		this.partition();
	}
	
	/**
	 * Not currently used.
	 */
	public void setDevolutionCondition(IDevolutionCondition condition)
	{
		this.condition = condition;
	}
	
	/**
	 * Not currently used.
	 */
	public IDevolutionCondition getDevolutionCondition() {
		return condition;
	}
	
	/**
	 * Set the partition rule
	 * @param rule IRule implementation instance - Rule
	 */
	public void setRule(IRule rule)
	{
		this.rule = rule;
	}

	/**
	 * Get the partition rule
	 * @return rule IRule implementation instance - Rule
	 */
	public IRule getRule()
	{
		return this.rule;
	}
	
	/**
	 * This method sets up all the properties to drive partitioning
	 */
	public void interpret()
	{	
		// Setup all the common properties derived from task definitions for all partition types.
		IRule rule = getRule();
		
		Task task = (Task) rule.getTaskMap().get(TaskType.SRC_PATH);

		MatchProperties match = (MatchProperties)task.getMatchProperties();
		
		this.setMatchingAttributeName(match.getAttributeName());
		this.setAttributeMatchType(match.getType());
		
		if (match.getType() == TaskMatchType.EXPR)
		{
			this.setMatchingAttributeValue(match.getAttributeValue());
			this.setAttributeMatchExpression(match.getExprType());
		}
		else if (match.getType() == TaskMatchType.REGEX)
		{
			this.setAttributeMatchRegexPattern(match.getPattern());
		}
		
		if (match.getType() == TaskMatchType.EXPR)
		{
			// "//body/div/p[starts-with(@class,'HeadingProcessName ')]"			
			StringBuffer sb = new StringBuffer();
			sb.append(task.getSource());
			switch (match.getExprType())
			{
			case STARTS_WITH:
			case CONTAINS:
				sb.append("[");
				sb.append(match.getExprTypeAsString());
				sb.append("(@");
				sb.append(match.getAttributeName());
				sb.append(",'");
				sb.append(match.getAttributeValue());
				sb.append("')]");
				setMatchingElementXPath(sb.toString());
				break;
			case NOT_SET:
				break;
			default:
				break;	
			}
		}
		else if (match.getType() == TaskMatchType.REGEX)
		{
			// "//body/div/p[matches(@class,'{pattern}')]"
			StringBuffer sb = new StringBuffer();
			sb.append(task.getSource());
			sb.append("[");
			sb.append("matches");
			sb.append("(@");
			sb.append(match.getAttributeName());
			sb.append(",'");
			sb.append(match.getPattern());
			sb.append("')]");
			setMatchingElementXPath(sb.toString());
		}
		
		// Process the title match properties. This is an optional directive.
		if (rule.getTaskMap().containsKey(TaskType.SRC_TEXT_MATCH))
		{
			task = (Task) rule.getTaskMap().get(TaskType.SRC_TEXT_MATCH);
			
			match = (MatchProperties)task.getMatchProperties();
			
			if (match != null)
			{
				this.setTitleNameMatchCondition(true);				
				this.setTitleNameMatchType(match.getType());
				
				if (match.getType() == TaskMatchType.EXPR)
				{
					this.setTitleNameMatchExprType(match.getExprType());
					this.setTitleNameMatchExprValue(match.getExprValue());
				}
				else if (match.getType() == TaskMatchType.REGEX)
				{
					this.setTitleNameMatchRegexPattern(match.getPattern());
				}
				else
					this.setTitleNameMatchCondition(false);
			}
		}
		
		// Process the title replace regex properties. This is an optional directive.
		if (rule.getTaskMap().containsKey(TaskType.TARGET_NAME))
		{
			task = (Task) rule.getTaskMap().get(TaskType.TARGET_NAME);
			match = (MatchProperties)task.getMatchProperties();
			if (match != null && match.getPattern() != null && match.getReplaceWith() != null)
			{
				this.setTitleNameReplaceCondition(true);
				this.setTitleNameReplaceRegexPattern(match.getPattern());
				this.setTitleNameReplaceWithRegexPattern(match.getReplaceWith());
			}
		}
		
		// Process the partition options
		if (rule.getTaskMap().containsKey(TaskType.PARTITION_OPTIONS))
		{
			task = (Task) rule.getTaskMap().get(TaskType.PARTITION_OPTIONS);
			if (task.getKeepIntroNodes())
				this.setKeepFirstElements(task.getKeepIntroNodes());
			if (task.getIntroPartitionTitle() != null)
				this.setIntroPartitionTitle(task.getIntroPartitionTitle());
		}

	}
	
	/**
	 * Do the partitioning.
	 */
	public void process() throws TransformException
	{
		partition();
	}
	
	/**
	 * Method used by derived classes to check if this operation is valid.
	 * @return true Is this a valid operation. Overriden by sub-classes
	 */
	public boolean isValidOperation(String srcClass, String targetClass, TaskType operation) throws InvalidOperationException
	{
		return true;
	}
	
	/**
	 * Not currently used.
	 * @param prune Prune any empty partitions
	 */
	public void setPruneEmptyPartition(boolean prune)
	{
		this.pruneEmptyChildren = prune;
	}

	/**
	 * Not currently used.
	 * @return pruneEmptyChildren Prune empty partitions
	 */
	public boolean isPruneEmptyPartition()
	{
		return this.pruneEmptyChildren;
	}
	
	/**
	 * The following methods are used to set or retrieve matching conditions for a partition attribute.
	 * Get the attribute match type
	 * @return attributeMatchType Task Match Type implementation instance
	 */
	public ITaskMatchType getAttributeMatchType()
	{
		return this.attributeMatchType;
	}
	
	/**
	 * Set the attribute match type
	 * @param type Task Match Type implementation instance
	 */
	public void setAttributeMatchType(ITaskMatchType type)
	{
		this.attributeMatchType = type;
	}
	
	/**
	 * Set the matching regular expression for regex match type.
	 * @param pattern Match element regular expression pattern
	 */
	public void setAttributeMatchRegexPattern(String pattern)
	{
		this.attributeMatchRegexPattern = pattern;
	}
	
	/**
	 * Get the matching regular expression pattern.
	 * @return attributeMatchRegexPattern Match element regular expression pattern
	 */
	public String getAttributeMatchRegexPattern()
	{
		return this.attributeMatchRegexPattern;
	}

	/**
	 * The following methods are used to set or retrieve matching conditions for a partition title.
	 * Get the title attribute match type
	 * @return titleNameMatchType  Task Match Type implementation instance for the title
	 */
	public ITaskMatchType getTitleNameMatchType() {
		return titleNameMatchType;
	}

	/**
	 * Set the title for a name match type
	 * @param titleNameMatchType ITaskMatchType implementation instance - TaskMatchType
	 */
	public void setTitleNameMatchType(ITaskMatchType titleNameMatchType) {
		this.titleNameMatchType = titleNameMatchType;
	}

	/**
	 * Get the title for a name match type
	 * @return titleNameMatchExprType Title for a name match type
	 */
	public ITaskExprType getTitleNameMatchExprType() {
		return titleNameMatchExprType;
	}

	/**
	 * Set the title for a name match expression type i.e. startsWith, contains, endsWith etc.
	 * @param titleNameMatchExprType Title for a name match type
	 */
	public void setTitleNameMatchExprType(ITaskExprType titleNameMatchExprType) {
		this.titleNameMatchExprType = titleNameMatchExprType;
	}

	/**
	 * Get the expression value for a title name match
	 * @return titleNameMatchExprValue Title name matching expression value
	 */
	public String getTitleNameMatchExprValue() {
		return titleNameMatchExprValue;
	}

	/**
	 * Set the expression value for a title name match
	 * @param titleNameMatchExprValue Title name matching expression value
	 */
	public void setTitleNameMatchExprValue(String titleNameMatchExprValue) {
		this.titleNameMatchExprValue = titleNameMatchExprValue;
	}

	/**
	 * Get the regex value for a title name match
	 * @return titleNameMatchRegexPattern Title name match regular expression pattern String
	 */
	public String getTitleNameMatchRegexPattern() {
		return titleNameMatchRegexPattern;
	}

	/**
	 * Set the regex value for a title name match
	 * @param titleNameMatchRegexPattern Title name match regular expression pattern String
	 */
	public void setTitleNameMatchRegexPattern(String titleNameMatchRegexPattern) {
		this.titleNameMatchRegexPattern = titleNameMatchRegexPattern;
	}

	/**
	 * Retrieve flag to indicate if the title name is to be replaced.
	 * @return titleNameReplaceCondition Boolean flag to indicate if the title name is to be replaced
	 */
	public boolean isTitleNameReplaceCondition() {
		return titleNameReplaceCondition;
	}

	/**
	 * Set flag to indicate if the title name is to be replaced.
	 * @param titleNameReplaceCondition Boolean flag to indicate if the title name is to be replaced 
	 */
	public void setTitleNameReplaceCondition(boolean titleNameReplaceCondition) {
		this.titleNameReplaceCondition = titleNameReplaceCondition;
	}

	/**
	 * Retrieve a flag to indicate if the title name is to be replaced using a regex.
	 * @return titleNameReplaceRegexPattern Boolean flag to indicate if the title name is to be replaced using a regex
	 */
	public String getTitleNameReplaceRegexPattern() {
		return titleNameReplaceRegexPattern;
	}

	/**
	 * Set a flag to indicate if the title name is to be replaced using a regex.
	 * @param titleNameReplaceRegexPattern Flag to indicate if the title name is to be replaced using a regex.
	 */
	public void setTitleNameReplaceRegexPattern(
			String titleNameReplaceRegexPattern) {
		this.titleNameReplaceRegexPattern = titleNameReplaceRegexPattern;
	}

	/**
	 * Get a flag to indicate if the title name will be used as a matching condition.
	 * @return titleNameMatchCondition Boolean flag to indicate if the title name will be used as a matching condition
	 */
	public boolean isTitleNameMatchCondition() {
		return titleNameMatchCondition;
	}

	/**
	 * Set a flag to indicate if the title name will be used as a matching condition.
	 * @param titleNameMatchCondition Boolean flag to indicate if the title name will be used as a matching condition
	 */
	public void setTitleNameMatchCondition(boolean titleNameMatchCondition) {
		this.titleNameMatchCondition = titleNameMatchCondition;
	}

	/**
	 * Get the regex pattern used for a title name replacement
	 * @return titleNameReplaceWithRegexPattern Regex pattern used for a title name replacement
	 */
	public String getTitleNameReplaceWithRegexPattern() {
		return titleNameReplaceWithRegexPattern;
	}

	/**
	 * Set the regex pattern used for a title name replacement
	 * @param titleNameReplaceWithRegexPattern Regex pattern used for a title name replacement
	 */
	public void setTitleNameReplaceWithRegexPattern(
			String titleNameReplaceWithRegexPattern) {
		this.titleNameReplaceWithRegexPattern = titleNameReplaceWithRegexPattern;
	}	
	
	/**
	 * Set the first partition reference
	 * @param partition First partition
	 */
	public void setFirstPartition(Partition partition)
	{
		firstPartition = partition;
	}

	/**
	 * Get the first partition reference
	 * @return firstPartition First Partition
	 */
	public Partition getFirstPartition()
	{
		return firstPartition;
	}

	/**
	 * Set the first partition id
	 * @param id First partition id
	 */
	public void setFirstPartitionId(int id)
	{
		this.firstPartitionId = id;
	}

	/**
	 * Get the first partition id
	 * @return firstPartitionId First Partition Id
	 */
	public int getFirstPartitionId()
	{
		return this.firstPartitionId;
	}
	
	/**
	 * Enable internal debugging.
	 * @param debug Flat to enable or disable internal debugging
	 */
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	/**
	 * Retrieve internal debugging flag.
	 * @return debug
	 */
	public boolean isDebug()
	{
		return this.debug;
	}
	
	/**
	 * Setter methods to set the ancestral folder id.
	 * @param lastFolderId Last Folder Id
	 */
	public void setLastFolderId(int lastFolderId)
	{
		this.lastFolderId = lastFolderId;
	}
	
	/**
	 * Getter methods retrieve the ancestral folder id.
	 * @return Last Folder Id
	 */
	public int getLastFolderId()
	{
		return this.lastFolderId;
	}
}
