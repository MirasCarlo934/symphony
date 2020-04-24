package symphony.bm.core.iot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListener;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@Slf4j
public class SuperGroup extends Group {
    private final MongoOperations mongo;

    public SuperGroup(MongoTemplate mongoTemplate, List<ActivityListener> activityListeners) {
        super("", "Super Group");
        this.mongo = mongoTemplate;
        setActivityListeners(activityListeners);

        buildContext();
        log.info("IOT context initialized");
    }

    private void buildContext() {
        List<Thing> thingList = mongo.findAll(Thing.class);
        List<Group> groupList = mongo.findAll(Group.class);
        groupList.add(this);

        for (Thing thing : thingList) {
            log.info("Thing " + thing.getUid() + " retrieved from DB");
            thing.setActivityListeners(activityListeners);
            if (thing.getParentGroups().isEmpty()) {
                this.things.add(thing);
            } else {
                for (String GID : thing.getParentGroups()) {
                    getGroupFromList(GID, groupList).things.add(thing);
                }
            }
        }

        for (Group group : groupList) {
            log.info("Group " + group.getGid() + " retrieved from DB");
            group.setActivityListeners(activityListeners);
            if (group.getParentGroups().isEmpty() && !group.equals(this)) {
                this.groups.add(group);
            } else {
                for (String GID : group.getParentGroups()) {
                    getGroupFromList(GID, groupList).groups.add(group);
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

    public void printContentCount() {
        log.info(getContainedThings().size() + " things and " + getContainedGroups().size() + " groups currently exists");
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
