package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.GenotypeDAO;
import hci.biominer.model.Genotype;


@Service("GenotypeService")
@Transactional
public class GenotypeService {
	@Autowired
	private GenotypeDAO genotypeDAO;
	
	public Genotype getGenotype(Long idGenotype) {
		return genotypeDAO.getGenotypeById(idGenotype);
	}
	
	public List<Genotype> getAllGenotypes() {
		return genotypeDAO.getGenotypes();
	}
	
	
	public void addGenotype(Genotype genotype) {
		genotypeDAO.addGenotype(genotype);		
		
	}

	public void deleteGenotypeById(Long idGenotype) {
        genotypeDAO.deleteGenotype(idGenotype);
		
	}

	public void updateGenotype(Long idGenotype, Genotype genotype) {
        genotypeDAO.updateGenotype(idGenotype, genotype);
	}
	
	
	
}
