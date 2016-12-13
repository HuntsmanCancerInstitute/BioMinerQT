package hci.biominer.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import hci.biominer.model.LiftoverChain;
import hci.biominer.model.LiftoverSupport;
import hci.biominer.model.OrganismBuild;
import hci.biominer.service.LiftoverChainService;
import hci.biominer.service.LiftoverSupportService;
import hci.biominer.service.OrganismBuildService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import returnModel.IdxModel;


@Controller
@RequestMapping("liftover")
public class LiftoverController {
	@Autowired
	private LiftoverChainService liftChainService;
	
	@Autowired
	private LiftoverSupportService liftSupportService;
	
	@Autowired
	private OrganismBuildService obService;
	
	/************************************************
	 * 
	 * URL: /liftover/get_liftover_chains
	 * method: getLiftoverChains()
	 * function: This method return all loaded liftover chains
	 * 
	 ************************************************/
	@RequestMapping(value="/get_liftover_chains",method=RequestMethod.GET)
	@ResponseBody
	public List<LiftoverChain> getLiftoverChains() {
		List<LiftoverChain> liftoverChains = liftChainService.getLiftoverChains();
		return liftoverChains;
	}
	
	/************************************************
	 * 
	 * URL: /liftover/get_liftover_support
	 * method: getLiftoverSupport()
	 * function: This method return all loaded liftover supports
	 * 
	 ************************************************/
	@RequestMapping(value="/get_liftover_supports",method=RequestMethod.GET)
	@ResponseBody
	public List<LiftoverSupport> getLiftoverSupports() {
		List<LiftoverSupport> liftoverSupports = liftSupportService.getLiftoverSupports();
		return liftoverSupports;
	}
	
	/************************************************
	 * 
	 * URL: /liftover/add_liftover_chain
	 * method: addLiftoverChain()
	 * function: This method adds a new liftover chain to the database
	 * 
	 ************************************************/
	@RequestMapping(value="/add_liftover_chain",method=RequestMethod.POST)
	@ResponseBody
	public IdxModel addLiftoverChain(@RequestParam("file") MultipartFile file, @RequestParam("idSourceBuild") Long idSourceBuild, 
			@RequestParam("idDestBuild") Long idDestBuild, HttpServletResponse response) {
		String message = null;
		
		OrganismBuild sourceBuild = obService.getOrganismBuildById(idSourceBuild);
		OrganismBuild destBuild = obService.getOrganismBuildById(idDestBuild);
		
		Long idx = null;
		try {
			File localFile = uploadFile(file);
			LiftoverChain lc = new LiftoverChain(sourceBuild,destBuild,localFile.getName());
			liftChainService.addLiftoverChain(lc);
			idx = lc.getIdLiftoverChain();
			message = "OK";
		} catch (Exception ex) {
			response.setStatus(999);
			message = "Failed to create Liftover Chain Object: " + ex.getMessage();
			ex.printStackTrace();
			idx = null;
		}
		IdxModel idxModel = new IdxModel(message, idx);
		return idxModel;
	}
	
	/************************************************
	 * 
	 * URL: /liftover/add_liftover_support
	 * method: addLiftoverSupport()
	 * function: This method adds a new liftover support object to the database
	 * 
	 ************************************************/
	@RequestMapping(value="/add_liftover_support",method=RequestMethod.POST)
	@ResponseBody
	public IdxModel addLiftoverSupport(@RequestParam("idChainList") List<Long> idChainList, @RequestParam("idSourceBuild") Long idSourceBuild, 
			@RequestParam("idDestBuild") Long idDestBuild, HttpServletResponse response) {
		String message = null;
		
		OrganismBuild sourceBuild = obService.getOrganismBuildById(idSourceBuild);
		OrganismBuild destBuild = obService.getOrganismBuildById(idDestBuild);
		
		Long idx = null;
		try {
			List<LiftoverChain> chainList = new ArrayList<LiftoverChain>();
			for (Long idChain: idChainList) {
				LiftoverChain lc = liftChainService.getLiftoverChainByID(idChain);
				chainList.add(lc);
			}
			LiftoverSupport ls = new LiftoverSupport(sourceBuild,destBuild,chainList);
			liftSupportService.addLiftoverSupport(ls);
			idx = ls.getIdLiftoverSupport();
			message = "OK";
		} catch (Exception ex) {
			response.setStatus(999);
			message = "Failed to create Liftover Support Object: " + ex.getMessage();
			ex.printStackTrace();
			idx = null;
		}
		IdxModel idxModel = new IdxModel(message, idx);
		return idxModel;
	}
	
	/************************************************
	 * 
	 * URL: /liftover/delete_liftover_chain
	 * method: deleteLiftoverChain
	 * function: This method deletes a liftover chain entry from the database
	 * 
	 ************************************************/
	@RequestMapping(value="/delete_liftover_chain",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteLiftoverChain(@RequestParam("idLiftoverChain") Long idLiftoverChain) throws Exception {
		//Set up the source and destination organism builds
		LiftoverChain lc = liftChainService.getLiftoverChainByID(idLiftoverChain);
		File localFile = new File(FileController.getLiftoverDirectory(),lc.getChainFile());
		if (localFile.exists()) {
			localFile.delete();
		}
		liftChainService.deleteLiftoverChain(idLiftoverChain);
	}
	
	/************************************************
	 * 
	 * URL: /liftover/delete_liftover_support
	 * method: deleteLiftoverSupport
	 * function: This method deletes a liftover support entry from the database
	 * 
	 ************************************************/
	@RequestMapping(value="/delete_liftover_support",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteLiftoverSupport(@RequestParam("idLiftoverSupport") Long idLiftoverSupport) throws Exception {
		//Set up the source and destination organism builds
		liftSupportService.deleteLiftoverSupport(idLiftoverSupport);
	}
	
	private File uploadFile(MultipartFile file) throws Exception{
		String name = file.getOriginalFilename();
		File localFile;
		//copy file to directory, support gzip 
		if (name.endsWith(".gz")) {
			localFile = new File(FileController.getLiftoverDirectory(),name);
			FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
		} else {
			localFile = new File(FileController.getLiftoverDirectory(),name + ".gz");
			FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
		}
		return localFile;
		
	}
	
}
