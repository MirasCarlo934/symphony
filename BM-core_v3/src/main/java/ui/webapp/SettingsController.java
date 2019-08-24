package ui.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/settings")
public class SettingsController extends AbstController {

	public SettingsController(@Value("${log.domain.ui}") String logDomain) {
		super(logDomain, SettingsController.class.getSimpleName());
	}
	
	@RequestMapping()
	public String settings(@RequestParam(value="admin", required=false) boolean admin, Model model, 
			HttpServletRequest request, HttpServletResponse response) {
		LOG.debug("Requested 'settings' view access...");
		
		LOG.debug("Dispatching view 'settings'");
		model.addAttribute("admin", admin);
		model.addAttribute("plexURL", userPropManager.get("plex.serverURL"));
		model.addAttribute("plexToken", userPropManager.get("plex.token"));
		return authenticate(model, "/settings");
//		if(authenticate(model)) {
////		if(authenticate(getUserPwdFromCookie(request))) {
//			LOG.debug("Dispatching view 'settings'");
//			model.addAttribute("admin", admin);
//			model.addAttribute("plexURL", userPropManager.get("plex.serverURL"));
//			model.addAttribute("plexToken", userPropManager.get("plex.token"));
//			return "settings";
//		} else {
//			return requireLogin(model, "settings");
//		}
	}
	
	/**
	 * <b><i>AJAX-accessed request mapping</b></i>
	 * <br><br>
	 * Changes VM user password.
	 * 
	 * @param newPWD the new user password
	 * @param oldPWD the old user password
	 * @param checkNewPWD the retyped new user password, must be the same as new user password
	 * @param model the MVC model
	 * @return A notification indicating the success or failure of the password change depending on the supplied 
	 * 		parameters
	 */
	@RequestMapping("/userPwdConfig")
	public String userPwdConfig(@RequestParam(value="newPWD", required=true) String newPWD, 
			@RequestParam(value="oldPWD", required=true) String oldPWD,
			@RequestParam(value="checkNewPWD", required=true) String checkNewPWD, Model model) {
		String userPwd = userPropManager.getUserPwd();
		LOG.debug("User settings updateRules requested... Updating user.properties file!");
		if(newPWD == null || newPWD.isEmpty()) {
			LOG.error("Cannot updateRules password!");
			return notify("error", "New password is empty!", model);
		} else if(!oldPWD.equals(userPwd)) {
			LOG.error("Cannot updateRules password!");
			return notify("error", "Old password is invalid!", model);
		} else if(newPWD.equals(oldPWD)) {
			LOG.error("Cannot updateRules password!");
			return notify("error", "New password is same as old password!", model);
		} else if(!newPWD.equals(checkNewPWD)) {
			LOG.error("New password and confirm new password do not match! Cannot updateRules password!");
			return notify("error.", "New password and confirm new password do not match!", model);
		} else {
//			model.addAttribute(userPropManager.getUserPwdPropKey(), newPWD);
//			addUserPwdCookie(response, newPWD);
			setUserPwdInModel(model, newPWD);
			userPropManager.setUserPwd(newPWD);
			LOG.info("VM password updated!");
			return notify(null, "User settings updated!", model);
		}
	}
	
	@RequestMapping("/VMConfig")
	public String vmConfig(@RequestParam(value="PlexServerURL", required=false) String plexURL, 
			@RequestParam(value="PlexToken", required=false) String plexToken, Model model) {
//			@RequestParam(value="DB_URL", required=false) String dbURL,
//			@RequestParam(value="MQTT_URL", required=false) String mqttURL, 
//			@RequestParam(value="OH_URL", required=false) String ohURL, 
		LOG.debug("VM configuration requested... Updating user.properties file!");
		while(plexURL.endsWith("/")) {
			plexURL = plexURL.substring(0, plexURL.length() - 1);
		}
		userPropManager.setProperty("plex.serverURL", plexURL);
		userPropManager.setProperty("plex.sessionXML_URL", plexURL + "/status/sessions");
		userPropManager.setProperty("plex.token", plexToken);
		return notify(null, "VM configured!", model);
	}
}
