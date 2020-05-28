package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
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
public class Thing extends Groupable implements Resource {
    @Id @JsonIgnore private String _id;
    @NotNull @NonNull @Setter @Getter private String uid;
    @NotNull @NonNull @Getter private String name;
    @NotNull @NonNull @Getter private boolean active;

    @Transient @NotNull @NonNull @Setter @Getter(/*AccessLevel.PACKAGE*/) private List<Attribute> attributes = new Vector<>();

    @PersistenceConstructor
    public Thing(@NonNull List<String> parentGroups, String _id, @NonNull String uid, @NonNull String name,
                 @NonNull boolean active) {
        super(parentGroups);
        this._id = _id;
        this.uid = uid;
        this.name = name;
        this.active = active;
    }

    /**
     * For Thing JSON creation with attribute JSONs that do not declare 'thing' field
     * @param parentGroups
     * @param uid
     * @param name
     * @param attributes
     */
    @JsonCreator
    public Thing(@JsonProperty("parentGroups") List<String> parentGroups,
                 @JsonProperty("uid") @NonNull String uid,
                 @JsonProperty("name") @NonNull String name,
                 @JsonProperty("attributes") @NonNull List<Attribute> attributes,
                 @JsonProperty("active") @NonNull boolean active) throws Exception {
        super(parentGroups);
        this.uid = uid;
        this.name = name;
        this.active = active;
        for (Attribute a : attributes) {
            Attribute attribute = new Attribute(a.getAid(), a.getName(), a.getMode(), a.getDataType(), a.getValue());
            attribute.setThing(uid);
            this.attributes.add(attribute);
        }
    }

    public void setName(String name) throws ValueUnchangedException {
        if (!this.name.equals(name)) {
            this.name = name;
            activityListenerManager.thingUpdated(this, "name", name);
        } else {
            throw new ValueUnchangedException();
        }
    }
    
    public void setActive(boolean active) throws ValueUnchangedException {
        if (!this.active == active) {
            this.active = active;
            activityListenerManager.thingUpdated(this, "active", active);
        } else {
            throw new ValueUnchangedException();
        }
    }
    
    /**
     * Sets the active field of this Thing. Used primarily for REST API update field request.
     * @param activeStr The string containing the active boolean, case-insensitive
     * @throws ValueUnchangedException
     */
    public void setActive(String activeStr) throws ValueUnchangedException {
        setActive(Boolean.parseBoolean(activeStr));
    }

    public Attribute getAttribute(String aid) {
        for (Attribute attribute : attributes) {
            if (attribute.getAid().equals(aid)) {
                return attribute;
            }
        }
        return null;
    }

    public void addAttribute(Attribute attribute) {
        if (!attributes.contains(attribute)) {
            attributes.add(attribute);
            attribute.setThing(uid);
            attribute.setActivityListenerManager(activityListenerManager);
            activityListenerManager.attributeAddedToThing(attribute, this);
        }
    }

    public Attribute deleteAttribute(String aid) {
        Attribute attribute = getAttribute(aid);
        if (attribute != null) {
            attributes.remove(attribute);
            activityListenerManager.attributeRemovedFromThing(attribute, this);
            return attribute;
        } else {
            return null;
        }
    }

    @JsonIgnore
    public List<Attribute> getCopyOfAttributeList() {
        return new Vector<>(attributes);
    }

    @Override
    public void setActivityListenerManager(ActivityListenerManager activityListenerManager) {
        super.setActivityListenerManager(activityListenerManager);
        for (Attribute attribute : attributes) {
            attribute.setActivityListenerManager(activityListenerManager);
        }
    }

    @Override
    public void create() {
        activityListenerManager.thingCreated(this);
        attributes.forEach(attribute -> activityListenerManager.attributeAddedToThing(attribute, this));
    }

    @SneakyThrows
    @Override
    public boolean update(Form form) {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            String paramName = param.getKey().toLowerCase();
            changed = update(paramName, param.getValue());
        }
        return changed;
    }

    public boolean update(String fieldName, Object fieldValue) throws Exception {
        for (Method method : this.getClass().getDeclaredMethods()) {
            String methodName = method.getName().toLowerCase();
            if (!fieldName.equals("attributes") && methodName.contains("set") &&
                    methodName.substring(3).equalsIgnoreCase(fieldName) && method.getParameterCount() == 1 &&
                    method.getParameterTypes()[0].isInstance(fieldValue)) {
                try {
                    method.invoke(this, fieldValue);
                    log.info("Changed " + fieldName + " to " + fieldValue);
                    return true;
                } catch (InvocationTargetException e) {
                    if (!e.getCause().getClass().equals(ValueUnchangedException.class)) {
                        throw (Exception) e.getCause();
                    }
                }
                break;
            }
        }
        return false;
    }
    
    @Override
    @SneakyThrows
    public Object getField(String fieldName) {
        for (Method method : this.getClass().getMethods()) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("get") && methodName.substring(3).equalsIgnoreCase(fieldName)) {
                return method.invoke(this);
            }
        }
        return null;
    }
    
    @Override
    public void delete() {
        for (Attribute attribute : attributes) {
            activityListenerManager.attributeRemovedFromThing(attribute, this);
        }
        activityListenerManager.thingDeleted(this);
    }
}
