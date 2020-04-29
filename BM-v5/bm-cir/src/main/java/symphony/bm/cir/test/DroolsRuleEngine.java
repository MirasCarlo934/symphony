package symphony.bm.cir.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DroolsRuleEngine {
//    private static final String drlFile = "test/test.drl";
//
//    public TestRuleEngine() {
//        TestThing thing1 = new TestThing();
//        TestThing target = new TestThing();
//        thing1.setValue(1);
//        target.setValue(0);
//
//        KieSession kieSession = kieContainer().newKieSession();
//        kieSession.setGlobal("target", target);
//        kieSession.insert(thing1);
//        kieSession.fireAllRules();
//        kieSession.dispose();
//
//        log.error(target.getValue() + " - " + thing1.getValue());
//    }
//
//    public KieContainer kieContainer() {
//        KieServices kieServices = KieServices.Factory.get();
//
//        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
//        kieFileSystem.write(ResourceFactory.newClassPathResource(drlFile));
//        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
//        kieBuilder.buildAll();
//        KieModule kieModule = kieBuilder.getKieModule();
//
//        return kieServices.newKieContainer(kieModule.getReleaseId());
//    }
//
//    public KieServices kieServices() {
//        return KieServices.Factory.get();
//    }
}
