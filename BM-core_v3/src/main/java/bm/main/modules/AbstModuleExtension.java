package bm.main.modules;

import bm.comms.Protocol;
import org.apache.log4j.Logger;

import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.device.JEEPErrorResponse;
import bm.jeep.vo.device.ResError;

public abstract class AbstModuleExtension {
	protected String logDomain;
	protected Logger mainLOG;
	protected Logger errorLOG;
	private String name;
	private String[] params;

	public AbstModuleExtension(String logDomain, String errorLogDomain, String name, String[] params
			/*MQTTPublisher mp*/) {
		mainLOG = Logger.getLogger(logDomain + "." + name);
		errorLOG = Logger.getLogger(errorLogDomain + "." + name);
		this.name = name;
		if(params == null) {
			params = new String[0];
		} else {
			this.params = params;			
		}
	}
	
	public void processRequest(JEEPRequest request) {
		mainLOG.debug(name + " extended request processing started!");
		if(checkTertiaryRequestParameters(request)) {
			mainLOG.trace("Request valid! Proceeding to request processing...");
			process(request);
		} else {
			mainLOG.warn("Tertiary request params didn't check out. " + name + 
					" ModuleExtension will not be run.");
		}
	}
	
	/**
	 * Checks if the request contains all the required tertiary parameters
	 * 
	 * @param request The Request object
	 * @return <b><i>True</b></i> if the request is valid, <b><i>false</i></b> if: <br>
	 * 		<ul>
	 * 			<li>There are missing tertiary request parameters</li>
	 * 			<li>There are tertiary request parameters that are null/empty</li>
	 * 			<li>The ModuleExtension-specific request parameter check failed
	 * 			<br><i>Each ModuleExtension can have additional request checks, see individual
	 * 			modules for more details.</i></li>
	 * 		</ul>
	 */
	public boolean checkTertiaryRequestParameters(JEEPRequest request) {
		mainLOG.trace("Checking tertiary request parameters...");
		boolean b = false; //true if request is valid
		
		if(params == null || params.length == 0) //there are no secondary request params
			b = true;
		else {
			for(int i = 0; i < params.length; i++) {
				String param = params[i];
				if(request.getParameter(param) != null && !request.getParameter(param).equals("")) 
					b = true;
				else {
					error("Parameter '" + param + "' is either empty or nonexistent!", 
							request.getProtocol());
					b = false;
					break;
				}
			}
			
			if(b) { //if basic parameter checking is good
				b = additionalRequestChecking(request);
			}
		}
		
		return b;
	}
	
	protected abstract void process(JEEPRequest request);
	
	/**
	 * Used in case of additional request parameter checking. <i><b>Must always return true 
	 * if there are no additional request checking</b></i>
	 * 
	 * @param request The Request object
	 * @return <b>True</b> if Request checks out, <b>false</b> otherwise. 
	 */
	protected abstract boolean additionalRequestChecking(JEEPRequest request);
	
	protected void error(String msg, Exception e, Protocol protocol) {
		mainLOG.error(msg);
		errorLOG.error(msg, e);
//		mp.publishToErrorTopic(msg + " (" + e.getMessage() + ")");
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(msg + " ("
				+ e.getMessage() + ")", protocol));
	}
	
	protected void error(Exception e, Protocol protocol) {
		mainLOG.error(e.getMessage());
		errorLOG.error(e.getMessage(), e);
//		mp.publishToErrorTopic(e.getMessage());
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(e.getMessage(), protocol));
	}
	
	protected void error(String msg, Protocol protocol) {
		mainLOG.error(msg);
		errorLOG.error(msg);
//		mp.publishToErrorTopic(msg);
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(msg, protocol));
	}
	
	protected void error(ResError error, Exception e, Protocol protocol) {
		mainLOG.error(error.getMessage());
		errorLOG.error(error.getMessage(), e);
//		mp.publishToErrorTopic(error + " (" + e.getMessage() + ")");
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(error + " (" + e.getMessage() + ")", protocol));
	}
	
	protected void error(JEEPErrorResponse error) {
		mainLOG.error(error.getMessage());
		errorLOG.error(error.getMessage());
//		mp.publishToErrorTopic(error);
		error.getProtocol().getSender().sendErrorResponse(error);
	}
}
