package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing different rule sub types. A sub type is used to describe extensions to rules or 
 * custom operations also performed by the rule.
 *  
 * @author jcharles
 *
 */
public enum RuleSubType implements IRuleSubType {
	MOVE_IMAGES,
	COPY_IMAGES,
	PROCESS_EMAILS,
	PROCESS_TASKS,
	CONTENT,
	ADJACENT,
	NOT_SET;
	
	private RuleSubType subType;
	
	private RuleSubType()
	{
	}
	
	public void setSubType(RuleSubType subType)
	{
		this.subType = subType;
	}
	
	public RuleSubType getSubType()
	{
		return this.subType;
	}
}
