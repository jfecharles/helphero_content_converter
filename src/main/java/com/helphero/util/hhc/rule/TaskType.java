package com.helphero.util.hhc.rule;

/**
 * Enumerated type describing the different types of task.
 * @author jcharles
 *
 */
public enum TaskType implements ITaskType {
	TARGET_NAME,
	TARGET_PATH,
	SRC_PATH,
	SRC_FROM_PATH,
	SRC_TO_PATH,
	SRC_CHILDREN_OF_PATH,
	SRC_TEXT_MATCH,
	IMAGE_URI,
	ELEMENT,
	ATTRIBUTE_VALUE,
	PARTITION_OPTIONS,
	APPEND_CHILD,		// Append as children
	INSERT_BEFORE,		// Insert siblings before
	INSERT_AFTER,		// Insert siblings after
	NOT_SET;
	
	private TaskType value;
	
	private TaskType()
	{
	}
	
	private TaskType(TaskType value)
	{
		this.value = value;
	}

	public TaskType getValue() {
		return value;
	}

	public void setValue(TaskType value) {
		this.value = value;
	}

}
