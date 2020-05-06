package symphony.bm.cir.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionHandler implements MessageHandler {
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        Exception e = (Exception) message.getPayload();
        log.error(e.getMessage(), e);
    }
}
