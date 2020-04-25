package symphony.bm.core.rest.forms.attribute;

import lombok.Data;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.forms.Form;

@Data
public class AttributeUpdateForm extends Form {
    private String name;
    private AttributeMode mode;
    private AttributeDataType dataType;
    private Object value;
}
