package hci.biominer.service;

import java.util.List;

import hci.biominer.dao.BiominerGeneDAO;
import hci.biominer.model.BiominerGene;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("BiominerGeneService")
@Transactional
public class BiominerGeneService {
	@Autowired
	private BiominerGeneDAO biominerGeneDAO;
	
	public List<BiominerGene> getBiominerGenes() {
		return this.biominerGeneDAO.getBiominerGenes();
	}
	
	public void deleteBiominerGenes() throws Exception {
		this.biominerGeneDAO.deleteBiominerGenes();
	}
	
	public void addBiominerGene(BiominerGene bg) {
		this.biominerGeneDAO.addBiominerGene(bg);
	}

	public void addBiominerGenes(List<BiominerGene> bgList) throws Exception {
		this.biominerGeneDAO.addBiominerGenes(bgList);
	}
}
