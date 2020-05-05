package symphony.bm.cir.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import symphony.bm.cir.rules.namespaces.Namespace;

import java.util.List;

@Slf4j
public class Rule implements MessageHandler {
    @Id @JsonIgnore private String _id; // for mongoDB
    
    @Setter @Getter private String rid;
    @Setter @Getter private String name;
    @Setter @Getter private List<Namespace> namespaces;
    @Setter @Getter private String condition;
    @Setter @Getter private String actions;
    
    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private MessageChannel outboundChannel;
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_topic", String.class);
        log.error(topic);
    }
}
