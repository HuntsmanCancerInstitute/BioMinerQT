package hci.biominer.parser;

import hci.biominer.model.GenericResult;
import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.util.Enumerated.VarLocationEnum;
import hci.biominer.util.Enumerated.VarTypeEnum;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class VCFIntervalTreeParserV2 {
	private HashMap<String,IntervalTree<GenericResult>> invlTree = null;
	private File vcfFile = null;
	private Genome genome = null;
	private int numParsedLines = 0;

	public VCFIntervalTreeParserV2(File vcfFile, Genome genome) throws Exception{
		if (!vcfFile.canRead()) {
			throw new Exception(String.format("Specified VCF file does not exist or can't be read: %s",this.vcfFile.getAbsolutePath()));
		}
		this.vcfFile = vcfFile;
		this.genome = genome;
	}
	
	public void parseVcf() throws Exception{
		BufferedReader br = ModelUtil.fetchBufferedReader(this.vcfFile);
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
				String reference = items[3];
				String alternate = items[4];
				Integer wild = Integer.parseInt(items[5]);
				Integer het = Integer.parseInt(items[6]);
				Integer mut = Integer.parseInt(items[7]);
				Integer other = Integer.parseInt(items[8]);
				String originalName = items[9];
				String mappedName = items[10];
				String dbSNP = items[13];
				
				VarTypeEnum varType = null;
				VarLocationEnum varLocation = null;
				
				if (!items[11].equals("null")) {
					varType = VarTypeEnum.valueOf(items[11]);
				}
				
				if (!items[12].equals("null")) {
					varLocation = VarLocationEnum.valueOf(items[12]);
				}
				
				
				
				//Create intervals
				GenericResult gr = new GenericResult();
				gr.loadVariantData(chrom, start, end, reference, alternate, wild, het, mut, other, originalName, mappedName, varType, varLocation, dbSNP);
				
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
	
	public File getVcfFile() {
		return this.vcfFile;
	}
	
	public int getNumberParsedLines() {
		return this.numParsedLines;
	}
	
	public HashMap<String,IntervalTree<GenericResult>> getChromNameIntervalTree() throws Exception {
		if (invlTree == null) {
			this.parseVcf();
		}
		return this.invlTree;
	}
}
