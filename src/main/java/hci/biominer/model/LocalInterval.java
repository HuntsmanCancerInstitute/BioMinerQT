package hci.biominer.model;

public class LocalInterval {
	private String chrom;
	private int start;
	private int end;
	private String search; //what was searched to get this interval 


	public LocalInterval(String chrom, int start, int end, String search) {
		this.chrom = chrom;
		this.start = start;
		this.end = end;
		this.search = search;
	}
	
	public String getChrom() {
		return this.chrom;
	} 
	
	public int getStart() {
		return this.start;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public String getSearch() {
		return this.search;
	}
}
