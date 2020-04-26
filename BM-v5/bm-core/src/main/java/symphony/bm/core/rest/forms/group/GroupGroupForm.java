package symphony.bm.core.rest.forms.group;

import lombok.Data;
import symphony.bm.core.rest.forms.Form;

import java.util.List;

@Data
public class GroupGroupForm extends Form {
    private List<String> parentGroups;
}
