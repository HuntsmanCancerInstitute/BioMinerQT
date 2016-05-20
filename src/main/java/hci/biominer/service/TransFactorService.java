package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.TransFactorDAO;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.TransFactor;

@Service("TransFactorService")
@Transactional
public class TransFactorService {
	@Autowired
	private TransFactorDAO transFactorDAO;
	
	public List<TransFactor> getAllTransFactors() {
		return transFactorDAO.getAllTfs();
	}
	
	public List<TransFactor> getTransFactorByGenomeBuild(OrganismBuild ob) {
		return transFactorDAO.getTfByBuild(ob);
	}
	
	public void deleteTransFactor(Long idTf) {
		transFactorDAO.deleteTf(idTf);
	}
	
	public void updateTransFactor(TransFactor tf, Long idTf) {
		transFactorDAO.updateTf(tf, idTf);
	}
	
	public void addTransFactor(TransFactor tf) {
		transFactorDAO.addTf(tf);
	}
	
	public TransFactor getTransFactorById(Long idTransFactor) {
		return transFactorDAO.getTfById(idTransFactor);
	}
	
	public boolean checkName(String filename) {
		return transFactorDAO.checkName(filename);
	}
}
