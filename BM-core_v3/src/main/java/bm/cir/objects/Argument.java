package bm.cir.objects;

import org.apache.log4j.Logger;

import bm.tools.StringTools;

public class Argument extends CodeBlock {
	private static final Logger logger = Logger.getLogger(Argument.class);
	private ArgOperator operator;
	private Relationship relationshipWithNextArgument;

	public Argument(String comID, int comProperty, Object comValue, ArgOperator operator,
			Relationship relationshipWithNextArgument) {
		super(comID, comProperty, comValue);
		setOperator(operator);
		setRelationshipWithNextArgument(relationshipWithNextArgument);
	}

	/**
	 * @return the operator
	 */
	public ArgOperator getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(ArgOperator operator) {
		this.operator = operator;
	}

	/**
	 * @return the relationshipWithNextArgument
	 */
	public Relationship getRelationshipWithNextArgument() {
		return relationshipWithNextArgument;
	}

	/**
	 * @param relationshipWithNextArgument the relationshipWithNextArgument to set
	 */
	public void setRelationshipWithNextArgument(Relationship relationshipWithNextArgument) {
		this.relationshipWithNextArgument = relationshipWithNextArgument;
	}

	@Override
	public String toString() {
		String rel = getRelationshipWithNextArgument().toString();
		if(rel.equals("NONE")) {
			rel = "";
		}
		return StringTools.injectStrings("%s:%s %s %s %s", new String[]{getDeviceID(),
				String.valueOf(getPropertyIndex()), getOperator().getSymbol(), getPropertyValue().toString(), rel},
				"%s");
	}
}
