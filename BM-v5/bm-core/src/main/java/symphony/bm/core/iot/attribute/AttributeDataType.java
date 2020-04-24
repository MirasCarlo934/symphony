package symphony.bm.core.iot.attribute;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor
public class AttributeDataType {
    @NonNull @Getter private final String name;
    @NonNull @Getter private final HashMap<String, Object> constraints;

    public enum AttributeDataTypeEnum {
        binary, number, enumeration, string
    }
}
