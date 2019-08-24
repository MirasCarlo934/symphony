package bm.comms.http;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.net.ssl.*;

import bm.comms.ResponseManager;
import bm.comms.Sender;
import bm.comms.http.messages.HTTPErrorResponse;
import bm.comms.http.messages.HTTPRequest;
import bm.comms.http.messages.HTTPRequestType;
import bm.comms.http.messages.HTTPResponse;
import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.device.JEEPErrorResponse;

public class HTTPSender extends Sender {
	private LinkedList<HTTPRequest> queue = new LinkedList<HTTPRequest>();
	private LinkedList<HTTPResponse> responses= new LinkedList<HTTPResponse>();
	private int requestCounter = 0;

	public HTTPSender(String name, String logDomain, ResponseManager responseManager) {
		super(logDomain, name, responseManager);
		LOG.info(name + " started!");
	}

	/**
	 * 
	 * @param httpReq
	 * @param waitForResponse
	 * @return The HTTPResponse, <i>null</i> if <b>waitForResponse</b> is false
	 */
	public HTTPResponse sendHTTPRequest(HTTPRequest httpReq, boolean waitForResponse) throws HTTPException {
		httpReq.setID(requestCounter);
		queue.add(httpReq);
		requestCounter++;
		if(waitForResponse) {
			while(true) {
				for(int i = 0; i < responses.size(); i++) {
					HTTPResponse res = responses.get(i);
					if(res.getID() == httpReq.getID()) {
						responses.remove(i);
						if(res.getClass().equals(HTTPErrorResponse.class)) {
							HTTPErrorResponse error = (HTTPErrorResponse) res;
							throw error.getException();
						}
						return res;
					}
				}
			}
		} else {
			return null;
		}
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			if(!queue.isEmpty()) {
				HTTPRequest httpReq = queue.poll();
				try {
					LOG.trace("Sending HTTPRequest...");
					HTTPResponse response = sendHTTPRequest(httpReq.getURL(), httpReq.getRequestType(), 
							httpReq.getHeaders(), httpReq.getParameters(), httpReq.doOutput());
					int i = response.getResponseCode();
					if(httpReq.checkResponseCodeValidity(i)) {
						response.setID(httpReq.getID());
						responses.add(response);
						LOG.trace("HTTPRequest send successful!");
					} else {
						HTTPErrorResponse res = new HTTPErrorResponse(
								new HTTPException("Server responded with error " + i));
						res.setID(httpReq.getID());
						responses.add(res);
					}
				} catch (IOException e) {
					HTTPErrorResponse res = new HTTPErrorResponse(
							new HTTPException(e));
					res.setID(httpReq.getID());
					responses.add(res);
				}
			} else {
				requestCounter = 0;
			}
		}
	}
	
	private HTTPResponse sendHTTPRequest(String url, HTTPRequestType method, HashMap<String, String> headers, 
			HashMap<String, String> parameters, boolean doOutput) 
			throws IOException {
		URLConnection conn = null;
		String params = "";
		try {
			conn = establishConnection(url, doOutput);
			if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
				((HttpURLConnection) conn).setRequestMethod(method.toString());
			//LATER HTTPEngine: The arguments below are crude. Fix if fixable.
			} else if(conn.getClass().getSimpleName().equals(HttpsURLConnection.class.getSimpleName() + "Impl")) {
				((HttpsURLConnection) conn).setRequestMethod(method.toString());
			} else {
				throw new IOException("Invalid protocol!");
			}
		} catch (MalformedURLException e) {
			LOG.error("Malformed URL!");
			throw e;
		} catch (IOException e) {
			LOG.error("Cannot open connection to specified URL!");
			throw e;
		}
		
		LOG.trace("Conducting a " + method.toString() + " request to " + conn.getURL());
		
		//building headers part
		if(headers != null) {
			for(int i = 0; i < headers.size(); i++) {
				String key = headers.keySet().toArray()[i].toString();
				String value = headers.get(key);
				LOG.trace("Setting header '" + key + "' to '" + value + "'");
				conn.setRequestProperty(key, value);
			}
		}
		
		//building parameters part
		if(doOutput) {
			if(parameters != null) {
				for(int i = 0; parameters != null && i < parameters.size(); i++) {
					String key = parameters.keySet().toArray()[i].toString();
					String value = parameters.get(key);
					if(key.equals("null") || key == null) {
						params += value;
					} else {
						params += key + "=" + value;
					}
				}
				LOG.trace("Sending parameters '" + params + "'");
				DataOutputStream connOut = new DataOutputStream(conn.getOutputStream());
				connOut.writeBytes(params);
				connOut.flush();
				connOut.close();
			}
		}
		
		//Get Response  
		HTTPResponse res = null;
		try {
		    if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
		    		res = new HTTPResponse(((HttpURLConnection) conn).getResponseCode(), conn.getInputStream());
			} else {
				res = new HTTPResponse(((HttpsURLConnection) conn).getResponseCode(), conn.getInputStream());
			} 
		} catch(FileNotFoundException e) { //if there is no response
			if(conn.getClass().getSimpleName().equals(HttpURLConnection.class.getSimpleName())) {
		    		res = new HTTPResponse(((HttpURLConnection) conn).getResponseCode());
			} else {
				res = new HTTPResponse(((HttpsURLConnection) conn).getResponseCode());
			} 
		}
		return res;
	}
	
	/**
	 * Establishes an HTTP connection with the specified URL
	 * 
	 * @param url The URL where the connection will take place
	 * @return An <i>HttpURLConnection</i>
	 * @throws IOException if connection is not possible
	 * @throws MalformedURLException if URL is malformed
	 */
	private URLConnection establishConnection(String url, boolean doOutput) throws IOException, MalformedURLException {
		URL urlObj = new URL(url);
		URLConnection conn;
		LOG.trace("Establishing connection at " + url);
		conn = urlObj.openConnection();
		conn.setDoOutput(doOutput);
		return conn;
	}

	@Override
	public void sendJEEPMessage(JEEPMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendErrorResponse(JEEPErrorResponse error) {
		// TODO Auto-generated method stub
		
	}
}
