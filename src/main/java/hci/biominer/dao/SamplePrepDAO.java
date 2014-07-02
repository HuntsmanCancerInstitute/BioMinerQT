package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.SamplePrep;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SamplePrepDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<SamplePrep> getSamplePreps() {
		Session session = this.getCurrentSession();
		List<SamplePrep> samplePreps = session.createQuery("from SamplePrep").list();
		session.close();
		return samplePreps;
	}
	
	@SuppressWarnings("unchecked")
	public List<SamplePrep> getSamplePrepBySampleType(Long idSampleType) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("from SamplePrep where idSampleType = :idSampleType");
		query.setParameter("idSampleType", idSampleType);
		List<SamplePrep> samplePreps = query.list();
		session.close();
		return samplePreps;
	}
	
	public SamplePrep getSamplePrepById(Long idSamplePrep) {
		Session session = getCurrentSession();
		SamplePrep samplePrep = (SamplePrep)session.get(SamplePrep.class, idSamplePrep);
		session.close();
		return samplePrep;
	}
	
	public void addSamplePrep(SamplePrep samplePrep) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(samplePrep);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateSamplePrep(Long idSamplePrep, SamplePrep samplePrep) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SamplePrep SamplePrepToUpdate = (SamplePrep) session.get(SamplePrep.class, idSamplePrep);
		SamplePrepToUpdate.setDescription(samplePrep.getDescription());
		SamplePrepToUpdate.setSampleType(samplePrep.getSampleType());
		session.update(SamplePrepToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteSamplePrep(Long idSamplePrep) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SamplePrep SamplePrep = (SamplePrep) session.get(SamplePrep.class, idSamplePrep);
		if (SamplePrep != null) 
			session.delete(SamplePrep);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
}
