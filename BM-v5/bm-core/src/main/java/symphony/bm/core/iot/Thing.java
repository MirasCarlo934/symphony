package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.resources.Resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class Thing extends Groupable implements Resource {
    @Id @JsonIgnore private String _id;
    @NonNull @Getter private final String uid;
    @NonNull @Setter @Getter private String name;
    @NonNull @Getter(AccessLevel.PACKAGE) private final List<Attribute> attributes = new Vector<>();

    @PersistenceConstructor
    public Thing(@NonNull List<String> parentGroups, String _id, @NonNull String uid, @NonNull String name) {
        super(parentGroups);
        this._id = _id;
        this.uid = uid;
        this.name = name;
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
            activityListeners.forEach( activityListener -> activityListener.attributeAddedToThing(attribute, this));
        }
    }

    public Attribute deleteAttribute(String aid) {
        Attribute attribute = getAttribute(aid);
        if (attribute != null) {
            attributes.remove(attribute);
            activityListeners.forEach( activityListener -> activityListener.attributeRemovedFromThing(attribute, this));
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
    public void setActivityListeners(List<ActivityListener> activityListeners) {
        super.setActivityListeners(activityListeners);
        for (Attribute attribute : attributes) {
            attribute.setActivityListeners(activityListeners);
        }
    }

    @Override
    public void create() {
        activityListeners.forEach( activityListener -> activityListener.thingCreated(this));
    }

    @SneakyThrows
    @Override
    public boolean update(Form form) {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            boolean paramSettable = false;
            String paramName = param.getKey().toLowerCase();
            for (Method method : Thing.class.getMethods()) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains("set") && methodName.substring(3).equals(paramName)) {
                    log.info("Changing " + param.getKey() + " to " + param.getValue());
                    method.invoke(this, param.getValue());
                    paramSettable = changed = true;
                }
            }
            if (!paramSettable) {
                params.remove(param.getKey());
            }
        }
        if (changed) {
            activityListeners.forEach(activityListener -> activityListener.thingUpdated(this, params));
        }
        return changed;
    }

    @Override
    public void delete() {
        activityListeners.forEach( activityListener -> activityListener.thingDeleted(this));
    }
}
