package symphony.bm.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import symphony.bm.data.iot.attribute.AttributeValueRecord;

import java.util.Date;

@Component
//@RepositoryRestResource(collectionResourceRel = "attributes/values", path = "attributes/values")
//@CrossOrigin
public interface AttributeValueRecordRepository extends MongoRepository<AttributeValueRecord, Date> {
    
    @Query(sort = "{timestamp: -1 }")
    AttributeValueRecord findFirstByThingAndAid(String thing, String aid);
    
    @Query(sort = "{timestamp: -1 }")
    Page<AttributeValueRecord> findByThingAndAid(String thing, String aid, Pageable p);
    
//    @RestResource(path = "findByThingAndAidFrom", rel = "findByThingAndAidFrom")
    @Query(sort = "{timestamp: -1 }")
    Page<AttributeValueRecord> findByThingAndAidAndTimestampGreaterThanEqual(String thing, String aid,
                                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                                                  Pageable p);
    
//    @RestResource(path = "findByThingAndAidBetween", rel = "findByThingAndAidBetween")
    @Query(value = "{'thing': ?0, 'aid': ?1, 'timestamp': {'$gte': ?2, '$lte': ?3}}", sort = "{timestamp: -1 }")
    Page<AttributeValueRecord> findByThingAndAidAndTimestampBetween(String thing, String aid,
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
                                                                    Pageable p);
}
