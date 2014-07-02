package hci.biominer.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;

@Repository
public class LabDAO  {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public void addLab(Lab lab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(lab);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateLab(Lab lab, Long idLab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		Lab labToUpdate = (Lab) session.get(Lab.class, idLab);
		labToUpdate.setFirst(lab.getFirst());
		labToUpdate.setLast(lab.getLast());
		labToUpdate.setInstitutes(lab.getInstitutes());
		session.update(labToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public Lab getLab(Long idLab) {
		Session session = this.getCurrentSession();
		Lab lab  = (Lab)session.get(Lab.class, idLab);
		Hibernate.initialize(lab.getInstitutes());
		session.close();
		return lab;
	}
	
	public void deleteLab(Long idLab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		Lab lab = (Lab)session.get(Lab.class, idLab);
		if (lab != null) {
			session.delete(lab);
		}
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<Lab> getAllLabs() {
		Session session  = this.getCurrentSession();
		List<Lab> lab = session.createQuery("from Lab").list();
		for (Lab l: lab) {
			Hibernate.initialize(l.getInstitutes());
		}
		session.close();
		return lab;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers(Lab lab) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("from User where lab = :lab");
		query.setParameter("lab", lab);
		List<User> user = query.list();
		session.close();
		return user;
	}

}
