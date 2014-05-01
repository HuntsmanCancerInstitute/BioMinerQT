package hci.biominer.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hci.biominer.model.Species;

@Repository
public class SpeciesDAO {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}

	public void addSpecies(Species species) {
		Session session = getCurrentSession();
		session.save(species);
		session.close();
	}

	@Transactional(readOnly=false)
	public void updateSpecies(Species species) {
	
		Session session = getCurrentSession();
		Species speciesToUpdate = (Species) session.get(Species.class, species.getId());
		speciesToUpdate.setName(species.getName());
		session.update(speciesToUpdate);
		session.flush();
		session.close();
		
	}

	public Species getSpecies(Long id) {
		Session session = getCurrentSession();
		Species species = (Species) session.get(Species.class, id);
		session.close();
		return species;
	}

	@Transactional(readOnly=false)
	public void deleteSpecies(Long id) {
		Session session = getCurrentSession();
		Species species = (Species) session.get(Species.class, id);
		if (species != null) 
			session.delete(species);
		session.flush();
		session.close();
	}

	@SuppressWarnings("unchecked")
	public List<Species> getAllSpecies() {
		Session session = getCurrentSession();
		List<Species> species = session.createQuery("from Species").list();
		session.close();
		return species;
	}

}
