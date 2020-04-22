package symphony.bm.core.iot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
public class IotContext {
    private final MongoOperations mongo;

    @Getter private List<Thing> things;
    @Getter private List<Group> groups;
    @Getter private final Group superGroup;

    public IotContext(MongoTemplate mongoTemplate) {
        this.mongo = mongoTemplate;
        this.superGroup = new Group("", "", "Super Group");

        buildContext();
        log.info("IOT context initialized");
    }

    private void buildContext() {
        things = mongo.findAll(Thing.class);
        groups = mongo.findAll(Group.class);

        for (Thing thing : things) {
            Group group = getGroup(thing.getParentGID());
            group.getThings().add(thing);
        }
        for (Group group : groups) {
            Group parentGroup = getGroup(group.getParentGID());
            parentGroup.getGroups().add(group);
        }

        printContents();
    }

    public Thing getThing(String UID) {
        for (Thing thing : things) {
            if (thing.getUID().equals(UID)) {
                return thing;
            }
        }
        return null;
    }

    public Group getGroup(String GID) {
        if (GID == null || GID.isEmpty()) {
            return superGroup;
        }
        for (Group group : groups) {
            if (group.getGID().equals(GID)) {
                return group;
            }
        }
        return null;
    }

    public void printContents() {
        log.info(things.size() + " things and " + groups.size() + " groups currently in context");
    }
}
