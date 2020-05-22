package symphony.bm.data.rest.resource.stats;

import org.springframework.data.domain.Pageable;

import java.util.Date;

public class EmptyAttributeValueRecordsStats extends AttributeValueRecordsStats {
    public EmptyAttributeValueRecordsStats(String thing, String aid, Date from, Date to, Pageable p) {
        super(thing, aid, from, to, p);
    }
}
