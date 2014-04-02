package hci.biominer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

import hci.biominer.model.genome.Genome;


public class ColumnValidators {
	
	public static void checkGeneExistance() {
		
	}
	
	public static void checkTranscriptExistence() {
		
	}
	
//	public static String checkBuildExistance(String build) {
//		String genomeBuild = null;
//		System.out.print("Loading genome build information..");
//		if (GenomeBuilds.BUILD_INFO.containsKey(build)) {
//			genomeBuild = build;
//			System.out.println("OK");
//		} else {
//			System.out.println(String.format("\nGenome build not supported: %s",build));
//			System.exit(1);
//		}
//		
//		return genomeBuild;
//	}
	
	public static int validateColumns(Integer[] colsToCheck, String[] colNames, File inputFile) {
		System.out.print("[ColumnValidator] Reading input file header to determine number of columns..");
		
		int colMax = -1;
		
		try {
			BufferedReader br = ModelUtil.fetchBufferedReader(inputFile);
			br.readLine(); //Skip header
			
			String line = br.readLine();
			String[] headParts = line.split("\t");
			colMax = headParts.length;
			br.close();
			System.out.println("OK");
		} catch (IOException ioex) {
			System.out.println(String.format("\n[ColumnValidator] Error reading input file: %s. Message: %s",inputFile.getAbsolutePath(),ioex.getMessage()));
			System.exit(1);
		}
		
		//Check to make sure column designations are within range
		System.out.print("[ColumnValidator] Making sure column indexes are set and within range..");
				
		int idx = 0;
		boolean failed = false;
		for (Integer col: colsToCheck) {
			if (col == -1) {
				System.out.println(String.format("[ColumnValidator] %s was not set",colNames[idx]));
				failed = true;
			} if (col < 0 || col > colMax ) {
				System.out.println(String.format("[ColumnValidator] %s index was not valid: %d, must be between %d and %d.  If the number of columns seems"
						+ " low, check to make sure your file is tab-delimited",colNames[idx],col,0,colMax));
				failed = true;
			}
			
			idx++;
		}
		
		if (failed) {
			System.out.println("Errors while checking column designations, please fix errors and try again\n");
			System.exit(1);
		} else {
			System.out.println("OK");
		}
		
		return colMax;
	}
	
	public static int validateCoordiate(Genome build, String chromosome, String coordinate) {
		int tempStart = -1;
		boolean failed = false;
		
		//Make sure value is an integer
		try {
			tempStart = Integer.parseInt(coordinate);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("[ColumnValidator] Can't parse coordinate, not an integer: %s",coordinate));
			failed = true;
		}
		
		//Make sure it falls within boundaries
		int maxPos = build.getNameChromosome().get(chromosome).getLength();
		//int maxPos = GenomeBuilds.BUILD_INFO.get(build).get(chromosome);
		if (tempStart < 0 || tempStart >= maxPos) {
			System.out.println(String.format("[ColumnValidator] Parsed start position ( %d ) does not fall with %s boundaries: %d - %d",
					tempStart,chromosome,0,maxPos));
			failed = true;
		}
		
		if (failed) {
			return -1;
		} else {
			return tempStart;
		}
		
	}
	
	public static String validateChromosome(Genome build, String chromosome) {
		boolean failed = false;
		
		if (chromosome.startsWith("chr")) {
			chromosome = chromosome.substring(3);
		}
		
		LinkedHashMap nameChromosome = build.getNameChromosome();
		
		
		if (!nameChromosome.containsKey(chromosome)) {
			System.out.println(String.format("[ColumnValidator] Can't find chromsome %s in genome build.",chromosome));
			failed = true;
		} 
		
		if (failed) {
			return null;
		} else {
			return chromosome;
		}
	}
	
	public static float validateFdr(String fdr, boolean transformed) {
		float tempFdr = -1;
		boolean failed = false;
		
		//Parse FDR value
	    try {
	    	tempFdr = Float.parseFloat(fdr);
	    } catch (NumberFormatException nfe) {
	    	System.out.println(String.format("[ColumnValidator] Can't parse FDR, not a floating point value: %s",fdr));
	    	failed = true;
	    }
	    
	    //Make sure value is greater than 0
	    if (!failed && tempFdr < 0) {
	    	System.out.println(String.format("[ColumnValidator] The FDR value is less than zero: %f, please make sure you selected the correct column",tempFdr));
	    	failed = true;
	    }
	    
	    //If the score isn't transformed, make sure it's less than one
	    if (!transformed) {
	    	if (tempFdr > 1) {
		    	System.out.println(String.format("[ColumnValidator] The FDR formatting style was set as 'untransformed', but the value is greater than 1: %f",tempFdr));
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
			System.out.println(String.format("[ColumnValidator] Can't parse log2ratio, not a floating point value: %s",log2ratio));
			failed = true;
		}
		
		if (failed) {
			return Float.MAX_VALUE;
		} else {
			return tempLog;
		}
	}
	

}
