package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class ThingModel extends RepresentationModel<ThingModel> {
    @Getter public final String uid;
    @Getter public final List<String> parentGroups;
    @Getter public final String name;
    @Getter public final List<AttributeModel> attributes = new Vector<>();

    public ThingModel(Thing thing) {
        this.uid = thing.getUid();
        this.parentGroups = thing.getCopyOfParentGroups();
        this.name = thing.getName();

        List<Attribute> attributeList = thing.getCopyOfAttributeList();
        for (Attribute attribute : attributeList) {
            AttributeModel attributeModel = new AttributeModel(attribute, uid);
            attributes.add(attributeModel);
        }

        this.add(linkTo(methodOn(ThingController.class).get(uid)).withSelfRel()
                .andAffordance(afford(methodOn(ThingController.class).add(uid, null)))
//                .andAffordance(afford(methodOn(ThingController.class).replace(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).update(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).addGroup(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).removeGroup(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).delete(uid)))
        );
        if (parentGroups.isEmpty()) {
            this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
        } else {
            for (String parentGID : parentGroups) {
                if (parentGID == null || parentGID.equals("")) {
                    this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
                } else {
                    this.add(linkTo(methodOn(GroupController.class).get(parentGID))
                            .withRel("parent." + parentGID));
                }
            }
        }
    }
}
