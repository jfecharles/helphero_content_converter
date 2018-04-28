package com.helphero.util.hhc.dom.processing.transforms;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.Transformer;
import com.helphero.util.hhc.rule.ITask;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.Task;
import com.helphero.util.hhc.rule.TaskType;

/**
 * This transform allows content to be deleted from target node(s).
 * 
 * @author jcharles
 *
 */
public class DeleteTransformer extends Transformer {
	static Logger logger = Logger.getLogger(DeleteTransformer.class);	
	private RuleType ruleType = RuleType.DELETE;			// General type of rule being performed
	private String targetXPath;     					// XPath to the target node

	public DeleteTransformer() {
	}

	/**
	 * Initialise all the parameters from the rule to perform the delete transform 
	 */
	public void interpret()
	{
		// Find the XPaths to the target node(s) 	
		for (ITask task : this.getRule().getTasks())
		{	
			switch ((TaskType)((Task)task).getType())
			{
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
	 * Execute the delete transform
	 */
	public void process() throws TransformException 
	{
		XPath xPath = getXPathHandler();
		
		NodeList targetNodes;
		Node parentNode;
		
		logger.info("=== Processing DeleteTransform ===");

		try {
			targetNodes = (NodeList)xPath.compile(this.getTargetXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\t\tSrc xPath="+this.getTargetXPath());
			logger.info("\t\tSrc Nodes Count="+targetNodes.getLength());
			
			for (int i = 0; i < targetNodes.getLength(); i++)
			{
				parentNode = targetNodes.item(i).getParentNode();
				
				Node targetNode = targetNodes.item(i);
				logger.info("\t\tDeleting element:name="+((Element)targetNode).getNodeName()+":class="+((Element)targetNode).getAttribute("class"));
				
				parentNode.removeChild(targetNode);				
			}
			
		} catch (Exception ex) {
			throw new TransformException("Invalid Delete Operation: "+ex.getMessage());
		}		
	}

	/**
	 * Get the XPath to the target node(s).
	 * @return targetXPath Target XPath string
	 */
	public String getTargetXPath() {
		return targetXPath;
	}

	/**
	 * Set the XPath to the target node(s).
	 * @param targetXPath Target XPath String
	 */
	public void setTargetXPath(String targetXPath) {
		this.targetXPath = targetXPath;
	}
}
