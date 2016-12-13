package hci.biominer.service;

import java.util.List;

import hci.biominer.dao.LiftoverChainDAO;
import hci.biominer.model.LiftoverChain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("LiftoverChainService")
@Transactional
public class LiftoverChainService {
	@Autowired
	private LiftoverChainDAO liftoverChainDAO;
	
	public LiftoverChain getLiftoverChainByID(long idLiftoverChain) {
		return liftoverChainDAO.getLiftoverChainById(idLiftoverChain);
	}
	
	public List<LiftoverChain> getLiftoverChains() {
		return liftoverChainDAO.getLiftoverChains();
	}
	
	public void deleteLiftoverChain(Long idLiftoverChain) {
		liftoverChainDAO.deleteLiftoverChain(idLiftoverChain);
	}
	
	public void addLiftoverChain(LiftoverChain liftChain) {
		liftoverChainDAO.addLiftoverChain(liftChain);
	}
}
