package hci.biominer.dao;


import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
		userToUpdate.setAdmin(user.getAdmin());
		userToUpdate.setEmail(user.getEmail());
		userToUpdate.setFirst(user.getFirst());
		userToUpdate.setLast(user.getLast());
		if (user.getPassword() != null) {
			userToUpdate.setPassword(user.getPassword());
			userToUpdate.setSalt(user.getSalt());
		}
		
		userToUpdate.setPhone(user.getPhone());
		userToUpdate.setLab(user.getLab());
		userToUpdate.setUsername(user.getUsername());
		session.update(userToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public User getUser(Long id) {
		Session session = this.getCurrentSession();
		User user = (User)session.get(User.class, id);
		
		
		Hibernate.initialize(user.getLab());
		for (Lab l: user.getLab()) {
			Hibernate.initialize(l.getInstitutes());
		}
		
		session.close();
		return user;
	}
	
	public void deleteUser(Long id) {
		Session session  = this.getCurrentSession();
		session.beginTransaction();
		User user = (User)session.get(User.class,id);
		session.delete(user);
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers() {
		Session session = this.getCurrentSession();
		
		List<User> users = session.createQuery("from User").list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLab());
			for (Lab l: u.getLab()) {
				Hibernate.initialize(l.getInstitutes());
			}
		}
		
		session.close();
		return users;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getUserByLab(Long id) {
		Session session =  this.getCurrentSession();
		
		Query query = session.createQuery("from User u where :labIdx in (select l.idx from u.labs l)");
		query.setParameter("labIdx", id);
		List<User> users = query.list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLab());
			for (Lab l: u.getLab()) {
				Hibernate.initialize(l.getInstitutes());
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
		Session session = this.getCurrentSession();
		
		Query query = session.createQuery("from User where username = :username");
		query.setParameter("username", username);
		List<User> users = query.list();
		
		for (User u: users) {
			Hibernate.initialize(u.getLab());
			for (Lab l: u.getLab()) {
				Hibernate.initialize(l.getInstitutes());
			}
		}
		
		session.close();
		if (users.size() == 0) {
			return null;
		} else {
			return users.get(0);
		}
	}
	
}