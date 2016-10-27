package hci.biominer.model;

import hci.biominer.util.Enumerated.AnalysisTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryResult implements Serializable,Cloneable {
	private final static Pattern p1 = Pattern.compile("(chr)*(\\w+?):(\\d+)-(\\d+)");
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
	private String ensemblName = "NA";
	private String mappedName = "NA";
	private Long idAnalysis;
	private String chrom;
	private Integer start;
	private Integer end;
	private boolean isAlpha;
	
	public QueryResult() {
	}

	
	public Long getIdAnalysis() {
		return idAnalysis;
	}

	public void setIdAnalysis(Long idAnalysis) {
		this.idAnalysis = idAnalysis;
	}

	public String getChrom() {
		return chrom;
	}
	
	public Integer getStart() {
		return start;
	}

	public Integer getEnd() {
		return end;
	}

	public boolean isAlpha() {
		return isAlpha;
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
		
		Matcher m1 = p1.matcher(coordinates);
		if (m1.matches()) {
			this.chrom = m1.group(2);
			this.start = Integer.parseInt(m1.group(3));
			this.end = Integer.parseInt(m1.group(4));
			
			try {
				Integer.parseInt(this.chrom);
				this.isAlpha = false;
			} catch (NumberFormatException nfe) {
				this.isAlpha = true;
			}
		} else {
			System.out.println("Could not parse coordinate");
			this.chrom = "NA";
			this.start = -1;
			this.end = -1;
		}
		
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
		String header = "Index\tProjectName\tAnalysisType\tAnalysisName\tSampleConditions\tSearch\tEnsemblID\tCommonName\tCoordinates\tLog2Ratio\tFDR\n";
		return header;
	}

	public String writeRegion() {
		String coordinate = "=HYPERLINK(\"http://127.0.0.1:60151/goto?locus=" + this.coordinates + "\",\"" + this.coordinates + "\")";
		
		String outline = String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%f\t%s\n", index, projectName, analysisType, analysisName, sampleConditions, search, coordinate, log2Ratio, FDR);
		return outline;
	}
	

	public String writeGene() {
		String coordinate = "=HYPERLINK(\"http://127.0.0.1:60151/goto?locus=" + this.coordinates + "\",\"" + this.coordinates + "\")";
		String outline = String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%f\t%s\n", index, projectName, analysisType, analysisName, sampleConditions, search, ensemblName, mappedName, coordinate, log2Ratio, FDR);
		return outline;
	}


	public String getEnsemblName() {
		return ensemblName;
	}


	public void setEnsemblName(String ensemblName) {
		this.ensemblName = ensemblName;
	}
	
	public QueryResult clone() throws CloneNotSupportedException {
		return (QueryResult)super.clone();
	}


}
