package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Transient @Getter protected final List<Thing> things = new Vector<>();
    @Transient @Getter protected final List<Group> groups = new Vector<>();

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

    public Thing getThing(String UID) {
        for (Thing thing : things) {
            if (thing.getUID().equals(UID)) {
                return thing;
            }
        }
        for (Group group : groups) {
            return group.getThing(UID);
        }
        return null;
    }

    public Group getGroup(String GID) {
        if (GID.equals(this.GID)) {
            return this;
        }
        for (Group group : groups) {
            return group.getGroup(GID);
        }
        return null;
    }

    public int getContainedThingsCount() {
        int count = things.size();
        for (Group group : groups) {
            count += group.getContainedThingsCount();
        }
        return count;
    }

    public int getContainedGroupsCount() {
        int count = groups.size();
        for (Group group : groups) {
            count += group.getContainedGroupsCount();
        }
        return count;
    }
}
