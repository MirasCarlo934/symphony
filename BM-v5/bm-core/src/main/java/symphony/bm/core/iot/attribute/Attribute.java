package symphony.bm.core.iot.attribute;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor
public class Attribute {
    @NonNull @Getter private String name;
    @NonNull @Getter private AttributeMode mode;
    @NonNull @Getter private AttributeDataType dataType;
    @NonNull @Getter private Object value;
}
