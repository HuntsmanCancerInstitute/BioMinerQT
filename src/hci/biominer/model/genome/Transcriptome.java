package hci.biominer.model.genome;

import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class Transcriptome {
	
	//fields
	private String name;
	private String description;
	private Genome parent;
	private File transcriptomeSource;
	private ChromosomeGene[] chromosomeGenes;
	private Gene[] genes;
	
	//constructors
	public Transcriptome (File source){
		transcriptomeSource = source;
		parseRefFlat();
	}
	
	/** Parses a UCSC refflat formated gene line with geneName and transcriptName in first two columns: 
	 * ENSG00000230759	ENSTENSG00000220751	chr1	+	103957500	103968087	103968087	103968087	2	103957500,103967726	103957557,103968087 
	 * Assumes file is sorted by geneName.*/
	public void parseRefFlat(){
		BufferedReader in;
		String line = null;
		ArrayList<String[]> transcriptsAL = new ArrayList<String[]>();
		ArrayList<Gene> genesAL = new ArrayList<Gene>();
		String oldGeneName = null;
		HashSet<String> parsedGeneNames = new HashSet<String>();
		try{
			//fetch reader, gz/zip OK
			in = ModelUtil.fetchBufferedReader(transcriptomeSource);
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
						genesAL.add(Gene.makeGene(transcriptsAL));
						oldGeneName = tokens[0];
						transcriptsAL.clear();
					}
				} 
				else oldGeneName = tokens[0];
				transcriptsAL.add(tokens);
				
			}
			in.close();
			//add last and make array
			genesAL.add(Gene.makeGene(transcriptsAL));
			genes = new Gene[genesAL.size()];
			genesAL.toArray(genes);
		
		} catch (Exception e){
			System.err.println("\nProblem parsing "+line);
			e.printStackTrace();
		}
	}
	

	
	public static void main (String[] args){
		Transcriptome t = new Transcriptome (new File (args[0]));
		for (Gene g: t.getGenes()) {
			System.out.println(g);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Genome getParent() {
		return parent;
	}

	public void setParent(Genome parent) {
		this.parent = parent;
	}

	public File getTranscriptomeSource() {
		return transcriptomeSource;
	}

	public void setTranscriptomeSource(File transcriptomeSource) {
		this.transcriptomeSource = transcriptomeSource;
	}

	public ChromosomeGene[] getChromosomeGenes() {
		return chromosomeGenes;
	}

	public void setChromosomeGenes(ChromosomeGene[] chromosomeGenes) {
		this.chromosomeGenes = chromosomeGenes;
	}

	public Gene[] getGenes() {
		return genes;
	}

	public void setGenes(Gene[] genes) {
		this.genes = genes;
	}
}
