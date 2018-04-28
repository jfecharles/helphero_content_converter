package com.helphero.util.hhc.rule;

import java.util.HashMap;
import java.util.List;

/**
 * Interface defining a rule set and its operations
 * @author jcharles
 */
public interface IRuleSet {
	/**
	 * Add a rule to the rule set
	 * @param rule IRule implementation instance 
	 */
	public void add(IRule rule);
	
	/**
	 * Remove a rule from the rule set
	 * @param rule IRule implementation instance
	 */
	public void remove(IRule rule);
	
	/**
	 * Does this rule contain the specified rule
	 * @param rule IRule implementation instance
	 * @return boolean Flag to indicate if the Rule Set contains this rule
	 */
	public boolean contains(IRule rule);
	
	/**
	 * Retrieve a list of rules in the rule set.
	 * @return List of IRule implementation instances
	 */
	public List<IRule> getRules();
	
	/**
	 * 
	 * @return String Document root XPath
	 */
	public String getDocumentRootXPath();
	
	/**
	 * Set the xpath to the common object model xHtml internal document root node 
	 * @param documentRootXPath Document root XPath
	 */
	public void setDocumentRootXPath(String documentRootXPath);
	
	/**
	 * Create a HashMap for the rule set
	 * @throws Exception Thrown if a problem occurs creating a HashMap of rules in a rule set
	 */
	public void hashRules() throws Exception;
	
	/**
	 * Get a hash map for the rule set
	 * @return HashMap HashMap of rule key and IRule implementation instance pairs
	 */
	public HashMap<String, IRule> getRulesMap();
}
