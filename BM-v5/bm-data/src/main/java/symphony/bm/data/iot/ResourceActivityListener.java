package symphony.bm.data.iot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.data.iot.attribute.AttributeValueRecord;
import symphony.bm.data.iot.thing.ThingActiveState;
import symphony.bm.data.repositories.AttributeValueRecordRepository;
import symphony.bm.data.repositories.ThingActiveStateRepository;

import java.util.Calendar;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResourceActivityListener implements ActivityListener {
    private final ThingActiveStateRepository tasRepository;
    private final AttributeValueRecordRepository avrRepository;
    
    @Override
    public void thingCreated(Thing thing) {
    
    }
    
    @Override
    public void thingUpdated(Thing thing, String fieldName, Object fieldValue) {
        if (fieldName.equals("active")) {
            tasRepository.save(new ThingActiveState(thing.getUid(), Calendar.getInstance().getTime(),
                    Boolean.parseBoolean(fieldValue.toString())));
        }
    }
    
    @Override
    public void thingAddedToGroup(Thing thing, Group group) {
    
    }
    
    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {
    
    }
    
    @Override
    public void thingDeleted(Thing thing) {
        if (thing.isActive()) {
            thingUpdated(thing, "active", false);
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
        if (fieldName.equals("value")) {
            Date now = Calendar.getInstance().getTime();
            AttributeValueRecord avr = new AttributeValueRecord(attribute.getAid(), attribute.getThing(), now,
                    fieldValue);
            avrRepository.save(avr);
        }
    }
    
    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {
    
    }
    
    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {
    
    }
}
