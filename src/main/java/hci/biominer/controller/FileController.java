package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hci.biominer.model.genome.Genome;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.FileUpload;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.service.FileUploadService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.ProjectService;
import hci.biominer.parser.ChipParser;
import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.RnaSeqParser;
import hci.biominer.parser.VCFParser;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.Enumerated.FileStateEnum;
import hci.biominer.util.Enumerated.FileTypeEnum;
import hci.biominer.util.FileMeta;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.PreviewMap;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.Enumerated.*;

@Controller
@RequestMapping("/submit")
public class FileController {
	
	@Autowired
	private FileUploadService fileUploadService;
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ExternalGeneService externalGeneService;
	
	@Autowired
	private OrganismBuildService organismBuildService;
	
	@Autowired
	private AnalysisTypeService analysisTypeService;
	
	public static void checkProperties() throws Exception{
		if (!BiominerProperties.isLoaded()) {
			BiominerProperties.loadProperties();
		}
	}
	
	
	public static File generateFilePath(FileUpload fileUpload) throws Exception  {
		checkProperties();
		File localDirectory = new File(BiominerProperties.getProperty("filePath"));
		File subDirectory = new File(localDirectory,fileUpload.getDirectory());
		File filePath = new File(subDirectory,fileUpload.getName());
		return filePath;
	}
	
	public static File getRawDirectory() throws Exception {
		return createDirectory("raw");
	}
	
	public static File getParsedDirectory() throws Exception {
		return createDirectory("parsed");
	}
	
	public static File getDownloadDirectory() throws Exception {
		return createDirectory("results");
	}
	
	public static File getGenomeDirectory() throws Exception {
		return createDirectory("genome");
	}
	
	public static File getIgvDirectory() throws Exception {
		return createDirectory("IGV");
	}
	
	public static File getGenesDirectory() throws Exception {
		return createDirectory("genes");
	}
	
	public static File getQueryDirectory() throws Exception {
		return createDirectory("queries");
	}
	
	private static File createDirectory(String subdir) throws Exception {
		checkProperties();
		File localDirectory = new File(BiominerProperties.getProperty("filePath"));
		File subDirectory = new File(localDirectory,subdir);
		if (!subDirectory.exists()) {
			subDirectory.mkdir();
		}
		return subDirectory;
	}

