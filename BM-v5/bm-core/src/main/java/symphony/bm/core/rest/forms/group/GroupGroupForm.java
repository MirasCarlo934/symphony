package symphony.bm.core.rest.forms.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import symphony.bm.core.rest.forms.Form;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupGroupForm extends Form {
    List<String> parentGroups;
}
