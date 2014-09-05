package hci.biominer.model;

import java.math.BigDecimal;


public class QueryResult {
  
  private String projectName;
  private String analysisType;
  private String analysisName;
  private String sampleConditions;
  private String analysisSummary;
  private String coordinates;
  private Float log2Ratio;
  private Float FDR; 
	
	public QueryResult() {
	}

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getAnalysisType() {
    return analysisType;
  }

  public void setAnalysisType(String analysisType) {
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

  public Float getFDR() {
    return FDR;
  }

  public void setFDR(Float fDR) {
    FDR = fDR;
  }

  public String getAnalysisName() {
    return analysisName;
  }

  public void setAnalysisName(String analysisName) {
    this.analysisName = analysisName;
  }
	

	
}
