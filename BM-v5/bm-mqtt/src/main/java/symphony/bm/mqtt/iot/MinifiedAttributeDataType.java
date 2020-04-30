package symphony.bm.mqtt.iot;

import lombok.Value;
import symphony.bm.core.iot.attribute.AttributeDataType;

import java.util.Map;

@Value
public class MinifiedAttributeDataType implements Minified<AttributeDataType> {
    MinifiedAttributeDataTypeEnum typ;
    Map<String, Object> cnstr;
    
    @Override
    public AttributeDataType unminify() {
        return new AttributeDataType(typ.unminify(), cnstr);
    }
}