	/***************************************************
	 * URL: /submit/uploadchunk  
	 * upload(): receives files
	 * post():
	 * @param file : MultipartFile
	 * @return FileMeta as json format
	 ****************************************************/
	@RequestMapping(value="/uploadchunk", method = RequestMethod.POST)
	public @ResponseBody 
	FileUpload uploadchunk(@RequestParam("file") MultipartFile file,
			@RequestParam(value="index") Integer index, 
			@RequestParam(value="total") Integer total, 
			@RequestParam(value="name") String filename,		
			@RequestParam("idProject") Long idProject,
			HttpServletResponse response) throws Exception {
 
		FileUpload fileUpload = new FileUpload();
		
		String name = filename;      
		
		if (!file.isEmpty()) {
			File localFile = null;
			try {
				
				//Create directory
				File directory = new File(getRawDirectory(),String.valueOf(idProject));
				File directoryStub = new File("/raw/",String.valueOf(idProject));
				if (!directory.exists()) {
					directory.mkdir();
				}

				int ftype = 0;
				if (name.endsWith(".bam") || name.endsWith(".bai") || name.endsWith(".useq") || name.endsWith(".bw") || name.endsWith(".gz") || name.endsWith(".zip")) {
					localFile = new File(directory,name);
				}
				else {
					localFile = new File(directory,name + ".gz");
					ftype = 1;
				}
				
				//If first file, set append flag to false and delete existing files with the same name.
				boolean append = true;
				if (index == 0) {
					if (localFile.exists()) {
						localFile.delete();
					}
					append = false;
				}
				
				//copy file to directory
				if (name.endsWith(".bam") || name.endsWith(".bai") || name.endsWith(".useq") || name.endsWith(".bw") || name.endsWith(".gz") || name.endsWith(".zip")) {
					FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile,append));
				} 
				else {
					FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile,append)));
				}
					
				//If last file, return info
				if (index+1 == total) {
					fileUpload.setName(name);
					
					if (ftype == 1) {
						fileUpload.setName(name + ".gz");
					}
				
					//Grab project object
					Project project = this.projectService.getProjectById(idProject);
				
					//Setup fileUpload object.
					fileUpload.setDirectory(directoryStub.getPath());
					fileUpload.setState(FileStateEnum.SUCCESS);
					fileUpload.setMessage("");
					fileUpload.setType(FileTypeEnum.UPLOADED);
					fileUpload.setProject(project);
					fileUpload.setSize(localFile.length());
									
					//Create/update fileUpload object
					FileUpload existing = this.fileUploadService.getFileUploadByName(fileUpload.getName(), fileUpload.getType(), project);
					if (existing == null) {
						this.fileUploadService.addFileUpload(fileUpload);
					} else {
						this.fileUploadService.updateFileUpload(existing.getIdFileUpload(),fileUpload);
					}
				}					
				
				response.setStatus(200);
			} catch (Exception ex) {
				ex.printStackTrace();
				
				//If failed, send error response back
				response.setStatus(405);
				fileUpload.setMessage(ex.getMessage());

			}			
				
		} else {
			fileUpload.setMessage("File is empty");
			response.setStatus(405);
		}

		return fileUpload;
	}
	
	
	
	/***************************************************
	 * URL: /submit/upload  
	 * upload(): receives files
	 * post():
	 * @param file : MultipartFile
	 * @return FileMeta as json format
	 ****************************************************/
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public @ResponseBody 
	FileUpload upload(@RequestParam("file") MultipartFile file, @RequestParam("idProject") Long idProject) {
 
		FileUpload fileUpload = new FileUpload();

		String name = file.getOriginalFilename();
		
		if (!file.isEmpty()) {
			try {
				
				//Create directory
				File directory = new File(getRawDirectory(),String.valueOf(idProject));
				File directoryStub = new File("/raw/",String.valueOf(idProject));
				if (!directory.exists()) {
					directory.mkdir();
				}
				
				//Upload file
				if (name.endsWith(".bam") || name.endsWith(".bai") || name.endsWith(".useq") || name.endsWith(".bw") || name.endsWith(".gz") || name.endsWith(".zip")) {
					File localFile = new File(directory,name);
					FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
					fileUpload.setSize(localFile.length());
					fileUpload.setName(name);
				} else {
					File localFile = new File(directory,name + ".gz");
					FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
					fileUpload.setSize(localFile.length());
					fileUpload.setName(name + ".gz");
				}
				System.out.println("File upload successful! " + name);
				
				//Grab project object
				Project project = this.projectService.getProjectById(idProject);
				
				//Setup fileUpload object.
				fileUpload.setDirectory(directoryStub.getPath());
				fileUpload.setState(FileStateEnum.SUCCESS);
				fileUpload.setMessage("");
				fileUpload.setType(FileTypeEnum.UPLOADED);
				fileUpload.setProject(project);
				
				
				
				//Create/update fileUpload object
				FileUpload existing = this.fileUploadService.getFileUploadByName(fileUpload.getName(), fileUpload.getType(), project);
				if (existing == null) {
					this.fileUploadService.addFileUpload(fileUpload);
				} else {
					this.fileUploadService.updateFileUpload(existing.getIdFileUpload(),fileUpload);
				}
				
			} catch (Exception ex) {
				System.out.println("File upload failed: " + name + " " + ex.getMessage());
				fileUpload.setState(FileStateEnum.FAILURE);
				fileUpload.setMessage(ex.getMessage());
				ex.printStackTrace();
			}
		} else {
			fileUpload.setState(FileStateEnum.FAILURE);
			fileUpload.setMessage("File is empty");
		}

		return fileUpload;
	}
	
	
	/***************************************************
	 * URL: /submit/upload/get/
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param file : filename
	 * @param type : file type (IMPORTED or UPLOADED)
	 * @return void
	 * @throws Exception 
	 ****************************************************/
	 @RequestMapping(value = "/upload/get", method = RequestMethod.GET)
	 public void getUpload(HttpServletResponse response,@RequestParam("file") String file, @RequestParam("type") FileTypeEnum type, @RequestParam("idProject") Long idProject) throws Exception{
		 Project project = this.projectService.getProjectById(idProject);
		 FileUpload fileUpload = this.fileUploadService.getFileUploadByName(file,type,project);
		 
		 if (fileUpload == null) {
			try {
				response.sendError(400,"The specified file type is not recognized");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		 }
		 
		 try {		
		 	//response.setContentType(getFile.getFileType());
		 	response.setHeader("Content-disposition", "attachment; filename=\""+fileUpload.getName()+"\"");
		 	
		 	File localFile = generateFilePath(fileUpload);
		 	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile));
		 	
	        FileCopyUtils.copy(bis, response.getOutputStream());
		 }catch (IOException e) {
			e.printStackTrace();
		 }
	 }
	 
	
	 /***************************************************
	 * URL: /submit/upload/delete/
	 * delete: Delete uploaded/parsed file
	 * @param response : passed by the server
	 * @param file : filename
	 * @param type : file type (IMPORTED or UPLOADED)
	 * @return void
	 * @throws Exception 
	 ****************************************************/
	 @RequestMapping(value = "/upload/delete", method = RequestMethod.DELETE)
	 public void deleteFile(HttpServletResponse response,@RequestParam("file") String file, @RequestParam("type") FileTypeEnum type, @RequestParam("idProject") Long idProject) throws Exception{
		 Project project = this.projectService.getProjectById(idProject);
		 FileUpload fileUpload = this.fileUploadService.getFileUploadByName(file, type, project);
		 
		 
		 File fileToDelete = generateFilePath(fileUpload);
		 
		 boolean success = fileToDelete.delete();
		 
		 if (!success) {
			 System.out.println("File " + fileToDelete + " not deleted");
		 } else {
			 this.fileUploadService.deleteFileUploadById(fileUpload.getIdFileUpload());
		 }
	 }
	 
	 /***************************************************
	 * URL: /submit/upload/load/
	 * get(): add already loaded files into the hash
	 * @param response : passed by the server
	 * @param file : filename
	 * @param type : file type (parsed or imported)
	 * @return void
	 ****************************************************/
	@RequestMapping(value = "upload/load", method = RequestMethod.GET)
	 public @ResponseBody List<FileUpload>  get(HttpServletResponse response, @RequestParam("type") FileTypeEnum type, @RequestParam("idProject") Long idProject){
		Project project = this.projectService.getProjectById(idProject);
		List<FileUpload> fileMap = this.fileUploadService.getFileUploadByType(type,project);
		return fileMap;
	}
	
	
	 /***************************************************
	 * URL: /submit/parse/preview/
	 * post(): Generate a file preview
	 * @param file : filename
	 * @return PreviewMap: Container that holds the file preview
	 * @throws Exception 
	 ****************************************************/
	@RequestMapping(value = "parse/preview", method = RequestMethod.GET)
    @ResponseBody
	public PreviewMap getHeader(@RequestParam(value="name") String name, @RequestParam("idProject") Long idProject) throws Exception {
		 Project project = this.projectService.getProjectById(idProject);
		 FileUpload fileUpload = this.fileUploadService.getFileUploadByName(name, FileTypeEnum.UPLOADED, project);
		 PreviewMap pm = new PreviewMap();
		 
		 try {		
			 	//initialize variables
			 	String temp = null;
			 	int counter = 0;
			 	
			 	//Open a buffered reader
			 	BufferedReader br = ModelUtil.fetchBufferedReader(generateFilePath(fileUpload));
			 
			 	//Grab the first 20 lines of the file
			 	String[] lastHeader = null;
			 	while((temp = br.readLine()) != null) {
			 		if (temp.startsWith("#")) {
			 			lastHeader  = temp.split("\t");
			 			continue;
			 		}
			 		if (lastHeader != null) {
			 			pm.addPreviewData(lastHeader);
			 		}
			 		
			 		
			 		if (counter == 10) {
			 			break;
			 		}
			 		
			 		//split the file by tabs and add to the preview object
			 		String[] items = temp.split("\t");
			 		//LinkedList<String> dataLine = new LinkedList<String>(Arrays.asList(items));
			 		
			 		pm.addPreviewData(items);
			 		counter++;
			 	}
			 	
			 	pm.setMessage("success");
			 	br.close();
		 }catch (IOException ioex) {
			    pm.setMessage("Error processing file: " + ioex.getMessage());
			    System.out.println("Error messaging: " + ioex.getMessage());
		 }
		 return pm;
	 }
	
	
	 /***************************************************
	 * URL: /submit/parse/chip/
	 * post(): call chip parser
	 * @param input : input filename
	 * @param output: output filename
	 * @param chromosome: column index chromsome
	 * @param start: column index start
	 * @param end: column index stop
	 * @param log: column index log ratio
	 * @param fdr: column index fdr
	 * @param idFileUpload: reference to the uploaded file
	 * @param idAnalysisType: reference to the analysis type
	 * @return FileMap: parsed file information
	 ****************************************************/
	@RequestMapping(value="parse/chip", method = RequestMethod.POST)
	public @ResponseBody 
	FileUpload parseChip(
			@RequestParam("inputFile") String input, 
			@RequestParam("outputFile") String output,
			@RequestParam("Chromosome") Integer chrom, 
			@RequestParam("Start") Integer start, 
			@RequestParam("End") Integer end,
			@RequestParam("Log2Ratio") Integer log, 
			@RequestParam("FDR") Integer fdr, 
			@RequestParam("build") Long idOrganismBuild, 
			@RequestParam("analysisID") String id, 
			@RequestParam("-10*log10(FDR)") Integer logFDR, 
			@RequestParam("idFileUpload") Long idParent,
			@RequestParam("idAnalysisType") Long idAnalysisType) {
 
		FileUpload outputMeta = new FileUpload();
		
		try {
			
			OrganismBuild gb = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
			AnalysisType at = this.analysisTypeService.getAnalysisTypeById(idAnalysisType);
			Genome genome = null;
			
			try {
				genome = GenomeBuilds.fetchGenome(gb);
			} catch (Exception ex) {
				ex.printStackTrace();
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage(String.format(ex.getMessage(), gb.getName()));
				return outputMeta;
			}
			
			
			File importDir = new File(getRawDirectory(),id);

			File parseDir = new File(getParsedDirectory(),id);
			if (!parseDir.exists()) {
				parseDir.mkdir();
			}
			File parseStub = new File("/parsed/",id);
			
			File inputFile = new File(importDir, input);
			File outputFile = new File(parseDir, output);

			//Add gz extension if it doesn't exist
			if (!outputFile.getName().endsWith(".gz")) {
				outputFile = new File(outputFile.getParent(),outputFile.getName() + ".gz");
			}
			
			//Run parser, throw errors if log2 or FDR are not set
			String warningMessage = "";
			if (fdr != -1) {
				ChipParser cp = new ChipParser(inputFile, outputFile, chrom, start, end, fdr, log, false, genome);
				warningMessage = cp.run();
			} else if (logFDR != 1) {
				ChipParser cp = new ChipParser(inputFile, outputFile, chrom, start, end, logFDR, log, true, genome);
				warningMessage = cp.run();
			} else {
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage("Neither FDR or 10*log10(FDR) were set.");
				return outputMeta;
			}
			
			//Set result messages
			if (warningMessage.equals("")) {
				outputMeta.setState(FileStateEnum.SUCCESS);
				outputMeta.setMessage("");
			} else {
				outputMeta.setState(FileStateEnum.WARNING);
				outputMeta.setMessage(warningMessage);
			}
		
			outputMeta.setName(output);
			outputMeta.setDirectory(parseStub.getPath());
			outputMeta.setSize(new Long(outputFile.length()));
			
			
			FileUpload parent = this.fileUploadService.getFileUploadById(idParent);		
			
			outputMeta.setType(FileTypeEnum.IMPORTED);
			outputMeta.setProject(parent.getProject());
			outputMeta.setAnalysisType(at);
			
			FileUpload existing = this.fileUploadService.getFileUploadByName(outputMeta.getName(), outputMeta.getType(), outputMeta.getProject());
			if (existing == null) {
				this.fileUploadService.addFileUpload(outputMeta);
			} else {
				this.fileUploadService.updateFileUpload(existing.getIdFileUpload(),outputMeta);
			}
			
		
		} catch (Exception ioex) {
			outputMeta.setName(output);
			outputMeta.setSize(null);
			outputMeta.setState(FileStateEnum.FAILURE);
			outputMeta.setMessage(ioex.getMessage());
		}
		
		return outputMeta;
	}
	
	/***************************************************
	 * URL: /submit/parse/rnaseq/
	 * post(): call rnaseq parser
	 * @param input : input filename
	 * @param output: output filename
	 * @param gene: gene name
	 * @param log: column index log ratio
	 * @param fdr: column index fdr
	 * @param idFileUpload: reference to the uploaded file
	 * @param idAnalysisType: reference to the analysis type
	 * @return FileMap: parsed file information
	 ****************************************************/
	@RequestMapping(value="parse/rnaseq", method = RequestMethod.POST)
	public @ResponseBody 
	FileUpload parseRnaseq(
			@RequestParam("inputFile") String input, 
			@RequestParam("outputFile") String output,
			@RequestParam("Gene") Integer gene,
			@RequestParam("Log2Ratio") Integer log, 
			@RequestParam("FDR") Integer fdr, 
			@RequestParam("build") Long idOrganismBuild, 
			@RequestParam("analysisID") String id, 
			@RequestParam("-10*log10(FDR)") Integer logFDR, 
			@RequestParam("idFileUpload") Long idParent,
			@RequestParam("idAnalysisType") Long idAnalysisType) {
 
		FileUpload outputMeta = new FileUpload();
		
		try {
			
			OrganismBuild gb = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
			AnalysisType at = this.analysisTypeService.getAnalysisTypeById(idAnalysisType);
			
			List<ExternalGene> egList = this.externalGeneService.getExternalGenesByOrganismBuild(gb);
			
			Genome genome = null;
			
			try {
				genome = GenomeBuilds.fetchGenome(gb);
			} catch (Exception ex) {
				ex.printStackTrace();
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage(String.format(ex.getMessage(), gb.getName()));
				return outputMeta;
			}
			
			File importDir = new File(getRawDirectory(),id);
			File parseDir = new File(getParsedDirectory(),id);
			if (!parseDir.exists()) {
				parseDir.mkdir();
			}
			File parseStub = new File("/parsed/",id);
			
			File inputFile = new File(importDir, input);
			File outputFile = new File(parseDir, output);
			
			//Add gz extension if it doesn't exist
			if (!outputFile.getName().endsWith(".gz")) {
				outputFile = new File(outputFile.getParent(),outputFile.getName() + ".gz");
			}
			
			//Run parser, throw errors if log2 or FDR are not set
			String warningMessage = "";
			if (fdr != -1) {
				RnaSeqParser rp = new RnaSeqParser(inputFile, outputFile, gene, fdr, log, false, egList, genome);
				warningMessage = rp.run();
			} else if (logFDR != 1) {
				RnaSeqParser rp = new RnaSeqParser(inputFile, outputFile, gene, logFDR, log, true, egList, genome);
				warningMessage = rp.run();
			} else {
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage("Neither FDR or 10*log10(FDR) were set.");
				return outputMeta;
			}
			
			//Set result messages
			if (warningMessage.equals("")) {
				outputMeta.setState(FileStateEnum.SUCCESS);
				outputMeta.setMessage("");
			} else {
				outputMeta.setState(FileStateEnum.WARNING);
				outputMeta.setMessage(warningMessage);
			}
		
			outputMeta.setName(output);
			outputMeta.setDirectory(parseStub.getPath());
			outputMeta.setSize(new Long(outputFile.length()));
			
			
			FileUpload parent = this.fileUploadService.getFileUploadById(idParent);		
			
			outputMeta.setType(FileTypeEnum.IMPORTED);
			outputMeta.setProject(parent.getProject());
			outputMeta.setAnalysisType(at); 
			
			FileUpload existing = this.fileUploadService.getFileUploadByName(outputMeta.getName(), outputMeta.getType(), outputMeta.getProject());
			if (existing == null) {
				this.fileUploadService.addFileUpload(outputMeta);
			} else {
				this.fileUploadService.updateFileUpload(existing.getIdFileUpload(),outputMeta);
			}
			
		
		} catch (Exception ioex) {
			ioex.printStackTrace();
			outputMeta.setName(output);
			outputMeta.setSize(null);
			outputMeta.setState(FileStateEnum.FAILURE);
			outputMeta.setMessage(ioex.getMessage());
		}
		
		return outputMeta;
	}
	
	/***************************************************
	 * URL: /submit/parse/variant/
	 * post(): call variant parser
	 * @param input : input filename
	 * @param output: output filename
	 * @param idFileUpload: reference to the uploaded file
	 * @param idAnalysisType: reference to the analysis type
	 * @return FileMap: parsed file information
	 ****************************************************/
	@RequestMapping(value="parse/variant", method = RequestMethod.POST)
	public @ResponseBody 
	FileUpload parseVariant(
			@RequestParam("inputFile") String input, 
			@RequestParam("outputFile") String output,
			@RequestParam("build") Long idOrganismBuild, 
			@RequestParam("analysisID") String id, 
			@RequestParam("idFileUpload") Long idParent,
			@RequestParam("idAnalysisType") Long idAnalysisType) {
 
		FileUpload outputMeta = new FileUpload();
		
		try {
			
			OrganismBuild gb = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
			AnalysisType at = this.analysisTypeService.getAnalysisTypeById(idAnalysisType);
			
			List<ExternalGene> egList = this.externalGeneService.getExternalGenesByOrganismBuild(gb);
			
			File importDir = new File(getRawDirectory(),id);
			File parseDir = new File(getParsedDirectory(),id);
			if (!parseDir.exists()) {
				parseDir.mkdir();
			}
			File parseStub = new File("/parsed/",id);
			
			File inputFile = new File(importDir, input);
			File outputFile = new File(parseDir, output);
			
			//Add gz extension if it doesn't exist
			if (!outputFile.getName().endsWith(".gz")) {
				outputFile = new File(outputFile.getParent(),outputFile.getName() + ".gz");
			}
			
			//Run parser, throw errors if log2 or FDR are not set
			String warningMessage = "";
			
			Genome genome = null;
			
			try {
				genome = GenomeBuilds.fetchGenome(gb);
			} catch (Exception ex) {
				ex.printStackTrace();
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage(String.format(ex.getMessage(), gb.getName()));
				return outputMeta;
			}
			
			VCFParser vp = new VCFParser(inputFile, outputFile, egList, genome);
			warningMessage = vp.run();
			
			//Set result messages
			if (warningMessage.equals("")) {
				outputMeta.setState(FileStateEnum.SUCCESS);
				outputMeta.setMessage("");
			} else {
				outputMeta.setState(FileStateEnum.WARNING);
				outputMeta.setMessage(warningMessage);
			}
		
			outputMeta.setName(output);
			outputMeta.setDirectory(parseStub.getPath());
			outputMeta.setSize(new Long(outputFile.length()));
			
			
			FileUpload parent = this.fileUploadService.getFileUploadById(idParent);		
			
			outputMeta.setType(FileTypeEnum.IMPORTED);
			outputMeta.setProject(parent.getProject());
			outputMeta.setAnalysisType(at); 
			
			FileUpload existing = this.fileUploadService.getFileUploadByName(outputMeta.getName(), outputMeta.getType(), outputMeta.getProject());
			if (existing == null) {
				this.fileUploadService.addFileUpload(outputMeta);
			} else {
				this.fileUploadService.updateFileUpload(existing.getIdFileUpload(),outputMeta);
			}
			
		
		} catch (Exception ioex) {
			ioex.printStackTrace();
			outputMeta.setName(output);
			outputMeta.setSize(null);
			outputMeta.setState(FileStateEnum.FAILURE);
			outputMeta.setMessage(ioex.getMessage());
		}
		
		return outputMeta;
	}
	
}
