package symphony.bm.core.iot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Null;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
public class SuperGroup extends Group {
    private final MongoOperations mongo;

    public SuperGroup(MongoTemplate mongoTemplate) {
        super("", "", "Super Group");
        this.mongo = mongoTemplate;

        buildContext();
        log.info("IOT context initialized");
    }

    private void buildContext() {
        List<Thing> thingList = mongo.findAll(Thing.class);
        List<Group> groupList = mongo.findAll(Group.class);
        groupList.add(this);

        for (Group group : groupList) {
            for (Thing thing : thingList) {
                if (thing.getParentGID().equals(group.getGID())) {
                    group.getThings().add(thing);
                }
            }
            for (Group g : groupList) {
                if (g.getParentGID().equals(group.getGID()) && !g.getClass().equals(SuperGroup.class)) {
                    group.getGroups().add(g);
                }
            }
        }

        printContentCount();
    }

    public void printContentCount() {
        log.info(getContainedThingsCount() + " things and " + getContainedGroupsCount() + " groups currently exists");
    }
}
