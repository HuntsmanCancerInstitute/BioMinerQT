package hci.biominer.model.genome;

import java.io.File;

public class Transcriptome {
	
	//fields
	private String name;
	private String description;
	private Gene[] genes;
	private File refflatSource;
	
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
