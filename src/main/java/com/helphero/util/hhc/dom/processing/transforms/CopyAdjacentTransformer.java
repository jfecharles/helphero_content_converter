package com.helphero.util.hhc.dom.processing.transforms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This transform copies nodes from a list of source node locations to adjacent (or nearby) target node locations. 
 * The copied nodes can be placed before or after the target node as siblings or appended as a child to the target node. 
 * The transform accommodates 1:1 child node copies to N target adjacent node(s).
 * The number of source nodes must be identical to the number of target nodes.
 * @author jcharles
 *
 */
public class CopyAdjacentTransformer extends CopyTransformer {

	public CopyAdjacentTransformer() {
	}
	
	/**
	 * Execute this transform
	 */
	@Override
	public void process() throws TransformException {	
		XPath xPath = getXPathHandler();
		
		NodeList srcNodes;
		NodeList targetNodes;
		Node targetNode;
		Node srcNode;
		Node parentNode;
		
		logger.info("\tProcessing CopyAdjacentTransform ===");
		logger.info("\tSrc xPath="+this.getSrcXPath());
		logger.info("\tTarget xPath="+this.getTargetXPath());
		
		try {
			srcNodes = (NodeList)xPath.compile(this.getSrcXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\tSrc Nodes Count="+srcNodes.getLength());
			
			targetNodes = (NodeList)xPath.compile(this.getTargetXPath()).evaluate(this.document, XPathConstants.NODESET);

			logger.info("\tTarget Nodes Count="+targetNodes.getLength());

			// Need to address the 1:1 where the Number of Src Nodes = Number of Target Node
			if (targetNodes.getLength() == srcNodes.getLength()) {
				
				for (int i = 0; i < targetNodes.getLength(); i++)
				{
					targetNode = targetNodes.item(i);
					
					srcNode = srcNodes.item(i);
					
					logger.info("\tTarget Node: class="+((Element)targetNode).getAttribute("class")+" : id="+((Element)targetNode).getAttribute("id"));
				
					boolean doOperation = false;
				
					try {
						doOperation = this.isValidOperation(((Element)srcNodes.item(0)).getAttribute("class"), ((Element)targetNode).getAttribute("class"), this.getOperation());
					
						if (doOperation)
						{				
							parentNode = targetNode.getParentNode();
							
							// We are only interested in the children of the source node
							NodeList childNodes = srcNode.getChildNodes();
							
							logger.info("\tTarget Node: Child Node Count="+childNodes.getLength());
							 
							// Insert all the children of the source node before or after the target node
							for (int j = 0; j < childNodes.getLength(); j++)
							{  
								Node srcNodeCopy = childNodes.item(j).cloneNode(true);
					
								if (this.getOperation() == TaskType.APPEND_CHILD) {
									logger.info("Append Node: class="+((Element)srcNodeCopy).getAttribute("class")+ "to Target Node: "+((Element)targetNode).getAttribute("class"));
									targetNode.appendChild(srcNodeCopy);
								} else if (this.getOperation() == TaskType.INSERT_BEFORE) {
									logger.info("Insert Node: class="+((Element)srcNodeCopy).getAttribute("class")+ "before Target Node: "+((Element)targetNode).getAttribute("class"));
				         			DomUtils.insertBefore(srcNodeCopy, targetNode);	 						
								} else if (this.getOperation() == TaskType.INSERT_AFTER) {
									Node nextSibling = targetNode.getNextSibling();
									if (nextSibling != null) {
										// Insert the node before the next sibling
										logger.info("Insert Node: class="+((Element)srcNodeCopy).getAttribute("class")+ "before Target Node Next Sibling: "+((Element)nextSibling).getAttribute("class"));
					         			DomUtils.insertBefore(srcNodeCopy, nextSibling);	 
									} else {
										// If there is no sibling append to the end of the parent nodes children
										logger.info("\tAppend Src Node to "+((Element)srcNodeCopy).getAttribute("class")+" Parent Node: class="+((Element)parentNode).getAttribute("class"));
										if (parentNode != null) parentNode.appendChild(srcNodeCopy);
									}
									targetNode = srcNodeCopy; // The target becomes the new last entry
								}
							}
							
						}
					} catch (InvalidOperationException ex) {
						throw new TransformException("Invalid CopyAdjacent Operation: "+ex.getMessage());
					}
				}
			}
		} catch (Exception ex) {
			throw new TransformException("Invalid CopyAdjacent Operation: "+ex.getMessage());
		}	
	}
	
	/**
	 * This method defines all the rules driving what is allowed when performing an adjacent copy of child element content 
	 * from a source node to a nearby target node for each occurrence of the source nodes
	 * @param srcClass - Source class
	 * @param targetClass - Target class
	 * @param operation - The type of operation to be performed: Insert before/after or Append as a child
	 * @return boolean - Flag indicating if this operation is allowed.
	 * @throws InvalidOperationException - Thrown if the operation is not allowed.
	 */
	@Override
	public boolean isValidOperation(String srcClass, String targetClass, TaskType operation) throws InvalidOperationException
	{
		boolean valid = false;
		
		if (srcClass != null && targetClass != null) {
			if (srcClass.equalsIgnoreCase("Folder") || srcClass.equalsIgnoreCase("Document"))
			{
				throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
			}
			else if (srcClass.equalsIgnoreCase("Section"))
			{
				if (targetClass.equalsIgnoreCase("Folder") || targetClass.equalsIgnoreCase("Document"))
					throw new InvalidOperationException("Source nodes of Type \""+srcClass+"\" cannot perform \"Copy\" ANY operation on Target node of type \""+targetClass+"\"");
				else if (targetClass.equalsIgnoreCase("Section") || targetClass.equalsIgnoreCase("Task"))
				{
					if (!(operation == TaskType.APPEND_CHILD))
						throw new InvalidOperationException("Source nodes of Type \"Section\" cannot perform \"Copy\" operation "+operation.toString()+" on Target node of type \"Document\". Only append operations are valid.");
					else
						valid = true;
				}
				else 
				{
					// For non Folder, Document, Section or Task targets all operations are allowed
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

}
