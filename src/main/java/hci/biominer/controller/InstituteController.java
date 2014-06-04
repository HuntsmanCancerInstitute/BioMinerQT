package hci.biominer.controller;

import hci.biominer.model.access.Institute;
import hci.biominer.service.InstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("institute")
public class InstituteController {
	
	@Autowired
	private InstituteService instituteService;
	

	@RequestMapping(value="all",method=RequestMethod.POST)
	@ResponseBody
	public List<Institute> getInstituteList() {
		return instituteService.getAllInstitutes();
	}
	
	

}
