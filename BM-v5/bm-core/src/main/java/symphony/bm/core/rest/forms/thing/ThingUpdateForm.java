package symphony.bm.core.rest.forms.thing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.hateoas.AttributeModel;

import java.util.List;
import java.util.Map;
import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThingUpdateForm extends Form {
    String name = null;
    List<String> parentGroups = null;
    List<Attribute> attributes = null;
}
