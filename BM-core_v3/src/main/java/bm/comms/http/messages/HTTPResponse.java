package bm.comms.http.messages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HTTPResponse {
	private int id;
	private int responseCode;
	private InputStream inputStream;
	
	public HTTPResponse(int responseCode, InputStream inputStream) {
		this.responseCode = responseCode;
		this.inputStream = inputStream;
	}
	
	public HTTPResponse(int responseCode) {
		this.responseCode = responseCode;
		this.inputStream = null;
	}
	
	/**
	 * Returns the response code of the requested server
	 * 
	 * @return the response code
	 */
	public int getResponseCode() {
		return responseCode;
	}
	
	/**
	 * Returns the response of the requested server in String format. <b><i>WARNING:</b> Once this method is invoked, 
	 * the input stream specified with this HTTPResponse will no longer be available</i>
	 * 
	 * @return the response, <i>null</i> if there is no response
	 */
	public String getResponse() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	    StringBuilder response = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }
	    reader.close();
		return response.toString();
	}
	
	/**
	 * Returns the input stream for the HTTP response of the requested server
	 * 
	 * @return the input stream, <i>null</i> if there is no response
	 */
	public InputStream getInputStream() {
		return inputStream;
	}
	


	/**
	 * Returns the ID of this HTTPResponse; set by the HTTPSender
	 * 
	 * @return the id of this HTTPResponse
	 */
	public int getID() {
		return id;
	}

	/**
	 * Sets the ID of this HTTPResponse.
	 * <br/>
	 * <b>NOTE:</b> This method must only be invoked by the Sender
	 * 
	 * @param id the id to set
	 */
	public void setID(int id) {
		this.id = id;
	}
}
