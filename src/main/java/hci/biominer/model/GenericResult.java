package hci.biominer.model;

import hci.biominer.util.Enumerated.AnalysisTypeEnum;

public class GenericResult {
	private Integer start = null;
	private Integer stop = null;
	private String transFDR = null;
	private String originalName = null;
	private String mappedName = null;
	private Float log2Rto = null;
	private String chrom = null;
	private AnalysisTypeEnum analysisType = null;
	
	
	public GenericResult() {
		
	}
		
	public void loadChipData(String chrom, int start, int stop, String transFDR, float log2Rto, AnalysisTypeEnum analysisType) {
		this.chrom = chrom;
		this.start = start;
		this.stop = stop;
		this.transFDR = transFDR;
		this.log2Rto = log2Rto;
		this.analysisType = analysisType;
	}
	
	public void loadRnaseqData(String chrom, int start, int stop, String originalName, String mappedName, String transFDR, float log2Rto, AnalysisTypeEnum analysisType) {
		this.chrom = chrom;
		this.start = start;
		this.stop = stop;
		this.transFDR = transFDR;
		this.log2Rto = log2Rto;
		this.originalName = originalName;
		this.mappedName = mappedName;
		this.analysisType = analysisType;
	}
			
	

	public Integer getStart() {
		return start;
	}

	public Integer getStop() {
		return stop;
	}

	public String getTransFDR() {
		return transFDR;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getMappedName() {
		return mappedName;
	}

	public Float getLog2Rto() {
		return log2Rto;
	}

	public String getChrom() {
		return chrom;
	}

	public AnalysisTypeEnum getAnalysisType() {
		return analysisType;
	}
	
	
	
	
}
