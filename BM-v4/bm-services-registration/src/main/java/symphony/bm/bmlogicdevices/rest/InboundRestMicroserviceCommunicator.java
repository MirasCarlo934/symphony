package symphony.bm.bmlogicdevices.rest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bmlogicdevices.SymphonyEnvironment;
import symphony.bm.bmlogicdevices.controller.Controller;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

@RestController
public class InboundRestMicroserviceCommunicator {
    private Logger LOG;
    private Controller controller;
    private SymphonyEnvironment env;
    private OutboundRestMicroserviceCommunicator outbound;

    public InboundRestMicroserviceCommunicator(@Value("${log.rest}") String logDomain,
            @Autowired @Qualifier("CORE.controller") Controller controller,
            @Autowired @Qualifier("CORE.env") SymphonyEnvironment symphonyEnvironment,
            @Autowired OutboundRestMicroserviceCommunicator outbound) {
        LOG = LoggerFactory.getLogger(logDomain + "." + "Inbound");
        this.controller = controller;
        this.env = symphonyEnvironment;
    }

    @RequestMapping("/")
    public String receiveJeepMessage(@RequestParam(name = "msg") String msgStr) {
        LOG.debug("Message arrived: " + msgStr);
        JeepMessage msg = new JeepMessage(msgStr);
        try {
            JeepResponse response = controller.processJEEPMessage(msg);
            return response.toString();
//            publish(devices_topic + "/" + response.getCID(), response.toString());
        }
        catch (MessageParameterCheckingException e) {
            LOG.error("Unable to process message!", e);
            JeepResponse error = new JeepResponse(msg, e.getMessage());
            return error.toString();
        }
    }

    @RequestMapping("/checkCID")
    public boolean checkCID(@RequestParam(name = "cid") String cid) {
        return env.containsDeviceObject(cid);
    }

}
