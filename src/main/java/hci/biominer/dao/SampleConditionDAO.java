package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.SampleCondition;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SampleConditionDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleCondition> getSampleConditions() {
		Session session = this.getCurrentSession();
		List<SampleCondition> sampleConditions = session.createQuery("from SampleCondition").list();
		session.close();
		return sampleConditions;
	}

	
	public SampleCondition getSampleConditionById(Long idSampleCondition) {
		Session session = getCurrentSession();
		SampleCondition sampleCondition = (SampleCondition)session.get(SampleCondition.class, idSampleCondition);
		session.close();
		return sampleCondition;
	}
	
	public void addSampleCondition(SampleCondition sampleCondition) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sampleCondition);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateSampleCondition(Long idSampleCondition, SampleCondition sampleCondition) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleCondition SampleConditionToUpdate = (SampleCondition) session.get(SampleCondition.class, idSampleCondition);
		SampleConditionToUpdate.setCond(sampleCondition.getCond());
		session.update(SampleConditionToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteSampleCondition(Long idSampleCondition) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleCondition SampleCondition = (SampleCondition) session.get(SampleCondition.class, idSampleCondition);
		if (SampleCondition != null) 
			session.delete(SampleCondition);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
}
