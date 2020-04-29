package symphony.bm.cir.test;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.mvel.MVELRule;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JEasyRuleEngine {

    public JEasyRuleEngine() {
//        Rule weatherRule = new MVELRule()
//                .name("weather rule")
//                .description("if it rains then take an umbrella")
//                .when("thing1.getValue() != \"dry\"")
//                .then("thing2.setValue(\"'Tis raining y'all\")");
//
//        TestThing thing1 = new TestThing();
//        TestThing thing2 = new TestThing();
//        thing1.setValue("rain");
//        thing2.setValue("HAHA");
//
//        // define facts
//        Facts facts = new Facts();
//        facts.put("thing1", thing1);
//        facts.put("thing2", thing2);
//
//        // define rules
//        Rules rules = new Rules();
//        rules.register(weatherRule);
//
//        // fire rules on known facts
//        RulesEngine rulesEngine = new DefaultRulesEngine();
//        rulesEngine.fire(rules, facts);
//
//        log.error(thing2.getValue().toString());
    }

}
