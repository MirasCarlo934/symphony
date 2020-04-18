package symphony.bm.cache.rules.vo.triggers;

import lombok.Value;

@Value
public class DevicePropertyTrigger implements Triggerable {
    int i;
    ConditionalOperator op;
    String val;
    
    @Override
    public boolean isTriggered() {
        return false;
    }
}
