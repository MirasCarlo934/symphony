package symphony.bm.core.iot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.rest.forms.Form;

public abstract class IotResource {
    @Transient @JsonIgnore @Setter(onMethod_ = {@Transient}) protected ActivityListenerManager activityListenerManager;

    public abstract void create();
    public abstract boolean update(String fieldName, Object fieldValue) throws Exception;
    public abstract Object getField(String fieldName);
    public abstract void delete();

//    public void addActivityListener(ActivityListener activityListener) {
//        activityListeners.add(activityListener);
//    }
}
