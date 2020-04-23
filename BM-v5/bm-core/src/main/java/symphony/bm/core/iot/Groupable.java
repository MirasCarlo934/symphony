package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Groupable {
    @NonNull @Getter private List<String> parentGroups;

    public Groupable(List<String> parentGroups) {
        this.parentGroups = parentGroups;
    }
}
