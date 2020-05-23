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
import symphony.bm.data.iot.thing.ThingActiveState;

import java.util.Date;

@Component
@RepositoryRestResource(collectionResourceRel = "thingActiveStates", path = "thingActiveStates")
@CrossOrigin
public interface ThingActiveStateRepository extends MongoRepository<ThingActiveState, Date> {
    
    @Query(sort = "{timestamp: -1 }")
    ThingActiveState findFirstByUid(String uid);
    
    @Query(sort = "{timestamp: -1 }")
    Page<ThingActiveState> findByUid(String uid, Pageable p);
    
    @RestResource(path = "findByUidFrom", rel = "findByUidFrom")
    @Query(sort = "{timestamp: -1 }")
    Page<ThingActiveState> findByUidAndTimestampGreaterThanEqual(String uid,
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                                                 Pageable p);
    
    @RestResource(path = "findByUidBetween", rel = "findByUidBetween")
    @Query(value = "{'uid': ?0, 'timestamp': {'$gte': ?1, '$lte': ?2}}", sort = "{timestamp: -1 }")
    Page<ThingActiveState> findByUidAndTimestampBetween(String uid,
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
                                                        Pageable p);
}
