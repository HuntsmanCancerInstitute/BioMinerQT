package hci.biominer.model;

import hci.biominer.util.Enumerated.AnalysisTypeEnum;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class QueryResult {
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
	private String mappedName;
	private Long idAnalysis;
	private String chrom;
	private Long start;
	private Long end;
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
	
	public Long getStart() {
		return start;
	}

	public Long getEnd() {
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
			this.start = Long.parseLong(m1.group(3));
			this.end = Long.parseLong(m1.group(4));
			
			try {
				Integer.parseInt(this.chrom);
				this.isAlpha = false;
			} catch (NumberFormatException nfe) {
				this.isAlpha = true;
			}
		} else {
			System.out.println("Could not parse coordinate");
			this.chrom = "NA";
			this.start = (long)-1;
			this.end = (long)-1;
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
