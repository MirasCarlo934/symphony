package bm.jeep.vo.device;

import bm.comms.Protocol;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

/*
 * This is the object populated when a register JSON transaction arrives from mqtt
 * Sample data is:
 * 		{"RID":"18fe34cf4fc1","CID":"ESP","RTY":"register","name":"Esp12e_RGB","roomID":"MasterBedroom","prodID":"0002"}
 */
public class InboundRegistrationRequest extends JEEPRequest{
	//we are setting the parameters as public to make it easier to access
	private String name;
	private String room;
	private String mac;
	private JSONObject propvals = null; //optional
	private JSONArray proplist = null; //only if CID = "0000"
	private String icon = null; //only if CID = "0000"
	private boolean productless = false;

	public InboundRegistrationRequest(JSONObject json, Protocol protocol, String nameParam, String roomIDParam,
									  String propsParam, String proplistParam, String iconParam) {
		super(json, protocol);
		this.name = json.getString(nameParam);
		this.room = json.getString(roomIDParam);
		try {
			this.propvals = json.getJSONObject(propsParam);
		} catch(JSONException e) {

		}
		try {
			this.proplist = json.getJSONArray(proplistParam);
		} catch (JSONException e) {

		}
		try {
			this.icon = json.getString(iconParam);
		} catch (JSONException e) {

		}
		this.mac = json.getString("RID");
		if(json.getString("CID").equals("0000")) {
			productless = true;
		}
	}
	
	public InboundRegistrationRequest(JEEPRequest request, String nameParam, String roomIDParam, String propValsParam,
									  String proplistParam, String iconParam) {
		super(request);
		this.name = json.getString(nameParam);
		this.room = json.getString(roomIDParam);
		try {
			this.propvals = json.getJSONObject(propValsParam);
		} catch(JSONException e) {

		}
		try {
			this.proplist = json.getJSONArray(proplistParam);
		} catch (JSONException e) {

		}
		try {
			this.icon = json.getString(iconParam);
		} catch (JSONException e) {

		}
		this.mac = json.getString("RID");
		if(json.getString("CID").equals("0000")) {
			productless = true;
		}
	}

	public String getName() {
		return name;
	}

	public String getRoomID() {
		return room;
	}

	public String getMAC() {
		return mac;
	}

	public JSONObject getPropvals() {
		return propvals;
	}

	public JSONArray getProplist() {
		return proplist;
	}

	public String getIcon() {
		return icon;
	}

	public boolean isProductless() {
		return productless;
	}
}
