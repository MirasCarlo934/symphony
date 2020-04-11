package symphony.bm.services.registry.cir.rule;

import org.bson.Document;
//import symphony.bm.services.adaptors.Adaptor;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class Rule {
    private Document ruleDoc;
    private Vector<String> triggers = new Vector<>();
    
//    private List<Adaptor> adaptors;

//    public Rule(Document ruleDoc, List<Adaptor> adaptors) {
//        this.ruleDoc = ruleDoc;
//        this.adaptors = adaptors;
//
//        setTriggers(getTriggerBlock());
//        for (Adaptor adaptor : adaptors) {
//            adaptor.ruleCreated(this);
//        }
//    }
    
    private void setTriggers(Document triggerBlock) {
        Set<String> keys = triggerBlock.keySet();
        for (String key : keys) {
            if (key.equals("OR") || key.equals("AND")) {
                setTriggers(triggerBlock.get(key, Document.class));
            } else {
                String trigger = key;
                Document device = triggerBlock.get(key, Document.class);
                trigger += "-" + device.getInteger("i");
                if (!triggers.contains(trigger))
                    triggers.add(trigger);
            }
        }
    }

    public boolean hasTriggerDeviceProperty(String cid, int propIndex) {
        String s = cid + "-" + propIndex;
        return triggers.contains(s);
    }

    public boolean isTriggered(HashMap<String, Integer> devicePropertyValues) {
        return recurseCheckTriggerBlock(devicePropertyValues, getTriggerBlock(), TriggerLogicalOperator.AND);
    }

    private boolean recurseCheckTriggerBlock(HashMap<String, Integer> devicePropertyValues, Document triggerBlock,
                                             TriggerLogicalOperator op) {
        Set<String> keys = triggerBlock.keySet();
        boolean b = false;
        for (String key : keys) {
            Document trigger = triggerBlock.get(key, Document.class);
            if (key.equals("OR") || key.equals("AND")) {
                b = recurseCheckTriggerBlock(devicePropertyValues, trigger, TriggerLogicalOperator.valueOf(key));
            } else {
                int val = devicePropertyValues.get(key + "-" + trigger.getInteger("i"));
                int ruleVal = trigger.getInteger("val");
                TriggerComparativeOperator comp = TriggerComparativeOperator.valueOf(trigger.getString("op"));
                switch (comp) {
                    case EQ:
                        b = val == ruleVal;
                        break;
                    case GT:
                        b = val > ruleVal;
                        break;
                    case LT:
                        b = val < ruleVal;
                        break;
                    case GTE:
                        b = val >= ruleVal;
                        break;
                    case LTE:
                        b = val <= ruleVal;
                        break;
                    case NE:
                        b = val != ruleVal;
                        break;
                }
            }
            if (op == TriggerLogicalOperator.AND  && !b) return false;
            if (op == TriggerLogicalOperator.OR && b) return true;
        }
        return b;
    }

    public boolean isCascading() {
        return ruleDoc.getBoolean("cascade");
    }

    private Document getTriggerBlock() {
        return ruleDoc.get("trigger", Document.class);
    }
    
    public Document getDocument() {
        return ruleDoc;
    }

    public HashMap<String, HashMap<Integer, Integer>> getActionBlock() {
        HashMap<String, HashMap<Integer, Integer>> actionBlock = new HashMap<>();
        Document actionDoc = ruleDoc.get("action", Document.class);

        for (String cid : actionDoc.keySet()) {
            Document action = actionDoc.get(cid, Document.class);
            if (!actionBlock.containsKey(cid))
                actionBlock.put(cid, new HashMap<>());
            for (String index : action.keySet()) {
                actionBlock.get(cid).put(Integer.parseInt(index), action.getInteger(index));
            }
        }

        return actionBlock;
    }

    public String getID() {
        return ruleDoc.getString("rule_id");
    }

    public String getName() {
        return ruleDoc.getString("rule_name");
    }
}
