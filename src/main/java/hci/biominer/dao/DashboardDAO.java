package hci.biominer.dao;



import java.util.List;

import hci.biominer.model.Dashboard;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	private void testDashboard() {
		Session session = getCurrentSession();
		Query query  = session.createQuery("select d from Dashboard d");
		List<Dashboard> dl = query.list();
		if (dl.size() == 0) {
			Dashboard d = new Dashboard(new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0), new Long(0));
			session.beginTransaction();
			session.save(d);
			session.getTransaction().commit();
		}
		session.close();
	}
	
	private Dashboard getDashboard() {
		this.testDashboard();
		Session session = getCurrentSession();
		Dashboard d = (Dashboard)session.get(Dashboard.class,(long)1);
		session.close();
		return d;
	}
	
	private void updateDashboard(Dashboard d) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.update(d);
		session.getTransaction().commit();
		session.close();
	}
	
	public Long getQueryDate() {
		Dashboard d = getDashboard();
		return d.getLastQuery();
	}
	
	public Long getLastReportDate() {
		Dashboard d = getDashboard();
		return d.getLastReportDate();
	}
	
	public Long getLastCrashDate() {
		Dashboard d = getDashboard();
		return d.getLastCrashDate();
	}
	
	public Long getSubmissionDate() {
		Dashboard d = getDashboard();
		return d.getLastSubmission();
	}
	
	public Long getQueryCount() {
		Dashboard d = getDashboard();
		return d.getNumQuery();
	}
	
	public Long getIgvCount() {
		Dashboard d = getDashboard();
		return d.getNumIgv();
	}
	
	public Long getLoginCount() {
		Dashboard d = getDashboard();
		return d.getNumLogins();
	}
	
	public Long getCrashCount() {
		Dashboard d = getDashboard();
		return d.getNumCrashes();
	}
	
	public Long getReportCount() {
		Dashboard d = getDashboard();
		return d.getNumReports();
	}
	
	public void updateQueryDate(Long date) {
		Dashboard d = getDashboard();
		d.setLastQuery(date);
		updateDashboard(d);
	}
	
	public void updateSubmissionDate(Long date) {
		Dashboard d = getDashboard();
		d.setLastSubmission(date);
		updateDashboard(d);
	}
	
	public void updateLastReportDate(Long date) {
		Dashboard d = getDashboard();
		d.setLastReportDate(date);
		updateDashboard(d);
	}
	
	public void updateLastCrashDate(Long date) {
		Dashboard d = getDashboard();
		d.setLastCrashDate(date);
		updateDashboard(d);
	}
	
	public void increaseQuery() {
		Dashboard d = getDashboard();
		Long queryCount = d.getNumQuery() + 1;
		d.setNumQuery(queryCount);
		updateDashboard(d);
	}
	
	public void increaseIgv() {
		Dashboard d = getDashboard();
		Long igvCount = d.getNumIgv() + 1;
		d.setNumIgv(igvCount);
		updateDashboard(d);
	}
	
	public void increaseLogins() {
		Dashboard d = getDashboard();
		Long loginCount = d.getNumLogins() + 1;
		d.setNumLogins(loginCount);
		updateDashboard(d);
	}
	
	public void increaseCrashes() {
		Dashboard d = getDashboard();
		Long crashCount = d.getNumCrashes() + 1;
		d.setNumCrashes(crashCount);
		updateDashboard(d);
	}
	
	
	public void increaseReports() {
		Dashboard d = getDashboard();
		Long reportCount = d.getNumReports() + 1;
		d.setNumReports(reportCount);
		updateDashboard(d);
	}
}
