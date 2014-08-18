package hci.biominer.controller;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hci.biominer.model.access.User;
import hci.biominer.service.UserService;
import hci.biominer.util.LoginModel;

@Controller
@RequestMapping("security")
public class SecurityController {
	@Autowired
	UserService userService;
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public @ResponseBody LoginModel login(@RequestParam(value="username") String username, @RequestParam(value="password") String password, @RequestParam(value="remember") boolean remember) {
		LoginModel lm = new LoginModel();
	
		UsernamePasswordToken token = new UsernamePasswordToken(username,password,remember);
		
		try {
			SecurityUtils.getSubject().login(token);
			Long id = (Long)SecurityUtils.getSubject().getPrincipal();
			SecurityUtils.getSubject().getSession().setTimeout(1800000);
			User user = userService.getUser(id);
			lm.setUser(user);
		} catch (AuthenticationException e) {
			e.printStackTrace();
			lm.setMessage("Invalid username or password. Please try again.");
		}
		
		return lm;
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
