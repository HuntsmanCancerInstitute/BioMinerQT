package hci.biominer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.dao.AnalysisDAO;
import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.DashboardModel;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.SampleSource;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;

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
	
	public List<Analysis> getAnalysesByQuery(List<Long> labs, List<Long> projects, 
			List<Long> sources, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		return analysisDAO.getAnalysesByQuery(labs, projects, sources, analysisTypes, idOrganismBuild, user);
	}
	
	public List<Analysis> getAnalysesToPreload(OrganismBuild ob, AnalysisType at) {
		return analysisDAO.getAnalysesToPreload(ob, at);
	}

	public List<OrganismBuild> getOrgansimBuildByQuery(User user, List<Long> analysisTypes, List<Long> labs, List<Long> projects,
			List<Long> analyses, List<Long> sampleSources) {
		return analysisDAO.getOrgansimBuildByQuery(user, analysisTypes, labs, projects, analyses, sampleSources);
	}
	
	public List<Lab> getLabByQuery(User user, List<Long> analysisTypes, List<Long> projects,
			List<Long> analyses, List<Long> sampleSources, Long idOrganismBuild) {
		return analysisDAO.getLabByQuery(user, analysisTypes, projects, analyses, sampleSources, idOrganismBuild);
	}
	
	public List<Project> getProjectsByQuery(List<Long> labs, List<Long> analyses, 
			List<Long> sources, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		return analysisDAO.getProjectsByQuery(labs, analyses, sources, analysisTypes, idOrganismBuild, user);
	}
	
	public List<AnalysisType> getAnalysisTypesByQuery(List<Long> labs, List<Long> projects, List<Long> analyses, 
			List<Long> sources, Long idOrganismBuild, User user) {
		return analysisDAO.getAnalysisTypesByQuery(labs, projects, analyses, sources, idOrganismBuild, user);
	}
	
	public List<SampleSource> getSampleSourceByQuery(List<Long> labs, List<Long> analyses, 
			List<Long> projects, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		return analysisDAO.getSampleSourceByQuery(labs, analyses, projects, analysisTypes, idOrganismBuild, user);
	}
	
	public ArrayList<DashboardModel> getDashboard(AnalysisTypeEnum type) {
		return analysisDAO.getDashboard(type);
	}
	
	
	
}
