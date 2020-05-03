package symphony.bm.mqtt.iot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;

import java.util.List;
import java.util.Vector;

@Value
public class MinifiedThing implements Minified<Thing> {
    String uid;
    String name;
    List<MinifiedAttribute> attribs;
    List<String> grps;
    
    /**
     * This constructor is needed to force Jackson ObjectMapper to throw an exception when a property is not found
     * @param uid
     * @param name
     * @param attribs
     * @param grps
     */
    @JsonCreator
    public MinifiedThing(@NonNull @JsonProperty("uid") String uid, @NonNull @JsonProperty("name") String name,
                         @NonNull @JsonProperty("attribs") List<MinifiedAttribute> attribs,
                         @NonNull @JsonProperty("grps") List<String> grps) {
        this.uid = uid;
        this.name = name;
        this.attribs = attribs;
        this.grps = grps;
    }
    
    
    @Override
    public Thing unminify() {
        Thing thing = new Thing(grps, null, uid, name);
        attribs.forEach( attr -> thing.addAttribute(attr.unminify()));
        return thing;
    }
}
