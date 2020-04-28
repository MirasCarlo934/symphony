package symphony.bm.core.rest.forms.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import symphony.bm.core.rest.forms.Form;

import java.util.List;
import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupUpdateForm extends Form {
    String gid;
    String name;
    List<String> parentGroups;
}