package symphony.bm.bm_comms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bm_comms.jeep.RawMessage;

public abstract class Listener {
	protected Logger LOG;
	protected Protocol protocol;
	private InboundTrafficManager itm;
	
	public Listener(String logName, String logDomain, InboundTrafficManager inboundTrafficManager) {
		this.itm = inboundTrafficManager;
		LOG = LoggerFactory.getLogger(logDomain + "." + logName);
		LOG.info("Listener started!");
	}
	
	/**
	 * Sends a RawMessage to be processed by the <i>Controller</i>.
	 * @param msg The RawMessage to be sent
	 */
	protected void processRawMessage(RawMessage msg) {
		itm.addInboundRawMessage(msg);
	}

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
