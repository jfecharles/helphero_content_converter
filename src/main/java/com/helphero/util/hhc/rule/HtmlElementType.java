package com.helphero.util.hhc.rule;

/**
 * Enumeration detailing the components that can be added to the common object document inside a create transform.
 *  
 * @author jcharles
 *
 */
public enum HtmlElementType implements IElementType {
	A,			// link
	IMG,		// image
	P,			// paragraph
	SPAN,		// span
	NOT_SET;
	
	private HtmlElementType type;
	
	private HtmlElementType()
	{
	}
	
	private HtmlElementType(HtmlElementType type)
	{
		this.type = type;
	}
	
	public HtmlElementType getType() {
		return type;
	}

	public void setType(HtmlElementType type) {
		this.type = type;
	}
	
	public void setType(String type) throws Exception {	
		if (type != null)
		{
			this.type = HtmlElementType.valueOf(type.toUpperCase());
		}
		else 
			throw new Exception ("Invalid Html Element type specified.");
	}
}