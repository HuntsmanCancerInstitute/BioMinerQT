package hci.biominer.controller;

import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Institute;
import hci.biominer.service.LabService;
import hci.biominer.service.InstituteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("lab")
public class LabController {
	
	@Autowired
	private LabService labService;
	
	@Autowired
	private InstituteService instituteService;

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
	public List<Lab> getLabList() {
		return labService.getAllLabs();
	}
	
	
	@RequestMapping(value = "addlab", method=RequestMethod.PUT)
    @ResponseBody
    public void addLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName) {
    	Lab newLab = new Lab(firstName,lastName);
    	labService.addLab(newLab);
    }
	
	@RequestMapping(value="deletelab",method=RequestMethod.DELETE)
    @ResponseBody
    public void deleteLab(@RequestParam(value="idLab") Long idLab) {
    	labService.deleteLab(idLab);
    }
    
    @RequestMapping(value = "modifylab", method=RequestMethod.PUT)
    @ResponseBody
    public void modifyLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName,
    		@RequestParam(value="idLab") Long idLab){
    	
    	//Create a new lab
    	Lab lab = new Lab(firstName,lastName);
    	
    	//Update user
    	labService.updateLab(lab, idLab);
    }
	

}

