package hci.biominer.model.genome;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.parser.GenomeParser;
import hci.biominer.parser.VCFIntervalTreeParser;
import hci.biominer.parser.ChipParser;
import hci.biominer.parser.ChipIntervalTreeParser;
import hci.biominer.model.chip.ChipIntervalTreeSerialized;
import hci.biominer.model.chip.Chip;
import hci.biominer.util.ModelUtil;

public class GenomeTranscriptomeTestApp {

	public static void main(String[] args) {
		try {
			//create a genome from a descriptor file
			//File localDir = new File("/Users/timmosbruger/Documents/Projects/AE/ChIP/");
			File localDir = new File("/Users/u0028003/Code/BioMiner");
			
			System.out.println("Loading genome...");
			File descriptorFile = new File (localDir,"/AnnotationFiles/hg19_GRCh37_Genome.txt");
			GenomeParser gp = new GenomeParser (descriptorFile,null);
			Genome genome = gp.getGenome();
			//System.out.println(genome);
			
			//fetch genes intersecting several regions near ENSG00000164197
			testGeneSearchIntervalTree(genome);
			
			System.exit(0);

			//load a vcf file
			System.out.println("Loading vcf file...");
			File vcfFile = new File(localDir,"TestDataSets/clinvar_00-latest.vcf.gz");
			VCFIntervalTreeParser vcfITP = new VCFIntervalTreeParser(vcfFile, genome);
			vcfITP.getChromNameIntervalTree(); //triggers parsing
			//System.out.println(vcfITP);
			
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
			
			Timer timer = new Timer();
			//Load a Chip File
			
			System.out.println("[TEST] Parsing raw ChIP file...");
			timer.start();
			File chipFile = new File(localDir,"TestDataSets/EncodeMcfPol2.txt.gz");
			File chipParsed = new File(localDir,"Storage/EncodeMcfPol2_parsed.txt");
			File chipIT = new File(localDir,"Storage/EncodeMcfPol2_parsed.txt.ser");
			new ChipParser(chipFile, chipParsed, 0, 1, 2, 6, 7, true, genome); //parse raw file
			
			System.out.println("[TEST] Elapsed time: " + timer.stop());
			
			
			//Create intervalTree from flat file
			timer.start();
			System.out.println("[TEST] Creating Interval Tree from Parsed ChIP.. ");
			ChipIntervalTreeParser citp = new ChipIntervalTreeParser(chipParsed, genome);
			HashMap<String, IntervalTree<Chip>> chipItFull1 = citp.getChromNameIntervalTree();
			IntervalTree<Chip> chipItChr171 = chipItFull1.get("17");
			System.out.println("[TEST] Elapsed time: " + timer.stop());
			
			
			//Load serialized Chip object
			timer.start();
			System.out.println("[TEST] Loading serialized interval tree.. ");
			HashMap<String, IntervalTree<Chip>> chipItFull2 = ChipIntervalTreeSerialized.getSerializedTree(chipIT);
			IntervalTree<Chip> chipItChr172 = chipItFull2.get("17");
			System.out.println("[TEST] Elapsed time: " + timer.stop());
			
			//fetch Chip over BRCA1
			timer.start();
			System.out.println("[TEST] Searching Chip IntervalTree...");
			ArrayList<Chip> chipLines1 = chipItChr171.search(brca1.getStart(), brca1.getStop());
			System.out.println("[TEST] Elapsed time: " + timer.stop());
			
			ArrayList<Chip> chipLines2 = chipItChr172.search(brca1.getStart(), brca1.getStop());
			
			for (int i=0;i<chipLines1.size();i++) {
				String out1 = chipLines1.get(i).toString();
				String out2 = chipLines2.get(i).toString();
				if (!out1.equals(out2)) {
					System.out.println(String.format("Lines don't match: %s vs %s",out1,out2));
				}
			}
			
			System.out.println("[TEST] Mcf7Pol2 Chip regions intersecting BRCA1 regions: "+chipLines1.size());
			String chipRes = ModelUtil.arrayListToString(chipLines1, "\n");
			System.out.println(chipRes);
			
			
			//Test done
			System.out.println("[TEST] Finished Test");
			
			
			
		} catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**Does lookup for genes that intersect several regions on chr5.*/
	private static void testGeneSearchIntervalTree(Genome genome) {
		String[] type = {"internal", "overlapping", "5\'", "3\'"};
		Region[] regions = new Region[4];
		regions[0] = new Region(63653148, 63653215);
		regions[1] = new Region(63461635, 63461703);
		regions[2] = new Region(63461561, 63461629);
		regions[3] = new Region(63668763, 63668830);
		
		//direct overlap
		System.out.println("\nSearching for overlaping Genes");
		HashMap<String, IntervalTree<Gene>> geneITs = genome.getTranscriptomes()[0].getChromGeneIntervalTrees();
		IntervalTree<Gene> chr5IT = geneITs.get("chr5");
		//enable searching for neighbors if no intersection found
		chr5IT.setSearchForNeighbors(true);
		for (int i=0; i< type.length; i++){
			System.out.println(type[i]);
			ArrayList<Gene> genes = chr5IT.search(regions[i].getStart(), regions[i].getStop());
			for (Gene g : genes) System.out.println("\t"+g.getName()+"\t"+g.getMergedTranscript().distance(regions[i]));
			Gene left = chr5IT.getLeftNeighbor();
			if (left != null) System.out.println("\t5' of Region "+left.getName()+"\t"+left.getMergedTranscript().distance(regions[i]));
			Gene right = chr5IT.getRightNeighbor();
			if (right != null) System.out.println("\t3' of Region "+right.getName()+"\t"+right.getMergedTranscript().distance(regions[i]));
		}
		
		//now search using IT with 100KB +/- each gene
		System.out.println("\nSearching Genes +/- 100KB");
		chr5IT = genome.getTranscriptomes()[0].getChromGene100KIntervalTrees().get("chr5");
		//enable searching for neighbors if no intersection found
		chr5IT.setSearchForNeighbors(true);
		for (int i=0; i< type.length; i++){
			System.out.println(type[i]);
			ArrayList<Gene> genes = chr5IT.search(regions[i].getStart(), regions[i].getStop());
			for (Gene g : genes) System.out.println("\t"+g.getName()+"\t"+g.getMergedTranscript().distance(regions[i]));
			Gene left = chr5IT.getLeftNeighbor();
			if (left != null) System.out.println("\t5' of Region "+left.getName()+"\t"+left.getMergedTranscript().distance(regions[i]));
			Gene right = chr5IT.getRightNeighbor();
			if (right != null) System.out.println("\t3' of Region "+right.getName()+"\t"+right.getMergedTranscript().distance(regions[i]));
		}
		
		//search for genes with a TSS within 100kb of each region
		System.out.println("\nSearching Genes with TSS +/- 100KB");
		IntervalTree<Transcript> chr5ITTrans = genome.getTranscriptomes()[0].getChromTSS100KIntervalTrees().get("chr5");
		//enable searching for neighbors if no intersection found
		chr5ITTrans.setSearchForNeighbors(true);
		for (int i=0; i< type.length; i++){
			System.out.println(type[i]);
			ArrayList<Transcript> genes = chr5ITTrans.search(regions[i].getStart(), regions[i].getStop());
			for (Transcript g : genes) System.out.println("\t"+g.getTranscriptName()+"\t"+g.distanceToTSS(regions[i]));
			Transcript left = chr5ITTrans.getLeftNeighbor();
			if (left != null) System.out.println("\t5' of Region "+left.getTranscriptName()+"\t"+left.distanceToTSS(regions[i]));
			Transcript right = chr5ITTrans.getRightNeighbor();
			if (right != null) System.out.println("\t3' of Region "+right.getTranscriptName()+"\t"+right.distanceToTSS(regions[i]));
		}
		
	}
	
}

class Timer {
	private Date startTime = null;
	public Timer() {
		startTime = new Date();
	}

	public void start() {
		startTime = new Date();
	}
	
	public String stop() {
		Date endTime = new Date();
		float elapsed = (endTime.getTime() - startTime.getTime());
		return String.format("%.2f ms", elapsed);
		
	}
}
