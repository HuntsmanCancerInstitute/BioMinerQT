package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SampleTypeDAO;
import hci.biominer.model.SampleType;

@Service("SampleTypeService")
@Transactional
public class SampleTypeService {
	@Autowired
	private SampleTypeDAO sampleTypeDAO;
	
	public SampleType getSampleTypeById(Long idSampleType) {
		return sampleTypeDAO.getSampleTypeById(idSampleType);
	}
	
	public List<SampleType> getAllSampleTypes() {
		return sampleTypeDAO.getSampleTypes();
	}
	
	public void addSampleType(SampleType sampleType) {
		sampleTypeDAO.addSampleType(sampleType);		
		
	}

	public void deleteSampleTypeById(Long id) {
        sampleTypeDAO.deleteSampleType(id);
		
	}

	public void updateSampleType(Long idSampleType, SampleType sampleType) {
        sampleTypeDAO.updateSampleType(idSampleType, sampleType);
	}
}
