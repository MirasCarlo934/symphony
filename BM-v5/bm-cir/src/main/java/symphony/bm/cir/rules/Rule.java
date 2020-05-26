package symphony.bm.cir.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.IotResource;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.attribute.AttributeMode;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Rule implements MessageHandler {
    @Transient private final Logger log;
    @Id @JsonIgnore private String _id; // for mongoDB
    
    @Setter @Getter private String rid;
    @Setter @Getter private String description;
    @Setter @Getter private List<Namespace> namespaces;
    @Setter @Getter private String condition;
    @Setter @Getter private String actions;

    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private String resourceAPI_URL;
    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private ActivityListenerManager activityListenerManager;
    @JsonIgnore @Transient @Setter(AccessLevel.PACKAGE) private ObjectMapper objectMapper;
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
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payloadStr = (String) message.getPayload();
        assert topic != null;
        log.debug("Message received from topic " + topic);
        log.debug("Message: " + payloadStr);

        if (!isActive() || payloadStr.isEmpty()) {
            configureNamespacesFromMqtt(message);
        }
        if (!isActive()) {
            log.warn("Rule inactive. Check namespace resources");
            return;
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
    
            if (namespace.isCondition()) {
                MVELRule r = new MVELRule()
                        .name(rid)
                        .description(description)
                        .when(condition)
                        .then(actions);
                Rules rules = new Rules();
                Facts facts = new Facts();
                namespaces.forEach(n -> {
                    n.getResource().setActivityListenerManager(activityListenerManager);
                    if (n.isCondition() || n.isThing() || ((Attribute) n.getResource()).getMode() == AttributeMode.controllable) {
                        facts.put(n.getName(), n.getResource());
                    } else {
                        log.warn("Namespace attribute " + n.getURL() + " is not a condition and is not controllable. " +
                                "It will not be updated.");
                    }
                });
                rules.register(r);
                engine.fire(rules, facts);
            }
        }
    }

    @SneakyThrows
    /**
     * Retrieves and links namespace resources from resource API.
     */
    void linkNamespacesToResources() {
        RestTemplate restTemplate = new RestTemplate();
        Iterator<Namespace> i = missing.iterator();

        while (i.hasNext()) {
            Namespace namespace = i.next();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        resourceAPI_URL + "/things/" + namespace.getURL() + "?restful=false",
                        String.class
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    IotResource resource;
                    try {
                        resource = objectMapper.readValue(response.getBody(), Thing.class);
                    } catch (JsonMappingException e) {
                        resource = objectMapper.readValue(response.getBody(), Attribute.class);
                    }
                    resource.setActivityListenerManager(activityListenerManager);
                    namespace.setResource(resource);
                    i.remove();
                } else {
                    log.warn("No resource " + namespace.getURL() + " found for namespace " + namespace.getName());
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    log.warn("No resource " + namespace.getURL() + " found for namespace " + namespace.getName());
                }
            } catch (RestClientException e) {
                log.error("Error in retrieving resource " + namespace.getURL(), e);
            }
        }

        if (missing.isEmpty()) {
            log.info("Rule is running");
        } else {
            log.warn("Rule will not run");
        }
    }

    @SneakyThrows
    private void configureNamespacesFromMqtt(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payload = (String) message.getPayload();
        assert topic != null;

        for (Namespace namespace : missing) {
            if (topic.equals("things/" + namespace.getThingURL())) {
                if (payload.isEmpty()) { // Thing is deleted, namespace is no longer valid
                    log.warn("Resource " + namespace.getURL() + " deleted (Namespace: "
                            + namespace.getName() + "). Rule will not run.");
                    missing.add(namespace);
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
    
    class RuleActiveNotifier implements Runnable {
        boolean notified = false;

        @Override
        public void run() {
            if (!missing.isEmpty() && !notified) {
                missing.forEach( namespace -> log.warn("No resource " + namespace.getURL() + " found for namespace  "
                        + namespace.getName()));
                log.warn("Rule will not run");
                notified = true;
            }
        }
    }
}
