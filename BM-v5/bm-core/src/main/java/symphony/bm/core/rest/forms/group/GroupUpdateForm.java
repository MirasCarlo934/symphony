package symphony.bm.core.rest.forms.group;

import lombok.Data;
import symphony.bm.core.rest.forms.Form;

import java.util.List;
import java.util.Vector;

@Data
public class GroupUpdateForm extends Form {
    private String gid;
    private String name;
    private List<String> parentGroups;
}