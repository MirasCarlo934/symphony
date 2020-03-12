package symphony.bm.bm_comms.jeep.vo;


import symphony.bm.bm_comms.Protocol;

public class RawMessage {
	private String requestStr;
	private Protocol protocol;
//	private Sender sender;

	/**
	 * Creates a <i>RawRequest</i> object which contains the raw request string intercepted by a <i>Listener</i>.
	 * This is considered as "raw" since it is not yet considered as a valid JEEP request.
	 * @param requestStr The raw request string
     * @param protocol The protocol in which this RawRequest was intercepted from
	 */
	public RawMessage(String requestStr, Protocol protocol) {
		this.requestStr = requestStr;
		this.protocol = protocol;
	}

	/**
	 * Returns the request string of this <i>RawRequest</i>.
	 * @return The request string
	 */
	public String getMessageStr() {
		return requestStr;
	}
	
	/**
	 * Returns the <i>Sender</i> object paired with the <i>Listener</i> that intercepted this <i>RawRequest</i>.
	 * @return The <i>Sender</i> object
	 */
	public Protocol getProtocol() {
		return protocol;
	}
}
