package hci.biominer.parser;

import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcriptome;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class GenomeParser {
	
	//fields
	private Genome genome;
	
	public GenomeParser(File desc) throws Exception {
		parseDescriptorFile(desc);
		makeTranscriptomes();
	}

	private void makeTranscriptomes() throws Exception {
		//get name file containers and null them so they are regenerated
		Transcriptome[] ts = genome.getTranscriptomes();
		genome.setTranscriptomes(null);
		if (ts.length == 0) return;
		for (int i=0; i< ts.length; i++){
			TranscriptomeParser tp = new TranscriptomeParser(ts[i].getName(), ts[i].getSourceFile(), genome);
		}
	}
	

	private void parseDescriptorFile(File desc) throws Exception{
		genome = new Genome();
		BufferedReader in = ModelUtil.fetchBufferedReader(desc);
		String line;
		
		//species
		while ((line = in.readLine()) != null) if (line.length() !=0 && line.startsWith("#") == false) break;
		String[] tokens = ModelUtil.TAB.split(line);
		if (tokens.length !=2 || tokens[0].equals("Species") == false) throw new Exception("Error: could not parse species name from "+line+", aborting.\n");
		genome.setSpeciesName(tokens[1]);
		
		//name
		while ((line = in.readLine()) != null) if (line.length() !=0 && line.startsWith("#") == false) break;
		tokens = ModelUtil.TAB.split(line);
		if (tokens.length !=2 || tokens[0].equals("BuildName") == false) throw new Exception("Error: could not parse build name from "+line+", aborting.\n");
		genome.setName(tokens[1]);
		
		//transcripts, just create empty containers with the name and file
		boolean foundTranscriptomeName = false;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0 || line.startsWith("#")) continue;
			if (line.startsWith("TranscriptomeName")) {
				foundTranscriptomeName = true;
				break;
			}
		}
		if (foundTranscriptomeName == false) throw new Exception("Error: could not find the TranscriptomeName line, aborting.\n");
		ArrayList<Transcriptome> transAL = new ArrayList<Transcriptome>();
		boolean chromLengthFound = false;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0 || line.startsWith("#")) continue;
			if (line.startsWith("ChromosomeLength")) {
				chromLengthFound = true;
				break;
			}
			tokens = ModelUtil.TAB.split(line);
			if (tokens.length != 2) throw new Exception("Error: could not transcriptome name and file from "+line+", aborting.\n");
			String transName = tokens[0];
			File transFile = new File(tokens[1]);
			if (transFile.canRead() == false) throw new Exception("Error: could not read the transcriptome file in "+line+", aborting.\n");
			transAL.add(new Transcriptome(transName, transFile));
		}
		Transcriptome[] transcriptomes = new Transcriptome[transAL.size()];
		transAL.toArray(transcriptomes);
		genome.setTranscriptomes(transcriptomes);
		
		//chromosome length and aliases 
		if (chromLengthFound == false) throw new Exception("Error: could not find the ChromosomeLength line, aborting.\n");
		LinkedHashMap<String, Chromosome> nameChromosome = new LinkedHashMap<String, Chromosome>();
		
		while ((line = in.readLine()) != null){
			if (line.length() == 0 || line.startsWith("#")) continue;
			tokens = ModelUtil.TAB.split(line);
			//length, add 1 to bring to interbase coordinates?
			Integer length = Integer.parseInt(tokens[0]) +1;
			//names, the first is used as the primary name
			String name = tokens[1];
			//does it exist?
			if (nameChromosome.containsKey(name)) throw new Exception("Error: duplicate chromosome name encountered in "+line+",aborting.\n");
			//create Chromosome
			Chromosome chromosome = new Chromosome(length, name, null);
			//add it
			nameChromosome.put(name, chromosome);
			//fetch aliases
			String[] aliases = new String[tokens.length - 2];
			int j = 0;
			for (int i=2; i< tokens.length; i++){
				//look to see if it is already present
				if (nameChromosome.containsKey(tokens[i])) throw new Exception("Error: duplicate chromosome name encountered in "+line+",aborting.\n");
				nameChromosome.put(tokens[i], chromosome);
				aliases[j++] = tokens[i];
			}
			//add aliases
			chromosome.setAliases(aliases);
		}
		genome.setNameChromosome(nameChromosome);
	}

	public Genome getGenome() {
		return genome;
	}
}
