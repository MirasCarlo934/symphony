package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicThingModel extends RepresentationModel<BasicThingModel> {
    @Getter public final String uid;
//    @Getter public final List<String> parentGroups;
    @Getter public final String name;

    @SneakyThrows
    public BasicThingModel(Thing thing) {
        this.uid = thing.getUid();
//        this.parentGroups = thing.getCopyOfParentGroups();
        this.name = thing.getName();
    
        this.add(linkTo(methodOn(ThingController.class).get(uid)).withSelfRel());
//        if (parentGroups.isEmpty()) {
//            this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
//        } else {
//            for (String parentGID : parentGroups) {
//                if (parentGID == null || parentGID.equals("")) {
//                    this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
//                } else {
//                    this.add(linkTo(methodOn(GroupController.class).get(parentGID))
//                            .withRel("parent." + parentGID));
//                }
//            }
//        }
    }
}
