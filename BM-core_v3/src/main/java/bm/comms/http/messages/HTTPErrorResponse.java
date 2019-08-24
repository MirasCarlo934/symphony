package bm.comms.http.messages;

import java.io.InputStream;

import bm.comms.http.HTTPException;

public class HTTPErrorResponse extends HTTPResponse {
	private HTTPException exception = null;
	
//	public HTTPErrorResponse(int responseCode) {
//		super(responseCode);
//		// TODO Auto-generated constructor stub
//	}
	
	public HTTPErrorResponse(HTTPException e) {
		super(0);
		this.exception = e;
	}
	
	public HTTPErrorResponse(int responseCode, HTTPException e) {
		super(responseCode);
		this.exception = e;
	}
	
	public HTTPException getException() {
		return exception;
	}
}
