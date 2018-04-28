package com.helphero.util.hhc.rule;

/**
 * This class is used set conditional match properties for a rule of type partition for a target_path task.
 * Most of this can also be achieved using XPATH notation for the target path although the regular expression
 * capability exposes the full power java based regular expressions
 *  
 * @author jcharles
 *
 */
public class PartitionTargetProperties extends MatchProperties {
	private ITaskMatchType taskMatchType; // COPY, EXPR, REGEX
	private ITaskExprType taskExprValue; // STARTS_WITH, CONTAINS
	private String targetXPath = null;
	private String pattern;
	private String replaceWith;
	
	boolean nameMatchCondition;

	public PartitionTargetProperties() {

	}

	/**
	 * Get the enumerated Match type: COPY, EXPR, REGEX
	 * @return ITaskMatchType
	 */
	public ITaskMatchType getMatchType() {
		return taskMatchType;
	}

	/**
	 * Set the enumerated match type: COPY, EXPR, REGEX
	 * @param taskMatchType - ITaskMatchType
	 */
	public void setMatchType(ITaskMatchType taskMatchType) {
		this.taskMatchType = taskMatchType;
		
		switch ((TaskMatchType)taskMatchType)
		{
		case COPY:
			nameMatchCondition = false;
			break;
		case EXPR:
			nameMatchCondition = true;
			break;
		case REGEX:
			nameMatchCondition = true;
			break;			
		case NOT_SET:
			break;
		default:
			break;
		}
	}

	/**
	 * Get the task expression value: STARTS_WITH, CONTAINS
	 * @return taskExprValue Task Expression Type enumerated value 
	 */
	public ITaskExprType getTaskExprValue() {
		return taskExprValue;
	}

	/**
	 * Set the task expression value: STARTS_WITH, CONTAINS
	 * @param taskExprValue Task Expression Type enumerated value 
	 */
	public void setTaskExprValue(ITaskExprType taskExprValue) {
		this.taskExprValue = taskExprValue;
	}

	/**
	 * Get the target XPATH
	 * @return String
	 */
	public String getTargetXPath() {
		return targetXPath;
	}

	/**
	 * Set the target XPATH
	 * @param targetXPath - String
	 */
	public void setTargetXPath(String targetXPath) {
		this.targetXPath = targetXPath;
	}

	/**
	 * Get the matching regular expression pattern
	 * @return String
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Set the regular expression pattern
	 * 
	 * @param pattern - Match element regular expression string
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Get the replace-with value
	 * @return replaceWith replace with string value
	 */
	public String getReplaceWith() {
		return replaceWith;
	}

	/**
	 * Set the replace with value
	 * @param replaceWith Replace with string value
	 */
	public void setReplaceWith(String replaceWith) {
		this.replaceWith = replaceWith;
	}
}
