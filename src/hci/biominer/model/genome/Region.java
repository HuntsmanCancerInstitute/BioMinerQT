package hci.biominer.model.genome;

import java.io.Serializable;
import java.util.Arrays;

/**Interbase coordinates describing a region on a chromosome.*/
public class Region implements Serializable {
	
	//fields
	private int start;
	private int stop;
	private static final long serialVersionUID = 1L;
	
	//constructors
	public Region(int start, int stop) {
		this.start = start;
		this.stop = stop;
	}
	
	//methods
	/**Checks to see that stop is always > start.*/
	public static boolean isOK(Region[] regions){
		for (Region r: regions){
			if (r.getStart()>= r.getStop()) return false;
		}
		return true;
	}
	
	/**Assumes interbase coordinates*/
	public boolean intersects(int start, int stop){
		if (stop < this.start || start >= this.stop) return false;
		return true;
	}
	
	/**Assumes interbase coordinates.*/
	public boolean intersects(Region coding) {
		if (coding.stop < this.start || coding.start >= this.stop) return false;
		return true;
	}
	
	/**Assumes interbase coordinates.*/
	public static Region[] merge(Region[] a, Region[] b){
		//find min and max
		int[] minMaxOne = minMax(a);
		int[] minMaxTwo = minMax(b);
		int min = minMaxOne[0];
		int max = minMaxOne[1];
		if (minMaxTwo[0]< min) min = minMaxTwo[0];
		if (minMaxTwo[1]> max) max = minMaxTwo[1];
		//load boolean array
		int length = max-min+1;
		boolean[] fetchFalse = new boolean[length];
		Arrays.fill(fetchFalse, true);
		for (int i=0; i< a.length; i++){
			int start = a[i].start -min;
			int stop = a[i].stop-min;		
			for (int j=start; j< stop; j++) fetchFalse[j] = false;
		}
		for (int i=0; i< b.length; i++){
			int start = b[i].start -min;
			int stop = b[i].stop-min;		
			for (int j=start; j< stop; j++) fetchFalse[j] = false;
		}
		//retrieve blocks, ends included
		int[][] blocks = Transcript.fetchFalseBlocks(fetchFalse, 0, 0);
		//convert to interbase coordinate exon introls
		Region[] ei = new Region[blocks.length];
		int minPlusOne = min +1;
		for (int i=0; i< ei.length; i++){
			ei[i] = new Region(blocks[i][0]+min, blocks[i][1]+minPlusOne);
		}
		return ei;
	}
	
	public static int[] minMax(Region[] ei){
		int min = ei[0].start;
		int max = ei[0].stop;
		for (int i=1; i< ei.length; i++){
			if (ei[i].start < min) min = ei[i].start;
			if (ei[i].stop > max) max = ei[i].stop;
		}
		return new int[]{min,max};
	}
	
	public String toString(){
		return start +"\t" + stop;
	}
	
	//getters setters
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getStop() {
		return stop;
	}
	public void setStop(int stop) {
		this.stop = stop;
	}
}
