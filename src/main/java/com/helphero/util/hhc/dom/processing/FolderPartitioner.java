package com.helphero.util.hhc.dom.processing;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.processing.FileConverter;
import com.helphero.util.hhc.rule.TaskExprType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This derived class performs folder specific attuned partitioning of an xHtml document. 
 * @author jcharles
 */
public class FolderPartitioner extends Partitioner {
	static Logger logger = Logger.getLogger(FolderPartitioner.class);

	public FolderPartitioner() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Initialise all the default settings for a folder level partition.
	 */
	public void initialise()
	{
		String pcXPath = "//body/div[starts-with(@class,'document')]";
		setDocumentRootXPath(pcXPath);
		setParentContainerType(PartitionType.ROOT);
		setParentContainerXPath(pcXPath);
		
		setKeepFirstElements(false);
		
		setMatchingAttributeName("class");
		setMatchingAttributeValue("HeadingProcessName ");
		setAttributeMatchExpression(TaskExprType.STARTS_WITH);
		setPartitionType(PartitionType.FOLDER);

		setMatchingElementXPath("//body/div/p[starts-with(@class,'HeadingProcessName ')]");
		
		this.deriveMatchingElementXPath();
	}
	
	@Override
	public void interpret()
	{
		// Common properties specific to this transform
		this.setPartitionType(PartitionType.FOLDER);
		
		// All other properties are common to all partition types and are derived from the rules 
		super.interpret();
	}
	
