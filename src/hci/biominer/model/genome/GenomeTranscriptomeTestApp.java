package hci.biominer.model.genome;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.VCFIntervalTreeParser;
import hci.biominer.util.ModelUtil;

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
			System.out.println("Loading vcf file...");
			File vcfFile = new File("/Users/u0028003/Code/BioMiner/TestDataSets/clinvar_00-latest.vcf.gz");
			VCFIntervalTreeParser vcfITP = new VCFIntervalTreeParser(vcfFile, genome);
			vcfITP.getChromNameIntervalTree(); //triggers parsing
			System.out.println(vcfITP);
			
			//fetch vcf records over BRCA1
			System.out.println("Searching vcf IntervalTree...");
			HashMap<String, IntervalTree<String>> its = vcfITP.getChromNameIntervalTree();
			IntervalTree<String> chr17It = its.get("17");
			Region brca1 = new Region(41196313, 41277468);
			ArrayList<String> vcfLines = chr17It.search(brca1.getStart(), brca1.getStop());
			System.out.println("BRCA1 clinvar variants: "+vcfLines.size());
			String res = ModelUtil.arrayListToString(vcfLines, "\n");
			//print to screen
			System.out.println(res);
			
			
			
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
