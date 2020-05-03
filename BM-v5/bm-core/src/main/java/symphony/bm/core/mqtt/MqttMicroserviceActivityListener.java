package symphony.bm.core.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.messages.MicroserviceMessage;

import java.util.*;

@Slf4j
public class MqttMicroserviceActivityListener implements ActivityListener {
    private final String microserviceURL;
    
    private final HashMap<Object, String> statesWaitingToUpdate = new HashMap<>();
    private final HashMap<Object, StateUpdater> updaters = new HashMap<>();
    private final Timer timer = new Timer(MqttMicroserviceActivityListener.class.getSimpleName());
    
    public MqttMicroserviceActivityListener(String bmURL, String bmMqttMicroservicePort) {
        this.microserviceURL = bmURL + ":" + bmMqttMicroservicePort;
    }
    
    private void scheduleUpdate(Object obj, String url) {
        if (updaters.containsKey(obj)) {
            updaters.get(obj).cancel();
        }
        StateUpdater updater = new StateUpdater();
        statesWaitingToUpdate.put(obj, url);
        updaters.put(obj, updater);
        timer.schedule(updater, 10);
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
        scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
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
        scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
    }
    
    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
        log.debug("Adding Thing " + thing.getUid() + " to Group " + group.getGid() + " in MQTT...");
//        RestTemplate restTemplate = new RestTemplate();
//        MicroserviceMessage response = restTemplate.postForObject(
//                microserviceURL + "/things/" + thing.getUid() + "/parentGroups",
//                thing.getParentGroups(), MicroserviceMessage.class);
//
//        assert response != null;
//        logResponse(response);
        scheduleUpdate(thing.getParentGroups(), microserviceURL + "/things/" + thing.getUid() + "/parentGroups");
        scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
    }
    
    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
        log.debug("Removing Thing " + thing.getUid() + " from Group " + group.getGid() + " in MQTT...");
//        RestTemplate restTemplate = new RestTemplate();
//        MicroserviceMessage response = restTemplate.postForObject(
//                microserviceURL + "/things/" + thing.getUid() + "/parentGroups",
//                thing.getParentGroups(), MicroserviceMessage.class);
//
//        assert response != null;
//        logResponse(response);
        scheduleUpdate(thing.getParentGroups(), microserviceURL + "/things/" + thing.getUid() + "/parentGroups");
        scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
    }
    
    @Override
    public void thingDeleted(Thing thing) {
        log.debug("Deleting Thing " + thing.getUid() + " state representation in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(microserviceURL + "/things/" + thing.getUid());
        log.debug("Thing state representation deleted");
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
        log.debug("Forwarding Attribute " + attribute.getAid() + "/" + attribute.getAid() + " " + fieldName
                + " update to MQTT microservice...");
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(
                microserviceURL + "/things/" + attribute.getThing() + "/attributes/" + attribute.getAid()
                        + "/" + fieldName,
                fieldValue, MicroserviceMessage.class);
    
        assert response != null;
        logResponse(response);
        scheduleUpdate(attribute, microserviceURL + "/things/" + attribute.getThing() + "/attributes/" + attribute.getAid());
    }
    
    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
        log.debug("Forwarding Attribute " + thing.getUid() + "/" + attribute.getAid() + " state representation in MQTT...");
        scheduleUpdate(attribute, microserviceURL + "/things/" + thing.getUid() + "/attributes/" + attribute.getAid());
    }
    
    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
        log.debug("Deleting Attribute " + thing.getUid() + "/" + attribute.getAid() + " state representation in MQTT...");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(microserviceURL + "/things/" + thing.getUid() + "/attributes/" + attribute.getAid());
        log.debug("Attribute state representation deleted");
    }
    
    private class StateUpdater extends TimerTask {
        @Override
        public void run() {
            for (Object obj : statesWaitingToUpdate.keySet()) {
                String url = statesWaitingToUpdate.remove(obj);
                log.debug("Updating " + url + " state representation in MQTT...");
                RestTemplate restTemplate = new RestTemplate();
                MicroserviceMessage response = restTemplate.postForObject(url, obj, MicroserviceMessage.class);
                assert response != null;
                logResponse(response);
                updaters.remove(obj);
            }
        }
    }
}
