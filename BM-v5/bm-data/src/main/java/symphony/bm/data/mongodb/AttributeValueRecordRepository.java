package symphony.bm.data.mongodb;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import symphony.bm.data.iot.AttributeValueRecord;

import java.util.Date;

@Component
public interface AttributeValueRecordRepository extends PagingAndSortingRepository<AttributeValueRecord, Date> {
    AttributeValueRecord findFirstByThingAndAid(String thing, String aid);
}
