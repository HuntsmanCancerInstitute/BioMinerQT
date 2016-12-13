package returnModel;

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
	private int analysisNum;
	private int dataTrackNum;
	private long idOrganismBuild;
	private String returnedEnsemblCode;
	private String returnedOrganismName;
	
	
	public QueryResultContainer(List<QueryResult> results, int resultsSize, int analysisNum, int dataTrackNum, int page, String sortType, boolean sortOnCreate, 
			Long idOrganismBuild, String returnedEnsemblCode, String returnedOrganismName, boolean reverse) {
		this.resultList = results;
		this.resultNum = resultsSize;
		this.pages = page;
		this.sortType = sortType;
		if (sortOnCreate) {
			sortResults(sortType, reverse);
		}
		this.analysisNum = analysisNum;
		this.dataTrackNum = dataTrackNum;
		this.idOrganismBuild = idOrganismBuild;
		this.returnedEnsemblCode = returnedEnsemblCode;
		this.returnedOrganismName = returnedOrganismName;
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

	public Long getIdOrganismBuild() {
		return idOrganismBuild;
	}
	
	public void setResultList(List<QueryResult> results) {
		this.resultList = results;
		this.resultNum = results.size();
	}
	
	
	public String getReturnedOrganismName() {
		return returnedOrganismName;
	}


	public void setReturnedOrganismName(String returnedOrganismName) {
		this.returnedOrganismName = returnedOrganismName;
	}


	public String getReturnedEnsemblCode() {
		return returnedEnsemblCode;
	}

	public void setReturnedEnsemblCode(String returnedEnsemblCode) {
		this.returnedEnsemblCode = returnedEnsemblCode;
	}

	private int calcPages(List<QueryResult> results, int queriesPerPage) {
		if (results.size() % queriesPerPage == 0) {
			return results.size() / queriesPerPage;
		} else {
			return results.size() / queriesPerPage + 1;
		}
	}
	
	private void sortResults(String sortType, boolean reverse) {
		if (sortType.equals("FDR")) {
			Collections.sort(this.resultList,new QueryResultComparatorFDR());
		} else if (sortType.equals("Log2Ratio")) {
			Collections.sort(this.resultList,new QueryResultComparatorLog2Ratio());
		} else if (sortType.equals("Coordinate")) {
			Collections.sort(this.resultList,new QueryResultComparatorCoordinate());
		}
		
		if (reverse) {
			Collections.reverse(this.resultList);
		} 
		
		for (int i=0;i<this.resultList.size();i++) {
			this.resultList.get(i).setIndex(i+1);
		}
	}
	
	@JsonIgnore
	public QueryResultContainer getQrcSubset(int queriesPerPage, int page, String sortType, boolean reverse) {
		sortResults(sortType, reverse);
		this.sortType = sortType;
		
		int start = page * queriesPerPage;
		int end = start + queriesPerPage;
		
		if (end > this.resultList.size()) {
			end = this.resultList.size();
		}
		
		this.pages = this.calcPages(this.resultList, queriesPerPage);
		
		List<QueryResult> subset = this.resultList.subList(start, end);
		
		QueryResultContainer qrc = new QueryResultContainer(subset,this.resultNum, this.analysisNum, this.dataTrackNum, this.pages, sortType, false, idOrganismBuild, returnedEnsemblCode, returnedOrganismName, reverse);
		return qrc;
		
	}


	public void setAnalysisNum(int analysisNum) {
		this.analysisNum = analysisNum;
	}


	public void setDataTrackNum(int dataTrackNum) {
		this.dataTrackNum = dataTrackNum;
	}


	public void setIdOrganismBuild(long idOrganismBuild) {
		this.idOrganismBuild = idOrganismBuild;
	}
	
		

}
