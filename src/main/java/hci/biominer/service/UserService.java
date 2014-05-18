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
	

	public User getUser(Long id) {
		return userDAO.getUser(id);
	}
	
	public List<User> getAllUsers() {
		return userDAO.getAllUsers();
	}
	
	public List<User> getUsersByLab(Long id) {
		return userDAO.getUserByLab(id);
	}
	
	public List<String> getUsernames() {
		return userDAO.getUsernames();
	}
	
	public void addUser(User user) {
		userDAO.addUser(user);
	}
	
	public void deleteUser(Long id) {
		userDAO.deleteUser(id);
	}
	
	public void updateUser(Long userIdx, User user) {
		userDAO.updateUser(userIdx, user);
	}
	
	public User getUserByUsername(String username) {
		return userDAO.getUserByUsername(username);
	}

}
