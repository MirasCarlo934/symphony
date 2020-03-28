package symphony.bm.bm_comms.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import symphony.bm.bm_comms.Sender;
import symphony.bm.bm_comms.jeep.JeepMessage;
import symphony.bm.bm_comms.mqtt.objects.MQTTMessage;

import java.util.LinkedList;

public class MQTTPublisher extends Sender {
	private MQTTClient client;
	private String universal_topic;
	private String error_topic;
	private String devices_topic;
	private LinkedList<MQTTMessage> queue = new LinkedList<MQTTMessage>();
	private String msn_register;

	public MQTTPublisher(String logName, String logDomain, String universal_topic, String error_topic,
						 String devices_topic, String msn_register) {
		super(logName, logDomain);
		this.msn_register = msn_register;
		this.universal_topic = universal_topic;
		this.error_topic = error_topic;
		this.devices_topic = devices_topic;

		Thread t = new Thread(this, MQTTPublisher.class.getSimpleName());
		t.start();
	}
	
	void setClient(MQTTClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			if(!queue.isEmpty()) {
				MQTTMessage m = queue.removeFirst();
				LOG.debug("Publishing message:" + m.message + " to " + m.topic);
				MqttMessage payload = new MqttMessage(m.message.getBytes());
				payload.setQos(2);
				try {
					client.publish(m.topic, payload);
				} catch (MqttException e) {
					LOG.error("Cannot publish message \"" + m.message + "\" to topic \"" + m.topic + "\" "
							+ "topic!", e);
				}
			}
		}
	}

	@Override
	public void sendJeepMessage(JeepMessage message) {
		if(message.getMSN().equals(msn_register)) {
			publishToDefaultTopic(message);
		} else {
			publish(devices_topic + "/" + message.getCID(), message.toString());
		}
	}
	
	@Override
	public void sendErrorMessage(JeepMessage error) {
		if (error.has("CID")) {
			publish(devices_topic + "/" + error.getString("CID") + "/" + error_topic, error.toString());
		}
		publishToUniversalErrorTopic(error);
	}
	
	/**
	 * Publish to MQTT with String message
	 * @param topic The topic to publish to
	 * @param message The message
	 */
	private void publish(String topic, String message) {
		LOG.trace("Adding new MQTTMessage to topic '" + topic + "' to queue...");
		queue.add(new MQTTMessage(topic, message, Thread.currentThread()));
	}

	public void publishToDefaultTopic(JeepMessage message) {
		publish(universal_topic, message.toString());
	}

	private void publishToUniversalErrorTopic(JeepMessage message) {
		publish(universal_topic + "/" + error_topic, message.toString());
	}
}
