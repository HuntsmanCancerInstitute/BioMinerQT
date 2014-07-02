package hci.biominer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import hci.biominer.util.Enumerated.ProjectVisibilityEnum;

//Services
import hci.biominer.service.ProjectService;
//import hci.biominer.service.AnalysisService;
import hci.biominer.service.LabService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.DataTrackService;
import hci.biominer.service.SampleService;
import hci.biominer.service.SampleConditionService;
import hci.biominer.service.SamplePrepService;
import hci.biominer.service.SampleTypeService;
import hci.biominer.service.SampleSourceService;

//Models
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.DataTrack;
import hci.biominer.model.Sample;
import hci.biominer.model.SampleCondition;
import hci.biominer.model.SamplePrep;
import hci.biominer.model.SampleType;
import hci.biominer.model.SampleSource;
import hci.biominer.model.access.Lab;

/**
 * 
 * By: Tony Di Sera
 * Date: Apr 17, 2014
 * 
 */
@Controller
@RequestMapping("/project")
public class SubmitController {
	
    @Autowired
    private DataTrackService dataTrackService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private LabService labService;
    
    @Autowired
    private OrganismBuildService organismBuildService;
    
    @Autowired
    private SampleService sampleService;
    
    @Autowired
    private SampleConditionService sampleConditionService;
    
    @Autowired
    private SampleSourceService sampleSourceService;
    
    @Autowired
    private SamplePrepService samplePrepService;
    
    @Autowired
    private SampleTypeService sampleTypeService;
    
    
    /***************************************************
	 * URL: /project/createProject
	 * createProject(): creates a new project and returns the id
	 * method: post
	 * @param name : Project name
	 * @param description: Descripton of the project
	 * @param visibility: Project visibility
	 * @return Long projectID
	 ****************************************************/
    @RequestMapping(value="createProject",method=RequestMethod.POST)
    @ResponseBody
    public Long createProject(@RequestParam(value="name") String name, @RequestParam(value="description") String description,
    		@RequestParam(value="visibility") ProjectVisibilityEnum visibility) {
    	Project project = new Project();
    	project.setName(name);
    	project.setDescription(description);
    	project.setVisibility(visibility);
    	
    	return this.projectService.addProject(project);
    }

    /***************************************************
	 * URL: /project/updateProject
	 * updateProject(): Updates projectInformation
	 * method: post
	 * @param name : Project name
	 * @param description: Description of the project
	 * @param idLab: List of lab ids
	 * @param idOrganismBuild: Organism Build Id
	 * @param visibility: project visibility enum
	 * @param idProject: Project id
	 * @return nada
	 ****************************************************/
    @RequestMapping(value="updateProject",method=RequestMethod.POST)
    @ResponseBody
    public void updateProject(@RequestParam(value="name") String name, @RequestParam(value="description") String description,
    		@RequestParam(value="idLab",required=false) List<Long> idLab, @RequestParam(value="idOrganismBuild",required=false) Long idOrganismBuild,
    		@RequestParam(value="visibility") ProjectVisibilityEnum visibility, @RequestParam(value="idProject") Long idProject) {
    	
    	//Update basic information
    	Project project = new Project();
    	project.setName(name);
    	project.setDescription(description);
    	project.setVisibility(visibility);
    	
    	//Create lab objects and add to project
    	
    	if (idLab != null) {
    		List<Lab> labs = new ArrayList<Lab>();
    		for (Long id: idLab) {
        		labs.add(this.labService.getLab(id));
        	}
    		project.setLabs(labs);
    	}
    	
    	//Create organismBuild object and add to project
    	if (idOrganismBuild != null) {
    		OrganismBuild build = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
        	project.setOrganismBuild(build);
    	}
    	
    	
    	this.projectService.updateProject(project, idProject);
    }

    /***************************************************
	 * URL: /project/getAllProjects
	 * getAllProjects(): gets all projects, regardless of ownership
	 * method: post
	 * @return list of projects
	 ****************************************************/
    @RequestMapping(value="getAllProjects",method=RequestMethod.POST)
    @ResponseBody
    public List<Project> getAllProjects() {
    	List<Project> projects = this.projectService.getAllProjects();
    	return projects;
    }
    
    /***************************************************
	 * URL: /project/deleteProject
	 * deleteProject(): deletes project based on primary index
	 * method: post
	 * @param idProject project identifier
	 ****************************************************/
    @RequestMapping(value="deleteProject",method=RequestMethod.POST)
    @ResponseBody
    public void deleteProject(@RequestParam(value="idProject") Long idProject) {
    	this.projectService.deleteProject(idProject);
    }
    
    /***************************************************
	 * URL: /project/deleteSample
	 * deleteSample(): delete sample based on primary index
	 * method: post
	 * @param idSample primary index of the sample
	 ****************************************************/
    @RequestMapping(value="deleteSample",method=RequestMethod.POST)
    @ResponseBody
    public void deleteSample(@RequestParam(value="idSample") Long idSample) {
    	this.sampleService.deleteSampleById(idSample);
    }
    
