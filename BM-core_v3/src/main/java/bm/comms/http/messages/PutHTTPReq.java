package bm.comms.http.messages;

import java.util.HashMap;

import bm.comms.http.HTTPSender;

/**
 * The HTTPEngineRequest representation of an HTTP PUT request that the HTTPEngine will send to the specified URL.
 * 
 * @author carlomiras
 *
 */
public class PutHTTPReq extends HTTPRequest {
	
	/**
	 * Creates an HTTPEngine request that the HTTPEngine uses to create an HTTP PUT request that will be sent to the
	 * specified URL.
	 * 
	 * @param id the ID of this engine request
	 * @param url the URL where the PUT request will be sent to
	 * @param headers the headers of this PUT request
	 * @param parameters the parameters of this PUT request
	 * @param validResponseCodes the response codes that are valid for this request. Responses with codes that are not 
	 * 		specified here will be considered as <i>bad</i> responses, and the HTTPEngine will throw an exception
	 */
	public PutHTTPReq(/*String id, HTTPEngine engine, */String url, HashMap<String, String> headers, 
			HashMap<String, String> parameters, int[] validResponseCodes) {
		super(/*id, engine, */url, HTTPRequestType.PUT, true, headers, parameters, validResponseCodes);
	}
}
