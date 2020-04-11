package symphony.bm.cache.devices.entities;

import org.springframework.data.annotation.Transient;
import symphony.bm.cache.devices.adaptors.Adaptor;

import java.util.List;

public abstract class Entity {
    @Transient protected List<Adaptor> adaptors;
    
    /**
     * Sets this entity's AND its children's adaptors.
     * @param adaptors The adaptors list
     */
    public void setAdaptors(List<Adaptor> adaptors) {
        this.adaptors = adaptors;
        setAdaptorsToChildren(adaptors);
    }
    
    protected abstract void setAdaptorsToChildren(List<Adaptor> adaptors);
    protected abstract void setSelfToChildren();
}
