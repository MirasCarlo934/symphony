package symphony.bm.bm_comms;

import com.mongodb.*;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bm_comms.jeep.JeepMessage;
import symphony.bm.bm_comms.jeep.RawMessage;
import symphony.bm.bm_comms.jeep.exceptions.PrimaryMessageCheckingException;
import symphony.bm.bm_comms.mongodb.BMCommsMongoDBManager;
import symphony.bm.bm_comms.rest.RestMicroserviceCommunicator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class InboundTrafficManager implements Runnable {
    private final Logger LOG;
    private List<String> msnList;
    private LinkedList<RawMessage> rawMsgQueue = new LinkedList<>();
    private RestMicroserviceCommunicator rest;
    private BMCommsMongoDBManager mongoDBManager;
    private String devicesDBCollection;
    private String msn_register;

    public InboundTrafficManager(String logDomain, String logName, RestMicroserviceCommunicator rest,
                                 /*ResponseManager responseManager,*/ BMCommsMongoDBManager mongoDBManager,
                                 String devicesDBCollection, List<String> msnList, String msn_register) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        this.rest = rest;
        this.mongoDBManager = mongoDBManager;
        this.devicesDBCollection = devicesDBCollection;
        this.msnList = msnList;
        this.msn_register = msn_register;

        Thread t = new Thread(this,logDomain + "." + logName);
        t.start();
        LOG.info(InboundTrafficManager.class.getSimpleName() + " started!");
    }

    public void addInboundRawMessage(RawMessage rawMsg) {
        rawMsgQueue.add(rawMsg);
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            RawMessage rawMsg = rawMsgQueue.poll();
            if(rawMsg != null) {
                LOG.debug("New JEEP message received! Checking primary message validity...");
                try {
                    JeepMessage msg = checkPrimaryMessageValidity(rawMsg);
                    LOG.info("Valid JEEP message received. Forwarding to logic layer.");
                    LOG.info("Forwarding message " + msg.getCID() + "." + msg.getMRN() + " to service layer");
                    rest.forwardJeepMessage(msg);
                    LOG.info("Message " + msg.getCID() + "." + msg.getMRN() + " forwarded successfully");
                } catch(PrimaryMessageCheckingException e) {
                    JeepMessage errorMsg = new JeepMessage(rawMsg.getCheckingJSON().toString(), rawMsg.getProtocol());
                    errorMsg.put("error", e.getMessage());
                    sendError(errorMsg);
                } catch (IOException e) {
                    LOG.error("Error in forwarding message.", e);
                    JeepMessage errorMsg = new JeepMessage(rawMsg.getCheckingJSON().toString(), rawMsg.getProtocol());
                    errorMsg.put("error", e.getMessage());
                    sendError(errorMsg);
                }
            }
        }
    }

    /**
     * Checks if the raw JEEP message string contains all the required primary parameters
     *
     * @param rawMsg The RawMessage object
     * @return The JEEP message type of the request if valid; either <b><i>JeepMessageType.REQUEST</b></i>
     * 		or <b><i>JeepMessageType.RESPONSE</i></b>, <b><i>null</i></b> if: <br>
     * 		<ul>
     * 			<li>The intercepted request is not in JSON format</li>
     * 			<li>There are missing primary request parameters</li>
     * 			<li>There are primary request parameters that are null/empty</li>
     * 			<li>CID does not exist</li>
     * 			<li>MSN does not exist</li>
     * 		</ul>
     */
    private JeepMessage checkPrimaryMessageValidity(RawMessage rawMsg)
            throws PrimaryMessageCheckingException {
        LOG.trace("Checking primary request parameters...");
        JSONObject json;

        //#1: Checks if the intercepted request is in proper JSON format
        try {
            json = new JSONObject(rawMsg.getMessageString());
        } catch(JSONException e) {
            throw new PrimaryMessageCheckingException("Improper JSON construction!");
        }

        //#2: Checks if there are missing primary request parameters
        if (json.has("CID")) {
            //#4: Checks if CID exists
            if(!json.getString("MSN").equals(msn_register) && !checkIfDeviceExists(json.getString("CID"))) {
                throw new PrimaryMessageCheckingException("CID does not exist!");
            } else {
                rawMsg.getCheckingJSON().put("CID",json.getString("CID"));
            }
        } else {
            throw new PrimaryMessageCheckingException("CID parameter not found!");
        }
        if (json.has("MSN")) {
            rawMsg.getCheckingJSON().put("MSN",json.getString("MSN"));
        } else {
            throw new PrimaryMessageCheckingException("MSN parameter not found!");
        }
        if(json.has("MRN")) {
            rawMsg.getCheckingJSON().put("MRN", json.getString("MRN"));
        } else {
            throw new PrimaryMessageCheckingException("MRN parameter not found!");
        }

        //#3: Checks if the primary request parameters are null/empty
        if(json.getString("MRN").equals("") || json.getString("MRN") == null) {
            throw new PrimaryMessageCheckingException("Null MRN!");
        } else if(json.getString("MSN").equals("") || json.getString("MSN") == null) {
            throw new PrimaryMessageCheckingException("Null MSN!");
        }

        //#5 Checks if MSN exists
        if(!msnList.contains(json.getString("MSN"))) {
            throw new PrimaryMessageCheckingException("Invalid MSN!");
        }

        return new JeepMessage(rawMsg);

    }

    private boolean checkIfDeviceExists(String cid) {
        Document device = mongoDBManager.getCollection(devicesDBCollection).find(eq("CID", cid)).first();
        return (device != null);
    }

    private void sendError(JeepMessage errorMsg) {
        LOG.error(errorMsg.getString("error"));
        errorMsg.sendAsError();
    }
}
