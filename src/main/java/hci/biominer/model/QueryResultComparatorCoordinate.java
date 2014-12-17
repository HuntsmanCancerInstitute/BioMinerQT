package hci.biominer.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResultComparatorCoordinate implements Comparator<QueryResult> {
	
	@Override
    public int compare(QueryResult o1, QueryResult o2)  {
		
		Integer start1 = o1.getStart();
		Integer start2 = o2.getStart();
		Integer end1 = o1.getEnd();
		Integer end2 = o2.getEnd();
		
		if (!o1.isAlpha() && !o2.isAlpha()) {
			Integer chrom1 = Integer.parseInt(o1.getChrom());
			Integer chrom2 = Integer.parseInt(o2.getChrom());
			
			
			if (chrom1 == chrom2) {
				if (start1 == start2) {
					return end1.compareTo(end2);
				} else {
					return start1.compareTo(start2);
				}
			} else {
				return chrom1.compareTo(chrom2);
			}
 		} else {
 			String chrom1 = o1.getChrom();
 			String chrom2 = o2.getChrom();
			
			
			if (chrom1.equals(chrom2)) {
				if (start1 == start2) {
					return end1.compareTo(end2);
				} else {
					return start1.compareTo(start2);
				}
			} else {
				return chrom1.compareTo(chrom2);
			}
 		}
    }
}
