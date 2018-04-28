package com.helphero.util.hhc.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

/**
 * Implementation class of the IMatchProperties interface.
 * This class is used to provide conditional matching for source and target path tasks.
 * The match element is defined as a child of the task element in the rules.xml rules template file.
 * Many of the capabilities of this class can be performed using XPATH conditional notation. However
 * the replacement capability is unique to this class.
 * 
 * @author jcharles
 *
 */
public class MatchProperties implements IMatchProperties {

	private TaskMatchType type; 	// COPY, EXPR, REGEX, NOT_SET;
	private TaskExprType exprType; 	// STARTS_WITH, CONTAINS
	private String exprValue;
	private String pattern;
	private String replaceWith;
	private String attributeName;
	private String attributeValue;
	
	private boolean matchCondition = false;
	private boolean replaceCondition = false;
	
	public MatchProperties() {
	}

	/**
	 * Get the match type: One of COPY, EXPR, REGEX, NOT_SET
	 * @return TaskMatchType
	 */
	public TaskMatchType getType() {
		return type;
	}

	/**
	 * Set the match type: COPY, EXPR, REGEX, NOT_SET
	 * @param type - TaskMatchType
	 */
	public void setType(TaskMatchType type) {
		this.type = type;
	}
	
	/**
	 * Set the match type from a String. The supplied string type must match one of: copy, expr, regex, not_set 
	 * otherwise an exception will be thrown.  
	 * @param type - String
	 * @throws Exception Thrown if the supplied Match Type string is not valid
	 */
	public void setType(String type) throws Exception {	
		if (type != null)
		{
			this.type = TaskMatchType.valueOf(type);
		}
		else 
			throw new Exception ("Invalid Task match type specified.");
	}
	
	/**
	 * This method is used by the RuleSetManager parser to set the type from a node attribute value in the rules.xml 
	 * rules template file.
	 * @param xType - Node
	 * @throws Exception Thrown if the supplied match element type attribute is not valid
	 */
	public void setType(Node xType) throws Exception {	
		if (xType.getNodeType() == Node.ATTRIBUTE_NODE && xType.getNodeName().equalsIgnoreCase("type"))
		{
			this.type = TaskMatchType.valueOf(xType.getNodeValue());
		}
		else 
			throw new Exception ("Invalid Task match type specified.");
	}

	/**
	 * Get the regular expression pattern.
	 * @return pattern Regular expression pattern string
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Set the regular expression pattern. If null an Exception will be thrown.
	 * @param pattern Regular expression pattern string
	 * @throws Exception Thrown if the regular expression pattern is invalid
	 */
	public void setPattern(String pattern) throws Exception {
		if (pattern != null)
			this.pattern = pattern;
		else
			throw new Exception ("Invalid Task match pattern specified.");
	}
	
	/**
	 * Set the regular expression pattern from the task element pattern attribute in the rules template file - rules.xml.
	 * This method is used by the RuleSetManager parser. 
	 * @param xPattern - Node
	 * @throws Exception Thrown if the regular expression "pattern" attribute in the match element is invalid
	 */
	public void setPattern(Node xPattern) throws Exception {	
		if (xPattern.getNodeValue() != null && xPattern.getNodeType() == Node.ATTRIBUTE_NODE && xPattern.getNodeName().equalsIgnoreCase("pattern"))
		{
			this.pattern = xPattern.getNodeValue();
		}
		else 
			throw new Exception ("Invalid Task match pattern specified.");
	}

	/**
	 * Get the replacewith attribute string value in the match element associated with a task element in the rules template file.
	 * @return replaceWith Replace with string
	 */
	public String getReplaceWith() {
		return replaceWith;
	}

	/**
	 * Set the replacewith attribute value in the match element associated with a task element in the rules template file.
	 * @param replaceWith Replace with string
	 * @throws Exception Thrown if an invalid replaceWith value is used
	 */
	public void setReplaceWith(String replaceWith) throws Exception {	
		if (replaceWith != null)
		{
			this.replaceWith = replaceWith;
		}
		else 
			throw new Exception ("Invalid Task match replaceWith specified.");
	}
	
	/**
	 * Set the replacewith attribute value in the match element associated with a task element in the rules template file
	 * from match node. This method is used by the RuleSetManager parser.
	 * @param xReplaceWith Replace with match attribute "replacewith" attribute Node
	 * @throws Exception Thrown if an invalid replaceWith value is used
	 */
	public void setReplaceWith(Node xReplaceWith) throws Exception {	
		if (xReplaceWith.getNodeType() == Node.ATTRIBUTE_NODE && xReplaceWith.getNodeName().equalsIgnoreCase("replacewith"))
		{
			this.replaceWith = xReplaceWith.getNodeValue();
		}
		else 
			throw new Exception ("Invalid Task match replaceWith specified.");
	}
	
