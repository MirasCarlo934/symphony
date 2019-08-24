package bm.comms.http.messages;

import java.util.HashMap;

import bm.comms.http.HTTPSender;

/**
 * The HTTPEngineRequest representation of an HTTP POST request that the HTTPEngine will send to the specified URL.
 * 
 * @author carlomiras
 *
 */
public class PostHTTPReq extends HTTPRequest {
	
	/**
	 * Creates an HTTPEngine request that the HTTPEngine uses to create an HTTP POST request that will be sent to the
	 * specified URL.
	 * 
	 * @param id the ID of this engine request
	 * @param url the URL where the POST request will be sent to
	 * @param headers the headers of this POST request
	 * @param parameters the parameters of this POST request
	 * @param validResponseCodes the response codes that are valid for this request. Responses with codes that are not 
	 * 		specified here will be considered as <i>bad</i> responses, and the HTTPEngine will throw an exception
	 */
	public PostHTTPReq(/*String id, HTTPEngine engine, */String url, HashMap<String, String> headers, HashMap<String, String> parameters, 
			int[] validResponseCodes) {
		super(/*id, engine, */url, HTTPRequestType.POST, true, headers, parameters, validResponseCodes);
	}
}
