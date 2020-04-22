package symphony.bm.core.rest.hateoas;

import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.GroupController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class GroupModel extends RepresentationModel<GroupModel> {
    public final String GID;
    public final String parentGID;
    public final String name;

    public final List<BasicThingModel> things = new Vector<>();
    public final List<BasicGroupModel> groups = new Vector<>();

    public GroupModel(Group group) {
        this.GID = group.getGID();
        this.parentGID = group.getParentGID();
        this.name = group.getName();
        if (!GID.equals("")) {
            this.add(linkTo(methodOn(GroupController.class).getGroup(GID)).withSelfRel());
            this.add(linkTo(methodOn(GroupController.class).getGroup(parentGID)).withRel("parent"));
        } else {
            this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withSelfRel());
        }
        for (Thing thing : group.getThings()) {
            things.add(new BasicThingModel(thing));
        }
        for (Group subgroup : group.getGroups()) {
            groups.add(new BasicGroupModel(subgroup));
        }
    }
}
