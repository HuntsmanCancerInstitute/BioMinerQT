package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.SampleSource;

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
	
	public void addSampleSource(SampleSource sampleSource) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sampleSource);
		session.getTransaction().commit();
		session.close();
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
}
