package hci.biominer.controller;

import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.service.LabService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("lab")
public class LabController {
	
	@Autowired
	private LabService labService;

	@RequestMapping(value = "all", method = RequestMethod.POST)
	@ResponseBody
	public List<Lab> getLabList() {
		return labService.getAllLabs();
	}
	
	@RequestMapping(value = "addlab", method=RequestMethod.POST)
    @ResponseBody
    public void addLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName) {
    	Lab newLab = new Lab(firstName,lastName);
    	labService.addLab(newLab);
    }
	
	@RequestMapping(value="deletelab",method=RequestMethod.POST)
    @ResponseBody
    public void deleteUser(@RequestParam(value="id") Long id) {
    	labService.deleteLab(id);
    }
    
    @RequestMapping(value = "modifylab", method=RequestMethod.POST)
    @ResponseBody
    public void modifyUser(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName,
    		@RequestParam(value="id") Long id){
 
    	//Create a new lab
    	Lab lab = new Lab(firstName,lastName);
    	
    	//Update user
    	labService.updateLab(lab, id);
    }
	

}

