package symphony.bm.bmlogicdevices.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.controller.Controller;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.jeep.JeepResponse;
import symphony.bm.bmlogicdevices.services.exceptions.MessageParameterCheckingException;

import java.util.List;

public class MQTTClient implements MqttCallback {
    private Logger LOG;
    private String bm_topic;
    private String univ_topic;
    private String error_topic;
    private String devices_topic;
    private List<String> service_topics;
    private MqttClient client;
    private Controller controller;

    public MQTTClient(String serverURI, String clientId, String logDomain, String logName, String bm_topic,
                      String univ_topic, String error_topic, String devices_topic, List<String> service_topics,
                      Controller controller)
            throws MqttException {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        this.bm_topic = bm_topic;
        this.univ_topic = univ_topic;
        this.error_topic = error_topic;
        this.devices_topic = devices_topic;
        this.service_topics = service_topics;
        this.controller = controller;
        client = new MqttClient(serverURI, clientId, new MemoryPersistence());

        connect();
    }

    private void connect() {
        try {
            LOG.info("Connecting to MQTT...");
//            JSONObject lastWill = new JSONObject();
//            lastWill.put("RID", "BM-exit");
//            lastWill.put("RTY", "exit");
//            lastWill.put("msg", "BM has terminated.");
            MqttConnectOptions connOpts = new MqttConnectOptions();
//            connOpts.setWill(default_topic, lastWill.toString().getBytes(), 2, false);
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            for (String service_topic : service_topics) {
                String topic = bm_topic + "/" + service_topic;
                client.subscribe(topic);
                LOG.debug("Subscribed to " + topic + " topic!");
            }
            LOG.info("Fully connected to MQTT!");
            client.setCallback(this);
        } catch (MqttException e) {
            LOG.error("Cannot connect to MQTT!", e);
//            LOG.info("Attempting to reconnect...");
//            connectToMQTT();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        LOG.warn("Disconnected.");
        connect();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        LOG.debug("Message arrived from " + s + ":");
        LOG.debug("Message: " + mqttMessage.toString());
        try {
            JeepResponse response = controller.processJEEPMessage(new JeepMessage(mqttMessage.toString()));
            publish(devices_topic + "/" + response.getCID(), response.toString());
        }
        catch (MessageParameterCheckingException e) { // implies a primary parameter checking exception
            LOG.error("Unable to process message!", e);
            JeepMessage error = new JeepMessage(false, e.getMessage());
            JSONObject msg;
            try {
                msg = new JSONObject(mqttMessage.toString());
                error.put("CID", msg.getString("CID"));
                error.put("MRN", msg.getString("MRN"));
                error.put("MSN", msg.getString("MSN"));
            } catch (Exception e1) {
                if (error.has("CID"))
                    publish(devices_topic + "/" + error.getCID() + "/" + error_topic, error.toString());
                else
                    publish(univ_topic + "/" + error_topic, error.toString());
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    private void publish(String topic, String payload) {
        LOG.debug("Publishing to "  + topic);
        LOG.debug("Message: " + payload);
        try {
            client.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            LOG.error("Unable to publish " + payload + " to " + topic, e);
        }
    }
}
