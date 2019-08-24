package bm.jeep.vo.device;

import bm.comms.Protocol;
import bm.jeep.vo.JEEPMessage;
import bm.jeep.vo.JEEPResponse;

public class ResPOOP extends JEEPResponse {
	private String propSSID;
	private Object propVal;
	private String propIDParam;
	private String propValParam;

	public ResPOOP(String rid, String cid, String rty, Protocol protocol, String propIDParam, String propValParam,
				   String propSSID, Object propVal) {
		super(rid, cid, rty, protocol, true);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		setPropSSID(propSSID);
		setPropVal(propVal);
	}
	
	public ResPOOP(JEEPMessage message, String propSSID, String propIDParam, String propValParam,
				   Object propVal) {
		super(message, true);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		setPropSSID(propSSID);
		setPropVal(propVal);
	}

	public String getPropSSID() {
		return propSSID;
	}

	public void setPropSSID(String propSSID) {
		this.propSSID = propSSID;
		super.addParameter(propIDParam, propSSID);
	}

	public Object getPropValParam() {
		return propValParam;
	}

	public void setPropVal(Object propVal) {
		super.addParameter(propValParam, propVal);
	}
}
