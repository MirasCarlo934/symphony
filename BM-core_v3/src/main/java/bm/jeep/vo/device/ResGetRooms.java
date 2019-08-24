package bm.jeep.vo.device;

import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;

public class ResGetRooms extends JEEPResponse {

	public ResGetRooms(JEEPRequest request, boolean success, String id, String name) {
		super(request, success);
		addParameter("id", id);
		addParameter("name", name);
	}
}
