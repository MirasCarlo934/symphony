package symphony.bm.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import symphony.bm.data.iot.attribute.AttributeValueRecord;
import symphony.bm.data.iot.thing.ThingActiveState;

import java.util.Date;

@Component
//@RepositoryRestResource(collectionResourceRel = "things/activestates", path = "things/activestates")
//@CrossOrigin
public interface ThingActiveStateRepository extends MongoRepository<ThingActiveState, Date> {
    
    @Query(sort = "{timestamp: -1 }")
    ThingActiveState findFirstByUid(String uid);
    
    @Query(sort = "{timestamp: -1 }")
    Page<ThingActiveState> findByUid(String uid, Pageable p);
}
