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
	
	public static int validateColumns(Integer[] colsToCheck, String[] colNames, File inputFile, boolean vcfHeader) throws Exception {
		System.out.print("[ColumnValidator] Reading input file header to determine number of columns..");
		
		int colMax = -1;
		
		try {
			BufferedReader br = ModelUtil.fetchBufferedReader(inputFile);
			
			if (vcfHeader) {
				while(true) {
					String line = br.readLine();
					if (line.startsWith("#CHROM")) {
						break;
					}
				}
			} else {
				br.readLine(); //Skip header
			}
			
			String line = br.readLine();
			String[] headParts = line.split("\t");
			colMax = headParts.length;
			br.close();
			System.out.println("OK");
		} catch (IOException ioex) {
			throw new IOException(String.format("[ColumnValidator] Error reading input file: %s. Message: %s.",inputFile.getAbsolutePath(),ioex.getMessage()));
		}
		
		//Check to make sure column designations are within range
		System.out.print("[ColumnValidator] Making sure column indexes are set and within range..");
				
		int idx = 0;

		for (Integer col: colsToCheck) {
			if (col == -1) {
				throw new IOException(String.format("[ColumnValidator] %s was not set.", colNames[idx]));
			} if (col < 0 || col > colMax ) {
				throw new IOException(String.format("[ColumnValidator] %s index was not valid: %d, must be between %d and %d.  If the number of columns seems"
						+ " low, check to make sure your file is tab-delimited.",colNames[idx],col,0,colMax));
			}
			idx++;
		}
		
		System.out.println("OK");
		return colMax;
	}
	
	public static int validateCoordiate(Genome build, String chromosome, String coordinate) throws Exception{
		int tempStart = -1;
		
		//Make sure value is an integer
		try {
			tempStart = Integer.parseInt(coordinate);
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException(String.format("[ColumnValidator] Can't parse coordinate, not an integer: %s. Please make sure you selected the proper columns.",coordinate));	
		}
		
		//Make sure it falls within boundaries
		int maxPos = build.getNameChromosome().get(chromosome).getLength();
		//int maxPos = GenomeBuilds.BUILD_INFO.get(build).get(chromosome);
		if (tempStart < 0 || tempStart >= maxPos) {
			throw new NumberFormatException(String.format("[ColumnValidator] Parsed start position ( %d ) does not fall with %s boundaries: %d - %d. Please make"
					+ " sure you selected the proper genome build.",
					tempStart,chromosome,0,maxPos));	
		}
		
		return tempStart;
	}
	
	public static String validateChromosome(Genome build, String chromosome) throws Exception {

		
		if (chromosome.startsWith("chr")) {
			chromosome = chromosome.substring(3);
		}
		
		LinkedHashMap nameChromosome = build.getNameChromosome();
		
		
		if (!nameChromosome.containsKey(chromosome)) {
			throw new Exception(String.format("[ColumnValidator] Can't find chromosome '%s' in genome build '%s'. Please make sure your genome build is correct and that "
					+ "you selected the proper columns.",chromosome,build.getBuildName()));
		} 
		
		return chromosome;
	}
	
	public static float validateFdr(String fdr, boolean transformed) throws Exception{
		float tempFdr = -1;

		
		//Parse FDR value
	    try {
	    	tempFdr = Float.parseFloat(fdr);
	    } catch (NumberFormatException nfe) {
	    	throw new NumberFormatException(String.format("[ColumnValidator] Can't parse FDR, not a floating point value: %s.",fdr));
	    }
	    
	    //Make sure value is greater than 0
	    if (tempFdr < 0) {
	    	throw new Exception(String.format("[ColumnValidator] The FDR value is less than zero: %f, please make sure you selected the correct column.",tempFdr));
	    }
	    
	    //If the score isn't transformed, make sure it's less than one
	    if (!transformed) {
	    	if (tempFdr > 1) {
	    		throw new Exception(String.format("[ColumnValidator] The FDR formatting style was set as 'untransformed', but the value is greater than 1: %f.",tempFdr));
	    	}
	    	tempFdr = (float)(-10 * Math.log(tempFdr));
	    }
	    
	
	    return tempFdr;
	}
	
	public static float validateLog2Ratio(String log2ratio) throws Exception{
		float tempLog = Float.MAX_VALUE;
		
		//Parser log2ratio value
		try {
			tempLog = Float.parseFloat(log2ratio);
		} catch (NumberFormatException nfe) {
			throw new NumberFormatException(String.format("[ColumnValidator] Can't parse log2ratio, not a floating point value: %s.",log2ratio));
		}
		
		return tempLog;
	}
}
