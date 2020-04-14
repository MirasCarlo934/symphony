package symphony.bm.bm_comms.mqtt.objects;

public final class MQTTMessage {
	public String topic;
	public String message;
	private Thread callerThread;
	
	/**
	 * 
	 * @param topic
	 * @param message
	 * @param callerThread the Thread that called for the publishing of this MQTTMessage
	 */
	public MQTTMessage(String topic, String message, Thread callerThread) {
		this.topic = topic;
		this.message = message;
		this.callerThread = callerThread;
	}
}
