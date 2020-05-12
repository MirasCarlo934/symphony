package symphony.bm.data.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.ResourceDataController;

@Component
@AllArgsConstructor
@Slf4j
public class MqttListener implements MessageHandler {
    private final ObjectMapper objectMapper;
    private final ResourceDataController resourceDataController;
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        assert topic != null;
    
        String[] topicLevels = topic.split("/");
        if (checkIfThingTopic(topic)) {
            try {
                Thing thing = objectMapper.readValue(payload, Thing.class);
                resourceDataController.addThing(thing);
            } catch (JsonProcessingException e) {
                throw new MessagingException(e.getMessage(), e);
            }
        }
    }
    
    private boolean checkIfThingTopic(String topic) {
        String[] topicLevels = topic.split("/");
        return topicLevels.length == 2;
    }
}
