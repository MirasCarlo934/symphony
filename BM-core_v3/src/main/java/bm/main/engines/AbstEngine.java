package bm.main.engines;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;

public abstract class AbstEngine implements Runnable {
	private String logDomain;
	protected Logger LOG;
	protected Logger errorLOG;
	protected String name;
	private String className;
	protected LinkedList<EngineRequest> reqQueue = new LinkedList<EngineRequest>();
	protected HashMap<String, Object> responses = new HashMap<String, Object>(10, 1);
	private Timer timer;
	private int counter = 0;

	/**
	 * The current EngineRequest being processed by this Engine. Changes every time the <i>run()</i>
	 * method is invoked.
	 */
	protected EngineRequest currentRequest = null;
	
	public AbstEngine(String logDomain, String errorLogDomain, String name, String className) {
		this.logDomain = logDomain;
		this.name = name;
		this.className = className;
		LOG = LogManager.getLogger(logDomain + "." + name);
		errorLOG = LogManager.getLogger(errorLogDomain + "." + name);
	}
	
	/**
	 * Forwards and ERQS request to the specified engine. The request will be put into a queue and the engine will processRequest
	 * the queued requests in a turn-per-turn basis. After processing, the engine will return a response for the forwarded
	 * request.
	 * 
	 * @param request The ERQS request for the specified engine to processRequest
	 * @param caller The thread of the object that calls this method
	 * @param waitForResponse <b>True</b> if engine response <b><i>must</i></b> be returned. <b>False</b> if not. 
	 * 			<b>WARNING:</b> If this is false, this method will return a null value.
	 * @return The response of the engine to the request, or <b>null</b> if <i>waitForResponse</i> is set to false.
	 * @throws EngineException if the engine encounters an error while processing the request
	 */
	public Object putRequest(EngineRequest request, Thread caller, boolean waitForResponse) 
			throws EngineException {
		request.setRequestingThread(caller);
		request.setWaitForResponse(waitForResponse);
		LOG.trace("Adding " + request.getClass().getSimpleName() + " " + request.getSSID() + "!");
		reqQueue.add(request);
		if(waitForResponse) {
			synchronized(caller){try {
				LOG.trace("Thread " + caller.getName() + " set to wait");
				caller.wait();
			} catch (InterruptedException e) {
				LOG.error("Thread " + caller.getName() + " was interrupted! Cannot processRequest " +
						request.getSSID(), e);
				throw new EngineException(this, "Thread " + caller.getName() + 
						" was interrupted!", e);
			}}
			Object o = getResponse(request.getSSID());
			if(o.getClass().equals(EngineException.class)) {
				throw (EngineException) o; 
			} else {
				return o;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves the response from the specified EngineRequest and removes it from the Engine
	 * 
	 * @param engineRequestID The ID of the EngineRequest
	 * @return the response Object
	 */
	public Object getResponse(String engineRequestID) {
		Object o = responses.remove(engineRequestID);
		LOG.trace("Returning response for request " + engineRequestID);
		return o;
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			if(!reqQueue.isEmpty()) {
				counter++;
				EngineRequest er = reqQueue.removeFirst();
				LOG.trace("Processing EngineRequest " + er.getSSID() + "...");
				
				Object res;
				if(checkEngineRequest(er)) {
					try {
						res = processRequest(er);
						LOG.trace("EngineRequest " + er.getSSID() + " processing complete!");
					} catch (EngineException e) {
					    res = e;
						LOG.error("EngineRequest " + er.getSSID() + " processing failed!", e);
					}
				} else {
					res = new EngineException(er.getEngine(), "Invalid EngineRequest for " + name);
					LOG.error("Invalid EngineRequest for " + name);
				}

				synchronized (er.getRequestingThread()) {
					if(er.waitForResponse() == true) {
						responses.put(er.getSSID(), res);
						
						er.getRequestingThread().notifyAll();
						LOG.trace("Thread " + er.getRequestingThread().getName() + " notified!");
					}
				}
			}
		}
	}
	
	/**
	 * Checks if the <i>EngineRequest</i> is valid. Returns false if <i>EngineRequest</i> is not for 
	 * this <i>Engine</i>
	 * 
	 * @param er The <i>EngineRequest</i> to be checked
	 * @return <b><i>True</b></i> if the EngineRequest checks out, <b><i>false</b></i> otherwise
	 */
	private boolean checkEngineRequest(EngineRequest er) {
		if(er.getEngine().getClass().toString().equals(className)) {
			return true;
		} else {
			return false;
		}
	}
	
	protected abstract Object processRequest(EngineRequest er) throws EngineException;
	
	public String getLogDomain() {
		return logDomain;
	}
	
	/**
	 * Returns the name of this <i>Engine</i>
	 * 
	 * @return The name
	 */
	public String getName() {
		return name;
	}
}
