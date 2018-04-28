package com.helphero.util.hhc.dom.processing.transforms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.Transformer;
import com.helphero.util.hhc.rule.ITask;
import com.helphero.util.hhc.rule.RuleSubType;
import com.helphero.util.hhc.rule.RuleTarget;
import com.helphero.util.hhc.rule.RuleTargetType;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.Task;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This transform copies nodes from a source location to one or more target locations. 
 * The copied nodes can be placed before or after the target node as siblings or appended as a child to the target node. 
 * The transform accommodates 1:1, 1:N, N:1 and N:M source to target node(s) copies.
 * @author jcharles
 */
public class CopyTransformer extends Transformer {
	static Logger logger = Logger.getLogger(CopyTransformer.class);
	private RuleType ruleType = RuleType.COPY;			// General type of rule being performed
	private String srcXPath;							// XPAth to the source node or nodelist
	private TaskType operation = TaskType.APPEND_CHILD;	// Operation to be performed on the target node
	private String targetXPath;     					// XPath to the target node

	public CopyTransformer() {
	}
	
	/**
	 * Initialise all the parameters from the rule to perform the transform 
	 */
	public void interpret() {
		// Find the XPaths to both the source node(s) and the target node 
		// and the type of operation to be performed.	
		for (ITask task : this.getRule().getTasks())
		{	
			switch ((TaskType)((Task)task).getType())
			{
			case SRC_PATH:
				// Set the source node(s) XPath
				this.setSrcXPath(((Task)task).getSource());
				break;
			case INSERT_BEFORE:
			case INSERT_AFTER:
			case APPEND_CHILD:
				// Determine the operation to be performed on the parent
				this.setOperation((TaskType)((Task)task).getType());
				break;
			case TARGET_PATH:
				// Set the target node(s) XPath
				this.setTargetXPath(((Task)task).getSource());
				break;
			default:
				break;
			}
		}	
	}
	
