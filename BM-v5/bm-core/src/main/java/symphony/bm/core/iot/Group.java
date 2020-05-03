package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.iot.exceptions.ValueUnchangedException;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.resources.Resource;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class Group extends Groupable implements Resource {
    @Id @JsonIgnore private String _id;
    @NotNull @NonNull @Setter @Getter private String gid;
    @NotNull @NonNull @Getter private String name;

    @Transient protected List<Thing> things = new Vector<>();
    @Transient protected List<Group> groups = new Vector<>();

    @PersistenceConstructor
    @JsonCreator
    public Group(@JsonProperty("parentGroups") List<String> parentGroups, String _id,
                 @JsonProperty("gid") @NonNull String gid, @JsonProperty("name") @NonNull String name) {
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
    
    public void setName(String name) throws ValueUnchangedException {
        if (!this.name.equals(name)) {
            this.name = name;
            activityListenerManager.groupUpdated(this, "name", name);
        } else {
            throw new ValueUnchangedException();
        }
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
        thing.setActivityListenerManager(activityListenerManager);
        things.add(thing);
        thing.addParentGroup(gid);
        activityListenerManager.thingAddedToGroup(thing, this);
    }

    public void removeThing(Thing thing) {
        if (things.remove(thing)) {
            thing.removeParentGroup(gid);
            activityListenerManager.thingRemovedFromGroup(thing, this);
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
        if (getGroup(group.getGid()) != null) return;
        group.setActivityListenerManager(activityListenerManager);
        groups.add(group);
        group.addParentGroup(gid);
        activityListenerManager.groupAddedToGroup(group, this);
    }

    public void removeGroup(Group group) {
        if (groups.remove(group)) {
            group.removeParentGroup(gid);
            activityListenerManager.groupRemovedFromGroup(group, this);
        }
    }
    
    @JsonIgnore
    public List<Thing> getCopyOfThingList() {
        return new Vector<>(things);
    }
    
    @JsonIgnore
    public List<Group> getCopyOfGroupList() {
        return new Vector<>(groups);
    }

    @JsonIgnore
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

    @JsonIgnore
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
        activityListenerManager.groupCreated(this);
    }

    @SneakyThrows
    @Override
    public boolean update(Form form) {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            boolean paramSettable = false;
            String paramName = param.getKey().toLowerCase();
            for (Method method : Group.class.getDeclaredMethods()) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains("set") && methodName.substring(3).equals(paramName)) {
                    try {
                        method.invoke(this, param.getValue());
                        log.info("Changed " + param.getKey() + " to " + param.getValue());
                        changed = true;
                    } catch (InvocationTargetException e) {
                        if (!e.getCause().getClass().equals(ValueUnchangedException.class)) {
                            throw (Exception) e.getCause();
                        }
                    }
                    break;
                }
            }
        }
//        if (changed) {
//            activityListeners.forEach(activityListener -> activityListener.groupUpdated(this, paramsChanged));
//        }
        return changed;
    }

    @Override
    public void delete() {
        activityListenerManager.groupDeleted(this);
    }
}
