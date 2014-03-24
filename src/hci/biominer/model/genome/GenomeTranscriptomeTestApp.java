package hci.biominer.model.genome;

import java.io.File;

import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.VCFIntervalTreeParser;

public class GenomeTranscriptomeTestApp {

	public static void main(String[] args) {
		try {
			//create a genome from a descriptor file
			System.out.println("Loading genome...");
			File descriptorFile = new File ("/Users/u0028003/Code/BioMiner/AnnotationFiles/hg19_GRCh37_Genome.txt");
			GenomeParser gp = new GenomeParser (descriptorFile);
			Genome genome = gp.getGenome();
			System.out.println(genome);
			
			//load a vcf file
			File vcfFile = new File("/Users/u0028003/Code/BioMiner/TestDataSets/clinvar_00-latest.vcf.gz");
			VCFIntervalTreeParser vcfITP = new VCFIntervalTreeParser(vcfFile, genome);
			vcfITP.getChromNameIntervalTree(); //triggers parsing
			System.out.println(vcfITP);
			
			
			
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
