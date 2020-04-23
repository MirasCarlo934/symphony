package symphony.bm.core.iot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@Slf4j
public class SuperGroup extends Group {
    private final MongoOperations mongo;
    
    @Getter private List<Thing> thingList;
    @Getter private List<Group> groupList;

    public SuperGroup(MongoTemplate mongoTemplate) {
        super("", "Super Group");
        this.mongo = mongoTemplate;

        buildContext();
        log.info("IOT context initialized");
    }

    private void buildContext() {
        thingList = mongo.findAll(Thing.class);
        groupList = mongo.findAll(Group.class);
        groupList.add(this);

        for (Group group : groupList) {
            for (Thing thing : thingList) {
                if (thing.getParentGroups().isEmpty()) {
                    this.things.add(thing);
                } else {
                    for (String parentGID : thing.getParentGroups()) {
                        if (parentGID.equals(group.getGID())) {
                            group.getThings().add(thing);
                            break;
                        }
                    }
                }
            }
            for (Group g : groupList) {
                if (g.getParentGroups().isEmpty() && !g.getClass().equals(SuperGroup.class)) {
                    this.groups.add(g);
                } else {
                    for (String parentGID : g.getParentGroups()) {
                        if (parentGID.equals(group.getGID())) {
                            group.getGroups().add(g);
                            break;
                        }
                    }
                }
            }
        }
        
        groupList.remove(this);

        printContentCount();
    }
    
    @Override
    public Group getGroup(String GID) {
        if (GID == null || GID.isEmpty()) {
            return this;
        } else {
            return super.getGroup(GID);
        }
    }

    public void printContentCount() {
        log.info(thingList.size() + " things and " + groupList.size() + " groups currently exists");
    }
}
