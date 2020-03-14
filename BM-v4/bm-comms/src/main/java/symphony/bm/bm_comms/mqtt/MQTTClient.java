package symphony.bm.bm_comms.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class MQTTClient extends MqttClient {
	private Logger LOG;
	private MQTTListener callback;
	private MQTTPublisher publisher;
	private String BM_topic;
	private String default_topic;

	public MQTTClient(String serverURI, String clientId, String logDomain, String name, String BM_topic,
					  String default_topic, MQTTListener listener, MQTTPublisher publisher, int reconnectPeriod)
					throws MqttException {
		super(serverURI, clientId, new MemoryPersistence());
		LOG = LoggerFactory.getLogger(logDomain + "." + name);
		LOG.error("LOOOG");
		System.out.println("HEHEHEHE");
		this.BM_topic = BM_topic;
		this.default_topic = default_topic;
		this.callback = listener;
		this.publisher = publisher;
		connectToMQTT();

		Timer timer = new Timer("SystemTimer");
		timer.schedule(new MQTTClientReconnector(this), 0, reconnectPeriod);
	}
	
	/**
	 * Connects this MQTTHandler to the MQTT broker
	 * 
	 * @return <b>True</b> if the MQTTHandler has successfully connected, <b>false</b> otherwise.
	 */
	private boolean connectToMQTT() {
		try {
			LOG.info("Connecting to MQTT...");
			JSONObject lastWill = new JSONObject();
			lastWill.put("RID", "BM-exit");
			lastWill.put("RTY", "exit");
			lastWill.put("msg", "BM has terminated.");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setWill(default_topic, lastWill.toString().getBytes(), 2, false);
			connOpts.setCleanSession(true);
			connect(connOpts);
			LOG.info("Connected to MQTT!");
			subscribe(BM_topic);
			LOG.debug("Subscribed to BM topic!");
			setCallback(callback);
			LOG.debug("Listener set!");
			publisher.setClient(this);
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
	
	private class MQTTClientReconnector extends TimerTask {
		private MQTTClient client;
		
		MQTTClientReconnector(MQTTClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			if(!client.isConnected()) {
				LOG.info("Reconnecting...");
				try {
					client.reconnect();
					LOG.info("Connected to MQTT!");
				} catch (MqttException e) {
					LOG.error("Error reconnecting!", e);
				}
			}
		}
	}
}
