package returnModel;

import java.util.Comparator;

public class QueryResultComparatorLog2Ratio implements Comparator<QueryResult> {
	@Override
    public int compare(QueryResult o1, QueryResult o2)  {
		
		try {
			Float log1 = o1.getLog2Ratio();
			Float log2 = o2.getLog2Ratio();
			
			if (Math.abs(log1) > Math.abs(log2)) {
				return -1;
			} else if (Math.abs(log2) > Math.abs(log1)) {
				return 1;
			} else {
				return 0;
			}
		} catch (Exception ex) {
			return 0;
		}
    }
}
