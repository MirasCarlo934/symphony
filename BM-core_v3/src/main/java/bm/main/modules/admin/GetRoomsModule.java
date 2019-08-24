//package bm.main.modules.admin;
//
//import bm.context.rooms.Room;
//import bm.jeep.vo.JEEPRequest;
//import bm.jeep.vo.device.ResGetRooms;
//import bm.main.modules.Module;
//import bm.main.repositories.DeviceRepository;
//import bm.main.repositories.RoomRepository;
//
//public class GetRoomsModule extends Module {
//	private RoomRepository rr;
//
//	public GetRoomsModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp,*/
//			DeviceRepository dr, RoomRepository rr) {
//		super(logDomain, errorLogDomain, GetRoomsModule.class.getSimpleName(), RTY, null, /*mp, */dr);
//		this.rr = rr;
//	}
//
//	@Override
//	protected boolean processRequest(JEEPRequest request) {
//		Room[] rooms = rr.getAllRooms();
//		for(int i = 0; i < rooms.length; i++) {
//			ResGetRooms response = new ResGetRooms(request, true, rooms[i].getSSID(), rooms[i].getName());
//			request.getProtocol().getSender().send(response);
//		}
//		return true;
//	}
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		return true;
//	}
//
//}
