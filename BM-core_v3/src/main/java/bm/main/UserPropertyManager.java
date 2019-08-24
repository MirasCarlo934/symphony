package bm.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;

import bm.tools.Cipher;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import bm.tools.SystemTimer;

//@PropertySource({"file:configuration/bm.properties", "file:configuration/user.properties"})
public class UserPropertyManager extends Properties {
	private Logger LOG;
	private String userPwdPropKey; //injectable
	private File userPropsFile = new File("configuration/user.properties");
	private Cipher cipher;
	private boolean updated;

	public UserPropertyManager(@Value("${log.domain.main}") String logDomain, @Value("${user.pwd.key}") 
			String userPwdPropKey, Cipher cipher, SystemTimer sysTimer) {
		super();
		LOG = Logger.getLogger(logDomain + "." + UserPropertyManager.class.getSimpleName());
		this.userPwdPropKey = userPwdPropKey;
		this.cipher = cipher;
		sysTimer.schedule(new UserPropertyManagerStorer(this));
		
		try {
			this.load(new FileInputStream(userPropsFile));
		} catch (FileNotFoundException e) {
			LOG.fatal("user.properties file not found! WebController cannot be started!");
			Maestro.errorStartup(new Exception("user.properties file not found! WebController "
					+ "cannot be started!", e), 300);
		} catch (IOException e) {
			LOG.fatal("user.properties file cannot be read! WebController cannot be started!");
			Maestro.errorStartup(new Exception("user.properties cannot be read! WebController "
					+ "cannot be started!", e), 300);
		}
		
		LOG.info(UserPropertyManager.class.getSimpleName() + " started!");
	}
	
	@Override
	public Object setProperty(String key, String value) {
		updated = true;
		return super.setProperty(key, value);
	}
	
	public String getUserPwd() {
		if(this.getProperty(userPwdPropKey) == null) {
			return null;
		}
		return cipher.decrypt(this.getProperty(userPwdPropKey));
	}
	
	public void setUserPwd(String pwd) {
		updated = true;
		this.setProperty(userPwdPropKey, cipher.encrypt(pwd));
	}
	
	public void store() throws FileNotFoundException, IOException {
		super.store(new FileOutputStream(userPropsFile), null);
	}
	
	/**
	 * Returns the property key of the user's password in the user.properties file
	 * 
	 * @return The property key
	 */
	public String getUserPwdPropKey() {
		return userPwdPropKey;
	}
	
	private class UserPropertyManagerStorer extends TimerTask {
		private UserPropertyManager upm;
		
		private UserPropertyManagerStorer(UserPropertyManager upm) {
			this.upm = upm;
		}

		@Override
		public void run() {
			if(updated) { //only updates file if this properties object was updated
				LOG.debug("Updating user.properties...");
				try {
					upm.store();
					updated = false;
				} catch (IOException e) {
					LOG.error("User property manager state cannot be stored to user.properties!", e);
				}
			}
		}
	}
}
