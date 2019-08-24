package ui.webapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import bm.main.UserPropertyManager;
import bm.tools.Cipher;

//@Controller
@SessionAttributes("user.pwd")
public class AbstController {
	protected Logger LOG;
	protected String logDomain;
	@Value("${log.domain.ui}") String cipherStr;
	@Value("${ui.userPropManager}") String upmStr;
	@Autowired
	protected Cipher cipher; //injectable
	@Autowired
	protected UserPropertyManager userPropManager;

//	public AbstController(String logDomain, String name, UserPropertyManager userPropManager, 
//			VMCipher cipher) {
//		LOG = Logger.getLogger(logDomain + "." + name);
//		this.userPropManager = userPropManager;
//		this.cipher = cipher;
//	}
	
	public AbstController(String logDomain, String name) {
		LOG = Logger.getLogger(logDomain + "." + name);
//		LOG.fatal(config.getClass());
		LOG.info(name + " started!");
		this.logDomain = logDomain;
//		cipher = (Cipher)config.getApplicationContext().getBean(cipherStr);
//		userPropManager = (UserPropertyManager)config.getApplicationContext().getBean(upmStr);
	}
	
	/**
	 * <b><i>AJAX-accessed request mapping</b></i>
	 * <br><br>
	 * Handles the sending of notifications to a webpage
	 * 
	 * @param header
	 * @param msg
	 * @param model
	 * @return
	 */
	@RequestMapping("/notify")
	public String notify(@RequestParam(value="header", required=true) String header, 
			@RequestParam(value="msg", required=true) String msg, Model model) {
		return notify(header, msg, true, model);
	}
	
	/**
	 * <b><i>AJAX-accessed request mapping</b></i>
	 * <br><br>
	 * Handles the sending of notifications to a webpage/
	 * 
	 * @param header
	 * @param msg
	 * @param model
	 * @return
	 */
	public String notify(String header, String msg, boolean status, Model model) {
		model.addAttribute("header", header);
		model.addAttribute("status", status);
		if(msg == null || msg.isEmpty()) {
			model.addAttribute("msg", null);
		} else {
			model.addAttribute("msg", msg);
		}
		LOG.trace("Sending notification \"" + msg + "\"...");
		return "ajax/notifBox";
	}
	
	public String notifyError(String msg, Model model) {
		return notify("error", msg, false, model);
	}
	
	public String sendResponse(HashMap<String, String> responses, Model model) {
		model.addAttribute("responses", responses);
		return "ajax/responses";
	}
	
	/**
	 * Checks if the current model contains the valid user password in its session.
	 * 
	 * @param pageAddress The page address within the server where the authentication was called from 
	 * 		(eg. devices/overview)
	 * @param model The current MVC model
	 * @return <b>true</b> if the model contains the valid user password, <b>false</b> otherwise
	 */
	protected String authenticate(Model model, String pageAddress) {
		LOG.debug("Accessing a password-restricted view, authenticating session password...");
		if(model.asMap().containsKey(userPropManager.getUserPwdPropKey())) {
			LOG.debug("Session password valid!");
			return authenticate(model.asMap().get(userPropManager.getUserPwdPropKey()).toString(), 
					pageAddress, model);
		} else {
			LOG.warn("Session password empty!");
			return requireLogin(model, pageAddress);
		}
	}
	
	/**
	 * Checks if the specified string is equal to the user's password
	 * 
	 * @param pwd The string containing the encrypted pwd to be specified
	 * @param pageAddress The page address within the server where the authentication was called from 
	 * 		(eg. devices/overview)
	 * @param model	The current MVC model
	 * @return <b>pageAddress</b> if authentication is successful, <i>login template</i> if not, 
	 * 		<i>registration template</i> if user password not yet set
	 */
	protected String authenticate(String pwd, String pageAddress, Model model) {
		LOG.debug("Accessing a password-restricted view, authenticating request-sent password...");
		if(userPropManager.getUserPwd() == null || userPropManager.getUserPwd().isEmpty()) {
			LOG.warn("User password not yet set!");
			return requireRegister(model, pageAddress);
		}
		if(cipher.decrypt(pwd) != null && cipher.decrypt(pwd).equals(userPropManager.getUserPwd())) {
			LOG.debug("Request password valid!");
			return pageAddress;
		} else if(decryptUserPwdInModel(model).equals(userPropManager.getUserPwd())){
			LOG.debug("Session password valid!");
			return pageAddress;
		} else {
			return requireLogin(model, pageAddress);
		}
	}
//	private boolean authenticate(String pwd) {
//		LOG.debug("Accessing a password-restricted view, authenticating request-sent password...");
//		if(pwd.equals(userPropManager.getUserPwd())) {
//			LOG.debug("Request password valid!");
//			return true;
//		} else {
//			LOG.debug("Request password invalid/nonexistent!");
//			return false;
//		}
//	}
	
