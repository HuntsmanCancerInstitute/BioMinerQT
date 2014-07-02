package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.access.Institute;
import hci.biominer.model.access.User;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InstituteDAO {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Institute> getInstitutes() {
		Session session = this.getCurrentSession();
		List<Institute> institutes = session.createQuery("from Institute").list();
		session.close();
		return institutes;
		
	}
	
	public Institute getInstitute(Long idInstitute) {
		Session session = this.getCurrentSession();
		Institute institute = (Institute)session.get(Institute.class,idInstitute);
		session.close();
		return institute;
	}
	
	

}
