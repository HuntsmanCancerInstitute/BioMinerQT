package hci.biominer.model.genome;

import java.io.File;

public class Transcriptome {
	
	//fields
	private String name;
	private String description;
	private Gene[] genes;
	private File refflatSource;
	
	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TranscriptomeName:\t"+ name); sb.append("\n");
		sb.append("TranscriptomeName:\t"+ description); sb.append("\n");
		sb.append("TranscriptomeName:\t"+ refflatSource); sb.append("\n");
		sb.append("NumberGenes:\t"+ genes.length); sb.append("\n");
		sb.append("First 5:\t"); sb.append("\n");
		for (int i=0; i< 5; i++){
			sb.append(genes[i]); sb.append("\n");
		}
		return sb.toString();
	}
	
	//getters and setters
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
	public Gene[] getGenes() {
		return genes;
	}
	public void setGenes(Gene[] genes) {
		this.genes = genes;
	}
	public File getRefflatSource() {
		return refflatSource;
	}
	public void setRefflatSource(File refflatSource) {
		this.refflatSource = refflatSource;
	}
}
