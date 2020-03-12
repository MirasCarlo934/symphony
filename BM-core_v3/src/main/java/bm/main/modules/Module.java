package bm.main.modules;

import bm.comms.Protocol;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPResponse;
import bm.main.modules.exceptions.RequestProcessingException;
import bm.main.modules.exceptions.ResponseProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.device.JEEPErrorResponse;
import bm.jeep.vo.device.ResError;
import bm.main.repositories.DeviceRepository;

/**
 * The Module object handles the JEEP requests sent by Symphony components to the BM. A Module handles these requests by 
 * doing the following:
 * 	<ol>
 * 		<li>Checking of secondary request parameters</li>
 * 		<li>Management of devices, properties, and/or rooms based on the request</li>
 * 		<li>Sending of JEEP response back to the requesting component</li>
 * 	</ol>
 * 
 * @author carlomiras
 *
 */
public abstract class Module implements Runnable {
	private int rn;
	protected String logDomain;
	protected Logger LOG;
	protected Logger errorLOG;
	protected AbstModuleExtension[] extensions = new AbstModuleExtension[0];
	protected String name;
	protected String requestType;
	protected String[] requestParams;
	private String[] responseParams;
	protected DeviceRepository dr;
	protected JEEPRequest request;
	protected JEEPResponse response;

	/**
	 * Creates a Module object with no extensions
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param name the name of this module
	 * @param RTY the JEEP request type that this module handles
	 * @param requestParams the secondary request parameters for the JEEP requests this module will handle,
	 *                      <i>can be set to null if there are no secondary request parameters</i>
	 * @param responseParams the secondary response parameters for the JEEP responses this module will handle,
	 *                       <i>can be set to null if there are no secondary response parameters</i>
	 * @param deviceRepository the DeviceRepository of this BM
	 */
	public Module(String logDomain, String errorLogDomain, String name, String RTY, String[]
			requestParams, String[] responseParams, /*MQTTPublisher mp, */DeviceRepository deviceRepository) {
		LOG = LogManager.getLogger(logDomain + "." + name);
		errorLOG = LogManager.getLogger(errorLogDomain + "." + name);
		this.logDomain = logDomain;
		this.name = name;
		if(requestParams == null) {
			this.requestParams = new String[0];
		} else {
			this.requestParams = requestParams;
		}
		if(responseParams == null) {
			this.responseParams = new String[0];
		} else {
			this.responseParams = requestParams;
		}
//		this.mp = mp;
		this.dr = deviceRepository;
		requestType = RTY;
	}
	
//	/**
//	 * Creates a Module object with extensions
//	 *
//	 * @param logDomain the log4j domain that this module will use
//	 * @param errorLogDomain the log4j domain where errors will be logged to
//	 * @param name the name of this module
//	 * @param RTY the JEEP request type that this module handles
//	 * @param params the secondary request parameters for the JEEP requests this module will handle
//	 * @param dr the DeviceRepository of this BM
//	 * @param extensions the ModuleExtensions attached to this Module
//	 */
//	public Module(String logDomain, String errorLogDomain, String name, String RTY, String[]
//			params, /*MQTTPublisher mp, */DeviceRepository dr, AbstModuleExtension[] extensions) {
//		LOG = Logger.getLogger(logDomain + "." + name);
//		errorLOG = Logger.getLogger(errorLogDomain + "." + name);
//		this.logDomain = logDomain;
//		this.name = name;
//		this.requestParams = params;
//		this.dr = dr;
//		this.extensions = extensions;
//		requestType = RTY;
//	}
	
	public void setRequest(JEEPRequest request) {
		this.request = request;
	}

	public JEEPRequest getRequest() {
	    return request;
    }

	public void setResponse(JEEPResponse response) {
		this.response = response;
	}
	
