package symphony.bm.cache.rules.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.rules.vo.triggers.ConditionalOperator;
import symphony.bm.cache.rules.vo.triggers.LogicalOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Document(collection = "jsonrules")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class JSONRule {
    @Id @JsonIgnore private String _id;
    @NonNull @Field("rule_id") @JsonProperty("rule_id") @Getter private String ruleID;
    @NonNull @Field("rule_name") @JsonProperty("rule_name") @Getter private String ruleName;
    @NonNull private HashMap<String, Object> trigger;
    @NonNull private HashMap<String, HashMap<Integer, String>> action;
    @Getter private boolean cascading;
    
    @Transient @Getter @JsonIgnore private HashMap<String, List<Integer>> triggerProperties = new HashMap<>();
    @Transient @Getter @JsonIgnore private HashMap<String, List<Integer>> actionProperties = new HashMap<>();
    
    @PersistenceConstructor
    @JsonCreator
    public JSONRule(@NonNull @JsonProperty("rule_id") String ruleID, @NonNull @JsonProperty("rule_name") String ruleName,
                    @NonNull @JsonProperty("trigger") HashMap<String, Object> trigger,
                    @NonNull @JsonProperty("action") HashMap<String, HashMap<Integer, String>> action,
                    @JsonProperty("cascading") boolean cascading) {
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
    
//    @JsonCreator
//    public Rule(@NonNull @JsonProperty("rule_id") String ruleID, @NonNull @JsonProperty("rule_name") String ruleName,
//                @NonNull @JsonProperty("trigger") HashMap<String, Object> trigger,
//                @NonNull @JsonProperty("action") HashMap<String, HashMap<Integer, String>> action,
//                @JsonProperty("cascading") boolean cascading) {
//        this.ruleID = ruleID;
//        this.ruleName = ruleName;
//        this.trigger = trigger;
//        this.action = action;
//        this.cascading = cascading;
//        this.triggerProperties = triggerProperties;
//        this.actionProperties = actionProperties;
//    }
    
    public boolean isTriggerable(String cid, int prop_index) {
        if (triggerProperties.containsKey(cid)) {
            if (triggerProperties.get(cid).contains(prop_index)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isTriggered(List<DeviceProperty> properties) throws IllegalArgumentException {
        // check if property list contains all the trigger properties
        for (Map.Entry<String, List<Integer>> entry : triggerProperties.entrySet()) {
            for (int index : entry.getValue()) {
                DeviceProperty p = getDevicePropertyFromList(entry.getKey(), index, properties);
                if (p == null) {
                    throw new IllegalArgumentException("Insufficient data! Some trigger properties were not supplied.");
                }
            }
        }

        return checkIfTriggered(trigger, LogicalOperator.AND, properties);
    }

    private boolean checkIfTriggered(HashMap<String, Object> triggerBlock, LogicalOperator logic,
                                     List<DeviceProperty> properties) {
        switch(logic) {
            case AND:
                for (Map.Entry<String, Object> t : triggerBlock.entrySet()) {
                    try { // if another trigger block is nested
                        LogicalOperator l_op = LogicalOperator.valueOf(t.getKey());
                        if (!checkIfTriggered((HashMap<String, Object>) t.getValue(), l_op, properties)) {
                            return false;
                        }
                    } catch (IllegalArgumentException e) { // if device trigger
                        String cid = t.getKey();
                        ConditionalOperator op = ConditionalOperator.valueOf(
                                (String) ((HashMap<String, Object>) t.getValue()).get("op"));
                        int index = (Integer) ((HashMap<String, Object>) t.getValue()).get("i");
                        String triggerValue = (String) ((HashMap<String, Object>) t.getValue()).get("val");
                        DeviceProperty prop = getDevicePropertyFromList(cid, index, properties);
                        if (!op.evaluate(prop.getValue(), triggerValue)) {
                            return false;
                        }
                    }
                }
                return true;
            case OR:
                boolean b;
                for (Map.Entry<String, Object> t : triggerBlock.entrySet()) {
                    try { // if another trigger block is nested
                        LogicalOperator l_op = LogicalOperator.valueOf(t.getKey());
                        if (checkIfTriggered((HashMap<String, Object>) t.getValue(), l_op, properties)) {
                            return true;
                        }
                    } catch (IllegalArgumentException e) { // if device trigger
                        String cid = t.getKey();
                        ConditionalOperator op = ConditionalOperator.valueOf(
                                (String) ((HashMap<String, Object>) t.getValue()).get("op"));
                        int index = (Integer) ((HashMap<String, Object>) t.getValue()).get("i");
                        String triggerValue = (String) ((HashMap<String, Object>) t.getValue()).get("val");
                        DeviceProperty prop = getDevicePropertyFromList(cid, index, properties);
                        if (op.evaluate(prop.getValue(), triggerValue)) {
                            return true;
                        }
                    }
                }
                return false;
            default:
                return false;
        }
    }
    
    public String getPropertyActionValue(String cid, int index) {
        return action.get(cid).get(index);
    }
    
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
