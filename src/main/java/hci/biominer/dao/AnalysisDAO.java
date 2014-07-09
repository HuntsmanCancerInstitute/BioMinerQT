package hci.biominer.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import hci.biominer.model.Analysis;
import hci.biominer.model.Project;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis>  getAllAnalysis() {
		Session session = this.getCurrentSession();
		List<Analysis> analyses = session.createQuery("from Analysis").list();
		
		for (Analysis a: analyses) {
			Hibernate.initialize(a.getFile());
			Hibernate.initialize(a.getDataTracks());
			Hibernate.initialize(a.getSamples());
		}
		
		session.close();
		return analyses;
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getProjectsByProject(Project project) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select a from Analysis a where a.project = :project");
		query.setParameter("project",project);
		List<Analysis> analyses = query.list();
		
		for (Analysis a: analyses) {
			Hibernate.initialize(a.getFile());
			Hibernate.initialize(a.getDataTracks());
			Hibernate.initialize(a.getSamples());
		}
		
		session.close();
		return analyses;
	}
	
	public Long addAnalysis(Analysis analysis) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(analysis);
		session.getTransaction().commit();
		session.close();
		return analysis.getIdAnalysis();
	}
	
	public void updateAnalysis(Analysis analysis, Long idAnalysis) {
	    Analysis analysisToUpdate = this.getAnalysisById(idAnalysis);
	    Session session = getCurrentSession();
	    session.beginTransaction();
	    analysisToUpdate.setName(analysis.getName());
	    analysisToUpdate.setDescription(analysis.getDescription());
	    analysisToUpdate.setDate(analysis.getDate());
	    analysisToUpdate.setProject(analysis.getProject());
	    analysisToUpdate.setFile(analysis.getFile());
	    analysisToUpdate.setSamples(analysis.getSamples());
	    analysisToUpdate.setDataTracks(analysis.getDataTracks());
	    session.update(analysisToUpdate);
	    session.getTransaction().commit();
	    session.close();
	}
	
	public Analysis getAnalysisById(Long idAnalysis) {
		Session session = getCurrentSession();
		Analysis analysis = (Analysis)session.get(Analysis.class, idAnalysis);
		
		Hibernate.initialize(analysis.getFile());
		Hibernate.initialize(analysis.getDataTracks());
		Hibernate.initialize(analysis.getSamples());
		
		session.close();
		return analysis;
	}
	
	public void deleteAnalysis(Long idAnalysis) {
		Analysis analysis = this.getAnalysisById(idAnalysis);
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.delete(analysis);
		session.getTransaction().commit();
		session.close();
	}
	
	
}
