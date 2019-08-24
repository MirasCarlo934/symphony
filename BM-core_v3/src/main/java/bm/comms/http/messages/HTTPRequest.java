package bm.comms.http.messages;

import java.util.HashMap;

import bm.comms.http.HTTPSender;
import bm.main.engines.requests.EngineRequest;

public abstract class HTTPRequest  {
	private int id; //assigned by Sender
	protected String url;
	protected boolean doOutput;
	protected HTTPRequestType type;
	protected HashMap<String, String> headers = null;
	protected HashMap<String, String> parameters = null;
	protected int[] validResponseCodes;

	public HTTPRequest(/*String id, HTTPEngine engine, */String url, HTTPRequestType type, boolean doOutput,
			HashMap<String, String> headers, HashMap<String, String> parameters, int[] validResponseCodes) {
//		super(id, engine);
		this.doOutput = doOutput;
		this.type = type;
		this.headers = headers;
		this.parameters = parameters;
		this.validResponseCodes = validResponseCodes;
		if(url.startsWith("http://") || url.startsWith("https://"))
			this.url = url;
		else
			this.url = "http://" + url;
	}
	
	public boolean checkResponseCodeValidity(int responseCode) {
		if(validResponseCodes ==  null || validResponseCodes.length == 0) {
			return true;
		}
		for(int i = 0; i < validResponseCodes.length; i++) {
			if(responseCode == validResponseCodes[i]) {
				return true;
			}
		}
		return false;
	}
	
	public int[] getValidResponseCodes() {
		return validResponseCodes;
	}
	
	public HTTPRequestType getRequestType() {
		return type;
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	
	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public String getURL() {
		return url;
	}
	
	public boolean doOutput() {
		return doOutput;
	}

	/**
	 * Returns the ID of this HTTPRequest; set by the HTTPSender
	 * 
	 * @return the id of this HTTPRequest
	 */
	public int getID() {
		return id;
	}

	/**
	 * Sets the ID of this HTTPRequest.
	 * <br/>
	 * <b>NOTE:</b> This method must only be invoked by the Sender
	 * 
	 * @param id the id to set
	 */
	public void setID(int id) {
		this.id = id;
	}
}