    /***************************************************
	 * URL: /project/updateSample
	 * updateSample(): update sample with new information
	 * method: post
	 * @param idSample
	 * @param idProject
	 * @param name
	 * @param idSampleType
	 * @param idSamplePrep
	 * @param idSampleSource
	 * @param idSampleCondition
	 ****************************************************/
    @RequestMapping(value="updateSample",method=RequestMethod.POST)
    @ResponseBody
    public void updateSample(@RequestParam(value="idProject") Long idProject, @RequestParam(value="idSample") Long idSample, @RequestParam(value="name") String name, 
    		@RequestParam(value="idSampleType") Long idSampleType, @RequestParam(value="idSamplePrep") Long idSamplePrep,
    		@RequestParam(value="idSampleSource") Long idSampleSource, @RequestParam(value="idSampleCondition") Long idSampleCondition) {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	SampleType sampleType = this.sampleTypeService.getSampleTypeById(idSampleType);
    	SamplePrep samplePrep = this.samplePrepService.getSamplePrepById(idSamplePrep);
    	SampleCondition sampleCondition = this.sampleConditionService.getSampleConditionById(idSampleCondition);
    	SampleSource sampleSource = this.sampleSourceService.getSampleSourceById(idSampleSource);
    	
    	//Create sample object
    	Sample updatedSample = new Sample(name, sampleType, samplePrep, sampleSource, sampleCondition, project);
    	
    	//Update sample
    	this.sampleService.updateSample(idSample, updatedSample);
    }
    
    
    /***************************************************
	 * URL: /project/createSample
	 * createSample(): update sample with new information
	 * method: post
	 * @param idProject
	 * @param name
	 * @param idSampleType
	 * @param idSamplePrep
	 * @param idSampleSource
	 * @param idSampleCondition
	 ****************************************************/
    @RequestMapping(value="createSample",method=RequestMethod.POST)
    @ResponseBody
    public void createSample(@RequestParam(value="idProject") Long idProject, @RequestParam(value="name") String name, 
    		@RequestParam(value="idSampleType") Long idSampleType, @RequestParam(value="idSamplePrep") Long idSamplePrep, 
    		@RequestParam(value="idSampleSource") Long idSampleSource, @RequestParam(value="idSampleCondition") Long idSampleCondition) {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	SampleType sampleType = this.sampleTypeService.getSampleTypeById(idSampleType);
    	SamplePrep samplePrep = this.samplePrepService.getSamplePrepById(idSamplePrep);
    	SampleCondition sampleCondition = this.sampleConditionService.getSampleConditionById(idSampleCondition);
    	SampleSource sampleSource = this.sampleSourceService.getSampleSourceById(idSampleSource);
    	
    	//Create sample object
    	Sample sample = new Sample(name, sampleType, samplePrep, sampleSource, sampleCondition, project);
    	
    	//Update sample
    	this.sampleService.addSample(sample);
    }
    
    /***************************************************
	 * URL: /project/updateSampleAnalysis
	 * updateSampleAnalysis(): update sample with new information
	 * method: post
	 * @param idSample
	 * @param idAnalysis
	 ****************************************************/
    @RequestMapping(value="updateSampleAnalysis",method=RequestMethod.POST)
    @ResponseBody
    public void updateSampleAnalysis(@RequestParam(value="idSample") Long idSample, @RequestParam(value="idAnalysis") Long idAnalysis) {
    	
    }
    
    /***************************************************
	 * URL: /project/deleteDataTrack
	 * deleteDataTrack(): delete dataTrack based on primary index
	 * method: post
	 * @param idDataTrack primary index of the sample
	 ****************************************************/
    @RequestMapping(value="deleteDataTrack",method=RequestMethod.POST)
    @ResponseBody
    public void deleteDataTrack(@RequestParam(value="idDataTrack") Long idDataTrack) {
    	this.dataTrackService.deleteDataTrackById(idDataTrack);
    }
    
    /***************************************************
	 * URL: /project/updateDataTrack
	 * updateDataTrack(): update dataTrack with new information
	 * method: post
	 * @param idDataTrack
	 * @param idProject
	 * @param name
	 * @param url
	 ****************************************************/
    @RequestMapping(value="updateDataTrack",method=RequestMethod.POST)
    @ResponseBody
    public void updateDataTrack(@RequestParam(value="idProject") Long idProject, @RequestParam(value="idDataTrack") Long idDataTrack, 
    		@RequestParam(value="name") String name, @RequestParam(value="url") String url) {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	
    	//Create datatrack object
    	DataTrack updatedDataTrack = new DataTrack(name, url, project);
    	
    	//Update sample
    	this.dataTrackService.updateDataTrack(idDataTrack, updatedDataTrack);
    }
    
    
    /***************************************************
	 * URL: /project/createDataTrack
	 * createDataTrack(): update dataTrack with new information
	 * method: post
	 * @param idProject
	 * @param name
	 * @param url
	 ****************************************************/
    @RequestMapping(value="createDataTrack",method=RequestMethod.POST)
    @ResponseBody
    public void createDataTrack(@RequestParam(value="idProject") Long idProject, @RequestParam(value="name") String name, 
    		@RequestParam(value="url") String url) {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	
    	//Create sample object
    	DataTrack dataTrack = new DataTrack(name, url, project);
    	
    	//Update sample
    	this.dataTrackService.addDataTrack(dataTrack);
    }
    
    /***************************************************
	 * URL: /project/updateDataTrackAnalysis
	 * createDataTrack(): update sample with new information
	 * method: post
	 * @param idDataTrack
	 * @param idAnalysis
	 ****************************************************/
    @RequestMapping(value="updateDataTrackAnalysis",method=RequestMethod.POST)
    @ResponseBody
    public void updateDataTrackAnalysis(@RequestParam(value="idDataTrack") Long idDataTrack, @RequestParam(value="idAnalysis") Long idAnalysis) {
    	
    }
    
    
    
    
   
    
}
