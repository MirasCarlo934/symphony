package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.interfaces.Resource;

import java.util.List;
import java.util.Vector;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Group extends Groupable implements Resource {
    @Id @JsonIgnore private String _id;
    @NonNull @Getter private String gid;
    @NonNull @Getter private String name;

    @Transient protected final List<Thing> things = new Vector<>();
    @Transient protected final List<Group> groups = new Vector<>();

    @PersistenceConstructor
    public Group(List<String> parentGroups, String _id, @NonNull String gid, @NonNull String name) {
        super(parentGroups);
        this._id = _id;
        this.gid = gid;
        this.name = name;
    }

    public Group(@NonNull String gid, @NonNull String name) {
        super(new Vector<>());
        this.gid = gid;
        this.name = name;
    }

    public Thing getThing(String UID) {
        for (Thing thing : things) {
            if (thing.getUid().equals(UID)) {
                return thing;
            }
        }
        return null;
    }

    public Thing getThingRecursively(String UID) {
        Thing thing = getThing(UID);
        if (thing != null) {
            return thing;
        }
        for (Group group : groups) {
            thing = group.getThingRecursively(UID);
            if (thing != null) {
                return thing;
            }
        }
        return null;
    }
    
    public void addThing(Thing thing) {
        if (getThing(thing.getUid()) != null) return;
        thing.setActivityListeners(activityListeners);
        things.add(thing);
        thing.addParentGroup(gid);
        activityListeners.forEach( listener -> listener.thingAddedToGroup(thing, this));
    }

    public void removeThing(Thing thing) {
        boolean b = things.remove(thing);
        if (b) {
            thing.removeParentGroup(gid);
            activityListeners.forEach( listener -> listener.thingRemovedFromGroup(thing, this));
        }
    }

    public Group getGroup(String GID) {
        if (GID.equals(this.gid)) {
            return this;
        }
        for (Group group : groups) {
            if (group.getGid().equals(GID)) {
                return group;
            }
        }
        return null;
    }

    public Group getGroupRecursively(String GID) {
        Group group = getGroup(GID);
        if (group != null) {
            return group;
        }
        for (Group subgroup : groups) {
            group = subgroup.getGroupRecursively(GID);
            if (group != null) {
                return group;
            }
        }
        return null;
    }

    public void addGroup(Group group) {
        group.setActivityListeners(activityListeners);
        groups.add(group);
        activityListeners.forEach( listener -> listener.groupAddedToGroup(group, this));
    }

    public List<Thing> getCopyOfThingList() {
        return new Vector<>(things);
    }

    public List<Group> getCopyOfGroupList() {
        return new Vector<>(groups);
    }

    public List<Thing> getContainedThings() {
        List<Thing> thingList = new Vector<>(things);
        for (Group group : groups) {
            List<Thing> subThingList = group.getContainedThings();
            for (Thing thing : subThingList) {
                if (!thingList.contains(thing)) {
                    thingList.add(thing);
                }
            }
        }
        return thingList;
    }

    public List<Group> getContainedGroups() {
        List<Group> groupList = new Vector<>(groups);
        for (Group group : groups) {
            List<Group> subGroupList = group.getContainedGroups();
            for (Group subgroup : subGroupList) {
                if (!groupList.contains(subgroup)) {
                    groupList.add(subgroup);
                }
            }
        }
        return groupList;
    }

    @Override
    public void create() {
        activityListeners.forEach( listener -> listener.groupCreated(this));
    }

    @Override
    public boolean update(Form form) {
        return false;
    }

    @Override
    public void delete() {

    }
}
