package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.SampleCondition;
import hci.biominer.model.SamplePrep;

import org.hibernate.Query;
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
	
	@SuppressWarnings("unchecked")
	public SampleCondition getSampleConditionByCondition(String condition, Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from SampleCondition where cond = :condition and idOrganismBuild = :idOrganismBuild");
		query.setParameter("condition", condition);
		query.setParameter("idOrganismBuild", idOrganismBuild);
		List<SampleCondition> sampleConditions = query.list();
		session.close();
		if (sampleConditions.size() == 0) {
			return null;
		} else {
			return sampleConditions.get(0);
		}
	}
	
	public Long addSampleCondition(SampleCondition sampleCondition) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sampleCondition);
		session.getTransaction().commit();
		session.close();
		return sampleCondition.getIdSampleCondition();
		
	}
	
	public void deleteSampleConditions(List<Long> sampleConditionIdList) {
		for (Long id: sampleConditionIdList) {
			deleteSampleCondition(id);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleCondition> getUnusedSampleConditions(Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select sc from Sample s right outer join s.sampleCondition sc where s.idSample is null and idOrganismBuild = :idOrganismBuild");
		query.setParameter("idOrganismBuild", idOrganismBuild);
		List<SampleCondition> sampleConditions = query.list();
		session.close();
		return sampleConditions;
		
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
