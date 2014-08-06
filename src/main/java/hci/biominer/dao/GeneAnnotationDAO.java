package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.GeneAnnotation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class GeneAnnotationDAO {

	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<GeneAnnotation> getGeneAnnotations() {
		Session session  = getCurrentSession();
		List<GeneAnnotation> geneAnnotations = session.createQuery("from GeneAnnotation").list();
		session.close();
		return geneAnnotations;	
	}
	
	public GeneAnnotation getGeneAnnotationById(Long idGeneAnnotation) {
		Session session = getCurrentSession();
		GeneAnnotation geneAnnotation = (GeneAnnotation)session.get(GeneAnnotation.class, idGeneAnnotation);
		session.close();
		return geneAnnotation;
	}
	
	public void addGeneAnnotation(GeneAnnotation geneAnnotation) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(geneAnnotation);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateGeneAnnotation(Long idGeneAnnotation, GeneAnnotation GeneAnnotation) {
		Session session = getCurrentSession();
		session.getTransaction();
		GeneAnnotation geneAnnotationToUpdate = (GeneAnnotation) session.get(GeneAnnotation.class, idGeneAnnotation);
		geneAnnotationToUpdate.setName(geneAnnotationToUpdate.getName());
		session.update(geneAnnotationToUpdate);
		session.flush();
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteGeneAnnotation(Long idGeneAnnotation) {
		Session session = getCurrentSession();
		session.getTransaction();
		GeneAnnotation geneAnnotation = (GeneAnnotation) session.get(GeneAnnotation.class, idGeneAnnotation);
		if (geneAnnotation != null) 
			session.delete(geneAnnotation);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
}




