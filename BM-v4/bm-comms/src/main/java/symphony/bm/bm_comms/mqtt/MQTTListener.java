package symphony.bm.bm_comms.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import symphony.bm.bm_comms.InboundTrafficManager;
import symphony.bm.bm_comms.Listener;
import symphony.bm.bm_comms.jeep.vo.RawMessage;

/**
 * The MQTTHandler is the object that handles all interactions related to an MQTT server. 
 * Specifically, this object handles the connection of an MqttClient to the MQTT server, 
 * reception of messages into the BM topic, the publishing of messages to different topics, 
 * connection error handling, and the reconnection function for higher availability.
 * 
 * @author User
 *
 */
public class MQTTListener extends Listener implements MqttCallback {
//	private static Logger LOG;
	private String default_topic;
	private String error_topic;

	public MQTTListener(String name, String logDomain, String default_topic, String error_topic,
						InboundTrafficManager inboundTrafficManager) {
		super(name, logDomain, inboundTrafficManager);
//		logger = Logger.getLogger(logDomain + "." + MQTTListener.class.getSimpleName());
		setDefault_topic(default_topic);
		setError_topic(error_topic);
	}

	public void connectionLost(Throwable arg0) {
		LOG.error("Connection lost with MQTT server!", arg0);
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		MqttMessage m = null;
		try {
			m = arg0.getMessage();
			if(m == null) {
				LOG.debug("Message published!");
			} else {
				LOG.error("Unable to deliver message: " + m.getPayload().toString());
			}
		} catch (MqttException e) {
			LOG.error("Unable to deliver message: " + m.getPayload().toString());
		}
	} 

	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		System.out.println("\n\n");
		LOG.debug("Message arrived at topic " + topic);
		LOG.debug("Message is: \n" + msg.toString());
		processRawMessage(new RawMessage(msg.toString(), protocol));
	}

	/**
	 * @return the default_topic
	 */
	public String getDefault_topic() {
		return default_topic;
	}

	/**
	 * @param default_topic the default_topic to set
	 */
	public void setDefault_topic(String default_topic) {
		this.default_topic = default_topic;
	}

	/**
	 * @return the error_topic
	 */
	public String getError_topic() {
		return error_topic;
	}

	/**
	 * @param error_topic the error_topic to set
	 */
	public void setError_topic(String error_topic) {
		this.error_topic = error_topic;
	}

//	@Override
//	public String convertReceivedMessageToJEEPRequestString(Object message) {
//		MqttMessage msg = (MqttMessage) message;
//		return msg.toXML();
//	}
	
//	private final class MQTTPublisher implements Runnable {
//
//		@Override
//		public void run() {
//			MQTTMessage m = queue.removeFirst();
//			logger.debug("Publishing message:" + m.message + " to " + m.topic);
//			MqttMessage payload = new MqttMessage(m.message.getBytes());
//			payload.setQos(0);
//			try {
//				client.publish(m.topic, payload);
//				logger.trace("Message published!");
//			} catch (MqttPersistenceException e) {
//				logger.error("Cannot publish message:" + m.message + " to topic:" + m.topic + "!", e);
//			} catch (MqttException e) {
//				logger.error("Cannot publish message:" + m.message + " to topic:" + m.topic + "!", e);
//			}
//		}
//	}
}