	public void run() {
		LOG.debug(name + " request processing started!");

		if(request != null) {
			try {
				if (checkSecondaryRequestParameters(request)) {
					LOG.trace("Request valid! Proceeding to request processing...");
					try {
						processRequest(request);
						for (int i = 0; i < extensions.length; i++) {
							AbstModuleExtension ext = extensions[i];
							ext.processRequest(request);
						}
						LOG.info("Request processing finished!");
					} catch(RequestProcessingException e) {
						error("Request processing failed!", e, request.getProtocol());
					}
				}
			} catch (SecondaryMessageCheckingException e){
				LOG.error("Secondary request requestParams didn't check out. See also the additional request requestParams"
						+ " checking.");
				error(e.getMessage(), request.getProtocol());
			}
		} else if(response != null) {
			try {
				if (checkSecondaryResponseParameters(response)) {
					LOG.trace("Response valid! Proceeding to response processing...");
					try {
						processResponse(response);
	//					for (int i = 0; i < extensions.length; i++) {
	//						AbstModuleExtension ext = extensions[i];
	//						ext.processResponse(request);
	//					}
						LOG.info("Response processing finished!");
					} catch(ResponseProcessingException e) {
						error("Response processing failed!", e, response.getProtocol());
					}
				}
			} catch(SecondaryMessageCheckingException e) {
				LOG.error("Secondary response requestParams didn't check out. See also the additional request requestParams"
						+ " checking.");
				error(e.getMessage(), request.getProtocol());
			}
		}
	}
	
	/**
	 * Forwards the supplied EngineRequest to the specified Engine. Handles the thread waiting
	 * procedure and error handling for the Engine response.
	 * 
	 * @param engine The Engine where the EngineRequest will be sent to
	 * @param engineRequest The EngineRequest that will be processed by the Engine
	 * @return The Engine response object. Returns ResError object if the Engine encountered
	 * 		an error during EngineRequest processing.
	 */
//	protected Object forwardEngineRequest(AbstEngine engine, EngineRequest engineRequest) {
//		engine.processJEEPMessage(engineRequest, Thread.currentThread());
//		try {
//			synchronized (Thread.currentThread()){Thread.currentThread().wait();}
//		} catch (InterruptedException e) {
//			LOG.error("Cannot stop thread!", e);
//			e.printStackTrace();
//		}
//		Object o = engine.getResponse(engineRequest.getId());
//		if(o.getClass().equals(EngineException.class)) {
//			EngineException error = (EngineException) o;
//			error(error);
//			return error;
//		}
//		else {
//			return o;
//		}
//	}
	
	/**
	 * Checks if the request contains all the required secondary parameters
	 * 
	 * @param request The Request object
	 * @return <b><i>True</b></i> if the request is valid. A request can only be valid if: <br>
	 * 		<ul>
	 * 			<li>There are no missing secondary request parameters</li>
	 * 			<li>There are no secondary request parameters that are null/empty</li>
	 * 			<li>The module-specific request parameter check succeeded
	 * 			<br><i>Each module can have additional request checks, see individual
	 * 			modules for more details.</i></li>
	 * 		</ul>
	 * @throws SecondaryMessageCheckingException if the request checking failed
	 */
	protected boolean checkSecondaryRequestParameters(JEEPRequest request)
			throws SecondaryMessageCheckingException {
		LOG.trace("Checking secondary request parameters...");
		boolean b = false; //true if request is valid
		
		if(requestParams == null || requestParams.length == 0) //there are no secondary request requestParams
			b = true;
		else {
			for(int i = 0; i < getRequestParams().length; i++) {
				String param = getRequestParams()[i];
				if(request.getParameter(param) != null && !request.getParameter(param).equals("")) 
					b = true;
				else {
					throw new SecondaryMessageCheckingException("Parameter '" + param
							+ "' is either empty or nonexistent!");
//					error("Parameter '" + param + "' is either empty or nonexistent!",
//							request.getProtocol());
//					b = false;
//					break;
				}
			}
			
			if(b) { //if basic parameter checking is good
				b = additionalRequestChecking(request);
			}
		}
		
		return b;
	}

