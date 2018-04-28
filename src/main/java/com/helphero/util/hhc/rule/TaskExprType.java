package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing the different types expressions used in a task match element. 
 * 
 * @author jcharles
 */
public enum TaskExprType implements ITaskExprType {
	STARTS_WITH,
	CONTAINS,
	NOT_SET;
	
	private TaskExprType value;
	
	private TaskExprType()
	{
	}
	
	private TaskExprType(TaskExprType value)
	{
		this.value = value;
	}

	public TaskExprType getValue() {
		return value;
	}

	public void setValue(TaskExprType value) {
		this.value = value;
	}

}
