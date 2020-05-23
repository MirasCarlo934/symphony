package symphony.bm.data.rest.resource.stats.avr;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Value
public class BinaryAttributeValueRecordsStats extends AttributeValueRecordsStats {
    Map<String, Long> timeSpentAt = new HashMap<>();
    long totalTime;
    
    @SneakyThrows
    public BinaryAttributeValueRecordsStats(long timeSpentAtOne, long timeSpentAtZero, long totalTime,
                                            String thing, String aid, Date from, Date to, Pageable p) {
        super(thing, aid, from, to, p);
        timeSpentAt.put("1", timeSpentAtOne);
        timeSpentAt.put("0", timeSpentAtZero);
        this.totalTime = totalTime;
    }
}
