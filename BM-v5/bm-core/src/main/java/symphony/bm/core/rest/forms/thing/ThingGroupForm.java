package symphony.bm.core.rest.forms.thing;

import lombok.Data;
import symphony.bm.core.rest.forms.Form;

import java.util.List;

@Data
public class ThingGroupForm extends Form {
    List<String> parentGroups;
}
