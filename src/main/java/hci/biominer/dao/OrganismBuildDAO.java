package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Organism;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OrganismBuildDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrganismBuild> getOrganismBuilds() {
		Session session = this.getCurrentSession();
		List<OrganismBuild> organismBuilds = session.createQuery("from OrganismBuild").list();
		session.close();
		return organismBuilds;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrganismBuild> getOrganismBuildByOrganism(Organism organism) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("from OrganismBuild where organism = :organism");
		query.setParameter("organism", organism);
		List<OrganismBuild> organismBuilds = query.list();
		session.close();
		return organismBuilds;
	}
	
	public OrganismBuild getOrganismBuildById(Long idOrganismBuild) {
		Session session = getCurrentSession();
		OrganismBuild organismBuild = (OrganismBuild)session.get(OrganismBuild.class, idOrganismBuild);
		session.close();
		return organismBuild;
	}
	
	public void addOrganismBuild(OrganismBuild organismBuild) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(organismBuild);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateOrganismBuild(Long idOrganismBuild, OrganismBuild organismBuild) {
		Session session = getCurrentSession();
		session.beginTransaction();
		OrganismBuild OrganismBuildToUpdate = (OrganismBuild) session.get(OrganismBuild.class, idOrganismBuild);
		OrganismBuildToUpdate.setName(organismBuild.getName());
		OrganismBuildToUpdate.setOrganism(organismBuild.getOrganism());
		session.update(OrganismBuildToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteOrganismBuild(Long idOrganismBuild) {
		Session session = getCurrentSession();
		session.beginTransaction();
		OrganismBuild organismBuild = (OrganismBuild) session.get(OrganismBuild.class, idOrganismBuild);
		if (organismBuild != null) 
			session.delete(organismBuild);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
	
	
}
