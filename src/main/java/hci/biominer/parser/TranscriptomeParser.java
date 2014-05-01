package hci.biominer.parser;

import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcriptome;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class TranscriptomeParser {

	//fields
	Transcriptome transcriptome;

	//constructors
	public TranscriptomeParser(String name, File refflatSource, Genome genome) throws Exception{
		parseRefFlat(name, refflatSource, genome);
	}

	/** Parses a UCSC refflat formated gene line with geneName and transcriptName in first two columns: 
	 * ENSG00000230759	ENSTENSG00000220751	chr1	+	103957500	103968087	103968087	103968087	2	103957500,103967726	103957557,103968087 
	 * Assumes file is sorted by geneName.
	 * Adds the transcriptome to the Genome.*/
	public void parseRefFlat(String name, File refflatSource, Genome genome) throws Exception{
		transcriptome = new Transcriptome();
		transcriptome.setSourceFile(refflatSource);
		transcriptome.setName(name);
		transcriptome.setGenome(genome);
		LinkedHashMap<String, Chromosome> nameChromosome = genome.getNameChromosome();
		
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
					genesAL.add(Gene.makeGene(transcriptsAL, nameChromosome));
					oldGeneName = tokens[0];
					transcriptsAL.clear();
				}
			} 
			else oldGeneName = tokens[0];
			transcriptsAL.add(tokens);

		}
		in.close();
		//add last and make array
		genesAL.add(Gene.makeGene(transcriptsAL, nameChromosome));
		Gene[] genes = new Gene[genesAL.size()];
		genesAL.toArray(genes);
		transcriptome.setGenes(genes);
		
		//add to genome
		genome.addTranscriptome(transcriptome);
	}

	public Transcriptome getTranscriptome() {
		return transcriptome;
	}
}
