package hci.biominer.service;

import java.util.List;

import hci.biominer.dao.GeneIdConversionDAO;
import hci.biominer.model.GeneIdConversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("GeneIdConversionService")
@Transactional
public class GeneIdConversionService {
	@Autowired
	private GeneIdConversionDAO geneIdConversionDAO;
	
	public GeneIdConversion getGeneIdConversionByID(Long idGeneIdConversion) {
		return geneIdConversionDAO.getGeneIdConversionsById(idGeneIdConversion);
	}
	
	public List<GeneIdConversion> getGeneIdConversions() {
		return geneIdConversionDAO.getGeneIdConversions();
	}
	
	public void deleteGeneIdConversion(Long idGeneIdConversion) {
		geneIdConversionDAO.deleteGeneIdConversion(idGeneIdConversion);
	}
	
	public void addGeneIdConversion(GeneIdConversion conversion) {
		geneIdConversionDAO.addGeneIdConversion(conversion);
	}
	
	public void updateGeneIdConversion(GeneIdConversion conversion, Long idGeneIdConversion) {
		geneIdConversionDAO.updateGeneIdConversion(idGeneIdConversion, conversion);
	}
	
}
