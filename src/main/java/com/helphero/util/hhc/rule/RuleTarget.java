package com.helphero.util.hhc.rule;

import org.apache.commons.cli.ParseException;
import org.w3c.dom.Node;

/**
 * An enumerated type listing end target for a rule.
 * @author jcharles
 *
 */
public enum RuleTarget implements IRuleTarget {
	FOLDER,
	DOCUMENT,
	SECTION,
	TASK,
	ELEMENT,
	ELEMENT_VALUE,
	ATTRIBUTE_VALUE,
	NOT_SET;
	
	private RuleTarget value;

	private RuleTarget()
	{
	}
	
	private RuleTarget(RuleTarget value)
	{
		this.value = value;
	}

	public RuleTarget getTarget() {
		return value;
	}

	public void setValue(RuleTarget value) {
		this.value = value;
	}
}
