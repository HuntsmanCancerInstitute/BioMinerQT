package hci.biominer.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import hci.biominer.model.BiominerGene;
import hci.biominer.model.ExternalGene;


@Repository
public class BiominerGeneDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public List<BiominerGene> getBiominerGenes() {
		Session session = this.getCurrentSession();
		List<BiominerGene> bmGenes = session.createQuery("from BiominerGene").list();
		session.close();
		return bmGenes;
		
	}
	
	public void deleteBiominerGenes() throws Exception{
		Session session = this.getCurrentSession();
		
		Query q1 = session.createQuery("select b from BiominerGene b where b.externalGenes is empty");
		List<BiominerGene> bgList = q1.list();
		
		try {
			session.beginTransaction();
			int counter = 0;
			for (BiominerGene bg: bgList) {
				session.delete(bg);
				
				if ( counter % 20 == 0 && counter != 0 ) {
			        //flush a batch of inserts and release memory:
			        session.flush();
			        session.clear();
			    }
				counter++;
			}
		
			session.getTransaction().commit();
		} catch (Exception ex) {
			session.getTransaction().rollback();
			throw ex;
		}
		session.close();
	}
	
	public void addBiominerGene(BiominerGene bg) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(bg);
		session.getTransaction().commit();
		session.close();
	}
	
	public void addBiominerGenes(List<BiominerGene> bgList) throws Exception {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		
		
		try {
			int counter = 0;
			for (BiominerGene bg: bgList) {
				session.save(bg);
				
				if ( counter % 20 == 0 && counter != 0 ) {
			        //flush a batch of inserts and release memory:
			        session.flush();
			        session.clear();
			    }
				counter++;
			}
			session.getTransaction().commit();
		} catch (Exception ex) {
			session.getTransaction().rollback();
			throw ex;
		}
		
		session.close();
	}
}
