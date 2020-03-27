package symphony.bm.bmlogicdevices.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.rest.OutboundRestMicroserviceCommunicator;
import symphony.bm.bmlogicdevices.services.Service;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionException;

public class Controller {
    private Logger LOG;
    private LinkedList<Service> serviceQueue = new LinkedList<Service>();
    private HashMap<String, Service> ongoing = new HashMap<String, Service>(1);
    private HashMap<String, Service> services;
    private ThreadPool threadPool;
    private OutboundRestMicroserviceCommunicator outboundRestCommunicator;
//    private int rn = 1;

    public Controller(String logDomain, HashMap<String, Service> services,
                      OutboundRestMicroserviceCommunicator outboundRestMicroserviceCommunicator
            /*, ThreadPool threadPool*/) {
        LOG = LoggerFactory.getLogger(logDomain + ".Controller");
        this.services = services;
        this.outboundRestCommunicator = outboundRestMicroserviceCommunicator;
//        this.threadPool = threadPool;
        LOG.info("Controller started!");
    }

    public void processJEEPMessage(JeepMessage message) throws SecondaryMessageParameterCheckingException {
        String msn = message.getMSN();
        Service s = services.get(msn);
        s.processMessage(message);

//        LOG.trace("Adding " + s.getClass().getSimpleName() + " to ServiceQueue (RN:" + rn + ")");
//        serviceQueue.add(s);
//        rn++;
    }

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