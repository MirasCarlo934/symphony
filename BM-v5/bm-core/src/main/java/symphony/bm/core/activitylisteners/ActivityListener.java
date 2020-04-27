package symphony.bm.core.activitylisteners;

import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.Attribute;

import java.util.Map;

public interface ActivityListener {
    void thingCreated(Thing thing);
    void thingUpdated(Thing thing, Map<String, Object> updatedFields);
    void thingAddedToGroup(Thing thing, Group group);
    void thingRemovedFromGroup(Thing thing, Group group);
    void thingDeleted(Thing thing);

    void groupCreated(Group group);
    void groupUpdated(Group group, Map<String, Object> updatedFields);
    void groupAddedToGroup(Group group, Group parent);
    void groupRemovedFromGroup(Group group, Group parent);
    void groupDeleted(Group group);

    void attributeUpdated(Attribute attribute, Map<String, Object> updatedFields);
    void attributeUpdatedValue(Attribute attribute, Object value);
    void attributeAddedToThing(Attribute attribute, Thing thing);
    void attributeRemovedFromThing(Attribute attribute, Thing thing);
}