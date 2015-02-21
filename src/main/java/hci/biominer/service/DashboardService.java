package hci.biominer.service;

import hci.biominer.dao.DashboardDAO;
import hci.biominer.model.Dashboard;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("DashboardService")
@Transactional
public class DashboardService {
	@Autowired
	private DashboardDAO dashboardDAO;
	
	
	public Long getQueryDate() {
		return dashboardDAO.getQueryDate();
	}
	
	public Long getSubmissionDate() {
		return dashboardDAO.getSubmissionDate();
	}
	
	public Long getQueryCount() {
		return dashboardDAO.getQueryCount();
	}
	
	public Long getIgvCount() {
		return dashboardDAO.getIgvCount();
	}
	
	public Long getLoginCount() {
		return dashboardDAO.getLoginCount();
	}
	
	public Long getCrashCount() {
		return dashboardDAO.getCrashCount();
	}
	
	public Long getReportCount() {
		return dashboardDAO.getReportCount();
	}
	
	public Long getLastReportDate() {
		return dashboardDAO.getLastReportDate();
	}
	
	public Long getLastCrashDate() {
		return dashboardDAO.getLastCrashDate();
	}
	
	public void updateQueryDate(Long date) {
		dashboardDAO.updateQueryDate(date);
	}
	
	public void updateSubmissionDate(Long date) {
		dashboardDAO.updateQueryDate(date);
	}
	
	public void updateLastReportDate(Long date) {
		dashboardDAO.updateLastReportDate(date);
	}
	
	public void updateLastCrashDate(Long date) {
		dashboardDAO.updateLastCrashDate(date);
	}
	
	public void increaseQuery() {
		dashboardDAO.increaseQuery();
	}
	
	public void increaseIgv() {
		dashboardDAO.increaseIgv();
	}
	
	public void increaseLogins() {
		dashboardDAO.increaseLogins();
	}
	
	public void increaseCrashes() {
		dashboardDAO.increaseCrashes();
	}
	
	public void increaseReports() {
		dashboardDAO.increaseReports();
	}
}
