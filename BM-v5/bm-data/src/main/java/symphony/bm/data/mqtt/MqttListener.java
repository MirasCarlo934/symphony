package symphony.bm.data.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
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
import symphony.bm.data.iot.ResourceDataController;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttListener implements MessageHandler {
    private final ObjectMapper objectMapper;
    private final ResourceDataController resourceDataController;
    private final ActivityListenerManager activityListenerManager;
    
    @SneakyThrows
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        assert topic != null;
    
        String[] topicLevels = topic.split("/");
        if (checkIfThingTopic(topic)) { // new Thing added to BeeHive
            Thing thing = objectMapper.readValue(payload, Thing.class);
            if (resourceDataController.getThing(thing.getUid()) == null) {
                resourceDataController.addThing(thing);
            }
//            thing.setActivityListenerManager(activityListenerManager);
//            resourceDataController.addThing(thing);
        } else if (checkIfAttributeTopic(topic)) {
            Thing thing = resourceDataController.getThing(topicLevels[1]);
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
