package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing the different types of match operations within a task.
 * @author jcharles
 *
 */
public enum TaskMatchType implements ITaskMatchType {
	COPY,
	EXPR,
	REGEX,
	NOT_SET;
	
	private TaskMatchType value;

	private TaskMatchType()
	{
	}

	private TaskMatchType(TaskMatchType value)
	{
		this.value = value;
	}

	public TaskMatchType getValue() {
		return value;
	}

	public void setValue(TaskMatchType value) {
		this.value = value;
	}

}
