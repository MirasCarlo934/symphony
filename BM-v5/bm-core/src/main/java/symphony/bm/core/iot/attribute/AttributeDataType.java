package symphony.bm.core.iot.attribute;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor
public class AttributeDataType {
    @NonNull @Getter private final AttributeDataTypeEnum type;
    @NonNull @Getter private final HashMap<String, Object> constraints;

    public boolean checkValueIfValid(Object value) throws Exception {
        return type.checkValueIfValid(value, constraints);
    }
}
