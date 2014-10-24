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
}
