package hci.biominer.security;

import java.math.BigInteger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

public class PBKDF2CredentialsMatcher implements CredentialsMatcher {
	private int iterations = 0;
	private int key_length = 0;
	
	
	public PBKDF2CredentialsMatcher(int interations, int key_length) {
		this.iterations = interations;
		this.key_length = key_length;
	}
	
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		UsernamePasswordToken t = (UsernamePasswordToken)token;
		char[] passwordChars = t.getPassword();
		
		SimpleAuthenticationInfo sai = (SimpleAuthenticationInfo)info;
		byte[] saltBytes = sai.getCredentialsSalt().getBytes();
				
		PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, iterations, key_length);
		
		byte[] hashedPassword = null;
		try {
			SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hashedPassword = key.generateSecret(spec).getEncoded();

		} catch(Exception ioex) {
			System.out.println(ioex.getMessage());
		}
		
		//Check if passwords match
		String toCheck =  String.format("%x", new BigInteger(hashedPassword));
		
		if (toCheck.equals((String)sai.getCredentials())) {
			return true;
		} else {
			return false;
		}
	}
	

	
	//build password making machine
	

}
