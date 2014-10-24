package hci.biominer.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.springframework.beans.factory.annotation.Autowired;

import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcript;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.util.ColumnValidators;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.ModelUtil;

public class RnaSeqParser {
	private int geneColumn = -1;
	private int fdrColumn = -1;
	private int logColumn = -1;
	private int colMax = -1;
	private File inputFile = null;
	private File outputFile = null;
	private boolean isConverted = false;
	private ArrayList<String> parsedData = null;
	private StringBuilder warningMessage = new StringBuilder("");
	private OrganismBuild ob = null;
	private Genome genome = null;
	private HashMap<String,ArrayList<Long>> externalGeneToBiominerId;
	private HashMap<Long,String> biominerIdToEnsembl;
	
 
	
	/* 
	 * Constructor for internal call. Assumes 0-based indexes.
	 */
	public RnaSeqParser(File inputFile, File outputFile, Integer geneColumn, Integer fdrColumn, 
			Integer logColumn, boolean isConverted, OrganismBuild ob, List<ExternalGene> egList) throws Exception {
				
		if (!inputFile.exists()) {
			throw new Exception("[RnaSeqParser] Specified input file does not exist");
		}
		
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.geneColumn = geneColumn;
		this.fdrColumn = fdrColumn;
		this.logColumn = logColumn;
		this.isConverted = isConverted;
		this.ob = ob;
		
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
		
		try {
			this.genome = this.fetchGenome(ob);
		} catch (Exception ex) {
			throw ex;
		}
	
	
		//Slurp header for number of columns:
		Integer[] colsToCheck = new Integer[]{this.geneColumn,this.fdrColumn,this.logColumn};
		String[] colNames = new String[] {"Gene Column","FDR Column","Log Column"};
		colMax = ColumnValidators.validateColumns(colsToCheck, colNames, this.inputFile);
		
		try {
			this.run();
		} catch(Exception ex) {
			//System.out.println(ex);
			throw ex;
		}
	}
	
	
	/*
	 * Main workflow
	 */
	public String run() throws Exception {
		System.out.println("[RnaSeqParser] Processing input file...");
		this.processData();
		
		//Write output
		System.out.println("[RnaSeqParser] Writing output file...");
		this.writeData();
		
		System.out.println("[RnaSeqParser] All done!\n");
		
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
			throw new IOException(String.format("[RnaSeqParser] Error writing to output file file: %s.",ioex.getMessage()));
		} finally {
			try {
				bw.close();
			} catch (Exception e){}
		}
	}
	
	/*
	 * Parse and validate Chip file
	 */
	private void processData() throws Exception {
		BufferedReader br = null;
		try {
			//Open file handle
			br = ModelUtil.fetchBufferedReader(this.inputFile);
			
			//Slurp the header
			br.readLine();
			
			//Initialize input file variables
			this.parsedData = new ArrayList<String>();
			String line = null;
			int lineCount = 1;
			
			//Initialize warning variables
			boolean allTransformedFdrLessThanOne = true;
			if (!this.isConverted) {
				allTransformedFdrLessThanOne = false;
			}
			boolean allLog2RatioPos = true;
			boolean allLog2RatioNeg = true;
			
			NumberFormat formatter = new DecimalFormat("0.##E0");
			
			boolean badGene = false;
			int badGeneCount = 0;
			int allGeneCount = 0;
			
			while ((line = br.readLine()) != null) {
				allGeneCount++;
				String[] parts = line.split("\t");
				
				//Initialize line variables
				double tempFdr = -1;
				float tempLog = -1;
				
				//Make sure the line length matches header. Parsing might go OK if this fails, but I 
				//think it's best to error out when the file is at all malformed.
				if (parts.length != this.colMax) {
					throw new Exception(String.format("[RnaSeqParser] The number of columns in row %d ( %d ) doesn't match the header ( %d )\n",
							lineCount,parts.length,this.colMax));
				}
				

				//Parse chromsome, check to make sure it's recognizable
				String geneName = parts[this.geneColumn];
				
				Gene geneObject = this.checkGene(geneName);
				
				if (geneObject == null) {
					badGeneCount++;
					if (!badGene) {
						badGene = true;
						String body = this.warningMessage.toString();
						this.warningMessage = new StringBuilder("The RNASeq dataset contained genes that were not found in Biominer's annotation. "
								+ "Results associated with the following genes can't be searched:<br>");
						
						this.warningMessage.append(body);
					}
					continue;
				}
				
		
				//Parse FDR
				tempFdr = ColumnValidators.validateFdr(parts[this.fdrColumn], this.isConverted);
					
				if (this.isConverted && tempFdr > 1) {
					allTransformedFdrLessThanOne = false;
				}
				
				if (this.isConverted) {
					double decimal = tempFdr / -10;
					tempFdr = Math.pow(10, decimal);
				}
				
				String stringFdr = formatter.format(tempFdr);
				
				//Parse LogRatio
				tempLog = ColumnValidators.validateLog2Ratio(parts[this.logColumn]);
				
				if (tempLog > 0) {
					allLog2RatioNeg = false;
				} else if (tempLog < 0) {
					allLog2RatioPos = false;
				}
				
				Transcript t = geneObject.getMergedTranscript();
				String mappedName = geneObject.getName();
				String chromName = t.getChrom();
				int geneStart = t.getTxStart();
				int geneEnd = t.getTxEnd();
				
				//Create output string and add to data list
				String outputFile = String.format("%s\t%d\t%d\t%s\t%s\t%s\t%f\n",chromName, geneStart, geneEnd, geneName, mappedName, stringFdr, tempLog);
				this.parsedData.add(outputFile);
				
				lineCount++;
			}
			
			if (badGene) {
				this.warningMessage.append(String.format("Failed to process %d RNASeq records of %d.<br>",badGeneCount,allGeneCount));
			}
			
			//Throw failure message or warning messages
		    if (allTransformedFdrLessThanOne) {
		    	this.warningMessage.append("[RnaSeqParser] WARNING: FDR formatting style was set as transformed, but all values were between 0 and 1.  Are you sure the FDR values were transformed?<br> "
						+ "FDR values are transformed using the formula -10 * log10(FDR)");		
			} else if (allLog2RatioNeg) {
				this.warningMessage.append("[RnaSeqParser] WARNING: All log2ratios were less than or equal to zero.  Are you sure the log2Ratios are formatted correctly?<br>");
			} else if (allLog2RatioPos) {
				this.warningMessage.append("[RnaSeqParser] WARNING: All log2ratios were greater than or equal to zero.  Are you sure the log2Ratios are formatted correctly?<br>");
			}
			
		} catch (IOException ioex) {
			throw new IOException(String.format("[RnaSeqParser] Error processing data file: %s.",ioex.getMessage()));
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
		List<Long> exIdList;
		if (this.externalGeneToBiominerId.containsKey(gene)) {
			exIdList = this.externalGeneToBiominerId.get(gene);
		} else {
			this.warningMessage.append(String.format("Could not find a BiominerID for gene: %s in the BiominerDB.<br>", gene));
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
    		this.warningMessage.append(String.format("Could not find an EnsemblID for gene: %s in the BiominerDB.<br>",gene));
    		return null;
    	} else if (extIdFinal.size() > 1) {
    		String ensemblName = extIdFinal.get(0);
    		this.warningMessage.append(String.format("Found multiple EnsemblIDs for gene: %s, using %s.<br>",gene,ensemblName));
    	}
    	
    	
		String ensemblName = extIdFinal.get(0);
    	HashMap<String, Gene> geneNameGene = genome.getTranscriptomes()[0].getGeneNameGene();
    	
    	
    	if (geneNameGene.containsKey(ensemblName)) {
    		return geneNameGene.get(ensemblName);
    	} else {
    		this.warningMessage.append(String.format("Found EnsemblID %s for gene %s in BiominerDB, but could not find EnsemblID in transcript file.<br>",ensemblName,gene));
    		return null;
    	}
		
	}
	
	private Genome fetchGenome(OrganismBuild ob) throws Exception {
    	try {
    		if (!GenomeBuilds.doesGenomeExist(ob)) {
        		GenomeBuilds.loadGenome(ob);
        	} 
        	return GenomeBuilds.getGenome(ob);
    	} catch (Exception ex) {
    		throw ex;
    	}
    }
	



}
