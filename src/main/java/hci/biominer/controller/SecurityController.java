package hci.biominer.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.util.SimpleByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hci.biominer.dao.UserDAO;
import hci.biominer.model.access.User;
import hci.biominer.service.UserService;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.LoginModel;
import hci.biominer.util.MailUtil;

import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.math.BigInteger;



@Controller
@RequestMapping("security")
public class SecurityController {
	@Autowired
	UserService userService;
	
	@Autowired
	private UserController userController;

	private Long timeout = null;
	
	@PostConstruct
	private void loadTimeout() throws Exception {
		if (!BiominerProperties.isLoaded()) {
			BiominerProperties.loadProperties();
		}
		
		try {
			timeout = Long.parseLong(BiominerProperties.getProperty("sessionLength"));
		} catch (NumberFormatException nfe) {
			throw new Exception("The session length set in the propties file is not a void number: " + BiominerProperties.getProperty("sessionLength") );
		}
	}
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public @ResponseBody LoginModel login(@RequestParam(value="username") String username, @RequestParam(value="password") String password, @RequestParam(value="remember") boolean remember)  {
		LoginModel lm = new LoginModel();
	
		UsernamePasswordToken token = new UsernamePasswordToken(username,password,remember);
		
		try {
			SecurityUtils.getSubject().login(token);
			Long id = (Long)SecurityUtils.getSubject().getPrincipal();
			SecurityUtils.getSubject().getSession().setTimeout(timeout);
			User user = userService.getUser(id);
			if (user.getisActive() != null && user.getisActive().equalsIgnoreCase("n")) {
				lm.setMessage("Username is inactivate.");
				SecurityUtils.getSubject().logout();
			}
			else {
				lm.setUser(user);
				lm.setTimeout(timeout);
			}
		} catch (AuthenticationException e) {
			e.printStackTrace();
			lm.setMessage("Invalid username or password. Please try again.");
		}
		
		return lm;
	}
	
	@RequestMapping(value="/resetpassword",method=RequestMethod.POST)
	public @ResponseBody String resetpassword(@RequestParam(value="username") String username, @RequestParam(value="theUrl") String theUrl, @RequestParam(value="remember") boolean remember)  {
		String result = null;
	
		//System.out.println ("[/resetpassword] username is " + username);
		//System.out.println ("[/resetpassword] theUrl is " + theUrl);
		User user = userService.getUserByUsername(username);
		
		if (user != null) {
			// got the user, see if active
			if (user.getisActive() != null && user.getisActive().equalsIgnoreCase("n")) {
				result = "Username is inactivate.";
				return result;
			}
					
			// send the email
			String email = user.getEmail();
			result = "Instructions on how to reset your password have been emailed to you. ";
			
			String url = "http://localhost:8080";
			
			// get the first part of the url
			int ipos = theUrl.toLowerCase().indexOf("/biominer");
			if (ipos != -1) {
				url = theUrl.substring(0, ipos);
				//System.out.println ("[/resetpassword] url is " + url);
			}
			
			long GUID_EXPIRATION = 1800000;  //30 minutes
            UUID uuid = UUID.randomUUID();
            Timestamp ts = new Timestamp(System.currentTimeMillis() + GUID_EXPIRATION);
			
            // update the user with the guid and guidExpiration
			String guid = uuid.toString();   //"d9eb3fa2-171c-4045-b0ac-9b431a072ea2";
            user.setGuid(guid);
            user.setGuidExpiration(ts);
            
            // flush to database
            long userId = user.getIdUser();
            userService.updateUser(userId, user);
            		
            String reset = "<a href=\"" + url + "/biominer/#/changepassword?guid=" + guid +"\">Click here</a>" ;
			String body = "A change of password has been requested for the BioMiner account associated with this email.<br><br>" +
		              reset + " to change your password.<br><br>" + 
		              "This link will expire in 30 minutes.<br>";
			
			String subject = "Reset BioMiner Password";
			
			String [] emails = new String[1];
			emails[0] = email;
			String status = MailUtil.sendMail("DoNotReply@hci.utah.edu",emails,body,subject);
			if (status != null)
			{
				result = "Unable to send password reset instructions, error: " + status;
			}
		} else {
			result = "Username does not exist.";
		}	
		
		//System.out.println ("[/resetpassword] returning result: " + result);
		return result;
	}
	
	@RequestMapping(value="/changepassword",method=RequestMethod.POST)
	public @ResponseBody String changepassword(@RequestParam(value="username") String username, @RequestParam(value="password") String password, @RequestParam(value="passwordconfirm") String passwordconfirm, @RequestParam(value="guid") String guid, @RequestParam(value="remember") boolean remember)  {
		String result = null;
	
		//System.out.println ("[/changepassword] username is " + username);
		//System.out.println ("[/changepassword] guid is " + guid);
		
		// did we get a good guid?
		if (guid == null || guid.equals("")) {
			result = "Parameter error, guid is missing.";
			return result;
		}
		
		// password match?
		if (!password.equals(passwordconfirm)) {
			result = "Passwords do not match.";
			return result;
		}
		
		User user = userService.getUserByUsername(username);
		
		if (user != null) {
			
			String userguid = user.getGuid();
			Timestamp userguidExpiration = user.getGuidExpiration();
			
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            
            if (userguidExpiration == null || userguidExpiration.before(currentTime)) {
            	result = "Password reset request has expired. Try reset password again.";
 
                user.setGuid(null);
                user.setGuidExpiration(null);
                
                // flush to database
                long userId = user.getIdUser();
                userService.updateUser(userId, user);

                return result;
            }
            
            if (userguid == null || !userguid.equals(guid)) {
            	result = "Guid doesn't match. Try reset password again.";
 
                user.setGuid(null);
                user.setGuidExpiration(null);
                
                // flush to database
                long userId = user.getIdUser();
                userService.updateUser(userId, user);

                return result;
            }
            
			
            // update the user with the new password
        	String salt = userController.createSalt();
        	user.setSalt(salt);
        	
        	String npass = userController.createPassword(password, salt);
        	user.setPassword(npass);
            
            user.setGuid(null);
            user.setGuidExpiration(null);
            
            // flush to database
            long userId = user.getIdUser();
            userService.updateUser(userId, user);
            
            result = "Password successfully changed.";
            
		} else {
			result = "Username does not exist.";
		}	
		
		//System.out.println ("[/changepassword] returning result: " + result);
		return result;
	}
	
	@RequestMapping(value="/logout",method=RequestMethod.POST)
	public @ResponseBody void logout() {
		SecurityUtils.getSubject().logout(); 
	}
	
	@RequestMapping(value="/auth",method=RequestMethod.GET)
	public @ResponseBody LoginModel isAuthenticated() {	
		LoginModel lm = new LoginModel();
		
		if (SecurityUtils.getSubject().isAuthenticated()) {
			Long id = (Long)SecurityUtils.getSubject().getPrincipal();
			User user = userService.getUser(id);
			lm.setUser(user);
		}
		
		
			
		return lm;
	}
	
	
	
}
