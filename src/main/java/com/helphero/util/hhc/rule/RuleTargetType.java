package com.helphero.util.hhc.rule;

/**
 * An enumerated type describing the different document types supported.
 * These types are only relevant for SupportPoint documents.
 * 
 * @author jcharles
 */
public enum RuleTargetType implements IRuleTargetType {
		PROCEDURE,
		POLICY,
		PROCESS,
		EXTERNAL,
		NOT_SET;
		
		private RuleTargetType value;

		private RuleTargetType()
		{
		}
		
		private RuleTargetType(RuleTargetType value)
		{
			this.value = value;
		}

		public RuleTargetType getTargetType() {
			return value;
		}

		public void setTargetType(RuleTargetType value) {
			this.value = value;
		}
}