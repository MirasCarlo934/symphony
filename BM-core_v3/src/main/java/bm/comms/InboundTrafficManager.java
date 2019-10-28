package bm.comms;

import bm.jeep.exceptions.PrimaryMessageCheckingException;
import bm.jeep.vo.*;
import bm.jeep.vo.device.JEEPErrorResponse;
import bm.jeep.vo.device.ReqRequest;
import bm.main.Maestro;
import bm.main.controller.Controller;
import bm.main.repositories.DeviceRepository;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;

public class InboundTrafficManager implements Runnable {
    private final Logger LOG;
    private LinkedList<RawMessage> rawMsgQueue = new LinkedList<RawMessage>();
    private ResponseManager rm;
    private Controller controller;
    private DeviceRepository dr;

    public InboundTrafficManager(String logDomain, Controller controller, DeviceRepository deviceRepository,
                                 ResponseManager responseManager) {
        LOG = Logger.getLogger(logDomain + "." + InboundTrafficManager.class.getSimpleName());
        this.controller = controller;
        this.dr = deviceRepository;
        this.rm = responseManager;
    }

    public void addInboundRawMessage(RawMessage rawMsg) {
        rawMsgQueue.add(rawMsg);
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            RawMessage rawMsg = rawMsgQueue.poll();
            if(rawMsg != null) {
                LOG.trace("New request found! Checking primary validity...");
                try {
                    if (checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.REQUEST) {
                        JEEPRequest request = new ReqRequest(new JSONObject(rawMsg.getMessageStr()),
                                rawMsg.getProtocol());
                        controller.processJEEPMessage(request);
                    } else if (checkPrimaryMessageValidity(rawMsg) == JEEPMessageType.RESPONSE) {
                        JEEPResponse response = new JEEPResponse(new JSONObject(rawMsg.getMessageStr()),
                                rawMsg.getProtocol());
                        rm.removeActiveRequest(response.getRID());
                        controller.processJEEPMessage(response);
                    }
                } catch(PrimaryMessageCheckingException e) {
                    sendError(e.getMessage(), rawMsg.getProtocol());
                }
            }
        }
    }

    /**
     * Checks if the raw JEEP message string contains all the required primary parameters
     *
     * @param rawMsg The RawMessage object
     * @return The JEEP message type of the request if valid; either <b><i>JEEPMessageType.REQUEST</b></i>
     * 		or <b><i>JEEPMessageType.RESPONSE</i></b>, <b><i>null</i></b> if: <br>
     * 		<ul>
     * 			<li>The intercepted request is not in JSON format</li>
     * 			<li>There are missing primary request parameters</li>
     * 			<li>There are primary request parameters that are null/empty</li>
     * 			<li>CID does not exist</li>
     * 			<li>RTY does not exist</li>
     * 		</ul>
     */
    private JEEPMessageType checkPrimaryMessageValidity(RawMessage rawMsg)
            throws PrimaryMessageCheckingException {
        LOG.trace("Checking primary request parameters...");
        JSONObject json;
        String request = rawMsg.getMessageStr();
        ApplicationContext appContext = Maestro.getApplicationContext();

        //#1: Checks if the intercepted request is in proper JSON format
        try {
            json = new JSONObject(request);
        } catch(JSONException e) {
            throw new PrimaryMessageCheckingException("Improper JSON construction!");
//            sendError("Improper JSON construction!", rawMsg.getProtocol());
//            return null;
        }

        //#2: Checks if there are missing primary request parameters
        if(!json.keySet().contains("RID") || !json.keySet().contains("CID") ||
                !json.keySet().contains("RTY")) {
            throw new PrimaryMessageCheckingException("Request does not contain all primary " +
                    "request parameters!");
//            sendError("Request does not contain all primary request parameters!",
//                    rawMsg.getProtocol());
//            return null;
        }

        //#3: Checks if the primary request parameters are null/empty
        if(json.getString("RID").equals("") || json.getString("RID") == null) {
            throw new PrimaryMessageCheckingException("Null RID!");
//            sendError("Null RID!", rawMsg.getProtocol());
//            return null;
        } else if(json.getString("CID").equals("") || json.getString("CID") == null) {
            throw new PrimaryMessageCheckingException("Null CID!");
//            sendError("Null CID!", rawMsg.getProtocol());
//            return null;
        } else if(json.getString("RTY").equals("") || json.getString("RTY") == null) {
            throw new PrimaryMessageCheckingException("Null RTY!");
//            sendError("Null RTY!", rawMsg.getProtocol());
//            return null;
        }

        //#4: Checks if CID exists
        if(json.getString("RTY").equals("register") ||
                (json.getString("RTY").equals("getRooms") &&
                        json.getString("CID").equals("default_topic")));
        else if(!dr.containsDevice(json.getString("CID"))) {
            throw new PrimaryMessageCheckingException("CID does not exist!");
//            sendError("CID does not exist!", rawMsg.getProtocol());
//            return null;
        }

        //#5 Checks if RTY exists
        boolean b = false;

        if(appContext.containsBean(json.getString("RTY"))) {
            b = true;
        }

        if(b) {
            LOG.trace("Checking if message is a request or response...");
            if(json.has("success")) {
                LOG.trace("Primary message parameters good to go!");
                return JEEPMessageType.RESPONSE;
            } else {
                LOG.trace("Primary message parameters good to go!");
                return JEEPMessageType.REQUEST;
            }
        }
        else {
            throw new PrimaryMessageCheckingException("Invalid RTY!");
//            sendError("Invalid RTY!", rawMsg.getProtocol());
//            return null;
        }
    }

    private void sendError(String message, Protocol protocol) {
        LOG.error(message);
        protocol.getSender().sendErrorResponse(new JEEPErrorResponse(message, protocol));
    }
}
