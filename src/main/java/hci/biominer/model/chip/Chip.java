package hci.biominer.model.chip;

public class Chip implements java.io.Serializable  {
	private static final long serialVersionUID = 1L;
	private int start;
	private int stop;
	private String transFDR;
	private float log2Rto;
	private String chrom;
	private String toStringVal;
	
	
	public Chip(String chrom, int start, int stop, String transFDR, float log2Rto) {
		this.chrom = chrom;
		this.start = start;
		this.stop = stop;
		this.transFDR = transFDR;
		this.log2Rto = log2Rto;
		this.toStringVal = String.format("%s\t%d\t%d\t%s\t%.2f",this.chrom,this.start,this.stop,this.transFDR,this.log2Rto);
	}
	
	public int getStart() {
		return start;
	}
	
	public int getStop() {
		return stop;
	}
	
	public String getTransFDR() {
		return transFDR;
	}
	
	public float getLog2Rto() {
		return log2Rto;
	}
	
	public String toString() {
		return this.toStringVal;
	}

}
