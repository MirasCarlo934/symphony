package symphony.bm.cache.devices.entities.deviceproperty;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DevicePropertyValueSnapshotRepository extends
        PagingAndSortingRepository<DevicePropertyValueSnapshot, String> {

//    @Query()
//    List<DevicePropertyValueSnapshot> findByCIDandIndex(String CID, int index);
}
