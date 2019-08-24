package bm.context.adaptors;

import java.util.HashMap;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.context.rooms.Room;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.tools.IDGenerator;

public class DBAdaptor extends AbstAdaptor {
	private IDGenerator idg = new IDGenerator();
	private DBEngine dbe;
	private String devsTable;
	private String propsTable;
	private String roomsTable;

	public DBAdaptor(String logDomain, String adaptorID, String adaptorName, DBEngine dbe, String comsTable,
					 String propsTable, String roomsTable) {
		super(logDomain, adaptorID, adaptorName/*, "database"*/);
		this.dbe = dbe;
		this.devsTable = comsTable;
		this.propsTable = propsTable;
		this.roomsTable = roomsTable;
	}
	
	@Override
	public void deviceCreated(Device d, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting component to DB...");
		HashMap<String, Object> valuesCom = new HashMap<String, Object>(7,1);
		InsertDBEReq insertCom;
		valuesCom.put("SSID", d.getSSID());
		valuesCom.put("topic", d.getTopic());
		valuesCom.put("mac", d.getMAC());
		valuesCom.put("name", d.getName());
		valuesCom.put("room", d.getParentRoom().getSSID());
		valuesCom.put("product", d.getProduct().getSSID());
		valuesCom.put("active", d.isActive());
		valuesCom.put("index", d.getIndex());
		insertCom = new InsertDBEReq(idg.generateERQSRequestID(), dbe, devsTable, valuesCom);
		
		//inserts component to DB
		try {
			dbe.putRequest(insertCom, Thread.currentThread(), waitUntilPersisted);
		} catch (EngineException e1) {
			AdaptorException e = new AdaptorException("Error inserting component to DB!", e1, getName());
			throw e;
		}
	}
	
	@Override
	public void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating component in DB...");
		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> values = new HashMap<String, Object>(2, 1);
		args.put("SSID", d.getSSID());
		values.put("name", d.getName());
		values.put("active", d.isActive());
		values.put("index", d.getIndex());
		if(d.getParentRoom() != null)
			values.put("room", d.getParentRoom().getSSID());
		else 
			values.put("room", null);
		
		UpdateDBEReq update = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, devsTable, values, args);
		try {
			dbe.putRequest(update, Thread.currentThread(), waitUntilUpdated);
			LOG.trace("Update successful!");
		} catch (EngineException e) {
			throw new AdaptorException("Error updating component in DB!", e, getName());
		}
	}
	
