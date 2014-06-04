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

	@RequestMapping(value = "all", method = RequestMethod.POST)
	@ResponseBody
	public List<Lab> getLabList() {
		return labService.getAllLabs();
	}
	
	
	@RequestMapping(value = "addlab", method=RequestMethod.POST)
    @ResponseBody
    public void addLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName, 
    		@RequestParam(value="institutes") List<Long> instituteIds) {
		
		List<Institute> instituteList = new ArrayList<Institute>();
		for (Long id: instituteIds) {
			instituteList.add(instituteService.getInstituteById(id));
		}
		
    	Lab newLab = new Lab(firstName,lastName, instituteList);
    	labService.addLab(newLab);
    }
	
	@RequestMapping(value="deletelab",method=RequestMethod.POST)
    @ResponseBody
    public void deleteLab(@RequestParam(value="id") Long id) {
    	labService.deleteLab(id);
    }
    
    @RequestMapping(value = "modifylab", method=RequestMethod.POST)
    @ResponseBody
    public void modifyLab(@RequestParam(value="first") String firstName, @RequestParam(value="last") String lastName,
    		@RequestParam(value="id") Long id, @RequestParam(value="institutes") List<Long> instituteIds){
    	
    	List<Institute> instituteList = new ArrayList<Institute>();
		for (Long instId: instituteIds) {
			//System.out.println("Identifier: " + instId);
			instituteList.add(instituteService.getInstituteById(instId));
		}
 
    	//Create a new lab
    	Lab lab = new Lab(firstName,lastName, instituteList);
    	
    	//Update user
    	labService.updateLab(lab, id);
    }
	

}

