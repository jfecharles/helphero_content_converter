package com.helphero.util.hhc.dom.processing;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.docx4j.openpackaging.exceptions.InvalidOperationException;
import org.w3c.dom.Document;

import com.helphero.util.hhc.rule.IRule;
import com.helphero.util.hhc.rule.TaskType;

/**
 * This class is an abstract implementation of the ITransform interface.
 * All rule specific transformations will be derived from this class providing rule type and target specific document transformations.
 * @author jcharles
 */
public abstract class Transformer implements ITransform {
	private IRule rule;
	private IDevolutionCondition condition;
	protected Document document;
	private XPath xPath =  null;
	private boolean debug = false;
	
	public Transformer() {
		xPath =  XPathFactory.newInstance().newXPath();
	}
	
	/**
	 * Convenience method to retrieve the XPath handler used to navigate nodes in the DOM.
	 * @return xPath XPath handler
	 */
	protected XPath getXPathHandler()
	{
		return xPath;
	}

	/**
	 * Assign a rule to the transform.
	 * @param rule - Rule instance
	 */
	public void setRule(IRule rule) {
		this.rule = rule;
	}

	/**
	 * Retrieve the rule assigned to the transform
	 * @return rule Rule instance
	 */
	public IRule getRule() {
		return rule;
	}

	/**
	 * Interpret the rule settings and initialise the transform setup values. 
	 */
	public void interpret() {
	}

	/**
	 * Execute the transform
	 */
	public void process() throws TransformException {
	}

	/**
	 * Execute the transform
	 */
	public void run() {
	}
	
	/**
	 * Check if the operation performed by this transform is valid. Implemented by derived classes.
	 * @return boolean
	 */
	public boolean isValidOperation(String srcClass, String targetClass, TaskType operation) throws InvalidOperationException
	{
		return true;
	}

	public void setDevolutionCondition(IDevolutionCondition condition) {
		this.condition = condition;
	}

	public IDevolutionCondition getDevolutionCondition() {
		return condition;
	}
	
	/**
	 * Set the document (DOM) to apply the transform to.
	 * @param doc Document DOM
	 */
	public void setDocument(Document doc)
	{
		this.document = doc;		
	}
	
	/**
	 * Get the document (DOM) that is to have the transform applied.
	 *  
	 * @return document - Document DOM
	 */
	public Document getDocument()
	{
		return this.document;		
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
