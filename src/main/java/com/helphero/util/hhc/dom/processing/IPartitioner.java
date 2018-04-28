package com.helphero.util.hhc.dom.processing;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.rule.ITaskExprType;
import com.helphero.util.hhc.rule.ITaskMatchType;
import com.helphero.util.hhc.rule.RuleTargetType;

/**
 * @author jcharles
 * 
 * This interface defines all the methods to partition a document.
 */
public interface IPartitioner {
	
	/**
	 * This method sets the XPath to the elements that mark the partitions within the document.
	 * 
	 * @param xPath - XPath String to the Partition Matching Elements
	 */
	public void setMatchingElementXPath(String xPath);
	
	/**
	 * This method retrieves the XPath to the elements that mark the partitions within the document.
	 * 
	 * @return String - The XPath to the matching element(s)
	 */
	public String getMatchingElementXPath();
	
	/**
	 * This method sets the name of the partition element attribute used to determine which elements are used as partitions.
	 *  
	 * @param name Matching attribute name
	 */
	public void setMatchingAttributeName(String name);
	
	/**
	 * This method returns the name of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @return String Name of matching attribute
	 */
	public String getMatchingAttributeName();
	
	/**
	 * This method sets the value of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @param value Matching attribute value
	 */
	public void setMatchingAttributeValue(String value);
	
	/**
	 * This method gets the value of the partition element attribute used to determine which elements are used as partitions.
	 * 
	 * @return String Matching attribute value
	 */
	public String getMatchingAttributeValue();
	
	/**
	 * This method sets the type of expression match that is used on the element attribute used to determine which elements are used as partitions.
	 * 
	 * @param value Task expression type implementation instance
	 */
	public void setAttributeMatchExpression(ITaskExprType value);
	
	/**
	 * This method gets the type of expression match that is used on the element attribute used to determine which elements are used as partitions.
	 * 
	 * @return ITaskExprType Task expression type implementation instance
	 */
	public ITaskExprType getAttributeMatchExpression();
	
	/**
	 * @return ITaskMatchType This method retrieves the type of attribute matching mechanism i.e. an epxr { contains, startsWith } or a regex.
	 */
	public ITaskMatchType getAttributeMatchType();
	
	/**
	 * @param type This method sets the type of attribute matching mechanism i.e. an epxr { contains, startsWith } or a regex.
	 */
	public void setAttributeMatchType(ITaskMatchType type);
	
	/**
	 * @param pattern This method sets the regular expression pattern to be used for matching on an attribute.
	 */
	public void setAttributeMatchRegexPattern(String pattern);
	
	/**
	 * @return String This method returns the regular expression pattern to be used for matching on an attribute.
	 */
	public String getAttributeMatchRegexPattern();
	
	/**
	 * This method provides the xPath to the node or nodes the content of which will be used for partitioning.
	 * Folder partitioning is applied to siblings of this node.
	 * For Document/Section/Task/etc. partitioning, this method provides the xPath to a node or nodes whose children will be
	 * used for partitioning.
	 *    
	 * @param sPath Parent container XPath
	 */
	public void setParentContainerXPath(String sPath);
	
	/**
	 * This method returns the parent container XPath string.
	 * 
	 * @return String Parent container XPath string.
	 */
	public String getParentContainerXPath();
	
	/**
	 * This method sets the XPath to the starting element used to partition a document for the highest level of partitioning. 
	 * If folder partitioning is used, the Parent Container XPath should be the same as this. If folder level partitioning is not used,
	 * the devolution condition of REALIGN_TO_ROOT_CONTAINER should enforce the next highest level of partitioning to realign its starting point
	 * (Parent Container XPath) to be the same as the Document Root XPath.  This is handled by the Transformation Manager.  
	 * @param sPath Document root XPath
	 */
	public void setDocumentRootXPath(String sPath);
	
	/**
	 * Returns the starting element XPath for the highest level of partitioning.  
	 * @return String Document root XPath
	 */
	public String getDocumentRootXPath();
	
