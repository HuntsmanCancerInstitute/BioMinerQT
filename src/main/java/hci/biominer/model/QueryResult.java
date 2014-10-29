package hci.biominer.model;

import hci.biominer.util.Enumerated.AnalysisTypeEnum;

import java.math.BigDecimal;


public class QueryResult {

	private Integer index;
	private String projectName;
	private AnalysisTypeEnum analysisType;
	private String analysisName;
	private String sampleConditions;
	private String analysisSummary;
	private String coordinates;
	private Float log2Ratio;
	private String FDR; 
	private String search;
	private String mappedName;

	public QueryResult() {
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public AnalysisTypeEnum getAnalysisType() {
		return analysisType;
	}

	public void setAnalysisType(AnalysisTypeEnum analysisType) {
		this.analysisType = analysisType;
	}

	public String getSampleConditions() {
		return sampleConditions;
	}

	public void setSampleConditions(String sampleConditions) {
		this.sampleConditions = sampleConditions;
	}

	public String getAnalysisSummary() {
		return analysisSummary;
	}

	public void setAnalysisSummary(String analysisSummary) {
		this.analysisSummary = analysisSummary;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public Float getLog2Ratio() {
		return log2Ratio;
	}

	public void setLog2Ratio(Float log2Ratio) {
		this.log2Ratio = log2Ratio;
	}

	public String getFDR() {
		return FDR;
	}

	public void setFDR(String fDR) {
		FDR = fDR;
	}

	public String getAnalysisName() {
		return analysisName;
	}

	public void setAnalysisName(String analysisName) {
		this.analysisName = analysisName;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getMappedName() {
		return mappedName;
	}

	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
	}
	
	public String writeRegionHeader() {
		String header = "Index\tProjectName\tAnalysisType\tAnalysisName\tSampleConditions\tSearch\tCoordinates\tLog2Ratio\tFDR\n";
		return header;
	}
	
	
	
	public String writeGeneHeader() {
		String header = "Index\tProjectName\tAnalysisType\tAnalysisName\tSampleConditions\tSearch\tGene\tCoordinates\tLog2Ratio\tFDR\n";
		return header;
	}

	public String writeRegion() {
		String outline = String.format("%d\t%s\t%s\t%s\t%s\t%s\t%f\t%s\n", index, projectName, analysisType, analysisName, sampleConditions, coordinates, log2Ratio, FDR);
		return outline;
	}
	
	

	public String writeGene() {
		String outline = String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%f\t%s\n", index, projectName, analysisType, analysisName, sampleConditions, search, mappedName, coordinates, log2Ratio, FDR);
		return outline;
	}


}
