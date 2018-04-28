package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing boolean operations that can be used to join tasks.
 * This is enum is currently not used.
 *  
 * @author jcharles
 *
 */
public enum TaskJoinType implements ITaskJoinType {
	AND,
	OR,
	NOT_SET;
	
	private TaskJoinType value;
	
	private TaskJoinType()
	{
	}
	
	private TaskJoinType(TaskJoinType value)
	{
		this.value = value;
	}

	public TaskJoinType getValue() {
		return value;
	}

	public void setValue(TaskJoinType value) {
		this.value = value;
	}

}
