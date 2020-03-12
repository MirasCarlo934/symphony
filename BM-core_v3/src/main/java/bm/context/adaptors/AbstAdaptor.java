package bm.context.adaptors;

import bm.context.properties.Property;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.rooms.Room;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstAdaptor {
	private String id;
	private String name;
	protected final Logger LOG;

	/**
	 * 
	 * @param logDomain
	 * @param adaptorID
	 * @param adaptorName
	 */
	public AbstAdaptor(String logDomain, String adaptorID, String adaptorName/*, String serviceName*/) {
		this.id = adaptorID;
		this.name = adaptorName;
		LOG = LogManager.getLogger(logDomain + "." + adaptorName);
	}
	
	/*
	 * adaptor methods for device
	 */
	public abstract void deviceCreated(Device d, boolean waitUntilCreated) throws AdaptorException;
	public abstract void deviceDeleted(Device d, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
//	public abstract void deviceStateUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
//	public abstract void deviceRoomUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for property
	 */
	public abstract void propertyCreated(Property p, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void propertyDeleted(Property p, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void propertyValueUpdated(Property p, boolean waitUntilUpdated) throws AdaptorException;
	
	/*
	 * adaptor methods for room
	 */
	public abstract void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException;
	public abstract void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException;
	public abstract void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException;
	public abstract void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException;
	
	public String getName() {
		return name;
	}
	
	public String getID() {
		return id;
	}
}
