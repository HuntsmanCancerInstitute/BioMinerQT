package hci.biominer.controller;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Organism;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.SampleType;
import hci.biominer.model.SamplePrep;
import hci.biominer.model.SampleSource;
import hci.biominer.model.SampleCondition;

import hci.biominer.model.access.Institute;
import hci.biominer.service.InstituteService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.OrganismService;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.SampleTypeService;
import hci.biominer.service.SamplePrepService;
import hci.biominer.service.SampleSourceService;
import hci.biominer.service.SampleConditionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("shared")
public class SharedController {
	
	@Autowired
	private OrganismBuildService organismBuildService;
	
	@Autowired
	private OrganismService organismService;
	
	@Autowired
	private AnalysisTypeService analysisTypeService;
	
	@Autowired
	private SampleTypeService sampleTypeService;
	
	@Autowired
	private SamplePrepService samplePrepService;
	
	@Autowired
	private SampleConditionService sampleConditionService;
	
	@Autowired
	private SampleSourceService sampleSourceService;
	
	@Autowired
	private InstituteService instituteService;
	

	@RequestMapping(value="getAllInstitutes",method=RequestMethod.POST)
	@ResponseBody
	public List<Institute> getInstituteList() {
		return instituteService.getAllInstitutes();
	}
	
	@RequestMapping(value="getAllOrganisms",method=RequestMethod.POST)
	@ResponseBody
	public List<Organism> getAllOrganisms() {
		return organismService.getAllOrganisms();
	}
	
	@RequestMapping(value="getAllBuilds",method=RequestMethod.POST)
	@ResponseBody
	public List<OrganismBuild> getAllBuilds() {
		return organismBuildService.getAllOrganismBuilds();
	}
	
	@RequestMapping(value="getBuildByOrganism",method=RequestMethod.POST)
	@ResponseBody
	public List<OrganismBuild> getBuildByOrganism(@RequestParam(value="idOrganism") Long idOrganism) {
		Organism organism = this.organismService.getOrganism(idOrganism);
		return organismBuildService.getOrganismBuildByOrganism(organism);
	}
	
	@RequestMapping(value="getAllSampleTypes",method=RequestMethod.POST)
	@ResponseBody
	public List<SampleType> getAllSampleTypesTypes() {
		return sampleTypeService.getAllSampleTypes();
	}
	
	@RequestMapping(value="getAllAnalysisTypes",method=RequestMethod.POST)
	@ResponseBody
	public List<AnalysisType> getAllAnalysisTypes() {
		return analysisTypeService.getAllAnalysisTypes();
	}
	
	@RequestMapping(value="getAllSamplePreps",method=RequestMethod.POST)
	@ResponseBody
	public List<SamplePrep> getAllSamplePreps() {
		return samplePrepService.getAllSamplePreps();
	}
	
	@RequestMapping(value="getSamplePrepsBySampleType",method=RequestMethod.POST)
	@ResponseBody
	public List<SamplePrep> getSamplePrepsBySampleType(@RequestParam(value="idSampleType") Long idSampleType) {
		return samplePrepService.getSamplePrepBySampleType(idSampleType);
	}
	
	@RequestMapping(value="getAllSampleSources",method=RequestMethod.POST)
	@ResponseBody
	public List<SampleSource> getAllSampleSources() {
		return sampleSourceService.getAllSampleSources();
	}
	
	@RequestMapping(value="getAllSampleConditions",method=RequestMethod.POST)
	@ResponseBody
	public List<SampleCondition> getAllSampleConditions() {
		return sampleConditionService.getAllSampleConditions();
	}
	
	
}
