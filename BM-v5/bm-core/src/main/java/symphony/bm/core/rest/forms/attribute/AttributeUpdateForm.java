package symphony.bm.core.rest.forms.attribute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.forms.Form;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeUpdateForm extends Form {
    String name;
    AttributeMode mode;
    AttributeDataType dataType;
    Object value;
}
