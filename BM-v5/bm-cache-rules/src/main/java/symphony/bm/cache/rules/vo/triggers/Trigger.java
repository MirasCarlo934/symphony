package symphony.bm.cache.rules.vo.triggers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.Map;

public class Trigger implements Triggerable {
    @NonNull private LogicalOperator logicalOperator;
    @NonNull private List<Triggerable> subtriggers;
    
    public Trigger(Map<String, Map> map) {
        for (Map.Entry<String, Map> m : map.entrySet()) {
            LogicalOperator op = LogicalOperator.AND;
            try {
                op = LogicalOperator.valueOf(m.getKey());
            } catch (IllegalArgumentException e) {
            
            }
        }
    }
    
    @Override
    public boolean isTriggered() {
        return false;
    }
}
