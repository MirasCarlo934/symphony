package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.activitylisteners.ActivityListenerManager;

public abstract class IotResource {
    @Transient @JsonIgnore @Setter(onMethod_ = {@Transient}) protected ActivityListenerManager activityListenerManager;

//    public void addActivityListener(ActivityListener activityListener) {
//        activityListeners.add(activityListener);
//    }
}
