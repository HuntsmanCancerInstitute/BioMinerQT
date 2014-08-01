package hci.biominer.dao;


import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.model.access.Role;


@Repository
public class RoleDAO {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public Role getRoleByName(String name) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("from Role where name =:name");
		query.setParameter("name", name);
		List<Role> roles = query.list();
		session.close();
		
		if (roles.size() == 0) {
			return null;
		} else {
			return roles.get(0);
		}
	}
}