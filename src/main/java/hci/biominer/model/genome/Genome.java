package hci.biominer.model.genome;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Genome implements Serializable{
	//fields
	private String buildName;
	private String buildNamesAliases;
	private String speciesName;
	private Transcriptome[] transcriptomes;
	private LinkedHashMap<String, Chromosome> nameChromosome;
	private static final long serialVersionUID = 1L;
	
	//constructors
	public Genome (){}

	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SpeciesName:\t"+ speciesName); sb.append("\n");
		sb.append("GenomeBuildName:\t"+ buildName); sb.append("\n");
		sb.append("GenomeBuildNameAliases:\t"+ buildNamesAliases); sb.append("\n");
		sb.append("Chromosome Hash:\n");
		for (Chromosome c: nameChromosome.values()){
			sb.append(c.toString()); 
		}
		for (Transcriptome t: transcriptomes){
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
	public String getBuildName() {
		return buildName;
	}
	public String getSpeciesName() {
		return speciesName;
	}
	public Transcriptome[] getTranscriptomes() {
		return transcriptomes;
	}
	public LinkedHashMap<String, Chromosome> getNameChromosome() {
		return nameChromosome;
	}
	public void setBuildName(String name) {
		this.buildName = name;
	}
	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	public void setNameChromosome(LinkedHashMap<String, Chromosome> nameChromosome) {
		this.nameChromosome = nameChromosome;
	}

	public void setTranscriptomes(Transcriptome[] transcriptomes) {
		this.transcriptomes = transcriptomes;
	}
	public String getBuildNamesAliases() {
		return buildNamesAliases;
	}
	public void setBuildNamesAliases(String buildNamesAliases) {
		this.buildNamesAliases = buildNamesAliases;
	}

	
}


