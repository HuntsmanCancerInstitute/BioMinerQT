package hci.biominer.model;

import java.io.Serializable;

import hci.biominer.util.Enumerated.*;

public class GenericResult implements Serializable {
	private String chrom = null;
	private Integer start = null;
	private Integer stop = null;
	
	private String transFDR = null;
	private Float log2Rto = null;
	
	private String originalName = null;
	private String mappedName = null;
	
	private AnalysisTypeEnum analysisType = null;
	
	//Variant Only
	private VarLocationEnum variantLocation = null;
	private VarTypeEnum variantType = null;
	private String dbSNP = null;
	private String reference = null;
	private String alternate = null;
	private Integer mut = null;
	private Integer wild = null;
	private Integer het = null;
	private Integer other = null;

	
	
	public GenericResult() {
		
	}
	
	public void loadVariantData(String chrom, Integer start, Integer end, 
			String reference, String alternative,
			Integer wild, Integer het, Integer mut, Integer other,
			String originalName, String mappedName, 
			VarTypeEnum variantType, VarLocationEnum variantLocation, String dbSNP) {
		this.chrom = chrom;
		this.start = start;
		this.stop = end;
		this.wild = wild;
		this.het = het;
		this.mut = mut;
		this.other = other;
		this.originalName = originalName;
		this.mappedName = mappedName;
		this.variantLocation = variantLocation;
		this.variantType = variantType;
		this.dbSNP = dbSNP;
		
	}
		
	public void loadChipData(String chrom, int start, int stop, String transFDR, float log2Rto, AnalysisTypeEnum analysisType) {
		this.chrom = chrom;
		this.start = start;
		this.stop = stop;
		this.transFDR = transFDR;
		this.log2Rto = log2Rto;
		this.analysisType = analysisType;
	}
	
	public void loadBedData(String chrom, int start, int stop, String transFDR, AnalysisTypeEnum analysisType) {
		this.chrom = chrom;
		this.start = start;
		this.stop = stop;
		this.transFDR = transFDR;
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

	public VarLocationEnum getVariantLocation() {
		return variantLocation;
	}

	public VarTypeEnum getVariantType() {
		return variantType;
	}

	public String getDbSNP() {
		return dbSNP;
	}

	public String getReference() {
		return reference;
	}

	public String getAlternate() {
		return alternate;
	}

	public Integer getMut() {
		return mut;
	}

	public Integer getWild() {
		return wild;
	}

	public Integer getHet() {
		return het;
	}

	public Integer getOther() {
		return other;
	}

	
	
	
	
	
	
}
