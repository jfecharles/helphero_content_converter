package com.helphero.util.hhc.dom.processing;

public enum PartitionType implements IPartitionType {
	ROOT,
	FOLDER,
	DOCUMENT,
	SECTION,
	TASK,
	NOT_SET;
	
	private PartitionType type;
	
	private PartitionType()
	{
	}
	
	public void setType(PartitionType type)
	{
		this.type = type;
	}
	
	public PartitionType getType()
	{
		return this.type;
	}
}
