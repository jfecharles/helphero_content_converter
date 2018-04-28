package com.helphero.util.hhc.processing.postprocessors;

public class HelpHeroImageInfo {
	private String id;
	private String organisation;
	private String srcRef;
	private String fileRef;
	private String width;
	private String height;
	private String data;
	

	public HelpHeroImageInfo() {
	}


	public String getSrcRef() {
		return srcRef;
	}


	public void setSrcRef(String srcRef) {
		this.srcRef = srcRef;
	}


	public String getFileRef() {
		return fileRef;
	}


	public void setFileRef(String fileRef) {
		this.fileRef = fileRef;
	}


	public String getWidth() {
		return width;
	}


	public void setWidth(String width) {
		this.width = width;
	}


	public String getHeight() {
		return height;
	}


	public void setHeight(String height) {
		this.height = height;
	}


	public String getData() {
		return data;
	}


	public void setData(String data) {
		this.data = data;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getOrganisation() {
		return organisation;
	}


	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

}
