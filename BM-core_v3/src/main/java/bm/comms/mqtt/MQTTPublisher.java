package bm.comms.mqtt;

import java.util.LinkedList;
import java.util.Vector;

import bm.comms.ResponseManager;
import bm.context.devices.Device;
import bm.main.interfaces.Initializable;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import bm.comms.Sender;
import bm.comms.mqtt.objects.MQTTMessage;
import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.JEEPErrorResponse;
import bm.main.repositories.DeviceRepository;

public class MQTTPublisher extends Sender {
	private MQTTClient client;
	private String default_topic;
	private String error_topic;
	private DeviceRepository dr;
	protected LinkedList<MQTTMessage> queue = new LinkedList<MQTTMessage>();
//	private Vector<String> deviceTopics;
	private String regRTY;

	public MQTTPublisher(String name, String logDomain, String default_topic, String error_topic, String regRTY,
						 DeviceRepository deviceRepository, ResponseManager responseManager) {
		super(logDomain, name, responseManager);
		this.regRTY = regRTY;
		this.default_topic = default_topic;
		this.error_topic = error_topic;
		this.dr = deviceRepository;
//		this.deviceTopics = new Vector<String>(dr.getAllDevices().length, 1);
	}
	
	public void setClient(MQTTClient client) {
		this.client = client;
	}

//	@Override
//	public void initialize() {
//		LOG.debug("Getting device topics from DeviceRepository...");
//		getDevices();
//		LOG.debug("Device topics retrieved!");
//	}

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
	public void sendJEEPMessage(JEEPMessage message) {
		if(message.getRTY().equals(regRTY)) {
			publishToDefaultTopic(message);
		} else {
			publish(message);
		}
	}
	
	@Override
	public void sendErrorResponse(JEEPErrorResponse error) {
		if(error.isComplete()) {
			publish(error);
		} else {
			publishToErrorTopic(error);
		}
	}
	
	/**
	 * Publish to MQTT with String message
	 * @param destination The topic to publish to
	 * @param message The message
	 */
	private void publish(String destination, String message) {
		LOG.trace("Adding new MQTTMessage to topic '" + destination + "' to queue...");
		queue.add(new MQTTMessage(destination, message, Thread.currentThread()));
	}
	
	/**
	 * Publish a JEEPMessage to MQTT
	 *
	 * @param message The JEEPMessage
	 */
	private void publish(JEEPMessage message) {
		String topic = default_topic;
		Device d = dr.getDevice(message.getCID());
		if(d != null) {
			topic = d.getTopic();
//			if(deviceTopics.contains(d.getTopic())) {
//				topic = d.getTopic();
//			} else { //flow goes here if device is newly created
//				LOG.warn("Device " + d.getSSID() + " is not yet active. Sending to default_topic instead...");
//				deviceTopics.add(d.getTopic());
//			}
		}
		publish(topic, message.toString());
	}

	public void publishToDefaultTopic(JEEPMessage message) {
		publish(default_topic, message.getJSON().toString());
	}

	private void publishToErrorTopic(JEEPResponse response) {
		publish(error_topic, response.toString());
	}
}
