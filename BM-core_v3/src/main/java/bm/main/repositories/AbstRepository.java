package bm.main.repositories;

import org.apache.log4j.Logger;

import bm.comms.mqtt.MQTTPublisher;

/**
 * @deprecated ALL REPOSITORY OBJECTS NOW IMPLEMENT INITIALIZABLE INTERFACE.
 * <br/><br/>
 * The superclass extended by Repository objects
 * <br><br>
 * Repository objects are the ones that hold all data related to the smarthome elements (ie. devices, properties, rooms).
 * The Repository object prevents heavy DB-dependenct operation.
 * 
 * @author carlomiras
 *
 */
public abstract class AbstRepository {
	protected Logger LOG;
	protected String logDomain;
	protected String name;
	protected boolean initialized = false;
	protected MQTTPublisher mp;

	public AbstRepository(String logDomain, String name) {
		this.logDomain = logDomain;
		this.name = name;
		LOG = Logger.getLogger(logDomain + "." + name);
	}
	
	public void initialize() {
		LOG.info("Initializing " + name + "...");
		try {
			initializeProcess();
		} catch (Exception e) {
			LOG.fatal("Cannot initialize " + name + "!", e);
			return;
		}
		LOG.info(name + " has initialized successfully!");
		initialized = true;
	}
	
	protected abstract void initializeProcess();
	
	public boolean hasInitialized() {
		return initialized;
	}
}
