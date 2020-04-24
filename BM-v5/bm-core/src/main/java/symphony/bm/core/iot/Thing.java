package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.interfaces.Resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
//        setterVisibility = JsonAutoDetect.Visibility.NONE)
@Slf4j
public class Thing extends Groupable implements Resource {
    @Id @JsonIgnore private String _id;
    @NonNull @Getter private final String uid;
    @NonNull @Setter @Getter private String name;
    @NonNull @Getter private final List<Attribute> attributes;

    @PersistenceConstructor
    public Thing(@NonNull List<String> parentGroups, String _id, @NonNull String uid, @NonNull String name,
                 @NonNull List<Attribute> attributes) {
        super(parentGroups);
        this._id = _id;
        this.uid = uid;
        this.name = name;
        this.attributes = attributes;
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
