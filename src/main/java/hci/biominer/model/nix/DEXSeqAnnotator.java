package hci.biominer.model.nix;

import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Region;
import hci.biominer.model.genome.Transcript;
import hci.biominer.parser.GenomeParser;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class DEXSeqAnnotator {

	//fields
	private double minAbsLog2Rto = 1.5;
	private double minAdjPval = 20;
	private Pattern plus = Pattern.compile("\\+");
	private HashMap<String, Gene> geneNameGene;
	private HashMap<String, Region[]> aggregateGeneNameRegion;
	private HashSet<String> diffSpliced = new HashSet<String>();
	private HashSet<String> diffSplicedCoding = new HashSet<String>();
	private HashSet<String> diffSpliced5Utr = new HashSet<String>();
	private HashSet<String> diffSpliced3Utr = new HashSet<String>();


	public DEXSeqAnnotator(String[] dexSeqFiles){
		try {
			//create a aggregateGene : parts from the DEXSeq GTF file
			System.out.println("Loading gtf file...");
			File gtfFile = new File ("/Users/u0028003/HCI/Annotation/Hg19/GTF/Homo_sapiens.Hg19.norm.74.merged.gtf");
			loadGTFFile(gtfFile);

			//create a geneName : Gene from UCSC dataset
			System.out.println("Loading refFlat genome...");
			File descriptorFile = new File ("/Users/u0028003/Code/BioMiner/AnnotationFiles/hg19_GRCh37_Genome.txt");
			loadGenome(descriptorFile);

			//walk through each dexseq results file   fractionCoding fractionNonCoding tss tts
			System.out.println("\nStats for the number of genes and gene fusions displaying one or more exons:");
			for (String path : dexSeqFiles){
				File dexSeq = new File (path);
				annotate(dexSeq);
				printClearStats(dexSeq);
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}


	private void printClearStats(File f) {
		System.out.println(f.getName()+"\tName");
		System.out.println(diffSpliced.size()+"\tDiffSpliced");
		System.out.println(diffSplicedCoding.size()+"\tDiffSpliceCoding");
		System.out.println(diffSpliced5Utr.size()+"\tDiffSpliced5Utr");
		System.out.println(diffSpliced3Utr.size()+"\tDiffSpliced3Utr");
		System.out.println();
		
		diffSpliced.clear();
		diffSplicedCoding.clear();
		diffSpliced5Utr.clear();
		diffSpliced3Utr.clear();
	}


	private void annotate(File dexseqFile) throws Exception{

		BufferedReader in = ModelUtil.fetchBufferedReader(dexseqFile);
		PrintWriter out = new PrintWriter ( new FileWriter (new File(dexseqFile.getParentFile(), "anno_"+(int)minAdjPval+dexseqFile.getName())));
		String line;
		String[] tokens;
		String geneName = "";
		String chromStrand = null;
		Region[] exonParts = null;
		Region[] utr5 = null;
		Region[] utr3 = null;
		Region[] coding = null;
		short codingSeq;
		short utr3Seq;
		short utr5Seq;

		//skip header
		line = in.readLine();
		out.println("#Gene:ExonPart\t"+line+"\tChr\tStrand\tStart\tStop\tCoding\tUTR5\tUTR3");

		while ((line = in.readLine()) != null){
			//gene:Exon	geneID exonID dispersion pvalue padjust meanBase log2fold(MI/GV)
			//   0        1      2        3        4      5        6          7
			tokens = ModelUtil.TAB.split(line);

			//check thresholds
			if (tokens[7].equals("NA") || tokens[5].equals("NA")) continue;
			double log2Rto = Double.parseDouble(tokens[7]);
			if (Math.abs(log2Rto) < minAbsLog2Rto) continue;
			double adjP = -10 * Math.log10(Double.parseDouble(tokens[5]));
			if (adjP < minAdjPval) continue;

			//reset
			codingSeq = 0;
			utr3Seq = 0;
			utr5Seq = 0;

			//new geneName?
			if (tokens[1].equals(geneName) == false){
				geneName = tokens[1];
				diffSpliced.add(geneName);

				//fetch coordinates
				exonParts = aggregateGeneNameRegion.get(geneName);
				//watch out for more than one
				String[] geneNames = plus.split(geneName);

				//create ArrayLists
				ArrayList<Region> utr5Al = new ArrayList<Region>();
				ArrayList<Region> utr3Al = new ArrayList<Region>();
				ArrayList<Region> codingAl = new ArrayList<Region>();

				//add gene each
				for (int i=0; i< geneNames.length; i++){
					Gene gene = geneNameGene.get(geneNames[i]);
					if (gene == null)  ModelUtil.errorExit("Missing gene in UCSC file? "+geneNames[i]); 
					for (Region r: gene.get5Exons()) utr5Al.add(r);
					for (Region r: gene.get3Exons()) utr3Al.add(r);
					for (Transcript t: gene.getTranscripts()) codingAl.add(t.getCodingRegion());
					if (i==0) chromStrand = gene.getChromosome().getName()+"\t"+gene.getStrand();
				}

				utr5 = new Region[utr5Al.size()];
				utr5Al.toArray(utr5);
				utr3 = new Region[utr3Al.size()];
				utr3Al.toArray(utr3);
				coding = new Region[codingAl.size()];
				codingAl.toArray(coding);
			}

			int exonIndex = Integer.parseInt(tokens[2].substring(1)) -1;
			Region exonPart = exonParts[exonIndex];
			
			//scan
			for (Region r: coding){
				if (r.intersects(exonPart)){
					codingSeq = 1;
					diffSplicedCoding.add(geneName);
					break;
				}
			}
			for (Region r: utr5){
				if (r.intersects(exonPart)){
					utr5Seq = 1;
					diffSpliced5Utr.add(geneName);
					break;
				}
			}
			for (Region r: utr3){
				if (r.intersects(exonPart)){
					utr3Seq = 1;
					diffSpliced3Utr.add(geneName);
					break;
				}
			}
			//print line plus chrom strand start stop isCodingSeq is5Utr is3Utr
			out.println(line+"\t"+ chromStrand +"\t"+ exonParts[exonIndex] + "\t"+codingSeq+"\t"+utr5Seq+"\t"+utr3Seq);
		}
		in.close();
		out.close();
	}


	private void loadGenome(File descriptorFile) throws Exception{
		GenomeParser gp = new GenomeParser (descriptorFile);
		Genome genome = gp.getGenome();
		geneNameGene= genome.getTranscriptomes()[0].getGeneNameGene();
	}

	private void loadGTFFile(File gtfFile) throws Exception {
		aggregateGeneNameRegion = new HashMap<String, Region[]>();
		String[] tokens;
		String line;
		BufferedReader in = ModelUtil.fetchBufferedReader(gtfFile);
		ArrayList<Region> al = new ArrayList<Region>();
		String geneId = null;
		while ((line = in.readLine()) !=null ){
			tokens = ModelUtil.TAB.split(line);
			//start of new block?
			if (line.contains("aggregate_gene")){
				//create old
				if (geneId != null){
					Region[] ep = new Region[al.size()];
					al.toArray(ep);
					aggregateGeneNameRegion.put(geneId, ep);
					al.clear();
				}
				geneId = tokens[8].substring(9, tokens[8].length()-1);
			}
			//nope, add ExonPart
			else {
				Region r = new Region(Integer.parseInt(tokens[3])-1, Integer.parseInt(tokens[4]));
				al.add(r);
			}
		}
		//create last
		Region[] ep = new Region[al.size()];
		al.toArray(ep);
		aggregateGeneNameRegion.put(geneId, ep);

	}


	public static void main(String[] args) {
		new DEXSeqAnnotator(args);

	}

}
