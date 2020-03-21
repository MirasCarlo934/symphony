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
import symphony.bm.bmlogicdevices.controller.Controller;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.rest.vo.Success;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

@RestController
public class InboundRestMicroserviceCommunicator {
    private Logger LOG;
    private Controller controller;

    public InboundRestMicroserviceCommunicator(@Value("${log.rest}") String logDomain,
            @Autowired @Qualifier("CORE.controller") Controller controller) {
        LOG = LoggerFactory.getLogger(logDomain + "." + "Inbound");
        this.controller = controller;
    }

    @RequestMapping("/")
    public Success receiveJeepMessage(@RequestParam(name = "msg") String msg) {
        JeepMessage jeepMsg = new JeepMessage(msg);
        try {
            controller.processJEEPMessage(jeepMsg);
            return new Success(true);
        } catch (SecondaryMessageParameterCheckingException e) {
            String cid = jeepMsg.getString("CID");
            LOG.error("Error on secondary message parameter checking.");
            return new Success(false);
        }
    }

}
