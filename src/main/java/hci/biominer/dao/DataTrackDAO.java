package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.DataTrack;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;
import hci.biominer.util.Enumerated.FileStateEnum;

import org.hibernate.Hibernate;
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
		for (DataTrack d: dataTracks) {
			Hibernate.initialize(d.isAnalysisSet());
		}
		session.close();
		return dataTracks;
	}
	
	public DataTrack getDataTrackById(Long idDataTrack) {
		Session session = getCurrentSession();
		DataTrack dataTrack = (DataTrack)session.get(DataTrack.class, idDataTrack);
		Hibernate.initialize(dataTrack.isAnalysisSet());
		session.close();
		return dataTrack;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTrackByProject(Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from DataTrack where project = :project");
		query.setParameter("project", project);
		List<DataTrack> dataTracks = query.list();
		for (DataTrack d: dataTracks) {
			Hibernate.initialize(d.isAnalysisSet());
		}
		session.close();
		return dataTracks;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTrackByAnalysis(Analysis analysis) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from DataTrack where analysis = :analysis");
		query.setParameter("analysis", analysis);
		List<DataTrack> dataTracks = query.list();
		for (DataTrack d: dataTracks) {
			Hibernate.initialize(d.isAnalysisSet());
		}
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
		DataTrackToUpdate.setPath(dataTrack.getPath());
		DataTrackToUpdate.setProject(dataTrack.getProject());
		DataTrackToUpdate.setState(dataTrack.getState());
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
	
	public void finalizeDataTrack(Long idDataTrack, FileStateEnum fs, String message) {
		Session session = getCurrentSession();
		session.beginTransaction();
		DataTrack DataTrackToUpdate = (DataTrack) session.get(DataTrack.class, idDataTrack);
		DataTrackToUpdate.setState(fs);
		DataTrackToUpdate.setMessage(message);
		session.update(DataTrackToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTrack> getDataTrackByName(Long idProject, List<String> nameList) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select dt from DataTrack as dt left join dt.project as p where p.idProject = :idProject and dt.name in (:nameList)");
		query.setParameter("idProject", idProject);
		query.setParameterList("nameList", nameList);
		List<DataTrack> dtList = query.list();
		session.close();
		return dtList;
	}
	
}
