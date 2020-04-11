package symphony.bm.services;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import symphony.bm.services.jeep.JeepMessage;
import symphony.bm.services.jeep.JeepResponse;
import symphony.bm.services.jeep.request.register.RegisterRequest;
//import symphony.bm.registry.adaptors.*;

import java.util.List;

@RestController
public class ServicesController {
    private Logger LOG;

//    private CIRManager cirm;
//    private POOPService poop;
//    private List<Adaptor> adaptors;

    public ServicesController(@Value("${log.rest}") String logDomain/*,
            DevicePropertyRegistry registry, POOPService poop, CIRManager cirm*/) {
        LOG = LoggerFactory.getLogger(logDomain + ".communicator");
//        this.poop = poop;
//        this.registry = registry;
    }
    
    @PutMapping("/register")
    public JeepResponse register(@RequestBody RegisterRequest request) {
        return new JeepResponse(request);
    }
    
    @RequestMapping("/internal/add")
    public boolean add(@RequestParam("cid") String cid) {
        LOG.info("Adding properties of device " + cid + " from DB");
//        registry.addFromDB(cid);
        LOG.info("Properties of device " + cid + " added");
        return true;
    }
    
    @GetMapping("/internal/rules")
    public String addRule() {
        JSONArray res = new JSONArray();
        
        return res.toString();
    }
    
    @PutMapping("/test")
    public boolean test(@RequestBody JeepMessage msg) {
        return true;
    }

//    @RequestMapping("/test")
//    public Object test(@RequestParam("cid") String cid, @RequestParam("index") int index,
//                       @RequestParam("value") int value) {
//        JeepMessage msg = new JeepMessage("{}");
//        msg.put("MRN", "1234");
//        msg.put("MSN", "poop");
//        msg.put("CID", cid);
//        msg.put("prop-index", index);
//        msg.put("prop-value", value);
//        try {
//            poop.processMessage(msg);
//        } catch (MessageParameterCheckingException e) {
//            LOG.error(e.getMessage(), e);
//        }
//        return true;
//    }
}
