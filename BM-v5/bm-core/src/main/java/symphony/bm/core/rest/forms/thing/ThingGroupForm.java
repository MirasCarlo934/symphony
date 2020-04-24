package symphony.bm.core.rest.forms.thing;

import lombok.Data;

import java.util.List;

@Data
public class ThingGroupForm {
    List<String> parentGroups;
}
