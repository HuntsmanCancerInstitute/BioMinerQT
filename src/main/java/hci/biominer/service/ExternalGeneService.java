package hci.biominer.service;

import java.util.List;

import hci.biominer.dao.ExternalGeneDAO;
import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ExternalGeneService")
@Transactional
public class ExternalGeneService {
	@Autowired
	private ExternalGeneDAO externalGeneDAO;
	
	public List<ExternalGene> getBiominerIdByExternalName(String name, Long obId) {
		return externalGeneDAO.getBiominerIdByExternalName(name, obId);
	}
	
	public List<ExternalGene> getExternalGeneByBiominerId(List<Long> biominerIds, String source, Long obId) {
		return externalGeneDAO.getExternalGeneByBiominerId(biominerIds, source, obId);
	}
	
	public void deleteExternalGenesByOrganismId(Long obId) throws Exception{
		externalGeneDAO.deleteExternalGenesByOrganismId(obId);
	}
	
	public void addExternalGenes(List<ExternalGene> genes) throws Exception{
		externalGeneDAO.addExternalGenes(genes);
	}
	
	public List<ExternalGene> getExternalGenesByOrganismBuild(OrganismBuild ob) {
		return externalGeneDAO.getExternalGenesByOrganismBuild(ob);
	}
	
	public List<ExternalGene> getHugoNamesGenesByOrganismBuild(OrganismBuild ob) {
		return externalGeneDAO.getHugoNamesGenesByOrganismBuild(ob);
	}
	
	public List<ExternalGene> getEnsemblNamesById(Long idExternalGene, String source, Long obId) {
		return externalGeneDAO.getEnsemblNamesById(idExternalGene, source, obId);
	}

}
