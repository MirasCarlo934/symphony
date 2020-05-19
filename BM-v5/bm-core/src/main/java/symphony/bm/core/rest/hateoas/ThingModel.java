package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.rest.BaseController;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class ThingModel extends RepresentationModel<ThingModel> {
    @Getter public final String uid;
    @Getter public final String name;
    @Getter public final boolean active;
    @Getter public final List<BasicGroupModel> parentGroups = new Vector<>();
    @Getter public final List<AttributeModel> attributes = new Vector<>();

    @SneakyThrows
    public ThingModel(Thing thing) {
        this.uid = thing.getUid();
        this.name = thing.getName();
        this.active = thing.isActive();

        thing.getParentGroupObjects().forEach( group -> parentGroups.add(new BasicGroupModel(group)) );

        List<Attribute> attributeList = thing.getCopyOfAttributeList();
        for (Attribute attribute : attributeList) {
            AttributeModel attributeModel = new AttributeModel(attribute, false);
            attributes.add(attributeModel);
        }

        this.add(linkTo(methodOn(ThingController.class).get(uid, true)).withSelfRel()
                .andAffordance(afford(methodOn(ThingController.class).add(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).update(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).addGroup(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).removeGroup(uid, null)))
                .andAffordance(afford(methodOn(ThingController.class).delete(uid)))
        );
        if (parentGroups.isEmpty()) {
            this.add(linkTo(methodOn(BaseController.class).getSuperGroup()).withRel("parent"));
        } else {
            for (Group parentGroup : thing.getParentGroupObjects()) {
                String parentGID = parentGroup.getGid();
                if (parentGID == null || parentGID.equals("")) {
                    this.add(linkTo(methodOn(BaseController.class).getSuperGroup()).withRel("parent"));
                } else {
                    this.add(linkTo(methodOn(GroupController.class).get(parentGID))
                            .withRel("parent." + parentGID));
                }
            }
        }
    }
}