	/**
	 * Returns the login template name and creates the error message in the model
	 * 
	 * @param model The model
	 * @param redirectTo The page where the user will be redirected to after successful login
	 * @return The login template name
	 */
	protected String requireLogin(Model model, String redirectTo) {
		LOG.debug("Dispatching view 'login'");
		setErrorNotifInModel(model, "Please log-in to continue");
		model.addAttribute("redirect", redirectTo);
		return "login";
	}
	
	/**
	 * Returns the registration template name and creates the error message in the model
	 * 
	 * @param model The model
	 * @param redirectTo The page where the user will be redirected to after successful login
	 * @return The login template name
	 */
	protected String requireRegister(Model model, String redirectTo) {
		LOG.debug("Dispatching view 'register'");
		setErrorNotifInModel(model, "Password not yet set");
		model.addAttribute("redirect", redirectTo);
		return "register";
	}
	
	/**
	 * Returns the redirect page. The redirect page only contains the location of the webpage to be redirected
	 * to. The redirection must be handled by a client-side script (ie. JavaScript, JQuery)
	 * 
	 * @param model The model
	 * @param requestMapping The request mapping to redirect to
	 * @return
	 */
	protected String redirect(Model model, String requestMapping) {
		LOG.debug("Redirecting to '" + requestMapping + "' view");
		model.addAttribute("newLocation", requestMapping);
		return "ajax/redirect";
	}
	
//	private void storeUserProps(String comments) throws FileNotFoundException, IOException {
//		userProps.store(new FileOutputStream(userPropsFile), comments);
//	}
	
	protected String getUserPwdFromCookie(HttpServletRequest request) {
		Cookie pwd = null;
		Iterator<Cookie> cookies = Arrays.asList(request.getCookies()).iterator();
		while(cookies.hasNext()) {
			Cookie cookie = cookies.next();
			if(cookie.getName().equals(userPropManager.getUserPwdPropKey())) {
				pwd = cookie;
				break;
			}
		}
		if(pwd != null) {
			LOG.fatal(cipher.decrypt(pwd.getValue()));
			return cipher.decrypt(pwd.getValue());
		} else {
			return null;
		}
	}
	
	protected void addUserPwdCookie(HttpServletResponse httpResponse, String pwd) {
		LOG.debug("Adding cookie for encrypted user password...");
		Cookie cookie = new Cookie(userPropManager.getUserPwdPropKey(), cipher.encrypt(pwd));
		httpResponse.addCookie(cookie);
	}
	
//	private void removeUserPwdCookie(HttpServletRequest request) {
//		request.
//	}
	
	protected String decryptUserPwdInModel(Model model) {
		if(model.containsAttribute(userPropManager.getUserPwdPropKey())) {
			return cipher.decrypt(model.asMap().get(userPropManager.getUserPwdPropKey()).toString());
		} else {
			return null;
		}
	}
	
	protected void setUserPwdInModel(Model model, String pwd) {
		model.addAttribute(userPropManager.getUserPwdPropKey(), cipher.encrypt(pwd));
	}
	
	protected void removeUserPwdInModel(Model model) {
		model.asMap().remove(userPropManager.getUserPwdPropKey());
	}
	
	protected void setErrorNotifInModel(Model model, String errorMsg) {
		model.addAttribute("error", errorMsg);
	}
	
	/**
	 * Dispatches the specified view. This method logs the dispatch first before returning the view. 
	 * 
	 * @param view the view to be dispatched
	 * @return the view to be dispatched
	 */
	protected String dispatchView(String view) {
		LOG.debug("Dispatching view '" + view + "'");
		return view;
	}
}
