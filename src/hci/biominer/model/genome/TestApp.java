package hci.biominer.model.genome;

import java.io.File;

import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.TranscriptomeParser;

public class TestApp {

	public static void main(String[] args) {
		try {
			//create a genome from a descriptor file
			File descriptorFile = new File (args[0]);
			GenomeParser gp = new GenomeParser (descriptorFile);
			Genome genome = new Genome (gp);
			
			//add a transcriptome,
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
