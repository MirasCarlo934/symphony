package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Vector;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Groupable extends IotResource {
    // this field is for DB persistence only
    @NonNull @Setter(/*AccessLevel.PRIVATE*/) /*@Getter(AccessLevel.PROTECTED)*/ private List<String> parentGroups;
    // this field is for resource representation
    @Transient @Getter private List<Group> parentGroupObjects = new Vector<>();

    public Groupable(List<String> parentGroups) {
        if (parentGroups == null) {
            parentGroups = new Vector<>();
        }
        this.parentGroups = parentGroups;
    }

//    @JsonIgnore
//    public List<String> getCopyOfParentGroups() {
//        return new Vector<>(parentGroups);
//    }

    public int parentGroupsCount() {
        return parentGroups.size();
    }

    public boolean hasSameParentGroups(List<String> groups) {
        return parentGroups.size() == groups.size() && parentGroups.containsAll(groups);
    }

    public boolean hasGroup(String GID) {
        return parentGroups.contains(GID);
    }

    public boolean hasNoGroup() {
        return parentGroups.size() == 0;
    }

    protected void addParentGroup(Group group) {
        if (!parentGroups.contains(group.getGid())) {
            parentGroups.add(group.getGid());
            parentGroupObjects.add(group);
        }
    }

    protected void removeParentGroup(Group group) {
        parentGroups.remove(group.getGid());
        parentGroupObjects.remove(group);
    }
}
