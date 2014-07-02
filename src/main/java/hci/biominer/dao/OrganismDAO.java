package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.Organism;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class OrganismDAO {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Organism> getOrganisms() {
		Session session  = getCurrentSession();
		List<Organism> organisms = session.createQuery("from Organism").list();
		session.close();
		return organisms;	
	}
	
	public Organism getOrganismById(Long idOrganism) {
		Session session = getCurrentSession();
		Organism organism = (Organism)session.get(Organism.class, idOrganism);
		session.close();
		return organism;
	}
	
	public void addOrganism(Organism organism) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(organism);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateOrganism(Long idOrganism, Organism Organism) {
		Session session = getCurrentSession();
		session.getTransaction();
		Organism OrganismToUpdate = (Organism) session.get(Organism.class, idOrganism);
		OrganismToUpdate.setCommon(OrganismToUpdate.getCommon());
		OrganismToUpdate.setBinomial(OrganismToUpdate.getBinomial());
		session.update(OrganismToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteOrganism(Long idOrganism) {
		Session session = getCurrentSession();
		session.getTransaction();
		Organism Organism = (Organism) session.get(Organism.class, idOrganism);
		if (Organism != null) 
			session.delete(Organism);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
}




