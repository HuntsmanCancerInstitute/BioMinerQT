package hci.biominer.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResultComparatorCoordinate implements Comparator<QueryResult> {
	private Pattern p1 = Pattern.compile("(chr)*(\\S+):(\\d+)-(\\d+)");
	
	@Override
    public int compare(QueryResult o1, QueryResult o2)  {
		
		try {
			
			
			String coord1 = o1.getCoordinates();
			String coord2 = o2.getCoordinates();
			
			
//			Matcher m1 = p1.matcher(coord1);
//			Matcher m2 = p1.matcher(coord2);
//			
//			if (!m1.matches() || !m2.matches()) {
//				ç
//			} else {
//				String chrom1 = m1.group(2);
//				String chrom2 = m2.group(2);
//				
//				
//				
//				
//				try {
//					int chrom1i = Integer.parseInt(chrom1);
//					int chrom2i = Integer.parseInt(chrom2);
//					
//					
//					if (chrom1i < chrom2i) {
//						return -1;
//					} else if (chrom1i > chrom2i) {
//						return 1;
//					} else {
//						try {
//							int pos1 = Integer
//						}
//					}
//					
//				} catch (NumberFormatException nfe) {
//					
//				}
				
				
			return coord1.compareTo(coord2);
			
		} catch (Exception ex) {
			return 0;
		}
    }
}