	/* 
	 * Folder level partitioner. The starting point for the folder level partitioner is a single top level element. The XPath to this element is the Parent Container XPath.
	 * For xHtml documents converted from docx using docx4j this is the top level div inside the body of the document with a class setting of 'document'.
	 * For nested document, section or task level partitioners there may be several top level elements matching the partitioning XPath. The elements between these elements 
	 * are iterated through and moved to be children of the newly created div container.   
	 * 
	 * @see dom.processing.Partitioner#partition()
	 */
	@Override
	public void partition()
	{
		// String expr = "//body/div[starts-with(@class,'document')]";
		XPath xPath = this.getXPathHandler();
		
		logger.info(">>> Running Folder Partitioner");
		
		Node xDocNode;
		try {
			xDocNode = (Node)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODE);
			
			logger.info("\t=== " + this.getPartitionType().name() + " Partitioner partition(): Parent Container XPath="+ this.getParentContainerXPath());
			
			NodeList nodeList = xDocNode.getChildNodes();
			Node xDivNode = null;
			
			boolean first = true;
			boolean keepFirst = this.getKeepFirstElements();	
			
			for (int i = 0, pCount = 0; i < nodeList.getLength(); i++) 
			{        	
	         	Node xNode = nodeList.item(i);
	         	if (xNode instanceof Element)
	         	{
	         		String sAttrName = this.getMatchingAttributeName();
	         		String sClass = xNode.getAttributes().getNamedItem(sAttrName) != null ? xNode.getAttributes().getNamedItem(sAttrName).getNodeValue() : "";
		         	boolean isTable = xNode.getNodeName().equalsIgnoreCase("table") ? true : false;
		         	String sId = isTable && xNode.getAttributes().getNamedItem("id") != null ? xNode.getAttributes().getNamedItem("id").getNodeValue() : "";
		         	
		         	if (sClass != null)
		         	{
		         		logger.info("\t\tNode Name=" + xNode.getNodeName()+":class="+ sClass);
		         		
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
			         		logger.info("\t\t*** Matching Node Name=" + xNode.getNodeName()+":class="+ sClass);
		         			if (first)
		         			{
		         				Partition partition = new Partition();
		         				partition.setType(PartitionType.FOLDER);
		         				partition.setsId(sId);
		         				this.setFirstPartition(partition);
		         				this.setFirstPartitionId(pCount+1);
		         			}
		         			
		         			first = false;
		         			
		         			if (this.isTitleNameReplaceCondition())
		         			{
		         				Pattern r = Pattern.compile(this.getTitleNameReplaceRegexPattern());			         			
			         			Matcher m = r.matcher(title);	
			         			title = m.replaceAll(this.getTitleNameReplaceWithRegexPattern());
		         			}
		         			
		         			xDivNode = this.document.createElement("div");
		         			((Element)xDivNode).setAttribute("class", this.getPartitionTypeAsString());
		         			((Element)xDivNode).setAttribute("title", title);
		         			((Element)xDivNode).setAttribute("id", Integer.toString(++pCount));
		         			
		        			// Used by the document partitioner. First document partition id = last folder partition id + 1		         			
		         			this.setLastFolderId(pCount);

	         				logger.info("\t\tInsert Node Name=" + xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class") + " before Node Name="+ xNode.getNodeName()+":class="+xNode.getAttributes().getNamedItem("class"));
		         			DomUtils.insertBefore(xDivNode, xNode);
	         				logger.info("\t\tRemove Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class"));		         			
		         			DomUtils.removeElement((Element) xNode, false);
		         		}
		         		else
		         		{
		         			// Skip over all nodes leading up to the first Title node
		         			if (!first && xDivNode != null)
		         			{
		         				logger.info("\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class"));
		         				xDivNode.appendChild(xNode);
		         				i--;
		         			}       			
		         			else if (first && !keepFirst)
		         			{
		         				// Tag the node for removal by the xslt script
		         				logger.info("\t\tRemove Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class"));
		         				DomUtils.removeElement((Element) xNode, false);
		         				i--;
		         			}
		         		}
		         	} else if (isTable && sId != null)
		         	{
		         		logger.info("\tNode Name=" + xNode.getNodeName()+":id="+ sId);
	         			// Skip over all nodes leading up to the first Heading1 node
	         			if (!first && xDivNode != null)
	         			{
	         				logger.info("\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class"));	         				
	         				xDivNode.appendChild(xNode);
	         				i--;
	         			}
	         			else if (first && !keepFirst)
	         			{
	         				logger.info("\t\tRemove Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class"));
	         				DomUtils.removeElement((Element) xNode, false);
	         				i--;
	         			}
		         	}	         	
	         	}
	        } // end for			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * Do the folder level partitioning. 
	 */
	@Override
	public void process()
	{
		if (hasPartitions())
		{
			partition();
		}
		else
		{
			this.insertDummyFolder();
		}
	}
	
	/**
	 * This method is used as part of the partitioning process to ensure there is always a folder level partition.
	 * SupportPoint will not import a document with multiple document and not parent folder. This method protects against such a condition.
	 */
	private void insertDummyFolder()
	{
		XPath xPath = this.getXPathHandler();
		
		Node xDocNode;
		try {
			xDocNode = (Node)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODE);
			
			logger.info("=== " + this.getPartitionType().name() + " Partitioner partition(): Parent Container XPath="+ this.getParentContainerXPath());
			
			NodeList nodeList = xDocNode.getChildNodes();
			Node xDivNode = this.document.createElement("div");
 			((Element)xDivNode).setAttribute("class", this.getPartitionTypeAsString());
 			String sUuid = UUID.randomUUID().toString();
 			((Element)xDivNode).setAttribute("title", "Folder "+sUuid.substring(0, sUuid.indexOf("-", 0)));
 			((Element)xDivNode).setAttribute("id", Integer.toString(1));
 			
			Partition partition = new Partition();
			partition.setType(PartitionType.FOLDER);
			partition.setsId(Integer.toString(1));
			this.setFirstPartition(partition);
			this.setFirstPartitionId(1);
 			this.setLastFolderId(1);

			boolean first = true;	
			
			for (int i = 0; i < nodeList.getLength(); i++) 
			{        	
	         	Node xNode = nodeList.item(i);
	         	if (xNode instanceof Element)
	         	{
		         	if (first)
		         	{	         			
         				logger.info("\t\tInsert Node Name=" + xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class") + " before Node Name="+ xNode.getNodeName()+":class="+xNode.getAttributes().getNamedItem("class"));
	         			DomUtils.insertBefore(xDivNode, xNode);
	         			
	         			first = false;
		         	}
		         	else
		         	{
         				logger.info("\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class"));	         				
         				xDivNode.appendChild(xNode);
         				i--;
		         	}
	         	}
	        } // end for			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
}
