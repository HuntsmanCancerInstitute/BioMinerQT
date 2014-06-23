package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hci.biominer.model.genome.Genome;
import hci.biominer.parser.ChipParser;
import hci.biominer.parser.GenomeParser;
import hci.biominer.util.FileMap;
import hci.biominer.util.FileMeta;
import hci.biominer.util.PreviewMap;
import hci.biominer.util.ModelUtil;

@Controller
@RequestMapping("/submit")
public class FileController {

	//Persistence containers.
	private HashMap<String,FileMeta> importedFileHash = new HashMap<String,FileMeta>();
	private HashMap<String,FileMeta> parsedFileHash = new HashMap<String,FileMeta>();
	
	//Hard-coded file locations
	private final static String FILES_PATH = "/temp/";
	private final static String PARSED_PATH = "/parsed/";
	
	private final static String SUCCESS = "success";
	private final static String FAILURE = "failure";
	private final static String WARNING = "warning";
	
	//Load genome descriptons
	private HashMap<String,File> genomePaths; 
    private HashMap<String,Genome> loadedGenomes;
	
	public FileController() {
		loadedGenomes = new HashMap<String,Genome>();
		
		genomePaths = new HashMap<String,File>();
		genomePaths.put("hg19",new File("/Users/timmosbruger/Documents/eclipse4.3/BiominerQT/AnnotationFiles/hg19_GRCh37_Genome.txt"));
		
		
		for (String key: genomePaths.keySet()) {
			GenomeParser gp;
			try {
				gp = new GenomeParser(genomePaths.get(key));
				Genome genome = gp.getGenome();
				loadedGenomes.put(key,genome);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	FileMeta upload(@RequestParam("file") MultipartFile file, @RequestParam("analysisID") String id) {
 
		FileMeta fileMeta = new FileMeta();
		String name = file.getOriginalFilename();
		
		if (!file.isEmpty()) {
			try {
				
				File directory = new File(FILES_PATH,id);
				if (!directory.exists()) {
					directory.mkdir();
				}
				fileMeta.setDirectory(directory.getAbsolutePath());
				
				if (name.endsWith(".bam") || name.endsWith(".bai") || name.endsWith(".useq") || name.endsWith(".bw") || name.endsWith(".gz") || name.endsWith(".zip")) {
					File localFile = new File(directory,name);
					FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
					fileMeta.setSize(String.valueOf(localFile.length()));
					fileMeta.setName(name);
				} else {
					File localFile = new File(directory,name + ".gz");
					FileCopyUtils.copy(file.getInputStream(), new GZIPOutputStream(new FileOutputStream(localFile)));
					fileMeta.setSize(String.valueOf(localFile.length()));
					fileMeta.setName(name + ".gz");
				}
				
				System.out.println("File upload successful! " + name);
				fileMeta.setState(SUCCESS);
				fileMeta.setMessage("");
				importedFileHash.put(name, fileMeta);
				
			} catch (Exception ex) {
				System.out.println("File upload failed: " + name + " " + ex.getMessage());
				fileMeta.setState(FAILURE);
				fileMeta.setMessage(ex.getMessage());
			}
		} else {
			fileMeta.setState(FAILURE);
			fileMeta.setMessage("File is empty");
		}
		
		return fileMeta;
	}
	
	
	/***************************************************
	 * URL: /submit/upload/get/
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param file : filename
	 * @param type : file type (parsed or imported)
	 * @return void
	 ****************************************************/
	 @RequestMapping(value = "/upload/get", method = RequestMethod.GET)
	 public void getFile(HttpServletResponse response,@RequestParam("file") String file, @RequestParam("type") String type){
		 HashMap<String,FileMeta> fileHash;
		 
		 if (type.equals("imported")) {
			 fileHash = this.importedFileHash;
		 } else if (type.equals("parsed")) {
			 fileHash = this.parsedFileHash;
		 } else {
				 try {
					response.sendError(400,"The specified file type is not recognized");
				} catch (IOException e) {
					e.printStackTrace();
				}
			 return;
		 }
		 
		 FileMeta getFile = fileHash.get(file);
		 
		 try {		
			 	//response.setContentType(getFile.getFileType());
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getName()+"\"");
			 	
			 	File localFile = new File(getFile.getDirectory(), getFile.getName());
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
	 * @param type : file type (parsed or imported)
	 * @return void
	 ****************************************************/
	 @RequestMapping(value = "/upload/delete", method = RequestMethod.DELETE)
	 public void deleteFile(HttpServletResponse response,@RequestParam("file") String file, @RequestParam("type") String type){
		 HashMap<String,FileMeta> fileHash;
		 
		 if (type.equals("imported")) {
			 fileHash = this.importedFileHash;
		 } else if (type.equals("parsed")) {
			 fileHash = this.parsedFileHash;
		 } else {
				try {
					response.sendError(400,"The specified file type is not recognized");
				} catch (IOException e) {
					e.printStackTrace();
				}
			 return;
		 }
		 
		 FileMeta deleteFile = fileHash.get(file);
		 try {		
			 
			    System.out.println(deleteFile.getDirectory());
			    System.out.println(deleteFile.getName());
			 	File f = new File(deleteFile.getDirectory(),deleteFile.getName());
			 	boolean success = f.delete();
			 	
			 	if (!success) {
			 		System.out.println("File " + deleteFile.getName() + " not deleted");
			 		fileHash.remove(file);
			 	}
			 	
		 }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	 public @ResponseBody FileMap  get(HttpServletResponse response, @RequestParam("type") String type, @RequestParam("analysisID") String id){
		HashMap<String,FileMeta> fileHash; //HashMap container, might get replaced with db
		FileMap fileMap = new FileMap();
		File folder;
		
		if (type.equals("imported")) {
			 folder = new File(FILES_PATH,id);
			 fileHash = this.importedFileHash;
		 } else if (type.equals("parsed")) {
			 folder = new File(PARSED_PATH,id);
			 fileHash = this.parsedFileHash;
		 } else {
				 try {
					response.sendError(400,"The specified file type is not recognized");
				} catch (IOException e) {
					e.printStackTrace();
				}
			 return fileMap;
		 }
		
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		
		File[] listOfFiles = folder.listFiles();
		FileMeta fileMeta;
		

		for (File file : listOfFiles) {
		    if (file.isFile() && !file.getName().startsWith(".")) {
		         String name = file.getName();
		        
	        	 fileMeta = new FileMeta();
				 fileMeta.setName(name);
				 fileMeta.setDirectory(folder.getPath());
				 fileMeta.setSize(new Long(file.length()).toString());
				 fileMeta.setMessage("");
				 fileMeta.setState(SUCCESS);
				 fileHash.put(name,fileMeta);
				 fileMap.addFile(fileMeta);
		    }
		}
		return fileMap;
	}
	
	
	 /***************************************************
	 * URL: /submit/parse/preview/
	 * post(): Generate a file preview
	 * @param file : filename
	 * @return PreviewMap: Container that holds the file preview
	 ****************************************************/
	@RequestMapping(value = "parse/preview", method = RequestMethod.POST)
    @ResponseBody
	public PreviewMap getHeader(@RequestParam(value="filename") String file) {
		 FileMeta fm = this.importedFileHash.get(file);
		 PreviewMap pm = new PreviewMap();
		 
		 try {		
			 	//initialize variables
			 	String temp = null;
			 	int counter = 0;
			 	
			 	//Open a buffered reader
			 	BufferedReader br = ModelUtil.fetchBufferedReader(new File(fm.getDirectory(),fm.getName()));
			 
			 	//Grab the first 20 lines of the file
			 	while((temp = br.readLine()) != null) {
			 		if (counter == 20) {
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
	 * @return FileMap: parsed file information
	 ****************************************************/
	@RequestMapping(value="parse/chip", method = RequestMethod.POST)
	public @ResponseBody 
	FileMeta upload(@RequestParam("inputFile") String input, @RequestParam("outputFile") String output,
			@RequestParam("Chromosome") Integer chrom, @RequestParam("Start") Integer start, @RequestParam("End") Integer end,
			@RequestParam("Log2Ratio") Integer log, @RequestParam("FDR") Integer fdr, @RequestParam("genome") String genomeName, 
			@RequestParam("analysisID") String id, @RequestParam("10*log10(FDR)") Integer logFDR) {
 
		FileMeta outputMeta = new FileMeta();
		
		try {
			
			if (!this.loadedGenomes.containsKey(genomeName)) {
				outputMeta.setName(output);
				outputMeta.setSize(null);
				outputMeta.setMessage(String.format("The selected genome %s does not have a transcriptome object.",genomeName));
				return outputMeta;
			}
			
			Genome genome = this.loadedGenomes.get(genomeName);
			
			File importDir = new File(FILES_PATH,id);
			File parseDir = new File(PARSED_PATH,id);
			File inputFile = new File(importDir, input);
			File outputFile = new File(parseDir, output);
			
			//Add gz extension if it doesn't exisst
			if (!outputFile.getName().endsWith(".gz")) {
				outputFile = new File(outputFile.getParent(),outputFile.getName() + ".gz");
			}
			
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
			
			if (warningMessage.equals("")) {
				outputMeta.setState(SUCCESS);
				outputMeta.setMessage("");
			} else {
				outputMeta.setState(WARNING);
				outputMeta.setMessage(warningMessage);
			}
			
			outputMeta.setName(output);
			outputMeta.setDirectory(parseDir.getAbsolutePath());
			outputMeta.setSize(new Long(outputFile.length()).toString());
			
			this.parsedFileHash.put(output, outputMeta);
		} catch (Exception ioex) {
			outputMeta.setName(output);
			outputMeta.setSize(null);
			outputMeta.setState(FAILURE);
			outputMeta.setMessage(ioex.getMessage());
		}
		
	
		return outputMeta;
	}
	
}
