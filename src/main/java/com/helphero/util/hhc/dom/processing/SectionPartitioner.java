package com.helphero.util.hhc.dom.processing;

import org.apache.log4j.Logger;

import com.helphero.util.hhc.rule.TaskExprType;

/**
 * This derived class performs section specific attuned partitioning of an xHtml document. 
 * @author jcharles
 */
public class SectionPartitioner extends Partitioner {
	static Logger logger = Logger.getLogger(SectionPartitioner.class);	

	public SectionPartitioner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Initialise all the default settings for a section level partition.
	 */
	public void initialise()
	{
		String docRootXPath = "//body/div[starts-with(@class,'document')]";
		setDocumentRootXPath(docRootXPath);
		
		setParentContainerType(PartitionType.DOCUMENT);
		String pcXPath = "//body/div/div/div[starts-with(@class,'Document')]";
		setParentContainerXPath(pcXPath);
		
		setMatchingAttributeName("class");
		setMatchingAttributeValue("Heading2");
		setAttributeMatchExpression(TaskExprType.STARTS_WITH);
		
		this.setKeepFirstElements(true);
		this.setIntroPartitionTitle("Preliminaries");

		setMatchingElementXPath("//body/div/div/div/p[starts-with(@class,'Heading2')]");
		
		setPartitionType(PartitionType.SECTION);
		
		this.deriveMatchingElementXPath();
	}	
	
	@Override
	public void interpret()
	{
		// Common properties specific to this transform
		this.setPartitionType(PartitionType.SECTION);
		
		// All other properties are common to all partition types and are derived from the rules 
		super.interpret();		
	}
	
	@Override
	public void process() throws TransformException
	{
		if (this.hasPartitions() || true)
			partition();
		else
			throw new TransformException("ErrMsg: No Section level partitions found");
	}

}
