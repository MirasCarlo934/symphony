package symphony.bm.bmlogicdevices.rest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.services.AbstService;
import symphony.bm.bmlogicdevices.services.UnregisterService;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;

    private AbstService reg;
    private UnregisterService unreg;

    public RestMicroserviceCommunicator(@Value("${log.rest}") String logDomain,
                                        AbstService registerService, UnregisterService unregisterService) {
        LOG = LoggerFactory.getLogger(logDomain + "." + "Inbound");
        this.reg = registerService;
        this.unreg = unregisterService;
    }

    @RequestMapping("/register")
    public String receiveRegisterMessage(@RequestParam("msg") String msgStr) {
        return receiveJeepMessage(msgStr, reg);
    }

    @RequestMapping("/unreg")
    public String receiveJeepMessage(@RequestParam(name = "msg") String msgStr) {
        return receiveJeepMessage(msgStr, unreg);
    }

    private String receiveJeepMessage(String msgStr, AbstService service) {
        LOG.debug("Message arrived: " + msgStr);
        JeepMessage msg = new JeepMessage(msgStr);
        try {
            JSONArray response = new JSONArray(service.processMessage(msg));
            return response.toString();
        }
        catch (MessageParameterCheckingException e) {
            LOG.error("Unable to process message!", e);
            JeepResponse error = new JeepResponse(msg, e.getMessage());
            return error.toString();
        }
    }
}
