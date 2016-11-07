package hci.biominer.controller;

import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Role;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Lab;
import hci.biominer.service.UserService;
import hci.biominer.service.LabService;
import hci.biominer.service.InstituteService;
import hci.biominer.service.RoleService;
import hci.biominer.util.MailUtil;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;

import java.math.BigInteger;

@Controller
@RequestMapping("user")
public class UserController {
	private static final int ITERATIONS = 1000;
	private static final int KEY_LENGTH = 192;
	
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private LabService labService;
	
	@Autowired
	private InstituteService instituteService;
	
	@Autowired
	private RoleService roleService;

    @RequestMapping(value = "all", method = RequestMethod.GET)
    @ResponseBody
//    @RequiresPermissions("user:view")
    public List<User> getUserList() {
        return userService.getAllUsers();
    }
    
    @RequestMapping(value = "bylab", method = RequestMethod.GET)
    @ResponseBody
//    @RequiresPermissions("user:view")
    public List<User> getUserListByLab(@RequestParam(value="idLab") Long idLab) {
        return userService.getUsersByLab(idLab);
    }
    
    @RequestMapping(value = "adduser", method=RequestMethod.POST)
    @ResponseBody
    @RequiresPermissions("user:create")
    public void addUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="admin") boolean admin, @RequestParam(value="lab") List<Long> labIds, @RequestParam(value="institutes") List<Long> instituteIds) {
 
    	List<Lab> labList = new ArrayList<Lab>();
    	for (Long idLab: labIds) {
    		labList.add(labService.getLab(idLab));
    	}
    	
    	List<Institute> instituteList = new ArrayList<Institute>();
		for (Long idInstitute: instituteIds) {
			instituteList.add(instituteService.getInstituteById(idInstitute));
		}
		
		//Get role list, currently only one
		List<Role> roleList = new ArrayList<Role>();
		if (admin) {
			Role role = roleService.getRoleByName("admin");
			roleList.add(role);
		}
    	
    	String salt = this.createSalt();
    	String npass = this.createPassword(password, salt);
    	User newUser = new User(firstName,lastName,username,npass,salt,email,phone,null,null,"Y",roleList,labList, instituteList);
    	userService.addUser(newUser);
    }

    @RequestMapping(value = "newuser", method=RequestMethod.POST,produces="text/plain")
    @ResponseBody
    public String newUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="admin") boolean admin, @RequestParam(value="lab") List<Long> labIds, @RequestParam(value="institutes") List<Long> instituteIds,
    		@RequestParam(value="theUrl") String theUrl, HttpServletRequest request) {
 
    	//System.out.println ("[newUser] username: " + username);
    	
    	// we are ignoring all but the first one, and admin will have to edit the user to add more...
    	List<Lab> labList = new ArrayList<Lab>();
    	Lab aLab = null;
    	for (Long idLab: labIds) {
    		aLab = labService.getLab(idLab);
    		labList.add(aLab);
    	}
    	
    	List<Institute> instituteList = new ArrayList<Institute>();
    	Institute anInstitute = null;
		for (Long idInstitute: instituteIds) {
			anInstitute = instituteService.getInstituteById(idInstitute);
			instituteList.add(anInstitute);
		}
			
		// When signing up for an account, admin checkbox is ignored.
		List<Role> roleList = new ArrayList<Role>();
		
        UUID uuid = UUID.randomUUID();
		
        // update the user with the guid
		String guid = uuid.toString();   
    	
    	String salt = this.createSalt();
    	String npass = this.createPassword(password, salt);
    	User newUser = new User(firstName,lastName,username,npass,salt,email,phone,guid,null,"N",roleList,labList, instituteList);
    	userService.addUser(newUser);
    	
    	// send the new user request email
    	User user = userService.getUserByUsername(username);
    	
    	String result = "You will receive and email when your account has been activated.";
    	
    	String status = newUserEmail (user,aLab,anInstitute,theUrl,request);
    	if (status != null) {
    		result = status;
    	}
    	
    	return result;
    	
    }
    
    @RequestMapping(value = "approveuser", method=RequestMethod.POST,produces="text/plain")
    @ResponseBody
    public String approveUser(@RequestParam(value="guid") String guid, @RequestParam(value="iduser") String iduser, @RequestParam(value="deleteuser") String deleteuser,
    		@RequestParam(value="theUrl") String theUrl) {
    	
    	String result = "";
 
    	//System.out.println ("[approveuser] guid: " + guid + " iduser: " + iduser + " deleteuser: " + deleteuser + " theUrl: " + theUrl);

    	// make sure the command line is correct
    	// yea or nay?
    	if (deleteuser.equalsIgnoreCase("n")) {
    		// if the guid matches, activate them
    		Long idUser = Long.valueOf(iduser);
    		User user = userService.getUser(idUser);
    		if (user == null) {
    			result = "User is not in the database.";
    			return result;
    		}
    		
    		String isActive = user.getisActive();
    		if (isActive == null) {
    			isActive = "Y";
    		}
    		
    		if (isActive.equalsIgnoreCase("Y")) {
    			result = "Username already activated.";
    			return result;
    		}
    		
    		String userguid = user.getGuid();
    		if (userguid == null || !userguid.equals(guid)) {
    			result = "Guid doesn't match. Can't activate user: " + user.getUsername() + ".";
    			return result;
    		}
    	
    		// mark them as approved and get rid of the guid
    		user.setisActive("Y");
    		user.setGuid(null);
			user.setGuidExpiration(null);
        
			// flush to database
			long userId = user.getIdUser();
			userService.updateUser(userId, user);
			
			result = "BioMiner User account created.";

			// tell the user they are approved
			String [] emails = new String [1];
			emails[0] = user.getEmail();
			String body = "Your BioMiner user account has been created.<br>";
			String subject = "BioMiner User Account";
			
			String status = MailUtil.sendMail("DoNotReply@hci.utah.edu",emails,body,subject);
			if (status != null) {
				result = "Unable to send user email, error: " + status;
			}
			
			return result;

    	} // end of deleteuser=n

    	if (deleteuser.equalsIgnoreCase("y")) {
    		// they were not approved
    		Long idUser = Long.valueOf(iduser);
    		User user = userService.getUser(idUser);
    		if (user == null) {
    			result = "User is not in the database.";
    			return result;
    		}
    		
    		String isActive = user.getisActive();
    		if (isActive == null) {
    			isActive = "Y";
    		}
    		
    		if (isActive.equalsIgnoreCase("y")) {
    			result = "Username is already activated.";
    			return result;
    		}
    		
    		String userguid = user.getGuid();
    		if (userguid == null || !userguid.equals(guid)) {
    			result = "Guid doesn't match. Can't delete user.";
    			return result;
    		}
    		

    		String useremail = user.getEmail();
		
    		// get rid of them
    		userService.deleteUser(idUser);
		
    		result = "BioMiner User account deleted.";

    		// tell the user they are approved
    		String [] emails = new String [1];
    		emails[0] = useremail;
    		String body = "Your BioMiner user account request has not been approved.<br>";
    		String subject = "BioMiner User Account";
		
    		String status = MailUtil.sendMail("DoNotReply@hci.utah.edu",emails,body,subject);
    		if (status != null) {
    			result = "Unable to send user email, error: " + status;
    		}
		
    		return result;
    	}
    	
    	result = "Invalid approve user url.";
    	return result;
    	
    }
    
    @RequestMapping(value = "usernames", method=RequestMethod.GET)
    @ResponseBody
