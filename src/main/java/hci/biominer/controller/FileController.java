package hci.biominer.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hci.biominer.util.FileMap;
import hci.biominer.util.FileMeta;

@Controller
@RequestMapping("/submit")
public class FileController {

	
	//FileMap fileMap = new FileMap();
	HashMap<String,FileMeta> files = new HashMap<String,FileMeta>();
	LinkedList<FileMeta> fileList = new LinkedList<FileMeta>();
	FileMap fileMap = new FileMap();
	
	private final static String FILES_PATH = "/temp/";
	
	FileMeta fileMeta = null;
	/***************************************************
	 * URL: /rest/controller/upload  
	 * upload(): receives files
	 * @param request : MultipartHttpServletRequest auto passed
	 * @param response : HttpServletResponse auto passed
	 * @return LinkedList<FileMeta> as json format
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
				fileMeta.setUrl("submit/upload/get/" + name);
				fileMeta.setDeleteUrl("submit/upload/delete/" + name);
				
				fileMeta.setBytes(file.getBytes());
				
				File localFile = new File(FILES_PATH,name);
				FileCopyUtils.copy(file.getBytes(), new FileOutputStream(localFile));
				
				System.out.println("File upload successful! " + name);
				fileMeta.setMessage("success");
				files.put(name, fileMeta);
				
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
	 * URL: /submit/upload/get/{value}
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param value : value from the URL
	 * @return void
	 ****************************************************/
	 @RequestMapping(value = "/upload/get/{value:.+}", method = RequestMethod.GET)
	 public void getFile(HttpServletResponse response,@PathVariable String value){
		 FileMeta getFile = files.get(value);
		 
		 try {		
			 	//response.setContentType(getFile.getFileType());
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getName()+"\"");
		        FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());
		 }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
	 }
	
	 @RequestMapping(value = "/upload/delete/{value:.+}", method = RequestMethod.DELETE)
	 public void deleteFile(HttpServletResponse response,@PathVariable String value){
		 FileMeta file = files.get(value);
		 try {		
			 	File f = new File(FILES_PATH+file.getName());
			 	boolean success = f.delete();
			 	
			 	if (!success) {
			 		System.out.println("File " + file.getName() + " not deleted");
			 		files.remove(value);
			 	}
			 	
		 }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
	 }
	 
	 

	
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	 public @ResponseBody FileMap  get(HttpServletResponse response){
		File folder = new File(FILES_PATH);
		File[] listOfFiles = folder.listFiles();
		FileMap fileMap = new FileMap();

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
				 fileMeta.setSize(new Long(file.length()).toString());
				 fileMeta.setUrl(      "submit/upload/get/" + name);
				 fileMeta.setDeleteUrl("submit/upload/delete/" + name);
				 fileMeta.setMessage("success");
				 fileMeta.setBytes(bFile);
				 files.put(name,fileMeta);
				 fileList.add(fileMeta);
		         
		    }
		}
		fileMap.setFiles(fileList);
		return fileMap;
	}
	
//	@RequestMapping(value = "/header/get/{value:.+}", method = RequestMethod.GET)
//	public List<List<String>> getHeader(@PathVariable String value) {
//		 FileMeta fm = files.get(value);
//		 
//		 try {		
//			 	BufferedReader br = new BufferedReader(new FileReader(new File(FILES_PATH,fm.getName())));
//			 
//			 	String temp = null;
//			 	
//			 	while((temp = br.readLine()) != null) {
//			 		
//			 		
//			 	}
//			 	
//			 	
//		 }catch (IOException e) {
//				e.printStackTrace();
//		 }
//	 }
 
}