//	public void deviceStateUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
//		LOG.trace("Updating device state in DB...");
//		Thread t = Thread.currentThread();
//		IDGenerator idg = new IDGenerator();
//		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
//		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
//		vals.put("active", d.isActive());
//		args.put("SSID", d.getSSID());
//
//		//LOG.trace("Updating active state of component to " + comsTable + " table!");
//		UpdateDBEReq udber = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, devsTable, vals, args);
//		try {
//			dbe.putRequest(udber, t, waitUntilUpdated);
//		} catch (EngineException e) {
//			AdaptorException a = new AdaptorException("Cannot updateRules device state!", e, getName());
//			throw a;
//		}
//	}
//
//	public void deviceRoomUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
//		LOG.trace("Updating device room in DB...");
//		Thread t = Thread.currentThread();
//		IDGenerator idg = new IDGenerator();
//		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
//		HashMap<String, Object> args = new HashMap<String, Object>(1, 1);
//		vals.put("room", d.getParentRoom().getSSID());
//		args.put("SSID", d.getSSID());
//
//		UpdateDBEReq udber = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, devsTable, vals, args);
//		try {
//			dbe.putRequest(udber, t, waitUntilUpdated);
//		} catch (EngineException e) {
//			AdaptorException a = new AdaptorException("Cannot updateRules device room!", e, getName());
//			throw a;
//		}
//	}
	
	public void deviceDeleted(Device c, boolean waitUntilDeleted) throws AdaptorException {
		LOG.trace("Deleting component from DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> args1 = new HashMap<String, Object>(1,1);
		HashMap<String, Object> args2 = new HashMap<String, Object>(1,1);
		args1.put("ssid", c.getSSID());
		args2.put("com_id", c.getSSID());
		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, devsTable, args1);
		DeleteDBEReq delete2 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, propsTable, args2);
		try {
			dbe.putRequest(delete1, t, waitUntilDeleted);
		} catch(EngineException e1) {
			AdaptorException a  = new AdaptorException("Error deleting records from DB!", e1, getName());
			throw a;
		}
		
		LOG.trace("Deleting component properties from DB...");
		//LOG.trace("Deleting properties from DB...");
		try {
			dbe.putRequest(delete2, t, waitUntilDeleted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Error deleting properties from DB!", e, getName());
			throw a;
		}
	}
	
	public void propertyCreated(Property p, boolean waitUntilPersisted) throws AdaptorException {
		//LOG.trace("Persisting property to DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> values = new HashMap<String, Object>(4,1);
		values.put("com_id", p.getDevice().getSSID());
		values.put("prop_name", p.getDisplayName());
		values.put("prop_value", String.valueOf(p.getValue()));
		values.put("index", p.getIndex());
		values.put("prop_type", p.getPropType().getSSID());
		values.put("prop_mode", p.getMode().toString());
		
		LOG.trace("Inserting property " + p.getDevice().getSSID() + "_" + p.getSSID() + " to " + propsTable +
				" table!");
		InsertDBEReq insert = new InsertDBEReq(idg.generateERQSRequestID(), dbe, propsTable, values);
		try {
			dbe.putRequest(insert, t, waitUntilPersisted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Error inserting property to DB!", e, getName());
			throw a;
		}
	}
	
	@Override
	public void propertyValueUpdated(Property p, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating property value of " + p.getSSID() + " to " + p.getValue() + " in " + propsTable
				+ " in DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		vals.put("prop_value", String.valueOf(p.getValue()));
		args.put("com_id", p.getDevice().getSSID());
		args.put("index", p.getIndex());
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, propsTable, vals, args);
		try {
			dbe.putRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot updateRules property value!", e, getName());
			throw a;
		}
	}
	
	/**
	 * <b><i>Defunct.</b><br>
	 * Properties must be deleted from DB alongside their component, deletion of single properties may result to
	 * errors.</i>
	 * <br><br>
	 * Deletes a single property from DB.
	 */
	@Override
	public void propertyDeleted(Property p, boolean waitUntilDeleted) throws AdaptorException {
		LOG.warn("'propertyDeleted()' method is not supported in DBAdaptor.");
	}

	@Override
	public void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException {
		LOG.trace("Persisting room " + r.getSSID() + " to DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1,1);
		vals.put("SSID", r.getSSID());
		vals.put("name", r.getName());
		if (r.getParentRoom() != null) {
			vals.put("parent_room", r.getParentRoom().getSSID());
		}
		vals.put("index", r.getIndex());
		vals.put("color", r.getColor());
		
		InsertDBEReq insert1 = new InsertDBEReq(idg.generateERQSRequestID(), dbe, roomsTable, vals);
		try {
			dbe.putRequest(insert1, t, waitUntilPersisted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot insert room to DB! Query : " + 
					insert1.getQuery(), getName());
			throw a;
		}
	}

	@Override
	public void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException{
		LOG.trace("Deleting room " + r.getSSID() + " from DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> args = new HashMap<String, Object>(1,1);
		args.put("SSID", r.getSSID());
		
		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, roomsTable, args);
		try {
			dbe.putRequest(delete1, t, waitUntilDeleted);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot delete room from DB! Query : " +
					delete1.getQuery(), e, getName());
			throw a;
		}
	}

	@Override
	public void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
		LOG.trace("Updating credentials of room " + r.getSSID() + " (" + r.getName() + ") in DB...");
		Thread t = Thread.currentThread();
		HashMap<String, Object> vals = new HashMap<String, Object>(1, 1);
		HashMap<String, Object> args = new HashMap<String, Object>(2, 1);
		vals.put("name", r.getName());
		vals.put("index", r.getIndex());
//		vals.put("color", r.getColor());
		if(r.getParentRoom() == null) {
			vals.put("parent_room", null);
		} else {
			vals.put("parent_room", r.getParentRoom().getSSID());
		}
		args.put("ssid", r.getSSID());
		UpdateDBEReq udber = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, roomsTable, vals, args);
		try {
			dbe.putRequest(udber, t, waitUntilUpdated);
		} catch (EngineException e) {
			AdaptorException a = new AdaptorException("Cannot updateRules property value!", e, getName());
			throw a;
		}
	}
	
	public void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
		//room updateRules is done in updateRoom() function
	}
}
