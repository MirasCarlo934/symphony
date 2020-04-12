package symphony.bm.cache.devices.entities;

import org.springframework.data.annotation.Transient;
import symphony.bm.cache.devices.adaptors.AdaptorManager;

public abstract class Entity {
    @Transient protected AdaptorManager adaptorManager;
    
    /**
     * Sets this entity's AND its children's adaptors.
     * @param adaptorManager The adaptors list
     */
    public void setAdaptorManager(AdaptorManager adaptorManager) {
        this.adaptorManager = adaptorManager;
        setAdaptorManagerToChildren(adaptorManager);
    }
    
    protected abstract void setAdaptorManagerToChildren(AdaptorManager adaptors);
    protected abstract void setSelfToChildren();
}
