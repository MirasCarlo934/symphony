package symphony.bm.bm_comms.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bm_comms.jeep.JeepMessage;
import symphony.bm.bm_comms.jeep.RawMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;
    private String name = RestMicroserviceCommunicator.class.getSimpleName();
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Value("${mongo.collection.devices}")
    private String devicesCollection;
    @Value("${http.url.bm}")
    private String bmServerURL;

    private Map<String, String> servicePorts;

    public RestMicroserviceCommunicator(@Value("${log.rest}")String logDomain,
                                        @Autowired @Qualifier("servicePorts") Map<String, String> servicePorts) {
        LOG = LoggerFactory.getLogger(logDomain + "." + name);
        this.servicePorts = servicePorts;
        LOG.info(name + " started!");
    }

    /**
     * Forwards a JEEP message to the logic layer
     * @param message the JEEP message to be forwarded
     */
    public void forwardJeepMessage(JeepMessage message) {
        executor.submit(new ForwardingUnit(message));
    }

    private class ForwardingUnit implements Runnable {
        private final HttpClient httpClient = HttpClientBuilder.create().build();
        private final JeepMessage message;

        public ForwardingUnit(JeepMessage message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                HttpPost request = new HttpPost(bmServerURL + ":" + servicePorts.get(message.getMSN()) + "/registry");
                StringEntity params = new StringEntity(message.toString());
                request.addHeader("content-type", "application/x-www-form-urlencoded");
                request.setEntity(params);

                HttpResponse response = httpClient.execute(request);
                String responseMsgStr = EntityUtils.toString(response.getEntity());
                LOG.info("Response received:");
                LOG.info(responseMsgStr);
                try {
                    JSONArray array = new JSONArray(responseMsgStr);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = (JSONObject) array.get(i);
                        JeepMessage msg = new JeepMessage(new RawMessage(json.toString(), message.getProtocol()));
                        if (msg.getBoolean("success"))
                            msg.send();
                        else
                            msg.sendAsError();
                    }
                } catch (JSONException e) { // msg sent is in JSONObject format
                    JSONObject json = new JSONObject(responseMsgStr);
                    JeepMessage msg = new JeepMessage(new RawMessage(json.toString(), message.getProtocol()));
                    if (msg.getBoolean("success"))
                        msg.send();
                    else
                        msg.sendAsError();
                }
            } catch (IOException e) {
                LOG.error("Error in forwarding message.", e);
                JeepMessage errorMsg = new JeepMessage(message.toString(), message.getProtocol());
                errorMsg.put("error", e.getMessage());
                errorMsg.sendAsError();
            }
        }
    }
}
