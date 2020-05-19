package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.AttributeController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicAttributeModel extends RepresentationModel<BasicAttributeModel> {
    @Getter public final String aid;
    @Getter public final String name;
    @Getter public final AttributeMode mode;
    @Getter public final AttributeDataType dataType;
    @Getter public final Object value;

    @SneakyThrows
    public BasicAttributeModel(Attribute attribute, String UID) {
        this.aid = attribute.getAid();
        this.name = attribute.getName();
        this.mode = attribute.getMode();
        this.dataType = attribute.getDataType();
        this.value = attribute.getValue();
        this.add(linkTo(methodOn(AttributeController.class).get(UID, aid, true)).withSelfRel());
    }
}
