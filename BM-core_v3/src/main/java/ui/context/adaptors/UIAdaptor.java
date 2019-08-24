//package ui.context.adaptors;
//
//import java.util.HashMap;
//
//import bm.context.adaptors.AbstAdaptor;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.devices.Device;
//import bm.context.properties.Property;
//import bm.context.rooms.Room;
//import bm.main.engines.DBEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.engines.requests.DBEngine.InsertDBEReq;
//import bm.tools.IDGenerator;
//import ui.context.rooms.UIRoom;
//import ui.main.repositories.UIDeviceRepository;
//import ui.main.repositories.UIRoomRepository;
//
//public class UIAdaptor extends AbstAdaptor {
//	private UIRoomRepository ui_rr;
//	private UIDeviceRepository ui_dr;
//	private DBEngine uidbe;
//	private IDGenerator idg;
//
//	private String ui_roomstable;
//	private String ui_roomstable_ssid;
//	private String ui_roomstable_color;
//
//	public UIAdaptor(String logDomain, String adaptorID, String adaptorName, String ui_roomstable,
//                     String ui_roomstable_ssid, String ui_roomstable_color,
//                     DBEngine uidbe, IDGenerator idg) {
//		super(logDomain, adaptorID, adaptorName);
//		this.uidbe = uidbe;
//		this.idg = idg;
//		this.ui_roomstable = ui_roomstable;
//		this.ui_roomstable_ssid = ui_roomstable_ssid;
//		this.ui_roomstable_color = ui_roomstable_color;
//	}
//
//	@Override
//	public void deviceCreated(Device d, boolean waitUntilCreated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void deviceDeleted(Device d, boolean waitUntilDeleted) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void deviceCredentialsUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void deviceStateUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void deviceRoomUpdated(Device d, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void propertyCreated(Property p, boolean waitUntilPersisted) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void propertyDeleted(Property p, boolean waitUntilDeleted) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void propertyValueUpdated(Property p, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void roomCreated(Room r, boolean waitUntilPersisted) throws AdaptorException {
//		UIRoom uir = ui_rr.getUIRoom(r.getSSID());
//		HashMap<String, Object> values = new HashMap<String, Object>(2, 0);
//		values.put(ui_roomstable_ssid, uir.getSSID());
//		values.put(ui_roomstable_color, uir.getColor());
//		InsertDBEReq dber1 = new InsertDBEReq(idg.generateERQSRequestID(), uidbe, ui_roomstable, values);
//		try {
//			LOG.trace("Persisting UI-relevant Room information to WebUI DB... ");
//			uidbe.putRequest(dber1, Thread.currentThread(), false);
//			LOG.trace("Room persisted!");
//		} catch (EngineException e) {
//			throw new AdaptorException("Room cannot be persisted to WebUI DB!", e);
//		}
//	}
//
//	@Override
//	public void roomDeleted(Room r, boolean waitUntilDeleted) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	//TASK do roomCredentialsUpdated
//	@Override
//	public void roomCredentialsUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void roomParentUpdated(Room r, boolean waitUntilUpdated) throws AdaptorException {
//		// TODO Auto-generated method stub
//
//	}
//
//    public void setUIRoomRepository(UIRoomRepository ui_rr) {
//        this.ui_rr = ui_rr;
//    }
//    public void setUIDeviceRepository(UIDeviceRepository ui_dr) {
//		this.ui_dr = ui_dr;
//	}
//}
