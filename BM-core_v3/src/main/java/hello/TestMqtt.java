package hello;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;


public class TestMqtt extends MqttClient {
    private static Logger LOG;
    public TestMqtt(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
        LOG = Logger.getLogger("MQTT");
    }
    public void connect() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        String lastWill = "Last Will";
        connOpts.setWill("default_topic", lastWill.toString().getBytes(), 0, false);
        connOpts.setCleanSession(true);
        try {
            LOG.info("start Connect to MQTT");
            connect(connOpts);
            LOG.info("done Connect to MQTT");
        } catch (MqttException e) {
            LOG.info("error Connect to MQTT");
            e.printStackTrace();
        }
    }
    public void send(String s) {
        try {
            LOG.info("start publishing to MQTT");
            publish("/BM", s.getBytes(), 0, false);
            LOG.info("done publishing to MQTT");
        } catch (MqttException e) {
            LOG.error("error publishing to MQTT");
            e.printStackTrace();
        }
    }
}
