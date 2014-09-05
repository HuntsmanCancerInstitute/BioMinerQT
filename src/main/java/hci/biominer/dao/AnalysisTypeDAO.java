package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.AnalysisType;
import hci.biominer.model.Project;
import hci.biominer.util.Enumerated.ProjectVisibilityEnum;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class AnalysisTypeDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<AnalysisType> getAnalysisTypes() {
		Session session = this.getCurrentSession();
		List<AnalysisType> analysisTypes = session.createQuery("from AnalysisType").list();
		session.close();
		return analysisTypes;
	}
	
	
	public AnalysisType getAnalysisTypeById(Long idAnalysisType) {
		Session session = getCurrentSession();
		AnalysisType analysisType = (AnalysisType)session.get(AnalysisType.class, idAnalysisType);
		session.close();
		return analysisType;
	}
	
	public void addAnalysisType(AnalysisType analysisType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(analysisType);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateAnalysisType(Long idAnalysisType, AnalysisType analysisType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		AnalysisType AnalysisTypeToUpdate = (AnalysisType) session.get(AnalysisType.class, idAnalysisType);
		AnalysisTypeToUpdate.setType(analysisType.getType());
		session.update(AnalysisTypeToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteAnalysisType(Long idAnalysisType) {
		Session session = getCurrentSession();
		session.beginTransaction();
		AnalysisType AnalysisType = (AnalysisType) session.get(AnalysisType.class, idAnalysisType);
		if (AnalysisType != null) 
			session.delete(AnalysisType);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public AnalysisType getAnalysisTypeByName(String name) {
		Session session  = getCurrentSession();
		session.beginTransaction();
		
		Query query = session.createQuery("select at from AnalysisType at where at.type = :name");
		query.setParameter("name",name);
		List<AnalysisType> analysisTypes = query.list();
		session.close();
		if (analysisTypes.size() > 0) {
			return analysisTypes.get(0);
		} else {
			return null;
		}
		
	}

}
