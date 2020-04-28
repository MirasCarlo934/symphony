package symphony.bm.mqtt;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttErrorController implements MessageHandler {
    private MessageChannel outbound;

    public MqttErrorController(@Qualifier("mqttOutboundChannel") MessageChannel outbound) {
        this.outbound = outbound;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        MessagingException e = (MessagingException) message.getPayload();
        log.error("MessagingException:", e);
    }
}
