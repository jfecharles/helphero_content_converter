package com.helphero.util.hhc.dom.processing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.rule.TaskExprType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This derived class performs task specific attuned partitioning of an xHtml document. 
 * @author jcharles
 */
public class TaskPartitioner extends Partitioner {
	static Logger logger = Logger.getLogger(TaskPartitioner.class);

	public TaskPartitioner() {
		// TODO Auto-generated constructor stub
	}

	@Override
	/**
	 * Initialise all the default settings for a task level partition.
	 */	
	public void initialise() {
		String docRootXPath = "//body/div[starts-with(@class,'document')]";
		setDocumentRootXPath(docRootXPath);
		
		setParentContainerType(PartitionType.SECTION);
		String pcXPath = "//body/div/div/div/div[starts-with(@class,'Section')]";
		setParentContainerXPath(pcXPath);
		
		setMatchingAttributeName("class");
		setMatchingAttributeValue("Heading3");
		setAttributeMatchExpression(TaskExprType.STARTS_WITH);
		
		this.setKeepFirstElements(true);
		this.setIntroPartitionTitle("Preliminaries");

		setMatchingElementXPath("//body/div/div/div/div/p[starts-with(@class,'Heading3')]");
		
		setPartitionType(PartitionType.TASK);
			
		this.deriveMatchingElementXPath();		
	}
	
	@Override
	public void interpret()
	{
		// Common properties specific to this transform
		this.setPartitionType(PartitionType.TASK);
		
		// All other properties are common to all partition types and are derived from the rules 
		super.interpret();
	}
	
	/**
	 *  Do the task level partitioning of the document. 
	 *  
	 */	
	@Override
	public void partition()
	{
		logger.info(">>> Task Partitioner");
		
		XPath xPath = getXPathHandler();
		NodeList parentNodes;
		try {
			parentNodes = (NodeList)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\t=== " + this.getPartitionType().name() + " Partitioner: partition()");
			
			for (int i = 0; i < parentNodes.getLength(); i++) 
			{        	
				Node parentNode = (Node)parentNodes.item(i);
				
				if (parentNode instanceof Element)
				{
					String sParentClass = parentNode.getAttributes().getNamedItem("class") != null ? parentNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
					logger.info("\tParent Node Name=" + parentNode.getNodeName()+":class="+ sParentClass);
					
					NodeList nodeList = parentNode.getChildNodes();
					Node xDivNode = null;
					
					/*
					 *  First check to see if the immediate siblings contain any matching task partitions. If not don't bother to perform task level partitioning.
					 *  If the partition contains at least one task partition then proceed and quarantine any initial elements not preceded by a task partition into an introductory partition.
					 */
					boolean hasTaskPartitions = false;
					for (int j = 0; j < nodeList.getLength(); j++)
					{
					 	Node xNode = nodeList.item(j);

					 	if (xNode instanceof Element)
			         	{
			         		String sClass = xNode.getAttributes().getNamedItem("class") != null ? xNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
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
			         		
			         		if (match)
			         		{
			         			hasTaskPartitions = true;
			         			break;
			         		}
			         	}					
					}
					
					if (hasTaskPartitions) // Go ahead and task partition the document
					{
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
					} // end if hasTaskPartitions
				}
			} // end for i			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