//    @RequiresPermissions("user:view")
    public List<String> getUsernames() {
    	return userService.getUsernames();
    }
    
    @RequestMapping(value="deleteuser",method=RequestMethod.DELETE)
    @ResponseBody
    @RequiresPermissions("user:delete")
    public void deleteUser(@RequestParam(value="idUser") Long idUser) {
    	userService.deleteUser(idUser);
    }
    
    
    @RequestMapping(value = "selfmodify", method=RequestMethod.POST)
    @ResponseBody
    public void selfModify(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="idUser") Long idUser) {
    	
    	//Create new password if updated
    	String salt = null;
    	String npass = null;
    	if (!password.equals("placeholder")) {
    		salt = this.createSalt();
        	npass = this.createPassword(password, salt);
    	}
    	
    	User user = this.userService.getUser(idUser);
    	user.setEmail(email);
    	user.setFirst(firstName);
    	user.setLast(lastName);
    	user.setUsername(username);
    	user.setSalt(salt);
    	user.setPassword(npass);
    	
    	//Update user
    	userService.updateUser(idUser,user);
    }
    
    @RequestMapping(value = "modifyuser", method=RequestMethod.POST)
    @ResponseBody
    @RequiresPermissions("user:modify")
    public void modifyUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="admin") boolean admin, @RequestParam(value="lab") List<Long> labIds, @RequestParam(value="institutes") List<Long> instituteIds, @RequestParam(value="idUser") Long idUser) {
 
    	//Get lab
    	List<Lab> labList = new ArrayList<Lab>();
    	for (Long idLab: labIds) {
    		labList.add(labService.getLab(idLab));
    	}
    	
    	//Get Institute
    	List<Institute> instituteList = new ArrayList<Institute>();
		for (Long idInstitute: instituteIds) {
			instituteList.add(instituteService.getInstituteById(idInstitute));
		}
		
		//Get role list, currently only one
		List<Role> roleList = new ArrayList<Role>();
		if (admin) {
			Role role = roleService.getRoleByName("admin");
			roleList.add(role);
		}
    	
    	//Create new password if updated
    	String salt = null;
    	String npass = null;
    	if (!password.equals("placeholder")) {
    		salt = this.createSalt();
        	npass = this.createPassword(password, salt);
    	}
    	
    	//Create a new user
    	User user = new User(firstName,lastName,username,npass,salt,email,phone,null,null,"Y",roleList,labList, instituteList);
    	
    	//Update user
    	userService.updateUser(idUser,user);
    }
   
    public String createSalt() {
    	SecureRandom srandom = new SecureRandom();
    	byte[] data = new byte[64];
    	srandom.nextBytes(data);
    	return String.format("%x", new BigInteger(data));
    }
    
    public String createPassword(String password, String salt) {
    	char[] passwordChars = password.toCharArray();
    	byte[] saltBytes = salt.getBytes();
    	
    	PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, UserController.ITERATIONS, UserController.KEY_LENGTH);
    	
    	byte[] hashedPassword = null;
    	try {
    		SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    		hashedPassword = key.generateSecret(spec).getEncoded();
 
    	} catch(Exception ioex) {
    		System.out.println(ioex.getMessage());
    	}
    	
    	return String.format("%x", new BigInteger(hashedPassword));
   
    }
        
    public String newUserEmail (User user, Lab lab, Institute institute, String theUrl, HttpServletRequest request) {
    	String result = null;
    	
    	String url = request.getLocalName();
    	if (url.equals("127.0.0.1")) {
    		url += ":8080";
    	}
    	   	
		//String url = "http://localhost:8080";
		
		// get the first part of the url
		int ipos = theUrl.toLowerCase().lastIndexOf("/biominer");
		if (ipos != -1) {
			url = theUrl.substring(0, ipos);
			//System.out.println ("[newUserEmail] url is " + url);
		}
		
		String labEmail = null;
		String theLab = "";
		
		if (lab != null) {
			labEmail = lab.getEmail();
			theLab = lab.getFirst() + " " + lab.getLast();
		
		}
		
		String theInstitute = "";

		if (institute != null) {
			theInstitute = institute.getName();
		}
		String activate = "<a href=\"" + url + "/biominer/#/approveuser?guid=" + user.getGuid() + "&idUser=" + user.getIdUser() + "&deleteuser=n\">Click here</a>" ;
		String deny = "<a href=\"" + url + "/biominer/#/denyuser?guid=" + user.getGuid() + "&idUser=" + user.getIdUser() + "&deleteuser=y\">Click here</a>" ;
		String body = "The following person has signed up for a BioMiner user account. The user account has been created but not activated.<br><br>" +
				      activate + " to activate the account. BioMiner will automatically send an email to notify the user that his/her user account has been activated.<br><br>" +
					  deny + " to deny and delete the pending user. BioMiner will automatically send an email to notify the user that they have been denied an account with BioMiner.<br><br>" +
					  "<table border='0'><tr><td>Last name:</td><td>" + user.getLast() +
					  "</td></tr><tr><td>First name:</td><td>" + user.getFirst() +
				      "</td></tr><tr><td>Lab:</td><td>" + theLab +
				      "</td></tr><tr><td>Institution:</td><td>" + theInstitute +
				      "</td></tr><tr><td>Email:</td><td>" + user.getEmail() +
					  "</td></tr><tr><td>Phone:</td><td>" + user.getPhone() + 
					  "</td></tr></table>";
					  
		String subject = "BioMiner user account pending approval for " + user.getFirst() + " " + user.getLast();
		
		int numEmails = 1;
		String email = "BioMinerSupport@hci.utah.edu";
		//String email = "tim.mosbruger@hci.utah.edu";
		if (labEmail != null && !email.toLowerCase().equals(labEmail.toLowerCase())) {
			numEmails++;
		}
		
		String [] emails = new String[numEmails];
		if (numEmails == 2) {
			emails[0] = labEmail;
		
			emails[1] = email;
		}
		else {
			emails[0] = email;
		}
			
		String status = MailUtil.sendMail("DoNotReply@hci.utah.edu",emails,body,subject);
		if (status != null) {
			result = "Unable to send new user request email, error: " + status;
		}
		
		// tell the user we got their request
		emails = new String [1];
		emails[0] = user.getEmail();
		body = "Thank you for signing up for a BioMiner account.  We will send you an email once your user account has been activated.<br><br>";
		subject = "BioMiner User Account";
		
		status = MailUtil.sendMail("DoNotReply@hci.utah.edu",emails,body,subject);
		if (status != null) {
			result = "Unable to send new user email, error: " + status;
		}
		
   	
    	return result;
    }
}
