package symphony.bm.bmservicespoop.rest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bmservicespoop.jeep.JeepMessage;
import symphony.bm.bmservicespoop.jeep.JeepResponse;
import symphony.bm.bmservicespoop.services.POOPService;
import symphony.bm.bmservicespoop.services.exceptions.MessageParameterCheckingException;

import java.util.List;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;

    private POOPService poop;

    public RestMicroserviceCommunicator(@Value("${log.rest}") String logDomain,
                                        POOPService poop) {
        LOG = LoggerFactory.getLogger(logDomain + ".communicator");
        this.poop = poop;
    }

    @RequestMapping("/poop")
    public String receivePOOPMessage(@RequestParam("msg") String msgStr) {
        LOG.debug("Message arrived: " + msgStr);
        JeepMessage msg = new JeepMessage(msgStr);
        try {
            List<JeepResponse> responses = poop.processMessage(msg);
            JSONArray responseArray = new JSONArray();
            for (JeepResponse res : responses) {
                responseArray.put(res);
            }
            return responseArray.toString();
        }
        catch (MessageParameterCheckingException e) {
            LOG.error("Unable to process message!", e);
            JeepResponse error = new JeepResponse(msg, e.getMessage());
            return error.toString();
        }
    }

    @RequestMapping("/test")
    public Object test(@RequestParam("cid") String cid, @RequestParam("index") int index,
                       @RequestParam("value") int value) {
        JeepMessage msg = new JeepMessage("{}");
        msg.put("MRN", "1234");
        msg.put("MSN", "poop");
        msg.put("CID", cid);
        msg.put("prop-index", index);
        msg.put("prop-value", value);
        try {
            poop.processMessage(msg);
        } catch (MessageParameterCheckingException e) {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }
}
