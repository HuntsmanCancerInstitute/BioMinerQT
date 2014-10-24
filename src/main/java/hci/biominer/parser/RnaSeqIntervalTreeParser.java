package hci.biominer.parser;

import hci.biominer.model.GenericResult;
import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.util.ModelUtil;

import hci.biominer.util.Enumerated;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RnaSeqIntervalTreeParser {
	private HashMap<String,IntervalTree<GenericResult>> invlTree = null;
	private File rnaSeqFile = null;
	private Genome genome = null;
	private int numParsedLines = 0;

	public RnaSeqIntervalTreeParser(File rnaSeqFile, Genome genome) throws Exception{
		if (!rnaSeqFile.canRead()) {
			throw new Exception(String.format("Specified RNASeq file does not exist or can't be read: %s",this.rnaSeqFile.getAbsolutePath()));
		}
		this.rnaSeqFile = rnaSeqFile;
		this.genome = genome;
	}
	
	public void parseChip() throws Exception{
		BufferedReader br = ModelUtil.fetchBufferedReader(this.rnaSeqFile);
		HashMap<String,ArrayList<Interval<GenericResult>>> invlHash = new HashMap<String,ArrayList<Interval<GenericResult>>>();
		LinkedHashMap<String, Chromosome> chromosomeName = genome.getNameChromosome();
		
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\t");
				
				//Parse values
				String chrom = items[0];
				if (chromosomeName.get(chrom) == null) {
					throw new Exception(String.format("Chromosome can't be found the genome: %s",chrom));
				}
				int start = Integer.parseInt(items[1]);
				int end = Integer.parseInt(items[2]);
				String originalName = items[3];
				String parsedName = items[4];
				String fdr = items[5];
				float log = Float.parseFloat(items[6]);
				
				//Create intervals
				GenericResult gr = new GenericResult();
				gr.loadRnaseqData(chrom, start, end, originalName, parsedName, fdr,log, Enumerated.AnalysisTypeEnum.RNASeq);
				
				Interval<GenericResult> invl = new Interval<GenericResult>(start,end,gr); 
				
				//Add to hash
				if (!invlHash.containsKey(chrom)) {
					invlHash.put(chrom, new ArrayList<Interval<GenericResult>>());
				}
				
				invlHash.get(chrom).add(invl);
				this.numParsedLines += 1;
			}
		} catch (IOException ioex) {
			throw ioex;
		} finally {
			try {
				br.close();
			} catch (Exception ex) {};
		}
		
		invlTree = new HashMap<String,IntervalTree<GenericResult>>();
		for (String chrom: invlHash.keySet()) {
			Chromosome chromObj = chromosomeName.get(chrom);
			for (String alias: chromObj.getAliases()) {
				this.invlTree.put(alias,new IntervalTree<GenericResult>(invlHash.get(chrom),false));
			}
		}
	}
	
	public File getRnaSeqFile() {
		return this.rnaSeqFile;
	}
	
	public int getNumberParsedLines() {
		return this.numParsedLines;
	}
	
	public HashMap<String,IntervalTree<GenericResult>> getChromNameIntervalTree() throws Exception {
		if (invlTree == null) {
			this.parseChip();
		}
		return this.invlTree;
	}
}
