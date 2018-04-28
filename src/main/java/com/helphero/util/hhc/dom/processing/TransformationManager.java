package com.helphero.util.hhc.dom.processing;

import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.helphero.util.hhc.dom.processing.transforms.CopyAdjacentTransformer;
import com.helphero.util.hhc.dom.processing.transforms.CopyTransformer;
import com.helphero.util.hhc.dom.processing.transforms.CreateTransformer;
import com.helphero.util.hhc.dom.processing.transforms.DeleteTransformer;
import com.helphero.util.hhc.dom.processing.transforms.MoveTransformer;
import com.helphero.util.hhc.rule.IRule;
import com.helphero.util.hhc.rule.ITask;
import com.helphero.util.hhc.rule.ITaskType;
import com.helphero.util.hhc.rule.Rule;
import com.helphero.util.hhc.rule.RuleSet;
import com.helphero.util.hhc.rule.RuleSubType;
import com.helphero.util.hhc.rule.RuleTarget;
import com.helphero.util.hhc.rule.RuleTargetType;
import com.helphero.util.hhc.rule.RuleType;
import com.helphero.util.hhc.rule.TaskType;

/**
 * @author jcharles
 * 
 * This class processes all the transforms defined in the RuleSet (originally loaded from the DSL rules.xml file) to be applied to the Input DOM Document.
 *  
 */
public class TransformationManager {
	static Logger logger = Logger.getLogger(TransformationManager.class);
	private RuleSet ruleSet = null;
	private Document inputDom = null;
	private boolean debug = false;
	private Partition topPartition = null;
	private int lastFolderId = 1;
	private int firstFolderId = -1;
	private int firstDocumentId = -1;
	private PartitionType topPartitionType = PartitionType.NOT_SET;

	public TransformationManager() {
	}
	
	public TransformationManager(RuleSet ruleSet, Document inputDom) {
		setRuleSet(ruleSet);
		setInputDom(inputDom);
		if (ruleSet.isDebug()) debug = true;
	}
	
	/**
	 * Apply all the rules in the rules set to the input document.
	 * @throws Exception Thrown if an error occurs during the execution of rules in the rule set
	 */
	public void process() throws Exception
	{
		// Partitioning MUST be performed first before any other rules are processed. In addition partitioning MUST be processed in an explicit order.
		// The partitioning order is: FOLDER, DOCUMENT, SECTION, TASK
		HashMap<String, IRule> rulesMap = ruleSet.getRulesMap();
		String parentBaseXPath = ruleSet.getDocumentRootXPath();
		parentBaseXPath = parentBaseXPath.contains("[") ? parentBaseXPath.substring(0, parentBaseXPath.indexOf("[")) : parentBaseXPath;
		
		logger.info("=== TransformationManager: process: parentBaseXPath="+parentBaseXPath);
		
		boolean hasFolderPartition = false;
		boolean hasDocumentPartition = false;
		boolean hasSectionPartition = false;
		boolean hasTaskPartition = false;
		
		// Process Folder Partitions. There can only be one of these rules set.
		String key = RuleType.PARTITION.name() + "_" + RuleTarget.FOLDER.name();
		if (rulesMap.containsKey(key))
		{
			hasFolderPartition = true;
			Rule folderRule = (Rule) rulesMap.get(key);
			if (!folderRule.isDisable())
				this.processPartition(folderRule, parentBaseXPath, hasFolderPartition, hasDocumentPartition, hasSectionPartition, hasTaskPartition);
					
			parentBaseXPath += "/div";
		}
		
		// Process Document Partitions. There can only be one of these rules set.
		key = RuleType.PARTITION.name() + "_" + RuleTarget.DOCUMENT.name();
		if (rulesMap.containsKey(key))
		{
			hasDocumentPartition = true;
			
			Rule documentRule = (Rule) rulesMap.get(key);
			if (!documentRule.isDisable())
				this.processPartition(documentRule, parentBaseXPath, hasFolderPartition, hasDocumentPartition, hasSectionPartition, hasTaskPartition);
			parentBaseXPath += "/div";
		}

		// Process Section Partitions. There can only be one of these rules set.
		key = RuleType.PARTITION.name() + "_" + RuleTarget.SECTION.name();
		if (rulesMap.containsKey(key))
		{
			hasSectionPartition = true;
			Rule sectionRule = (Rule) rulesMap.get(key);
			if (!sectionRule.isDisable())
				this.processPartition(sectionRule, parentBaseXPath, hasFolderPartition, hasDocumentPartition, hasSectionPartition, hasTaskPartition);
			parentBaseXPath += "/div";
		}
		
		// Process Task Partitions. There can only be one of these rules set.
		key = RuleType.PARTITION.name() + "_" + RuleTarget.TASK.name();
		if (rulesMap.containsKey(key))
		{
			hasTaskPartition = true;
			Rule taskRule = (Rule) rulesMap.get(key);
			if (!taskRule.isDisable())
				this.processPartition(taskRule, parentBaseXPath, hasFolderPartition, hasDocumentPartition, hasSectionPartition, hasTaskPartition);
		}
		
		// Process everything else
		for (IRule rule : ruleSet.getRules())
		{
			if (!rule.isDisable()) {
				RuleType type = (RuleType) rule.getType();
				RuleTarget target = (RuleTarget)rule.getTarget();
				String id = rule.getId();
				
				if (type != RuleType.PARTITION && type != RuleType.NOT_SET)
				{
					this.processRule(rule);
				}			
			}
		}						
	}
	
