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
	
	public SampleSource getSampleSourceBySource(String source, Long idOrganismBuild) {
		return sampleSourceDAO.getSampleSourceBySource(source, idOrganismBuild);
	}
	
	public List<SampleSource> getAllSampleSources() {
		return sampleSourceDAO.getSampleSources();
	}
	
	public Long addSampleSource(SampleSource sampleSource) {
		return sampleSourceDAO.addSampleSource(sampleSource);		
		
	}

	public void deleteSampleSourceById(Long idSampleSource) {
        sampleSourceDAO.deleteSampleSource(idSampleSource);
	}

	public void updateSampleSource(Long idSampleSource, SampleSource sampleSource) {
        sampleSourceDAO.updateSampleSource(idSampleSource, sampleSource);
	}
	
	public void deleteSampleSources(List<Long> sampleSourceIdList) {
		sampleSourceDAO.deleteSampleSources(sampleSourceIdList);
	}
	
	public List<SampleSource> getUnusedSampleSources(Long idOrganismBuild) {
		return sampleSourceDAO.getUnusedSampleSources(idOrganismBuild);
	}
	
}
