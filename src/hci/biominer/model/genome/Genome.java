package hci.biominer.model.genome;

import java.io.File;

public class Genome {
	private int genomeNameID;
	private String name;
	private String speciesName;
	private Transcriptome[] transcriptomes;
	private Chromosome[] chromosomes;
	
	public Genome (File genomeDescriptorFile){
		//complete dave!
	}
	
	public static void main (String[] args){
		new Genome(new File(args[0]));
	}
}


