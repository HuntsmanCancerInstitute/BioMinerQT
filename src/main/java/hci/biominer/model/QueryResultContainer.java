package hci.biominer.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class QueryResultContainer implements Serializable {
	

	private static final long serialVersionUID = 1L;
	private List<QueryResult> resultList;
	private int resultNum;
	private int pages;
	private String sortType;
	private boolean reverse = true;
	private int analysisNum;
	private int dataTrackNum;
	
	
	public QueryResultContainer(List<QueryResult> results, int resultsSize, int analysisNum, int dataTrackNum, int page, String sortType, boolean sortOnCreate) {
		this.resultList = results;
		this.resultNum = resultsSize;
		this.pages = page;
		this.sortType = sortType;
		if (sortOnCreate) {
			sortResults(sortType);
		}
		this.analysisNum = analysisNum;
		this.dataTrackNum = dataTrackNum;
	}

	public List<QueryResult> getResultList() {
		return resultList;
	}
	
	public int getResultNum() {
		return resultNum;
	}
	
	public int getPages() {
		return this.pages;
	}
	
	public String getSortType() {
		return sortType;
	}

	public int getAnalysisNum() {
		return analysisNum;
	}

	public int getDataTrackNum() {
		return dataTrackNum;
	}

	public void setResultList(List<QueryResult> results) {
		this.resultList = results;
	}
	
	private int calcPages(List<QueryResult> results, int queriesPerPage) {
		if (results.size() % queriesPerPage == 0) {
			return results.size() / queriesPerPage;
		} else {
			return results.size() / queriesPerPage + 1;
		}
	}
	
	private void sortResults(String sortType) {
		if (sortType.equals("FDR")) {
			Collections.sort(this.resultList,new QueryResultComparatorFDR());
		} else if (sortType.equals("Log2Ratio")) {
			Collections.sort(this.resultList,new QueryResultComparatorLog2Ratio());
		} else if (sortType.equals("Coordinate")) {
			Collections.sort(this.resultList,new QueryResultComparatorCoordinate());
		}
		
		if (this.reverse) {
			Collections.reverse(this.resultList);
			this.reverse = false;
		} else {
			this.reverse = true;
		}
		
		for (int i=0;i<this.resultList.size();i++) {
			this.resultList.get(i).setIndex(i+1);
		}
	}
	
	@JsonIgnore
	public QueryResultContainer getQrcSubset(int queriesPerPage, int page, String sortType) {
		sortResults(sortType);
		this.sortType = sortType;
		
		int start = page * queriesPerPage;
		int end = start + queriesPerPage;
		
		if (end > this.resultList.size()) {
			end = this.resultList.size();
		}
		
		this.pages = this.calcPages(this.resultList, queriesPerPage);
		
		List<QueryResult> subset = this.resultList.subList(start, end);
		
		QueryResultContainer qrc = new QueryResultContainer(subset,this.resultNum, this.analysisNum, this.dataTrackNum, this.pages, sortType, false);
		return qrc;
		
	}
		

}
