package hci.biominer.parser;

import hci.biominer.model.AnalysisType;
import hci.biominer.model.GenericResult;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Genome;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.Enumerated;

public class ChipIntervalTreeParser {
	private HashMap<String,IntervalTree<GenericResult>> invlTree = null;
	private File chipFile = null;
	private Genome genome = null;
	private int numParsedLines = 0;

	public ChipIntervalTreeParser(File chipFile, Genome genome) throws Exception{
		if (!chipFile.canRead()) {
			throw new Exception(String.format("Specified ChIP file does not exist or can't be read: %s",this.chipFile.getAbsolutePath()));
		}
		this.chipFile = chipFile;
		this.genome = genome;
	}
	
	public void parseChip() throws Exception{
		BufferedReader br = ModelUtil.fetchBufferedReader(this.chipFile);
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
				String fdr = items[3];
				float log = Float.parseFloat(items[4]);
				
				//Create intervals
				GenericResult gr = new GenericResult();
				gr.loadChipData(chrom,start,end,fdr,log, Enumerated.AnalysisTypeEnum.ChIPSeq);
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
	
	public File getChipFile() {
		return this.chipFile;
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
