package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing different positions source nodes can be created, copied to or moved relative to
 * a target.
 * @author jcharles
 *
 */
public enum TaskPosition implements ITaskPosition {
	BEFORE,
	AFTER,
	NOT_SET;
	
	private TaskPosition value;
	
	private TaskPosition()
	{
	}
	
	private TaskPosition(TaskPosition value)
	{
		this.value = value;
	}

	public TaskPosition getValue() {
		return value;
	}

	public void setValue(TaskPosition value) {
		this.value = value;
	}
}
