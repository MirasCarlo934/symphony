package symphony.bm.mqtt.iot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import symphony.bm.core.iot.Attribute;

@Value
public class MinifiedAttribute implements Minified<Attribute> {
    String aid;
    String name;
    MinifiedAttributeMode mode;
    MinifiedAttributeDataType dTyp;
    Object val;
    
    /**
     * This constructor is needed to force Jackson ObjectMapper to throw an exception when a property is not found
     * @param aid
     * @param name
     * @param mode
     * @param dTyp
     * @param val
     */
    @JsonCreator
    public MinifiedAttribute(@NonNull @JsonProperty("aid") String aid, @NonNull @JsonProperty("name") String name,
                             @NonNull @JsonProperty("mode") MinifiedAttributeMode mode,
                             @NonNull @JsonProperty("dTyp") MinifiedAttributeDataType dTyp,
                             @NonNull @JsonProperty("val") Object val) {
        this.aid = aid;
        this.name = name;
        this.mode = mode;
        this.dTyp = dTyp;
        this.val = val;
    }

    @SneakyThrows
    @Override
    public Attribute unminify() {
        return new Attribute(aid, name, mode.unminify(), dTyp.unminify(), val);
    }
}
