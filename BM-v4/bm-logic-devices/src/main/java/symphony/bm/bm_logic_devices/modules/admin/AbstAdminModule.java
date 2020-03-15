package symphony.bm.bm_logic_devices.modules.admin;//package bm.main.modules.admin;
//
//import bm.tools.Cipher;
//import org.json.JSONException;
//
//import bm.jeep.vo.JEEPRequest;
//import bm.main.modules.*;
//import bm.main.repositories.DeviceRepository;
//
//public abstract class AbstAdminModule extends Module {
//	private Cipher cipher;
//	private String encryptedPwd;
//	private String pwdParam;
//
//	public AbstAdminModule(String logDomain, String errorLogDomain, String name, String RTY, String[] params,
//                           DeviceRepository dr, Cipher cipher, String pwdParam, String encryptedPwd) {
//		super(logDomain, errorLogDomain, name, RTY, params, dr);
//		this.cipher = cipher;
//		this.encryptedPwd = encryptedPwd;
//		this.pwdParam = pwdParam;
//	}
//
//	public AbstAdminModule(String logDomain, String errorLogDomain, String name, String RTY, String[] params,
//                           DeviceRepository dr, Cipher cipher, String pwdParam, String encryptedPwd,
//                           AbstModuleExtension[] extensions) {
//		super(logDomain, errorLogDomain, name, RTY, params, dr, extensions);
//		this.cipher = cipher;
//		this.encryptedPwd = encryptedPwd;
//		this.pwdParam = pwdParam;
//	}
//
////	@Override
////	public void setRequest(JEEPRequest request) {
////		try {
////			this.request = (JEEPAdminRequest) request;
////		} catch(ClassCastException e) {
////			LOG.error("Request is not a JEEPAdminRequest!", e);
////		}
////	}
//
//	@Override
//	protected boolean checkSecondaryRequestParameters(JEEPRequest request) {
////		JEEPAdminRequest adminReq = (JEEPAdminRequest) request;
//
//		String reqPwd;
//		try {
//			reqPwd = request.getJSON().getString(pwdParam);
//		} catch(JSONException e) {
//			LOG.error("No password supplied!", e);
//			return false;
//		}
//
//		String actualPwd = cipher.decrypt(encryptedPwd);
//		if(reqPwd.equals(actualPwd)) {
//			return super.checkSecondaryRequestParameters(request);
//		} else {
//			LOG.error("Invalid password given!");
//			return false;
//		}
//	}
//}
