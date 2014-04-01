package hci.biominer.parser;

import hci.biominer.model.genome.Chromosome;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.util.ModelUtil;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**Simple parser to load a vcf file into a chromID: IntervalTree HashMap.
 * Coordinates are converted to interbase!
 * Will not load file until calling getChromIdIntervalTree();
 * */
public class VCFIntervalTreeParser {

	//fields
	private File vcfFile;
	private ArrayList<String> header = new ArrayList<String>();
	private int numberParsedLines = 0;
	private HashMap<String, IntervalTree<String>> chromNameIntervalTree = null;
	private Genome genome;

	//indexs for ripping vcf records
	private static final int chromosomeIndex= 0;
	private static final int positionIndex = 1;
	private static final int referenceIndex = 3;
	private static final int alternateIndex = 4;

	//Constructors
	public VCFIntervalTreeParser(File vcfFile, Genome genome) throws Exception{
		this.vcfFile = vcfFile;
		this.genome = genome;
		if (vcfFile.canRead() == false) throw new Exception ("\nCould not read/ find this vcf file -> "+vcfFile);
	}

	//Methods
	/**Parses a vcf file into a HashMap of chromID: IntervalTree for rapid searching.
	 * 
	##fileformat=VCFv4.1
	....
	#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	carcinoid5	carcinoid6	carcinoid7	colon1	colon2	colon3	colon4
	chr1	54708	.	G	C	51.35	PASS	AC=3;AF=0.750;AN=4;BaseQRankSum=0.736;DP=3;Dels=0.00;FS=4.771;HaplotypeScore=0.4999;MLEAC=3;MLEAF=0.750;MQ=70.00;MQ0=0;MQRankSum=0.736;QD=17.12;ReadPosRankSum=0.736;VQSLOD=10.70;culprit=MQ	GT:AD:DP:GQ:PL	./.	0/1:1,1:2:34:36,0,34	./.	1/1:0,1:1:3:41,3,0	./.	./.	./.
	chr1	714427	rs12028261	G	A	57.36	PASS	AC=4;AF=1.00;AN=4;DB;DP=2;Dels=0.00;FS=0.000;HaplotypeScore=0.0000;MLEAC=4;MLEAF=1.00;MQ=70.00;MQ0=0;QD=28.68;VQSLOD=22.08;culprit=HaplotypeScore	GT:AD:DP:GQ:PL	./.	1/1:0,1:1:3:39,3,0	./.	./.	./.	./.	1/1:0,1:1:3:41,3,0
	chr1	725822	rs199845677	G	A	45.59	PASS	AC=1;AF=0.167;AN=6;BaseQRankSum=-1.231;DB;DP=5;Dels=0.00;FS=3.979;HaplotypeScore=0.3333;MLEAC=1;MLEAF=0.167;MQ=70.00;MQ0=0;MQRankSum=0.358;QD=15.20;ReadPosRankSum=0.358;VQSLOD=11.31;culprit=HaplotypeScore	GT:AD:DP:GQ:PL	0/0:1,0:1:3:0,3,42	0/1:1,2:3:32:72,0,32	./.	./.	./.	0/0:1,0:1:3:0,3,43	./.
	 */
	private void parseVCF() throws Exception{
		BufferedReader in  = ModelUtil.fetchBufferedReader(vcfFile);
		String line = null;
		
		//find "#CHROM" line saving header
		boolean failedToFindChromLine = true;
		while ((line=in.readLine()) != null){
			//comments
			if (line.startsWith("#")){
				header.add(line);
				if (line.startsWith("#CHROM")) {
					failedToFindChromLine = false;
					break;
				}
			}
		}
		if (failedToFindChromLine) throw new Exception("\nFailed to find the #CHROM header line.");
		
		LinkedHashMap<String, Chromosome> chromosomeName = genome.getNameChromosome();

		//Initilize variables
		String currentChrom = "";
		int currentChromLength = 0;
		ArrayList<Interval<String>> intervalAL = null;
		HashMap<String, ArrayList<Interval<String>>> chromIdInterval = new HashMap<String, ArrayList<Interval<String>>>();
		
		while ((line=in.readLine()) != null){
			String[] cells = ModelUtil.TAB.split(line);
			//parse and check chrom
			String chrom = cells[chromosomeIndex];
			//same chrom?
			if (chrom.equals(currentChrom) == false){
				//get length of chrom and check if chromId is present in genome
				Chromosome c = chromosomeName.get(chrom);
				if (c == null) throw new Exception ("\nFailed to find the vcf record chromosome in the nameChromosome LinkedHashMap?! See -> "+line);
				currentChromLength = c.getLength();
				currentChrom = chrom;
				intervalAL = chromIdInterval.get(chrom);
				if (intervalAL == null) {
					intervalAL = new ArrayList<Interval<String>>();
					chromIdInterval.put(chrom, intervalAL);
				}
			}
			//need start stop in interbase coordinates
			int start = Integer.parseInt(cells[positionIndex]) -1;
			int stop;
			int lenRef = cells[referenceIndex].length();
			int lenAlt = cells[alternateIndex].length();
			if (lenAlt > lenRef) stop = start + lenAlt;
			else stop = start +lenRef;
			//check end
			if (stop >= currentChromLength) throw new Exception ("\nStop position of vcf record exceeds the length of the chromosome ("+currentChromLength+")?! See -> "+line);
			//add interval
			intervalAL.add( new Interval<String>(start, stop, line));
		}

		in.close();
		
		//make IntervalTrees
		chromNameIntervalTree = new HashMap<String, IntervalTree<String>>();
		for (String chromId: chromIdInterval.keySet()){
			intervalAL = chromIdInterval.get(chromId);
			chromNameIntervalTree.put(chromId, new IntervalTree<String>(intervalAL, false));
			numberParsedLines+= intervalAL.size();
		}
		chromIdInterval = null;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(vcfFile +"\tFile\n");
		sb.append(numberParsedLines +"\tNumber VCF Records\n");
		if (chromNameIntervalTree != null )sb.append(chromNameIntervalTree.keySet() +"\tChromosomes\n");
		return sb.toString();
	}
	
	//getters and setters
	public File getVcfFile() {
		return vcfFile;
	}
	public ArrayList<String> getHeader() {
		return header;
	}
	public int getNumberParsedLines() {
		return numberParsedLines;
	}
	/**Will trigger parsing of file if needed.*/
	public HashMap<String, IntervalTree<String>> getChromNameIntervalTree() throws Exception{
		if (chromNameIntervalTree == null) parseVCF();
		return chromNameIntervalTree;
	}









}

