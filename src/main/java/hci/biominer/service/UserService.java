package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.UserDAO;
import hci.biominer.model.access.User;

@Service("UserService")
@Transactional
public class UserService {

	@Autowired
	private UserDAO userDAO;
	

	public User getUser(Long idUser) {
		return userDAO.getUser(idUser);
	}
	
	public List<User> getAllUsers() {
		return userDAO.getAllUsers();
	}
	
	public List<User> getUsersByLab(Long idLab) {
		return userDAO.getUserByLab(idLab);
	}
	
	public List<String> getUsernames() {
		return userDAO.getUsernames();
	}
	
	public void addUser(User user) {
		userDAO.addUser(user);
	}
	
	public void deleteUser(Long idUser) {
		userDAO.deleteUser(idUser);
	}
	
	public void updateUser(Long idUser, User user) {
		userDAO.updateUser(idUser, user);
	}
	
	public User getUserByUsername(String username) {
		return userDAO.getUserByUsername(username);
	}

}
