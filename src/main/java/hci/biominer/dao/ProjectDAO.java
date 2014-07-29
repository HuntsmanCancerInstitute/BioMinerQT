package hci.biominer.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import hci.biominer.model.DataTrack;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;
import hci.biominer.model.Sample;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.util.Enumerated.ProjectVisibilityEnum;;

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
		List<Project> initProjects = initializeProjects(projects);
		session.close();
		return initProjects;
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getProjectsByVisibility(User user) {
		List<Long> labList = new ArrayList<Long>();
		List<Long> instituteList = new ArrayList<Long>();
		HashSet<Long> instituteSet = new HashSet<Long>();
		
		
		for (Lab l: user.getLabs()) {
			labList.add(l.getIdLab());
			for (Institute i: l.getInstitutes()) {
				instituteList.add(i.getIdInstitute());
			}
		}
		instituteList.addAll(instituteSet);
		
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select distinct p from Project as p "
				+ "left join fetch p.labs as l "
				+ "left join fetch l.institutes as i " 
				+ "where (l.idLab in (:userLabs) and p.visibility = :vis1) or "
				+ "(i.idInstitute in (:userInstitute) and p.visibility = :vis2) or "
				+ "(p.visibility = :vis3)");
		query.setParameterList("userLabs", labList);
		query.setParameterList("userInstitute", instituteList);
		query.setParameter("vis1",ProjectVisibilityEnum.LAB);
		query.setParameter("vis2",ProjectVisibilityEnum.INSTITUTE);
		query.setParameter("vis3", ProjectVisibilityEnum.PUBLIC);
		List<Project> projects = query.list();
		List<Project> initProjects = initializeProjects(projects);
		session.close();
		return initProjects;
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getPublicProjects() {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select p from Project as p where p.visibility = :visibility");
		query.setParameter("visibility", ProjectVisibilityEnum.PUBLIC);
		List<Project> projects = query.list();
		List<Project> initProjects = initializeProjects(projects);
		session.close();
		return initProjects;
	}
	
	public List<Project> initializeProjects(List<Project> projects) {
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
			
			for (Sample s: p.getSamples()) {
				Hibernate.initialize(s.isAnalysisSet());
			}
			
			for (DataTrack d: p.getDataTracks()) {
				Hibernate.initialize(d.isAnalysisSet());
			}
		}
		
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
		
		for (Sample s: project.getSamples()) {
			Hibernate.initialize(s.isAnalysisSet());
		}
		
		for (DataTrack d: project.getDataTracks()) {
			Hibernate.initialize(d.isAnalysisSet());
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
