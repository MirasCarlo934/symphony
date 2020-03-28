package symphony.bm.bmlogicdevices.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.SymphonyEnvironment;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.Service;
import symphony.bm.bmlogicdevices.services.ServiceManager;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

import java.util.HashMap;
import java.util.LinkedList;

public class Controller {
    private Logger LOG;
    private LinkedList<Service> serviceQueue = new LinkedList<Service>();
    private HashMap<String, Service> ongoing = new HashMap<String, Service>(1);
    private HashMap<String, ServiceManager> serviceManagers;
    private ThreadPool threadPool;
    private OutboundRestMicroserviceCommunicator outboundRestCommunicator;
    private SymphonyEnvironment env;
//    private int rn = 1;

    public Controller(String logDomain, HashMap<String, ServiceManager> serviceManagers,
                      OutboundRestMicroserviceCommunicator outboundRestMicroserviceCommunicator,
                      SymphonyEnvironment symphonyEnvironment
            /*, ThreadPool threadPool*/) {
        LOG = LoggerFactory.getLogger(logDomain + ".Controller");
        this.serviceManagers = serviceManagers;
        this.outboundRestCommunicator = outboundRestMicroserviceCommunicator;
        this.env = symphonyEnvironment;
//        this.threadPool = threadPool;
        LOG.info("Controller started!");
    }

    public JeepResponse processJEEPMessage(JeepMessage msg) throws MessageParameterCheckingException {
        LOG.info("Checking primary message parameters...");
        Service s = serviceManagers.get(msg.getMSN()).createService();
        return s.processMessage(msg);

//        LOG.trace("Adding " + s.getClass().getSimpleName() + " to ServiceQueue (RN:" + rn + ")");
//        serviceQueue.add(s);
//        rn++;
    }

//    private JeepMessage checkPrimaryMessageParameters(String rawMsg) throws PrimaryMessageParameterCheckingException {
//        JSONObject msgJSON;
//
//        //#1: Checks if the intercepted request is in proper JSON format
//        try {
//            msgJSON = new JSONObject(rawMsg);
//        } catch (JSONException e) {
//            throw primaryMessageCheckingException(e.getMessage(), e);
//        }
//
//        //#2: Checks if there are missing primary request parameters
//        if(!msgJSON.has("MRN") || !msgJSON.has("CID") ||
//                !msgJSON.has("MSN")) {
//            throw primaryMessageCheckingException("Request does not contain all primary request parameters!");
//        }
//
//        //#3: Checks if the primary request parameters are null/empty
//        if(msgJSON.get("MRN").equals("") || msgJSON.get("MRN") == null) {
//            throw primaryMessageCheckingException("Null MRN!");
//        } else if(msgJSON.get("CID") == null) {
//            throw primaryMessageCheckingException("Null CID!");
//        } else if(msgJSON.get("MSN").equals("") || msgJSON.get("MSN") == null) {
//            throw primaryMessageCheckingException("Null MSN!");
//        }
//
//        //#4 Checks if MSN exists
//        boolean b = false;
//
//        if(!serviceManagers.containsKey(msgJSON.getString("MSN"))) {
//            throw primaryMessageCheckingException("Service specified by MSN does not exist!");
//        }
//
//        return new JeepMessage(rawMsg);
//    }
//
//    private PrimaryMessageParameterCheckingException primaryMessageCheckingException(String errorMsg) {
//        LOG.error(errorMsg);
//        return new PrimaryMessageParameterCheckingException(errorMsg);
//    }
//
//    private PrimaryMessageParameterCheckingException primaryMessageCheckingException(String errorMsg, Exception e) {
//        LOG.error(errorMsg);
//        return new PrimaryMessageParameterCheckingException(errorMsg, e);
//    }

//    /**
//     * Checks if the request with the specified RID is currently undergoing processing.
//     *
//     * @param rid The RID of the request to check
//     * @return <b><i>true</i></b> if the request was found to be undergoing processing,
//     * <b><i>false</i></b> if not or the request doesn't exist
//     */
//    public boolean isUndergoingProcessing(String rid) {
//        return ongoing.containsKey(rid);
//    }

//    @Override
//    public void run() {
//        boolean waiting = false; //true if dispatcher is waiting for a thread to open
//        while (!Thread.currentThread().isInterrupted()) {
//            Service m = moduleQueue.poll();
//            while (m != null) {
//                try {
//                    LOG.trace("Executing " + m.getClass().getSimpleName() + " (RN:"
//                            + m.getReferenceNumber() + ")...");
//                    threadPool.execute(m);
//                    ongoing.put(m.getRequest().getRID(), m);
//                    waiting = false;
//                    break;
//                } catch (RejectedExecutionException e) {
//                    if (!waiting) {
//                        LOG.trace("Waiting for a thread to open...");
//                        waiting = true;
//                    }
//                }
//            }
//        }
//    }
}