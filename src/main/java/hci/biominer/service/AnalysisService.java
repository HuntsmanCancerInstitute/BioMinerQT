package hci.biominer.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.AnalysisDAO;
import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.SampleSource;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;

@Service("AnalysisService")
@Transactional
public class AnalysisService {
	@Autowired
	private AnalysisDAO analysisDAO;
	
	public List<Analysis> getAllAnalyses() {
		return analysisDAO.getAllAnalysis();
	}
	
	public List<Analysis> getAnalysesByProject(Project project) {
		return analysisDAO.getAnalysesByProject(project);
	}

	public List<Analysis> getAnalysesPublic() {
		return this.analysisDAO.getPublicAnalyses();
	}

	public List<Analysis> getAnalysesByVisibility(User user) {
		return this.analysisDAO.getAnalysesByVisibility(user);
	}
	
	public Long addAnalysis(Analysis analysis) {
		return analysisDAO.addAnalysis(analysis);
	}
	
	public void updateAnalysis(Analysis analysis, Long idAnalysis) {
		analysisDAO.updateAnalysis(analysis, idAnalysis);
	}
	
	public Analysis getAnalysisById(Long idAnalysis) {
		return analysisDAO.getAnalysisById(idAnalysis);
	}
	
	public void deleteAnalysis(Long idAnalysis) {
		analysisDAO.deleteAnalysis(idAnalysis);
	}
	
	public List<Analysis> getAnalysesByQuery(List<Long> labs, List<Long> projects, List<Long> analyses,
			List<Long> sources, Long analysisType, Long idGenomeBuild, User user) {
		return analysisDAO.getAnalysesByQuery(labs, projects, analyses, sources, analysisType, idGenomeBuild, user);
	}
	
	public List<Analysis> getAnalysesToPreload(OrganismBuild ob, AnalysisType at) {
		return analysisDAO.getAnalysesToPreload(ob, at);
	}

	
	
}
