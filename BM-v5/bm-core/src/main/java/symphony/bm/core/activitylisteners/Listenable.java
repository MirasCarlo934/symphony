package symphony.bm.core.activitylisteners;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Vector;

public abstract class Listenable {
    @Transient @JsonIgnore @Setter(onMethod_ = {@Transient}) protected List<ActivityListener> activityListeners = new Vector<>();

    public void addActivityListener(ActivityListener activityListener) {
        activityListeners.add(activityListener);
    }
}
