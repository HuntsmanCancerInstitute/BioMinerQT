package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.DataTrack;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DataTrackDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTracks() {
		Session session = this.getCurrentSession();
		List<DataTrack> dataTracks = session.createQuery("from DataTrack").list();
		session.close();
		return dataTracks;
	}
	
	public DataTrack getDataTrackById(Long idDataTrack) {
		Session session = getCurrentSession();
		DataTrack dataTrack = (DataTrack)session.get(DataTrack.class, idDataTrack);
		session.close();
		return dataTrack;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTrackByProject(Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from DataTrack where project = :project");
		query.setParameter("project", project);
		List<DataTrack> dataTracks = query.list();
		session.close();
		return dataTracks;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTrackByAnalysis(Analysis analysis) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from DataTrack where analysis = :analysis");
		query.setParameter("analysis", analysis);
		List<DataTrack> dataTracks = query.list();
		session.close();
		return dataTracks;
	}
	
	public void addDataTrack(DataTrack dataTrack) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(dataTrack);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateDataTrack(Long idDataTrack, DataTrack dataTrack) {
		Session session = getCurrentSession();
		session.beginTransaction();
		DataTrack DataTrackToUpdate = (DataTrack) session.get(DataTrack.class, idDataTrack);
		DataTrackToUpdate.setName(dataTrack.getName());
		DataTrackToUpdate.setUrl(dataTrack.getUrl());
		DataTrackToUpdate.setProject(dataTrack.getProject());
		DataTrackToUpdate.setAnalysis(dataTrack.getAnalysis());
		session.update(DataTrackToUpdate);
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteDataTrack(Long idDataTrack) {
		Session session = getCurrentSession();
		session.beginTransaction();
		DataTrack dataTrack = (DataTrack) session.get(DataTrack.class, idDataTrack);
		session.delete(dataTrack);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateDataTrackAnalysis(Long idDataTrack, Analysis analysis) {
		Session session = getCurrentSession();
		session.beginTransaction();
		DataTrack dataTrack = (DataTrack) session.get(DataTrack.class, idDataTrack);
		dataTrack.setAnalysis(analysis);
		session.update(dataTrack);
		session.getTransaction().commit();
		session.close();
	}
	
}
