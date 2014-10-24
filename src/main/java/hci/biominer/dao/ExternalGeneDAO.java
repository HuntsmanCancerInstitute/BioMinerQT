package hci.biominer.dao;

import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalGeneDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public List<ExternalGene> getBiominerIdByExternalName(String name, Long obId) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select e from ExternalGene e left join e.organismBuild ob where ExternalGeneName = :name and ob.idOrganismBuild = :ob");
		query.setParameter("name", name);
		query.setParameter("ob", obId);
		List<ExternalGene> externalGenes = query.list();
		session.close();
		return externalGenes;
	}
	
	public List<ExternalGene> getExternalGeneByBiominerId(List<Long> biominerIds, String source, Long obId) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select distinct e from ExternalGene e "
				+ "left join e.biominerGene as bg "
				+ "left join e.organismBuild ob "
				+ "where bg.idBiominerGene in :ids and "
				+ "e.ExternalGeneSource = :source and "
				+ "ob.idOrganismBuild = :ob");
		query.setParameter("source", source);
		query.setParameterList("ids",biominerIds);
		query.setParameter("ob", obId);
		
		List<ExternalGene> externalGenes = query.list();
		session.close();
		return externalGenes;
	}
	
	public void deleteExternalGenesByOrganismId(Long obId) throws Exception {
		Session session  = this.getCurrentSession();
		Query query = session.createQuery("select e from ExternalGene e left join e.organismBuild ob where ob.idOrganismBuild = :ob");
		query.setParameter("ob", obId);
		
		List<ExternalGene> egList = query.list();
		
		session.getTransaction().begin();
		try {
			for (ExternalGene eg: egList) {
				session.delete(eg);
				
				int counter = 0;
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

	
	
	
	
	public void addExternalGenes(List<ExternalGene> genes) throws Exception {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		int counter = 0;
		
		try {
			for (ExternalGene g: genes) {
				session.save(g);
				
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
	
	public List<ExternalGene> getExternalGenesByOrganismBuild(OrganismBuild ob) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select e from ExternalGene e where organismBuild = :ob");
		query.setParameter("ob", ob);
		List<ExternalGene> egList = query.list();
		session.close();
		return egList;
	}
	
	public List<ExternalGene> getHugoNamesGenesByOrganismBuild(OrganismBuild ob) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select e from ExternalGene e where organismBuild = :ob and ExternalGeneSource = :hugo");
		query.setParameter("ob", ob);
		query.setParameter("hugo","hugo");
		List<ExternalGene> egList = query.list();
		session.close();
		
	
		return egList;
	}
	
}
