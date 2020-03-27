package symphony.bm.bmlogicdevices.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.controller.Controller;
import symphony.bm.bmlogicdevices.jeep.JeepMessage;
import symphony.bm.bmlogicdevices.services.exceptions.SecondaryMessageParameterCheckingException;

import java.util.Timer;

public class MQTTClient implements MqttCallback {
    private Logger LOG;
    private String bm_logic_topic;
    private String error_topic;
    private MqttClient client;
    private Controller controller;
    private boolean devMode;

    public MQTTClient(String serverURI, String clientId, String logDomain, String logName, String bm_logic_topic,
                      String error_topic, boolean devMode, Controller controller)
            throws MqttException {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        this.bm_logic_topic = bm_logic_topic;
        this.error_topic = error_topic;
        this.devMode = devMode;
        this.controller = controller;
        client = new MqttClient(serverURI, clientId, new MemoryPersistence());

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
            LOG.info("Connected to MQTT!");
            client.subscribe(bm_logic_topic);
            LOG.debug("Subscribed to " + bm_logic_topic + " topic!");
            client.setCallback(this);
        } catch (MqttException e) {
            LOG.error("Cannot connect to MQTT!", e);
//            LOG.info("Attempting to reconnect...");
//            connectToMQTT();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        LOG.debug("Message arrived: " + mqttMessage.toString());
        JeepMessage msg = new JeepMessage(mqttMessage.toString());
        if (devMode) {
            try {
                controller.processJEEPMessage(msg);
            } catch (SecondaryMessageParameterCheckingException e) {
                LOG.error("Unable to process message!", e);
                publish(error_topic, e.getMessage());
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void publish(String topic, String payload) {
        LOG.debug("Publishing to "  + topic);
        LOG.debug("Message: " + payload);
        try {
            client.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            LOG.error("Unable to publish " + payload + " to " + topic, e);
        }
    }
}
