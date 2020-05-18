package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.BaseController;
import symphony.bm.core.rest.GroupController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class GroupModel extends RepresentationModel<GroupModel> {
    @Getter public final String gid;
    @Getter public final List<BasicGroupModel> parentGroups = new Vector<>();
    @Getter public final String name;

    @Getter public final List<BasicThingModel> things = new Vector<>();
    @Getter public final List<BasicGroupModel> groups = new Vector<>();

    @SneakyThrows
    public GroupModel(Group group) {
        this.gid = group.getGid();
        this.name = group.getName();
    
        group.getParentGroupObjects().forEach( parent -> parentGroups.add(new BasicGroupModel(parent)) );

        Link selfLink;
        if (gid == null || gid.equals("")) {
            selfLink = linkTo(methodOn(BaseController.class).getSuperGroup()).withSelfRel();
        } else {
            selfLink = linkTo(methodOn(GroupController.class).get(gid)).withSelfRel();
            if (parentGroups.isEmpty()) {
                this.add(linkTo(methodOn(BaseController.class).getSuperGroup()).withRel("parent"));
            }
            for (String parentGID : group.getParentGroups()) {
                if (parentGID == null || parentGID.equals("")) {
                    this.add(linkTo(methodOn(BaseController.class).getSuperGroup()).withRel("parent"));
                } else {
                    this.add(linkTo(methodOn(GroupController.class).get(parentGID)).withRel("parent." + parentGID));
                }
            }
        }
        this.add(selfLink
                .andAffordance(afford(methodOn(GroupController.class).add(gid, null)))
                .andAffordance(afford(methodOn(GroupController.class).update(gid, null)))
                .andAffordance(afford(methodOn(GroupController.class).addGroup(gid, null)))
                .andAffordance(afford(methodOn(GroupController.class).removeGroup(gid, null)))
                .andAffordance(afford(methodOn(GroupController.class).delete(gid)))
        );
        for (Thing thing : group.getCopyOfThingList()) {
            things.add(new BasicThingModel(thing));
        }
        for (Group subgroup : group.getCopyOfGroupList()) {
            groups.add(new BasicGroupModel(subgroup));
        }
    }
}
