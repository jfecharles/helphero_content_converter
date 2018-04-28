package com.helphero.util.hhc.dom.processing.transforms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.transforms.CopyTransformer;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DomUtils;

/**
 * This transform is derived from the copy transform and moves node(s) from a source location to a target location. 
 * The moved nodes can be placed before or after the target node as siblings or appended as a child to the target node. 
 * The cardinality for this transform is 1:1 or N:1. 
 * Note: A move operation can only be performed once for a given set of source node(s) to a single target. 
 * If the source nodes need to be duplicated at multiple targets use a copy transform for the N:M duplication and a delete transform to remove the original source nodes. 
 * @author jcharles
 */
public class MoveTransformer extends CopyTransformer {
	static Logger logger = Logger.getLogger(MoveTransformer.class);
	private RuleType ruleType = RuleType.MOVE;			// General type of rule being performed

	public MoveTransformer() {
	}

	/**
	 * Execute the move transform 
	 */
	public void process() throws TransformException {
		
		XPath xPath = getXPathHandler();
		
		NodeList srcNodes;
		Node targetNode;
		Node parentNode;
		
		logger.info("=== Processing MoveTransform ===");

		try {
			srcNodes = (NodeList)xPath.compile(this.getSrcXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\t\tSrc xPath="+this.getSrcXPath());
			logger.info("\t\t Src Nodes Count="+srcNodes.getLength());
			
			targetNode = (Node)xPath.compile(this.getTargetXPath()).evaluate(this.document, XPathConstants.NODE);
			
			if (targetNode != null && srcNodes.getLength() > 1) {
				
				boolean doOperation = false;
				
				logger.info("\t\tTarget Node: class="+((Element)targetNode).getAttribute("class"));
				
				try {
					doOperation = this.isValidOperation(((Element)srcNodes.item(0)).getAttribute("class"), ((Element)targetNode).getAttribute("class"), this.getOperation());
					
					if (doOperation) {
						
						parentNode = targetNode.getParentNode();
						
						for (int i = 0; i < srcNodes.getLength(); i++) 
						{  
							if (this.getOperation() == TaskType.APPEND_CHILD) {
								targetNode.appendChild(srcNodes.item(i));
							} else if (this.getOperation() == TaskType.INSERT_BEFORE) {
								DomUtils.insertBefore(srcNodes.item(i), targetNode);
							} else if (this.getOperation() == TaskType.INSERT_AFTER) {
								Node nextSibling = targetNode.getNextSibling();
								if (nextSibling != null) {
									// Insert the node before the next sibling
									DomUtils.insertBefore(srcNodes.item(i), nextSibling);
								} else {
									// If there is no sibling append to the end of the parent nodes children
									logger.info("\t\tParent Node: class="+((Element)parentNode).getAttribute("class"));
									if (parentNode != null)	parentNode.appendChild(srcNodes.item(i));
								}
								targetNode = srcNodes.item(i); // The target becomes the new last entry
							}
						}
					}
				} catch (InvalidOperationException ex) {
					throw new TransformException("Invalid Move Operation: "+ex.getMessage());
				}
			}
		} catch (Exception ex) {
			throw new TransformException("Invalid Move Operation: "+ex.getMessage());
		}
	}
}
