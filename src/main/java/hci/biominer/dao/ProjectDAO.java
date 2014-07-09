package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.Project;
import hci.biominer.model.Analysis;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Institute;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getAllProjects() {
		Session session = this.getCurrentSession();
		List<Project> projects = session.createQuery("from Project").list();
		
		for (Project p: projects) {
			Hibernate.initialize(p.getSamples());
			Hibernate.initialize(p.getDataTracks());
			Hibernate.initialize(p.getFiles());
			Hibernate.initialize(p.getAnalyses());
			Hibernate.initialize(p.getLabs());
			for (Lab l: p.getLabs()) {
				Hibernate.initialize(l.getInstitutes());
			}
			
			for (Analysis a: p.getAnalyses()) {
				Hibernate.initialize(a.getSamples());
				Hibernate.initialize(a.getFile());
				Hibernate.initialize(a.getDataTracks());
			}
		}
		
		session.close();
		return projects;
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getProjectsByVisibility(User user) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select p from Project p left join p.labs l where l.idLab in (:userLabs)");
		query.setParameter("userLabs", user.getLabs());
		List<Project> projects = query.list();
		session.close();
		return projects;
	}
	
	public Long addProject(Project project) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(project);
		session.getTransaction().commit();
		session.close();
		return project.getIdProject();
		
	}
	
	public void updateProject(Project project, Long idProject) {
		Project projectToUpdate = this.getProjectById(idProject);
		Session session = getCurrentSession();
		session.beginTransaction();
		projectToUpdate.setName(project.getName());
		projectToUpdate.setDescription(project.getDescription());
		projectToUpdate.setOrganismBuild(project.getOrganismBuild());
		projectToUpdate.setVisibility(project.getVisibility());
		projectToUpdate.setLabs(project.getLabs());
		session.update(projectToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public Project getProjectById(Long idProject) {
		Session session = getCurrentSession();
		Project project = (Project)session.get(Project.class,idProject);
		
		Hibernate.initialize(project.getSamples());
		Hibernate.initialize(project.getDataTracks());
		Hibernate.initialize(project.getFiles());
		Hibernate.initialize(project.getAnalyses());
		Hibernate.initialize(project.getLabs());
		for (Lab l: project.getLabs()) {
			Hibernate.initialize(l.getInstitutes());
		}
		
		for (Analysis a: project.getAnalyses()) {
			Hibernate.initialize(a.getSamples());
			Hibernate.initialize(a.getFile());
			Hibernate.initialize(a.getDataTracks());
		}
		
		session.close();
		return project;
	}
	
	public void deleteProject(Long idProject) {
		Project project = this.getProjectById(idProject);
		Session session = getCurrentSession();
		session.beginTransaction();
		session.delete(project);
		session.getTransaction().commit();
		session.close();
		
	}
	
	
}
