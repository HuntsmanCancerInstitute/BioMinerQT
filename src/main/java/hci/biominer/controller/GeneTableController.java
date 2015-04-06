package hci.biominer.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import hci.biominer.parser.AnnotationFileParser;
import hci.biominer.service.BiominerGeneService;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.OrganismService;
import hci.biominer.service.UserService;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.ParsedAnnotation;
import hci.biominer.util.PreviewMap;
import hci.biominer.util.Enumerated.FileTypeEnum;
import hci.biominer.model.BiominerGene;
import hci.biominer.model.FileUpload;
import hci.biominer.model.Organism;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.access.User;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/genetable")
public class GeneTableController {
	@Autowired
	private OrganismBuildService obService;
	
	@Autowired
	private ExternalGeneService egService;
	
	@Autowired
	private BiominerGeneService bgService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private OrganismService organismService;
	
	private HashMap<String,String> annotationFileHash = new HashMap<String,String>();

	
	/***************************************************
	 * URL: /genetable/list
	 * getFiles()
	 * @return OrganismBuild as json format
	 ****************************************************/
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public @ResponseBody 
	List<OrganismBuild> getFiles() {
		List<OrganismBuild> builds = this.obService.getAllOrganismBuilds();
		return builds;
	}
	
	
	
	private void removeExistingGenome(OrganismBuild ob) throws Exception  {
		GenomeBuilds.removeGenome(ob);
		if (ob.getGenomeFile() != null) {
			File genomeFile = new File(FileController.getGenomeDirectory(),ob.getGenomeFile());
			if (genomeFile.exists()) {
				genomeFile.delete();
			}
			obService.updateGenomeFile(ob.getIdOrganismBuild(), null);
		}	
	}
	
	private void removeExistingTranscript(OrganismBuild ob) throws Exception {
		GenomeBuilds.removeGenome(ob);
		if (ob.getTranscriptFile() != null) {
			File transcriptFile = new File(FileController.getGenomeDirectory(),ob.getTranscriptFile());
			if (transcriptFile.exists()) {
				transcriptFile.delete();
			}
			obService.updateTranscriptFile(ob.getIdOrganismBuild(), null);
		}
	}
	
