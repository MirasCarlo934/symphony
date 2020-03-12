package symphony.bm.bm_comms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import symphony.bm.bm_comms.jeep.vo.JeepErrorResponse;
import symphony.bm.bm_comms.jeep.vo.JeepMessage;
import symphony.bm.bm_comms.jeep.vo.JeepRequest;

import java.util.LinkedList;
import java.util.Timer;

public abstract class Sender implements Runnable {
	protected Logger LOG;
	private String name;
	protected LinkedList<JeepMessage> messageQueue = new LinkedList<JeepMessage>();
	private ResponseManager rm;
	private Timer timer;
	
	/**
	 * 
	 * @param name
	 * @param logDomain
	 */
	public Sender(String name, String logDomain, ResponseManager responseManager) {
		LOG = LogManager.getLogger(logDomain + "." + name);
		LOG.info("Starting " + name + " Sender...");
		this.name = name;
		this.rm = responseManager;
//		if(isResending) {
//            this.timer = new Timer(name + "Timer");
//            timer.schedule(new SenderResender(secondsToWaitBeforeResend * 1000,
//                            resendTimeout * 1000), 0,
//                    secondsToWaitBeforeResend * 1000);
//        } else {
//		    LOG.warn(name + " Sender is not set to resend outbound requests when a device is nonresponsive!");
//        }
        LOG.info(name + " Sender started!");
	}
	
	/**
	 * An abstract method to be initialized by a child class for sending JEEP messages to its corresponding
	 * protocol.
	 * 
	 * @param message The JeepMessage to send
	 */
	protected abstract void sendJeepMessage(JeepMessage message);
	
	/**
	 * Send a JeepMessage to the protocol of this Sender object.
	 * 
	 * @param message The JeepMessage to send
	 */
	public void send(JeepMessage message) throws IllegalArgumentException {
		if(message instanceof JeepRequest) {
			JeepRequest req = (JeepRequest) message;
			LOG.trace("JeepRequest to send! Putting request in active requests list...");
			rm.addActiveRequest(req);
		}
		sendJeepMessage(message);
	}
	
	
	/**
	 * Send a JEEPErrorResponse to the protocol of this Sender object.
	 * 
	 * @param error The JEEPErrorResponse
	 */
	public abstract void sendErrorResponse(JeepErrorResponse error);
}
