package hci.biominer.controller;

import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Role;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Lab;
import hci.biominer.service.UserService;
import hci.biominer.service.LabService;
import hci.biominer.service.InstituteService;
import hci.biominer.service.RoleService;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.security.SecureRandom;

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
	
	@Autowired
	private InstituteService instituteService;
	
	@Autowired
	private RoleService roleService;

    @RequestMapping(value = "all", method = RequestMethod.GET)
    @ResponseBody
    @RequiresPermissions("user:view")
    public List<User> getUserList() {
        return userService.getAllUsers();
    }
    
    @RequestMapping(value = "bylab", method = RequestMethod.GET)
    @ResponseBody
    @RequiresPermissions("user:view")
    public List<User> getUserListByLab(@RequestParam(value="idLab") Long idLab) {
        return userService.getUsersByLab(idLab);
    }
    
    @RequestMapping(value = "adduser", method=RequestMethod.POST)
    @ResponseBody
    @RequiresPermissions("user:add")
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
    	User newUser = new User(firstName,lastName,username,npass,salt,email,phone,roleList,labList, instituteList);
    	userService.addUser(newUser);
    }
    
    @RequestMapping(value = "usernames", method=RequestMethod.GET)
    @ResponseBody
    @RequiresPermissions("user:view")
    public List<String> getUsernames() {
    	return userService.getUsernames();
    }
    
    @RequestMapping(value="deleteuser",method=RequestMethod.DELETE)
    @ResponseBody
    @RequiresPermissions("user:delete")
    public void deleteUser(@RequestParam(value="idUser") Long idUser) {
    	userService.deleteUser(idUser);
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
    	
    	//Get lab
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
    	User user = new User(firstName,lastName,username,npass,salt,email,phone,roleList,labList, instituteList);
    	
    	//Update user
    	userService.updateUser(idUser,user);
    }
    
    
    
    @RequestMapping(value = "checkpass", method=RequestMethod.POST)
    @ResponseBody
    public User checkPass(@RequestParam(value="password") String password, @RequestParam(value="username") String username) {
   
    	//Grab user object that matches username 	
    	User user = this.userService.getUserByUsername(username);
    	
    	//if username doesn't exist, return false
    	if (user == null) {
    		return null;
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
    		return user;
    	} else {
    		return null;
    	}
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
    
    
    
}
