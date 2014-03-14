package hci.biominer.model.genome;

import hci.biominer.parser.GenomeParser;
import java.util.HashMap;

public class Genome {
	private String name;
	private String speciesName;
	private Transcriptome[] transcriptomes;
	private HashMap<String, Integer> chrNameLength = new HashMap<String, Integer>();
	
	
	public Genome (GenomeParser gp){
		this.name = gp.getGenomeBuildName();
		this.speciesName = gp.getSpecies();
		this.chrNameLength = gp.getChrNameLength();
	}


	public String getName() {
		return name;
	}
	public String getSpeciesName() {
		return speciesName;
	}
	public Transcriptome[] getTranscriptomes() {
		return transcriptomes;
	}
	public HashMap<String, Integer> getChrNameLength() {
		return chrNameLength;
	}


	public void addTranscriptome(Transcriptome transcriptome) {
		if (transcriptomes == null){
			transcriptomes = new Transcriptome[1];
			transcriptomes[0] = transcriptome;
		}
		else {
			Transcriptome[] t = new Transcriptome[transcriptomes.length +1];
			for (int i=0; i< transcriptomes.length; i++) t[i] = transcriptomes[i];
			t[transcriptomes.length] = transcriptome;
			transcriptomes = t;
		}
		
		
	}
	
}


