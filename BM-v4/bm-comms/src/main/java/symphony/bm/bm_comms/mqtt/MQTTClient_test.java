package symphony.bm.bm_comms.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTClient_test  {
	private static final Logger LOG = LoggerFactory.getLogger("MQTTCLIENTTEST");

	public MQTTClient_test(MQTTClient client) {
		LOG.error(String.valueOf(client.isConnected()));
	}
}
