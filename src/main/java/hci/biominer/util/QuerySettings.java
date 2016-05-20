package hci.biominer.util;

import java.io.Serializable;
import java.util.List;

public class QuerySettings implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//Enums
	private String codeResultType;
	private String target;
	
	//Analysis Filtering
    private Long idOrganismBuild;
    private List<Long> idAnalysisTypes;
    private List<Long> idLabs;
    private List<Long> idProjects;
    private List<Long> idAnalyses;
    private List<Long> idSampleSources;
    private boolean searchExisting;
    
    //Gene Filtering
    private String regions;
    private Integer regionMargins;
    private String genes;
    private Integer geneMargins;
    private boolean isReverse;
    private Long idTransFactor;
    private Integer tfMargins;
    
    //Threshold filtering
    private Float FDR;
    private String codeFDRComparison;
    private Float log2Ratio;
    private String codeLog2RatioComparison;
    
    //Pagination
    private Integer resultsPerPage;
    private String sortType;
    
    
    public QuerySettings(String codeResultType, String target, Long idOrganismBuild, List<Long> idAnalysisTypes, List<Long> idLabs,
    		List<Long> idProjects, List<Long> idAnalyses, List<Long> idSampleSources, Long idTransFactor, Integer tfMargins, String regions, Integer regionMargins, String genes,
    		Integer geneMargins, Float FDR, String codeFDRComparison, Float log2Ratio, String codeLog2RatioComparison, Integer resultsPerPage,
    		String sortType, boolean isReverse, boolean searchExisting) {
    	this.codeResultType = codeResultType;
    	this.target = target;
    	this.idOrganismBuild = idOrganismBuild;
    	this.idAnalysisTypes = idAnalysisTypes;
    	this.idLabs = idLabs;
    	this.idProjects = idProjects;
    	this.idAnalyses = idAnalyses;
    	this.idSampleSources = idSampleSources;
    	this.idTransFactor = idTransFactor;
    	this.tfMargins = tfMargins;
    	this.regions = regions;
    	this.regionMargins = regionMargins;
    	this.genes = genes;
    	this.geneMargins = geneMargins;
    	this.FDR = FDR;
    	this.codeFDRComparison = codeFDRComparison;
    	this.log2Ratio = log2Ratio;
    	this.codeLog2RatioComparison = codeLog2RatioComparison;
    	this.resultsPerPage = resultsPerPage;
    	this.sortType =sortType;
    	this.isReverse = isReverse;
    	this.searchExisting = searchExisting;
    }
    
    
    
	public Integer getTfMargins() {
		return tfMargins;
	}



	public void setTfMargins(Integer tfMargins) {
		this.tfMargins = tfMargins;
	}



	public Long getIdTransFactor() {
		return idTransFactor;
	}

	public void setIdTransFactor(Long idTransFactor) {
		this.idTransFactor = idTransFactor;
	}

	public boolean isSearchExisting() {
		return searchExisting;
	}

	public void setSearchExisting(boolean searchExisting) {
		this.searchExisting = searchExisting;
	}

	public boolean isReverse() {
		return isReverse;
	}

	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getCodeResultType() {
		return codeResultType;
	}
	public void setCodeResultType(String codeResultType) {
		this.codeResultType = codeResultType;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public Long getIdOrganismBuild() {
		return idOrganismBuild;
	}
	public void setIdOrganismBuild(Long idOrganismBuild) {
		this.idOrganismBuild = idOrganismBuild;
	}
	public List<Long> getIdAnalysisTypes() {
		return idAnalysisTypes;
	}
	public void setIdAnalysisTypes(List<Long> idAnalysisTypes) {
		this.idAnalysisTypes = idAnalysisTypes;
	}
	public List<Long> getIdLabs() {
		return idLabs;
	}
	public void setIdLabs(List<Long> idLabs) {
		this.idLabs = idLabs;
	}
	public List<Long> getIdProjects() {
		return idProjects;
	}
	public void setIdProjects(List<Long> idProjects) {
		this.idProjects = idProjects;
	}
	public List<Long> getIdAnalyses() {
		return idAnalyses;
	}
	public void setIdAnalyses(List<Long> idAnalyses) {
		this.idAnalyses = idAnalyses;
	}
	public List<Long> getIdSampleSources() {
		return idSampleSources;
	}
	public void setIdSampleSources(List<Long> idSampleSources) {
		this.idSampleSources = idSampleSources;
	}
	public String getRegions() {
		return regions;
	}
	public void setRegions(String regions) {
		this.regions = regions;
	}
	public Integer getRegionMargins() {
		return regionMargins;
	}
	public void setRegionMargins(Integer regionMargins) {
		this.regionMargins = regionMargins;
	}
	public String getGenes() {
		return genes;
	}
	public void setGenes(String genes) {
		this.genes = genes;
	}
	public Integer getGeneMargins() {
		return geneMargins;
	}
	public void setGeneMargins(Integer geneMargins) {
		this.geneMargins = geneMargins;
	}
	public Float getFDR() {
		return FDR;
	}
	public void setFDR(Float fDR) {
		FDR = fDR;
	}
	public String getCodeFDRComparison() {
		return codeFDRComparison;
	}
	public void setCodeFDRComparison(String codeFDRComparison) {
		this.codeFDRComparison = codeFDRComparison;
	}
	public Float getLog2Ratio() {
		return log2Ratio;
	}
	public void setLog2Ratio(Float log2Ratio) {
		this.log2Ratio = log2Ratio;
	}
	public String getCodeLog2RatioComparison() {
		return codeLog2RatioComparison;
	}
	public void setCodeLog2RatioComparison(String codeLog2RatioComparison) {
		this.codeLog2RatioComparison = codeLog2RatioComparison;
	}
	public Integer getResultsPerPage() {
		return resultsPerPage;
	}
	public void setResultsPerPage(Integer resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}
    
    
    
    
	
}
