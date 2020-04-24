package symphony.bm.core.rest.forms.thing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.hateoas.AttributeModel;

import java.util.List;
import java.util.Map;
import java.util.Vector;

@Data
public class ThingUpdateForm extends Form {
    String name = null;
    List<String> parentGroups = null;
}
