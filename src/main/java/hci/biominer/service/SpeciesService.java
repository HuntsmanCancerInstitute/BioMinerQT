package hci.biominer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SpeciesDAO;
import hci.biominer.model.Species;


@Service("SpeciesService")
@Transactional
public class SpeciesService {
    
	@Autowired
	private SpeciesDAO speciesDAO;
	
    private static List<Species> rsList = new ArrayList<Species>();
    private static Long id = 0L;

    public Species getSpeciesById(Long id) {
    	return speciesDAO.getSpecies(id);
    }

    private Species findSpeciesById(Long id) {
       return speciesDAO.getSpecies(id);
    }

	public List<Species> getAllSpecies() {		
		return speciesDAO.getAllSpecies();
	}


	public void addSpecies(Species species) {
		speciesDAO.addSpecies(species);		
		
	}

	public void deleteSpeciesById(Long id) {
        speciesDAO.deleteSpecies(id);
		
	}

	public void updateSpecies(Species species) {
        speciesDAO.updateSpecies(species);
	}

    public void deleteAll() {
        
    }
}
