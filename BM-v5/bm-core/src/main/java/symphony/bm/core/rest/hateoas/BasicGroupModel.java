package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Group;
import symphony.bm.core.rest.GroupController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicGroupModel extends RepresentationModel<BasicGroupModel> {
    @Getter public final String gid;
//    @Getter public final List<String> parentGroups;
    @Getter public final String name;

    public BasicGroupModel(Group group) {
        this.gid = group.getGid();
//        this.parentGroups = group.getCopyOfParentGroups();
        this.name = group.getName();
    
        if (gid == null || gid.equals("")) {
            this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withSelfRel());
        } else {
            this.add(linkTo(methodOn(GroupController.class).get(gid)).withSelfRel());
//            for (String parentGID : parentGroups) {
//                if (parentGID == null || parentGID.equals("")) {
//                    this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
//                } else {
//                    this.add(linkTo(methodOn(GroupController.class).get(parentGID)).withRel("parent." + parentGID));
//                }
//            }
        }
    }
}