	private void removeExistingAnnotation(OrganismBuild ob, boolean delay) throws Exception {
		if (delay) {
			Thread.sleep(60*1000);
		}
		
		if (ob.getGeneIdFile() != null) {
			File geneIdFile = new File(FileController.getGenomeDirectory(),ob.getGeneIdFile());
			if (geneIdFile.exists()) {
				geneIdFile.delete();
			}
			obService.updateGeneIdFile(ob.getIdOrganismBuild(), null);
		}
		egService.deleteExternalGenesByOrganismId(ob.getIdOrganismBuild());
		bgService.deleteBiominerGenes();
	}
	
	
	/***************************************************
	 * URL: /genetable/addAnnotationFile
	 * addTranscriptFile
	 ****************************************************/
	@RequestMapping(value="/addAnnotationFile",method=RequestMethod.POST)
	public @ResponseBody
	PreviewMap addAnnotationFile(@RequestParam("file") MultipartFile file, 
			HttpServletResponse response) throws Exception {
		File localFile = null;
		PreviewMap pm = new PreviewMap();
		
		//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		username = user.getUsername();
    	} else {
    		pm.setMessage("User is not authenticated, can't add annotations");
    		return pm;
    	}
		
		
		try {
			
			//Get the name of the file
			String name = file.getOriginalFilename();
			
			//copy file to directory
			if (name.endsWith(".gz") || name.endsWith(".zip")) {
				localFile = new File(FileController.getGenomeDirectory(),name);
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
			} else {
				localFile = new File(FileController.getGenomeDirectory(),name + ".gz");
				FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
			}
		
			//Store file name
			annotationFileHash.put(username, localFile.getName());
			
		 	String temp = null;
		 	int counter = 0;
		 	
		 	//Open a buffered reader
		 	BufferedReader br = ModelUtil.fetchBufferedReader(localFile);
		 
		 	//Grab the first 20 lines of the file
		 	while((temp = br.readLine()) != null) {
		 		if (counter == 10) {
		 			break;
		 		}
		 		
		 		//split the file by tabs and add to the preview object
		 		String[] items = temp.split("\t");
		 		//LinkedList<String> dataLine = new LinkedList<String>(Arrays.asList(items));
		 		
		 		pm.addPreviewData(items);
		 		counter++;
		 	}
		 	
		 	br.close();
			 	
		} catch (Exception ex) {
			//If failed, send error response back
			response.setStatus(405);
		
			ex.printStackTrace();
		
			//set error message
			pm.setMessage(ex.getMessage());
		}
		return pm;
	}
	
	
	
	
	/***************************************************
	 * URL: /genetable/addTranscriptFile
	 * addTranscriptFile
	 ****************************************************/
	@RequestMapping(value="/addTranscriptFile",method=RequestMethod.POST)
	public @ResponseBody
	String addTranscriptFile(@RequestParam("file") MultipartFile file, @RequestParam("idOrganismBuild") Long idOrganismBuild, 
			HttpServletResponse response) throws Exception {
		String message = "OK";
		File localFile = null;
		
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		try {
			
			//Get the name of the file
			String name = file.getOriginalFilename();
			
			
			
			//copy file to directory
			if (name.endsWith(".gz") || name.endsWith(".zip")) {
				//update the database entry
				obService.updateTranscriptFile(idOrganismBuild, name);
				localFile = new File(FileController.getGenomeDirectory(),name);
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
			} else {
				obService.updateTranscriptFile(idOrganismBuild, name + ".gz");
				localFile = new File(FileController.getGenomeDirectory(),name + ".gz");
				FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
			}
			
			ob = obService.getOrganismBuildById(ob.getIdOrganismBuild());
			
			if (ob.getTranscriptFile() != null && ob.getGenomeFile() != null) {
				GenomeBuilds.loadGenome(ob);
			}
		
		} catch (Exception ex) {
			//If failed, send error response back
			response.setStatus(405);
			
			//write error message
			ex.printStackTrace();
			this.removeExistingTranscript(ob);
			
			//set error message
			message = ex.getMessage();
		}
		return message;
	}
	
	/***************************************************
	 * URL: /genetable/addGenomeFile
	 * addGenomeFile
	 ****************************************************/
	@RequestMapping(value="/addGenomeFile",method=RequestMethod.POST)
	public @ResponseBody
	String addGenomeFile(@RequestParam("file") MultipartFile file, @RequestParam("idOrganismBuild") Long idOrganismBuild, 
			HttpServletResponse response) throws Exception {
		String message = "OK";
		OrganismBuild ob = this.obService.getOrganismBuildById(idOrganismBuild);
		try {
			//Remove exiting genome information
			removeExistingGenome(ob);
			
			//Get the name of the file
			String name = file.getOriginalFilename();
			
			//copy file to directory
			if (name.endsWith(".gz") || name.endsWith(".zip")) {
				//update the database entry
				obService.updateGenomeFile(idOrganismBuild, name);
				File localFile = new File(FileController.getGenomeDirectory(),name);
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
			} else {
				//update the database entry
				obService.updateGenomeFile(idOrganismBuild, name + ".gz");
				File localFile = new File(FileController.getGenomeDirectory(),name + ".gz");
				FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
			}
			
			//Try parsing the genome
			ob = this.obService.getOrganismBuildById(ob.getIdOrganismBuild());
			if (ob.getTranscriptFile() != null && ob.getGenomeFile() != null) {
				GenomeBuilds.loadGenome(ob);
			}
		} catch (Exception ex) {
			response.setStatus(405);
			this.removeExistingGenome(ob);
			ex.printStackTrace();
			message = ex.getMessage();
		}
		return message;
	}
	
	/***************************************************
	 * URL: /genetable/removeGenomeFile
	 * removeGenomeFile
	 ****************************************************/
	@RequestMapping(value="/removeGenomeFromBuild",method=RequestMethod.DELETE)
	public @ResponseBody
	void removeGenomeFile(@RequestParam("idOrganismBuild") Long idOrganismBuild) throws Exception {
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		this.removeExistingGenome(ob);
	}
	
	/***************************************************
	 * URL: /genetable/removeTranscriptFile
	 * removeGenomeFile
	 ****************************************************/
	@RequestMapping(value="/removeTranscriptsFromBuild",method=RequestMethod.DELETE)
	public @ResponseBody
	void removeTranscriptFile(@RequestParam("idOrganismBuild") Long idOrganismBuild) throws Exception {
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		this.removeExistingTranscript(ob);
	}
	
	/***************************************************
	 * URL: /genetable/removeAnnotationFile
	 * removeAnnotationFile
	 ****************************************************/
	@RequestMapping(value="/removeAnnotationsFromBuild",method=RequestMethod.DELETE)
	public @ResponseBody
	void removeAnnotationFile(@RequestParam("idOrganismBuild") Long idOrganismBuild, @RequestParam("delay") boolean delay) throws Exception {
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		this.removeExistingAnnotation(ob, delay);
	}
	
	/***************************************************
	 * URL: /genetable/removeOrganismBuild
	 * removeGenomeBuild
	 ****************************************************/
	@RequestMapping(value="/removeOrganismBuild",method=RequestMethod.DELETE)
	public @ResponseBody
	void removeOrganismBuild(@RequestParam("idOrganismBuild") Long idOrganismBuild) throws Exception {
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		this.removeExistingTranscript(ob);
		this.removeExistingGenome(ob);
		this.removeExistingAnnotation(ob, false);
		obService.deleteOrganismBuildById(idOrganismBuild);
	}
	 
	/***************************************************
	 * URL: /genetable/addOrganism
	 * addOrganism
	 * params: common, binomial
	 ****************************************************/
	@RequestMapping(value="/addOrganism",method=RequestMethod.PUT)
	public @ResponseBody
	void addOrganism(@RequestParam("common") String common, @RequestParam("binomial") String binomial) {
		Organism o = new Organism(common,binomial);
		organismService.addOrganism(o);
	}
	
	/***************************************************
	 * URL: /genetable/modifyOrganism
	 * modifyOrganism
	 * params: common, binomial, idOrganism
	 ****************************************************/
	@RequestMapping(value="/modifyOrganism",method=RequestMethod.PUT)
	public @ResponseBody
	void modifyOrganism(@RequestParam("common") String common, @RequestParam("binomial") String binomial, @RequestParam("idOrganism") Long idOrganism) {
		Organism o = new Organism(common,binomial);
		organismService.updateOrganism(idOrganism, o);
	}
	
	/***************************************************
	 * URL: /genetable/addOrganismBuild
	 * addOrganismBuild
	 * params: idOrganism, name
	 ****************************************************/
	@RequestMapping(value="/addOrganismBuild",method=RequestMethod.PUT)
	public @ResponseBody
	void addOrganismBuild(@RequestParam("idOrganism") Long idOrganism, @RequestParam("name") String name) {
		Organism o = organismService.getOrganism(idOrganism);
		System.out.println(o.getCommon());
		System.out.println(o.getBinomial());
		System.out.println(o.getIdOrganism());
		OrganismBuild ob = new OrganismBuild(o,name);
		obService.addOrganismBuild(ob);
	}
	
	/***************************************************
	 * URL: /genetable/modifyOrganismBuild
	 * modifyOrganismBuild
	 * params: idOrganism, name, idOrganismBuild
	 ****************************************************/
	@RequestMapping(value="/modifyOrganismBuild",method=RequestMethod.PUT)
	public @ResponseBody
	void modifyOrganismBuild(@RequestParam("idOrganism") Long idOrganism, @RequestParam("name") String name, @RequestParam("idOrganismBuild") Long idOrganismBuild) {
		Organism o = organismService.getOrganism(idOrganism);
		OrganismBuild ob = new OrganismBuild(o,name);
		obService.updateOrganismBuild(idOrganismBuild, ob);
	}
	
	/***************************************************
	 * URL: /genetable/parseAnnotations
	 * parseAnnotations
	 * params:
	 ****************************************************/
	@RequestMapping(value="/parseAnnotations",method=RequestMethod.PUT)
	public @ResponseBody
	String parseAnnotations(
			@RequestParam(value="Ensembl") Integer ensemblIdx, 
			@RequestParam(value="Common") Integer hugoIdx, 
			@RequestParam(value="RefSeq") Integer refseqIdx,
			@RequestParam(value="UCSC") Integer ucscIdx,
			@RequestParam(value="idOrganismBuild") Long idOrganismBuild,
			HttpServletResponse response) throws Exception {
		
		//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		username = user.getUsername();
    	} else {
    		return null;
    	}
		
		
		//change -1 to null
		if (refseqIdx == -1) {
			refseqIdx = null;
		}
		
		if (ucscIdx == -1) {
			ucscIdx = null;
		}
		
		
		//Get organismBuild
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		
		//Determine max bg id
		List<BiominerGene> bgList = bgService.getBiominerGenes();
		long bmIdx = 0;
		for (BiominerGene bg: bgList) {
			long bgNumber = Long.parseLong(bg.getBiominerGeneName().substring(2));
			if (bgNumber > bmIdx) {
				bmIdx = bgNumber;
			}
		}
		
		
		String message = null;
		try {
			//Get file
			String annotationFileName = annotationFileHash.get(username);
			File annotationFile = new File(FileController.getGenomeDirectory(),annotationFileName);
			
			//Parse annotation file
			AnnotationFileParser afp = new AnnotationFileParser(ob,annotationFile,ensemblIdx, hugoIdx, refseqIdx, ucscIdx, bmIdx);
			ParsedAnnotation pa = afp.run();
			
			//If parsing worked, update the organism build with new information
			this.removeExistingAnnotation(ob, false);
			obService.updateGeneIdFile(idOrganismBuild, annotationFileName);
			
			bgService.addBiominerGenes(pa.getBiominerGenes());
			egService.addExternalGenes(pa.getExternalGenes());
		} catch (Exception ex) {
			ex.printStackTrace();
			message = ex.getMessage();
			response.setStatus(405);
		}
		
		return message;
	}
	
	
	/***************************************************
	 * URL: /genetable/deleteAnnotationUpload
	 * parseAnnotations
	 * params:
	 ****************************************************/
	@RequestMapping(value="/deleteAnnotationUpload",method=RequestMethod.DELETE)
	public @ResponseBody
	void deleteAnnotationUpload() throws Exception {
		
		//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		username = user.getUsername();
    	} else {
    		return;
    	}
    	
    	File annotationFile = new File(FileController.getGenomeDirectory(),annotationFileHash.get(username));
    	if (annotationFile.exists()) {
    		annotationFile.delete();
    	}
    	
    	
	}
	
	
}