	/**
	 * Checks if the response contains all the required secondary parameters
	 *
	 * @param response The Request object
	 * @return <b><i>True</b></i> if the request is valid, <b><i>false</i></b> if: <br>
	 * 		<ul>
	 * 			<li>There are missing secondary response parameters</li>
	 * 			<li>There are secondary response parameters that are null/empty</li>
	 * 			<li>The module-specific response parameter check failed
	 * 			<br><i>Each module can have additional response checks, see individual
	 * 			modules for more details.</i></li>
	 * 		</ul>
	 */
	protected boolean checkSecondaryResponseParameters(JEEPResponse response)
			throws SecondaryMessageCheckingException {
		LOG.trace("Checking secondary request parameters...");
		boolean b = false; //true if request is valid

		if(responseParams == null || responseParams.length == 0) //there are no secondary request requestParams
			b = true;
		else {
			for(String param : responseParams) {
				if(response.getParameter(param) != null && !response.getParameter(param).equals(""))
					b = true;
				else {
					throw new SecondaryMessageCheckingException("Parameter '" + param
							+ "' is either empty or nonexistent!");
//					error("Parameter '" + param + "' is either empty or nonexistent!",
//							response.getProtocol());
//					b = false;
//					break;
				}
			}

			if(b) { //if basic parameter checking is good
				b = additionalResponseChecking(response);
			}
		}

		return b;
	}

	/**
	 * Processes the request set to this Module
	 * @param request The JEEPRequest object
	 * @throws RequestProcessingException if request was not processed successfully
	 */
	protected abstract void processRequest(JEEPRequest request) throws RequestProcessingException;

	/**
	 * Processes the response associated with this Module.
	 * @param response The JEEPResponse object
	 * @throws ResponseProcessingException if response was not processed successfully
	 */
	protected abstract void processResponse(JEEPResponse response) throws ResponseProcessingException;

	/**
	 * A fall-back method called when a request sent by Maestro that is
	 * associated with this <i>Module</i> is not responded to by the actual device in the Environment.
	 * @param request The JEEPRequest sent by Maestro
	 */
	public abstract void processNonResponse(JEEPRequest request);
	
	/**
	 * Used in case of additional request parameter checking. <i>Must always return
	 * <b>true</b> if there are no additional request checking</i>
	 * 
	 * @param request The JEEPRequest object to be checked
	 * @return <b>True</b> if JEEPRequest checking is successful
	 * @throws SecondaryMessageCheckingException if the JEEPRequest checking fails
	 */
	protected abstract boolean additionalRequestChecking(JEEPRequest request)
			throws SecondaryMessageCheckingException;

	/**
	 * Used in case of additional response parameter checking. <i>Must always return
	 * <b>true</b> if there are no additional response checking</i>
	 *
	 * @param response The JEEPResponse object to be checked
	 * @return <b>True</b> if JEEPResponse checks out, <b>false</b> otherwise.
	 */
	protected abstract boolean additionalResponseChecking(JEEPResponse response)
			throws SecondaryMessageCheckingException;
	
	protected void error(String msg, Exception e, Protocol protocol) {
		LOG.error(msg);
		errorLOG.error(msg, e);
		protocol.getSender().sendErrorResponse(new JEEPErrorResponse(msg
                + " (" + e.getMessage() + ")", protocol));
	}
	
	protected void error(Exception e, Protocol protocol) {
		LOG.error(e.getMessage());
		errorLOG.error(e.getMessage(), e);
        protocol.getSender().sendErrorResponse(new JEEPErrorResponse(e.getMessage(), protocol));
	}
	
	protected void error(String msg, Protocol protocol) {
		LOG.error(msg);
		errorLOG.error(msg);
        protocol.getSender().sendErrorResponse(new JEEPErrorResponse(msg, protocol));
	}
	
	protected void error(ResError error, Exception e, Protocol protocol) {
		LOG.error(error.getMessage());
		errorLOG.error(error.getMessage(), e);
        protocol.getSender().sendErrorResponse(new JEEPErrorResponse(error + " ("
                + e.getMessage() + ")", protocol));
	}
	
	protected void error(JEEPErrorResponse error) {
		LOG.error(error.getMessage());
		errorLOG.error(error.getMessage());
		error.getProtocol().getSender().sendErrorResponse(error);
	}

	/**
	 * @return the requestParams
	 */
	public String[] getRequestParams() {
		return requestParams;
	}

	public String[] getResponseParams() {
		return responseParams;
	}
	
	/**
	 * Returns the name of this module.
	 * 
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	public void setReferenceNumber(int rn) {
		this.rn = rn;
	}

	public int getReferenceNumber() {
		return rn;
	}
}
