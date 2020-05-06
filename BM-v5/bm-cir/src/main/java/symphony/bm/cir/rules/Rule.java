package symphony.bm.cir.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.iot.Thing;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Rule implements MessageHandler {
    private final Logger log;
    @Id @JsonIgnore private String _id; // for mongoDB
    
    @Setter @Getter private String rid;
    @Setter @Getter private String name;
    @Setter @Getter private List<Namespace> namespaces;
    @Setter @Getter private String condition;
    @Setter @Getter private String actions;

    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private MessageChannel outboundChannel;
    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private ObjectMapper objectMapper;
    @JsonIgnore @Transient private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    @JsonIgnore @Transient private List<Namespace> missing;

    public Rule(String rid, String name, List<Namespace> namespaces, String condition, String actions) {
        this.rid = rid;
        this.name = name;
        this.namespaces = namespaces;
        this.condition = condition;
        this.actions = actions;
        log = LoggerFactory.getLogger(Rule.class.getName() + "." + rid);
        missing = new Vector<>(namespaces);

        timer.scheduleAtFixedRate(() -> {
            if (!missing.isEmpty()) {
                missing.forEach( namespace -> log.warn("No resource " + namespace.getURL() + " found for namespace  "
                        + namespace.getName()));
                log.warn("Rule will not run");
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String payload = (String) message.getPayload();

        if (!isActive() || payload.isEmpty()) {
            buildNamespacesFromMqtt(message);
        }
        if (!isActive()) {
            log.warn("Rule inactive. Check namespace resources");
        }

        
    }

    @SneakyThrows
    private void buildNamespacesFromMqtt(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payload = (String) message.getPayload();
        assert topic != null;

        Iterator<Namespace> i = missing.iterator();
        while (i.hasNext()) {
            Namespace namespace = i.next();
            if (topic.equals("things/"+namespace.getThingURL())) {
                if (payload.isEmpty()) {
                    log.warn("Resource " + namespace.getURL() + " deleted (Namespace:  "
                            + namespace.getName() + "). Rule will not run.");
                    continue;
                }
                Thing thing = objectMapper.readValue(payload, Thing.class);

                if (namespace.isThing()) {
                    namespace.setResource(thing);
                } else {
                    namespace.setResource(thing.getAttribute(namespace.getAid()));
                }

                if (namespace.getResource() != null) {
                    log.info("Namespace " + namespace.getName() + " successfully linked to resource "
                            + namespace.getURL());
                    i.remove();
                } else {
                    log.warn("No resource " + namespace.getURL() + " found for namespace  "
                            + namespace.getName());
                }
            }
        }
    }
    
    private Namespace getNamespaceFromTopic(String topic) {
        for (Namespace namespace : namespaces) {
            if ( topic.equals("things/"+namespace.getURL()) ) {
                return namespace;
            }
        }
        return null;
    }

    public boolean isActive() {
        return missing.isEmpty();
    }
}
