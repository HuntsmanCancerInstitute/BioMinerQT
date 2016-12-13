package hci.biominer.dao;

import hci.biominer.model.LiftoverSupport;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class LiftoverSupportDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<LiftoverSupport> getLiftoverSupports() {
		Session session = getCurrentSession();
		List<LiftoverSupport> supports = session.createQuery("from LiftoverSupport").list();
		session.close();
		return supports;
	}
	
	public LiftoverSupport getLiftoverSupportById(Long idLiftoverSupport) {
		Session session = getCurrentSession();
		LiftoverSupport ls = (LiftoverSupport)session.get(LiftoverSupport.class, idLiftoverSupport);
		session.close();
		return ls;
	}
	
	public void addLiftoverSupport(LiftoverSupport ls) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(ls);
		session.getTransaction().commit();
		session.close();
	}
	
	public void deleteLiftoverSupport(Long idLiftoverSupport) {
		Session session = getCurrentSession();
		session.beginTransaction();
		LiftoverSupport ls = (LiftoverSupport)session.get(LiftoverSupport.class,idLiftoverSupport);
		session.delete(ls);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
}
