package hci.biominer.dao;

import hci.biominer.model.LiftoverChain;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LiftoverChainDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<LiftoverChain> getLiftoverChains() {
		Session session = getCurrentSession();
		List<LiftoverChain> chains = session.createQuery("from LiftoverChain").list();
		session.close();
		return chains;
	}
	
	public LiftoverChain getLiftoverChainById(Long idLiftoverChain) {
		Session session = getCurrentSession();
		LiftoverChain lc = (LiftoverChain)session.get(LiftoverChain.class, idLiftoverChain);
		session.close();
		return lc;
	}
	
	public void addLiftoverChain(LiftoverChain liftoverChain) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(liftoverChain);
		session.getTransaction().commit();
		session.close();
	}
	
	public void deleteLiftoverChain(Long idLiftoverChain) {
		Session session = getCurrentSession();
		session.beginTransaction();
		LiftoverChain lc = (LiftoverChain)session.get(LiftoverChain.class,idLiftoverChain);
		session.delete(lc);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
	
	
	
	
	
	
}
