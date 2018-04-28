package com.helphero.util.hhc.dom.processing.transforms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.batik.dom.util.HashTable;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helphero.util.hhc.dom.processing.Partition;
import com.helphero.util.hhc.dom.processing.PartitionException;
import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.Transformer;
import com.helphero.util.hhc.rule.HtmlElement;
import com.helphero.util.hhc.rule.HtmlElementType;
import com.helphero.util.hhc.rule.ITask;
import com.helphero.util.hhc.rule.Rule;
import com.helphero.util.hhc.rule.RuleSubType;
import com.helphero.util.hhc.rule.RuleTarget;
import com.helphero.util.hhc.rule.RuleTargetType;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.Task;
import com.helphero.util.hhc.rule.TaskExprType;
import com.helphero.util.hhc.rule.TaskMatchType;
import com.helphero.util.hhc.rule.TaskType;
import com.helphero.util.hhc.util.DomUtils;
import com.independentsoft.msg.*;

/**
 * This transform creates node(s) at a target location or locations. 
 * The created nodes can be placed before or after the target node(s) as siblings or appended as a children to the target node(s). 
 * The transform accommodates for 1:1, 1:N new nodes to be created at target node(s).
 * This class can be used to create new partitions or add new content to a document through the use of the Task type="element". 
 * In addition it provides 2 custom specific transforms:
 * 1. The addition of a partition whose child partitions house outlook msg styled external documents.
 *    The user selects and download the external document. If they open the document it launches the email details in Outlook
  *    This custom transform is triggered by adding an attribute sub_type="process_emails" to the rule definition.  
 * 2. The addition of a partition whose child partitions contain details about all unique images used in the document.
 *    This custom transform is triggered by adding an attribute sub_type="process_images" to the rule definition.
 *     
 * @author jcharles
 */
public class CreateTransformer extends Transformer {
	static Logger logger = Logger.getLogger(CreateTransformer.class);
	private RuleType ruleType = RuleType.CREATE;	// General type of rule being performed
	private RuleSubType ruleSubType = RuleSubType.NOT_SET;
	private String parentXPath;		// XPath to parent node
	private String contentXPath;	// XPath to the content. Used to extract task details from a tasks table
	private TaskType operation;		// Operation to be performed on the parent node
	private String targetTitle;		// The value of the target node to be added to the parent 
	private RuleTarget targetNodeType = RuleTarget.DOCUMENT;	// The type of the target node
	private RuleTargetType documentType = RuleTargetType.NOT_SET; // The type of document: Procedure, Policy, Process, External
	
	private List<HtmlElement> elements = new ArrayList<HtmlElement>();

	public CreateTransformer() {
	}
	
	/**
	 * Interpret all the transform rules and tasks 
	 */
	public void interpret() {		
		// Determine the target node type
		this.setTargetNodeType((RuleTarget) (((Rule)this.getRule()).getTarget()));
		
		// Set the type of document
		this.setDocumentType((RuleTargetType) (((Rule)this.getRule()).getTargetType()));
		
		// Set the sub-type
		this.setRuleSubType((RuleSubType) ((Rule)this.getRule()).getSubType());
		
		// Find the xpath to the parent node	
		for (ITask task : this.getRule().getTasks())
		{	
			switch ((TaskType)((Task)task).getType())
			{
			case SRC_PATH:
				this.setParentXPath(((Task)task).getSource());
				break;
			case APPEND_CHILD:
				// Determine the operation to be performed on the parent
				operation = TaskType.APPEND_CHILD;
				// Set the title for the new node 
				this.setTargetTitle(((Task)task).getTargetTitle());
				break;
			case INSERT_BEFORE:
				// Determine the operation to be performed on the parent
				operation = TaskType.INSERT_BEFORE;
				// Set the title for the new node 
				this.setTargetTitle(((Task)task).getTargetTitle());
				break;
			case INSERT_AFTER:
				// Determine the operation to be performed on the parent
				operation = TaskType.INSERT_AFTER;
				// Set the title for the new node 
				this.setTargetTitle(((Task)task).getTargetTitle());
				break;
			case ELEMENT:
				operation = TaskType.ELEMENT;				
				// Extract html element from the task and add to the list of elements
				HtmlElement elem = (HtmlElement) ((Task)task).getHtmlElement();
				elements.add(elem);
				break;
			}
		}
		
		logger.info("\tinterpret(): ParentXPath="+this.getParentXPath()+":OperationType="+operation.name()+":TargetNodeName="+this.getTargetTitle());
	}

