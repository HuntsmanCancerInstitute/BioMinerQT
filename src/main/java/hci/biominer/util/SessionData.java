package hci.biominer.util;

import java.io.Serializable;
import java.util.Date;

import hci.biominer.model.QueryResultContainer;

public class SessionData implements Serializable {
	private static final long serialVersionUID = 1L;
	private QueryResultContainer results = null;
	private QuerySettings settings = null;
	private StringBuilder queryWarnings = null;
	private String regionString = null;
	private String geneString = null;
	private Date lastTouched = null;
	
	public SessionData() {
		queryWarnings = new StringBuilder("");
	}
	
	public Date getLastTouched() {
		return lastTouched;
	}
	
	public void addWarning(String warning) {
		this.queryWarnings.append(warning);
	}
	
	public void clearWarnings() {
		this.queryWarnings = new StringBuilder("");
	}

	public void setLastTouched(Date lastTouched) {
		this.lastTouched = lastTouched;
	}

	public QueryResultContainer getResults() {
		return results;
	}

	public void setResults(QueryResultContainer results) {
		this.results = results;
	}

	public QuerySettings getSettings() {
		return settings;
	}

	public void setSettings(QuerySettings settings) {
		this.settings = settings;
	}

	public StringBuilder getQueryWarnings() {
		return queryWarnings;
	}

	public void setQueryWarnings(StringBuilder queryWarnings) {
		this.queryWarnings = queryWarnings;
	}

	public String getRegionString() {
		return regionString;
	}

	public void setRegionString(String regionString) {
		this.regionString = regionString;
	}

	public String getGeneString() {
		return geneString;
	}

	public void setGeneString(String geneString) {
		this.geneString = geneString;
	}
	

}
