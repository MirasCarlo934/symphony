package symphony.bm.data.rest.resource.stats;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Value
public class EnumerationAttributeValueRecordsStats extends AttributeValueRecordsStats {
    Map<String, Long> timeSpentAt;
    long totalTime;
    
    /**
     *
     * @param timeSpentAt A Map containing the time spent in milliseconds for all values of the enumeration Attribute
     * @param totalTime Total time of evaluation
     * @param thing
     * @param aid
     * @param from
     * @param to
     * @param p
     */
    @SneakyThrows
    public EnumerationAttributeValueRecordsStats(Map<String, Long> timeSpentAt, long totalTime,
                                                 String thing, String aid, Date from, Date to, Pageable p) {
        super(thing, aid, from, to, p);
        this.timeSpentAt = timeSpentAt;
        this.totalTime = totalTime;
    }
}
