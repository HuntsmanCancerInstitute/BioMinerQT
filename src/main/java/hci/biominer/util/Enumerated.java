package hci.biominer.util;

public class Enumerated {
	public static enum FileTypeEnum {
		UPLOADED, IMPORTED
	}

	public static enum FileStateEnum {
		SUCCESS, FAILURE, WARNING
	}
	
	public static enum ProjectVisibilityEnum {
		LAB, INSTITUTE, PUBLIC
	}
	
	public static enum AnalysisTypeEnum {
		ChIPSeq, RNASeq, Methylation, Variant
	}
	
	public static enum VarTypeEnum {
		nonframeshift_insertion,
		stoploss_SNV,
		unknown,
		synonymous_SNV,
		stopgain_SNV,
		frameshift_substitution,
		frameshift_insertion,
		nonsynonymous_SNV,
		frameshift_deletion,
		nonframeshift_deletion,
		reference
	}
	
	public static enum VarLocationEnum {
		splicing,
		downstream,
		upstream,
		UTR5,
		ncRNA_exonic,
		intergenic,
		ncRNA_UTR5,
		intronic,
		ncRNA_UTR3,
		exonic,
		ncRNA_splicing,
		UTR3,
		ncRNA_intronic,
	}
}
