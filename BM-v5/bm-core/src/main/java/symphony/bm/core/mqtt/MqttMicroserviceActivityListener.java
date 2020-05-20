package symphony.bm.core.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.messages.MicroserviceMessage;

import java.util.*;

@Slf4j
public class MqttMicroserviceActivityListener implements ActivityListener {
    private final String microserviceURL;
    private SuperGroup superGroup;

    // (k:Object, v:url)
    private final HashMap<Object, String> statesWaitingToUpdate = new HashMap<>();
    private final HashMap<Object, Timer> updaters = new HashMap<>();
    
    public MqttMicroserviceActivityListener(String bmURL, String bmMqttMicroservicePort) {
        this.microserviceURL = bmURL + ":" + bmMqttMicroservicePort;
    }

    public void setSuperGroup(SuperGroup superGroup) {
        this.superGroup = superGroup;
    }
    
    private void scheduleUpdate(Object obj, String url) {
        if (updaters.containsKey(obj)) {
            updaters.get(obj).cancel();
        }
        Timer timer = new Timer(StateUpdater.class.getSimpleName());
        statesWaitingToUpdate.put(obj, url);
        updaters.put(obj, timer);
        timer.schedule(new StateUpdater(obj), 100);
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
        RestTemplate restTemplate = new RestTemplate();
        MicroserviceMessage response = restTemplate.postForObject(microserviceURL, thing, MicroserviceMessage.class);
        assert response != null;
        logResponse(response);
//        scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
    }
    
    @Override
    public void thingUpdated(Thing thing, String fieldName, Object fieldValue) {
        try {
            log.debug("Forwarding Thing " + thing.getUid() + " " + fieldName + " update to MQTT microservice...");
            RestTemplate restTemplate = new RestTemplate();
            MicroserviceMessage response = restTemplate.postForObject(
                    microserviceURL + "/things/" + thing.getUid() + "/" + fieldName,
                    fieldValue, MicroserviceMessage.class);
            
            assert response != null;
            logResponse(response);
//            scheduleUpdate(thing, microserviceURL + "/things/" + thing.getUid());
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
        log.debug("Adding Thing " + thing.getUid() + " to Group " + group.getGid() + " in MQTT...");
        scheduleUpdate(thing.getParentGroups(), microserviceURL + "/things/" + thing.getUid() + "/parentGroups");
    }
    
    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
        log.debug("Removing Thing " + thing.getUid() + " from Group " + group.getGid() + " in MQTT...");
        scheduleUpdate(thing.getParentGroups(), microserviceURL + "/things/" + thing.getUid() + "/parentGroups");
    }
    
    @Override
    public void thingDeleted(Thing thing) {
        try {
            log.debug("Deleting Thing " + thing.getUid() + " state representation in MQTT...");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(microserviceURL + "/things/" + thing.getUid());
            log.debug("Thing state representation deleted");
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
        }
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
        try {
            log.debug("Forwarding Attribute " + attribute.getAid() + "/" + attribute.getAid() + " " + fieldName
                    + " update to MQTT microservice...");
            Thing thing = superGroup.getThingRecursively(attribute.getThing());
            RestTemplate restTemplate = new RestTemplate();
            MicroserviceMessage response = restTemplate.postForObject(
                    microserviceURL + "/things/" + attribute.getThing() + "/attributes/" + attribute.getAid()
                            + "/" + fieldName,
                    fieldValue, MicroserviceMessage.class);

            assert response != null;
            logResponse(response);
//            scheduleUpdate(thing, microserviceURL + "/things/" + attribute.getThing());
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
        log.debug("Forwarding Attribute " + thing.getUid() + "/" + attribute.getAid() + " state representation in MQTT...");
        scheduleUpdate(attribute, microserviceURL + "/things/" + thing.getUid() + "/attributes/" + attribute.getAid());
    }
    
    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
        try {
            log.debug("Deleting Attribute " + thing.getUid() + "/" + attribute.getAid() + " state representation in MQTT...");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(microserviceURL + "/things/" + thing.getUid() + "/attributes/" + attribute.getAid());
            log.debug("Attribute state representation deleted");
        } catch (RestClientException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private class StateUpdater extends TimerTask {
        Object objectToUpdate;
        
        StateUpdater(Object objectToUpdate) {
            this.objectToUpdate = objectToUpdate;
        }
        
        @Override
        public void run() {
            try {
                String url = statesWaitingToUpdate.remove(objectToUpdate);
                updaters.remove(objectToUpdate);
                log.debug("Updating " + url + " state representation in MQTT...");
                RestTemplate restTemplate = new RestTemplate();
                MicroserviceMessage response = restTemplate.postForObject(url, objectToUpdate, MicroserviceMessage.class);
                assert response != null;
                logResponse(response);
            } catch (RestClientException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
