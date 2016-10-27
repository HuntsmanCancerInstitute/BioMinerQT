package hci.biominer.dao;

import hci.biominer.model.GeneIdConversion;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeneIdConversionDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<GeneIdConversion> getGeneIdConversions() {
		Session session = getCurrentSession();
		List<GeneIdConversion> conversions = session.createQuery("from GeneIdConversion gic").list();
		session.close();
		return conversions;
	}
	
	public GeneIdConversion getGeneIdConversionsById(Long idGeneIdConversion) {
		Session session = getCurrentSession();
		GeneIdConversion conversion = (GeneIdConversion)session.get(GeneIdConversion.class, idGeneIdConversion);
		session.close();
		return conversion;
	}
	
	public void addGeneIdConversion(GeneIdConversion conversion) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(conversion);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateGeneIdConversion(Long idGeneIdConversion, GeneIdConversion conversion) {
		Session session = getCurrentSession();
		session.beginTransaction();
		GeneIdConversion conversionToUpdate = (GeneIdConversion) session.get(GeneIdConversion.class, idGeneIdConversion);
		conversionToUpdate.setConversionFile(conversion.getConversionFile());
		conversionToUpdate.setDestBuild(conversion.getDestBuild());
		conversionToUpdate.setSourceBuild(conversion.getSourceBuild());
		session.update(conversionToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public void deleteGeneIdConversion(Long idGeneIdConversion) {
		Session session = getCurrentSession();
		session.beginTransaction();
		GeneIdConversion conversionToDelete = (GeneIdConversion) session.get(GeneIdConversion.class, idGeneIdConversion);
		session.delete(conversionToDelete);
		session.flush();
		session.getTransaction().commit();
		session.close();
	}
	
	
}
