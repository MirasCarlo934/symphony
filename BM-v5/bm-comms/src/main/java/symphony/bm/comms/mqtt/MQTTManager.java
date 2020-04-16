package symphony.bm.comms.mqtt;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import symphony.bm.comms.rest.ServiceLocator;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MQTTManager implements MqttCallback, Runnable {
    private final Logger LOG = LoggerFactory.getLogger(MQTTManager.class);
    private MqttClient mqttClient;
    private Queue<MqttMessage> messageQueue = new LinkedBlockingQueue<>();
    
    private final int qos;
    private final String serverURI, clientID, bmTopic, univTopic, devicesTopic, errorTopic;
    private final List<ServiceLocator> serviceLocators;
    
    private int messages = 0;
    
    public MQTTManager(@Value("${mqtt.serverURI}") String serverURI, @Value("${mqtt.clientID}") String clientID,
                       @Value("${mqtt.topic.bm}") String bmTopic, @Value("${mqtt.topic.universal}") String univTopic,
                       @Value("${mqtt.topic.devices}") String devicesTopic,
                       @Value("${mqtt.topic.error}") String errorTopic,
                       @Value("${mqtt.qos}") int qos,
                       @Qualifier("serviceLocators") List<ServiceLocator> serviceLocators) {
        this.serverURI = serverURI;
        this.clientID = clientID;
        this.bmTopic = bmTopic;
        this.univTopic = univTopic;
        this.devicesTopic = devicesTopic;
        this.errorTopic = errorTopic;
        this.qos = qos;
        this.serviceLocators = serviceLocators;
        
        connectToMQTT();
    }
    
    private boolean connectToMQTT() {
        try {
            LOG.info("Connecting to MQTT @ " + serverURI + "...");
            this.mqttClient = new MqttClient(serverURI, clientID, new MemoryPersistence());
//            JSONObject lastWill = new JSONObject();
//            lastWill.put("RID", "BM-exit");
//            lastWill.put("RTY", "exit");
//            lastWill.put("msg", "BM has terminated.");
            MqttConnectOptions connOpts = new MqttConnectOptions();
//            connOpts.setWill(default_topic, lastWill.toString().getBytes(), 2, false);
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts);
            LOG.info("Connected to MQTT!");
            mqttClient.subscribe(bmTopic);
            LOG.debug("Subscribed to BM topic!");
            mqttClient.setCallback(this);
            LOG.debug("Listener set!");
            LOG.debug("Publisher set!");
            return true;
        } catch (MqttSecurityException e) {
            LOG.error("Cannot connect to MQTT server due to MqttSecurityException!", e);
            LOG.info("Attempting to reconnect...");
            connectToMQTT();
            return false;
        } catch (MqttException e) {
            LOG.error("Cannot connect to MQTT due to MqttException!", e);
            LOG.info("Attempting to reconnect...");
            connectToMQTT();
            return false;
        }
    }
    
    @Override
    public void connectionLost(Throwable throwable) {
        LOG.error("Connection lost to MQTT", throwable);
    }
    
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        messages++;
        LOG.info("Message arrived at topic " + s);
        LOG.info("Message: " + mqttMessage.toString());
        messageQueue.offer(mqttMessage);
        Thread t = new Thread(this, "Message " + messages);
        t.start();
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    
    }
    
    @Override
    public void run() {
        MqttMessage mqttMessage = messageQueue.poll();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpUriRequest httpMessage;
        JSONObject jsonReq;
        ServiceLocator locator;
        try {
            jsonReq = new JSONObject(mqttMessage.toString());
        } catch (JSONException e) {
            String errorMsg = "Unable to parse JSON from message string";
            LOG.error(errorMsg, e);
            try {
                publishToErrorTopic(errorMsg);
            } catch (MqttException e1) {
                LOG.error("Unable to publish to error topic", e1);
            }
            throw e;
        }
        try {
            locator = getServiceLocator(jsonReq.getString("MSN"));
        } catch (JSONException e) {
            String errorMsg = "No MSN field found";
            LOG.error(errorMsg, e);
            try {
                publishToErrorTopic(errorMsg);
            } catch (MqttException e1) {
                LOG.error("Unable to publish to error topic", e1);
            }
            throw e;
        }
        if (locator == null) {
            String errorMsg = "Invalid MSN";
            LOG.error(errorMsg);
            try {
                publishToErrorTopic(errorMsg);
            } catch (MqttException e1) {
                LOG.error("Unable to publish to error topic", e1);
            }
            return;
        }
    
        String uri = locator.getServiceURL();
        for (String var : locator.getVariablePaths()) {
            uri += "/" + jsonReq.getString(var);
        }
        switch (locator.getHttpMethod()) {
            case DELETE:
                httpMessage = new HttpDelete(uri);
                break;
            case GET:
                httpMessage = new HttpGet(uri);
                break;
            case HEAD:
                httpMessage = new HttpHead(uri);
                break;
            case OPTIONS:
                httpMessage = new HttpOptions(uri);
                break;
            case PATCH:
                HttpPatch patch = new HttpPatch(uri);
                patch.setEntity(new StringEntity(mqttMessage.toString(), ContentType.APPLICATION_JSON));
                httpMessage = patch;
                break;
            case PUT:
                HttpPut put = new HttpPut(uri);
                put.setEntity(new StringEntity(mqttMessage.toString(), ContentType.APPLICATION_JSON));
                httpMessage = put;
                break;
            case POST:
                HttpPost post = new HttpPost(uri);
                post.setEntity(new StringEntity(mqttMessage.toString(), ContentType.APPLICATION_JSON));
                httpMessage = post;
                break;
            default: // TRACE
                httpMessage = new HttpTrace(locator.getBmURL() + ":" + locator.getPort() + "/" + locator.getPath());
        }
    
        try {
            HttpResponse response = httpClient.execute(httpMessage);
            String rspStr = EntityUtils.toString(response.getEntity());
            LOG.info("Response from service: " + rspStr);
            JSONObject jsonRsp = new JSONObject(rspStr);
            String cid;
            try {
                cid = jsonRsp.getString("CID");
            } catch (JSONException e) {
                cid = jsonReq.getString("CID");
            }
            try {
                publishToDevice(cid, jsonRsp.toString());
            } catch (MqttException e) {
                LOG.error("Unable to publish to device " + cid, e);
            }
        } catch (IOException e) {
            LOG.error("IOException in message forwarding", e);
        }
    }
    
    private void publishToDevice(String cid, String msg) throws MqttException {
        String topic = devicesTopic + "/" + cid;
        LOG.info("Publishing to topic " + topic);
        LOG.info("Message: " + msg);
        mqttClient.publish(topic, msg.getBytes(), qos, false);
    }
    
    private void publishToErrorTopic(String msg) throws MqttException {
        LOG.info("Publishing to error topic");
        LOG.info("Message: " + msg);
        String topic = univTopic + "/" + errorTopic;
        mqttClient.publish(topic, msg.getBytes(), qos, false);
    }
    
    private ServiceLocator getServiceLocator(String msn) {
        for (ServiceLocator sl : serviceLocators) {
            if (sl.getMSN().equalsIgnoreCase(msn)) {
                return sl;
            }
        }
        return null;
    }
}
