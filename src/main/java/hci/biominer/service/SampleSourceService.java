package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SampleSourceDAO;
import hci.biominer.model.SampleSource;

@Service("SampleSource")
@Transactional
public class SampleSourceService {
	@Autowired
	private SampleSourceDAO sampleSourceDAO;
	
	public SampleSource getSampleSourceById(Long idSampleSource) {
		return sampleSourceDAO.getSampleSourceById(idSampleSource);
	}
	
	public List<SampleSource> getAllSampleSources() {
		return sampleSourceDAO.getSampleSources();
	}
	
	public void addSampleSource(SampleSource sampleSource) {
		sampleSourceDAO.addSampleSource(sampleSource);		
		
	}

	public void deleteSampleSourceById(Long idSampleSource) {
        sampleSourceDAO.deleteSampleSource(idSampleSource);
		
	}

	public void updateSampleSource(Long idSampleSource, SampleSource sampleSource) {
        sampleSourceDAO.updateSampleSource(idSampleSource, sampleSource);
	}
	
}