	/**
	 * Apply the replacewith regular expression or copy to the value of the specified attribute.
	 * @param s Replace with replacement string
	 * @return sResult Replacement string result after applying the regular expression pattern 
	 * @throws Exception Thrown if an invalid s param value is used
	 */
	public String applyReplace(String s) throws Exception
	{
		// This actually covers the COPY condition
		String sResult = s;
		
		if (this.isReplaceCondition())
		{
			if (type == TaskMatchType.REGEX)
			{		
				Pattern p = Pattern.compile(pattern);
				
				Matcher m = p.matcher(s);
				
				sResult = m.replaceAll(replaceWith);
			}
			else if (type == TaskMatchType.COPY)
			{
				sResult = s;
			}
			else
				throw new Exception ("Cannot apply a replace operation for the Task match type specified.");
		}
		else
			throw new Exception ("Cannot apply a replace operation with no replace conditions set.");
			
		
		return sResult;	
	}
	
	/**
	 * Check if the supplied string is a match for the regular expression or contains/starts_with expression.
	 * @param s String used to test the regular expression or the contains or starts-with expressions
	 * @return match Boolean flag to indicate if the supplied string matches the expression 
	 * @throws Exception Thrown if an invalid s param value is used
	 */
	public boolean isMatching(String s) throws Exception
	{
		boolean match = false;
		String value = "Heading1";  // This needs to be made a property of MatchProperties
		
		if (this.isMatchCondition())
		{
			if (type == TaskMatchType.REGEX)
			{		
				Pattern p = Pattern.compile(pattern);
				
				Matcher m = p.matcher(s);
				
				match = m.matches();				
			}
			else if (type == TaskMatchType.EXPR)
			{
				if (exprType == TaskExprType.CONTAINS)
				{
					if (s.contains(value))
						match = true;
				}
				else if (exprType == TaskExprType.STARTS_WITH)
				{
					if (s.startsWith(value))
						match = true;
				}
				
			}
			else
				throw new Exception ("Cannot apply a match operation for the Task match type specified.");			
		}
		else
			throw new Exception ("Cannot apply a matching operation with no match conditions set.");
		
		return match;
	}

	/**
	 * Get the type expression. Valid values are STARTS_WITH, CONTAINS or NOT_SET
	 * @return exprType Task Expression Type
	 */
	public TaskExprType getExprType() {
		return exprType;
	}
	
	/**
	 * Get the type expression as a string. Valid values are "starts-with", "contains" or "not_set"
	 * @return sVal Expression Type as a string
	 */
	public String getExprTypeAsString()
	{
		String sVal = null;
		
		switch (exprType)
		{
		case STARTS_WITH:
			sVal = "starts-with";
			break;
		case CONTAINS:
			sVal = "contains";
			break;
		case NOT_SET:
			break;
		default:
			break;
		}
		
		return sVal;
	}

	/**
	 * Set the expression enumerated type used in a task match. Valid values are STARTS_WITH, CONTAINS, NO_SET
	 * @param exprType Task Expression Type
	 */
	public void setExprType(TaskExprType exprType) {
		this.exprType = exprType;
	}
	
	/**
	 * Set the expression type from a string used in a task match. Valid values are STARTS_WITH, CONTAINS, NO_SET
	 * @param sExprType Expression Type as a string
	 * @throws Exception Thrown if an invalid expression type string is supplied
	 */
	public void setExprType(String sExprType) throws Exception {
		
		if (sExprType != null)
		{
			String upper = sExprType.toUpperCase();
			if (upper.equalsIgnoreCase("STARTSWITH") || upper.equals("STARTS-WITH") || upper.equals("STARTS_WITH"))
					this.exprType = TaskExprType.STARTS_WITH;
			else if (upper.equals("CONTAINS"))
				this.exprType = TaskExprType.CONTAINS;
			else
				throw new Exception ("Invalid Task expr type specified.");
		}
		else
			throw new Exception ("Invalid Task expr type specified.");
	}
	
	/**
	 * Set the expression type from the match element node attributes.
	 * This method is used by the RulesSetManager parser.
	 * @param xExprValue Expression Value Node
	 * @throws Exception Thrown if an invalid match element "expr" attribute value is specified in the rule template file
	 */
	public void setExprType(Node xExprValue) throws Exception {	
		if (xExprValue.getNodeType() == Node.ATTRIBUTE_NODE && xExprValue.getNodeName().equalsIgnoreCase("expr"))
		{
			String value = xExprValue.getNodeValue().toUpperCase();
			
			if (value.equals("STARTS-WITH"))
					this.exprType = TaskExprType.STARTS_WITH;
			else if (value.equals("CONTAINS"))
					this.exprType = TaskExprType.CONTAINS;
			else
				throw new Exception ("Invalid Task match expr specified.");
		}
		else 
			throw new Exception ("Invalid Task match expr specified.");
	}

