package com.helphero.util.hhc.dom.processing.transforms;

import org.apache.log4j.Logger;

import com.helphero.util.hhc.dom.processing.TransformException;
import com.helphero.util.hhc.dom.processing.Transformer;
import com.helphero.util.hhc.rule.RuleType;

/**
 * To be implemented.
 * 
 * This transform is intended to be used for the replacement of target node(s) attribute or content values.
 * 
 * @author jcharles
 *
 */
public class ReplaceTransformer extends Transformer {
	static Logger logger = Logger.getLogger(ReplaceTransformer.class);
	private RuleType ruleType = RuleType.REPLACE; // General type of rule being performed

	public ReplaceTransformer() {
	}
	
	public void interpret() {
		
	}
	
	public void process() throws TransformException {
		
	}
}
