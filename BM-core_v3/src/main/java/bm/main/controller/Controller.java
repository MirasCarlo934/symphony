package bm.main.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionException;

import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.main.modules.Module;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import bm.main.Maestro;

public class Controller implements Runnable {
	private Logger LOG;
	private LinkedList<Module> moduleQueue = new LinkedList<Module>();
	private HashMap<String, Module> ongoing = new HashMap<String, Module>(1);
    private ThreadPool threadPool;
	private int rn = 1;

	public Controller(String logDomain, ThreadPool threadPool) {
		LOG = Logger.getLogger(logDomain + ".Controller");
		this.threadPool = threadPool;
		LOG.info("Controller started!");
	}

	public void processJEEPMessage(JEEPMessage message) {
	    String rty = message.getRTY();
        LOG.trace("Retrieving module for RTY '" + rty + "'");
        ApplicationContext appContext = Maestro.getApplicationContext();
        Module m = (Module) appContext.getBean(rty);
        if(message instanceof JEEPRequest) {
            m.setRequest((JEEPRequest) message);
            m.setReferenceNumber(rn);
        } else {
            m.setResponse((JEEPResponse) message);
            m.setReferenceNumber(rn);
        }
        LOG.trace("Adding " + m.getClass().getSimpleName() + " to ModuleQueue (RN:" + rn + ")");
        moduleQueue.add(m);
        rn++;
    }

    public void processNonResponsiveJEEPRequest(JEEPRequest request) {
        String rty = request.getRTY();
        LOG.trace("Retrieving module RTY '" + rty + "' to process nonresponsiveness.");
        ApplicationContext appContext = Maestro.getApplicationContext();
        Module m = (Module) appContext.getBean(rty);
        m.processNonResponse(request);
        m.setReferenceNumber(rn);
        LOG.trace("Adding module to ModuleQueue (RN:" + rn + ")");
        moduleQueue.add(m);
        rn++;
    }

    /**
     * Checks if the request with the specified RID is currently undergoing processing.
     * @param rid The RID of the request to check
     * @return <b><i>true</i></b> if the request was found to be undergoing processing,
     *      <b><i>false</i></b> if not or the request doesn't exist
     */
    public boolean isUndergoingProcessing(String rid) {
	    return ongoing.containsKey(rid);
    }

    @Override
    public void run() {
        boolean waiting = false; //true if dispatcher is waiting for a thread to open
        while(!Thread.currentThread().isInterrupted()) {
            Module m = moduleQueue.poll();
            while(m != null) {
                try {
                    LOG.trace("Executing " + m.getClass().getSimpleName() + " (RN:"
                            + m.getReferenceNumber() + ")...");
                    threadPool.execute(m);
                    ongoing.put(m.getRequest().getRID(), m);
                    waiting = false;
                    break;
                } catch(RejectedExecutionException e) {
                    if(!waiting) {
                        LOG.trace("Waiting for a thread to open...");
                        waiting = true;
                    }
                }
            }
        }
    }
	
//	/**
//	 * Adds a RawMessage to the RawMessage queue.
//	 * @param msg
//	 */
//	public void addRawMessage(RawMessage msg) {
//		rawMsgQueue.add(msg);
//	}
//
//	/**
//	 * Processes the request intercepted by a <i>Listener</i> object.
//	 */
//	@Override
//	public void run() {
//		while(!Thread.currentThread().isInterrupted()) {
//			RawMessage rawMsg = rawMsgQueue.poll();
//			if(rawMsg != null) {
//				LOG.trace("New request found! Checking primary validity...");
//				ApplicationContext appContext = Maestro.getApplicationContext();
//				if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.REQUEST) {
//					ReqRequest r = new ReqRequest(new JSONObject(rawMsg.getMessageStr()),
//							rawMsg.getProtocol());
//					String rty = r.getString("RTY");
//					LOG.trace("Retrieving module for RTY '" + rty + "'");
//					Module m = (Module) appContext.getBean(rty);
//					m.setRequest(r);
//					LOG.trace("Adding module to ModuleQueue (RRN:" + rn + ")");
//					moduleQueue.add(m);
//					rn++;
//				} else if(checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.RESPONSE) {
//					JEEPResponse r = new JEEPResponse(new JSONObject(rawMsg.getMessageStr()),
//							rawMsg.getProtocol());
//					String rty = r.getJSON().getString("RTY");
//					LOG.trace("Retrieving module for RTY '" + rty + "'");
//					MultiModule m = (MultiModule) appContext.getBean(rty);
//					LOG.trace("Returning response for '" + m.getName() + "' module. [RID: " +
//							r.getRID() + "]");
//					m.returnResponse(r);
//				}
//			}
//		}
//	}
}