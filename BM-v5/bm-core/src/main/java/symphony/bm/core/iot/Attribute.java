package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.MethodInvocationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import symphony.bm.core.activitylisteners.Listenable;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.resources.Resource;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
@Slf4j
public class Attribute extends Listenable implements Resource {
    @Id @JsonIgnore private String _id;
    @NotNull @NonNull /*@Setter*/ @Getter private String aid;
    @NotNull @Setter(AccessLevel.PACKAGE) @Getter private String thing;
    
    @NotNull @NonNull @Getter private String name;
    @NotNull @NonNull @Getter private AttributeDataType dataType;
    @NotNull @NonNull @Getter private AttributeMode mode;
    @NotNull @NonNull @Getter private Object value;

    /**
     * For attributes with no 'thing' field. Usually in Thing JSON declaration.
     * @param aid
     * @param name
     * @param mode
     * @param dataType
     * @param value
     */
    @JsonCreator
    public Attribute(@JsonProperty("aid") @NotNull @NonNull String aid,
                     @JsonProperty("name") @NotNull @NonNull String name,
                     @JsonProperty("mode") @NotNull @NonNull AttributeMode mode,
                     @JsonProperty("dataType") @NotNull @NonNull AttributeDataType dataType,
                     @JsonProperty("value") @NotNull @NonNull Object value) {
        this.aid = aid;
        this.name = name;
        this.mode = mode;
        this.dataType = dataType;
        this.value = value;
    }

    public void setValue(Object value) throws Exception {
        if (dataType.checkValueIfValid(value)) {
            this.value = value;
            activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, "value", value));
        }
    }
    
    public void setName(String name) {
        this.name = name;
        activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, "name", name));
    }
    
    public void setDataType(AttributeDataType dataType) {
        this.dataType = dataType;
        activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, "dataType", dataType));
    }

    public void setMode(String mode) throws IllegalArgumentException {
        this.mode = AttributeMode.valueOf(mode);
        activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, "mode", mode));
    }

    @Override
    public void create() {
        // all attribute create/delete functions are done on Thing-level
    }

    @Override
    public boolean update(Form form) throws Exception {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        Map<String, Object> paramsChanged = new HashMap<>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            boolean paramSettable = false;
            String paramName = param.getKey().toLowerCase();
            for (Method method : Attribute.class.getDeclaredMethods()) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains("set") && methodName.substring(3).equals(paramName) &&
                        method.getParameterCount() == 1 &&
                        (method.getParameterTypes()[0].equals(param.getKey().getClass()) || paramName.equals("value"))) {
                    log.info("Changing " + param.getKey() + " to " + param.getValue());
                    try {
                        method.invoke(this, param.getValue());
                    } catch (InvocationTargetException e) {
                        throw (Exception) e.getCause();
                    }
                    paramSettable = changed = true;
                    break;
                }
            }
            if (paramSettable) {
                paramsChanged.put(param.getKey(), param.getValue());
            }
        }
//        if (changed) {
//            activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, paramsChanged));
//        }
        return changed;
    }

    @Override
    public void delete() {
        // all attribute create/delete functions are done on Thing-level
    }
}
