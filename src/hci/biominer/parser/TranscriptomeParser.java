package hci.biominer.parser;

import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcriptome;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TranscriptomeParser {

	//fields
	private File refflatSource;
	private Gene[] genes;
	private HashMap<String, Integer> chrNameLength;

	//constructors
	public TranscriptomeParser(File refflatSource, Genome genome) throws Exception{
		this.refflatSource = refflatSource;
		this.chrNameLength = genome.getChrNameLength();
		parseRefFlat();
	}
	
	/**Populates a Transcriptome container, name and description can be null.*/
	public Transcriptome makeTranscriptome(String name, String description){
		Transcriptome t = new Transcriptome();
		t.setRefflatSource(refflatSource);
		t.setGenes(genes);
		t.setName(name);
		t.setDescription(description);
		return t;
	}

	/** Parses a UCSC refflat formated gene line with geneName and transcriptName in first two columns: 
	 * ENSG00000230759	ENSTENSG00000220751	chr1	+	103957500	103968087	103968087	103968087	2	103957500,103967726	103957557,103968087 
	 * Assumes file is sorted by geneName.*/
	public void parseRefFlat() throws Exception{
		BufferedReader in;
		String line = null;
		ArrayList<String[]> transcriptsAL = new ArrayList<String[]>();
		ArrayList<Gene> genesAL = new ArrayList<Gene>();
		String oldGeneName = null;
		HashSet<String> parsedGeneNames = new HashSet<String>();
		//fetch reader, gz/zip OK
		in = ModelUtil.fetchBufferedReader(refflatSource);
		//for each line in the file, collect set with same gene name
		while ((line = in.readLine()) != null){
			line = line.trim();
			if (line.length() == 0 || line.startsWith("#")) continue;
			String[] tokens = line.split("\\t");
			if (tokens.length != 11) throw new Exception ("\nError: incorrect number of tokens "+tokens.length);				
			if (oldGeneName != null){
				if (oldGeneName.equals(tokens[0]) == false) {
					//has it been seen before?
					if (parsedGeneNames.contains(oldGeneName)) throw new Exception ("\nError: file does not appear to be sorted by geneName see "+ oldGeneName);
					parsedGeneNames.add(oldGeneName);
					genesAL.add(Gene.makeGene(transcriptsAL, chrNameLength));
					oldGeneName = tokens[0];
					transcriptsAL.clear();
				}
			} 
			else oldGeneName = tokens[0];
			transcriptsAL.add(tokens);

		}
		in.close();
		//add last and make array
		genesAL.add(Gene.makeGene(transcriptsAL, chrNameLength));
		genes = new Gene[genesAL.size()];
		genesAL.toArray(genes);
	}
	
	public Gene[] getGenes() {
		return genes;
	}
}
