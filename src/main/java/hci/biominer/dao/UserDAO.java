package hci.biominer.dao;


import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.model.access.Role;
import hci.biominer.model.access.User;
import hci.biominer.model.access.Lab;

@Repository
public class UserDAO {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public void addUser(User user) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(user);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateUser(Long userIdx, User user) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		User userToUpdate = (User)session.get(User.class, userIdx);
		userToUpdate.setRoles(user.getRoles());
		userToUpdate.setEmail(user.getEmail());
		userToUpdate.setFirst(user.getFirst());
		userToUpdate.setLast(user.getLast());
		if (user.getPassword() != null) {
			userToUpdate.setPassword(user.getPassword());
			userToUpdate.setSalt(user.getSalt());
		}
		

		userToUpdate.setGuid(user.getGuid());
		userToUpdate.setGuidExpiration(user.getGuidExpiration());


		if (user.getisActive() != null) {
			userToUpdate.setisActive(user.getisActive());
		}
		
		userToUpdate.setPhone(user.getPhone());
		userToUpdate.setLabs(user.getLabs());
		userToUpdate.setInstitutes(user.getInstitutes());
		userToUpdate.setUsername(user.getUsername());
		session.update(userToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public User getUser(Long idUser) {
		Session session = this.getCurrentSession();
		User user = (User)session.get(User.class, idUser);
		
		
		Hibernate.initialize(user.getLabs());
		Hibernate.initialize(user.getInstitutes());
		Hibernate.initialize(user.getRoles());
		for (Role r: user.getRoles()) {
			Hibernate.initialize(r.getPermissions());
		}
		
		session.close();
		return user;
	}
	
	public void deleteUser(Long idUser) {
		Session session  = this.getCurrentSession();
		session.beginTransaction();
		User user = (User)session.get(User.class,idUser);
		session.delete(user);
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers() {
		Session session = this.getCurrentSession();
		
		List<User> users = session.createQuery("from User").list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLabs());
			Hibernate.initialize(u.getInstitutes());
			Hibernate.initialize(u.getRoles());
			for (Role r: u.getRoles()) {
				Hibernate.initialize(r.getPermissions());
			}
		}
		
		session.close();
		return users;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUserByLab(Long idLab) {
		Session session =  this.getCurrentSession();
		
		Query query = session.createQuery("from User u where :idLab in (select l.idLab from u.labs l)");
		query.setParameter("idLab", idLab);
		List<User> users = query.list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLabs());
			Hibernate.initialize(u.getInstitutes());
			Hibernate.initialize(u.getRoles());
			for (Role r: u.getRoles()) {
				Hibernate.initialize(r.getPermissions());
			}
		}
		
		session.close();
		return users;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getUsernames() {
		Session session = this.getCurrentSession();
		List<String> results = session.createCriteria(User.class)
			    .setProjection(Projections.distinct(Projections.property("username")))
			    .list();
		
		session.close();
		return results;
	}
	
	@SuppressWarnings("unchecked")
	public User getUserByUsername(String username) {
		System.out.println("starting method");
		Session session = this.getCurrentSession();
		
		Query query = session.createQuery("from User where username = :username");
		query.setParameter("username", username);
		List<User> users = query.list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLabs());
			Hibernate.initialize(u.getInstitutes());
			Hibernate.initialize(u.getRoles());
			for (Role r: u.getRoles()) {
				Hibernate.initialize(r.getPermissions());
			}
		}
		
		System.out.println("Size of users " + users.size());
		
		session.close();
		if (users.size() == 0) {
			return null;
		} else {
			return users.get(0);
		}
	}
	
}
