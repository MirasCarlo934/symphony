package symphony.bm.bm_comms.rest;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bm_comms.jeep.vo.JeepMessage;
import symphony.bm.bm_comms.mongodb.BMCommsMongoDBManager;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;
    private String name = RestMicroserviceCommunicator.class.getSimpleName();

    @Value("${mongo.collection.devices}")
    private String devicesCollection;

    private BMCommsMongoDBManager mongoDBManager;

    public RestMicroserviceCommunicator(@Value("${log.comms}")String logDomain,
                                        @Autowired @Qualifier("DB.MongoManager") BMCommsMongoDBManager mongoDBManager) {
        LOG = LoggerFactory.getLogger(logDomain + "." + name);
        this.mongoDBManager = mongoDBManager;
        LOG.info(name + " started!");
    }

    @RequestMapping("/test")
    public TestTxt test(@RequestParam(value = "txt", defaultValue = "Hello, World!") String txt) {
        TestTxt obj = new TestTxt(txt);
        LOG.info(txt);
        return obj;
    }

    @RequestMapping("/registerNewDevice")
    public boolean registerNewDevice(@RequestParam(value="cid") String cid) {
        LOG.info("Registering device " + cid + " to database (for MQTT purposes)...");
        DBObject device = new BasicDBObject("CID", cid)
                            .append("topic", cid + "-topic");
        try {
            mongoDBManager.insert(devicesCollection, device);
            return true;
        } catch (Exception e) {
            LOG.error("Cannot register device " + cid + " to database!", e);
            return false;
        }
    }

    /**
     * Forwards a JEEP message to the logic layer
     * @param message the JEEP message to be forwarded
     */
    public void forwardJeepMessage(JeepMessage message) {

    }

    /**
     * Checks if the RTY corresponds to an existing request module in the logic layer
     * @param rty the RTY to be checked
     */
    public boolean checkRTY(String rty) {
        return true;
    }

    /**
     * Checks if the device with the corresponding CID exists
     * @param cid the RTY to be checked
     */
    public boolean checkDevice(String cid) {
        return true;
    }

    class TestTxt {
        private String txt;

        TestTxt(String txt) {
            this.txt = txt;
        }

        public String getTxt() {
            return txt;
        }
    }
}
