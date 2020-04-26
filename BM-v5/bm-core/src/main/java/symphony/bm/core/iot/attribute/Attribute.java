package symphony.bm.core.iot.attribute;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import symphony.bm.core.activitylisteners.Listenable;
import symphony.bm.core.rest.forms.Form;
import symphony.bm.core.rest.resources.Resource;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor
@Slf4j
public class Attribute extends Listenable implements Resource {
    @Id @JsonIgnore private String _id;
    @NotNull @NonNull @Getter private String aid;
    @NotNull @NonNull @Getter private String thing;
    @NotNull @NonNull @Setter @Getter private String name;
    @NotNull @NonNull @Setter @Getter private AttributeMode mode;
    @NotNull @NonNull @Setter @Getter private AttributeDataType dataType;
    @NotNull @NonNull @Setter @Getter private Object value;

    @Override
    public void create() {
        // all attribute create/delete functions are done on Thing-level
    }

    @SneakyThrows
    @Override
    public boolean update(Form form) {
        boolean changed = false;
        Map<String, Object> params = form.transformToMap();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            boolean paramSettable = false;
            String paramName = param.getKey().toLowerCase();
            for (Method method : Attribute.class.getMethods()) {
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
            activityListeners.forEach(activityListener -> activityListener.attributeUpdated(this, params));
        }
        return changed;
    }

    @Override
    public void delete() {
        // all attribute create/delete functions are done on Thing-level
    }
}
