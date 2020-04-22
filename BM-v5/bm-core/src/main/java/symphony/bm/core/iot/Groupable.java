package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Groupable {
    @NonNull @Getter private String parentGID;

    public Groupable(String parentGID) {
        if (parentGID ==  null) {
            parentGID = "";
        }
        this.parentGID = parentGID;
    }

    public void setGroup(Group group) {
        this.parentGID = group.getGID();
    }
}
