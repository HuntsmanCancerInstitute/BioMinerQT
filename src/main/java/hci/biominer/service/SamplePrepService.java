package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SamplePrepDAO;
import hci.biominer.model.SamplePrep;

@Service("SamplePrepService")
@Transactional
public class SamplePrepService {
	@Autowired
	private SamplePrepDAO samplePrepDAO;
	
	public SamplePrep getSamplePrepById(Long idSamplePrep) {
		return samplePrepDAO.getSamplePrepById(idSamplePrep);
	}
	
	public SamplePrep getSamplePrepByDescription(String description, Long idSampleType) {
		return samplePrepDAO.getSamplePrepByDescription(description, idSampleType);
	}
	
	public List<SamplePrep> getSamplePrepBySampleType(Long idSampleType) {
		return samplePrepDAO.getSamplePrepBySampleType(idSampleType);
	}
	
	public List<SamplePrep> getAllSamplePreps() {
		return samplePrepDAO.getSamplePreps();
	}
	
	public Long addSamplePrep(SamplePrep samplePrep) {
		return samplePrepDAO.addSamplePrep(samplePrep);		
		
	}

	public void deleteSamplePrepById(Long idSamplePrep) {
        samplePrepDAO.deleteSamplePrep(idSamplePrep);
		
	}

	public void updateSamplePrep(Long idSamplePrep, SamplePrep samplePrep) {
        samplePrepDAO.updateSamplePrep(idSamplePrep, samplePrep);
	}
	
	public void deleteSamplePreps(List<Long> samplePrepIdList) {
		samplePrepDAO.deleteSamplePreps(samplePrepIdList);
	}
	
	public List<SamplePrep> getUnusedSamplePreps() {
		return samplePrepDAO.getUnusedSamplePreps();
	}
	
	

}
