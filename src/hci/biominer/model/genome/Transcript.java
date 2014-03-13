package hci.biominer.model.genome;

import hci.biominer.util.ModelUtil;

import java.util.ArrayList;

public class Transcript {

	//fields
	private int transcriptNameID;
	private Gene parent;
	private String geneName;
	private String transcriptName;
	private String chrom;
	private char strand;
	private int txStart;
	private int txEnd;
	private int cdsStart;
	private int cdsEnd;
	private Region[] exons = null;
	private Region[] introns = null;
	private int tss;

	//constructors
	public Transcript (){}
	
	/** Parses a UCSC refflat formated gene line with geneName and transcriptName in first two columns: 
	 * ENSG00000230759	ENSTENSG00000220751	chr1	+	103957500	103968087	103968087	103968087	2	103957500,103967726	103957557,103968087 */
	public Transcript (String[] tokens, Gene parent) throws Exception{
		this.parent = parent;
		geneName = tokens[0];
		transcriptName = tokens[1];
		chrom = tokens[2];
		strand = tokens[3].charAt(0);
		txStart = Integer.parseInt(tokens[4]);
		txEnd = Integer.parseInt(tokens[5]);
		cdsStart = Integer.parseInt(tokens[6]);
		cdsEnd = Integer.parseInt(tokens[7]);

		//calc tss
		setTss();

		//make exons
		int exonCount = Integer.parseInt(tokens[8]);
		exons = new Region[exonCount];
		int[] starts = ModelUtil.stringArrayToInts(tokens[9],",");
		int[] stops = ModelUtil.stringArrayToInts(tokens[10],",");		
		if (starts.length != exonCount || stops.length != exonCount) throw new Exception("\nError: length of starts or stops doesn't match exon count.");
		for (int j=0; j< exonCount; j++) exons[j] = new Region(starts[j], stops[j]);

		//make introns
		makeIntrons();
		
		checkFields();
	}
	
	public void setTss() {
		if (strand == '-') tss = txEnd;
		else tss = txStart;
	}

	public void makeIntrons(){
		if (exons.length > 1){
			//find smallest start and size
			int smallest = exons[0].getStart();
			int size = exons[exons.length-1].getStop() - smallest;
			//make boolean array to mask
			boolean[] masked = new boolean[size];
			//for each exon mask boolean
			for (int k=0; k< exons.length; k++){
				int end = exons[k].getStop() - smallest;
				int start = exons[k].getStart() - smallest;
				for (int j= start; j< end; j++){
					masked[j] = true;
				}
			}
			//fetch blocks of false
			int[][] startStop = fetchFalseBlocks(masked,0,0);
			//add smallest to starts and ends to return to real coordinates and make introns
			if (startStop.length>0){
				introns = new Region[startStop.length];
				for (int k=0; k< startStop.length; k++){
					introns[k] = new Region(startStop[k][0]+smallest, startStop[k][1]+smallest+1);
				}
			}
		}
	}

	public void checkFields() throws Exception{
		//check strand
		if (strand != '+' && strand != '-' && strand != '.') throw new Exception ("\nError: the strand isn't +, -, or .  ?");

		//check exons
		if (Region.isOK(exons) == false) throw new Exception ("\nError: one of the exon starts is >= a stop.");

		//check introns
		if (introns != null && Region.isOK(introns) == false) throw new Exception ("\nError: one of the intron starts is >= a stop.");

		//check that last exon stop is == txEnd
		int lastExonEnd = exons[exons.length-1].getStop();
		if (lastExonEnd != txEnd) throw new Exception ("\nError: the txEnd is not = the last exon stop.");

		//check first exon start
		if (exons[0].getStart() !=txStart) throw new Exception ("\nError: the txStart is not = the first exon start.");

		//check that cdsStart and cdsEnd are internal or equal to
		if (cdsStart < txStart || cdsEnd > txEnd) throw new Exception ("\nError: the cdsStart or End aren't = or < the txStart, txEnd.");
	}

	public Transcript getPartialClone(){
		Transcript t = new Transcript();
		t.setParent(parent);
		t.setGeneName(geneName);
		t.setTranscriptName(transcriptName);
		t.setChrom(chrom);
		t.setStrand(strand);
		t.setTxStart(txStart);
		t.setTxEnd(txEnd);
		t.setCdsStart(cdsStart);
		t.setCdsEnd(cdsEnd);
		t.setTss(tss);
		Region[] e = new Region[exons.length];
		for (int i=0; i< e.length; i++) e[i] = new Region(exons[i].getStart(), exons[i].getStop());
		t.setExons(e);
		return t;
	}
	
