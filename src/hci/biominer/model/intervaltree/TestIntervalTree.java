package hci.biominer.model.intervaltree;

import hci.biominer.model.genome.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**Test app to work with Jannovars interval tree search classes.  Nix modified to use interbase coordinates.*/
public class TestIntervalTree {

	public static void main(String[] args) {
		
		//make set of intervals
		List<Interval<Integer>> intervals = getRegions();
		//List<Interval<String>> intervals = getIntervalListString();

		//make Integer Tree
		IntervalTree<Integer> tree = new IntervalTree<Integer>(intervals, false);
		//IntervalTree<String> tree = new IntervalTree<String>(intervals, false);

		//List<String> qy = tree.search(0, 3);
		List<Integer> qy = tree.search(0, 3);
		System.out.println(qy);
		
		//search it for 20K times
		/*for (int x=0; x<10; x++){
			long start = System.currentTimeMillis();
			for (int i=0; i< 20000; i++){
				List<Integer> qy = tree.search(i, i+10);
			}
			System.out.println(x+ "\t"+ (System.currentTimeMillis() - start));
		}*/
		
	}
	
	/**Takes a HashMap of chromosomeIDs and their associated Regions and searches them against the chromosomeID matched IntervalTrees.
	 * @return ArrayList of ids from the dataSet.*/
	public static ArrayList<Integer> search(HashMap<Integer, Region[]> query, HashMap<Integer, IntervalTree<Integer>> dataSet){
		ArrayList<Integer> dataSetIds = new ArrayList<Integer>();
		//for each chromosome ID
		for (Integer chrId: query.keySet()){
			//does the chrom exist in the dataSet?
			IntervalTree<Integer> tree = dataSet.get(chrId);
			if (tree == null) continue;
			//for each queryRegion
			Region[] queryRegions = query.get(chrId);
			for (Region r: queryRegions){
				dataSetIds.addAll(tree.search(r.getStart(), r.getStop()));
			}
		}
		return dataSetIds;
	}


	public static List<Interval<Integer>> getRegions(){
		List<Interval<Integer>> intervals = new ArrayList<Interval<Integer>>(20000);
		for (int i=0; i< 20000; i++){
			Integer x = new Integer(i);
			intervals.add( new Interval<Integer>(i, i+100, x) );
		}
		return intervals;
	}
	
	public static List<Interval<String>> getIntervalListString() {
	 	List<Interval<String>> ilist = new ArrayList<Interval<String>>();
	 	
	 	ilist.add(new Interval<String>(0,8,"a"));
	 	ilist.add(new Interval<String>(2,3,"b"));
	 	ilist.add(new Interval<String>(4,14,"c"));
	 	ilist.add(new Interval<String>(5,7,"d"));
	 	ilist.add(new Interval<String>(7,12,"e"));
	 	ilist.add(new Interval<String>(9,22,"f"));
	 	ilist.add(new Interval<String>(16,19,"g"));
	 	ilist.add(new Interval<String>(17,20,"h"));
	 	ilist.add(new Interval<String>(29,33,"i"));
	 	ilist.add(new Interval<String>(30,32,"j"));
	 
	 	return ilist;
	 
	}
}
