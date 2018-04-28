package com.helphero.util.hhc.rule;

import java.util.UUID;

import org.w3c.dom.Node;

/**
 * Class to manage the details of a task within a rule.
 * 
 * @author jcharles
 */
public class Task implements ITask {

	private ITaskType type;
	private ITaskType dependent;  // Dependent Type
	private String source;
	private ITaskPosition position;
	private IMatchProperties matchProperties;
	private ITaskJoinType joinType;
	private boolean keepIntroNodes = false;
	private String introPartitionTitle; // Shared variable with 2 sets of mutually exclusive setters/getters: setTitle/getTitle + setIntroPartitionTitle/getIntroPartitionTitle
	private IElement htmlElement;
	
	public Task() {
	}

	/**
	 * Set the task type
	 * @param type ITaskType implementation instance - TaskType
	 */
	public void setType(ITaskType type) {
		this.type = type;	
	}
	
	/**
	 * Set the task type from a supplied string. An exception is thrown if an invalid type if supplied.
	 * Valid types a found in the TaskType enumerated type.
	 * @param type - String
	 * @throws Exception Exception thrown if an invalid target type string is used
	 */
	public void setType(String type) throws Exception {	
		if (type != null)
		{
			this.type = TaskType.valueOf(type.toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task type specified.");
	}
	
	/**
	 * Set the task type from the type attribute within a task element.
	 * This is used by the RuleSetManager parser.
	 * @param xType - Node
	 * @throws Exception Exception thrown if a node with a node name other than type is used
	 */
	public void setType(Node xType) throws Exception {	
		if (xType.getNodeType() == Node.ATTRIBUTE_NODE && xType.getNodeName().equalsIgnoreCase("type"))
		{
			this.type = TaskType.valueOf(xType.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task type specified.");
	}
	
	/**
	 * This method is used to create dependencies between tasks.
	 * It is currently not used.
	 * @param dependent - String
	 * @throws Exception Thrown if an invalid dependent string is used
	 */
	public void setDependentType(String dependent) throws Exception {	
		if (dependent != null)
		{
			this.dependent = TaskType.valueOf(dependent.toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task dependent type specified.");
	}
	
	/**
	 * Set the dependent type from the dependent attribute in a task element
	 * This method is currently not used.
	 * @param xType - Node
	 * @throws Exception Thrown if a node with a node name other than dependent is used
	 */
	public void setDependentType(Node xType) throws Exception {	
		if (xType.getNodeType() == Node.ATTRIBUTE_NODE && xType.getNodeName().equalsIgnoreCase("dependent"))
		{
			this.dependent = TaskType.valueOf(xType.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task dependent type specified.");
	}

	/**
	 * Get the task type
	 * @return ITaskType - Type
	 */
	public ITaskType getType() {
		return this.type;
	}

	/**
	 * Set the Task position
	 * @param position ITaskPosition implementation instance - TaskPosition
	 */
	public void setPosition(ITaskPosition position) {
		this.position = position;		
	}
	
	/**
	 * Set the task type position from a string. 
	 * Valid values are found in the TaskPosition enumerated type. An exception is thrown if an invalid value is supplied.
	 * @param position - String
	 * @throws Exception Thrown if an invalid task position string is used. See TaskPosition enumerated type.
	 */
	public void setPosition(String position) throws Exception {	
		if (position != null)
		{
			this.position = TaskPosition.valueOf(position.toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task position specified.");
	}

	/**
	 * Set the task type position from the position attribute within a task element.
	 * This method is used by the RuleSetManager parser.
	 * @param xPosition - Node
	 * @throws Exception Thrown is a node with a node name other than position is used
	 */
	public void setPosition(Node xPosition) throws Exception {	
		if (xPosition.getNodeType() == Node.ATTRIBUTE_NODE && xPosition.getNodeName().equalsIgnoreCase("position"))
		{
			position = TaskPosition.valueOf(xPosition.getNodeValue().toUpperCase());
		}
		else 
			throw new Exception ("Invalid Task position specified.");
	}

	/**
	 * Get the task position.
	 * @return ITaskPosition - TaskPosition
	 */
	public ITaskPosition getPosition() {
		return this.position;
	}
	
	/**
	 * Set the source or target XPath value.
	 * @param source XPATH value for a source or target 
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * Set the XPATH value for a source or target xpath from the "src" attribute in a task element.
	 * This method is used by the RuleSetManager parser.
	 * @param xSrc - Node
	 * @throws Exception Thrown is node with a node name other than src is used.
	 */
	public void setSource(Node xSrc) throws Exception {	
		if (xSrc.getNodeType() == Node.ATTRIBUTE_NODE && xSrc.getNodeName().equalsIgnoreCase("src"))
		{
			this.source = xSrc.getNodeValue();
		}
		else 
			throw new Exception ("Invalid Task src specified.");
	}
	
	/**
	 * Get the XPATH value for a source or target xpath.
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Set the task boolean join type. Defined in the TaskJoinType enumerated type.
	 * @param joinType ITaskJoinType implementation instance - TaskJoinType 
	 */
	public void setJoinType(ITaskJoinType joinType) {
		this.joinType = joinType;
	}
	
	/**
	 * Set the task boolean join type from a string. 
	 * Valid values are defined in the TaskJoinType enumerated type. An exception will be thrown if an invalid value is used.
	 * @param join - String 
	 * @throws Exception Thrown if an invalid join param value is used. See TaskJoinType enumerated type
	 */
	public void setJoinType(String join) throws Exception {	
		if (join != null)
		{
			this.joinType = TaskJoinType.valueOf(join);
		}
		else 
			throw new Exception ("Invalid Task join specified.");
	}
	
	/**
	 * Set the task boolean join type from "join" attribute in a task element.
	 * This method is used by the RuleSetManager parser.
	 * @param xJoin - Node
	 * @throws Exception Thrown if a node with a node name other than join is used
	 */
	public void setJoinType(Node xJoin) throws Exception {	
		if (xJoin.getNodeType() == Node.ATTRIBUTE_NODE && xJoin.getNodeName().equalsIgnoreCase("join"))
		{
			this.joinType = TaskJoinType.valueOf(xJoin.getNodeValue());
		}
		else 
			throw new Exception ("Invalid Task join specified.");
	}
	
	/**
	 * Get the task boolean join type.
	 * @return ITaskJoinType - TaskJoinType
	 */
	public ITaskJoinType getJoinType() {
		return joinType;
	}

	/**
	 * Set the task MatchProperties instance
	 * @param properties IMatchProperties implementation instance - MatchProperties 
	 */
	public void setMatchProperties(IMatchProperties properties) {
		this.matchProperties = properties;		
	}
	
	/**
	 * Get the task MatchProperties instance
	 * @return IMatchProperties Implementation instance - MatchProperties
	 */
	public IMatchProperties getMatchProperties() {
		return matchProperties;
	}

	/**
	 * Set the dependent task type.
	 * This method is currently not used.
	 * @param dependent Dependent ITaskType implementation instance - TaskType
	 */
	public void setDependentType(ITaskType dependent) {
		this.dependent = dependent;
	}

	/**
	 * Get the dependent task type.
	 * @return ITaskType - Dependent TaskType instance 
	 */
	public ITaskType getDependentType() {
		return this.dependent;
	}

	/**
	 * Set the keep introductory nodes flag from a string. Valid values are: "true", "false", "yes", "T", "N", "1", "0"
	 * @param sKeep - String
	 */
	public void setKeepIntroNodes(String sKeep) {
		this.keepIntroNodes = true;
		if (sKeep != null)
		{
			if (sKeep.toUpperCase().equals("TRUE") || sKeep.toUpperCase().equals("YES") || sKeep.toUpperCase().equals("T") || sKeep.toUpperCase().equals("Y") || sKeep.toUpperCase().equals("1"))
				this.keepIntroNodes = true;
			else if (sKeep.toUpperCase().equals("FALSE") || sKeep.toUpperCase().equals("NO") || sKeep.toUpperCase().equals("F") || sKeep.toUpperCase().equals("N") || sKeep.toUpperCase().equals("0"))
				this.keepIntroNodes = false;
		}
	}

	/**
	 * Set the keep introductory nodes flag. This is used by the various partitioners to preserve introductory nodes
	 * between the partition definition and its first child partition. The introductory nodes will be
	 * preserved in a child partition title named "Folder|Document|Section|Task Preliminaries" by default. 
	 * @param keep Boolean flag to indicate if introductory nodes will keep preserved
	 */
	public void setKeepIntroNodes(boolean keep) {
		this.keepIntroNodes = keep;		
	}

	/**
	 * Get the keep introductory nodes flag
	 * @return keepIntroNodes Boolean flag to indicate if introductory nodes will keep preserved
	 */
	public boolean getKeepIntroNodes() {
		return this.keepIntroNodes;
	}

	/**
	 * Set the introductory partition title overriding the default name.
	 * @param title Introductory partition title 
	 */
	public void setIntroPartitionTitle(String title) {
		if (title != null && title.length() > 0)
			this.introPartitionTitle = title;
		else
			this.introPartitionTitle = "Preliminaries";
	}

	/**
	 * Get the introductory partition title
	 * @return introPartitionTitle Introductory Partition Title string
	 */
	public String getIntroPartitionTitle() {
		return this.introPartitionTitle;
	}

	/**
	 * For a task of type "insert_before" or "insert_after" or "append_child", set the target title
	 * @param title Target title string
	 */
	public void setTargetTitle(String title) {
		if (title != null && title.length() > 0)
			this.introPartitionTitle = title;
		else
		{
			String uuid = UUID.randomUUID().toString();
			this.introPartitionTitle = uuid.substring(0, uuid.indexOf("-", 0)); // A shortened uuid
		}
	}

	/**
	 * Get the target title attribute for a task of type "insert_before" or "insert_after" or "append_child"
	 * @return introPartitionTitle Introductory Partition Title string
	 */
	public String getTargetTitle() {
		return introPartitionTitle;
	}

	/**
	 * Get the task of type HtmlElement
	 * @return IElement Implementation instance - HtmlElement
	 */
	public IElement getHtmlElement() {
		return htmlElement;
	}

	/**
	 * Set the task to be of type HtmlElement
	 * @param htmlElement IElement Implementation instance - HtmlElement
	 */
	public void setHtmlElement(IElement htmlElement) {
		this.htmlElement = htmlElement;
	}		
}
