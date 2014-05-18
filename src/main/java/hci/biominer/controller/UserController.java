package hci.biominer.controller;

import hci.biominer.model.access.User;
import hci.biominer.model.access.Lab;
import hci.biominer.service.UserService;
import hci.biominer.service.LabService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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

    @RequestMapping(value = "all", method = RequestMethod.POST)
    @ResponseBody
    public List<User> getUserList() {
        return userService.getAllUsers();
    }
    
    @RequestMapping(value = "bylab", method = RequestMethod.POST)
    @ResponseBody
    public List<User> getUserListByLab(@RequestParam(value="id") Long id) {
        return userService.getUsersByLab(id);
    }
    
    @RequestMapping(value = "adduser", method=RequestMethod.POST)
    @ResponseBody
    public void addUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="admin") boolean admin, @RequestParam(value="lab") Long labId) {
 
    	Lab userLab = labService.getLab(labId);
    	String salt = this.createSalt();
    	String npass = this.createPassword(password, salt);
    	User newUser = new User(firstName,lastName,username,npass,salt,email,phone,admin,userLab);
    	userService.addUser(newUser);
    }
    
    @RequestMapping(value = "usernames", method=RequestMethod.POST)
    @ResponseBody
    public List<String> getUsernames() {
    	return userService.getUsernames();
    }
    
    @RequestMapping(value="deleteuser",method=RequestMethod.POST)
    @ResponseBody
    public void deleteUser(@RequestParam(value="id") Long id) {
    	userService.deleteUser(id);
    }
    
    @RequestMapping(value = "modifyuser", method=RequestMethod.POST)
    @ResponseBody
    public void modifyUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="username") String username,
    		@RequestParam(value="password") String password, @RequestParam(value="email") String email, @RequestParam(value="phone") Long phone, 
    		@RequestParam(value="admin") boolean admin, @RequestParam(value="lab") Long labId, @RequestParam(value="userid") Long userId) {
 
    	//Get lab
    	Lab userLab = labService.getLab(labId);
    	
    	//Create new password if updated
    	String salt = null;
    	String npass = null;
    	if (!password.equals("placeholder")) {
    		salt = this.createSalt();
        	npass = this.createPassword(password, salt);
    	}
    	
    	//Create a new user
    	User user = new User(firstName,lastName,username,npass,salt,email,phone,admin,userLab);
    	
    	//Update user
    	userService.updateUser(userId,user);
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
    
    @RequestMapping(value = "checkpass", method=RequestMethod.POST)
    @ResponseBody
    public boolean checkPass(@RequestParam(value="password") String password, @RequestParam(value="username") String username) {
   
    	//Grab user object that matches username 	
    	User user = this.userService.getUserByUsername(username);
    	
    	//if username doesn't exist, return false
    	if (user == null) {
    		return false;
    	}
    	
    	//build password making machine
    	char[] passwordChars = password.toCharArray();
    	byte[] saltBytes = user.getSalt().getBytes();
    	
    	PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, UserController.ITERATIONS, UserController.KEY_LENGTH);
    	
    	byte[] hashedPassword = null;
    	try {
    		SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    		hashedPassword = key.generateSecret(spec).getEncoded();
 
    	} catch(Exception ioex) {
    		System.out.println(ioex.getMessage());
    	}
    	
    	//Check if passwords match
    	String toCheck =  String.format("%x", new BigInteger(hashedPassword));
    	if (toCheck.equals(user.getPassword())) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
    
}
