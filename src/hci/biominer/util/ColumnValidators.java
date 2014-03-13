package hci.biominer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class ColumnValidators {

	public ColumnValidators() {
		// TODO Auto-generated constructor stub
	}
	
	public static void checkGeneExistance() {
		
	}
	
	public static void checkTranscriptExistence() {
		
	}
	
	public static String checkBuildExistance(String build) {
		String genomeBuild = null;
		System.out.print("Loading genome build information..");
		if (GenomeBuilds.BUILD_INFO.containsKey(build)) {
			genomeBuild = build;
			System.out.println("OK");
		} else {
			System.out.println(String.format("\nGenome build not supported: %s",build));
			System.exit(1);
		}
		
		return genomeBuild;
	}
	
	public static int validateColumns(Integer[] colsToCheck, String[] colNames, File inputFile) {
		System.out.print("Reading input file header to determine number of columns..");
		
		int colMax = -1;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			br.readLine(); //Skip header
			
			String[] headParts = br.readLine().split("\t");
			colMax = headParts.length;
			br.close();
			System.out.println("OK");
		} catch (IOException ioex) {
			System.out.println(String.format("\nError reading input file: %s. Message: %s",inputFile.getAbsolutePath(),ioex.getMessage()));
			System.exit(1);
		}
		
		//Check to make sure column designations are within range
		System.out.print("Making sure column indexes are set and within range..");
				
		int idx = 0;
		boolean failed = false;
		for (Integer col: colsToCheck) {
			if (col == -1) {
				System.out.println(String.format("%s was not set",colNames[idx]));
				failed = true;
			} if (col < 0 || col >= colMax ) {
				System.out.println(String.format("\n%s index was not valid: %d, must be between %d and %d.  If the number of columns seems"
						+ " low, check to make sure your file is tab-delimited",colNames[idx],col,0,colMax));
				failed = true;
			}
			
			idx++;
		}
		
		if (failed) {
			System.out.println("\nErrors while checking column designations, please fix errors and try again\n");
			System.exit(1);
		} else {
			System.out.println("OK");
		}
		
		return colMax;
	}
	
	public static int validateCoordiate(String build, String chromosome, String coordinate) {
		int tempStart = -1;
		boolean failed = false;
		
		//Make sure value is an integer
		try {
			tempStart = Integer.parseInt(coordinate);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Can't parse coordinate, not an integer: %s",coordinate));
			failed = true;
		}
		
		//Make sure it falls within boundaries
		int maxPos = GenomeBuilds.BUILD_INFO.get(build).get(chromosome);
		if (tempStart < 0 || tempStart > maxPos) {
			System.out.println(String.format("Parsed start position ( %d ) does not fall with %s boundaries: %d - %d",
					tempStart,chromosome,0,maxPos));
			failed = true;
		}
		
		if (failed) {
			return -1;
		} else {
			return tempStart;
		}
		
	}
	
	public static String validateChromosome(String build, String chromosome) {
		String tempChrom = chromosome;
		boolean failed = false;
		
		if (!tempChrom.startsWith("chr")) {
			tempChrom = "chr" + tempChrom;
		}
		if (tempChrom.equals("chrMT")) {
			tempChrom = "chrM";
		}
		if (!GenomeBuilds.BUILD_INFO.get(build).containsKey(tempChrom)) {
			System.out.println(String.format("Can't find chromsome %s in genome build %s.",tempChrom,build));
			failed = true;
		} 
		
		if (failed) {
			return null;
		} else {
			return tempChrom;
		}
	}
	
	public static float validateFdr(String fdr, boolean transformed) {
		float tempFdr = -1;
		boolean failed = false;
		
		//Parse FDR value
	    try {
	    	tempFdr = Float.parseFloat(fdr);
	    } catch (NumberFormatException nfe) {
	    	System.out.println(String.format("Can't parse FDR, not a floating point value: %s",fdr));
	    	failed = true;
	    }
	    
	    //Make sure value is greater than 0
	    if (!failed && tempFdr < 0) {
	    	System.out.println(String.format("The FDR value is less than zero: %f, please make sure you selected the correct column",tempFdr));
	    	failed = true;
	    }
	    
	    //If the score isn't transformed, make sure it's less than one
	    if (!transformed) {
	    	if (tempFdr > 1) {
		    	System.out.println(String.format("The FDR formatting style was set as 'untransformed', but the value is greater than 1: %f",tempFdr));
		    	failed = true;
	    	}
	    	tempFdr = (float)(-10 * Math.log(tempFdr));
	    }
	    
	    if (failed) {
	    	return -1;
	    } else {
	    	return tempFdr;
	    }
	}
	
	public static float validateLog2Ratio(String log2ratio) {
		float tempLog = Float.MAX_VALUE;
		boolean failed = false;
		
		//Parser log2ratio value
		try {
			tempLog = Float.parseFloat(log2ratio);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Can't parse log2ratio, not a floating point value: %s",log2ratio));
			failed = true;
		}
		
		if (failed) {
			return Float.MAX_VALUE;
		} else {
			return tempLog;
		}
	}
	

}
