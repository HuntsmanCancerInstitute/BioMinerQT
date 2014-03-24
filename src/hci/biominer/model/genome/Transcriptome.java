package hci.biominer.model.genome;

import java.io.File;

public class Transcriptome {
	
	//fields
	private String name;
	private Gene[] genes;
	private File sourceFile;
	
	//constructors
	public Transcriptome() {}
	
	public Transcriptome(String transName, File transFile) {
		this.name = transName;
		this.sourceFile = transFile;
	}

	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TranscriptomeName:\t"+ name); sb.append("\n");
		sb.append("TranscriptomeFile:\t"+ sourceFile); sb.append("\n");
		sb.append("NumberGenes:\t"+ genes.length); sb.append("\n");
		sb.append("\nFirst 5:\n"); 
		for (int i=0; i< 5; i++) sb.append(genes[i]);
		return sb.toString();
	}
	
	//getters and setters
	public String getName() {
		return name;
	}
	public Gene[] getGenes() {
		return genes;
	}
	public void setGenes(Gene[] genes) {
		this.genes = genes;
	}
	public File getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public void setName(String name) {
		this.name = name;
	}
}
