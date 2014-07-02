package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.OrganismDAO;
import hci.biominer.model.Organism;


@Service("OrganismService")
@Transactional
public class OrganismService {
	@Autowired
	private OrganismDAO organismDAO;
	
	public Organism getOrganism(Long idOrganism) {
		return organismDAO.getOrganismById(idOrganism);
	}
	
	public List<Organism> getAllOrganisms() {
		return organismDAO.getOrganisms();
	}
	
	
	public void addOrganism(Organism organism) {
		organismDAO.addOrganism(organism);		
		
	}

	public void deleteOrganismById(Long idOrganism) {
        organismDAO.deleteOrganism(idOrganism);
		
	}

	public void updateOrganism(Long idOrganism, Organism organism) {
        organismDAO.updateOrganism(idOrganism, organism);
	}
	
	
	
}