	/*
	 * This method processes each of the non-partitioning rules in the supplied rules xml file.
	 * @param IRule
	 */
	private void processRule(IRule rule) throws TransformException
	{	
		Transformer transformer = null;
		
		logger.info(">>> Processing Rule : "+(RuleType) ((Rule)rule).getType());
		
		switch ((RuleType) ((Rule)rule).getType()) {
		case CREATE:
			transformer = new CreateTransformer();
			break;
		case INSERT:
			break;			
		case REPLACE:
			break;	
		case DELETE:
			transformer = new DeleteTransformer();
			break;
		case MOVE:
			transformer = new MoveTransformer();
			break;			
		case COPY:
			if ((RuleSubType) ((Rule)rule).getSubType() == RuleSubType.ADJACENT)
				transformer = new CopyAdjacentTransformer();
			else
				transformer = new CopyTransformer();
			break;
		}
		
		if (transformer != null)
		{
			transformer.setDocument(this.getInputDom());
			transformer.setRule(rule);
			transformer.setDebug(debug);
			
			transformer.interpret();
			
			transformer.process();
		}
	}
	
	/**
	 * Process a partitioning rule.
	 * 
	 * @param rule Rule 
	 * @param baseXPath Base document XPath
	 * @param hasFolder Flag to indicate if the partition has a folder
	 * @param hasDocument Flag to indicate if the partition has a document
	 * @param hasSection Flag to indicate if the partition has a section
	 * @param hasTask Flag to indicate if the partition has a task
	 * @throws TransformException Exception thrown if an error occurs processing partition
	 */
	private void processPartition(IRule rule, String baseXPath, boolean hasFolder, boolean hasDocument, boolean hasSection, boolean hasTask) throws TransformException
	{
		RuleTarget target = (RuleTarget) rule.getTarget();
		
		HashMap<ITaskType,ITask> taskMap = rule.getTaskMap();
		ITask task = taskMap.get(TaskType.PARTITION_OPTIONS);
		
		Partitioner partitioner = null;
		
		switch (target)
		{
		case FOLDER:
	        partitioner = new FolderPartitioner();
	        partitioner.setParentContainerType(PartitionType.ROOT);
	        partitioner.setParentContainerXPath(rule.getDocumentRootXPath());
	        partitioner.setKeepFirstElements(false);
	        this.setTopPartition(partitioner.getFirstPartition());
			break;
		case DOCUMENT:
	        partitioner = new DocumentPartitioner();
	        partitioner.setTargetType((RuleTargetType)rule.getTargetType());
			if (hasFolder && this.getFirstFolderId() > -1)
			{
				partitioner.setParentContainerType(PartitionType.FOLDER);
				partitioner.setParentContainerXPath(baseXPath+"[starts-with(@class,'Folder')]");
				// partitioner.setKeepFirstElements(true);
				partitioner.setKeepFirstElements(task.getKeepIntroNodes());
				// Ensure document ids start count from 1 + the last folder id.
		        partitioner.setLastFolderId(this.getLastFolderId());
			}
			else
			{
		        partitioner.setParentContainerType(PartitionType.ROOT);
		        partitioner.setParentContainerXPath(rule.getDocumentRootXPath());
		        partitioner.setKeepFirstElements(false);
		        this.setTopPartition(partitioner.getFirstPartition());
			}
			break;
		case SECTION:
	        partitioner = new SectionPartitioner();
			if (hasDocument)
			{
				partitioner.setParentContainerType(PartitionType.DOCUMENT);
				partitioner.setParentContainerXPath(baseXPath+"[starts-with(@class,'Document')]");	
				// partitioner.setKeepFirstElements(true);
				partitioner.setKeepFirstElements(task.getKeepIntroNodes());
			}
			else
			{
		        partitioner.setParentContainerType(PartitionType.ROOT);
		        partitioner.setParentContainerXPath(rule.getDocumentRootXPath());
		        partitioner.setKeepFirstElements(false);
			}
			break;
		case TASK: 
			if (hasSection)
			{
		        partitioner = new TaskPartitioner();
				partitioner.setParentContainerType(PartitionType.SECTION);
				partitioner.setParentContainerXPath(baseXPath+"[starts-with(@class,'Section')]");	
				// partitioner.setKeepFirstElements(true);
				partitioner.setKeepFirstElements(task.getKeepIntroNodes());
			}
			// If there is no parent section container don't bother to partition
			break;			
		default:
			break;
		}
		
		if (partitioner != null)
		{
			partitioner.setDocument(inputDom);
			partitioner.setRule(rule);

			// For now
			partitioner.setDebug(debug);
			partitioner.interpret();
			try {
				partitioner.process();
				
				if (target == RuleTarget.FOLDER)
				{
			        this.setTopPartition(partitioner.getFirstPartition());
			        this.setLastFolderId(partitioner.getLastFolderId());
			        this.setFirstFolderId(partitioner.getFirstPartitionId());
			        this.setTopPartitionType(PartitionType.FOLDER);
				}
				else if (target == RuleTarget.DOCUMENT && (!hasFolder || this.getFirstFolderId() == -1))
				{
					// Set the top partition details to be document based if 
					//	1. There is no folder partition rule 
					//	2. There are no folder partitions matching the folder partition rule
			        this.setTopPartition(partitioner.getFirstPartition());
			        logger.info(">>>>>>>>>> Done Document Partitioning: Top Partition Type="+this.getTopPartition().getType().name()+" : Top Partition Id="+partitioner.getFirstPartitionId());
			        this.setFirstDocumentId(partitioner.getFirstPartitionId());
			        this.setTopPartitionType(PartitionType.DOCUMENT);
				}				
			} catch (TransformException e) {
				throw new TransformException(e.getMessage());
			}				
		}
	}

