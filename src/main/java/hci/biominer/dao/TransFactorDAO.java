package hci.biominer.dao;

import hci.biominer.model.OrganismBuild;
import hci.biominer.model.TransFactor;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TransFactorDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	public TransFactor getTfById(Long idTf) {
		Session session = getCurrentSession();
		TransFactor tf = (TransFactor)session.get(TransFactor.class, idTf);
		session.close();
		return tf;
	}
	
	@SuppressWarnings("unchecked")
	public List<TransFactor> getAllTfs() {
		Session session = this.getCurrentSession();
		List<TransFactor> tfList = session.createQuery("from TransFactor").list(); 
		session.close();
		return tfList;
	}
	
	@SuppressWarnings("unchecked")
	public List<TransFactor> getTfByBuild(OrganismBuild ob) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select tf from TransFactor tf where tf.organismBuild = :ob");
		query.setParameter("ob", ob);
		List<TransFactor> tfList = query.list();
		session.close();
		return tfList;
	}
	//e left join e.organismBuild ob where ob.idOrganismBuild = :ob
	
	public void deleteTf(Long idTf) {
		TransFactor tf = this.getTfById(idTf);
		Session session = getCurrentSession();
		session.beginTransaction();
		session.delete(tf);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateTf(TransFactor tf, Long idTf) {
		TransFactor tfToUpdate = this.getTfById(idTf);
		Session session = getCurrentSession();
		tfToUpdate.setName(tf.getName());
		tfToUpdate.setDescription(tf.getDescription());
		tfToUpdate.setOrganismBuild(tf.getOrganismBuild());
		tfToUpdate.setFilename(tf.getFilename());
		session.update(tfToUpdate);
		session.getTransaction().commit();
		session.close();
	}
	
	public Long addTf(TransFactor tf) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(tf);
		session.getTransaction().commit();
		session.close();
		return tf.getIdTransFactor();
	}
	
	public boolean checkName(String filename) {
		Session session  = getCurrentSession();
		Query query = session.createQuery("select tf from TransFactor as tf where tf.filename = :filename ");
		query.setParameter("filename", filename);
		List<TransFactor> tfList = query.list();
		session.close();
		if (tfList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
}
