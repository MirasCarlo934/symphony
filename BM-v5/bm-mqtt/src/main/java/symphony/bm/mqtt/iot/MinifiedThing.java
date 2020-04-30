package symphony.bm.mqtt.iot;

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
    
    @Override
    public Thing unminify() {
        Thing thing = new Thing(grps, null, uid, name);
        attribs.forEach( attr -> thing.addAttribute(attr.unminify()));
        return thing;
    }
}
