package symphony.bm.bm_comms.jeep.vo.admin;

import org.json.JSONObject;
import symphony.bm.bm_comms.Protocol;
import symphony.bm.bm_comms.jeep.vo.JeepRequest;

public class JeepAdminRequest extends JeepRequest {
	private String pwd;

	public JeepAdminRequest(JSONObject json, Protocol protocol) {
		super(json, protocol);
		this.pwd = json.getString("pwd");
	}
	
	/**
	 * Returns the pwd of this JeepAdminRequest.
	 * <br/><br/>
	 * <b>NOTE:</b> The pwd is an encrypted string.
	 * 
	 * @return The encrypted pwd string
	 */
	public String getPwd() {
		return pwd;
	}

//	public JeepAdminRequest(JeepRequest request) {
//		super(request);
//	}
}
