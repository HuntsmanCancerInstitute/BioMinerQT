package hci.biominer.parser;

import hci.biominer.model.ExternalGene;
import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.util.ColumnValidators;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.Enumerated.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class VCFParser {
	private int colMax = -1;
	private File inputFile = null;
	private File outputFile = null;
	private Genome genome = null;
	private ArrayList<String> parsedData = null;
	private StringBuilder warningMessage = new StringBuilder("");
	private HashMap<String,ArrayList<Long>> externalGeneToBiominerId;
	private HashMap<Long,String> biominerIdToEnsembl;
	
	private HashMap<String,Gene> alreadyObserved = new HashMap<String,Gene>();
	
	/* 
	 * Constructor for internal call. Assumes 0-based indexes.
	 */
	public VCFParser(File inputFile, File outputFile, List<ExternalGene> egList, Genome genome) throws Exception {
				
		if (!inputFile.exists()) {
			throw new Exception("Specified input file does not exist");
		}
		
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.genome = genome;

		externalGeneToBiominerId = new HashMap<String,ArrayList<Long>>();
		biominerIdToEnsembl = new HashMap<Long,String>();
		
		for (ExternalGene eg: egList) {
			String geneName = eg.getExternalGeneName();
			String source = eg.getExternalGeneSource();
			Long bId = eg.getBiominerGene().getIdBiominerGene();
			if (!externalGeneToBiominerId.containsKey(geneName)) {
				externalGeneToBiominerId.put(geneName, new ArrayList<Long>());
			}
			externalGeneToBiominerId.get(geneName).add(bId);
			
			if (source.equals("ensembl")) {
				biominerIdToEnsembl.put(bId,geneName);
			}
		}
		
		//Slurp header for number of columns:
		Integer[] colsToCheck = new Integer[]{0};
		String[] colNames = new String[] {"Test"};
		colMax = ColumnValidators.validateColumns(colsToCheck, colNames, this.inputFile, true);
	
	}
	
	
	/*
	 * Main workflow
	 */
	public String run() throws Exception {
		System.out.println("Processing input file...");
		this.processData();
		
		//Write output
		System.out.println("Writing output file...");
		this.writeData();
		
		System.out.println("All done!\n");
		
		return this.warningMessage.toString();

	}
	
	
	/*
	 * Write minimum format Chip file
	 */
	private void writeData() throws Exception{
		BufferedWriter bw = null;
		
		
		try {
			//Open file handle
			GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(this.outputFile));
			bw = new BufferedWriter(new OutputStreamWriter(zip));
			
			//Write data
			for (String line: this.parsedData) {
				bw.write(line);
			}
			
			//Close handle
			bw.close();
		} catch (IOException ioex) {
			throw new IOException(String.format("Error writing to output file file: %s.",ioex.getMessage()));
		} finally {
			try {
				bw.close();
			} catch (Exception e){}
		}
	}
	
	/*
	 * Parse and validate VCF file
	 */
	private void processData() throws Exception {
		BufferedReader br = null;
		
		Pattern typePattern = Pattern.compile("VarType=([a-zA-Z0-9_]+).*");
		Pattern locPattern = Pattern.compile("EnsemblRegion=([a-zA-Z0-9_]+).*");
		Pattern genePattern = Pattern.compile("EnsemblName=([a-zA-Z0-9_]+).*");
		Pattern dbsnpPattern = Pattern.compile("DBSNP=([a-zA-Z0-9_]+).*");
		
		try {
			//Open file handle
			br = ModelUtil.fetchBufferedReader(this.inputFile);
			
			//Slurp the header
			while(true) {
				String line = br.readLine();
				if (line.startsWith("#CHROM")) {
					break;
				}
			}
			
			//Initialize input file variables
			this.parsedData = new ArrayList<String>();
			String line = null;
			int lineCount = 1;			
			
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				
				
				//Make sure the line length matches header. Parsing might go OK if this fails, but I 
				//think it's best to error out when the file is at all malformed.
				if (parts.length != this.colMax) {
					throw new Exception(String.format("The number of columns in row %d ( %d ) doesn't match the header ( %d )\n",
							lineCount,parts.length,this.colMax));
				}
				
				//Variables
				String tempChrom = null;
				Integer tempPosition = null;
				VarTypeEnum tempVarType = null;
				VarLocationEnum tempVarLocation = null;
				String tempDbsnp = null;
				String tempGene = null;
				String mappedGene = null;
				
				
				//Parse chromsome, check to make sure it's recognizable
				tempChrom = ColumnValidators.validateChromosome(this.genome, parts[0]);
				
				//Parse start position, make sure it's an integer and within boundaries
				tempPosition = ColumnValidators.validateCoordiate(this.genome, tempChrom, parts[1]);
				
				String reference = parts[3];
				String alternative = parts[4];
				
				//Parse genotypes
				Integer het = 0;
				Integer mut = 0;
				Integer wild = 0;
				Integer other = 0;
				for (int i=8; i<parts.length; i++) {
					String[] sampleSpecific = parts[i].split(":");
					if (sampleSpecific[0] == "0/0") {
						wild++;
					} else if (sampleSpecific[0] == "0/1") {
						het++;
					} else if (sampleSpecific[0] == "1/1") {
						mut++;
					} else {
						other++;
					}
				}
				
				//Parse annotations
				String[] annotations = parts[7].split(";");
				for (String ann: annotations) {
					Matcher typeMatcher = typePattern.matcher(ann);
					Matcher locMatcher = locPattern.matcher(ann);
					Matcher geneMatcher = genePattern.matcher(ann);
					Matcher dbsnpMatcher = dbsnpPattern.matcher(ann);
					
					if (typeMatcher.matches()) {
						try {
							tempVarType = VarTypeEnum.valueOf(typeMatcher.group(1));
						} catch (Exception ex) {
							//this.warningMessage.append(String.format("Variant %s %d has unrecognized variant type: %s.<br>",tempChrom,tempPosition,typeMatcher.group(1)));
						}
						
					} else if (locMatcher.matches()) {
						try {
							tempVarLocation = VarLocationEnum.valueOf(locMatcher.group(1));
						} catch (Exception ex) {
							//this.warningMessage.append(String.format("Variant %s %d has unrecognized variant location: %s.<br>",tempChrom, tempPosition, locMatcher.group(1)));
						}
					} else if (dbsnpMatcher.matches()) {
						tempDbsnp = dbsnpMatcher.group(1);
					} else if (geneMatcher.matches()) {
						tempGene = geneMatcher.group(1);
					}
					
				}
				
				if (tempVarLocation == VarLocationEnum.intergenic) {
					tempGene = null;
				}
				
				if (tempGene != null) {
					Gene geneObject = this.checkGene(tempGene);
					
					if (geneObject != null) {
						mappedGene = geneObject.getName();
					}
				}
				
				
				//Create output string and add to data list
				String outputFile = String.format("%s\t%d\t%d\t%s\t%s\t%d\t%d\t%d\t%d\t%s\t%s\t%s\t%s\t%s\n",
						tempChrom,tempPosition,tempPosition+1,
						reference,alternative,
						wild,het,mut,other,
						tempGene,mappedGene,
						tempVarType,tempVarLocation,tempDbsnp);
				this.parsedData.add(outputFile);
				lineCount++;
			}
			
			
		} catch (IOException ioex) {
			throw new IOException(String.format("Error processing data file: %s.",ioex.getMessage()));
		} catch (Exception ex) {
		    throw ex;
		} finally {
			try {
				br.close();
			} catch (Exception e){}
		}
		
	}
	
	/** 
	 * This is more or less stolen from the Query Controller... These methods should be merged, but I am not exactly sure the best way
	 * to do it.  Chickening out and duplicating code.
	 * @param gene
	 * @return
	 */
	public Gene checkGene(String gene) {
		if (alreadyObserved.containsKey(gene)) {
			return alreadyObserved.get(gene);
		}
		
		
		List<Long> exIdList;
		if (this.externalGeneToBiominerId.containsKey(gene)) {
			exIdList = this.externalGeneToBiominerId.get(gene);
		} else {
			//this.warningMessage.append(String.format("Could not find a BiominerID for gene: %s in the BiominerDB.<br>", gene));
			alreadyObserved.put(gene, null);
			return null;
		}
		
		HashSet<Long> bIdSet = new HashSet<Long>();
		for (Long egId: exIdList) {
			bIdSet.add(egId);
		}
		
		List<Long> bIdList = new ArrayList<Long>();
		bIdList.addAll(bIdSet);
		
		
		List<String> extIdFinal = new ArrayList<String>();
		
		for (Long bId: bIdList) {
			if (this.biominerIdToEnsembl.containsKey(bId)) {
				extIdFinal.add(this.biominerIdToEnsembl.get(bId));
			}
		}
		
	
		if (extIdFinal.size() == 0) {
    		//this.warningMessage.append(String.format("Could not find an EnsemblID for gene: %s in the BiominerDB.<br>",gene));
    		alreadyObserved.put(gene, null);
    		return null;
    	} else if (extIdFinal.size() > 1) {
    		HashSet<String> names = new HashSet<String>();
    		for (String exId: extIdFinal) {
    			names.add(exId);
    		}
    		String ensemblName = null;
    		if (names.size() > 1) {
    			ensemblName = extIdFinal.get(0);
        		//this.warningMessage.append(String.format("Found multiple EnsemblIDs for gene: %s, using %s.<br>",gene,ensemblName));
    		} else {
    			ensemblName = extIdFinal.get(0);

    		}
    	}
    	
    	
		String ensemblName = extIdFinal.get(0);
    	HashMap<String, Gene> geneNameGene = genome.getTranscriptomes()[0].getGeneNameGene();
    	
    	
    	if (geneNameGene.containsKey(ensemblName)) {
    		alreadyObserved.put(gene,geneNameGene.get(ensemblName));
    		return geneNameGene.get(ensemblName);
    	} else {
    		//this.warningMessage.append(String.format("Found EnsemblID %s for gene %s in BiominerDB, but could not find EnsemblID in transcript file.<br>",ensemblName,gene));
    		alreadyObserved.put(gene, null);
    		return null;
    	}
		
	}

}
