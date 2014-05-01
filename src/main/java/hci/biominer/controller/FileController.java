package hci.biominer.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import hci.biominer.util.FileMap;
import hci.biominer.util.FileMeta;

@Controller
@RequestMapping("/submit")
public class FileController {

	
	FileMap fileMap = new FileMap();
	LinkedList<FileMeta> files = new LinkedList<FileMeta>();
	FileMeta fileMeta = null;
	/***************************************************
	 * URL: /rest/controller/upload  
	 * upload(): receives files
	 * @param request : MultipartHttpServletRequest auto passed
	 * @param response : HttpServletResponse auto passed
	 * @return LinkedList<FileMeta> as json format
	 ****************************************************/
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public @ResponseBody FileMap upload(MultipartHttpServletRequest request, HttpServletResponse response) {
 
		fileMap = new FileMap();
		files = new LinkedList<FileMeta>();
		
		 //1. build an iterator
		 Iterator<String> itr =  request.getFileNames();
		 MultipartFile mpf = null;
		 int index = 0;

		 //2. get each file
		 while(itr.hasNext()){
			 
			 //2.1 get next MultipartFile
			 mpf = request.getFile(itr.next()); 
			 System.out.println(mpf.getOriginalFilename() +" uploaded! "+files.size());

			 //2.2 if files > 10 remove the first from the list
			 if(files.size() >= 10)
				 files.pop();
			 
			 //2.3 create new fileMeta
			 fileMeta = new FileMeta();
			 fileMeta.setName(mpf.getOriginalFilename());
			 fileMeta.setSize(mpf.getSize()/1024+" Kb");
			 fileMeta.setUrl(      "http://localhost:8080/biominer/submit/upload/get/" + index);
			 fileMeta.setDeleteUrl("http://localhost:8080/biominer/submit/upload/delete/" + index);
			
			 
			 try {
				fileMeta.setBytes(mpf.getBytes());
				
				// copy file to local disk (make sure the path "e.g. D:/temp/files" exists)
				FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream("/temp/"+mpf.getOriginalFilename()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 //2.4 add to files
			 files.add(fileMeta);
			 index++;
			 
		 }
		 
		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
		fileMap.setFiles( files);
		return fileMap;
 
	}
	/***************************************************
	 * URL: /submit/upload/get/{value}
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param value : value from the URL
	 * @return void
	 ****************************************************/
	 @RequestMapping(value = "/upload/get/{value}", method = RequestMethod.GET)
	 public void getFile(HttpServletResponse response,@PathVariable String value){
		 FileMeta getFile = files.get(Integer.parseInt(value));
		 try {		
			 	//response.setContentType(getFile.getFileType());
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getName()+"\"");
		        FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());
		 }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
	 }
	
	 @RequestMapping(value = "/upload/delete/{value}", method = RequestMethod.DELETE)
	 public void deleteFile(HttpServletResponse response,@PathVariable String value){
		 FileMeta file = files.get(Integer.parseInt(value));
		 try {		
			 	File f = new File(file.getName());
			 	boolean success = f.delete();
			 	if (!success) {
			 		System.out.println("File " + file.getName() + " not deleted");
			 	}
			 	
		 }catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
	 }

	
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	 public @ResponseBody FileMap  get(HttpServletResponse response){
		File folder = new File("/temp");
		File[] listOfFiles = folder.listFiles();
		fileMap = new FileMap();
		files = new LinkedList<FileMeta>();
		int index = 0;

		for (File file : listOfFiles) {
		    if (file.isFile() && !file.getName().startsWith(".")) {
		        
				 fileMeta = new FileMeta();
				 fileMeta.setName(file.getName());
				 fileMeta.setSize(file.length()/1024+" Kb");
				 fileMeta.setUrl(      "http://localhost:8080/biominer/submit/upload/get/" + index);
				 fileMeta.setDeleteUrl("http://localhost:8080/biominer/submit/upload/delete/" + index);

				 files.add(fileMeta);
				 index++;
		    }
		}
		fileMap.setFiles(files);
		return fileMap;
		
	}
 
}
