package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

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

@Controller
@RequestMapping("/submit")
public class FileController {

	//Persistence containers.
	HashMap<String,FileMeta> importedFileHash = new HashMap<String,FileMeta>();
	HashMap<String,FileMeta> parsedFileHash = new HashMap<String,FileMeta>();
	
	//Hard-coded file locations
	private final static String FILES_PATH = "/temp/";
	private final static String PARSED_PATH = "/parsed/";
	
	
	/***************************************************
	 * URL: /submit/upload  
	 * upload(): receives files
	 * post():
	 * @param file : MultipartFile
	 * @return FileMeta as json format
	 ****************************************************/
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public @ResponseBody 
	FileMeta upload(@RequestParam("file") MultipartFile file) {
 
		FileMeta fileMeta = new FileMeta();
		String name = file.getOriginalFilename();
		
		if (!file.isEmpty()) {
			try {
				
				fileMeta.setName(name);
				fileMeta.setSize(new Long(file.getSize()).toString());
				fileMeta.setDirectory(FILES_PATH);
				
				File localFile = new File(FILES_PATH,name);
				FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(localFile));
				
				System.out.println("File upload successful! " + name);
				fileMeta.setMessage("success");
				importedFileHash.put(name, fileMeta);
				
			} catch (Exception ex) {
				System.out.println("File upload failed: " + name + " " + ex.getMessage());
				fileMeta.setMessage(ex.getMessage());
			}
		} else {
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
	 public @ResponseBody FileMap  get(HttpServletResponse response, @RequestParam("type") String type){
		HashMap<String,FileMeta> fileHash; //HashMap container, might get replaced with db
		FileMap fileMap = new FileMap();
		File folder;
		
		if (type.equals("imported")) {
			 folder = new File(FILES_PATH);
			 fileHash = this.importedFileHash;
		 } else if (type.equals("parsed")) {
			 folder = new File(PARSED_PATH);
			 fileHash = this.parsedFileHash;
		 } else {
				 try {
					response.sendError(400,"The specified file type is not recognized");
				} catch (IOException e) {
					e.printStackTrace();
				}
			 return fileMap;
		 }
		
		
		File[] listOfFiles = folder.listFiles();
		FileMeta fileMeta;
		

		for (File file : listOfFiles) {
		    if (file.isFile() && !file.getName().startsWith(".")) {
		         String name = file.getName();
		         
	        	 FileInputStream fileInputStream=null;
	       
	             byte[] bFile = new byte[(int) file.length()];
	      
	             try {
	                 //convert file into array of bytes
		     	    fileInputStream = new FileInputStream(file);
		     	    fileInputStream.read(bFile);
		     	    fileInputStream.close();
	    
	             }catch(Exception e){
	             	e.printStackTrace();
	             }

	        	 fileMeta = new FileMeta();
				 fileMeta.setName(name);
				 fileMeta.setDirectory(folder.getPath());
				 fileMeta.setSize(new Long(file.length()).toString());
				 fileMeta.setMessage("success");
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
			 	BufferedReader br = new BufferedReader(new FileReader(new File(fm.getDirectory(),fm.getName())));
			 
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
			@RequestParam("Log2Ratio") Integer log, @RequestParam("FDR") Integer fdr) {
 
		
		FileMeta outputMeta = new FileMeta();
		
		try {
			//Grab genome.. temporarily hard-coded
			File descriptorFile = new File ("/Users/timmosbruger/Documents/eclipse4.3/BiominerQT/AnnotationFiles/hg19_GRCh37_Genome.txt");
			GenomeParser gp = new GenomeParser (descriptorFile);
			Genome genome = gp.getGenome();
			
			File inputFile = new File(FILES_PATH, input);
			File outputFile = new File(PARSED_PATH, output);
			new ChipParser(inputFile, outputFile, chrom, start, end, fdr, log, true, genome);
			
			outputMeta.setName(output);
			outputMeta.setDirectory(PARSED_PATH);
			outputMeta.setSize(new Long(outputFile.length()).toString());
			outputMeta.setMessage("success");
		} catch (Exception ioex) {
			outputMeta.setName(output);
			outputMeta.setSize(null);
			outputMeta.setMessage(ioex.getMessage());
		}
		
		return outputMeta;
	}
	
}
