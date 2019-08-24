package bm.cir.objects;

import org.apache.log4j.Logger;

import bm.tools.StringTools;
import bm.tools.StringTools.StringInjectionException;

public class CIRStatementBuilder {
	private static final Logger logger = Logger.getLogger(CIRStatementBuilder.class);
	
	/**
	 * Builds a basic CIR statement from the given Strings. <br><br>
	 * 
	 * <b>Only creates a CIR statement with 1 Argument and 1 Execution Block<b>
	 * 
	 * @param com1 SSID of the component in the Argument
	 * @param com1_pname B_Property name of the component in the Argument
	 * @param com1_pval B_Property value of the component in the Argument
	 * @param com2 SSID of the component in the Execution Block
	 * @param com2_pname B_Property name of the component in the Execution Block
	 * @param com2_pval B_Property value of the component in the Execution Block
	 * @return
	 */
	public String buildStatement(String com1, String com1_pname, String com1_pval, 
			String com2, String com2_pname, String com2_pval) {
		String template = "IF %s.%s=%s THEN %s.%s=%s";
		String str = null;
		str = StringTools.injectStrings(template, new String[]{com1, com1_pname, com1_pval, com2, com2_pname,
				com2_pval}, "%s");
		
		return str;
	}
}
