package symphony.bm.cache.rules.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "rules")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Rule {
//    @NonNull @Field("rule_id") private String ruleID;
//    @NonNull @Field("rule_name") private String ruleName;
//    @NonNull private boolean cascading;
//    @NonNull private HashMap<>
}
