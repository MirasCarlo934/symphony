package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.iot.exceptions.ValueUnchangedException;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.resources.Resource;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
@Slf4j
public class Attribute extends IotResource implements Resource {
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
            if (!this.value.toString().equals(value.toString())) {
                this.value = value;
                activityListenerManager.attributeUpdated(this, "value", value);
            } else {
                throw new ValueUnchangedException();
            }
        }
    }
    
    public void setName(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            activityListenerManager.attributeUpdated(this, "name", name);
        } else {
            throw new ValueUnchangedException();
        }
    }
    
    public void setDataType(AttributeDataType dataType) throws Exception {
        if (!this.dataType.equals(dataType)) {
            this.dataType = dataType;
            try {
                dataType.checkValueIfValid(value);
            } catch (Exception e) {
                setValue(dataType.getDefaultValue());
            }
            activityListenerManager.attributeUpdated(this, "dataType", dataType);
        } else {
            throw new ValueUnchangedException();
        }
    }

    public void setMode(String mode) throws IllegalArgumentException {
        AttributeMode m = AttributeMode.valueOf(mode);
        if (!m.equals(this.mode)) {
            this.mode = AttributeMode.valueOf(mode);
            activityListenerManager.attributeUpdated(this, "mode", mode);
        } else {
            throw new ValueUnchangedException();
        }
    }

    @Override
    public void create() {
        // all attribute create/delete functions are done on Thing-level
    }

    @Override
    public boolean update(Form form) throws Exception {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            String paramName = param.getKey().toLowerCase();
            changed = update(paramName, param.getValue());
//            for (Method method : Attribute.class.getDeclaredMethods()) {
//                String methodName = method.getName().toLowerCase();
//                if (methodName.contains("set") && methodName.substring(3).equals(paramName) &&
//                        method.getParameterCount() == 1 &&
//                        (method.getParameterTypes()[0].equals(param.getKey().getClass()) || paramName.equals("value"))) {
//                    try {
//                        method.invoke(this, param.getValue());
//                        log.info("Changed " + param.getKey() + " to " + param.getValue());
//                        changed = true;
//                    } catch (InvocationTargetException e) {
//                        if (!e.getCause().getClass().equals(ValueUnchangedException.class)) {
//                            throw (Exception) e.getCause();
//                        }
//                    }
//                    break;
//                }
//            }
        }
//        if (changed) {
//            activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, paramsChanged));
//        }
        return changed;
    }

    public boolean update(String fieldName, Object fieldValue) throws Exception {
        for (Method method : this.getClass().getDeclaredMethods()) {
            String methodName = method.getName().toLowerCase();
            if (methodName.contains("set") && methodName.substring(3).equalsIgnoreCase(fieldName) &&
                    method.getParameterCount() == 1 &&
                    (method.getParameterTypes()[0].equals(fieldValue.getClass()) || fieldName.equals("value"))) {
                try {
                    method.invoke(this, fieldValue);
                    log.info("Changed " + fieldValue + " to " + fieldValue);
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
    public void delete() {
        // all attribute create/delete functions are done on Thing-level
    }
}
