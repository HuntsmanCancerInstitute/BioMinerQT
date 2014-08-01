package hci.biominer.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.RoleDAO;
import hci.biominer.model.access.Role;

@Service("RoleService")
@Transactional
public class RoleService {
	@Autowired
	private RoleDAO roleDAO;
	
	public Role getRoleByName(String roleName) {
		return roleDAO.getRoleByName(roleName);
	}
	

}
