package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.OrganismBuildDAO;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Organism;


@Service("OrganismBuildService")
@Transactional
public class OrganismBuildService {
	@Autowired
	private OrganismBuildDAO organismBuildDAO;
	
	public OrganismBuild getOrganismBuildById(Long idOrganismBuild) {
		return organismBuildDAO.getOrganismBuildById(idOrganismBuild);
	}
	
	public List<OrganismBuild> getOrganismBuildByOrganism(Organism organism) {
		return organismBuildDAO.getOrganismBuildByOrganism(organism);
	}
	
	public List<OrganismBuild> getAllOrganismBuilds() {
		return organismBuildDAO.getOrganismBuilds();
	}
	
	public void addOrganismBuild(OrganismBuild organismBuild) {
		organismBuildDAO.addOrganismBuild(organismBuild);		
	}

	public void deleteOrganismBuildById(Long idOrganismBuild) {
        organismBuildDAO.deleteOrganismBuild(idOrganismBuild);
	}

	public void updateOrganismBuild(Long idOrganismBuild, OrganismBuild organismBuild) {
        organismBuildDAO.updateOrganismBuild(idOrganismBuild, organismBuild);
	}
	
	public void updateGeneIdFile(Long idOrganismBuild, String GeneIdFile) {
		organismBuildDAO.updateGeneIdFile(idOrganismBuild, GeneIdFile);
	}
	
	public void updateGenomeFile(Long idOrganismBuild, String GenomeFile) {
		organismBuildDAO.updateGenomeFile(idOrganismBuild, GenomeFile);
	}
	
	public void updateTranscriptFile(Long idOrganismBuild, String TranscriptFile) {
		organismBuildDAO.updateTranscriptFile(idOrganismBuild, TranscriptFile);
	}
	
}
