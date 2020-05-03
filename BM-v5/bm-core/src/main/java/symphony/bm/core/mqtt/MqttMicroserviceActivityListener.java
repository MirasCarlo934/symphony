package symphony.bm.core.mqtt;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.messages.MicroserviceMessage;

import java.util.Map;

@Slf4j
public class MqttMicroserviceActivityListener implements ActivityListener {
    private final String microserviceURL;
    
    public MqttMicroserviceActivityListener(String bmURL, String bmMqttMicroservicePort) {
        this.microserviceURL = bmURL + ":" + bmMqttMicroservicePort;
    }
    
    private void updateThingStateRepresentation(Thing thing) {
        log.debug("Updating Thing state representation in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(
                microserviceURL + "/things/" + thing.getUid(),
                thing, MicroserviceMessage.class);
    
        assert response != null;
        logResponse(response);
    }
    
    private void logResponse(MicroserviceMessage response) {
        if (response.isSuccess()) {
            log.debug(response.getMessage());
        } else {
            log.error(response.getMessage());
        }
    }
    
    @Override
    public void thingCreated(Thing thing) {
        log.debug("Forwarding newly-created Thing " + thing.getUid() + " to MQTT microservice...");
        updateThingStateRepresentation(thing);
    }
    
    @Override
    public void thingUpdated(Thing thing, String fieldName, Object fieldValue) {
        log.debug("Forwarding Thing " + thing.getUid() + " " + fieldName + " update to MQTT microservice...");
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(
                microserviceURL + "/things/" + thing.getUid() + "/" + fieldName,
                fieldValue, MicroserviceMessage.class);
        
        assert response != null;
        logResponse(response);
        updateThingStateRepresentation(thing);
    }
    
    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
        log.debug("Adding Thing " + thing.getUid() + " to Group " + group.getGid() + " in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(
                microserviceURL + "/things/" + thing.getUid() + "/parentGroups",
                thing.getParentGroups(), MicroserviceMessage.class);
    
        assert response != null;
        logResponse(response);
        updateThingStateRepresentation(thing);
    }
    
    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
        log.debug("Removing Thing " + thing.getUid() + " from Group " + group.getGid() + " in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(
                microserviceURL + "/things/" + thing.getUid() + "/parentGroups",
                thing.getParentGroups(), MicroserviceMessage.class);
    
        assert response != null;
        logResponse(response);
        updateThingStateRepresentation(thing);
    }
    
    @Override
    public void thingDeleted(Thing thing) {
        log.debug("Deleting Thing " + thing.getUid() + " state representation in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(microserviceURL + "/things/" + thing.getUid());
        log.debug("Thing deleted");
    }
    
    @Override
    public void groupCreated(Group group) {
    
    }
    
    @Override
    public void groupUpdated(Group group, String fieldName, Object fieldValue) {
    
    }
    
    @Override
    public void groupAddedToGroup(Group group, Group parent) {
    
    }
    
    @Override
    public void groupRemovedFromGroup(Group group, Group parent) {
    
    }
    
    @Override
    public void groupDeleted(Group group) {
    
    }
    
    @Override
    public void attributeUpdated(Attribute attribute, String fieldName, Object fieldValue) {
    
    }
    
//    @Override
//    public void attributeUpdatedValue(Attribute attribute, Object value) {
//
//    }
    
    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
    
    }
    
    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
    
    }
}
