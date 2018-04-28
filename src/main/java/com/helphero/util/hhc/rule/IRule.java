package com.helphero.util.hhc.rule;

import java.util.HashMap;
import java.util.List;

/**
 * Interface defining all the rule transforms applied to the common object model document.
 * @author jcharles
 */
public interface IRule {
	/**
	 * Set the rule type
	 * @param type IRuleType implementation instance 
	 */
	public void setType(IRuleType type);
	
	/**
	 * Get the rule type
	 * @return IRuleType Implementation instance
	 */
	public IRuleType getType();
	
	/**
	 * Set the rule id
	 * @param id Attribute string value
	 */
	public void setId(String id);
	
	/**
	 * Get the rule id
	 * @return id Attribute string value
	 */
	public String getId();
	
	/**
	 * Set the rule target 
	 * @param target IRuleTarget implementation instance
	 */
	public void setTarget(IRuleTarget target);
	
	/**
	 * get the rule target
	 * @return IRuleTarget Implementation instance
	 */
	public IRuleTarget getTarget();
	
	/**
	 * Set the rule target type
	 * @param targetType IRuleTargetType implementation instance 
	 */
	public void setTargetType(IRuleTargetType targetType);
	
	/**
	 * set the rule target type with a string input. Used by the RuleSetManager parser.
	 * @param targetType Target type string 
	 * @throws Exception Thrown if an invalid target type is specified
	 */
	public void setTargetType(String targetType) throws Exception;
	
	/**
	 * Get the rule target type
	 * @return IRuleTargetType Target type
	 */
	public IRuleTargetType getTargetType();	
	
	/**
	 * Get the rule sub_type used to supply additional information about a rule
	 * @return IRuleSubType Rule sub type
	 */
	public IRuleSubType getSubType();
	
	/**
	 * Set the rule sub type
	 * @param subType IRuleSubType implementation instance
	 */
	public void setSubType(IRuleSubType subType);
	
	/**
	 * Set the rule sub type from a string. Used by the RuleSetManager parser.
	 * @param subType Rule sub type string
	 * @throws Exception Thrown if an invalid rule sub type is used
	 */
	public void setSubType(String subType) throws Exception;
	
	/**
	 * Is the rule disabled. If so skip over the rule and do not perform the transform. Useful for building a suite of workable rules.
	 * @return boolean flag to enable or disable the execution of the rule
	 */
	public boolean isDisable();
	
	/**
	 * Disable a rule from being executed
	 * @param disable Boolean flag to enable or disable the execution of the rule 
	 */
	public void setDisable(boolean disable);
	
	/**
	 * Get the document root XPath inside the common object xHtml document
	 * @return String Document root XPath
	 */
	public String getDocumentRootXPath();
	
	/**
	 * Set the document root XPath
	 * @param documentRootXPath The document root XPath
	 */
	public void setDocumentRootXPath(String documentRootXPath);
	
	/**
	 * Add a task to the rule
	 * @param task ITask implementation instance
	 */
	public void addTask(ITask task);
	
	/**
	 * Retrieve a list of tasks for this rule
	 * @return List of ITask implementation instances
	 */
	public List<ITask> getTasks();
	
	/**
	 * Hash the task list
	 */
	public void hashTasks();
	
	/**
	 * Get a hashmap of tasks in this rule
	 * @return HashMap HashMap of ITaskType implementation instance key and ITask implementation instance value pairs
	 */
	public HashMap<ITaskType, ITask> getTaskMap();
	
	/**
	 * Execute the rule
	 */
	public void execute();
}
