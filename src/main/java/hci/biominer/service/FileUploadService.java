package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.FileUploadDAO;
import hci.biominer.model.FileUpload;
import hci.biominer.model.Project;

import hci.biominer.util.Enumerated.*;

@Service("FileUploadService")
@Transactional
public class FileUploadService {
	@Autowired
	private FileUploadDAO fileUploadDAO;
	
	public FileUpload getFileUploadById(Long idFileUpload) {
		return fileUploadDAO.getFileUploadById(idFileUpload);
	}

	public List<FileUpload> getAllFileUploads() {
		return fileUploadDAO.getFileUploads();
	}
	
	public void addFileUpload(FileUpload fileUpload) {
		fileUploadDAO.addFileUpload(fileUpload);		
	}

	public void deleteFileUploadById(Long idFileUpload) {
        fileUploadDAO.deleteFileUpload(idFileUpload);
	}

	public void updateFileUpload(Long idFileUpload, FileUpload fileUpload) {
        fileUploadDAO.updateFileUpload(idFileUpload, fileUpload);
	}
	
	public FileUpload getFileUploadByName(String name, FileTypeEnum type, Project project) {
		return fileUploadDAO.getFileUploadByName(name, type, project);
	}
	
	public List<FileUpload> getFileUploadByType(FileTypeEnum type, Project project) {
		return fileUploadDAO.getFileUploadByType(type, project);
	}
	
}
