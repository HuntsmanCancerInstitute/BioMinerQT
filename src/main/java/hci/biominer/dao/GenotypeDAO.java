package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.Genotype;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class GenotypeDAO {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Genotype> getGenotypes() {
		Session session  = getCurrentSession();
		List<Genotype> genotypes = session.createQuery("from Genotype").list();
		session.close();
		return genotypes;	
	}
	
	public Genotype getGenotypeById(Long idGenotype) {
		Session session = getCurrentSession();
		Genotype genotype = (Genotype)session.get(Genotype.class, idGenotype);
		session.close();
		return genotype;
	}
	
	public void addGenotype(Genotype genotype) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(genotype);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateGenotype(Long idGenotype, Genotype genotype) {
		Session session = getCurrentSession();
		session.getTransaction();
		Genotype genotypeToUpdate = (Genotype) session.get(Genotype.class, idGenotype);
		genotypeToUpdate.setName(genotype.getName());
		session.update(genotypeToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteGenotype(Long idGenotype) {
		Session session = getCurrentSession();
		session.getTransaction();
		Genotype genotype = (Genotype) session.get(Genotype.class, idGenotype);
		if (genotype != null) 
			session.delete(genotype);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
}




