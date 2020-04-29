package symphony.bm.cir.rules;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRule;
import org.springframework.data.annotation.Transient;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Currently only supports Attributes
 */
@Slf4j
public class Rule implements ActivityListener {
    @Getter private String rid;
    @Getter private String name;
    @Getter private List<Namespace> namespaces;
    private String condition;
    private String action;

    @Transient @Setter private Map<String, Thing> thingFacts = new HashMap<>();
    @Transient @Setter private Map<String, Attribute> attributeFacts = new HashMap<>();

    public Rule(String rid, String name, List<Namespace> namespaces, String condition, String action) {
        this.rid = rid;
        this.name = name;
        this.namespaces = namespaces;
        this.condition = condition;
        this.action = action;
    }

    @Override
    public void attributeUpdatedValue(Attribute attribute, Object value) {
        log.info(attribute.getAid() + " value = " + value);
        Rules rules = new Rules();
        rules.register(new MVELRule()
                .name(rid)
                .description(name)
                .when(condition)
                .then(action));

        Facts facts = new Facts();
        namespaces.forEach( namespace -> {
            if (namespace.getAid() != null) {
                facts.put(namespace.getName(), attributeFacts.get(namespace.getAid()));
            } else {
                facts.put(namespace.getName(), thingFacts.get(namespace.getUid()));
            }
        } );

        RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);
    }

    @Override
    public void thingCreated(Thing thing) {

    }

    @Override
    public void thingUpdated(Thing thing, Map<String, Object> updatedFields) {

    }

    @Override
    public void thingAddedToGroup(Thing thing, Group group) {

    }

    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {

    }

    @Override
    public void thingDeleted(Thing thing) {

    }

    @Override
    public void groupCreated(Group group) {

    }

    @Override
    public void groupUpdated(Group group, Map<String, Object> updatedFields) {

    }

    @Override
    public void groupAddedToGroup(Group group, Group parent) {

    }

    @Override
    public void groupRemovedFromGroup(Group group, Group parent) {

    }

    @Override
    public void groupDeleted(Group group) {

    }

    @Override
    public void attributeUpdated(Attribute attribute, Map<String, Object> updatedFields) {

    }

    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {

    }

    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {

    }
}
