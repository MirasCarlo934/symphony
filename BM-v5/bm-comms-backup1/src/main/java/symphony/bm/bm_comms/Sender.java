package symphony.bm.bm_comms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bm_comms.jeep.JeepMessage;

import java.util.LinkedList;

public abstract class Sender implements Runnable {
	protected Logger LOG;
	private String name;
	protected LinkedList<JeepMessage> messageQueue = new LinkedList<JeepMessage>();
	
	/**
	 * 
	 * @param logName
	 * @param logDomain
	 */
	public Sender(String logName, String logDomain) {
		LOG = LoggerFactory.getLogger(logDomain + "." + logName);
		this.name = logName;
        LOG.info("Sender started!");
	}
	
	/**
	 * An abstract method to be initialized by a child class for sending JEEP messages to its corresponding
	 * protocol.
	 * 
	 * @param message The JEEP message to send
	 */
	protected abstract void sendJeepMessage(JeepMessage message);
	
	/**
	 * Send a JEEP message to the protocol of this Sender object.
	 * 
	 * @param message The JEEP message to send
	 */
	public void send(JeepMessage message) throws IllegalArgumentException {
		sendJeepMessage(message);
	}
	
	
	/**
	 * Send a JEEP error response to the protocol of this Sender object.
	 * 
	 * @param error The JEEPErrorResponse
	 */
	public abstract void sendErrorMessage(JeepMessage error);
	
	public String getName() {
		return name;
	}
}
