package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.beans.Transient;
import java.util.List;
import java.util.Vector;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class Groupable extends IotResource {
    @NonNull @Setter(/*AccessLevel.PRIVATE*/) @Getter(/*AccessLevel.PROTECTED*/) private List<String> parentGroups;

    public Groupable(List<String> parentGroups) {
        if (parentGroups == null) {
            parentGroups = new Vector<>();
        }
        this.parentGroups = parentGroups;
    }

    @JsonIgnore
    public List<String> getCopyOfParentGroups() {
        return new Vector<>(parentGroups);
    }

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

    protected void addParentGroup(String GID) {
        if (!parentGroups.contains(GID)) {
            parentGroups.add(GID);
        }
    }

    protected void removeParentGroup(String GID) {
        parentGroups.remove(GID);
    }
}
