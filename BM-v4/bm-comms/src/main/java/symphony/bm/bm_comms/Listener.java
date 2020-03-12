package symphony.bm.bm_comms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import symphony.bm.bm_comms.jeep.vo.RawMessage;

public abstract class Listener {
	protected Logger LOG;
	protected Protocol protocol;
	private InboundTrafficManager itm;
	
	public Listener(String name, String logDomain, InboundTrafficManager inboundTrafficManager) {
		this.itm = inboundTrafficManager;
		LOG = LogManager.getLogger(logDomain + "." + name);
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
