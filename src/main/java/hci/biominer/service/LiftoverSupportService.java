package hci.biominer.service;

import java.util.List;

import hci.biominer.dao.LiftoverSupportDAO;
import hci.biominer.model.LiftoverSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("LiftoverSupportService")
@Transactional
public class LiftoverSupportService {
	@Autowired
	private LiftoverSupportDAO liftoverSupportDAO;
	
	public LiftoverSupport getLiftoverSupportByID(Long idLiftoverSupport) {
		return liftoverSupportDAO.getLiftoverSupportById(idLiftoverSupport);
	}
	
	public List<LiftoverSupport> getLiftoverSupports() {
		return liftoverSupportDAO.getLiftoverSupports();
	}
	
	public void deleteLiftoverSupport(Long idLiftoverSupport) {
		liftoverSupportDAO.deleteLiftoverSupport(idLiftoverSupport);
	}
	
	public void addLiftoverSupport(LiftoverSupport ls) {
		liftoverSupportDAO.addLiftoverSupport(ls);
	}
}
