package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SampleConditionDAO;
import hci.biominer.model.SampleCondition;

@Service("SampleCondition")
@Transactional
public class SampleConditionService {
	@Autowired
	private SampleConditionDAO sampleConditionDAO;
	
	public SampleCondition getSampleConditionById(Long idSampleCondition) {
		return sampleConditionDAO.getSampleConditionById(idSampleCondition);
	}
	
	public List<SampleCondition> getAllSampleConditions() {
		return sampleConditionDAO.getSampleConditions();
	}
	
	public void addSampleCondition(SampleCondition sampleCondition) {
		sampleConditionDAO.addSampleCondition(sampleCondition);		
		
	}

	public void deleteSampleConditionById(Long idSampleCondition) {
        sampleConditionDAO.deleteSampleCondition(idSampleCondition);
		
	}

	public void updateSampleCondition(Long idSampleCondition, SampleCondition sampleCondition) {
        sampleConditionDAO.updateSampleCondition(idSampleCondition, sampleCondition);
	}
}
