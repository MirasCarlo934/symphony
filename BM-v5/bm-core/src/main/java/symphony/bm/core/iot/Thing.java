package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import symphony.bm.core.iot.attribute.Attribute;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Thing extends Groupable {
    @Id @JsonIgnore private String _id;
    @JsonProperty("UID") @NonNull @Getter private final String UID;
    @NonNull @Getter private String name;
    @NonNull @Getter private final List<Attribute> attributes;

    @PersistenceConstructor
    public Thing(List<String> parentGroups, String _id, @NonNull String UID, @NonNull String name,
                 @NonNull List<Attribute> attributes) {
        super(parentGroups);
        this._id = _id;
        this.UID = UID;
        this.name = name;
        this.attributes = attributes;
    }
}
