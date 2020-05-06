package symphony.bm.cir.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.iot.Attribute;
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
    @Setter @Getter private String description;
    @Setter @Getter private List<Namespace> namespaces;
    @Setter @Getter private String condition;
    @Setter @Getter private String actions;

    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private ActivityListenerManager activityListenerManager;
    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private ObjectMapper objectMapper;
    @JsonIgnore @Transient private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    @JsonIgnore @Transient private final List<Namespace> missing;
    @JsonIgnore @Transient private final RulesEngine engine = new DefaultRulesEngine();

    public Rule(String rid, String description, List<Namespace> namespaces, String condition, String actions) {
        this.rid = rid;
        this.description = description;
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
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payloadStr = (String) message.getPayload();
        assert topic != null;

        if (!isActive() || payloadStr.isEmpty()) {
            buildNamespacesFromMqtt(message);
        }
        if (!isActive()) {
            log.warn("Rule inactive. Check namespace resources");
        }

        Namespace namespace = getNamespaceFromTopic(topic);
        if (namespace != null && !topic.equals("things/" + namespace.getURL())) {
            String[] topicLevels = topic.split("/");
            String field = topicLevels[topicLevels.length - 1];
            try {
                namespace.getResource().update(field, message.getPayload());
            } catch (Exception e) {
                String error = "Unable to change " + namespace.getURL() + " " + field + " to " + message.getPayload();
                log.error(error, e);
                throw new MessagingException(error, e);
            }
        }


        MVELRule r = new MVELRule()
                .name(rid)
                .description(description)
                .when(condition)
                .then(actions);
        Rules rules = new Rules();
        Facts facts = new Facts();
        namespaces.forEach( n -> facts.put(n.getName(), n.getResource()));
        rules.register(r);
        engine.fire(rules, facts);

//        namespaces.forEach( n -> {
//            log.error( ((Attribute) n.getResource()).getValue().toString() + " : " + n.getAid() );
//        });
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
                thing.setActivityListenerManager(activityListenerManager);

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
            if ( topic.contains("things/"+namespace.getURL()) ) {
                return namespace;
            }
        }
        return null;
    }

    public boolean isActive() {
        return missing.isEmpty();
    }
}
