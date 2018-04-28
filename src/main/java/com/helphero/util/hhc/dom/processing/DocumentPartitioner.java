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

import com.helphero.util.hhc.rule.RuleTargetType;
import com.helphero.util.hhc.rule.TaskExprType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This derived class performs document specific attuned partitioning of an xHtml document. 
 * @author jcharles
 */
public class DocumentPartitioner extends Partitioner {
	static Logger logger = Logger.getLogger(DocumentPartitioner.class);

	public DocumentPartitioner() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Initialise all the default settings for a document level partition.
	 */
	public void initialise()
	{
		String docRootXPath = "//body/div[starts-with(@class,'document')]";
		setDocumentRootXPath(docRootXPath);
		
		setParentContainerType(PartitionType.FOLDER);
		String pcXPath = "//body/div/div[starts-with(@class,'Folder')]";
		setParentContainerXPath(pcXPath);
		
		setMatchingAttributeName("class");
		setMatchingAttributeValue("Heading1");
		setAttributeMatchExpression(TaskExprType.STARTS_WITH);
		
		this.setKeepFirstElements(true);
		this.setIntroPartitionTitle("Introduction");

		setMatchingElementXPath("//body/div/div/p[starts-with(@class,'Heading1')]");
		
		setPartitionType(PartitionType.DOCUMENT);
		
		this.deriveMatchingElementXPath();
	}
	
	@Override
	public void interpret()
	{
		// Common properties specific to this transform
		this.setPartitionType(PartitionType.DOCUMENT);
		
		// All other properties are common to all partition types and are derived from the rules 
		super.interpret();
	}
	
	@Override
	public void process() throws TransformException
	{
		if (this.hasPartitions())
			partition();
		else
			throw new TransformException("ErrMsg: No Document level partitions found");
	}
	
	@Override
	/**
	 *  Do the partitioning of the document. The document partitioning varies slightly from the default partitioner 
	 *  because it requires document ids to start from last folder id + 1. 
	 */	
	public void partition() 
	{
		logger.info(">>> Document Partitioner");
		
		XPath xPath = getXPathHandler();
		NodeList parentNodes;
		try {
			parentNodes = (NodeList)xPath.compile(this.getParentContainerXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\t=== " + this.getPartitionType().name() + " Partitioner: partition() : Parent Container XPath="+ this.getParentContainerXPath()+" : Node Count="+parentNodes.getLength());
			
			int pCount = this.getLastFolderId();
			
			for (int i = 0; i < parentNodes.getLength(); i++) 
			{        	
				Node parentNode = (Node)parentNodes.item(i);	

				if (parentNode instanceof Element)
				{
					String sParentClass = parentNode.getAttributes().getNamedItem("class") != null ? parentNode.getAttributes().getNamedItem(this.getMatchingAttributeName()).getNodeValue() : "";
					logger.info("\tNode Name=" + parentNode.getNodeName()+":class="+ sParentClass);
					
					NodeList nodeList = parentNode.getChildNodes();
					Node xDivNode = null;
					
					Node xIntroDivNode = this.document.createElement("div");
					((Element)xIntroDivNode).setAttribute("class", this.getPartitionTypeAsString());
					((Element)xIntroDivNode).setAttribute("title", this.getIntroPartitionTitle());
					
					boolean first = true;
					boolean keepFirst = this.getKeepFirstElements();
					boolean hasIntroNode = false;
					
					// for (int j = 0, pCount = this.getLastFolderId(); j < nodeList.getLength(); j++) 
					for (int j = 0; j < nodeList.getLength(); j++)
					{        	
			         	Node xNode = nodeList.item(j);
			         	if (xNode instanceof Element)
			         	{
			         		if (first && keepFirst)
			         		{
			         			if (!hasIntroNode) {
				         			((Element)xIntroDivNode).setAttribute("id", Integer.toString(++pCount));
			         				logger.info("\t\tInsert Node Name=" + xIntroDivNode.getNodeName()+":"+xIntroDivNode.getAttributes().getNamedItem("class") + " before Node Name="+ xNode.getNodeName()+":class="+xNode.getAttributes().getNamedItem("class"));				         			
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
				         			if (first)
				         			{
				         				Partition partition = new Partition();
				         				partition.setType(this.getPartitionType());
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
				         			((Element)xDivNode).setAttribute("class", getPartitionTypeAsString());
				         			((Element)xDivNode).setAttribute("title", title);
				         			if (this.getTargetType() != RuleTargetType.NOT_SET && this.getTargetType() != RuleTargetType.PROCEDURE)
				         			{
				         				((Element)xDivNode).setAttribute("target_type", this.getTargetTypeAsString());				         				
				         			}
				         			// The Document partition ids start where Folder ids finish
				         			((Element)xDivNode).setAttribute("id", Integer.toString((++pCount)));

			         				logger.info("\t\t\tInsert Node Name=" + xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class") + ":LastFolderId=" + getLastFolderId() + ":Id= "+ xDivNode.getAttributes().getNamedItem("id") +" before Node Name="+ xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class"));
				         			DomUtils.insertBefore(xDivNode, xNode);
				         			DomUtils.removeElement((Element) xNode, false);	 
				         		}
				         		else
				         		{
				         			// This is a post document heading node so append it to document div node
				         			if (!first && xDivNode != null)
				         			{
				         				logger.info("\t\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class"));
				         				xDivNode.appendChild(xNode);
				         				j--;
				         			}
				         			// Remove unwanted nodes before the first document heading
				         			else if (first && !keepFirst && xDivNode == null)
				         			{
				         				logger.info("\t\t\tRemove Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class"));
				         				// Tag the node for removal by the xslt script
				         				((Element) xNode).setAttribute("class", "REMOVE");
				         			}
				         			else if (first && keepFirst && xIntroDivNode != null)
				         			{
				         				logger.info("\t\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xIntroDivNode.getNodeName()+":"+xIntroDivNode.getAttributes().getNamedItem("class"));
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
			         				logger.info("\t\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xDivNode.getNodeName()+":"+xDivNode.getAttributes().getNamedItem("class"));
			         				xDivNode.appendChild(xNode);
			         				j--;
			         			}
			         			else if (first && keepFirst && xIntroDivNode != null)
			         			{
			         				logger.info("\t\t\tAppend Node Name=" + xNode.getNodeName()+":"+xNode.getAttributes().getNamedItem("class") + " to Node Name="+ xIntroDivNode.getNodeName()+":"+xIntroDivNode.getAttributes().getNamedItem("class"));
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
}
