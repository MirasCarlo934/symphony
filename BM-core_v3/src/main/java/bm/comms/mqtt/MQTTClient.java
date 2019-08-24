package bm.comms.mqtt;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import bm.tools.SystemTimer;

public class MQTTClient extends MqttClient {
	private Logger logger;
	private MQTTListener callback;
	private MQTTPublisher publisher;
	private String BM_topic;
	private String default_topic;
	
//	private MemoryPersistence persistence = new MemoryPersistence();

	public MQTTClient(String serverURI, String clientId, String logDomain, String BM_topic, String default_topic, 
			MQTTListener callback, MQTTPublisher publisher, int reconnectPeriod, SystemTimer sysTimer) 
					throws MqttException {
		super(serverURI, clientId, new MemoryPersistence());
		logger = Logger.getLogger(logDomain + "." + MQTTClient.class.getSimpleName());
		this.BM_topic = BM_topic;
		this.default_topic = default_topic;
		this.callback = callback;
		this.publisher = publisher;
		connectToMQTT();
		sysTimer.schedule(new MQTTClientReconnector(this), 0, reconnectPeriod);
	}
	
	/**
	 * Connects this MQTTHandler to the MQTT broker
	 * 
	 * @return <b>True</b> if the MQTTHandler has successfully connected, <b>false</b> otherwise.
	 */
	public boolean connectToMQTT() {
		try {
			JSONObject lastWill = new JSONObject();
			lastWill.put("RID", "BM-exit");
			lastWill.put("RTY", "exit");
			lastWill.put("msg", "BM has terminated.");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setWill(default_topic, lastWill.toString().getBytes(), 2, false);
			connOpts.setCleanSession(true);
			connect(connOpts);
			logger.info("Connected to MQTT!");
			subscribe(BM_topic);
			logger.debug("Subscribed to BM topic!");
			setCallback(callback);
			logger.debug("Listener set!");
			publisher.setClient(this);
			logger.debug("Publisher set!");
			return true;
		} catch (MqttSecurityException e) {
			logger.fatal("Cannot connect to MQTT server due to MqttSecurityException!", e);
			logger.info("Attempting to reconnect...");
			connectToMQTT();
			return false;
		} catch (MqttException e) {
			logger.fatal("Cannot connect to MQTT due to MqttException!", e);
			logger.info("Attempting to reconnect...");
			connectToMQTT();
			return false;
		}
	}
	
	private class MQTTClientReconnector extends TimerTask {
		private MQTTClient client;
		
		protected MQTTClientReconnector(MQTTClient client) {
			this.client = client;
		}

		@Override
		public void run() {
			if(!client.isConnected()) {
				logger.info("Reconnecting...");
				try {
					client.reconnect();
					logger.info("Connected to MQTT!");
				} catch (MqttException e) {
					logger.fatal("Error reconnecting!", e);
				}
			}
		}
	}
}
