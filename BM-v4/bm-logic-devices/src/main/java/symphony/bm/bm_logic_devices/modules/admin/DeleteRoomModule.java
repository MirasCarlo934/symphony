package symphony.bm.bm_logic_devices.modules.admin;//package bm.main.modules.admin;
//
//import org.json.JSONException;
//
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.rooms.Room;
//import bm.jeep.vo.JEEPRequest;
//import bm.main.engines.DBEngine;
//import bm.main.engines.exceptions.EngineException;
//import bm.main.modules.Module;
//import bm.main.repositories.DeviceRepository;
//import bm.main.repositories.RoomRepository;
//import bm.tools.IDGenerator;
//
//public class DeleteRoomModule extends Module {
////	private String ssidColname;
//	private String roomIDParam;
//	private String roomsTable;
//	private DBEngine dbe;
//	private RoomRepository rr;
//	private IDGenerator idg;
//
//	public DeleteRoomModule(String logDomain, String errorLogDomain, String RTY, String roomIDParam,
//			String roomsTable, /*String ssidColname, *//*MQTTPublisher mp, */DeviceRepository dr,
//			RoomRepository rr, DBEngine dbe, IDGenerator idg) {
//		super(logDomain, errorLogDomain, DeleteRoomModule.class.getSimpleName(), RTY, new String[] {roomIDParam},
//				/*mp, */dr);
//		this.roomIDParam = roomIDParam;
//		this.idg = idg;
//		this.roomsTable = roomsTable;
////		this.ssidColname = ssidColname;
//		this.rr = rr;
//		this.dbe = dbe;
//	}
//
//	@Override
//	protected boolean processRequest(JEEPRequest request) {
//		String roomID = request.getJSON().getString(roomIDParam);
//
//		LOG.debug("Room " + roomID + " deletion requested!");
//		try {
//			deleteRoom(roomID);
//			LOG.info("Room " + roomID + " deleted!");
//		} catch (EngineException e) {
//			LOG.error("Cannot delete room!", e);
//			return false;
//		}
//		return true;
//	}
//
//	public void deleteRoom(String roomID) throws EngineException{
//		Room r = rr.getRoom(roomID);
//
//		LOG.debug("Deleting room " + r.getSSID() + " (" + r.getName() + ")...");
//		try {
//			r.delete(logDomain, true);
//		} catch (AdaptorException e) {
//			LOG.error("Cannot delete room!", e);
//		}
////		Thread t = Thread.currentThread();
////		HashMap<String, Object> args = new HashMap<String, Object>(1,1);
////		args.put("SSID", r.getSSID());
//
////		DeleteDBEReq delete1 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, roomsTable, args);
////		dbe.putRequest(delete1, t, true);
//	}
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		String roomID;
//		try {
//			roomID = request.getJSON().getString(roomIDParam);
//		} catch(JSONException e) {
//			LOG.error("No room ID specified!", e);
//			return false;
//		}
//
//		if(!rr.containsRoom(roomID)) {
//			LOG.error("Invalid room ID specified!");
//			return false;
//		}
//
//		return true;
//	}
//}
