package returnModel;

import java.util.ArrayList;
import java.util.List;

public class QueryResultMessage {
	private ArrayList<QueryResult> resultList;
	private String message;
	boolean failed = false;
	
	public QueryResultMessage(ArrayList<QueryResult> resultList, String message) {
		this.message = message;
		this.resultList = resultList;
	}
	
	public QueryResultMessage(String message) {
		this.resultList = null;
		this.message = message;
		this.failed = true;
	}
	
	public QueryResultMessage() {
		this.resultList = null;
		this.message = "";
		this.failed = true;
	}
	
	

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public List<QueryResult> getResultList() {
		return resultList;
	}

	public void setResultList(ArrayList<QueryResult> resultList) {
		this.resultList = resultList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
