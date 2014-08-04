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
	
	public void updateLab(Lab lab, Long idLab) {
		this.labDAO.updateLab(lab,idLab);
	}
	
	public void deleteLab(Long idLab) {
		this.labDAO.deleteLab(idLab);
	}
	
	public Lab getLab(Long idLab) {
		return this.labDAO.getLab(idLab);
	}
	
	public List<Lab> getAllLabs() {
		return this.labDAO.getAllLabs();
	}
	
	public List<User> getAllUsersInLab(Lab lab) {
		return this.labDAO.getAllUsers(lab);
	}
	
	public List<Lab> getQueryLabsPublic() {
		return this.labDAO.getQueryLabsPublic();
	}
	
	public List<Lab> getQueryLabsByVisibility(User user) {
		return this.labDAO.getQueryLabsByVisibility(user);
	}
	

}
