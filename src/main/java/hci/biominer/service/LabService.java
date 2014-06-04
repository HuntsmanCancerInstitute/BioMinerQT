package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.LabDAO;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;



@Service("LabService")
@Transactional
public class LabService {
	
	@Autowired
	private LabDAO labDAO;
	
	public void addLab(Lab lab) {
		this.labDAO.addLab(lab);
	}
	
	public void updateLab(Lab lab, Long id) {
		this.labDAO.updateLab(lab,id);
	}
	
	public void deleteLab(Long id) {
		this.labDAO.deleteLab(id);
	}
	
	public Lab getLab(Long id) {
		return this.labDAO.getLab(id);
	}
	
	public List<Lab> getAllLabs() {
		return this.labDAO.getAllLabs();
	}
	
	public List<User> getAllUsersInLab(Lab lab) {
		return this.labDAO.getAllUsers(lab);
	}
	

}
