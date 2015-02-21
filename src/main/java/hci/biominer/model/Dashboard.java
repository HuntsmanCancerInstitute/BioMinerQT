package hci.biominer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Dashboard")
public class Dashboard {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idDashboard")
	Long idDashboard;
	
	@Column(name="lastQuery")
	Long lastQuery;
	
	@Column(name="lastSubmission")
	Long lastSubmission;
	
	@Column(name="numSubmission")
	Long numSubmission;
	
	@Column(name="numQuery")
	Long numQuery;
	
	@Column(name="numIgv")
	Long numIgv;
	
	@Column(name="numLogins")
	Long numLogins;
	
	@Column(name="numCrashes")
	Long numCrashes;
	
	@Column(name="numReports")
	Long numReports;
	
	@Column(name="lastReportDate")
	Long lastReportDate;
	
	@Column(name="lastCrashDate")
	Long lastCrashDate;
	
	public Dashboard() {
		
	}
	
	public Dashboard(Long lastQuery, Long lastSubmission,Long numSubmission, Long numQuery, 
			Long numIgv, Long numLogins, Long numCrashes, Long numUsers, Long numReports, Long lastCrashDate, Long lastReportDate) {
		this.lastQuery = lastQuery;
		this.lastSubmission = lastSubmission;
		this.numSubmission = numSubmission;
		this.numQuery = numQuery;
		this.numIgv = numIgv;
		this.numLogins = numLogins;
		this.numCrashes = numCrashes;
		this.numReports = numReports;
		this.lastReportDate = lastReportDate;
		this.lastCrashDate = lastCrashDate;
	}

  
    
	

	public Long getLastReportDate() {
		return lastReportDate;
	}

	public void setLastReportDate(Long lastReportDate) {
		this.lastReportDate = lastReportDate;
	}

	public Long getLastCrashDate() {
		return lastCrashDate;
	}

	public void setLastCrashDate(Long lastCrashDate) {
		this.lastCrashDate = lastCrashDate;
	}

	public Long getNumReports() {
		return numReports;
	}

	public void setNumReports(Long numReports) {
		this.numReports = numReports;
	}

	public Long getIdDashboard() {
		return idDashboard;
	}

	public void setIdDashboard(Long idDashboard) {
		this.idDashboard = idDashboard;
	}

	public Long getLastQuery() {
		return lastQuery;
	}

	public void setLastQuery(Long lastQuery) {
		this.lastQuery = lastQuery;
	}

	public Long getLastSubmission() {
		return lastSubmission;
	}

	public void setLastSubmission(Long lastSubmission) {
		this.lastSubmission = lastSubmission;
	}

	public Long getNumSubmission() {
		return numSubmission;
	}

	public void setNumSubmission(Long numSubmission) {
		this.numSubmission = numSubmission;
	}

	public Long getNumQuery() {
		return numQuery;
	}

	public void setNumQuery(Long numQuery) {
		this.numQuery = numQuery;
	}

	public Long getNumIgv() {
		return numIgv;
	}

	public void setNumIgv(Long numIgv) {
		this.numIgv = numIgv;
	}

	public Long getNumLogins() {
		return numLogins;
	}

	public void setNumLogins(Long numLogins) {
		this.numLogins = numLogins;
	}

	public Long getNumCrashes() {
		return numCrashes;
	}

	public void setNumCrashes(Long numCrashes) {
		this.numCrashes = numCrashes;
	}
	
	
}
