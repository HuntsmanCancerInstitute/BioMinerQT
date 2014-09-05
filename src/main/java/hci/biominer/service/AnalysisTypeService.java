package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.AnalysisTypeDAO;
import hci.biominer.model.AnalysisType;

@Service("AnalysisTypeService")
@Transactional
public class AnalysisTypeService {
	@Autowired
	private AnalysisTypeDAO analysisTypeDAO;
	
	public AnalysisType getAnalysisTypeById(Long idAnalysisType) {
		return analysisTypeDAO.getAnalysisTypeById(idAnalysisType);
	}

	
	public List<AnalysisType> getAllAnalysisTypes() {
		return analysisTypeDAO.getAnalysisTypes();
	}
	
	public void addAnalysisType(AnalysisType analysisType) {
		analysisTypeDAO.addAnalysisType(analysisType);		
		
	}

	public void deleteAnalysisTypeById(Long idAnalysisType) {
        analysisTypeDAO.deleteAnalysisType(idAnalysisType);
		
	}

	public void updateAnalysisType(Long idAnalysisType, AnalysisType analysisType) {
        analysisTypeDAO.updateAnalysisType(idAnalysisType, analysisType);
	}
	
	public AnalysisType getAnalysisTypeByName(String name) {
		return analysisTypeDAO.getAnalysisTypeByName(name);
	}
}
