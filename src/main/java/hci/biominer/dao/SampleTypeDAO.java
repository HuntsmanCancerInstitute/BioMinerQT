package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.Sample;
import hci.biominer.model.SampleType;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SampleTypeDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleType> getSampleTypes() {
		Session session = this.getCurrentSession();
		List<SampleType> sampleTypes = session.createQuery("from SampleType").list();
		session.close();
		return sampleTypes;
	}
	
	public SampleType getSampleTypeById(Long idSampleType) {
		Session session = getCurrentSession();
		SampleType sampleType = (SampleType)session.get(SampleType.class, idSampleType);
		session.close();
		return sampleType;
	}
	
	@SuppressWarnings("unchecked")
	public SampleType getSampleTypeByType(String type) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from SampleType as st where st.type = :type");
		query.setParameter("type", type);
		List<SampleType> sampleTypes = query.list();
		session.close();
		if (sampleTypes.isEmpty()) {
			return null;
		} else {
			return sampleTypes.get(0);
		}
	}
	
	public void addSampleType(SampleType sampleType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sampleType);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateSampleType(Long idSampleType, SampleType sampleType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleType SampleTypeToUpdate = (SampleType) session.get(SampleType.class, idSampleType);
		SampleTypeToUpdate.setType(sampleType.getType());
		session.update(SampleTypeToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteSampleType(Long idSampleType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		SampleType SampleType = (SampleType) session.get(SampleType.class, idSampleType);
		if (SampleType != null) 
			session.delete(SampleType);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
}
