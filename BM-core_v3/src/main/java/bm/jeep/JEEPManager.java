package bm.jeep;

import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.*;
import bm.tools.IDGenerator;
import org.apache.log4j.Logger;

public class JEEPManager {
    private final Logger LOG;
    private IDGenerator idg;
    private String msgParam;

    //registration
    private String regRTY;
    private String regIdParam;
    private String regTopicParam;

    //detachment
    private String detRTY;

    //activity
    private String actRTY;

    //POOP
    private String poopRTY;
    private String poopPropIdParam;
    private String poopPropValParam;

    public JEEPManager(String logDomain, IDGenerator idGenerator,
                       String regRTY, String regIdParam, String regTopicParam,
                       String detRTY, String msgParam,
                       String actRTY,
                       String poopRTY, String poopPropIdParam, String poopPropValParam) {
        LOG = Logger.getLogger(logDomain + "." + JEEPManager.class.getSimpleName());
        this.idg = idGenerator;
        this.regRTY = regRTY;
        this.regIdParam = regIdParam;
        this.regTopicParam = regTopicParam;
        this.detRTY = detRTY;
        this.msgParam = msgParam;
        this.actRTY = actRTY;
        this.poopRTY = poopRTY;
        this.poopPropIdParam = poopPropIdParam;
        this.poopPropValParam = poopPropValParam;
    }

    public void sendRegistrationRequest(Device device) {
        LOG.debug("Sending registration request...");
        JEEPMessage msg = new OutboundRegistrationRequest(device.getMAC(), device.getSSID(),
                regRTY, device.getProtocol(), regIdParam, regTopicParam, device.getSSID(), device.getTopic());
        msg.send();
    }

    public void sendRegistrationResponse(Device device, JEEPRequest request) {
        LOG.debug("Sending registration response...");
        JEEPMessage msg = new OutboundRegistrationResponse(device.getMAC(), device.getSSID(), regRTY,
                device.getProtocol(), regIdParam, regTopicParam, device.getSSID(), device.getTopic());
        msg.send();
    }

    public void sendDetachmentRequest(Device device, String message) {
        LOG.debug("Sending detachment request...");
        JEEPMessage msg = new OutboundGenericRequest(idg.generateRID(), device.getSSID(),
                detRTY, device.getProtocol(), message, msgParam);
        msg.send();
    }

    public void sendDetachmentResponse(Device device, boolean success, JEEPRequest request) {
        LOG.debug("Sending detachment response...");
        JEEPMessage msg = new JEEPResponse(request, success);
        msg.send();
    }

    public void sendDeactivationRequest(Device device, String message) {
        LOG.debug("Sending deactivation request...");
        JEEPMessage msg = new OutboundGenericRequest(idg.generateRID(), device.getSSID(),
                actRTY, device.getProtocol(), message, msgParam);
        msg.send();
    }

    public void sendDeactivationResponse(Device device, boolean success) {
        LOG.debug("Sending deactivation response...");
        JEEPMessage msg = new JEEPResponse(idg.generateRID(), device.getSSID(), actRTY, device.getProtocol(),
                success);
        msg.send();
    }

    public void sendPOOPRequest(Property property) {
        LOG.debug("Sending POOP request...");
        JEEPMessage msg = new ReqPOOP(idg.generateRID(), property.getDevice().getSSID(), poopRTY,
                property.getDevice().getProtocol(), poopPropIdParam, poopPropValParam, property.getIndex(),
                property.getValue());
        msg.send();
    }

    public void sendPOOPResponse(Property property, JEEPRequest request) {
        LOG.debug("Sending POOP request...");
        JEEPMessage msg = new ResPOOP(idg.generateRID(), property.getDevice().getSSID(), poopRTY,
                property.getDevice().getProtocol(), poopPropIdParam, poopPropValParam, property.getSSID(),
                property.getValue());
        msg.send();
    }
}
