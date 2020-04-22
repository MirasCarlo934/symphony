package symphony.bm.cache.rules.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.rules.vo.actions.Action;
import symphony.bm.cache.rules.vo.variables.Variable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@Document(collection = "rules")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Rule {
    private static final Logger LOG = LoggerFactory.getLogger(Rule.class);
    
    @Id @JsonIgnore private String _id;
    @NonNull @Field("rule_id") @JsonProperty("rule_id") @Getter private final String ruleID;
    @NonNull @Field("rule_name") @JsonProperty("rule_name") @Getter private final String ruleName;
    @NonNull private final String trigger;
    @NonNull private final List<Action> action;
    @NonNull private final List<Variable> variables;
    @Getter private final boolean cascading;
    
    @Transient @Getter @JsonIgnore private final HashMap<String, List<Integer>> triggerProperties = new HashMap<>();
    @Transient @Getter @JsonIgnore private final HashMap<String, List<Integer>> actionProperties = new HashMap<>();
    @Transient @Getter @JsonIgnore private final HashMap<String, List<Integer>> variableProperties = new HashMap<>();
    @Transient @JsonIgnore private final List<Action> actionValues = new Vector<>();
    
    @Transient @JsonIgnore
    private final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    
    @PersistenceConstructor
    @JsonCreator
    public Rule(@NonNull @JsonProperty("rule_id") String ruleID, @NonNull @JsonProperty("rule_name") String ruleName,
                @NonNull @JsonProperty("trigger") String trigger, @NonNull @JsonProperty("action") List<Action> action,
                @NonNull @JsonProperty("variables") List<Variable> variables,
                @JsonProperty("cascading") boolean cascading) {
        this.ruleID = ruleID;
        this.ruleName = ruleName;
        this.trigger = trigger;
        this.action = action;
        this.variables = variables;
        this.cascading = cascading;
        
        setVariableProperties();
    }
    
    public boolean isTriggerable(String cid, int prop_index) {
        if (triggerProperties.containsKey(cid)) {
            if (triggerProperties.get(cid).contains(prop_index)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean evaluate(List<DeviceProperty> properties) throws IllegalArgumentException {
        actionValues.clear();
        for (Variable var : variables) {
            DeviceProperty p = getDevicePropertyFromList(var.getCID(), var.getIndex(), properties);
            if (p == null) {
                throw new IllegalArgumentException("Insufficient data! Some trigger properties were not supplied.");
            }
            jsEngine.put(var.getName(), p.getValue());
        }
        
        try {
            boolean result = (Boolean) jsEngine.eval(trigger);
            if (result) {
                for (Action action : action) {
                    Action actionValue = new Action(action.getCID(), action.getIndex(),
                            (String) jsEngine.eval(action.getValue()));
                    actionValues.add(actionValue);
                }
            }
            return result;
        } catch (ScriptException e) {
            LOG.error("Error evaluating trigger script", e);
            return false;
        }
    }
    
    public String getPropertyActionValue(String cid, int index) {
        for (Action action : actionValues) {
            if (action.getCID().equals(cid) && action.getIndex() == index) {
                return action.getValue();
            }
        }
        return null;
    }
    
    private void setVariableProperties() {
        for (Variable var : variables) {
            if (!variableProperties.containsKey(var.getCID())) {
                variableProperties.put(var.getCID(), new Vector<>());
            }
            variableProperties.get(var.getCID()).add(var.getIndex());
            if (var.isTrigger()) {
                if (!triggerProperties.containsKey(var.getCID())) {
                    triggerProperties.put(var.getCID(), new Vector<>());
                }
                triggerProperties.get(var.getCID()).add(var.getIndex());
            }
        }
        for (Action action : action) {
            if (!actionProperties.containsKey(action.getCID())) {
                actionProperties.put(action.getCID(), new Vector<>());
            }
            actionProperties.get(action.getCID()).add(action.getIndex());
        }
    }
    
    private DeviceProperty getDevicePropertyFromList(String cid, int index, List<DeviceProperty> properties) {
        for (DeviceProperty p : properties) {
            if (p.getCID().equals(cid) && p.getIndex() == index) return p;
        }
        return null;
    }
}
