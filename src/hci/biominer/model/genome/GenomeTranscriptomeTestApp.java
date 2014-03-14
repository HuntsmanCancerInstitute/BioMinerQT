package hci.biominer.model.genome;

import java.io.File;

import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.TranscriptomeParser;

public class GenomeTranscriptomeTestApp {

	public static void main(String[] args) {
		try {
			//create a genome from a descriptor file
			System.out.println("Loading genome...");
			File descriptorFile = new File ("/Users/u0028003/Code/BioMiner/AnnotationFiles/hg19_GRCh37_Genome.txt");
			GenomeParser gp = new GenomeParser (descriptorFile);
			Genome genome = new Genome (gp);
			
			//add a transcriptome,
			System.out.println("Loading transcriptomes...\n");
			String name = "EnsemblReleaseXXX";
			String description = "Refflat file downloaded from UCSC and reprocessed with XXX.";
			TranscriptomeParser tp = new TranscriptomeParser (gp.getTranscriptomeFiles()[0], genome);
			Transcriptome transcriptome = tp.makeTranscriptome(name, description);
			genome.addTranscriptome(transcriptome);
			
			System.out.println(genome);
			
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
