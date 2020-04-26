package symphony.bm.core.rest.forms.group;

import lombok.Data;

import java.util.List;

@Data
public class GroupGroupForm {
    private List<String> parentGroups;
}
