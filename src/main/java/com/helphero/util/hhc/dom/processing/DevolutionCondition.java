package com.helphero.util.hhc.dom.processing;

public enum DevolutionCondition implements IDevolutionCondition {
	REALIGN_TO_ROOT_CONTAINER,
	NOT_SET;
	
	private DevolutionCondition condition;
	
	private DevolutionCondition()
	{
	}
	
	public void setType(DevolutionCondition condition)
	{
		this.condition = condition;
	}
	
	public DevolutionCondition getType()
	{
		return this.condition;
	}
}
