package com.helphero.util.hhc.rule;

/**
 * Interface defining a task and getters and setters for its properties 
 * @author jcharles
 */
public interface ITask {
	/**
	 * Set the task type
	 * @param type ITaskType implementation instance
	 */
	public void setType(ITaskType type);
	
	/**
	 * Get the task type
	 * @return ITaskType Task Type instance
	 */
	public ITaskType getType();
	
	/**
	 * Set the dependent task type
	 * @param dependent Dependent task type  
	 */
	public void setDependentType(ITaskType dependent);
	
	/**
	 * Get the dependent task type
	 * @return ITaskType Dependent Task Type instance
	 */
	public ITaskType getDependentType();
	
	/**
	 * Set the task position: insert_before, insert_after, append_child
	 * @param position Task Position enumerated type 
	 */
	public void setPosition(ITaskPosition position);
	
	/**
	 * Get the task position. This is a directive to inform the rule of where to target the position of any moved, copied or created nodes.
	 * @return Task Position enumerated type 
	 */
	public ITaskPosition getPosition();	
	
	/**
	 * Set the source. Used to set the XPath to target nodes or source nodes 
	 * @param source XPath to target or source nodes  
	 */
	public void setSource(String source);
	
	/**
	 * Get the source XPath for source or target nodes.
	 * @return String XPath to target or source nodes  
	 */
	public String getSource();
	
	/**
	 * Set the type of join operation
	 * @param type Task Join Type. See TaskJoinType 
	 */
	public void setJoinType(ITaskJoinType type);
	
	/**
	 * Get the type of join operation
	 * @return ITaskJoinType Task Join Type
	 */
	public ITaskJoinType getJoinType();
	
	/**
	 * Retain (true) or discard (false) all nodes between the partition and its first child partition 
	 * @param keep boolean flag to indicate if nodes between the partition and its first child partition are kept
	 */
	public void setKeepIntroNodes(boolean keep);
	
	/**
	 * Get the keep intro nodes value. if true - retain or false - discard all nodes between the partition and its parent partition
	 * @return boolean flag to indicate if nodes between the partition and its first child partition are kept
	 */
	public boolean getKeepIntroNodes();

	/**
	 * set the introductory partition title
	 * @param title Introductory partition title 
	 */
	public void setIntroPartitionTitle(String title);
	
	/**
	 * Get the introductory partition title
	 * @return The introductory partition title
	 */
	public String getIntroPartitionTitle();
	
	/**
	 * Set the match condition properties for a task
	 * @param properties Task Match Properties 
	 */
	public void setMatchProperties(IMatchProperties properties);
	
	/**
	 * get the match condition properties for a task
	 * @return IMatchProperties Task Match Properties 
	 */
	public IMatchProperties getMatchProperties();	
	
	/**
	 * Set the title for the target node(s)
	 * @param title Target title string
	 */
	public void setTargetTitle(String title);
	
	/**
	 * Get the title for the target nodes.
	 * @return Target title string
	 */
	public String getTargetTitle();

}