package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Vector;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Group extends Groupable {
    @Id @JsonIgnore private String _id;
    @NonNull @Getter private String GID;
    @NonNull @Getter private String name;

    @Transient @Getter private final List<Thing> things = new Vector<>();
    @Transient @Getter private final List<Group> groups = new Vector<>();

    @PersistenceConstructor
    public Group(String parentGID, String _id, @NonNull String GID, @NonNull String name) {
        super(parentGID);
        this._id = _id;
        this.GID = GID;
        this.name = name;
    }

    public Group(String parentGID, @NonNull String GID, @NonNull String name) {
        super(parentGID);
        this.GID = GID;
        this.name = name;
    }
}
