package com.helphero.util.hhc.processing;

/**
 * Enumeration of the different types of document supported by this converter.
 * currently only Word document docx formats can be converted.
 * 
 * @author jcharles
 *
 */
public enum SupportedDocType {
	DOCX,
	DOTX,
	PPTX,
	XLSX,
	PDF,
	XHTML,
	XML,
	NOT_SET;
	
	private SupportedDocType type;
	
	/**
	 * Constructor
	 */
	private SupportedDocType()
	{
	}
	
	/**
	 * Set the supported document type for this enumeration instance.
	 * @param type Enumerate type SupportedDocType value
	 */
	public void setSupportedDocTypes(SupportedDocType type)
	{
		this.type = type;
	}
	
	/**
	 * Get the supported document type for this enumeration instance.
	 * @return type Enumerate type SupportedDocType value
	 */
	public SupportedDocType getSupportedDocTypes()
	{
		return this.type;
	}
}
