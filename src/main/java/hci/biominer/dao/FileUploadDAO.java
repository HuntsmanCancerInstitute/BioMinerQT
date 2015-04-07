package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.FileUpload;
import hci.biominer.model.Project;
import hci.biominer.util.Enumerated.*;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileUploadDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<FileUpload> getFileUploads() {
		Session session = this.getCurrentSession();
		List<FileUpload> fileUploads = session.createQuery("from FileUpload").list();
		session.close();
		return fileUploads;
	}
	
	
	public FileUpload getFileUploadById(Long idFileUpload) {
		Session session = getCurrentSession();
		FileUpload fileUpload = (FileUpload)session.get(FileUpload.class, idFileUpload);
		session.close();
		return fileUpload;
	}
	
	public void addFileUpload(FileUpload fileUpload) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(fileUpload);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateFileUpload(Long idFileUpload, FileUpload fileUpload) {
		Session session = getCurrentSession();
		session.beginTransaction();
		FileUpload FileUploadToUpdate = (FileUpload) session.get(FileUpload.class, idFileUpload);
		FileUploadToUpdate.setName(fileUpload.getName());
		FileUploadToUpdate.setDirectory(fileUpload.getDirectory());
		FileUploadToUpdate.setSize(fileUpload.getSize());
		FileUploadToUpdate.setType(fileUpload.getType());
		FileUploadToUpdate.setProject(fileUpload.getProject());
		FileUploadToUpdate.setAnalysisType(fileUpload.getAnalysisType());
		FileUploadToUpdate.setState(fileUpload.getState());
		FileUploadToUpdate.setParent(fileUpload.getParent());
		FileUploadToUpdate.setMessage(fileUpload.getMessage());
		session.update(FileUploadToUpdate);
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteFileUpload(Long idFileUpload) {
		Session session = getCurrentSession();
		session.beginTransaction();
		FileUpload FileUpload = (FileUpload) session.get(FileUpload.class, idFileUpload);
		if (FileUpload != null) {
			session.delete(FileUpload);
		}
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public FileUpload getFileUploadByName(String name, FileTypeEnum type, Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from FileUpload where name = :name and type = :type and project = :project");
		query.setParameter("name", name);
		query.setParameter("type", type);
		query.setParameter("project", project);
		List<FileUpload> fileUploads = query.list();
		session.close();
		//assumes there is only one!
		if (fileUploads.size() > 0) {
			return fileUploads.get(0);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<FileUpload> getFileUploadByType(FileTypeEnum type, Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from FileUpload where type = :type and project = :project");
		query.setParameter("type", type);
		query.setParameter("project",project);
		List<FileUpload> fileUploads = query.list();
		session.close();
		return fileUploads;
	}
	
	@SuppressWarnings("unchecked")
	public List<FileUpload> getFileUploadByProject(Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from FileUpload where project = :project");
		query.setParameter("project",project);
		List<FileUpload> fileUploads = query.list();
		session.close();
		return fileUploads;
	}
	
	
	
}
