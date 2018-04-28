package com.helphero.util.hhc.processing;

public enum SupportedOutputDocType {
	SPSV1,
	DOCX,
	PDF,
	XHTML,
	XML,
	HHV1,
	NOT_SET;
	
	private SupportedOutputDocType type;
	
	/**
	 * Constructor
	 */
	private SupportedOutputDocType()
	{
	}
	
	/**
	 * Set the supported output document type for this enumeration instance.
	 * @param type Enumerate type SupportedOutputDocType value
	 */
	public void setSupportedOutputDocTypes(SupportedOutputDocType type)
	{
		this.type = type;
	}
	
	/**
	 * Get the supported document type for this enumeration instance.
	 * @return type Enumerate type SupportedOutputDocType value
	 */
	public SupportedOutputDocType getSupportedOutputDocTypes()
	{
		return this.type;
	}
}
