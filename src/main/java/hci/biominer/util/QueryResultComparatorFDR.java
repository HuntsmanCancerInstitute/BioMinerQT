package hci.biominer.util;

import hci.biominer.model.QueryResult;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;

public class QueryResultComparatorFDR implements Comparator<QueryResult>{
	NumberFormat formatter = new DecimalFormat("0.##E0");
	
	@Override
    public int compare(QueryResult o1, QueryResult o2)  {
		
		try {
			Double fdr1 = formatter.parse(o1.getFDR()).doubleValue();
			Double fdr2 = formatter.parse(o2.getFDR()).doubleValue();
			
			if (fdr1 < fdr2) {
				return -1;
			} else if (fdr2 < fdr1) {
				return 1;
			} else {
				return 0;
			}
		} catch (Exception ex) {
			return 0;
		}
    }
}


