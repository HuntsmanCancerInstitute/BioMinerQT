package hci.biominer.controller;


import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.service.LabService;
import hci.biominer.service.InstituteService;
import hci.biominer.service.UserService;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("lab")
public class LabController {
	
	@Autowired
	private LabService labService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private InstituteService instituteService;

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
//	@RequiresPermissions("lab:view")
	public List<Lab> getLabList() {
		return labService.getAllLabs();
	}
	
	@RequestMapping(value="getQueryLabs",method=RequestMethod.GET)
	@ResponseBody
	public List<Lab> getQueryLabs() {
		Subject currentUser = SecurityUtils.getSubject();
    	List<Lab> labs;
    	if (currentUser.isAuthenticated()) {
    		//System.out.println("LAB: User is authenticated");
    		Long userId = (Long) currentUser.getPrincipal();
            User user = userService.getUser(userId);
            labs = this.labService.getQueryLabsByVisibility(user);
    	} else {
    		//System.out.println("LAB: User is anonymous");
    		labs = this.labService.getQueryLabsPublic();
    	}
    	
    	return labs;
	}
	
	
	@RequestMapping(value = "addlab", method=RequestMethod.PUT)
    @ResponseBody
    @RequiresPermissions("lab:create")
    public void addLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="email") String email, @RequestParam(value="phone") String phone) {
    	Lab newLab = new Lab(firstName,lastName,email,phone);
    	labService.addLab(newLab);
    }
	
	@RequestMapping(value="deletelab",method=RequestMethod.DELETE)
    @ResponseBody
    @RequiresPermissions("lab:delete")
    public void deleteLab(@RequestParam(value="idLab") Long idLab) {
    	labService.deleteLab(idLab);
    }
    
    @RequestMapping(value = "modifylab", method=RequestMethod.PUT)
    @ResponseBody
    @RequiresPermissions("lab:modify")
    public void modifyLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, @RequestParam(value="email") String email, @RequestParam(value="phone") String phone,
    		@RequestParam(value="idLab") Long idLab){
    	
    	//Create a new lab
    	Lab lab = new Lab(firstName,lastName,email,phone);
    	
    	//Update user
    	labService.updateLab(lab, idLab);
    }
	

}

