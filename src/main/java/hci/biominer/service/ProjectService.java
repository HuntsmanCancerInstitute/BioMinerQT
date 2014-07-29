package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.ProjectDAO;
import hci.biominer.model.Project;
import hci.biominer.model.access.User;

@Service("ProjectService")
@Transactional
public class ProjectService {
	@Autowired
	private ProjectDAO projectDAO;
	
	public List<Project> getAllProjects() {
		return projectDAO.getAllProjects();
	}
	
	public List<Project> getProjectByVisibility(User user) {
		return projectDAO.getProjectsByVisibility(user);
	}
	
	public List<Project> getPublicProjects() {
		return projectDAO.getPublicProjects();
	}
	
	public Long addProject(Project project) {
		return projectDAO.addProject(project);
	}
	
	public void updateProject(Project project, Long idProject) {
		projectDAO.updateProject(project, idProject);
	}
	
	public Project getProjectById(Long idProject) {
		return projectDAO.getProjectById(idProject);
	}
	
	public void deleteProject(Long idProject) {
		projectDAO.deleteProject(idProject);
	}
	
	
}
