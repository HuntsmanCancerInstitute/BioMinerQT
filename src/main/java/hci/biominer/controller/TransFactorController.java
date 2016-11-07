package hci.biominer.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.TransFactor;
import hci.biominer.model.genome.Genome;
import hci.biominer.parser.BedParser;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.TransFactorService;
import hci.biominer.service.UserService;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.FileMetaSimple;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.JBrowseUtilities;

import org.apache.commons.io.FileUtils;
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
@RequestMapping("/transFactor")
public class TransFactorController {
	@Autowired
	private TransFactorService tfService;
	
	@Autowired
	private OrganismBuildService obService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FileController fileContoller;
	
	
	
	/******************************
	 * URL: /transFactor/getAllTfs
	 * getAllTfs()
	 * @Return list of TransFactor
	 */
	@RequestMapping(value="getAllTfs",method=RequestMethod.GET)
	@ResponseBody
	public List<TransFactor> getAllTfs() {
		List<TransFactor> tfList = tfService.getAllTransFactors();
		return tfList;
	}
	
	/*****************************
	 * URL: /transFactor/getTfByGenomeBuild
	 * getTfByGenomeBuild()
	 * @Return list of TransFactors associated with given genome build
	 */
	@RequestMapping(value="getTfByGenomeBuild",method=RequestMethod.GET)
	@ResponseBody
	public List<TransFactor> getTfByGenomeBuild(@RequestParam("idOrganismBuild") Long idGenomeBuild) {
		OrganismBuild ob = obService.getOrganismBuildById(idGenomeBuild);
		List<TransFactor> tfList = tfService.getTransFactorByGenomeBuild(ob);
		return tfList;
	}
	
	/****************************
	 * URL: /transFactor/deleteTf
	 * deleteTf()
	 * remove TransFactor from database
	 */
	@RequestMapping(value="/deleteTf",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteTf(@RequestParam("idTransFactor") Long idTransFactor) throws Exception {
		TransFactor tf = tfService.getTransFactorById(idTransFactor);
		File file = new File(FileController.getTfRawDirectory(),tf.getFilename());
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileController.getTfParseDirectory(),tf.getFilename());
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileController.getTfParseDirectory(),tf.getFilename() + "_jbrowse");
		if (file.exists()) {
			FileUtils.deleteDirectory(file);
		}
		tfService.deleteTransFactor(idTransFactor);
	}
	
	/***************************
	 * URL: /transFactor/addTf
	 * addTf()
	 * add TF in the database
	 */
	@RequestMapping(value="/addTf",method=RequestMethod.POST)
	@ResponseBody
	public void addTf(@RequestParam("name") String name, @RequestParam("description") String description, 
			@RequestParam("filename") String filename,
			@RequestParam("idOrganismBuild") Long idOrganismBuild) throws Exception {
		
		TransFactor tf = new TransFactor();
		tf.setDescription(description);
		tf.setName(name);
		tf.setFilename(filename);
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		tf.setOrganismBuild(ob);
		tfService.addTransFactor(tf);
		createJbrowserBed(tf);
	}
	
	/***************************
	 * URL: /transFactor/deleteFile
	 */
	@RequestMapping(value="/deleteTfFile",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteTfFile(@RequestParam("name") String name) throws Exception {
		File file = new File(FileController.getTfRawDirectory(),name);
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileController.getTfParseDirectory(),name);
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileController.getTfParseDirectory(),name + "_jbrowse");
		if (file.exists()) {
			FileUtils.deleteDirectory(file);
		}
	}
	
	/***************************
	 * URL: /transFactor/updateTf
	 * updateTf()
	 * add TF in the database
	 */
	@RequestMapping(value="/updateTf",method=RequestMethod.POST)
	@ResponseBody
	public void updateTf(@RequestParam("name") String name, @RequestParam("description") String description, 
			@RequestParam("filename") String filename,
			@RequestParam("idOrganismBuild") Long idOrganismBuild,
			@RequestParam("idTransFactor") Long idTransFactor) throws Exception {
		TransFactor tf = new TransFactor();
		tf.setDescription(description);
		tf.setName(name);
		tf.setFilename(filename);
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		tf.setOrganismBuild(ob);
		tfService.updateTransFactor(tf, idTransFactor);
		createJbrowserBed(tf);
	}
	
	
	/***************************************************
	 * URL: /transFactor/parseTfFile
	 * parseTransFile()
	 * uploads a transcription factor file
	 ****************************************************/
	@RequestMapping(value="/parseTfFile",method=RequestMethod.POST)
	@ResponseBody
	public FileMetaSimple parseTfFile(@RequestParam("file") MultipartFile file, @RequestParam("idOrganismBuild") Long idOrganismBuild, 
			@RequestParam("isConverted") Boolean isConverted, HttpServletResponse response) throws Exception {
		File localFile = null;
		File outputFile = null;
		String name = file.getOriginalFilename();
		
		FileMetaSimple fms = new FileMetaSimple(false,"");
		
		//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
   
    	if (currentUser.isAuthenticated()) {
    		//pass
    	} else {
    		fms.setSuccess(false);
    		fms.setMessage("User is not authenticated, can't upload transcription factor files");
    		return fms;
    	}
		
		//Upload file. These should be pretty small... I hope
		try {
			//copy file to directory
			if (name.endsWith(".gz")) {
				localFile = new File(FileController.getTfRawDirectory(),name);
				outputFile = new File(FileController.getTfParseDirectory(),name);
				
				boolean exists = tfService.checkName(name);
				fms.setName(name);
				if (exists) {
					response.setStatus(405);
					fms.setSuccess(false);
					fms.setMessage("Filename already exists, please rename, or choose different file");
					return fms;
				}
				fms.setName(name);
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
			} else {
				String newName = name + ".gz";
				localFile = new File(FileController.getTfRawDirectory(),newName);
				outputFile = new File(FileController.getTfParseDirectory(),newName);
				boolean exists = tfService.checkName(newName);
				fms.setName(newName);
				if (exists) {
					response.setStatus(405);
					fms.setSuccess(false);
					fms.setMessage("Filename already exists, please rename, or choose different file");
					return fms;
				}
				fms.setName(newName);
				FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
			} 
			
		} catch (Exception ex) {
			//If failed, send error response back
			response.setStatus(405);
			ex.printStackTrace();
			//set error message
			fms.setSuccess(false);
			fms.setMessage(ex.getMessage());
			return fms;
		}
		
		//Get genome gile
		OrganismBuild ob = obService.getOrganismBuildById(idOrganismBuild);
		Genome genome = null;
		try {
			genome = GenomeBuilds.fetchGenome(ob);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(405);
			fms.setSuccess(false);
			fms.setMessage(ex.getMessage());
			return fms;
			
		}
		
		//Parse transcription factor file
		try {
			BedParser bfp = new BedParser(localFile, outputFile, genome, isConverted);
			String warning = bfp.run();
			fms.setSuccess(true);
			fms.setMessage(warning);
			return fms;
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(405);
			fms.setSuccess(false);
			fms.setMessage(ex.getMessage());
			return fms;
		}
	}
	
	private void createJbrowserBed(TransFactor tf) throws Exception{
		String pathToJBrowse = BiominerProperties.getProperty("jbrowsePath");
		File bedFile = new File(FileController.getTfParseDirectory(), tf.getFilename());
		if (bedFile.exists()) {
			JBrowseUtilities.createJbrowseFromBed(bedFile, tf.getName(), pathToJBrowse);
		}
	}

}
