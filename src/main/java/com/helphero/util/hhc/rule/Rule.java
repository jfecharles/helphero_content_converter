package com.helphero.util.hhc.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.w3c.dom.Node;

/**
 * This class models the fundamental components of a rule or transform.
 * 
 * @author jcharles
 */
public class Rule implements IRule {
	
	private IRuleTarget target;
	private IRuleTargetType targetType;
	private IRuleType ruleType;
	private IRuleSubType ruleSubType = RuleSubType.NOT_SET;
	private String documentRootXPath = null;
	private List<ITask> tasks = new ArrayList<ITask>();
	private HashMap<ITaskType, ITask> taskMap = new HashMap<ITaskType, ITask>();
	private String id = UUID.randomUUID().toString(); // if the id is not set, set it to something unique
	private boolean disable = false;

	public Rule()  {
	}

	/**
	 * Set the rule type passing RuleType enumerated type values. 
	 * Valid values are: PARTITION,	CREATE,	INSERT,	REPLACE, MOVE, COPY, DELETE, NOT_SET
	 * @param type IRuleType implementation instance 
	 */
	public void setType(IRuleType type) {
		this.ruleType = (RuleType)type;
	}
	
	/**
	 * Set the rule type from a string value. If the values does not exist in the RuleType enumeration an exception is thrown.
	 * @param type - String
	 * @throws Exception Thrown if an invalid type param is used
	 */
	public void setType(String type) throws Exception {	
		if (type != null)
		{
			this.ruleType = RuleType.valueOf(type.toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleType specified.");
	}

	/**
	 * Set the RuleType from rule element node attributes. 
	 * This method is used by the RuleSetManager parser.
	 * @param xType - Node
	 * @throws Exception Thrown if an invalid node with a node name other than type is used
	 */
	public void setType(Node xType) throws Exception {	
		if (xType.getNodeType() == Node.ATTRIBUTE_NODE && xType.getNodeName().equalsIgnoreCase("type"))
		{
			this.ruleType = RuleType.valueOf(xType.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleType specified.");
	}

	/**
	 * Get the RuleType. 
	 * Valid values are: PARTITION,	CREATE,	INSERT,	REPLACE, MOVE, COPY, DELETE, NOT_SET
	 * @return IRuleType - RuleType
	 */
	public IRuleType getType() {
		return this.ruleType;
	}
	
	/**
	 * Get the Rule Sub Type. The RuleSubType is used to indicate addition types of rules
	 * within a specific rule. It is used to define custom or just extensions to transforms.
	 * Valid values are: MOVE_IMAGES, COPY_IMAGES, PROCESS_EMAILS, CONTENT,	ADJACENT, NOT_SET;
	 * @return IRuleSubType - RuleSubType
	 */
	public IRuleSubType getSubType() {
		return ruleSubType;
	}

	/**
	 * Set the Rule Sub Type using RuleSubType enumerated values
	 * @param subType IRuleSubType implementation instance - RuleSubType 
	 */
	public void setSubType(IRuleSubType subType) {
		this.ruleSubType = (RuleSubType)subType;
	}
	
	/**
	 * Set the Rule Sub Type using a string value. If the value does not exist in the RuleSubType enumerated list
	 * an exception will be thrown.
	 * @param subType - String
	 */
	public void setSubType(String subType) throws Exception {	
		if (subType != null)
		{
			this.ruleSubType = RuleSubType.valueOf(subType.toUpperCase());
		}
		else 
			throw new Exception ("Invalid Rule SubType specified.");
	}
	
	/**
	 * Set the Rule Sub Type using a rule element attribute of sub_type.
	 * This method is used by the RuleSetManager parser.
	 * @param xType - Node
	 * @throws Exception Thrown if a noe name other than subtype is used
	 */
	public void setSubType(Node xType) throws Exception {	
		if (xType.getNodeType() == Node.ATTRIBUTE_NODE && xType.getNodeName().equalsIgnoreCase("subtype"))
		{
			this.ruleSubType = RuleSubType.valueOf(xType.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid Rule SubType specified.");
	}

	/**
	 * Set the rule target using one of the RuleTarget enumerated types.
	 * @param target IRuleTarget implementation instance
	 */
	public void setTarget(IRuleTarget target) {
		this.target = (RuleTarget)target;		
	}
	
	/**
	 * Set the rule target using a string. If a value not used in RuleTarget enumerated list an exception will be thrown
	 * Valid values include: FOLDER, DOCUMENT, SECTION, TASK, ELEMENT, ELEMENT_VALUE, ATTRIBUTE_VALUE, NOT_SET	
	 * @param target - String
	 * @throws Exception Thrown if an invalid rule target string is used 
	 */
	public void setTarget(String target) throws Exception {	
		if (target != null)
		{
			this.target = RuleTarget.valueOf(target.toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleTarget specified.");
	}

	/**
	 * Set the rule target from the rule element attribute called target. If an invalid value is used an exception will be thrown.
	 * @param xTarget - Node
	 * @throws Exception Thrown if an invalid node rule target is used
	 */
	public void setTarget(Node xTarget) throws Exception {	
		if (xTarget.getNodeType() == Node.ATTRIBUTE_NODE && xTarget.getNodeName().equalsIgnoreCase("target"))
		{
			target = RuleTarget.valueOf(xTarget.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleTarget specified.");
	}

	/**
	 * Get the rule target type.
	 * Valid values include: PROCEDURE, POLICY, PROCESS, EXTERNAL, NOT_SET
	 * @return IRuleTarget Rule Target
	 */
	public IRuleTargetType getTargetType() {
		return targetType;
	}

	/**
	 * Set the rule target type.
	 * Valid values include: PROCEDURE, POLICY, PROCESS, EXTERNAL, NOT_SET
	 * @param targetType IRuleTargetType implementation instance - RuleTargetType
	 */
	public void setTargetType(IRuleTargetType targetType) {
		this.targetType = (RuleTargetType)targetType;
	}
	
	/**
	 * Set the rule target type from a string. If an invalid value is passed an exception is thrown.
	 * Valid values include: "procedure", "policy", "process", "external", "not_set"
	 * @param targetType Target Type string
	 */
	public void setTargetType(String targetType) throws Exception {	
		if (targetType != null)
		{
			this.targetType = RuleTargetType.valueOf(targetType.toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleTargetType specified.");
	}
	
	/**
	 * Set the target type from the rule element target_type attribute. This is used to set different types of documents.
	 * The default document type is procedure.
	 * This method is used by the RuleSetManager parser.
	 * @param xTargetType - Node
	 * @throws Exception Thrown if a node name other than target_type is used.
	 */
	public void setTargetType(Node xTargetType) throws Exception {	
		if (xTargetType.getNodeType() == Node.ATTRIBUTE_NODE && xTargetType.getNodeName().equalsIgnoreCase("target_type"))
		{
			targetType = RuleTargetType.valueOf(xTargetType.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid RuleTargetType specified.");
	}

	/**
	 * Get the rule target.
	 * Valid values include: FOLDER, DOCUMENT, SECTION, TASK, ELEMENT, ELEMENT_VALUE, ATTRIBUTE_VALUE, NOT_SET
	 * Currently only FOLDER, DOCUMENT, SECTION, TASK values are used.
	 * @return target - IRuleTarget implementation instance - RuleTarget
	 */
	public IRuleTarget getTarget() {
		return this.target;
	}

	/**
	 * Add a task to the list of tasks for this rule.
	 * @param task ITask implementation instance - Task
	 */
	public void addTask(ITask task) {
		this.tasks.add((Task)task);
	}
	
	/**
	 * Get the list of tasks in this rule.
	 * @return tasks List of ITask implementation instances
	 */
	public List<ITask> getTasks() {
		return this.tasks;
	}
	
	/**
	 * Build a hash map of tasks in this rule.
	 */
	public void hashTasks()
	{
		for (ITask task : tasks)
		{
			if (!this.taskMap.containsKey(task.getType()))
				this.taskMap.put(task.getType(), task);
			else 
			{
				// Only Element Task Types can occur many times in a Rule. 
				if (task.getType() != TaskType.ELEMENT)
					System.err.println("Warning: Rule is ignoring duplicate task \'"+task.getType()+"\' declaration.");
			}
		}
	}
	
	/**
	 * Retrieve the hash map of rules in this task
	 * @return HashMap HashMap of ITaskType implementation instance as key and ITask implementation instance as value pairs 
	 */
	public HashMap<ITaskType, ITask> getTaskMap()
	{
		return taskMap;
	}
	
	/**
	 * Execute this rule.
	 */
	public void execute() {	
	}

	/**
	 * Get the document root XPATH. The generated XHtml documents have a top level div element with an attribute class="document".
	 * This method returns the XPATH to where that div is located in the intermediary XHtml document.
	 * @return documentRootXPath Document root XPath string
	 */
	public String getDocumentRootXPath() {
		return documentRootXPath;
	}

	/**
	 * Set the XPATH to the top level document div.
	 * @param documentRootXPath Document root XPath string
	 */
	public void setDocumentRootXPath(String documentRootXPath) {
		this.documentRootXPath = documentRootXPath;
	}

	/**
	 * Set the id for this rule.
	 * @param id Rule "id" attribute value
	 */
	public void setId(String id) {
		this.id = id;	
	}

	/**
	 * Get the id for this rule
	 * @return id Rule "id" attribute value
	 */
	public String getId() {
		return id;
	}

	/**
	 * Is this rule disabled from being executed
	 * @return boolean
	 */
	public boolean isDisable() {
		return disable;
	}

	/**
	 * Set the disable flag to enable or disable this rule from being executed.
	 * @param disable Flag to enable or disable this rule from being executed
	 */
	public void setDisable(boolean disable) {
		this.disable = disable;
	}
	
	/**
	 * Set the disable flag to enable or disable this rule from being executed using the rule element disable attribute.
	 * This method is used by the RuleSetManager.
	 * @param xDisable Rule disable attribute node
	 * @throws Exception Thrown if a node whose value other than true or false is used
	 */
	public void setDisable(Node xDisable) throws Exception {
		String sValue = xDisable.getNodeValue();
		if (sValue != null && sValue.length() > 0 && xDisable.getNodeType() == Node.ATTRIBUTE_NODE)
		{
			if (sValue.equalsIgnoreCase("true"))
				this.setDisable(true);
			else if (sValue.equalsIgnoreCase("false"))
				this.setDisable(false);
			else 
				throw new Exception ("Invalid Rule value=\""+sValue+"\" assigned to attribute \"disable\" specified.");
		} 
	}
}
