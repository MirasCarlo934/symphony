package bm.comms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.device.JEEPErrorResponse;
import bm.main.modules.Module;

public abstract class Sender implements Runnable {
	protected Logger LOG;
	private String name;
	protected LinkedList<JEEPMessage> messageQueue = new LinkedList<JEEPMessage>();
	private ResponseManager rm;
	private Timer timer;
	
	/**
	 * 
	 * @param name
	 * @param logDomain
	 */
	public Sender(String name, String logDomain, ResponseManager responseManager) {
		LOG = Logger.getLogger(logDomain + "." + name);
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
	 * @param message The JEEPMessage to send
	 */
	protected abstract void sendJEEPMessage(JEEPMessage message);
	
	/**
	 * Send a JEEPMessage to the protocol of this Sender object.
	 * 
	 * @param message The JEEPMessage to send
	 */
	public void send(JEEPMessage message) throws IllegalArgumentException {
		if(message instanceof JEEPRequest) {
			JEEPRequest req = (JEEPRequest) message;
			LOG.trace("JEEPRequest to send! Putting request in active requests list...");
			rm.addActiveRequest(req);
		}
		sendJEEPMessage(message);
	}
	
	
	/**
	 * Send a JEEPErrorResponse to the protocol of this Sender object.
	 * 
	 * @param error The JEEPErrorResponse
	 */
	public abstract void sendErrorResponse(JEEPErrorResponse error);
	
	public String getName() {
		return name;
	}

//    private class SenderResender extends TimerTask {
//		private HashMap<ModuleJEEPRequest, Integer> timeLeft = new HashMap<ModuleJEEPRequest, Integer>(1);
//		private int secondsToWaitBeforeResending;
//		private int resendTimeout;
//
//		private SenderResender(int secondsToWaitBeforeResend, int resendTimeout) {
//			this.secondsToWaitBeforeResending = secondsToWaitBeforeResend;
//			this.resendTimeout = resendTimeout;
//		}
//
//		@Override
//		public void run() {
//			Iterator<ModuleJEEPRequest> reqs = requests.values().iterator();
//			while(reqs.hasNext()) {
//				ModuleJEEPRequest req = reqs.next();
//				if(!timeLeft.containsKey(req)) {
//					timeLeft.put(req, resendTimeout - secondsToWaitBeforeResending);
//				}
//				if(timeLeft.get(req).intValue() > 0) {
//					LOG.debug("No response yet for JEEPRequest " + req.request.getJSON().toXML() +
//							". Resending.");
//					sendJEEPMessage(req.request);
//				} else {
//					LOG.warn("Device " + req.request.getCID() + " did not respond to JEEPRequest " +
//							req.request.getJSON().toXML() + " within the specified time! " +
//							"(Resending will stop)");
//					timeLeft.remove(req);
//				}
//			}
//		}
//	}

//	private class ModuleJEEPRequest {
//		JEEPRequest request;
//
//		private ModuleJEEPRequest(JEEPRequest request, Module module) throws ClassCastException {
//			this.request = request;
//		}
//	}
}