	/**
	 * Execute the copy transform
	 */
	public void process() throws TransformException {		
		XPath xPath = getXPathHandler();
		
		NodeList srcNodes;
		NodeList targetNodes;
		Node targetNode;
		Node parentNode;
		
		logger.info("\tProcessing CopyTransform ===");

		try {
			srcNodes = (NodeList)xPath.compile(this.getSrcXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\tSrc xPath="+this.getSrcXPath());
			logger.info("\tSrc Nodes Count="+srcNodes.getLength());
			
			boolean resetIds = this.hasResetableIds(srcNodes);
			
			targetNodes = (NodeList)xPath.compile(this.getTargetXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			// Need to address all copy cardinalities - 1:1, 1:N, N:1, N:M
			if (targetNodes.getLength() >= 1 && srcNodes.getLength() >= 1) {
				
				for (int i = 0; i < targetNodes.getLength(); i++)
				{
					targetNode = targetNodes.item(i);

					logger.info("\tTarget Node: class="+((Element)targetNode).getAttribute("class"));
				
					boolean doOperation = false;
				
					try {
						doOperation = this.isValidOperation(((Element)srcNodes.item(0)).getAttribute("class"), ((Element)targetNode).getAttribute("class"), this.getOperation());
					
						if (doOperation)
						{				
							parentNode = targetNode.getParentNode();
							
							for (int j = 0; j < srcNodes.getLength(); j++) 
							{  
								Node srcNodeCopy = srcNodes.item(j).cloneNode(true);
					
								if (this.getOperation() == TaskType.APPEND_CHILD) {
									targetNode.appendChild(srcNodeCopy);
								} else if (this.getOperation() == TaskType.INSERT_BEFORE) {
				         			DomUtils.insertBefore(srcNodeCopy, targetNode);	 						
								} else if (this.getOperation() == TaskType.INSERT_AFTER) {
									Node nextSibling = targetNode.getNextSibling();
									if (nextSibling != null) {
										// Insert the node before the next sibling
					         			DomUtils.insertBefore(srcNodeCopy, nextSibling);	 
									} else {
										// If there is no sibling append to the end of the parent nodes children
										logger.info("\tParent Node: class="+((Element)parentNode).getAttribute("class"));
										if (parentNode != null) parentNode.appendChild(srcNodeCopy);
									}
									targetNode = srcNodeCopy; // The target becomes the new last entry
								}
							}
							
							if (resetIds)
							{
								logger.info("\tCopyTransform Reseting Folder + Document Ids");
								try {
									// Retrieve the folders
									NodeList folderNodes = (NodeList)xPath.compile("//div/div[class='Folder']").evaluate(this.document, XPathConstants.NODESET);
									
									// Folder and document IDS MUST be unique in SupportPoint
									
									// Reset all the folder node ids
									int id = 1;
									for (int k = 0; k < folderNodes.getLength(); k++) 
									{
										Node node = folderNodes.item(k);							
										((Element)node).setAttribute("id", Integer.toString(id++));														
									}
									
									// Retrieve the documents
									NodeList docNodes = (NodeList)xPath.compile("//div/div[class='Document']").evaluate(this.document, XPathConstants.NODESET);
									// Reset all the document node ids
									for (int k = 0; k < docNodes.getLength(); k++) 
									{
										Node node = docNodes.item(k);							
										((Element)node).setAttribute("id", Integer.toString(id++));														
									}					
								} catch (XPathExpressionException e) {
									e.printStackTrace();
								}
							}
						}
					} catch (InvalidOperationException ex) {
						throw new TransformException("Invalid Copy Operation: "+ex.getMessage());
					}
				}
			}
		} catch (Exception ex) {
			throw new TransformException("Invalid Copy Operation: "+ex.getMessage());
		}	
	}
	
	/**
	 * This method determines if a source selection of nodes, to be copied to a target node, contains Folder or Document level nodes.
	 * If Folder or Document levels nodes are copied their Ids need to be re-computed.
	 * @param nodes Node list
	 * @return Boolean flag to indicate the nodes require their ids to be reset
	 */
	protected boolean hasResetableIds(NodeList nodes) {
		boolean reset = false;
		
		// Only need to check sibling nodes. If the sibling nodes are either a folder or document then the document needs its Ids recalculated. 
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			
			String sClass = ((Element)node).getAttribute("class");
			if (sClass.equalsIgnoreCase("Folder") || sClass.equalsIgnoreCase("Document"))
			{
				reset = true;
				break;
			}
		}
		
		return reset;
	}
	
	/**
	 * This method defines all the rules driving what is allowed when copying content from source node(s) to a target node.
	 * @param srcClass - Source class
	 * @param targetClass - Target class
	 * @param operation The type of operation (TaskType) to be performed: Insert before/after or Append as a child
	 * @return boolean - Flag indicating if this operation is allowed.
	 * @throws InvalidOperationException - Thrown if the operation is not allowed.
	 */
	@Override
	public boolean isValidOperation(String srcClass, String targetClass, TaskType operation) throws InvalidOperationException
	{
		boolean valid = false;
		
		if (srcClass != null && targetClass != null) {
			if (srcClass.equalsIgnoreCase("Folder"))
			{
				if (targetClass.equalsIgnoreCase("Folder"))
				{
					if (!(operation == TaskType.INSERT_BEFORE || operation == TaskType.INSERT_AFTER))
						throw new InvalidOperationException("Source nodes of Type \"Folder\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Folder\"");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Document") || targetClass.equalsIgnoreCase("Section") || targetClass.equalsIgnoreCase("Task"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
				else
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
			}
			else if (srcClass.equalsIgnoreCase("Document"))
			{
				if (targetClass.equalsIgnoreCase("Folder"))
				{
					if (!(operation == TaskType.APPEND_CHILD))
						throw new InvalidOperationException("Source nodes of Type \"Document\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Folder\". Only append operations are valid.");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Document"))
				{
					if (!(operation == TaskType.INSERT_BEFORE || operation == TaskType.INSERT_AFTER))
						throw new InvalidOperationException("Source nodes of Type \"Document\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Document\". Only insert operations are valid");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Section") || targetClass.equalsIgnoreCase("Task"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");						
				else
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");							
			}
			else if (srcClass.equalsIgnoreCase("Section"))
			{
				if (targetClass.equalsIgnoreCase("Folder"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
				else if (targetClass.equalsIgnoreCase("Document"))
				{
					if (!(operation == TaskType.APPEND_CHILD))
						throw new InvalidOperationException("Source nodes of Type \"Section\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Document\". Only append operations are valid.");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Section"))
				{
					if (!(operation == TaskType.INSERT_BEFORE || operation == TaskType.INSERT_AFTER))
						throw new InvalidOperationException("Source nodes of Type \"Section\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Section\". Only insert operations are valid.");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Task"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");							
				else
					valid = true;
					// throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");								
			}
			else if (srcClass.equalsIgnoreCase("Task"))
			{
				if (targetClass.equalsIgnoreCase("Folder"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
				else if (targetClass.equalsIgnoreCase("Document"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");					
				else if (targetClass.equalsIgnoreCase("Section"))
				{
					if (!(operation == TaskType.APPEND_CHILD))
						throw new InvalidOperationException("Source nodes of Type \"Task\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Section\". Only append operations are valid.");
					else
						valid = true;
				}
				else if (targetClass.equalsIgnoreCase("Task"))
				{
					if (!(operation == TaskType.INSERT_BEFORE || operation == TaskType.INSERT_AFTER))
						throw new InvalidOperationException("Source nodes of Type \"Task\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Task\". Only insert operations are valid.");
					else
						valid = true;
				}
				else
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");									
			}
			else
			{
				if (targetClass.equalsIgnoreCase("Folder") || targetClass.equalsIgnoreCase("Document"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
				else if (targetClass.equalsIgnoreCase("Task") || targetClass.equalsIgnoreCase("Section"))
				{
					if (!(operation == TaskType.APPEND_CHILD))
						throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \""+targetClass+"\". Only append operations are valid.");
					else
						valid = true;
				}
				else
				{
					valid = true;	
				}		
			}			
		} 
		else 
		{
			throw new InvalidOperationException("Cannot identity source node or target node class types.");
		}
		
		return valid;
	}
	
	/**
	 * Get the source node(s) xpath.
	 * @return String
	 */
	public String getSrcXPath() {
		return srcXPath;
	}

	/**
	 * Set the xpath to the source node(s).
	 * @param srcXPath XPATH to the source node(s)
	 */
	public void setSrcXPath(String srcXPath) {
		this.srcXPath = srcXPath;
	}

	/**
	 * Get the operation.
	 * @return operation The type of operation - See TaskType
	 */
	public TaskType getOperation() {
		return operation;
	}

	/**
	 * Set the operation.
	 * @param operation The type of operation. See TaskType
	 */
	public void setOperation(TaskType operation) {
		this.operation = operation;
	}

	/**
	 * Get the target node(s) xpath.
	 * @return XPATH to target node(s)
	 */
	public String getTargetXPath() {
		return targetXPath;
	}

	/**
	 * Set the xpath to the target node(s) .
	 * @param targetXPath The XPATH to target node(s)
	 */
	public void setTargetXPath(String targetXPath) {
		this.targetXPath = targetXPath;
	}
}
