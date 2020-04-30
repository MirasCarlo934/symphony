package symphony.bm.cir.rules;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
public class RulesManager {
    private final MongoOperations mongo;
    private final ObjectMapper objectMapper;
//    private final List<Thing> thingsTracking = new Vector<>();
    private final List<Attribute> attributesTracking = new Vector<>();
    private List<Rule> rules;

    private final String bmURL;
    private final String bmCorePort;

    public RulesManager(MongoOperations mongo, ObjectMapper objectMapper,
                        @Value("${bm.url}") String bmURL,
                        @Value("${bm.port.core}") String bmCorePort) {
        this.mongo = mongo;
        this.objectMapper = objectMapper;
        this.bmURL = bmURL;
        this.bmCorePort = bmCorePort;

        startup();
    }

    private void startup() {
        rules = mongo.findAll(Rule.class);
        int activeRules = rules.size();

        for (Rule rule : rules) {
            log.debug("Setting up rule " + rule.getRid() + " (" + rule.getName() + ")...");
//            HashMap<String, Thing> thingFacts = new HashMap<>();
            HashMap<String, Attribute> attributeFacts = new HashMap<>();
            boolean active = true;

            for (Namespace namespace : rule.getNamespaces()) {
                try {
//                    if (namespace.getAid() != null) {
                    Attribute attribute = getAttributeFromCore(namespace.getUid(), namespace.getAid());
                    attributeFacts.put(attribute.getAid(), attribute);
//                    } else {
//                        Thing thing = getThingFromCore(namespace.getUid());
//                        thingFacts.put(thing.getUid(), thing);
//                    }
                } catch (JsonMappingException jsonMappingException) {
                    log.warn("Rule " + rule.getRid() + " has a namespace '" + namespace.getName()
                            + "' that no longer exists. Not activating...", jsonMappingException);
                    active = false;
                    break;
                } catch (IOException e) {
                    log.warn("Exception in retrieving attribute " + namespace.getUid() + "/" + namespace.getAid()
                            + ". Rule " + rule.getRid() + " not activated", e);
                    active = false;
                    break;
                }
            }

            if (active) {
//                thingFacts.forEach( (s, thing) -> {
//                    thing.addActivityListener(rule);
//                    thingsTracking.add(thing);
//                    // this SHOULD NOT be done since it assigns the rule to ALL attributes
////                    thing.getCopyOfAttributeList().forEach( attribute -> attribute.addActivityListener(rule) );
//                });
                attributeFacts.forEach( (s, attribute) -> {
                    attribute.addActivityListener(rule);
                    attributesTracking.add(attribute);
                });
//                rule.setThingFacts(thingFacts);
                rule.setAttributeFacts(attributeFacts);
            } else {
                activeRules--;
            }
        }

        log.info(activeRules + " rules active");
    }

//    public Thing getThing(String uid) {
//        for (Thing t : thingsTracking) {
//            if (t.getUid().equals(uid)) return t;
//        }
//        return null;
//    }

    public Attribute getAttribute(String aid) {
        for (Attribute a : attributesTracking) {
            if (a.getAid().equals(aid)) return a;
        }
        return null;
    }

//    public void untrackThing(String uid) {
//        thingsTracking.removeIf( thing -> thing.getUid().equals(uid) );
//    }

    public void untrackAttribute(String aid) {
        attributesTracking.removeIf( attribute -> attribute.getAid().equals(aid) );
    }

//    private Thing getThingFromCore(String uid) throws IOException {
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        HttpGet request = new HttpGet(bmURL + ":" + bmCorePort + "/things/" + uid);
//        HttpResponse response = httpClient.execute(request);
//        return objectMapper.readValue(EntityUtils.toString(response.getEntity()), Thing.class);
//    }

    private Attribute getAttributeFromCore(String uid, String aid) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(bmURL + ":" + bmCorePort + "/things/" + uid + "/attributes/" + aid);
        HttpResponse response = httpClient.execute(request);
        return objectMapper.readValue(EntityUtils.toString(response.getEntity()), Attribute.class);
    }
}
