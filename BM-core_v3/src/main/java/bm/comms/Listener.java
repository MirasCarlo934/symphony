package bm.comms;

import org.apache.log4j.Logger;

import bm.jeep.vo.RawMessage;
import bm.main.controller.Controller;

public abstract class Listener {
	protected Logger LOG;
	protected Protocol protocol;
	private Controller controller;
	private InboundTrafficManager itm;
	
	public Listener(String name, String logDomain, InboundTrafficManager inboundTrafficManager) {
		this.itm = inboundTrafficManager;
		LOG = Logger.getLogger(logDomain + "." + name);
	}
	
	/**
	 * Sends a RawMessage to be processed by the <i>Controller</i>.
	 * @param msg The RawMessage to be sent
	 */
	protected void sendRawMessageToContoller(RawMessage msg) {
		itm.addInboundRawMessage(msg);
	}

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
