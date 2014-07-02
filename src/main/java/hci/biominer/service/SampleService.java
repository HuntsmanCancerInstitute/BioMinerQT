package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.SampleDAO;
import hci.biominer.model.Sample;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;

@Service("SampleService")
@Transactional
public class SampleService {
	@Autowired
	private SampleDAO sampleDAO;
	
	public Sample getSampleById(Long idSample) {
		return sampleDAO.getSampleById(idSample);
	}
	
	public List<Sample> getAllSamples() {
		return sampleDAO.getSamples();
	}
	
	public List<Sample> getSampleByProject(Project project) {
		return sampleDAO.getSampleByProject(project);
	}
	
	public List<Sample> getSampleByAnalysis(Analysis analysis) {
		return sampleDAO.getSampleByAnalysis(analysis);
	}
 	
	public void addSample(Sample sample) {
		sampleDAO.addSample(sample);		
	}

	public void deleteSampleById(Long idSample) {
        sampleDAO.deleteSample(idSample);
	}

	public void updateSample(Long idSample, Sample sample) {
        sampleDAO.updateSample(idSample, sample);
	}
	
	public void updateSampleAnalysis(Long idSample, Analysis analysis) {
		sampleDAO.updateSampleAnalysis(idSample, analysis);
	}
}
