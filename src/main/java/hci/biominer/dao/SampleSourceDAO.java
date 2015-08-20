package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.SampleCondition;
import hci.biominer.model.SamplePrep;
import hci.biominer.model.SampleSource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SampleSourceDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleSource> getSampleSources() {
		Session session = this.getCurrentSession();
		List<SampleSource> sampleSources = session.createQuery("from SampleSource").list();
		session.close();
		return sampleSources;
	}
	
	public SampleSource getSampleSourceById(Long idSampleSource) {
		Session session = getCurrentSession();
		SampleSource sampleSource = (SampleSource)session.get(SampleSource.class, idSampleSource);
		session.close();
		return sampleSource;
	}
	
	@SuppressWarnings("unchecked")
	public SampleSource getSampleSourceBySource(String source, Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from SampleSource where source = :source and idOrganismBuild = :idOrganismBuild");
		query.setParameter("source", source);
		query.setParameter("idOrganismBuild", idOrganismBuild);
		List<SampleSource> sampleSource = query.list();
		session.close();
		if (sampleSource.isEmpty()) {
			return null;
		} else {
			return sampleSource.get(0);
		}
	}
	
	public Long addSampleSource(SampleSource sampleSource) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sampleSource);
		session.getTransaction().commit();
		session.close();
		return sampleSource.getIdSampleSource();
	}
	
	public void updateSampleSource(Long idSampleSource, SampleSource sampleSource) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleSource SampleSourceToUpdate = (SampleSource) session.get(SampleSource.class, idSampleSource);
		SampleSourceToUpdate.setSource(sampleSource.getSource());
		session.update(SampleSourceToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteSampleSource(Long idSampleSource) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleSource SampleSource = (SampleSource) session.get(SampleSource.class, idSampleSource);
		if (SampleSource != null) 
			session.delete(SampleSource);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
	public void deleteSampleSources(List<Long> sampleSourceIdList) {
		for (Long id: sampleSourceIdList) {
			deleteSampleSource(id);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleSource> getUnusedSampleSources(Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select ss from Sample s right outer join s.sampleSource ss where s.idSample is null and idOrganismBuild = :idOrganismBuild");
		query.setParameter("idOrganismBuild", idOrganismBuild);
		List<SampleSource> sampleSources = query.list();
		session.close();
		return sampleSources; 
	}
	
}