	/**
	 * Getter to retrieve the underlying rule set. 
	 * @return RuleSet
	 */
	public RuleSet getRuleSet() {
		return ruleSet;
	}

	/**
	 * Setter to set the underlying rule set. 
	 * @param ruleSet RuleSet instance
	 */
	public void setRuleSet(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	/**
	 * Getter to retrieve the input DOM
	 * @return Document
	 */
	public Document getInputDom() {
		return inputDom;
	}

	/**
	 * Setter to set the input DOM
	 * @param inputDom Input Document DOM
	 */
	public void setInputDom(Document inputDom) {
		this.inputDom = inputDom;
	}

	/**
	 * Is internal debugging enabled.
	 * @return boolean
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Set internal debugging.
	 * @param debug boolean debug flag
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * The following are convenience methods to drive the underlying transforms.
	 * Get the top partition.
	 * @return topPartition Top Partition instance
	 */
	public Partition getTopPartition() {
		return topPartition;
	}

	/**
	 * Set the top partition.
	 * @param topPartition Top Partition instance
	 */
	public void setTopPartition(Partition topPartition) {
		this.topPartition = topPartition;
	}

	/**
	 * Get the last folder id
	 * @return lastFolderId Last Folder Id - int
	 */
	public int getLastFolderId() {
		return lastFolderId;
	}

	/**
	 * Set the last folder id
	 * @param lastFolderId Last Folder id - int
	 */
	public void setLastFolderId(int lastFolderId) {
		this.lastFolderId = lastFolderId;
	}

	/**
	 * Get the first folder id
	 * @return firstFolderId First Folder Id
	 */
	public int getFirstFolderId() {
		return firstFolderId;
	}

	/**
	 * Set the first folder id
	 * @param firstFolderId First Folder id
	 */
	public void setFirstFolderId(int firstFolderId) {
		this.firstFolderId = firstFolderId;
	}

	/**
	 * Get the first document id
	 * @return firstDocumentId First Document Id
	 */
	public int getFirstDocumentId() {
		return firstDocumentId;
	}

	/**
	 * Set the first document id
	 * @param firstDocumentId First Document Id
	 */
	public void setFirstDocumentId(int firstDocumentId) {
		this.firstDocumentId = firstDocumentId;
	}

	/**
	 * Get the top partition type
	 * @return topPartitionType Top PartitionType
	 */
	public PartitionType getTopPartitionType() {
		return topPartitionType;
	}

	/**
	 * Set the top partition type
	 * @param topPartitionType Top Partition Type
	 */
	public void setTopPartitionType(PartitionType topPartitionType) {
		this.topPartitionType = topPartitionType;
	}
	
	/**
	 * Get the top partition id
	 * @return int Top Partition id
	 */
	public int getTopPartitionId()
	{
		return this.getTopPartitionType() == PartitionType.FOLDER ? this.getFirstFolderId() : this.getFirstDocumentId();
	}
}
