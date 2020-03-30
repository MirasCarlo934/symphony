package symphony.bm.bmservicespoop.cir;

import org.bson.Document;

public class Rule {
    private Document ruleDoc;

    public Rule(Document ruleDoc) {
        this.ruleDoc = ruleDoc;
    }

    public String getRuleID() {
        return ruleDoc.getString("rule-id");
    }

    public String getRuleName() {
        return ruleDoc.getString("rule-name");
    }
}
