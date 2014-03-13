package hci.biominer.util;

import java.util.HashMap;

public class GenomeBuilds {
	
	private static final HashMap<String,Integer> DM3_BUILD;
	static
	{
		DM3_BUILD = new HashMap<String,Integer>();
		DM3_BUILD.put("chr2L",23011544);
		DM3_BUILD.put("chr2LHet", 368872);
		DM3_BUILD.put("chr2R", 21146708);
		DM3_BUILD.put("chr2RHet", 3288761);
		DM3_BUILD.put("chr3L", 24543557);
		DM3_BUILD.put("chr3LHet", 2555491);
		DM3_BUILD.put("chr3R", 27905053);
		DM3_BUILD.put("chr3RHet", 2517507);
		DM3_BUILD.put("chr4", 1351857);
		DM3_BUILD.put("chrX", 22422827);
		DM3_BUILD.put("chrXHet", 204112);
		DM3_BUILD.put("chrYHet", 347038);
		DM3_BUILD.put("chrU", 10049037);
		DM3_BUILD.put("chrUextra", 29004656);
		DM3_BUILD.put("chrM", 19517);
		
	}
	
	
	private static final HashMap<String,Integer> HG19_BUILD;
	static
	{
		HG19_BUILD = new HashMap<String,Integer>();
		HG19_BUILD.put("chr1", 248956422);
		HG19_BUILD.put("chr2", 242193529);
		HG19_BUILD.put("chr3", 198295559);
		HG19_BUILD.put("chr4", 190214555);
		HG19_BUILD.put("chr5", 181538259);
		HG19_BUILD.put("chr6", 170805979);
		HG19_BUILD.put("chr7", 159345973);
		HG19_BUILD.put("chr8", 145138636);
		HG19_BUILD.put("chr9", 138394717);
		HG19_BUILD.put("chr10", 133797422);
		HG19_BUILD.put("chr11", 135086622);
		HG19_BUILD.put("chr12", 133275309);
		HG19_BUILD.put("chr13", 114364328);
		HG19_BUILD.put("chr14", 107043718);
		HG19_BUILD.put("chr15", 101991189);
		HG19_BUILD.put("chr16", 90338345);
		HG19_BUILD.put("chr17", 83257441);
		HG19_BUILD.put("chr18", 80373285);
		HG19_BUILD.put("chr19", 58617616);
		HG19_BUILD.put("chr20", 64444167);
		HG19_BUILD.put("chr21", 46709983);
		HG19_BUILD.put("chr22", 50818468);
		HG19_BUILD.put("chrX", 156040895);
		HG19_BUILD.put("chrY", 57227415);
	}
	
    public static final HashMap<String,HashMap<String,Integer>> BUILD_INFO;
    static
    {
    	BUILD_INFO = new HashMap<String,HashMap<String,Integer>>();
    	BUILD_INFO.put("hg19", HG19_BUILD);
    	BUILD_INFO.put("dm3", DM3_BUILD);
    }
 
    
    
    
    
    
    
	

}