	/**
	 * Apply the transform to the document
	 */
	public void process() throws TransformException {
		
		XPath xPath = getXPathHandler();
		
		if (this.getRuleSubType() != RuleSubType.CONTENT)
		{
			this.processNewPartitions(xPath);
		}
		else
		{
			this.processNewContent(xPath);
		}
	}
	
	/**
	 * Private method to process new content tasks. 
	 * @param xPath XPath handler
	 * @throws TransformException Exception thrown if there is a problem processing the new content
	 */
	private void processNewContent(XPath xPath) throws TransformException {
		HashMap<String, Node> topNodesMap = new HashMap<String, Node>();
		List<Node> topNewNodesList = new ArrayList<Node>();
		
		// Check the Rule Target Type
		if (!(this.getTargetNodeType() == RuleTarget.SECTION || this.getTargetNodeType() == RuleTarget.TASK))
		{
			throw new TransformException ("Invalid Rule Target for Content. Only a Section or Task is allowed.");
		}
		
		// Create a list of all top nodes and add their children
		for (HtmlElement elem: elements) {
			// Create an xHtml element of the type supplied in the task definition
			HtmlElementType elemType = (HtmlElementType)elem.getType();			
			Node node = this.document.createElement(elemType.toString().toLowerCase());
			
			// Setup all the node attributes
			((Element)node).setAttribute("class", elem.getClassValue());
			((Element)node).setAttribute("style", elem.getStyleValue());
			((Element)node).setAttribute("id", elem.getId());

			if (elemType == HtmlElementType.A)
				((Element)node).setAttribute("href", elem.getSrc());
			else if (elemType == HtmlElementType.IMG)
				((Element)node).setAttribute("src", elem.getSrc());

			// Set the node value
			if (elem.getValue() != null)
				((Element)node).setTextContent(elem.getValue());
			
			// Add this node to the hash map if it has no parent
			if (elem.getParentId() == null) {
				topNodesMap.put(elem.getId(), node);
				topNewNodesList.add(node);
			}
			else
			{
				// Add this node to its parent
				if (topNodesMap.containsKey(elem.getParentId())) 
				{
					Node parent = topNodesMap.get(elem.getParentId());
				
					parent.appendChild(node);
				}
				else
				{
					// This node has an invalid parent id so elevate it to a root node.
					topNodesMap.put(elem.getId(), node);
					topNewNodesList.add(node);
				}
			}	
		}
		
		// Find the parent node
		Node xTargetNode;
		NodeList targetNodes;
		try {
			targetNodes = (NodeList)xPath.compile(this.getParentXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("processContent(): Target Nodes Length="+targetNodes.getLength());
			
			for (int i = 0; i < targetNodes.getLength(); i++) {
					
				xTargetNode = targetNodes.item(i);					
										
				logger.info("processContent(): Target Node: type="+((Element)xTargetNode).getNodeName()+":class="+((Element)xTargetNode).getAttribute("class")+":title="+((Element)xTargetNode).getAttribute("title"));
				
				for (int j = 0; j < topNewNodesList.size(); j++) {
				
					Node clonedNode = topNewNodesList.get(j).cloneNode(true); // clone the original node + children for each target node 
					
					// Perform the operation on the parent node
					if (this.getOperation() == TaskType.APPEND_CHILD) {
						xTargetNode.appendChild(clonedNode);
						logger.info("processContent(): type="+((Element)clonedNode).getNodeName()+":class="+((Element)clonedNode).getAttribute("class")+":style="+((Element)clonedNode).getAttribute("style")+": appended as a child to type="+((Element)xTargetNode).getNodeName()+":class="+((Element)xTargetNode).getAttribute("class")+":style="+((Element)xTargetNode).getAttribute("style"));
					}
					else if (this.getOperation() == TaskType.INSERT_BEFORE)
					{
						DomUtils.insertBefore(clonedNode, xTargetNode);
						logger.info("processContent(): type="+((Element)clonedNode).getNodeName()+":class="+((Element)clonedNode).getAttribute("class")+":style="+((Element)clonedNode).getAttribute("style")+": inserting before type="+((Element)xTargetNode).getNodeName()+":class="+((Element)xTargetNode).getAttribute("class")+":style="+((Element)xTargetNode).getAttribute("style"));
					}
					else if (this.getOperation() == TaskType.INSERT_AFTER)
					{
						// Get the next sibling
						Node next = xTargetNode.getNextSibling();
						
						if (next != null)
						{
							logger.info("processContent(): type="+((Element)clonedNode).getNodeName()+":class="+((Element)clonedNode).getAttribute("class")+":style="+((Element)clonedNode).getAttribute("style")+": inserting before type="+((Element)next).getNodeName()+":class="+((Element)next).getAttribute("class")+":style="+((Element)next).getAttribute("style"));
							DomUtils.insertBefore(clonedNode, next); // Insert before next sibling. In other words after the current node.
						} else {
							Node xParent = xTargetNode.getParentNode(); // No next sibling find the parent node and append after the last child
							logger.info("processContent(): type="+((Element)clonedNode).getNodeName()+":class="+((Element)clonedNode).getAttribute("class")+":style="+((Element)clonedNode).getAttribute("style")+": append as child to type="+((Element)xParent).getNodeName()+":class="+((Element)xParent).getAttribute("class")+":style="+((Element)xParent).getAttribute("style"));
							xParent.appendChild(clonedNode);
						}				
					}
				}
			}
		} catch (XPathExpressionException ex) {
			throw new TransformException("Invalid Create Operation: "+ex.getMessage());
		}		
	}
	
	/**
	 * Private method to process new partition tasks. 
	 * @param xPath XPath handler
	 * @throws TransformException Exception thrown if there is a problem processing new partitions.
	 */
	private void processNewPartitions(XPath xPath) throws TransformException {
		// Create the new node
		Node xNewNode = this.document.createElement("div");

		// Set the node class
		switch (this.getTargetNodeType())
		{
		case FOLDER:
			((Element)xNewNode).setAttribute("class", "Folder");
			break;
		case DOCUMENT:
			((Element)xNewNode).setAttribute("class", "Document");
			break;
		case SECTION:
			((Element)xNewNode).setAttribute("class", "Section");
			break;
		case TASK:
			((Element)xNewNode).setAttribute("class", "Task");
			break;
		}
		
		// Set the document type
		switch (this.getDocumentType())
		{
		case PROCEDURE:
		case NOT_SET:
			break;
		case POLICY:
			((Element)xNewNode).setAttribute("target_type", "policy");
			break;
		case PROCESS:
			((Element)xNewNode).setAttribute("target_type", "process");
			break;
		case EXTERNAL:
			((Element)xNewNode).setAttribute("target_type", "external");
			break;
		}
		
		// Set the node title value
		((Element)xNewNode).setAttribute("title", this.getTargetTitle());
		
		// Find the parent node
		Node xParentNode;
		NodeList parentNodes;
		try {
			parentNodes = (NodeList)xPath.compile(this.getParentXPath()).evaluate(this.document, XPathConstants.NODESET);
			
			logger.info("\tCreateTransformer: process(): Nodes.Length="+parentNodes.getLength());
			
			if (parentNodes.getLength() > 0)
			{
				for (int i = 0; i < parentNodes.getLength(); i++) {
					
					xParentNode = parentNodes.item(i);					
					
					Node xLastChildNode = xParentNode.getLastChild(); // The default operation is append
					Node xFirstChildNode = xParentNode.getFirstChild();
					
					if (this.getOperation() == TaskType.INSERT_BEFORE || this.getOperation() == TaskType.INSERT_AFTER)
					{
						if (this.getTargetNodeType() == RuleTarget.DOCUMENT || this.getTargetNodeType() == RuleTarget.FOLDER)
						{
							// Find the very last existing document id by retrieving a flattened list of document nodes.
							NodeList docNodes = (NodeList)xPath.compile("//div[@class='Document' and not(descendant::div[@class='Document'])]").evaluate(this.document, XPathConstants.NODESET);
							xLastChildNode = docNodes.item(docNodes.getLength()-1);
						}
						else
						{
							// Get this nodes parent and find the last child node. Used to determine the id
							xLastChildNode = xParentNode.getParentNode().getLastChild();
							xFirstChildNode = xParentNode.getParentNode().getFirstChild();
						}
					}				
	
					logger.info("\tParentNode: id="+((Element)xParentNode).getAttribute("id")+":class="+((Element)xParentNode).getAttribute("class"));
					
					if (xLastChildNode != null)
						logger.info("\tLastChildNode: id="+((Element)xLastChildNode).getAttribute("id")+":class="+((Element)xLastChildNode).getAttribute("class"));
	
					int newId = -1;
					if (this.getTargetNodeType() == RuleTarget.DOCUMENT || this.getTargetNodeType() == RuleTarget.FOLDER)
					{
						String id = ((Element)xLastChildNode).getAttribute("id");
						
						try {
							newId = Integer.parseInt(id);
							newId++;
						} catch (NumberFormatException ex) {
							Random r = new Random();
							newId = r.nextInt(10000) + 1000;
						}
									
						// Ensure the id of the new Document or Folder element is unique 
						boolean exists = true;
						while (exists) {					
							logger.info("\tLastChildNode: Checking existance of new last id="+newId);
							
							// Retrieve a matching Folder or Document Node containing the id. If the id exists then keep looping until we get a non-matching id.
							String xTestPath = "(//div/div[@id="+newId+"][starts-with(@class,'Folder')] | //div/div[@id="+newId+"][starts-with(@class,'Document')])";
							try {
								Node xNode = (Node)xPath.compile(xTestPath).evaluate(this.document, XPathConstants.NODE);
								
								if (xNode == null) 
									exists = false;	
								else
								{
									Random r = new Random();
									newId = r.nextInt(10000) + 1000;								
								}	
							} catch (XPathExpressionException ex1) {
								exists = false;
								throw new TransformException(ex1);
								// throw new TransformException("Invalid Create Operation: "+ex1.getMessage());
							}
						}
						
						// Assign the new unique id to the new node
						((Element)xNewNode).setAttribute("id", Integer.toString(newId));
					} else {
						if (xLastChildNode != null && xFirstChildNode != null) {
							String lastId = ((Element)xLastChildNode).getAttribute("id");
							// Under some circumstance namely a previous insert_before, the first id can be greater. 
							String firstId = ((Element)xFirstChildNode).getAttribute("id");
							
							try {
								newId = Integer.parseInt(lastId);
								int altId = Integer.parseInt(firstId);
								newId = altId > newId ? altId : newId;
								newId++;
							} catch (NumberFormatException ex) {
								Random r = new Random();
								newId = r.nextInt(10000) + 1000;
							}
						} else
							newId = 1;

						((Element)xNewNode).setAttribute("id", Integer.toString(newId));
					}
					
					logger.info("\tprocess(): NewNode: id="+((Element)xNewNode).getAttribute("id")+":class="+((Element)xNewNode).getAttribute("class")+":title="+((Element)xNewNode).getAttribute("title"));
					
					Node clonedNode = xNewNode.cloneNode(false); // clone the original node for each parent node 
					
					// Perform the operation on the parent node
					if (this.getOperation() == TaskType.APPEND_CHILD)
						xParentNode.appendChild(clonedNode);
					else if (this.getOperation() == TaskType.INSERT_BEFORE)
					{
						DomUtils.insertBefore(clonedNode, xParentNode);
					}
					else if (this.getOperation() == TaskType.INSERT_AFTER)
					{
						// Get the next sibling
						Node next = xParentNode.getNextSibling();					
						
						if (next != null)
						{
							logger.info("\tNewNode: id="+((Element)xNewNode).getAttribute("id")+":class="+((Element)xNewNode).getAttribute("class")+":title="+((Element)xNewNode).getAttribute("title")+": inserting before id="+((Element)next).getAttribute("id")+":class="+((Element)next).getAttribute("class")+":title="+((Element)next).getAttribute("title"));
							DomUtils.insertBefore(clonedNode, next); // Insert before next sibling. In other words after the current node.
						} else {
							Node xParent2 = xParentNode.getParentNode(); // No next sibling find the parent node and append after the last child
							logger.info("\tNewNode: id="+((Element)xNewNode).getAttribute("id")+":class="+((Element)xNewNode).getAttribute("class")+":title="+((Element)xNewNode).getAttribute("title")+": append as child to id="+((Element)xParent2).getAttribute("id")+":class="+((Element)xParent2).getAttribute("class")+":title="+((Element)xParent2).getAttribute("title"));
							xParent2.appendChild(clonedNode);
						}				
					}
					
					RuleSubType subType = (RuleSubType) ((Rule)this.getRule()).getSubType();				
					if (subType != RuleSubType.NOT_SET)
					{
						if (subType == RuleSubType.MOVE_IMAGES || subType == RuleSubType.COPY_IMAGES) {						
							this.processImages(xPath, clonedNode);						
						} else if (subType == RuleSubType.PROCESS_EMAILS) {
							this.processEmails(xPath, clonedNode, newId);
						} else if (subType == RuleSubType.PROCESS_TASKS) {
							this.processTasks(xPath, xParentNode, clonedNode);
						}
					}
				}
			} // end if parentNodes
		} catch (XPathExpressionException ex) {
			throw new TransformException("Invalid Create Operation: "+ex.getMessage());
		}		
	}
	
	/**
	 * Private method to create a "Sample Graphics" partition containing a list of unique images used in the document in child partitions.  
	 * @param xPath Reference node
	 * @param xNewNode New node
	 */
	private void processImages(XPath xPath, Node xNewNode)
	{				
		RuleSubType subType = (RuleSubType) ((Rule)this.getRule()).getSubType();
		
		logger.info("\t\tprocessImages()");
		
		HashMap<String, Integer> imagesMap = new HashMap<String, Integer>();
						
		NodeList imgNodes;

		String xImgPath = "//img";
		
		try {
			imgNodes = (NodeList)xPath.compile(xImgPath).evaluate(this.document, XPathConstants.NODESET);
			
			for (int i = 0; i < imgNodes.getLength(); i++) 
			{        	
				Node imgNode = (Node)imgNodes.item(i);
						
				if (imgNode instanceof Element)
				{
					// Extract the Image Id and name
					String sId = ((Element)imgNode).getAttribute("id");					
					String sNumId = sId.replaceAll("[^\\d]", "");
					// Extract the Image Id from the numeric part of the string
					int id = -1;
					try {
						id = Integer.parseInt(sNumId);
					} catch (NumberFormatException ex) {
						id = -1;
					}
					String sName = ((Element)imgNode).getAttribute("src");

					// Remove all the folder path information
					if (sName != null && sName.contains("/"))
					{
						int index = sName.lastIndexOf("/");
						if (index > -1) sName = sName.substring(index+1);
					}
							
					if (isDebug()) {	
						if (id > -1)
							logger.info("\t\tprocessImages(): ImgNode: id="+id+":sid="+sId+":name="+sName);
						else
							logger.info("\t\tprocessImages(): ImgNode: sid="+sId+":name="+sName);
					}
							
					// Create a span to house the text
					Node xSpan = this.document.createElement("span");
					((Element)xSpan).setAttribute("class", "DefaultParagraphFont");
					if (id > -1)
						((Element)xSpan).setTextContent("Img:id="+id+":sid="+sId+":name="+sName);
					else
						((Element)xSpan).setTextContent("Img:id="+sId+":name="+sName);	
														
					// Place the image text marker in an span before the image span
					Node xImgParentNode = imgNode.getParentNode();
					DomUtils.insertBefore(xSpan, xImgParentNode);
							
					// Create a Section title for each image
					Node xSection = this.document.createElement("div");
					((Element)xSection).setAttribute("class", "Section");
					if (id > -1) {
						((Element)xSection).setAttribute("id", Integer.toString(id));
						((Element)xSection).setAttribute("title", "Image: "+Integer.toString(id));
					}
					else {
						((Element)xSection).setAttribute("id", Integer.toString(i+1));
						((Element)xSection).setAttribute("title", "Image: "+sId);
					}
							
					// Append the image paragraph and image to the section as children by mimicking the xHtml generated by docx4j							
					// Create a paragraph wrapper
					Node xParaWrapper2 = this.document.createElement("p");
					((Element)xParaWrapper2).setAttribute("class", "Normal");
							
					// Create a paragraph span and add the text to it
					Node xParaSpan = this.document.createElement("span");
					((Element)xParaSpan).setAttribute("class", "DefaultParagraphFont");
					if (id > -1)
						((Element)xParaSpan).setTextContent("Img:id="+id+":sid="+sId+":name="+sName);
					else
						((Element)xParaSpan).setTextContent("Img:id="+sId+":name="+sName);
							
					// Create an image span and add the image to it
					Node xImgSpan = this.document.createElement("span");
					((Element)xImgSpan).setAttribute("class", "DefaultParagraphFont");
					if (subType == RuleSubType.MOVE_IMAGES)
						xImgSpan.appendChild(imgNode);
					else
						xImgSpan.appendChild(imgNode.cloneNode(true));
						
					// Add the text span and image span to the paragraph wrapper
					xParaWrapper2.appendChild(xParaSpan);
					xParaWrapper2.appendChild(xImgSpan);
						
					// Add the paragraph wrapper containing the text span and image span to the section
					xSection.appendChild(xParaWrapper2);
					
					// Only add image section if it has not already been added
					if (!imagesMap.containsKey(sId)) {
						// Append the image section to the document
						xNewNode.appendChild(xSection);							
						imagesMap.put(sId, new Integer(1));
					} 
				} // end if parentNode
			} // end for i					
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method traverses the input dom extracting tables containing tables with email (To, From, Subject and Body) content
	 * and writes this in Outlook Msg format to temp files that are referenced in email nodes. These entries are converted to external file
	 * entries in the final stage of conversion.
	 * @param xPath Reference node
	 * @param xNewNode New node
	 * @param parentId Parent Id
	 */
	private void processEmails(XPath xPath, Node xNewNode, int parentId) {
		RuleSubType subType = (RuleSubType) ((Rule)this.getRule()).getSubType();
		
		logger.info("\t\tprocessEmails()");
		
		NodeList emailTableNodes;
		
		int lookupType = 1; // Each email entry is in a separate table row with To,From,Subject,Body in row[0] and content in row[1] 

		// XPath expression to find tables containing email information 
		String xEmailPath = "//table/tbody[tr/td[1]/descendant::text()='From' or descendant::text()='from' or tr/td[1]/descendant::text()='To' or tr/td[1]/descendant::text()='to']";
		
		try {
			emailTableNodes = (NodeList)xPath.compile(xEmailPath).evaluate(this.document, XPathConstants.NODESET);
			
			if (emailTableNodes.getLength() == 0) {
				// Try an alternate XPath lookup
				lookupType = 2; // Each email entry is in a table cell where the To, From, Subject and Body are in separate paragraphs
				
				xEmailPath = "//table/tbody/tr/td[1][descendant::text()[starts-with(.,'From:') or starts-with(.,'from:') or starts-with(.,'To:') or starts-with(.,'to:')]]";
				
				emailTableNodes = (NodeList)xPath.compile(xEmailPath).evaluate(this.document, XPathConstants.NODESET);
			}
			
			logger.info("\t\tprocessEmails: Email Table Count="+emailTableNodes.getLength());
		
			for (int i = 0; i < emailTableNodes.getLength(); i++) 
			{        	
				Node emailTableNode = (Node)emailTableNodes.item(i);
				
				int docId = parentId + i + 1; // The external document Id must be unique. Make it an increment of the parentId+1+i
				
				boolean fromFound = false, toFound  = false, ccFound = false, bodyFound = false, subjectFound = false;
				String sFrom = null, sTo = null, sCc = null, sSubject = null, sBody = null;
						
				if (emailTableNode instanceof Element)
				{
					if (lookupType == 1) {
						NodeList rows = emailTableNode.getChildNodes();
						
						for (int j = 0; j < rows.getLength(); j++)
						{
							Node row = rows.item(j);
							
							if (row.hasChildNodes()) {
								NodeList cells = row.getChildNodes();
	
								if (cells.getLength() >= 2)
								{
									Node cell1 = cells.item(0);
									Node cell2 = cells.item(cells.getLength() > 1 ? 1 : 0);
									
									String c1val = cell1.getTextContent();
									String c2val = cell2.getTextContent();
									
									if (c1val != null) c1val = c1val.trim().toLowerCase();
									if (c2val != null) c2val = c2val.trim();
									
									if (c1val.equals("from")) {
										sFrom = c2val;
										fromFound = true;
									} else if (c1val.equals("to")) {
										sTo = c2val;
										toFound = true;
									} else if (c1val.equals("cc")) {
										sCc = c2val;
										ccFound = true;
									} else if (c1val.equals("subject")) {
										sSubject = c2val;
										subjectFound = true;
									} else if (c1val.contains("body")) {
										sBody = c2val;
										bodyFound = true;
									} else if (!bodyFound && c1val.contains("message")) {
										sBody = c2val;
										bodyFound = true;
									}
								}
							}
						}
					} else if (lookupType == 2) {
						
						NodeList paras = emailTableNode.getChildNodes();
						
						for (int j = 0; j < paras.getLength(); j++)
						{
							Node para = paras.item(j);
							
							String text = para.getTextContent();
													
							if (text.toLowerCase().startsWith("from")) {
								sFrom = text.replaceFirst("(From[:]{0,1}[ ]{0,})", "");
								sFrom = sFrom.trim();
								fromFound = true;
							} else if (text.toLowerCase().startsWith("to")) {
								sTo = text.replaceFirst("(To[:]{0,1}[ ]{0,})", "");
								sTo = sTo.trim();
								toFound = true;									
							} else if (text.toLowerCase().startsWith("cc")) {
								sCc = text.replaceFirst("(Cc[:]{0,1}[ ]{0,})", "");
								sCc = sCc.trim();
								ccFound = true;									
							} else if (text.toLowerCase().startsWith("subject")) {
								sSubject = text.replaceFirst("(Subject[:]{0,1}[ ]{0,})", "");
								sSubject = sSubject.trim();
								subjectFound = true;									
							} else if (text.toLowerCase().contains("body")) {
								sBody = text.replaceFirst("(Body[:]{0,1}[ ]{0,})", "");
								sBody = sBody.trim();
								bodyFound = true;
							} else if (!bodyFound && text.toLowerCase().contains("message")) {
								sBody = text.replaceFirst("(Message[:]{0,1}[ ]{0,})", "");
								sBody = sBody.trim();
								bodyFound = true;
							}
						}
					}
					
					logger.info("\t\tprocessEmails: Email From="+sFrom+":To="+sTo+":Subject="+sSubject+":Body="+sBody);
					
					// Must have a minimum of a From or To value + a Subject or Body value
					if ((fromFound || toFound) && (subjectFound || bodyFound)) {
						String msgFile = this.buildEmailMsg(sFrom, sTo, sCc, sSubject, sBody);
						
						if (msgFile != null)
						{
							// Create a Section title for each image
							Node xEmailDoc = this.document.createElement("div");
							((Element)xEmailDoc).setAttribute("class", "Document");
							((Element)xEmailDoc).setAttribute("id", Integer.toString(docId)); // The external document Id MUST be unique
							((Element)xEmailDoc).setAttribute("msgid", Integer.toString(i+1)); // The msgid only needs to be unique with respect to email messages
							((Element)xEmailDoc).setAttribute("target_type", "external");
							if (sSubject != null) {	
								((Element)xEmailDoc).setAttribute("title", sSubject+".msg");
							}
							((Element)xEmailDoc).setAttribute("src", msgFile);
							
							String sClass = ((Element)xNewNode).getAttribute("class");
							String sTitle = ((Element)xNewNode).getAttribute("title");
							
							// The parent of an external document must be a folder
							if (sClass != null && sClass.equalsIgnoreCase("Folder")) {
								xNewNode.appendChild(xEmailDoc);
								logger.info("\t\tprocessEmails: Appending External Document containing Outlook Msg format content to folder="+sTitle);
							}
						}						
					}							
				} // end if emailTableNodes instanceof HtmlElement
			} // end for i					
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method creates the temporary Outlook Msg format email file.
	 * @param sFrom Email From string
	 * @param sTo Email To string
	 * @param sCc Email Cc string
	 * @param sSubject Email Subject string
	 * @param sBody Email Body string
	 * @return String - The path to the outlook msg formatted email file 
	 */
	private String buildEmailMsg(String sFrom, String sTo, String sCc, String sSubject, String sBody) {
		
		String emailMsgFile = null;

        try {
    		Message message = new Message();
    		
    		Recipient toRecipient = new Recipient();
    		toRecipient.setAddressType("SMTP");
    		toRecipient.setDisplayType(DisplayType.MAIL_USER);
    		toRecipient.setObjectType(ObjectType.MAIL_USER);
    		toRecipient.setRecipientType(RecipientType.TO);
    		toRecipient.setDisplayName(sTo);
    		if (sTo != null && sTo.contains("@")) toRecipient.setEmailAddress(sTo);
             
    		Recipient ccRecipient = new Recipient();
    		ccRecipient.setAddressType("SMTP");
    		ccRecipient.setDisplayType(DisplayType.MAIL_USER);
    		ccRecipient.setObjectType(ObjectType.MAIL_USER);
    		ccRecipient.setRecipientType(RecipientType.CC);
    		if (sCc != null) 
    			ccRecipient.setDisplayName(sCc);
    		if (sCc != null && sCc.contains("@")) 
    			ccRecipient.setEmailAddress(sCc);
    		
    		if (sSubject != null) 	message.setSubject(sSubject);
    		if (sBody != null) 		message.setBody(sBody);
    		
    		if (sTo != null)		message.setDisplayTo(sTo);
    		if (sCc != null)		message.setDisplayCc(sCc);

    		message.getRecipients().add(toRecipient);
    		message.getRecipients().add(ccRecipient);		
            message.getMessageFlags().add(MessageFlag.UNSENT);
            message.getStoreSupportMasks().add(StoreSupportMask.CREATE);  
            
            // Address the situation where the document being converted contains no images
            File filesDir = new File("_files");
            if (!filesDir.exists())		filesDir.mkdir();
            
            String sBase = this.getDocument().getBaseURI();         
            //String sBase = this.getDocument().getDocumentURI();
            sBase = sBase.substring(0, sBase.lastIndexOf("/"));
            
            emailMsgFile = sBase + File.separator + "_files" + File.separator + UUID.randomUUID().toString() + ".msg";
            // Remove all the File URI prefix and replace Unix styles URI path delimiters to derive the Windows path to the file.
            emailMsgFile = emailMsgFile.replace("file:///", "").replaceAll("/", Matcher.quoteReplacement(File.separator));
            
            FileOutputStream fos = new FileOutputStream(emailMsgFile);
			message.save(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return emailMsgFile;
	}
	
	/**
	 * 
	 * @param xPath XPath handler
	 * @param xSrcNode The node from which task content is extracted
	 * @param xTargetNode The target Node to which task(s) content will be added
	 */
	private void processTasks(XPath xPath, Node xSrcNode, Node xTargetNode)
	{
		logger.info("processTasks()");
		
		// Extract the task table content from the source node and add as tasks to the target node
		if (xSrcNode.hasChildNodes()) {
			NodeList children = xSrcNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeName().toLowerCase().equals("table")) {
					if (child.hasChildNodes()) {
						NodeList parts = child.getChildNodes();
						for (int j = 0; j < parts.getLength(); j++) {
							Node part = parts.item(j);
							String name = part.getNodeName().toLowerCase();
							if (name.equals("tbody")) {
								if (part.hasChildNodes()) {
									NodeList rows = part.getChildNodes();
									
									int taskNum = 0;
									for (int k = 0; k < rows.getLength(); k++) {
										Node row = rows.item(k);
										
										if (row.hasChildNodes()) {
											NodeList cells = row.getChildNodes();
											
											if (cells.getLength() == 1) {
												Node cell = cells.item(0);
												
												Node xTaskNode = this.document.createElement("div");
												((Element)xTaskNode).setAttribute("class", "Task");
												((Element)xTaskNode).setAttribute("title", cell.getTextContent());
												((Element)xTaskNode).setAttribute("id", Integer.toString(++taskNum));
												
												xTargetNode.appendChild(xTaskNode);
												
												logger.info("Appended Node task title='"+cell.getTextContent()+"' id='"+taskNum+"' to Node class='"+((Element)xTargetNode).getAttribute("class")+"' title='"+((Element)xTargetNode).getAttribute("title")+"' id='"+((Element)xTargetNode).getAttribute("id")+"'");
												
											} else if (cells.getLength() > 1) {
												// Get the last newly created task 
												Node xLastTask = xTargetNode.getLastChild();
												
												// Deeply clone the second source cell node
												Node cell2 = cells.item(1);
												
												if (cell2.hasChildNodes()) {
													NodeList cellContentNodes = cell2.getChildNodes();
													
													// Append the child elements to the last newly created task  
													for (int n = 0; n < cellContentNodes.getLength(); n++) {
														Node contentNode = cellContentNodes.item(n).cloneNode(true);
														
														xLastTask.appendChild(contentNode);														
														logger.info("Appending to Node class='"+((Element)xLastTask).getAttribute("class")+"' title='"+((Element)xLastTask).getAttribute("title")+"' id='"+((Element)xLastTask).getAttribute("id")+"' Content="+contentNode.getTextContent());
													}
												}
											}
										}
									}
								}
								break;
							}
						}
					}
					break;
				}
			}
		}
	}
	
	public void run() {
	}

	/**
	 * Get the parent XPath.
	 * @return parentXPath String
	 */
	public String getParentXPath() {
		return parentXPath;
	}

	/**
	 * Set the parent XPath
	 * @param parentXPath Parent XPATH string
	 */
	public void setParentXPath(String parentXPath) {
		this.parentXPath = parentXPath;
	}

	/**
	 * Get the task operation type
	 * @return operation TaskType instance
	 */
	public TaskType getOperation() {
		return operation;
	}

	/**
	 * Set the task operation type
	 * @param operation TaskType instance
	 */
	public void setOperation(TaskType operation) {
		this.operation = operation;
	}

	/**
	 * Get the target title.
	 * @return targetTitle String
	 */
	public String getTargetTitle() {
		return targetTitle;
	}

	/**
	 * Set the target title.
	 * @param targetTitle Target title string
	 */
	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	/**
	 * Get the target for the rule i.e a folder, document, section, task, element etc.  
	 * @return targetNodeType RuleTarget instance
	 */
	public RuleTarget getTargetNodeType() {
		return targetNodeType;
	}

	/**
	 * Set the target for the rule i.e a folder, document, section, task, element etc.  
	 * @param targetNodeType RuleTarget instance
	 */
	public void setTargetNodeType(RuleTarget targetNodeType) {
		this.targetNodeType = targetNodeType;
	}

	/**
	 * Get the rule target document type i.e. procedure, policy, process, external
	 * @return documentType RuleTargetType
	 */
	public RuleTargetType getDocumentType() {
		return documentType;
	}

	/**
	 * Set the rule target document type i.e. procedure, policy, process, external
	 * @param documentType RuleTargetType
	 */
	public void setDocumentType(RuleTargetType documentType) {
		this.documentType = documentType;
	}

	/**
	 * Set the rule sub type i.e. process_emails, process_images, content. This is used to signal custom transform extensions.
	 * @return ruleSubType RuleSubType instance
	 */
	public RuleSubType getRuleSubType() {
		return ruleSubType;
	}

	/**
	 * Set the rule sub type i.e. process_emails, process_images, content. This is used to signal custom transform extensions.
	 * @param ruleSubType RuleSubType instance
	 */
	public void setRuleSubType(RuleSubType ruleSubType) {
		this.ruleSubType = ruleSubType;
	}

}