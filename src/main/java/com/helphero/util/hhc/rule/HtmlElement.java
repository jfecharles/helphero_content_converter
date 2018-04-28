package com.helphero.util.hhc.rule;

/**
 * Class to manage the details of an Html element to added as part of a create transform
 * to the common object model document.
 * 
 * @author jcharles
 */
public class HtmlElement implements IElement {
	
	private IElementType type;
	private String classValue;
	private String styleValue;
	private String value;
	private String src;
	private String id;
	private String parentId;

	public HtmlElement() {
	}

	/**
	 * Get the element enum type
	 * @return IElementType
	 */
	public IElementType getType() {
		return type;
	}

	/**
	 * Set the element IElementType enum type
	 * @param type IElementType implementation instance
	 */
	public void setType(IElementType type) {
		this.type = type;
	}

	/**
	 * Get the element class attribute value
	 * @return String
	 */
	public String getClassValue() {
		return classValue;
	}

	/**
	 * Set the element class attribute value
	 * @param classValue Html element class attribute value 
	 */
	public void setClassValue(String classValue) {
		this.classValue = classValue;
	}

	/**
	 * Get the element style attribute value
	 * @return String Html element class attribute value 
	 */
	public String getStyleValue() {
		return styleValue;
	}

	/**
	 * Set the element style attribute value
	 * @param styleValue Html Element style attribute string value
	 */
	public void setStyleValue(String styleValue) {
		this.styleValue = styleValue;
	}

	/**
	 * Get the element content value
	 * @return value Html Element style attribute string value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the element content value
	 * @param value Html element value 
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Get the element src attribute value
	 * @return src Html element src attribute string value
	 */
	public String getSrc() {
		return src;
	}

	/**
	 * Set the element src attribute value
	 * @param src Html element src attribute string value
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * Get the element id attribute value
	 * @return id Html element id attribute string value
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the element attribute id value
	 * @param id Html element id attribute string value
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the element parentid attribute value
	 * @return parentId Html element parentid attribute string value
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * Set the element parentid attribute value
	 * @param parentId Html element parentid attribute string value
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
}