package symphony.bm.bm_comms.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import symphony.bm.bm_comms.ResponseManager;
import symphony.bm.bm_comms.Sender;
import symphony.bm.bm_comms.jeep.vo.JeepErrorResponse;
import symphony.bm.bm_comms.jeep.vo.JeepMessage;
import symphony.bm.bm_comms.jeep.vo.JeepResponse;
import symphony.bm.bm_comms.mqtt.objects.MQTTMessage;

import java.util.LinkedList;

public class MQTTPublisher extends Sender {
	private MQTTClient client;
	private String default_topic;
	private String error_topic;
	private LinkedList<MQTTMessage> queue = new LinkedList<MQTTMessage>();
//	private Vector<String> deviceTopics;
	private String regRTY;

	public MQTTPublisher(String name, String logDomain, String default_topic, String error_topic, String regRTY,
						 ResponseManager responseManager) {
		super(logDomain, name, responseManager);
		this.regRTY = regRTY;
		this.default_topic = default_topic;
		this.error_topic = error_topic;
	}
	
	public void setClient(MQTTClient client) {
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
				} catch (MqttPersistenceException e) {
					LOG.error("Cannot publish message \"" + m.message + "\" to topic \"" + m.topic + "\" "
							+ "topic!", e);
				} catch (MqttException e) {
					LOG.error("Cannot publish message \"" + m.message + "\" to topic \"" + m.topic + "\" "
							+ "topic!", e);
				}

			}
		}
	}

//	private void getDevices() {
//		for(Device device : dr.getAllDevices()) {
//			if(deviceTopics.isEmpty()) {
//				deviceTopics.add(device.getTopic());
//			} else {
//				if (device.isActive()) {
//					deviceTopics.add(device.getTopic());
//				} else {
//					deviceTopics.remove(device.getTopic());
//				}
//			}
//		}
//	}

	@Override
	public void sendJeepMessage(JeepMessage message) {
		if(message.getRTY().equals(regRTY)) {
			publishToDefaultTopic(message);
		} else {
			publish(message);
		}
	}
	
	@Override
	public void sendErrorResponse(JeepErrorResponse error) {
		publishToDefaultTopic(error);
		publishToErrorTopic(error);
	}

	public void sendErrorResponse(String topic, JeepErrorResponse error) {
		publish(topic, error);
		sendErrorResponse(error);
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
		publish(default_topic, message.getJSON().toString());
	}

	private void publishToErrorTopic(JeepResponse response) {
		publish(error_topic, response.toString());
	}
}
