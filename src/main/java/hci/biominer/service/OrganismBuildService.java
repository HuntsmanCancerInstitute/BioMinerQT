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
	private OrganismBuildDAO organismOrganismBuildDAO;
	
	public OrganismBuild getOrganismBuildById(Long idOrganismBuild) {
		return organismOrganismBuildDAO.getOrganismBuildById(idOrganismBuild);
	}
	
	public List<OrganismBuild> getOrganismBuildByOrganism(Organism organism) {
		return organismOrganismBuildDAO.getOrganismBuildByOrganism(organism);
	}
	
	public List<OrganismBuild> getAllOrganismBuilds() {
		return organismOrganismBuildDAO.getOrganismBuilds();
	}
	
	public void addOrganismBuild(OrganismBuild organismOrganismBuild) {
		organismOrganismBuildDAO.addOrganismBuild(organismOrganismBuild);		
		
	}

	public void deleteOrganismBuildById(Long idOrganismBuild) {
        organismOrganismBuildDAO.deleteOrganismBuild(idOrganismBuild);
	}

	public void updateOrganismBuild(Long idOrganismBuild, OrganismBuild organismOrganismBuild) {
        organismOrganismBuildDAO.updateOrganismBuild(idOrganismBuild, organismOrganismBuild);
	}
	
	
}
