package symphony.bm.mqtt.iot;

import lombok.Value;
import symphony.bm.core.iot.Attribute;

@Value
public class MinifiedAttribute implements Minified<Attribute> {
    String aid;
    String name;
    MinifiedAttributeMode mode;
    MinifiedAttributeDataType dTyp;
    Object val;
    
    @Override
    public Attribute unminify() {
        return new Attribute(aid, name, mode.unminify(), dTyp.unminify(), val);
    }
}
