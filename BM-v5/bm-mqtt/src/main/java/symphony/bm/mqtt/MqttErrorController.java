package symphony.bm.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMessage;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MqttErrorController implements MessageHandler {
    private MessageChannel outbound;
    private ObjectMapper objectMapper;
    
    private final String errorTopic;

    public MqttErrorController(@Qualifier("mqttOutboundChannel") MessageChannel outbound, ObjectMapper objectMapper,
                               @Value("${mqtt.topic.error}") String errorTopic) {
        this.outbound = outbound;
        this.objectMapper = objectMapper;
        this.errorTopic = errorTopic;
    }

    @SneakyThrows
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        MessagingException e = (MessagingException) message.getPayload();
        try {
            MqttMessage mqttMsg = (MqttMessage) e.getCause().getCause();
            Map<String, Object> headers = new HashMap<>();
            headers.put("mqtt_topic", mqttMsg.getTopic());
            log.error(e.getMessage());
            outbound.send(new GenericMessage<>(objectMapper.writeValueAsString(mqttMsg.getMsg()), headers));
        } catch (ClassCastException | NullPointerException exc) { // cause was not a thrown MqttMessage
            MicroserviceUnsuccessfulMessage msg = new MicroserviceUnsuccessfulMessage(e.getMessage());
            Map<String, Object> headers = new HashMap<>();
            headers.put("mqtt_topic", errorTopic);
            log.error(e.getMessage());
            outbound.send(new GenericMessage<>(objectMapper.writeValueAsString(msg), headers));
        }
    }
}
