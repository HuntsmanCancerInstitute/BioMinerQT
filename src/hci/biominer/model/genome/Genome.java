package hci.biominer.model.genome;

import hci.biominer.parser.GenomeParser;
import java.util.HashMap;

public class Genome {
	//fields
	private String name;
	private String speciesName;
	private Transcriptome[] transcriptomes;
	private HashMap<String, Integer> chrNameLength = new HashMap<String, Integer>();
	
	//constructors
	public Genome (GenomeParser gp){
		this.name = gp.getGenomeBuildName();
		this.speciesName = gp.getSpecies();
		this.chrNameLength = gp.getChrNameLength();
	}

	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SpeciesName:\t"+ speciesName); sb.append("\n");
		sb.append("GenomeName:\t"+ name); sb.append("\n");
		sb.append("ChromosomeName Length Hash:\t"+chrNameLength); sb.append("\n");
		for (Transcriptome t: transcriptomes){
			sb.append("\nTranscriptome:\n");
			sb.append(t);  sb.append("\n");
		}
		return sb.toString();
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

	//getters and setters
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



	
}


