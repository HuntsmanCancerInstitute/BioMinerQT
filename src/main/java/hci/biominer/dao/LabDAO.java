package hci.biominer.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.model.Project;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.util.Enumerated.ProjectVisibilityEnum;

@Repository
public class LabDAO  {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public void addLab(Lab lab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(lab);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateLab(Lab lab, Long idLab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		Lab labToUpdate = (Lab) session.get(Lab.class, idLab);
		labToUpdate.setFirst(lab.getFirst());
		labToUpdate.setLast(lab.getLast());
		session.update(labToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public Lab getLab(Long idLab) {
		Session session = this.getCurrentSession();
		Lab lab  = (Lab)session.get(Lab.class, idLab);
		session.close();
		return lab;
	}
	
	public void deleteLab(Long idLab) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		Lab lab = (Lab)session.get(Lab.class, idLab);
		if (lab != null) {
			session.delete(lab);
		}
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public List<Lab> getAllLabs() {
		Session session  = this.getCurrentSession();
		List<Lab> lab = session.createQuery("from Lab").list();
		session.close();
		return lab;
	}
	
	@SuppressWarnings("unchecked")
	public List<Lab> getQueryLabsByVisibility(User user) {
		//Determine users lab and institute affiliations
		List<Long> labList = new ArrayList<Long>();
		List<Long> instituteList = new ArrayList<Long>();
		HashSet<Long> instituteSet = new HashSet<Long>();
		
		for (Lab l: user.getLabs()) {
			labList.add(l.getIdLab());
		}
		
		for (Institute i: user.getInstitutes()) {
			instituteList.add(i.getIdInstitute());
		}
		instituteList.addAll(instituteSet);
		
		//Get available data
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select distinct l from Project as p "
				+ "left join p.labs as l "
				+ "left join p.institutes as i " 
				+ "where (l.idLab in (:userLabs) and p.visibility = :vis1) or "
				+ "(i.idInstitute in (:userInstitute) and p.visibility = :vis2) or "
				+ "(p.visibility = :vis3)");
		query.setParameterList("userLabs", labList);
		query.setParameterList("userInstitute", instituteList);
		query.setParameter("vis1",ProjectVisibilityEnum.LAB);
		query.setParameter("vis2",ProjectVisibilityEnum.INSTITUTE);
		query.setParameter("vis3", ProjectVisibilityEnum.PUBLIC);
		List<Lab> availLabs = query.list();
		session.close();
		
		//Return output
		return availLabs;
	}
	
	@SuppressWarnings("unchecked")
	public List<Lab> getQueryLabsPublic() {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select l from Project as p "
				+ "left join p.labs as l "
				+ "where p.visibility = :visibility");
		query.setParameter("visibility", ProjectVisibilityEnum.PUBLIC);
		List<Lab> availLabs = query.list();
		session.close();
		
		
		return availLabs;
	}
	
	@SuppressWarnings("unchecked")
	public List<User> getAllUsers(Lab lab) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("from User where lab = :lab");
		query.setParameter("lab", lab);
		List<User> user = query.list();
		session.close();
		return user;
	}

}