	/**
	 * Has a match condition been set.
	 * @return matchCondition Boolean to flag indicating if a match condition is set.
	 */
	public boolean isMatchCondition() {	
		return matchCondition;
	}
	
	/**
	 * Determine if the their is a valid match condition set from match element attributes.
	 */
	public void setMatchConditionFromProperties() {
		matchCondition = false;
		
		switch (type)
		{
		case EXPR:
			if (exprType == TaskExprType.CONTAINS || exprType == TaskExprType.STARTS_WITH)
				matchCondition = true;
			break;
		case COPY:
			break;
		case NOT_SET:
			break;
		case REGEX:
			if (pattern != null && replaceWith == null)
				matchCondition = true;
			break;
		default:
			break;
		}
	}

	/**
	 * Set the matchCondition flag.
	 * @param matchCondition Boolean flag to set a match condition
	 */
	public void setMatchCondition(boolean matchCondition) {
		this.matchCondition = matchCondition;
	}

	/**
	 * Has a replace condition been set.
	 * @return replaceCondition Boolean flag to indicate if a replace condition has been set in the match element
	 */
	public boolean isReplaceCondition() {
		return replaceCondition;
	}

	/**
	 * Set the replace condition flag.
	 * @param replaceCondition Boolean flag to indicate if a replace condition has been set in the match element
	 */
	public void setReplaceCondition(boolean replaceCondition) {
		this.replaceCondition = replaceCondition;
	}
	
	/**
	 * Set the replace condition flag from the match element attributes.
	 */
	public void setReplaceConditionFromProperties() {
		replaceCondition = false;
		
		switch (type)
		{
		case EXPR:
			break;
		case COPY:
			replaceCondition = true;
			break;
		case NOT_SET:
			break;
		case REGEX:
			if (pattern != null && replaceWith != null)
				replaceCondition = true;
			break;
		default:
			break;
		}	
	}

	/**
	 * Get the attribute name used for a match or a match with a replace condition. 
	 * @return attributeName Attribute name string
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Set the attribute name used for a match or a match with a replace condition.
	 * @param attributeName - Attribute name string
	 */
	public void setAttributeName(String attributeName) {
			this.attributeName = attributeName;
	}
	
	/**
	 * Get the attribute name used for a match or a match with a replace condition from the match element node.
	 * This method is used by the RuleSetManager parser
	 * @param xAttrName Node
	 * @throws Exception Thrown if match "attr" attribute is not set
	 */
	public void setAttributeName(Node xAttrName) throws Exception {	
		if (xAttrName.getNodeType() == Node.ATTRIBUTE_NODE && xAttrName.getNodeName().equalsIgnoreCase("attr"))
		{
			this.attributeName = xAttrName.getNodeValue();
		}
		else 
			throw new Exception ("Invalid Task match attribute name specified.");
	}

	/**
	 * Get the value of the attribute used for a match or match + replace condition.
	 * @return attributeValue Attribute value for the match element "attr" attribute
	 */
	public String getAttributeValue() {
		return attributeValue;
	}

	/**
	 * Set the value of the attribute value used for a match or match + replace condition.
	 * @param attributeValue Match element attribute "attr" value
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	/**
	 * Get the attribute value for the nominated attribute used for a match or a match with a replace condition from the match element node.
	 * This method is used by the RuleSetManager parser.
	 * @param xAttrValue Match element "attr_value" attribute node
	 * @throws Exception Thrown when the "attr_value" attribute is not set
	 */
	public void setAttributeValue(Node xAttrValue) throws Exception {	
		if (xAttrValue.getNodeType() == Node.ATTRIBUTE_NODE && xAttrValue.getNodeName().equalsIgnoreCase("attr_value"))
		{
			this.attributeValue = xAttrValue.getNodeValue();
		}
		else 
			throw new Exception ("Invalid Task match attribute value specified.");
	}

	/**
	 * Get the expression value. Used when performing an expression match.
	 * @return exprValue Expression value
	 */
	public String getExprValue() {
		return exprValue;
	}

	/**
	 * Set the expression value. Used when performing an expression match.
	 * @param exprValue Expression value
	 */
	public void setExprValue(String exprValue) {
		this.exprValue = exprValue;
	}

}
