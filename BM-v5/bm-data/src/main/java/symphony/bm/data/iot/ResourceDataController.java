package symphony.bm.data.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.attribute.AttributeValueRecord;
import symphony.bm.data.iot.thing.ThingActiveState;
import symphony.bm.data.repositories.AttributeValueRecordRepository;
import symphony.bm.data.repositories.ThingActiveStateRepository;

import java.util.*;

@Component
@Slf4j
public class ResourceDataController {
    private final ThingActiveStateRepository tasRepository;
    private final AttributeValueRecordRepository avrRepository;
    
    private final ActivityListenerManager activityListenerManager;
    private final ObjectMapper objectMapper;
    private final String bmCoreURL;
    
    private Map<String, Thing> things = new HashMap<>();
    
    public ResourceDataController(ThingActiveStateRepository tasRepository,
                                  AttributeValueRecordRepository avrRepository,
                                  ActivityListenerManager activityListenerManager,
                                  ObjectMapper objectMapper,
                                  @Value("${bm.url}") String bmServerURL,
                                  @Value("${bm.port.core}") String bmCorePort) {
        this.tasRepository = tasRepository;
        this.avrRepository = avrRepository;
        this.activityListenerManager = activityListenerManager;
        this.objectMapper = objectMapper;
        this.bmCoreURL = bmServerURL + ":" + bmCorePort;
        
        getThingsFromCore();
    }
    
    private void getThingsFromCore() {
        RestTemplate restTemplate = new RestTemplate();
//        log.error(restTemplate.getForEntity(bmCoreURL + "/things?restful=false", String.class).getBody());
        Map[] thingMapList = restTemplate.getForEntity(bmCoreURL + "/things?restful=false", Map[].class).getBody();
        for (Map thingMap : thingMapList) {
            Thing thing = objectMapper.convertValue(thingMap, Thing.class);
            thing.setActivityListenerManager(activityListenerManager);
            things.put(thing.getUid(), thing);
        }
    }
    
    /**
     * Invoked ONLY when a new Thing is added to the BeeHive IoT universe.
     * @param thing The Thing object
     */
    public void addThing(Thing thing) {
        if (!things.containsKey(thing.getUid())) {
            thing.setActivityListenerManager(activityListenerManager);
            things.put(thing.getUid(), thing);
    
            ThingActiveState tas = tasRepository.findFirstByUid(thing.getUid());
            for (Attribute attr : thing.getAttributes()) {
                AttributeValueRecord avr = avrRepository.findFirstByThingAndAid(attr.getThing(), attr.getAid());
                if (avr == null || !attr.getDataType().checkValuesForEquality(avr.getValue(), attr.getValue())) {
                    Date now = Calendar.getInstance().getTime();
                    log.info("Saving new attribute value record for " + attr.getThing() + "/" + attr.getAid() +
                            " @ " + now);
                    AttributeValueRecord newAvr = new AttributeValueRecord(attr.getAid(), attr.getThing(), now,
                            attr.getValue());
                    avrRepository.save(newAvr);
                }
            }
            log.debug("Thing " + thing.getUid() + " added");
        }
    }
    
    public Thing getThing(String uid) {
        return things.get(uid);
    }
}
