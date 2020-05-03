package symphony.bm.core.iot.attribute;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@AllArgsConstructor
public class AttributeDataType {
    @NonNull @Getter private final AttributeDataTypeEnum type;
    @NonNull @Getter private final Map<String, Object> constraints;

    public boolean checkValueIfValid(Object value) throws Exception {
        return type.checkValueIfValid(value, constraints);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            AttributeDataType other = (AttributeDataType) obj;
            if (!other.getType().equals(type)) {
                return false;
            }
            if (!other.getConstraints().equals(constraints)) {
                return false;
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
