package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.AttributeController;
import symphony.bm.core.rest.ThingController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class AttributeModel extends RepresentationModel<AttributeModel> {
    @Getter public final String aid;
    @Getter public final String name;
    @Getter public final AttributeMode mode;
    @Getter public final AttributeDataType dataType;
    @Getter public final Object value;

    @SneakyThrows
    public AttributeModel(Attribute attribute, String UID) {
        this.aid = attribute.getAid();
        this.name = attribute.getName();
        this.mode = attribute.getMode();
        this.dataType = attribute.getDataType();
        this.value = attribute.getValue();
        this.add(linkTo(methodOn(AttributeController.class).get(UID, aid)).withSelfRel()
                .andAffordance(afford(methodOn(AttributeController.class).add(UID, aid, null)))
                .andAffordance(afford(methodOn(AttributeController.class).update(UID, aid, null)))
                .andAffordance(afford(methodOn(AttributeController.class).delete(UID, aid)))
        );
        this.add(linkTo(methodOn(ThingController.class).get(UID))
                .withRel("parent"));
    }
}
