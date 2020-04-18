package symphony.bm.cache.rules.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.rules.vo.triggers.LogicalOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Document(collection = "rules")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Rule {
    @NonNull @Field("rule_id") private String ruleID;
    @NonNull @Field("rule_name") private String ruleName;
    @NonNull private HashMap<String, Object> trigger;
    @NonNull private HashMap<String, HashMap<Integer, String>> action;
    private boolean cascading;
    
    @Transient @Getter private HashMap<String, List<Integer>> triggerProperties = new HashMap<>();
    @Transient @Getter private HashMap<String, List<Integer>> actionProperties = new HashMap<>();
    
    public Rule(@NonNull String ruleID, @NonNull String ruleName, @NonNull HashMap<String, Object> trigger,
                @NonNull HashMap<String, HashMap<Integer, String>> action, boolean cascading) {
        this.ruleID = ruleID;
        this.ruleName = ruleName;
        this.trigger = trigger;
        this.action = action;
        this.cascading = cascading;
        
        setTriggerProperties(trigger);
        setActionProperties();
//        for (String s : actionProperties.keySet()) {
//            System.out.print(s + ": [");
//            for (int i : actionProperties.get(s)) {
//                System.out.print(i + " ");
//            }
//            System.out.println("]");
//        }
    }
    
    public boolean isTriggerable(String cid, int prop_index) {
        if (triggerProperties.containsKey(cid)) {
            if (triggerProperties.get(cid).contains(prop_index)) {
                return true;
            }
        }
        return false;
    }
    
//    public boolean isTriggered(List<DeviceProperty> properties) {
//        // check if property list contains all the trigger properties
//        for (Map.Entry<String, List<Integer>> entry : triggerProperties.entrySet()) {
//            for (int index : entry.getValue()) {
//                DeviceProperty p = getDevicePropertyFromList(entry.getKey(), index, properties);
//                if (p == null) {
//                    return false;
//                }
//            }
//        }
//
//        return checkIfTriggered(trigger);
//    }
//
//    private boolean checkIfTriggered(HashMap<String, Object> triggerBlock) {
//
//    }
    
    private DeviceProperty getDevicePropertyFromList(String cid, int index, List<DeviceProperty> properties) {
        for (DeviceProperty p : properties) {
            if (p.getCID().equals(cid) && p.getIndex() == index) return p;
        }
        return null;
    }
    
    private void setTriggerProperties(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> entryValue = (Map<String, Object>) entry.getValue();
            try {
                LogicalOperator.valueOf(key);
                setTriggerProperties(entryValue);
            } catch (IllegalArgumentException e) { // key is already a CID
                if (!triggerProperties.containsKey(key)) {
                    triggerProperties.put(key, new Vector<>());
                }
                if (!triggerProperties.get(key).contains(entryValue.get("i"))) {
                    triggerProperties.get(key).add((Integer) entryValue.get("i"));
                }
            }
        }
    }
    
    private void setActionProperties() {
        for (Map.Entry<String, HashMap<Integer, String>> entry : action.entrySet()) {
            actionProperties.put(entry.getKey(), new Vector<>());
            for (Integer i : entry.getValue().keySet()) {
                actionProperties.get(entry.getKey()).add(i);
            }
        }
    }
}
