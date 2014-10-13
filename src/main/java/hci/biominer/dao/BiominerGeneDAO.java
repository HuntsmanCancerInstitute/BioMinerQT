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
		
		Query q1 = session.createQuery("from ExternalGene");
		List<ExternalGene> egList = q1.list();
		List<Long> bidList = new ArrayList<Long>();
		for (ExternalGene eg: egList) {
			bidList.add(eg.getBiominerGene().getIdBiominerGene());
		}
		
		Query q2 = null;
		if (bidList.size() == 0) {
			q2 = session.createQuery("from BiominerGene");
		} else {
			q2 = session.createQuery("select b from BiominerGene b where b.idBiominerGene not in (:idList) ");
			q2.setParameterList("idList", bidList);
		}
	
		List<BiominerGene> bgList = q2.list();
		
		session.beginTransaction();
		
		try {
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