	public static int[][] fetchFalseBlocks(boolean[] b, int bpToTrim, int minSize){
		ArrayList<int[]> al = new ArrayList<int[]>();
		int startFalse = 0;
		boolean inTrue;
		//check first base and set params
		if (b[0] == true) inTrue = true;
		else {
			inTrue = false;
			startFalse = 0;
		}

		//find blocks
		for (int i=1; i< b.length; i++){
			//true found?
			if (b[i]) {
				if (inTrue == false){					
					int size = i - startFalse;
					int left = startFalse + bpToTrim;
					int right = (i-1) - bpToTrim;
					size = right - left + 1;
					if (right >= left && size >= minSize){
						//make block, new true found
						int[] block = {left, right};
						al.add(block);
					}
					inTrue = true;
				}
				//otherwise continue
			}
			//false found
			//if inTrue is true then set the startFalse
			else if (inTrue) {
				startFalse = i;
				inTrue = false;
			}
		}
		//last one?
		if (inTrue == false){
			int size = b.length - startFalse;
			int left = startFalse + bpToTrim;
			int right = (b.length-1) - bpToTrim;
			size = right - left + 1;
			if (right >= left && size >= minSize){
				//make block, new true found
				int[] block = {left, right};
				al.add(block);
			}
		}
		//convert to int[][]
		int[][] blocks = new int[al.size()][2];
		for (int i=0; i< blocks.length; i++){
			blocks[i] = (int[]) al.get(i);
		}
		return blocks;
	}

	/**Assumes interbase coordinates*/
	public boolean intersects(int start, int stop){
		if (stop <= txStart || start >= txEnd) return false;
		return true;
	}
	
	/**Returns null if no overlap, otherwise the start and stop coordinates of the overlap.
	 * Assumes interbase coordinates*/
	public int[] fetchOverlap (int start, int stop) {
		if (intersects(start,stop) == false) return null;
		int beginning = 0;
		int ending = 0;
		//define the start
		if (this.txStart < start) beginning = start;
		else beginning = this.txStart;
		//define the end
		if (this.txEnd> stop) ending = stop;
		else ending = this.txEnd;
		return new int[]{beginning, ending};
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(parent.getName()); sb.append("\t");
		sb.append(transcriptName); sb.append("\t");
		sb.append(chrom); sb.append("\t");
		sb.append(strand); sb.append("\t");
		sb.append(txStart); sb.append("\t");
		sb.append(txEnd); sb.append("\t");
		sb.append(cdsStart); sb.append("\t");
		sb.append(cdsEnd); sb.append("\t");
		if (exons != null){
			sb.append(exons.length);sb.append("\t");
			sb.append(exons[0].getStart());
			for (int i=1; i<exons.length; i++){
				sb.append(",");
				sb.append(exons[i].getStart());
			}
			sb.append("\t");
			sb.append(exons[0].getStop());
			for (int i=1; i<exons.length; i++){
				sb.append(",");
				sb.append(exons[i].getStop());
			}
		}
		return sb.toString();
	}
	
	public boolean isPlusStand(){
		return strand == '+';
	}

	public int getTranscriptNameID() {
		return transcriptNameID;
	}

	public void setTranscriptNameID(int transcriptNameID) {
		this.transcriptNameID = transcriptNameID;
	}

	public Gene getParent() {
		return parent;
	}

	public void setParent(Gene parent) {
		this.parent = parent;
	}

	public String getTranscriptName() {
		return transcriptName;
	}

	public void setTranscriptName(String transcriptName) {
		this.transcriptName = transcriptName;
	}

	public String getChrom() {
		return chrom;
	}

	public void setChrom(String chrom) {
		this.chrom = chrom;
	}

	public char getStrand() {
		return strand;
	}

	public void setStrand(char strand) {
		this.strand = strand;
	}

	public int getTxStart() {
		return txStart;
	}

	public void setTxStart(int txStart) {
		this.txStart = txStart;
	}

	public int getTxEnd() {
		return txEnd;
	}

	public void setTxEnd(int txEnd) {
		this.txEnd = txEnd;
	}

	public int getCdsStart() {
		return cdsStart;
	}

	public void setCdsStart(int cdsStart) {
		this.cdsStart = cdsStart;
	}

	public int getCdsEnd() {
		return cdsEnd;
	}

	public void setCdsEnd(int cdsEnd) {
		this.cdsEnd = cdsEnd;
	}

	public Region[] getExons() {
		return exons;
	}

	public void setExons(Region[] exons) {
		this.exons = exons;
	}

	public Region[] getIntrons() {
		return introns;
	}

	public void setIntrons(Region[] introns) {
		this.introns = introns;
	}

	public int getTss() {
		return tss;
	}

	public void setTss(int tss) {
		this.tss = tss;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	//getters setters
}
