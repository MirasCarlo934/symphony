package bm.tools;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
//import org.apache.tomcat.util.codec.binary.Base64;

/**
 * A cipher for encrypting/decrypting sensitive data. Uses AES encryption (16-byte key & IV)
 * @author carlomiras
 *
 */
public class Cipher {
	private Logger LOG;
//	private byte[] mac;
//	private byte[] iv;
	
	private SecretKeySpec key;
//	private IvParameterSpec ivSpec;
	private javax.crypto.Cipher cipher;

	public Cipher(String logDomain, String VM_ID) {
		LOG = Logger.getLogger(logDomain + "." + Cipher.class.getSimpleName());
//		this.iv = (VM_ID + VM_ID + "ab").getBytes();
//		try {
//			InetAddress address = InetAddress.getLocalHost();
//			NetworkInterface nwi = NetworkInterface.getByInetAddress(address);
//			if(nwi == null) { //if no NetworkInterface (for MAC) found
//				LOG.warn("Key cannot be assembled! Creating backup key...");
//				this.mac = new String("1234567890123456").getBytes();
//			} else {
//				byte[] hardwareMAC = nwi.getHardwareAddress(); //only 6 bytes
//				this.mac = new byte[(hardwareMAC.length + 2) * 2]; //has to be 128 bits / 16 bytes
//				for(int i = 0; i < mac.length; i++) {
//					this.mac[i] = hardwareMAC[i % (hardwareMAC.length-1)];
//				}
//			}
//		} catch(Exception e) {
//			LOG.fatal("Cannot get MAC address of hardware!", e);
//			return;
//		}
		String keyStr = "";
		for(int i = 0; i < VM_ID.length(); i++) {
			keyStr += VM_ID.charAt(i);
		}
		for(int i = 0; i < VM_ID.length(); i++) {
			keyStr += VM_ID.charAt(VM_ID.length() - 1 - i);
		}
//			LOG.fatal(keyStr);
		key = new SecretKeySpec(keyStr.getBytes(), "AES");
		try {
			cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5PADDING");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			LOG.fatal("Cannot create cipher!", e);
		}
	}
	
	public String encrypt(String value) {
		LOG.trace("Encrypting...");
        try {
            String iv = StringTools.generateRandomString(16);
	    	IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encrypted = cipher.doFinal(value.getBytes());

//            return iv + Base64.encodeBase64String(encrypted);
            return iv + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            LOG.fatal("Cannot encrypt!", e);
            return null;
        }
    }

	/**
	 * Decrypts the given string.
	 * @param value
	 * @return
	 */
	public String decrypt(String value) {
		LOG.trace("Decrypting...");
		if(value == null || value.isEmpty()) {
			LOG.debug("Decrypting a null value!");
			return null;
		}
		try {
//			LOG.fatal("TEST2-   " + value);
			String iv = value.substring(0, 16);
			IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, ivSpec);

            value = value.substring(16);
//            byte[] original = cipher.doFinal(Base64.decodeBase64(value));
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(value));

            return new String(original);
        } catch (Exception e) {
            LOG.fatal("Cannot decrypt!", e);
            return null;
        }
	}
	
//	public String in(String s) {
//		byte[] input = s.getBytes();
//		
//		try {
//			cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
//		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
//			LOG.error("Cannot bill in!", e);
//		}
//		byte[] encrypted= new byte[cipher.getOutputSize(input.length)];
//		int enc_len;
//		try {
//			enc_len = cipher.updateRules(input, 0, input.length, encrypted, 0);
//			enc_len += cipher.doFinal(encrypted, enc_len);
//		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
//			LOG.error("Cannot bill in enc_len!", e);
//		}
//		
//		return new String(encrypted);
//	}
//	
//	public String out(String s) {
//		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
//		byte[] decrypted = new byte[cipher.getOutputSize(enc_len)];
//		int dec_len = cipher.updateRules(encrypted, 0, enc_len, decrypted, 0);
//		dec_len += cipher.doFinal(decrypted, dec_len);
//	}
}
