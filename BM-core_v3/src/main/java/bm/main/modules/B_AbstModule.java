package bm.main.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bm.jeep.vo.JEEPRequest;
import bm.main.repositories.DeviceRepository;

public abstract class B_AbstModule implements Runnable {
	protected String logDomain;
	protected Logger mainLOG;
	protected Logger errorLOG;
	protected AbstModuleExtension[] extensions = new AbstModuleExtension[0];
	protected String name;
	protected String requestType;
	protected String[] params;
	protected DeviceRepository dr;
	
	/**
	 * Creates a Module object with no extensions
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param name the name of this module
	 * @param RTY the JEEP request type that this module handles
	 * @param params the secondary request parameters for the JEEP requests this module will handle
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param dr the DeviceRepository of this BM
	 */
	public B_AbstModule(String logDomain, String errorLogDomain, String name, String RTY, String[] 
			params, /*MQTTPublisher mp, */DeviceRepository dr) {
		mainLOG = LogManager.getLogger(logDomain + "." + name);
		errorLOG = LogManager.getLogger(errorLogDomain + "." + name);
		this.logDomain = logDomain;
		this.name = name;
		if(params == null) {
			params = new String[0];
		} else {
			this.params = params;			
		}
//		this.mp = mp;
		this.dr = dr;
		requestType = RTY;
	}
	
	/**
	 * Creates a Module object with extensions
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param name the name of this module
	 * @param RTY the JEEP request type that this module handles
	 * @param params the secondary request parameters for the JEEP requests this module will handle
	 * @param mp the MQTTPublisher that will publish the JEEP responses
	 * @param dr the DeviceRepository of this BM
	 * @param extensions the ModuleExtensions attached to this Module
	 */
	public B_AbstModule(String logDomain, String errorLogDomain, String name, String RTY, String[] 
			params, /*MQTTPublisher mp, */DeviceRepository dr, AbstModuleExtension[] extensions) {
		mainLOG = LogManager.getLogger(logDomain + "." + name);
		errorLOG = LogManager.getLogger(errorLogDomain + "." + name);
		this.logDomain = logDomain;
		this.name = name;
		this.params = params;
		this.dr = dr;
		this.extensions = extensions;
		requestType = RTY;
	}
	
	protected abstract boolean checkSecondaryRequestParameters(JEEPRequest message);
}
