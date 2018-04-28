package com.helphero.util.hhc.dom.processing;

/**
 * Base class defining the core identifiers for a partition.
 *  
 * @author jcharles
 *
 */
public class Partition {
	private String sId;
	private PartitionType type = PartitionType.NOT_SET;

	public Partition() {
	}

	/**
	 * Retrieve the partition id
	 *  
	 * @return sId Partition id
	 */
	public String getsId() {
		return sId;
	}

	/**
	 * 
	 * Set the partition id
	 * 
	 * @param sId Partition id
	 */
	public void setsId(String sId) {
		this.sId = sId;
	}

	/**
	 * Retrieve the partition type.
	 * 
	 * @return type Partition type
	 */
	public PartitionType getType() {
		return type;
	}

	/**
	 * Set the partition type.
	 * 
	 * @param type Partition type
	 */ 
	public void setType(PartitionType type) {
		this.type = type;
	}
}
