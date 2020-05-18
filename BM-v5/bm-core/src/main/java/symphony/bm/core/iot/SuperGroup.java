package symphony.bm.core.iot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.activitylisteners.ActivityListenerManager;

import java.util.List;

@Component
@Slf4j
public class SuperGroup extends Group {
    private final MongoOperations mongo;

    public SuperGroup(MongoTemplate mongoTemplate) {
        super("", "Super Group");
        this.mongo = mongoTemplate;

        buildContext();
        log.info("IOT context initialized");
    }

    private void buildContext() {
        List<Thing> thingList = mongo.findAll(Thing.class);
        List<Group> groupList = mongo.findAll(Group.class);
        List<Attribute> attributeList = mongo.findAll(Attribute.class);
        groupList.add(this);

        for (Attribute attribute : attributeList) {
            log.info("Attribute " + attribute.getThing() + "/" + attribute.getAid() + " retrieved from DB");
            getThingFromList(attribute.getThing(), thingList).getAttributes().add(attribute);
        }

        for (Thing thing : thingList) {
            log.info("Thing " + thing.getUid() + " retrieved from DB");
            if (thing.getParentGroups().isEmpty()) {
                this.things.add(thing);
            } else {
                for (String GID : thing.getParentGroups()) {
                    Group group = getGroupFromList(GID, groupList);
                    assert group != null;
                    group.things.add(thing);
                    thing.addParentGroup(group); // to add the Group object to its list of parent group objects
                }
            }
        }

        for (Group group : groupList) {
            log.info("Group " + group.getGid() + " retrieved from DB");
            if (group.getParentGroups().isEmpty() && !group.equals(this)) {
                this.groups.add(group);
            } else {
                for (String GID : group.getParentGroups()) {
                    Group parent = getGroupFromList(GID, groupList);
                    assert parent != null;
                    parent.groups.add(group);
                    group.addParentGroup(parent);
                }
            }
        }

        groupList.remove(this);

        printContentCount();
    }
    
    @Override
    public Group getGroupRecursively(String GID) {
        if (GID == null || GID.isEmpty()) {
            return this;
        } else {
            return super.getGroupRecursively(GID);
        }
    }

    @Autowired
    @Override
    public void setActivityListenerManager(ActivityListenerManager activityListenerManager) {
        super.setActivityListenerManager(activityListenerManager);
        getContainedThings().forEach( thing -> thing.setActivityListenerManager(activityListenerManager));
        getContainedGroups().forEach( group -> group.setActivityListenerManager(activityListenerManager));
    }

    public void printContentCount() {
        log.info(getContainedThings().size() + " things and " + getContainedGroups().size() + " groups currently exist");
    }

    private Thing getThingFromList(String UID, List<Thing> thingList) {
        for (Thing thing : thingList) {
            if (thing.getUid().equals(UID)) {
                return thing;
            }
        }
        return null;
    }

    private Group getGroupFromList(String GID, List<Group> groupList) {
        for (Group group : groupList) {
            if (group.getGid().equals(GID)) {
                return group;
            }
        }
        return null;
    }
}
