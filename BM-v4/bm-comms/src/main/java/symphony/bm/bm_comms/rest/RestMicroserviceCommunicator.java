package symphony.bm.bm_comms.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bm_comms.jeep.JeepMessage;
import symphony.bm.bm_comms.jeep.RawMessage;
import symphony.bm.bm_comms.mongodb.BMCommsMongoDBManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;
    private String name = RestMicroserviceCommunicator.class.getSimpleName();
    private HttpClient httpClient = HttpClientBuilder.create().build();

    @Value("${mongo.collection.devices}")
    private String devicesCollection;
    @Value("${http.url.bm}")
    private String bmServerURL;

    private Map<String, String> servicePorts;

    public RestMicroserviceCommunicator(@Value("${log.comms}")String logDomain,
                                        @Autowired @Qualifier("servicePorts") Map<String, String> servicePorts) {
        LOG = LoggerFactory.getLogger(logDomain + "." + name);
        this.servicePorts = servicePorts;
        LOG.info(name + " started!");
    }

    @RequestMapping("/test")
    public TestTxt test(@RequestParam(value = "txt", defaultValue = "Hello, World!") String txt) {
        TestTxt obj = new TestTxt(txt);
        LOG.info(txt);
        return obj;
    }

//    @RequestMapping("/registerNewDevice")
//    public boolean registerNewDevice(@RequestParam(value="cid") String cid) {
//        LOG.info("Registering device " + cid + " to database (for MQTT purposes)...");
//        DBObject device = new BasicDBObject("CID", cid)
//                            .append("topic", cid + "-topic");
//        try {
//            mongoDBManager.insert(devicesCollection, device);
//            return true;
//        } catch (Exception e) {
//            LOG.error("Cannot register device " + cid + " to database!", e);
//            return false;
//        }
//    }

    /**
     * Forwards a JEEP message to the logic layer
     * @param message the JEEP message to be forwarded
     */
    public void forwardJeepMessage(JeepMessage message) throws IOException {
        HttpPost request = new HttpPost(bmServerURL + ":" + servicePorts.get(message.getMSN()) + "/"
                + message.getMSN());
        StringEntity params = new StringEntity("msg=" + message.toString());
        request.addHeader("content-type", "application/x-www-form-urlencoded");
        request.setEntity(params);

        HttpResponse response = httpClient.execute(request);
        String responseMsgStr = EntityUtils.toString(response.getEntity());
        LOG.error(responseMsgStr);
        JeepMessage msg = new JeepMessage(new RawMessage(responseMsgStr, message.getProtocol()));
        if (msg.getBoolean("success"))
            msg.send();
        else
            msg.sendAsError();
    }

//    /**
//     * Checks if the device with the corresponding CID exists
//     * @param cid the RTY to be checked
//     */
//    public boolean checkCID(String cid) {
//        HttpPost request = new HttpPost("http://yoururl");
//        StringEntity params =new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"} ");
//        request.addHeader("content-type", "application/x-www-form-urlencoded");
//        request.setEntity(params);
//        HttpResponse response = httpClient.execute(request);
//    }

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
