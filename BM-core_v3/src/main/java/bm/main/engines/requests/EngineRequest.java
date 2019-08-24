package bm.main.engines.requests;

import bm.main.engines.AbstEngine;

public abstract class EngineRequest {
	private String id; //the ID of the EngineRequest
	private AbstEngine engine;
//	private String engineType; //the Engine class string that can processRequest this EngineRequest
	private Thread requestingThread;
	private boolean waitForResponse;
	protected Object response = null; // the response of the Engine
	
	/**
	 * Creates a new <i>EngineRequest</i>.
	 * 
	 * @param id The unique ID of this <i>EngineRequest</i>, given upon instantiation of the <i>EngineRequest</i>
	 * @param engine The engine that will handle this <i>EngineRequest</i>
	 */
	public EngineRequest(String id, AbstEngine engine) {
		this.id = id;
		this.engine = engine;
	}

	/**
	 * Returns the Engine class in String format that can processRequest this EngineRequest.
	 * @return the Engine class String
	 */
//	public String getEngineType() {
//		return engineType;
//	}
	
	/**
	 * Returns the engine that will handle this <i>EngineRequest</i>
	 * 
	 * @return The <i>AbstEngine</i> object.
	 */
	public AbstEngine getEngine() {
		return engine;
	}

	/**
	 * Returns the response of the Engine after processing
	 * 
	 * @return Engine response in Object form. (Use casting)
	 */
	public Object getResponse() {
		return response;
	}
	
	/**
	 * Sets the response of the Engine after processing. 
	 * <b>Only the appropriate Engine must use this method</b>
	 * 
	 * @param response The response set by the Engine
	 */
	public void setResponse(Object response) {
		this.response = response;
	}

	/**
	 * Returns the ID of this EngineRequest
	 * 
	 * @return EngineRequest ID
	 */
	public String getSSID() {
		return id;
	}

	/**
	 * @return the requestingThread
	 */
	public Thread getRequestingThread() {
		return requestingThread;
	}

	/**
	 * @param requestingThread the requestingThread to set
	 */
	public void setRequestingThread(Thread requestingThread) {
		this.requestingThread = requestingThread;
	}

	/**
	 * @return the waitForResponse
	 */
	public boolean waitForResponse() {
		return waitForResponse;
	}

	/**
	 * @param waitForResponse the waitForResponse to set
	 */
	public void setWaitForResponse(boolean waitForResponse) {
		this.waitForResponse = waitForResponse;
	}
}
