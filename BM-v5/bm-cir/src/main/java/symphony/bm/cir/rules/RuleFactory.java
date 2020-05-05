package symphony.bm.cir.rules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RuleFactory {
    private MongoOperations mongo;
    private SubscribableChannel inbound;
    private MessageChannel outbound;
    
    private List<Rule> rules;
    
    public RuleFactory(MongoOperations mongo,
                       @Qualifier("mqttThingsChannel") SubscribableChannel inbound,
                       @Qualifier("mqttCoreChannel") MessageChannel outbound) {
        this.mongo = mongo;
        this.inbound = inbound;
        this.outbound = outbound;
        
        loadRulesFromDB();
    }
    
    public void loadRulesFromDB() {
        log.debug("Loading rules from DB...");
        rules = mongo.findAll(Rule.class);
        
        for (Rule rule : rules) {
            rule.setOutboundChannel(outbound);
            inbound.subscribe(rule);
        }
        
        log.info(rules.size() + " rules loaded from DB");
    }
}
