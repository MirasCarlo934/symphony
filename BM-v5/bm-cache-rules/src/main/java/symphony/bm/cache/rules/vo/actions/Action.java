package symphony.bm.cache.rules.vo.actions;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;

@Value
@AllArgsConstructor
public class Action {
    String CID;
    HashMap<Integer, String> propertyValues;
}
