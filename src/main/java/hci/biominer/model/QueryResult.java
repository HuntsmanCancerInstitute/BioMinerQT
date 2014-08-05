package hci.biominer.model;

import java.math.BigDecimal;


public class QueryResult {
  
  private String projectName;
  private String analysisType;
  private String analysisName;
  private String sampleConditions;
  private String analysisSummary;
  private String coordinates;
  private BigDecimal log2Ratio;
  private BigDecimal FDR; 
	
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

  public BigDecimal getLog2Ratio() {
    return log2Ratio;
  }

  public void setLog2Ratio(BigDecimal log2Ratio) {
    this.log2Ratio = log2Ratio;
  }

  public BigDecimal getFDR() {
    return FDR;
  }

  public void setFDR(BigDecimal fDR) {
    FDR = fDR;
  }

  public String getAnalysisName() {
    return analysisName;
  }

  public void setAnalysisName(String analysisName) {
    this.analysisName = analysisName;
  }
	

	
}
