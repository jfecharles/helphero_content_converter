package com.helphero.util.hhc.dom.processing;

import org.docx4j.openpackaging.exceptions.InvalidOperationException;

import com.helphero.util.hhc.rule.IRule;
import com.helphero.util.hhc.rule.TaskType;

/**
 * This interface defines the methods required to execute and adjust transforms.
 * 
 * A transform executes a rule to transform an xHtml common object model document.
 * 
 * @author jcharles
 *
 */
public interface ITransform {
	
	public void run();
	
	/**
	 * Set the rule associated with the transform
	 * 
	 * @param rule Rule instance
	 */
	public void setRule(IRule rule);
	
	/**
	 * Retrieve the rule associated with the transform
	 * 
	 * @return IRule Rule instance
	 */
	public IRule getRule();
	
	/**
	 * Interpret all the tasks within a rule extracting all the necessary parameters from the task properties to execute the transform.
	 */
	public void interpret();
	
	/**
	 * This method checks to see of the operation to be performed on the target is valid. It dpeends upon the combination of the type of source nodes
	 * target nodes and the operation to be performed.
	 * 
	 * @param srcClass 		- The class of the first source node
	 * @param targetClass 	- The class of the target node
	 * @param operation		- The operation being performed 
	 * @return boolean Flag indicating if the operation is valid
	 * @throws InvalidOperationException Exception thrown if the operation is invalid
	 */
	public boolean isValidOperation(String srcClass, String targetClass, TaskType operation) throws InvalidOperationException;
	
	/**
	 * Process the transform. 
	 * @throws TransformException Exception thrown if there a problem with the transform
	 */
	public void process() throws TransformException;
	
	public void setDevolutionCondition(IDevolutionCondition condition);
	
	public IDevolutionCondition getDevolutionCondition();	
}
