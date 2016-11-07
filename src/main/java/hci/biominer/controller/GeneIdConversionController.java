package hci.biominer.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import hci.biominer.model.ExternalGene;
import hci.biominer.model.GeneIdConversion;
import hci.biominer.model.OrganismBuild;
import hci.biominer.parser.HomologyParser;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.service.GeneIdConversionService;
import hci.biominer.service.OrganismBuildService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import returnModel.HomologyModel;
import returnModel.IdxModel;

@Controller
@RequestMapping("/id_conversion")
public class GeneIdConversionController {
	@Autowired
	private GeneIdConversionService gicService;
	
	@Autowired
	private OrganismBuildService obService;
	
	@Autowired
	private ExternalGeneService egService;
	
	/************************************************
	 * 
	 * URL: /id_conversion/get_conversions
	 * method: getGeneIdConversions()
	 * function: This method return all GeneIdConversions in the database
	 * 
	 ************************************************/
	@RequestMapping(value="/get_conversions",method=RequestMethod.GET)
	@ResponseBody
	public List<GeneIdConversion> getGeneIdConversions() {
		List<GeneIdConversion> conversions = gicService.getGeneIdConversions();
		return conversions;
	}
	
	/************************************************
	 * 
	 * URL: /id_conversion/add_conversion
	 * method: addGeneIdConversion()
	 * function: This method adds a new GeneIdConversion to the database
	 * 
	 ************************************************/
	@RequestMapping(value="/add_conversion",method=RequestMethod.POST)
	@ResponseBody
	public IdxModel addGeneIdConversion(@RequestParam("file") MultipartFile file, @RequestParam("idSourceBuild") Long idSourceBuild, 
			@RequestParam("idDestBuild") Long idDestBuild, HttpServletResponse response) {
		//Response message, empty of everything OK, error if failed
		String message = null;
		
		
		//Set up the source and destination organism builds
		OrganismBuild sourceBuild = obService.getOrganismBuildById(idSourceBuild);
		OrganismBuild destBuild = obService.getOrganismBuildById(idDestBuild);
		
		//Read in the file.  We are assuming that it isn't huge, should be checked on the client size
		Long idx = null;
		try {
			File localFile = uploadFile(file);			
			GeneIdConversion conversion = new GeneIdConversion(sourceBuild, destBuild, localFile.getName());
			gicService.addGeneIdConversion(conversion);
			idx = conversion.getIdGeneIdConversion();
			message = "OK";
		} catch (Exception ex) {
			//Sent error response back
			response.setStatus(999);
			message = "Failed to create GeneIdConversion Object: " + ex.getMessage();
			ex.printStackTrace();	
			idx = null;
		}
		IdxModel idxModel = new IdxModel(message, idx);
		return idxModel;
	}
	
	
	/************************************************
	 * 
	 * URL: /id_conversion/delete_conversion
	 * method: deleteGeneIdConversion
	 * function: This method deletes and exsting conversion
	 * 
	 ************************************************/
	@RequestMapping(value="/delete_conversion",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteGeneIdConversion(@RequestParam("idGeneIdConversion") Long idGeneIdConversion) throws Exception {
		//Set up the source and destination organism builds
		GeneIdConversion oldConversion = gicService.getGeneIdConversionByID(idGeneIdConversion);
		File localFile = new File(FileController.getHomologyDirectory(),oldConversion.getConversionFile());
		if (localFile.exists()) {
			localFile.delete();
		}
		gicService.deleteGeneIdConversion(idGeneIdConversion);
	}
	
	/************************************************
	 * 
	 * URL: /id_conversion/check_conversion
	 * method: checkConversionFile
	 * function: This method parses a homology file to make sure the names make sense
	 * 
	 ************************************************/
	@RequestMapping(value="/check_conversion",method=RequestMethod.POST)
	@ResponseBody
	public IdxModel checkConversionFile(@RequestParam("idGeneIdConversion") Long idGeneIdConversion, HttpServletResponse response) throws Exception{
		//Set up the source and destination organism builds
		GeneIdConversion conversion = gicService.getGeneIdConversionByID(idGeneIdConversion);
		OrganismBuild obSource = conversion.getSourceBuild();
		OrganismBuild obDest = conversion.getDestBuild();
		File file = new File(FileController.getHomologyDirectory(),conversion.getConversionFile());
		List<ExternalGene> egSource = egService.getExternalGenesByOrganismBuild(obSource);
		List<ExternalGene> egDest = egService.getExternalGenesByOrganismBuild(obDest);
		
		HomologyParser hp = new HomologyParser(file, egSource, egDest, obSource, obDest);
		HomologyModel hm = hp.processData();
		
		if (hm.getFailed()) {
			File localFile = new File(FileController.getHomologyDirectory(),conversion.getConversionFile());
			if (localFile.exists()) {
				localFile.delete();
			}
			gicService.deleteGeneIdConversion(idGeneIdConversion);
			response.setStatus(999);
		}
		
		IdxModel idxModel = new IdxModel(hm.getMessage(),idGeneIdConversion);
		return idxModel;
		
	}
	
	
	private File uploadFile(MultipartFile file) throws Exception{
		String name = file.getOriginalFilename();
		File localFile;
		//copy file to directory, support gzip 
		if (name.endsWith(".gz")) {
			localFile = new File(FileController.getHomologyDirectory(),name);
			FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
		} else {
			localFile = new File(FileController.getHomologyDirectory(),name + ".gz");
			FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
		}
		return localFile;
		
	}
	
}
