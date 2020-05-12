package symphony.bm.data.iot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.mongodb.AttributeValueRecordRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceDataController {
    private final MongoOperations mongo;
    private final AttributeValueRecordRepository avrRepository;
    private Map<String, Thing> things = new HashMap<>();
    
    public void addThing(Thing thing) {
        log.debug("Thing " + thing.getUid() + " added");
        if (!things.containsKey(thing.getUid())) {
            things.put(thing.getUid(), thing);
            for (Attribute attr : thing.getAttributes()) {
                AttributeValueRecord avr = avrRepository.findFirstByThingAndAid(attr.getThing(), attr.getAid());
                if (avr == null || !attr.getDataType().checkValuesForEquality(avr.getValue(), attr.getValue())) {
                    Date now = Calendar.getInstance().getTime();
                    log.info("Saving new attribute value record for " + attr.getThing() + "/" + attr.getAid() +
                            " @ " + now);
                    AttributeValueRecord newAvr = new AttributeValueRecord(attr.getAid(), attr.getThing(), now,
                            attr.getValue());
                    mongo.save(newAvr);
                }
            }
        }
    }
    
    public Thing getThing(String uid) {
        return things.get(uid);
    }
}
