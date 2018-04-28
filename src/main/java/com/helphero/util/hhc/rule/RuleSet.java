package com.helphero.util.hhc.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A container class that houses a list of rules and provides convenience methods to manage the rule set.
 * The rules are added from the user supplied rules.xml template file and parsed by the RuleSetManager.
 * 
 * @author jcharles
 */
public class RuleSet implements IRuleSet {

	private String documentRootXPath = null;
	private List<IRule> rules = new ArrayList<IRule>();
	private HashMap<String,IRule> rulesMap = new HashMap<String,IRule>();
	private boolean debug;
	
	public RuleSet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Add a rule
	 * @param rule IRule implementation instance - Rule
	 */
	public void add(IRule rule) {
		rules.add(rule);
	}

	/**
	 * Remove a rule
	 * @param rule IRule implementation instance - Rule
	 */
	public void remove(IRule rule) {
		rules.remove(rule);
	}

	/**
	 * Contains a supplied rule
	 * @param rule IRule implementation instance - Rule
	 */
	public boolean contains(IRule rule) {
		return rules.contains(rule);
	}
	
	/**
	 * Retrieve the rules as a list
	 * @return List of IRule (Rule) implementation instances  
	 */
	public List<IRule> getRules()
	{
		return this.rules;
	}

	/**
	 * Get the document root XPATH. 
	 * This is the path to the location of the div element with an attribute class="document" inside the XHtml intermediary common object model document.
	 * @return documentRootXPath Document root XPath string
	 */
	public String getDocumentRootXPath() {
		return documentRootXPath;
	}

	/**
	 * Set the document root XPATH
	 * @param documentRootXPath Document root XPath string
	 */
	public void setDocumentRootXPath(String documentRootXPath) {
		this.documentRootXPath = documentRootXPath;
	}

	/**
	 * Create a hashmap of the rule set used for individual rule lookup.
	 */
	public void hashRules() throws Exception {
		for (IRule rule : rules)
		{
			String key = rule.getType().toString() + "_" + rule.getTarget().toString();
			// Partition rules only occur once all other rules are instance specific
			if (rule.getType() != RuleType.PARTITION) key += "_" + rule.getId();
			if (!rulesMap.containsKey(key))
				rulesMap.put(key,rule);
			else 
				throw new Exception("Duplicate Rule: Type="+rule.getType()+": Target="+rule.getTarget()+": Id="+rule.getId());
		}
	}	
	
	/**
	 * Retrieve a HashMap of the rule set.
	 * @return HashMap of Rule key and IRule implementation instance (Rule) pairs
	 */
	public HashMap<String, IRule> getRulesMap()
	{
		return rulesMap;
	}

	/**
	 * Is internal debugging enabled?
	 * @return debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Enable internal debugging.
	 * @param debug - boolean
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
}