	/**
	 * This method returns a node or nodes whose siblings for folder partitioning or children for document/section/task partitioning 
	 * will be used to further partition a document.
	 * @return NodeList List of nodes whose siblings for folder partitioning or children for document/section/task partitioning will be used to further partition a document.
	 * @throws XPathExpressionException Thrown if an invalid XPath is used 
	 */
	public NodeList getParentContainerNodes() throws XPathExpressionException;
	
	/**
	 * This method sets the type of parent container. The parent container determines 
	 * whether sibling (for folders) or child nodes (for document/section/tasks) are used to further partition a document.
	 * @param type Parent Container Type
	 */
	public void setParentContainerType(IPartitionType type);
	
	/**
	 * This method retrieves the type of parent container.
	 * 
	 * @return IPartitionType Parent Container Type implementation instance
	 */
	public IPartitionType getParentContainerType();
	
	/**
	 * This method sets the title for the introductory partition. This partition captures all elements that are before 
	 * the first matching partition element in the document.
	 *  
	 * @param title Introductory partition title
	 */
	public void setIntroPartitionTitle(String title);
	
	/**
	 * @return String The introductory partition title.
	 */
	public String getIntroPartitionTitle();
	
	/**
	 * This method sets a flag to indicate the first non-matching elements should or should not be included in an introductory partition.
	 * @param bool Flag to indicate first elements will be preserved
	 */
	public void setKeepFirstElements(boolean bool);
	
	/**
	 * This method returns a flag to indicate the first non-matching elements should or should not be included in an introductory partition.
	 * @return boolean Flag to indicate first elements will be preserved
	 */
	public boolean getKeepFirstElements();
	
	/**
	 * This method returns the type of partition i.e. Folder, Document, Section, Task, ...
	 * 
	 * @return PartitionType Partition type implementation instance
	 */
	public PartitionType getPartitionType();	
	
	/**
	 * This method sets the type of partition.
	 * 
	 * @param partitionType Partition type implementation instance
	 */
	public void setPartitionType(PartitionType partitionType);
	
	/**
	 * @return String Returns the Partition Type as a Camel Notation String.
	 */
	public String getPartitionTypeAsString();

	/**
	 * This method returns the target type of partition i.e. Procedure, Policy, Process, External
	 * 
	 * @return RuleTargetType Rule target type
	 */
	public RuleTargetType getTargetType();	
	
	/**
	 * This method sets the target type of partition. Used only for documents at this stage.
	 * 
	 * @param targetType Rule target type
	 */
	public void setTargetType(RuleTargetType targetType);
	
	/**
	 * @return String Returns the Target Type as a Camel Notation String.
	 */
	public String getTargetTypeAsString();
	
	/**
	 * Does the document contain partitions?
	 * @return boolean
	 * @throws XPathExpressionException 
	 */
	// public boolean hasPartitions() throws XPathExpressionException;
	
	/**
	 *  Do the partitioning of the document.
	 * @throws XPathExpressionException Thrown if an invalid XPath is used during the partition process 
	 */
	public void partition() throws XPathExpressionException;
	
	/**
	 * Set the document (DOM) to partition.
	 * @param doc Document (DOM) to partition
	 */
	public void setDocument(Document doc);
	
	/**
	 * Get the document (DOM) to partition.
	 *  
	 * @return Document Document (DOM) to partition
	 */
	public Document getDocument();	
	
	/**
	 * Set the first partition for a partition type
	 * @param partition First partition
	 */
	public void setFirstPartition(Partition partition);
	
	/**
	 * Retrieve the first partition details
	 * 
	 * @return Partition First partition
	 */
	public Partition getFirstPartition();
	
	/**
	 * Set the first partition id (either the first folder id or document id) for a partition
	 * @param id First Partition id
	 */
	public void setFirstPartitionId(int id);
	
	/**
	 * Retrieve the first partition id (either document or folder id)
	 * 
	 * @return int First Partition id
	 */
	public int getFirstPartitionId();
	
	/**
	 * Set the last folder partition id. Folders are partitioned first documents next. Document ids start at last folder id + 1. 
	 * @param id Last Folder id
	 */
	public void setLastFolderId(int id);
	
	/**
	 * Retrieve the last folder partition id.
	 * 
	 * @return int Last Folder id
	 */
	public int getLastFolderId();
}
