package hci.biominer.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hci.biominer.util.BooleanModel;
import hci.biominer.util.Enumerated.FileStateEnum;
import hci.biominer.util.Enumerated.ProjectVisibilityEnum;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.FileMeta;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.IntervalTrees;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.PreviewMap;
import hci.biominer.util.VCFUtilities;
import hci.biominer.service.DashboardService;
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
import hci.biominer.service.AnalysisService;
import hci.biominer.service.FileUploadService;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.UserService;
import hci.biominer.service.InstituteService;

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
import hci.biominer.model.FileUpload;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Lab;
import hci.biominer.model.genome.Genome;

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
    private AnalysisService analysisService;
    
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
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private AnalysisTypeService analysisTypeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private InstituteService instituteService;
    
    @Autowired
    private DashboardService dashboardService;
   
    /***************************************************
	 * URL: /project/createProject
	 * createProject(): creates a new project and returns the id
	 * method: post
	 * @param name : Project name
	 * @param description: Descripton of the project
	 * @param visibility: Project visibility
	 * @return Long projectID
	 ****************************************************/
    @RequestMapping(value="createProject",method=RequestMethod.PUT)
    @ResponseBody
    public Long createProject(
    		@RequestParam(value="name") String name, 
    		@RequestParam(value="description") String description,
    		@RequestParam(value="visibility") ProjectVisibilityEnum visibility, 
    		@RequestParam(value="idLab") List<Long> idLab, 
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild,
    		@RequestParam(value="idInstitute") List<Long> idInstitute) {
    	
    	
    	Project project = new Project();
    	project.setName(name);
    	project.setDescription(description);
    	
    	//Create lab objects and add to project
		List<Lab> labs = new ArrayList<Lab>();
		for (Long id: idLab) {
    		labs.add(this.labService.getLab(id));
    	}
		project.setLabs(labs);
    	
    	
    	//Create intitute objects and add to project
		List<Institute> institutes = new ArrayList<Institute>();
		for (Long id: idInstitute) {
			institutes.add(this.instituteService.getInstituteById(id));
		}
		project.setInstitutes(institutes);
    	
    	
    	//Create organismBuild object and add to project
		OrganismBuild build = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
    	project.setOrganismBuild(build);
    	

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
    @RequestMapping(value="updateProject",method=RequestMethod.PUT)
    @ResponseBody
    public void updateProject(@RequestParam(value="name") String name, @RequestParam(value="description") String description,
    		@RequestParam(value="idLab",required=false) List<Long> idLab, @RequestParam(value="idOrganismBuild",required=false) Long idOrganismBuild,
    		@RequestParam(value="visibility") ProjectVisibilityEnum visibility, @RequestParam(value="idProject") Long idProject,
    		@RequestParam(value="idInstitute",required=false) List<Long> idInstitute, @RequestParam(value="dataUrls", required=false) String dataUrls) {
    	
    	//Update basic information
    	Project project = new Project();
    	project.setName(name);
    	project.setDescription(description);
    	project.setVisibility(visibility);
    	project.setDataUrls(dataUrls);
    	
    	//Create lab objects and add to project
    	
    	if (idLab != null) {
    		List<Lab> labs = new ArrayList<Lab>();
    		for (Long id: idLab) {
        		labs.add(this.labService.getLab(id));
        	}
    		project.setLabs(labs);
    	}
    	
    	//Create intitute objects and add to project
    	if (idInstitute != null) {
    		List<Institute> institutes = new ArrayList<Institute>();
    		for (Long id: idInstitute) {
    			institutes.add(this.instituteService.getInstituteById(id));
    		}
    		project.setInstitutes(institutes);
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
    @RequestMapping(value="getAllProjects",method=RequestMethod.GET)
    @ResponseBody
    public List<Project> getAllProjects() {
    	List<Project> projects = this.projectService.getAllProjects();
    	return projects;
    }
    
    /***************************************************
	 * URL: /project/getProjectsByUser
	 * getAllProjects(): gets all projects, regardless of ownership
	 * method: post
	 * @return list of projects
	 ****************************************************/
    @RequestMapping(value="getProjectsByVisibility",method=RequestMethod.GET)
    @ResponseBody
    public List<Project> getProjectsByVisibility() {
    	Subject currentUser = SecurityUtils.getSubject();
    	List<Project> projects;
    	if (currentUser.isAuthenticated()) {
    		System.out.println("User is authenitcated");
    		Long userId = (Long) currentUser.getPrincipal();
            User user = userService.getUser(userId);
        	projects = this.projectService.getProjectByVisibility(user);
    	} else {
    		System.out.println("User is anonymous");
    		projects = this.projectService.getPublicProjects();
    	}
    	
    	return projects;
    }
    
    /***************************************************
	 * URL: /project/getPublicProjects
	 * getPublicProjects(): gets all projects, regardless of ownership
	 * method: post
	 * @return list of public projects
	 ****************************************************/
    @RequestMapping(value="getPublicProjects",method=RequestMethod.GET)
    @ResponseBody
    public List<Project> getPublicProjects() {
    	List<Project> projects = this.projectService.getPublicProjects();
    	return projects;
    }
    
    
    /***************************************************
	 * URL: /project/deleteProject
	 * deleteProject(): deletes project based on primary index
	 * method: post
	 * @param idProject project identifier
	 ****************************************************/
    @RequestMapping(value="deleteProject",method=RequestMethod.DELETE)
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
    @RequestMapping(value="deleteSample",method=RequestMethod.DELETE)
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
    @RequestMapping(value="updateSample",method=RequestMethod.PUT)
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
    	
    	//Create updated sample from existing
    	Sample updatedSample = this.sampleService.getSampleById(idSample);
    	updatedSample.setName(name);
    	updatedSample.setProject(project);
    	updatedSample.setSampleCondition(sampleCondition);
    	updatedSample.setSamplePrep(samplePrep);
    	updatedSample.setSampleSource(sampleSource);
    	updatedSample.setSampleType(sampleType);
    	   	
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
    @RequestMapping(value="createSample",method=RequestMethod.PUT)
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
	 * URL: /project/createAnalysis
	 * createAnalysis(): creates a new project and returns the id
	 * method: post
	 * @param name : Analysis name
	 * @param description: Descripton of the analyis
	 * @param date: Date of analysis
	 * @param idAnalysisType: analysis type
	 * @param idProject: project
	 * @param List<Long> idSampleList: list of sample ids
	 * @param List<Long> idDataTrackList: list of datetrack ids
	 * @param List<Long> idFileUploadList: list of file upload ids
	 * @return Long idAnalysis
	 ****************************************************/
    @RequestMapping(value="createAnalysis",method=RequestMethod.PUT)
    @ResponseBody
    public Long createAnalysis(@RequestParam(value="name") String name, @RequestParam(value="description",required=false) String description, @RequestParam(value="idProject") Long idProject,
    		@RequestParam(value="date") Long date, @RequestParam(value="idAnalysisType") Long idAnalysisType, @RequestParam(value="idSampleList",required=false) List<Long> idSampleList,
    		@RequestParam(value="idDataTrackList",required=false) List<Long> idDataTrackList, @RequestParam(value="idFileUpload") Long idFileUpload,
    		HttpServletResponse response) {

    	//Create analysis basics.
    	AnalysisType analysisType = analysisTypeService.getAnalysisTypeById(idAnalysisType);
    	Project project = projectService.getProjectById(idProject);
    	
    	Analysis analysis = new Analysis();
    	analysis.setName(name);
    	analysis.setDescription(description);
    	analysis.setDate(date);
    	analysis.setAnalysisType(analysisType);
    	analysis.setProject(project);
    	
    	//Assign datatracks
    	if (idDataTrackList != null) {
    		List<DataTrack> dataTracks = new ArrayList<DataTrack>();
    		for (Long idDataTrack: idDataTrackList) {
    			DataTrack dataTrack = this.dataTrackService.getDataTrackById(idDataTrack);
    			dataTracks.add(dataTrack);
    		}
    		analysis.setDataTracks(dataTracks);
    	}
    	
    	//Assign samples
    	if (idSampleList != null) {
    		List<Sample> samples = new ArrayList<Sample>();
    		for (Long idSample: idSampleList) {
    			Sample sample = this.sampleService.getSampleById(idSample);
    			samples.add(sample);
    		}
    		analysis.setSamples(samples);
    	}
    	
    	//Assign files
    	FileUpload fileUpload = this.fileUploadService.getFileUploadById(idFileUpload);
    	analysis.setFile(fileUpload);
    	
    	//Update database
    	Date submitDate = new Date();
    	this.dashboardService.updateSubmissionDate(submitDate.getTime());
    
    	return this.analysisService.addAnalysis(analysis);
    }

    /***************************************************
	 * URL: /project/updateAnalysis
	 * updateAnalysis(): Updates analysis information
	 * method: post
	 * @param idAnalysis: Analysis id
	 * @param name : Analysis name
	 * @param description: Descripton of the analyis
	 * @param date: Date of analysis
	 * @param idAnalysisType: analysis type
	 * @param idProject: project
	 * @param List<Long> idSampleList: list of sample ids
	 * @param List<Long> idDataTrackList: list of datetrack ids
	 * @param List<Long> idFileUploadList: list of file upload ids
	 ****************************************************/
    @RequestMapping(value="updateAnalysis",method=RequestMethod.PUT)
    @ResponseBody
    public void updateAnalysis(@RequestParam(value="idAnalysis") Long idAnalysis, @RequestParam(value="name") String name, @RequestParam(value="description",required=false) String description, @RequestParam(value="idProject") Long idProject,
    		@RequestParam(value="date") Long date, @RequestParam(value="idAnalysisType") Long idAnalysisType, @RequestParam(value="idSampleList",required=false) List<Long> idSampleList,
    		@RequestParam(value="idDataTrackList",required=false) List<Long> idDataTrackList, @RequestParam(value="idFileUpload") Long idFileUpload,
    		HttpServletResponse response) {
    	
 
    	//Create analysis basics.
    	AnalysisType analysisType = analysisTypeService.getAnalysisTypeById(idAnalysisType);
    	Project project = projectService.getProjectById(idProject);
    	
    	Analysis analysis = analysisService.getAnalysisById(idAnalysis);
    	analysis.setName(name);
    	analysis.setDescription(description);
    	analysis.setDate(date);
    	analysis.setAnalysisType(analysisType);
    	analysis.setProject(project);
    	
    	//Assign datatracks
    	if (idDataTrackList != null) {
    		List<DataTrack> dataTracks = new ArrayList<DataTrack>();
    		for (Long idDataTrack: idDataTrackList) {
    			DataTrack dataTrack = this.dataTrackService.getDataTrackById(idDataTrack);
    			dataTracks.add(dataTrack);
    		}
    		analysis.setDataTracks(dataTracks);
    	}
    	
    	//Assign samples
    	if (idSampleList != null) {
    		List<Sample> samples = new ArrayList<Sample>();
    		for (Long idSample: idSampleList) {
    			Sample sample = this.sampleService.getSampleById(idSample);
    			samples.add(sample);
    		}
    		analysis.setSamples(samples);
    	}
    	
    	//Assign files
    	FileUpload fileUpload = this.fileUploadService.getFileUploadById(idFileUpload);
    	analysis.setFile(fileUpload);
    
    	this.analysisService.updateAnalysis(analysis,idAnalysis);
    
    }
    

    /***************************************************
   * URL: /project/getAllAnalyses
   * getAllAnalyses(): get all analyses 
   * method: post
   * @return list of analyses
   ****************************************************/
    @RequestMapping(value="getAllAnalyses",method=RequestMethod.GET)
    @ResponseBody
    public List<Analysis> getAllAnalyses() {
      Subject currentUser = SecurityUtils.getSubject();
      List<Analysis> analyses;
      if (currentUser.isAuthenticated()) {
        Long userId = (Long) currentUser.getPrincipal();
            User user = userService.getUser(userId);
            analyses = this.analysisService.getAnalysesByVisibility(user);
      } else {
        analyses = this.analysisService.getAnalysesPublic();
      }
      return analyses;
    }


    /***************************************************
	 * URL: /project/getAnalysisByProject
	 * getAnalysisByProject(): get analyses by project
	 * method: post
	 * @param Long idProject: project identifier
	 * @return list of analyses
	 ****************************************************/
    @RequestMapping(value="getAnalysisByProject",method=RequestMethod.GET)
    @ResponseBody
    public List<Analysis> getAnalysisByProject(@RequestParam(value="idProject") Long idProject) {
    	Project project = this.projectService.getProjectById(idProject);
    	List<Analysis> analyses = this.analysisService.getAnalysesByProject(project);
    	return analyses;
    }
    
    /***************************************************
	 * URL: /project/deleteAnalysis
	 * deleteAnalysis(): deletes analysis based on primary index
	 * method: post
	 * @param idAnalysis analysis identifier
	 ****************************************************/
    @RequestMapping(value="deleteAnalysis",method=RequestMethod.DELETE)
    @ResponseBody
    public void deleteAnalysis(@RequestParam(value="idAnalysis") Long idAnalysis) throws Exception {
    	Analysis a = this.analysisService.getAnalysisById(idAnalysis);
    	if (IntervalTrees.doesChipIntervalTreeExist(a)) {
    		IntervalTrees.removeIntervalTree(a);
    	}
    	
    	this.analysisService.deleteAnalysis(idAnalysis);
    }
    
    
    /***************************************************
	 * URL: /project/addSamplePrep
	 * addSamplePrep(): add new sample prep to database
	 * method: post
	 * @param description: description of the sample prep
	 * @param idSampleType: primary index of the associated sample type
	 ****************************************************/
    
    @RequestMapping(value="addSamplePrep",method=RequestMethod.PUT)
    @ResponseBody
    public SamplePrep addSamplePrep(@RequestParam(value="description") String description, @RequestParam(value="idSampleType") Long idSampleType) throws Exception {
    	SampleType sampleType = this.sampleTypeService.getSampleTypeById(idSampleType);
    	SamplePrep samplePrep  = new SamplePrep();
    	samplePrep.setSampleType(sampleType);
    	samplePrep.setDescription(description);
 
    	this.samplePrepService.addSamplePrep(samplePrep);
    	
    	return samplePrep;
    }
    
 
    /***************************************************
	 * URL: /project/addSampleCondition
	 * addSampleCondition(): add new sample condition to database
	 * method: post
	 * @param condition: sample condition
	 ****************************************************/
    @RequestMapping(value="addSampleCondition",method=RequestMethod.PUT)
    @ResponseBody
    public SampleCondition addSampleCondition(@RequestParam(value="condition") String condition, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
    	OrganismBuild ob = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
    	
    	SampleCondition sampleCondition = new SampleCondition();
    	sampleCondition.setCond(condition);
    	sampleCondition.setOrganismBuild(ob);

    	this.sampleConditionService.addSampleCondition(sampleCondition);
    	
    	return sampleCondition;
    }
    
    /***************************************************
	 * URL: /project/addSampleSource
	 * addSampleSource(): add new sample source to the database
	 * method: post
	 * @param source: sample source 
	 ****************************************************/
    @RequestMapping(value="addSampleSource",method=RequestMethod.PUT)
    @ResponseBody
    public SampleSource addSampleSource(@RequestParam(value="source") String source, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
    	OrganismBuild ob = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
    	
    	SampleSource sampleSource = new SampleSource();
    	sampleSource.setSource(source);
    	sampleSource.setOrganismBuild(ob);
    	
    	this.sampleSourceService.addSampleSource(sampleSource);
    	
    	return sampleSource;
    }

   
    
    /***************************************************
	 * URL: /project/getVisibleLabs
	 * getVisibleLabs: return a list of labs visible to the user
	 * method: get
	 ****************************************************/
    @RequestMapping(value="getVisibleLabs",method=RequestMethod.GET)
    @ResponseBody
    public List<Lab> getVisibleLabs() {
    	Subject currentUser = SecurityUtils.getSubject();
    	List<Lab> labs;
    	if (currentUser.isAuthenticated()) {
    		if (currentUser.hasRole("admin")) {
    			labs = this.labService.getAllLabs();
    		} else {
    			Long userId = (Long) currentUser.getPrincipal();
                User user = userService.getUser(userId);
                labs = user.getLabs();
    		}
    	} else {
    		labs = null;
    	}
    	
    	return labs;
    }
    
    /***************************************************
	 * URL: /project/getVisibleInstitutes
	 * getVisibleInstitutes: return a list of institutes visible to the user
	 * method: get
	 ****************************************************/
    @RequestMapping(value="getVisibleInstitutes",method=RequestMethod.GET)
    @ResponseBody
    public List<Institute> getVisibleInstitutes() {
    	Subject currentUser = SecurityUtils.getSubject();
    	List<Institute> institutes;
    	if (currentUser.isAuthenticated()) {
    		if (currentUser.hasRole("admin")) {
    			institutes = this.instituteService.getAllInstitutes();
    		} else {
    			Long userId = (Long) currentUser.getPrincipal();
                User user = userService.getUser(userId);
                institutes = user.getInstitutes();
    		}
    	} else {
    		institutes = null;
    	}
    	
    	return institutes;
    }
    
    /***************************************************
	 * URL: /project/uploadDataTrack
	 * This method uploads a datatrack to biominer
	 ****************************************************/
	@RequestMapping(value="uploadDataTrack",method=RequestMethod.POST)
	public @ResponseBody
	FileMeta uploadDataTrack(
			@RequestParam("file") MultipartFile file,  
			@RequestParam(value="index") Integer index, 
			@RequestParam(value="total") Integer total, 
			@RequestParam(value="name") String filename,
			@RequestParam(value="idProject") Long idProject,
			HttpServletResponse response) throws Exception {
		
		//Create a directory for the datatrack if it doesn't yet exist
		File subDirectory = new File(FileController.getIgvDirectory(),String.valueOf(idProject));
		if (!subDirectory.exists()) {
			subDirectory.mkdir();
		}
		
		//Create File handlers for uploaded files
		File localFile =  new File(subDirectory,filename);
		File tabixFile = new File(subDirectory,filename.substring(0,filename.length()-3));
		File tabixIndex = new File(subDirectory,filename + ".tbi");
		String tabixPath = BiominerProperties.getProperty("tabixPath");
		
		//Create FileMeta object, which contains information
		FileMeta fm = new FileMeta();
		
		//If first file, set append flag to false and delete existing files with the same name.
		boolean append = true;
		if (index == 0) {
			if (localFile.exists()) {
				localFile.delete();
			}
			append = false;
		}
		
		try {
	
			//copy file to directory
			if (filename.endsWith(".bw") || filename.endsWith(".bb") || filename.endsWith(".vcf.gz")) {
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile,append));
				
				//If last file, return info
				if (index+1 == total) {
					fm.setDirectory(filename);
					fm.setFinished(true);
				}
			
				if (filename.endsWith(".vcf.gz")) {
					try {
						VCFUtilities.unzipTabix(localFile, tabixFile, tabixPath);
						VCFUtilities.createTabix(tabixFile, localFile, tabixPath);
					} catch (Exception ex) {
						throw new Exception("Tabix creation or indexing failed: " + ex.getMessage());
					}
					
				}
				fm.setState(FileStateEnum.SUCCESS.toString());
				
			} else {
				fm.setMessage(String.format("The suffix for file %s is not supported.  Only bw, bb and vcf.gz can be loaded into biominer.",filename));
				fm.setState(FileStateEnum.FAILURE.toString());
				fm.setFinished(true);
			}
		} catch (Exception ex) {
			
			
			fm.setFinished(true);
			//Cleanup all partial files
			if (localFile.exists()) {
				localFile.delete();
			}
			
			if (tabixFile.exists()) {
				tabixFile.delete();
			}
			
			if (tabixIndex.exists()) {
				tabixIndex.delete();
			}
			
			//set error message
			fm.setMessage(ex.getMessage());
			fm.setState(FileStateEnum.FAILURE.toString());
		}
		return fm;
	}
	
	
	/***************************************************
	 * URL: /project/deleteDataTrack
	 * deleteDataTrack(): delete dataTrack based on primary index
	 * method: post
	 * @param idDataTrack primary index of the sample
	 ****************************************************/
    @RequestMapping(value="deleteDataTrack",method=RequestMethod.DELETE)
    @ResponseBody
    public void deleteDataTrack(@RequestParam(value="idDataTrack") Long idDataTrack) throws Exception{
    	DataTrack dt = this.dataTrackService.getDataTrackById(idDataTrack);
    	deleteDataTrackFile(dt);
    	this.dataTrackService.deleteDataTrackById(idDataTrack);
    }
    
    /***************************************************
     * URL: /project/finalizeDataTrack
     * finalizeDataTrack(): update dataTrack with upload status (FAILURE or SUCCESS)
     * @param idDataTrack
     * @param uploadStatus
     * @param message
     */
    @RequestMapping(value="finalizeDataTrack", method=RequestMethod.PUT)
    @ResponseBody
    public void finalizeDataTrack(@RequestParam(value="idDataTrack") Long idDataTrack, @RequestParam(value="uploadStatus") FileStateEnum fs, 
    		@RequestParam(value="message",required=false) String message) {
    	this.dataTrackService.finalizeDataTrack(idDataTrack, fs, message);
    }
    
    /***************************************************
	 * URL: /project/updateDataTrack
	 * updateDataTrack(): update dataTrack with new information
	 * method: put
	 * @param idDataTrack
	 * @param idProject
	 * @param name
	 * @param path
	 ****************************************************/
    @RequestMapping(value="updateDataTrack",method=RequestMethod.PUT)
    @ResponseBody
    public DataTrack updateDataTrack(@RequestParam(value="idProject") Long idProject, @RequestParam(value="idDataTrack") Long idDataTrack, 
    		@RequestParam(value="name") String name, @RequestParam(value="path") String path, @RequestParam(value="toDelete") boolean delete) throws Exception {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	
    
    	//Load existing datatrack
    	DataTrack updatedDataTrack = this.dataTrackService.getDataTrackById(idDataTrack);
    	
    	//delete
    	//if (!updatedDataTrack.getPath().equals(path) || updatedDataTrack.getState().equals(FileStateEnum.INCOMPLETE)) {
    	if (delete) {
    		deleteDataTrackFile(updatedDataTrack);
    		updatedDataTrack.setState(FileStateEnum.INCOMPLETE);
    	} 
    	
    	updatedDataTrack.setName(name);
    	updatedDataTrack.setPath(path);
    	updatedDataTrack.setProject(project);
    	

    	//Update sample
    	this.dataTrackService.updateDataTrack(idDataTrack, updatedDataTrack);
    	return this.dataTrackService.getDataTrackById(idDataTrack);
    }
   
    
    /***************************************************
	 * URL: /project/createDataTrack
	 * createDataTrack(): update dataTrack with new information
	 * method: post
	 * @param idProject
	 * @param name
	 * @param path
	 ****************************************************/
    @RequestMapping(value="createDataTrack",method=RequestMethod.PUT)
    @ResponseBody
    public DataTrack createDataTrack(@RequestParam(value="idProject") Long idProject, @RequestParam(value="name") String name, 
    		@RequestParam(value="path") String path) {
    	
    	//Create secondary objects
    	Project project = this.projectService.getProjectById(idProject);
    	
    	//Create sample object
    	DataTrack dataTrack = new DataTrack(name, path, project, FileStateEnum.INCOMPLETE);
    	
    	//Update sample
    	this.dataTrackService.addDataTrack(dataTrack);
    	
    	return dataTrack;
    }
	
	private void deleteDataTrackFile(DataTrack dt) throws Exception {
		File dataTrackFile = new File(FileController.getIgvDirectory(),dt.getProject().getIdProject() + "/" + dt.getPath());
		File dataTrackIndexFile = new File(FileController.getIgvDirectory(),dt.getProject().getIdProject() + "/" + dt.getPath() + ".tbi");
		if (dataTrackFile.exists()) {
			dataTrackFile.delete();
		}
		if (dataTrackIndexFile.exists()) {
			dataTrackIndexFile.delete();
		}
	}
	
	/***************************************************
	 * URL: /project/cleanDataTracks
	 * cleanDataTracks(): delete data tracks that are incomplete or failed
	 * method: post
	 * @param idProject : Project identifier
	 * @param idProtect : ID of datatrack you want to protect.  Optional
	 ****************************************************/
    @RequestMapping(value="cleanDataTracks",method=RequestMethod.DELETE)
    @ResponseBody
    public List<Long> cleanDataTrack(@RequestParam(value="idProject") Long idProject, @RequestParam(value="idProtect",required=false) Long idProtect) throws Exception {
    	Project project = this.projectService.getProjectById(idProject);
    	List<Long> removed = new ArrayList<Long>();
    	if (idProtect == null) {
    		idProtect = new Long(-1);
    	}
    	List<DataTrack> dtList = this.dataTrackService.getDataTrackByProject(project);
    	for (DataTrack dt: dtList) {
    		if (dt.getState() != FileStateEnum.SUCCESS && !dt.getIdDataTrack().equals(idProtect)) {
    			this.deleteDataTrack(dt.getIdDataTrack());
    			removed.add(dt.getIdDataTrack());
    		}
    	}
    	return removed;
    }
	
	@RequestMapping(value="isSampleSourceUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSampleSourceUsed(@RequestParam(value="source") String source, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
		BooleanModel bm = new BooleanModel();
		boolean response = this.sampleService.isSampleSourceUsed(source, idOrganismBuild);
		bm.setFound(response);
		return bm;
	}
	
	@RequestMapping(value="isSampleConditionUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSampleConditionUsed(@RequestParam(value="cond") String cond, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
		BooleanModel bm = new BooleanModel();
		boolean response = this.sampleService.isSampleConditionUsed(cond, idOrganismBuild);
		bm.setFound(response);
		return bm;
	}
	
	@RequestMapping(value="isSamplePrepUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSamplePrepUsed(@RequestParam(value="description") String description) {
		BooleanModel bm = new BooleanModel();
		boolean response = this.sampleService.isSamplePrepUsed(description);
		bm.setFound(response);
		return bm;
	}
	
	@RequestMapping(value="isSampleSourceNameUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSampleSourceNameUsed(@RequestParam(value="source") String source, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
		BooleanModel bm = new BooleanModel();
		List<SampleSource> ssList = this.sampleSourceService.getAllSampleSources();
		boolean found = false;
		for (SampleSource ss: ssList) {
			String dSource = ss.getSource().toUpperCase();
			if (idOrganismBuild.equals(ss.getIdOrganismBuild()) && dSource.equals(source.toUpperCase())) {
				found = true;
				break;
			}
		}
		bm.setFound(found);
		return bm;
	}

	
	@RequestMapping(value="isSampleConditionNameUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSampleConditionNameUsed(@RequestParam(value="cond") String cond, @RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
		BooleanModel bm = new BooleanModel();
		List<SampleCondition> scList = this.sampleConditionService.getAllSampleConditions();
		boolean found = false;
		for (SampleCondition sc: scList) {
			String dCond = sc.getCond().toUpperCase();
			if (idOrganismBuild.equals(sc.getIdOrganismBuild()) && dCond.equals(cond.toUpperCase())) {
				found = true;
				break;
			}
		}
		bm.setFound(found);
		return bm;
	}
	
	@RequestMapping(value="isSamplePrepNameUsed",method=RequestMethod.GET)
	@ResponseBody
	public BooleanModel isSampleConditionNameUsed(@RequestParam(value="prep") String prep) {
		BooleanModel bm = new BooleanModel();
		List<SamplePrep> spList = this.samplePrepService.getAllSamplePreps();
		boolean found = false;
		for (SamplePrep sp: spList) {
			String dPrep = sp.getDescription().toUpperCase();
			if (dPrep.equals(prep.toUpperCase())) {
				found = true;
				break;
			}
		}
		bm.setFound(found);
		return bm;
	}
	
	@RequestMapping(value="deleteSamplePrep",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteSamplePrep(@RequestParam(value="idSamplePrep") Long idSamplePrep) {
		this.samplePrepService.deleteSamplePrepById(idSamplePrep);
	}
	
	@RequestMapping(value="deleteSampleCondition",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteSampleCondition(@RequestParam(value="idSampleCondition") Long idSampleCondition) {
		this.sampleConditionService.deleteSampleConditionById(idSampleCondition);
	}
	
	@RequestMapping(value="deleteSampleSource",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteSampleSource(@RequestParam(value="idSampleSource") Long idSampleSource) {
		this.sampleSourceService.deleteSampleSourceById(idSampleSource);
	}
    
    
}
