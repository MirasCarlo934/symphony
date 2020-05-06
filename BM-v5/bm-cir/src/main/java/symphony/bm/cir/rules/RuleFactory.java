package symphony.bm.cir.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListenerManager;

import java.util.List;

@Component
@Slf4j
public class RuleFactory {
    private final MongoOperations mongo;
    private final SubscribableChannel inbound;
    private final ObjectMapper objectMapper;
    private final ActivityListenerManager activityListenerManager;
    
    private List<Rule> rules;
    
    public RuleFactory(MongoOperations mongo, ObjectMapper objectMapper,
                       @Qualifier("mqttThingsChannel") SubscribableChannel inbound,
                       ActivityListenerManager activityListenerManager) {
        this.mongo = mongo;
        this.inbound = inbound;
        this.objectMapper = objectMapper;
        this.activityListenerManager = activityListenerManager;
        
        loadRulesFromDB();
    }
    
    public void loadRulesFromDB() {
        log.debug("Loading rules from DB...");
        rules = mongo.findAll(Rule.class);
        
        for (Rule rule : rules) {
            rule.setActivityListenerManager(activityListenerManager);
            rule.setObjectMapper(objectMapper);
            inbound.subscribe(rule);
        }
        
        log.info(rules.size() + " rules loaded from DB");
    }
}
