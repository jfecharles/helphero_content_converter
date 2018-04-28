package com.helphero.util.hhc.rule;

import org.apache.commons.cli.ParseException;
import org.w3c.dom.Node;

/**
 * An enumerated type describing the different types of rules supported by the converter.
 * There are currently not transforms for the insert and replace types.
 * 
 * @author jcharles
 */
public enum RuleType implements IRuleType {
	PARTITION,
	CREATE,
	INSERT,
	REPLACE,
	MOVE,
	COPY,
	DELETE,
	NOT_SET;
	
	private RuleType type;
	
	private RuleType()
	{
	}
	
	public void setType(RuleType type)
	{
		this.type = type;
	}
	
	public RuleType getType()
	{
		return this.type;
	}

}
