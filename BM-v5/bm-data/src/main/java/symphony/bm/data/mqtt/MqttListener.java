package symphony.bm.data.mqtt;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.ResourceRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttListener implements MessageHandler {
    private final ObjectMapper objectMapper;
    private final ResourceRepository resourceRepository;
    
    @SneakyThrows
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        assert topic != null;
    
        String[] topicLevels = topic.split("/");
        if (checkIfThingTopic(topic)) {
            Thing thing;
            try { // new Thing added to BeeHive
                thing = objectMapper.readValue(payload, Thing.class);
                if (resourceRepository.getThing(thing.getUid()) == null) {
                    resourceRepository.addThing(thing);
                }
            } catch (JsonMappingException e) {
                if (topicLevels.length == 2 && payload.isEmpty()) {
                    resourceRepository.deleteThing(topicLevels[1]);
                } else if (topicLevels.length == 3) { // MQTT message is a Thing field update
                    thing = resourceRepository.getThing(topicLevels[1]);
                    if (thing != null) {
                        thing.update(topicLevels[2], payload);
                    }
                }
            }
        } else if (checkIfAttributeTopic(topic)) {
            Thing thing = resourceRepository.getThing(topicLevels[1]);
            if (thing == null) {
                throw new MessagingException("Thing " + topicLevels[1] + " does not exist");
            }
            Attribute attr = thing.getAttribute(topicLevels[3]);
            if (attr == null) {
                throw new MessagingException("Attribute " + topicLevels[3] + " does not exist");
            }
            if (topicLevels.length == 5) {
                attr.update(topicLevels[4], payload);
            }
        }
    }
    
    private boolean checkIfThingTopic(String topic) {
        String[] topicLevels = topic.split("/");
        return topicLevels.length >= 2 && topicLevels.length < 4;
    }
    
    private boolean checkIfAttributeTopic(String topic) {
        String[] topicLevels = topic.split("/");
        return topicLevels.length >= 4;
    }
}
