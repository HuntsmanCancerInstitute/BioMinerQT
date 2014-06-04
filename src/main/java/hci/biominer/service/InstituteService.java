package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.InstituteDAO;
import hci.biominer.model.access.Institute;

@Service("InstituteService")
@Transactional
public class InstituteService {
	@Autowired
	private InstituteDAO instituteDAO;
	
	public List<Institute> getAllInstitutes() {
		return instituteDAO.getInstitutes();
	}
	
	public Institute getInstituteById(Long idx) {
		return instituteDAO.getInstitute(idx);
	}
	
}
