package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.Sample;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SampleDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSamples() {
		Session session = this.getCurrentSession();
		List<Sample> samples = session.createQuery("from Sample").list();
		session.close();
		return samples;
	}
	
	public Sample getSampleById(Long idSample) {
		Session session = getCurrentSession();
		Sample sample = (Sample)session.get(Sample.class, idSample);
		session.close();
		return sample;
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSampleByProject(Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample where project = :project");
		query.setParameter("project", project);
		List<Sample> samples = query.list();
		session.close();
		return samples;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSampleByAnalysis(Analysis analysis) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample where analysis = :analysis");
		query.setParameter("analysis", analysis);
		List<Sample> samples = query.list();
		session.close();
		return samples;
	}
	
	public void addSample(Sample sample) {
		Session session = getCurrentSession();
		session.beginTransaction();
		session.save(sample);
		session.getTransaction().commit();
		session.close();
	}
	
	public void updateSample(Long idSample, Sample sample) {
		Session session = getCurrentSession();
		session.beginTransaction();
		Sample SampleToUpdate = (Sample) session.get(Sample.class, idSample);
		SampleToUpdate.setName(sample.getName());
		SampleToUpdate.setProject(sample.getProject());
		SampleToUpdate.setAnalyses(sample.getAnalyses());
		SampleToUpdate.setSampleType(sample.getSampleType());
		SampleToUpdate.setSamplePrep(sample.getSamplePrep());
		SampleToUpdate.setSampleSource(sample.getSampleSource());
		SampleToUpdate.setSampleCondition(sample.getSampleCondition());
		session.update(SampleToUpdate);
		session.getTransaction().commit();
		session.close();	
	}
	
	public void deleteSample(Long idSample) {
		Session session = getCurrentSession();
		session.beginTransaction();
		Sample sample = (Sample) session.get(Sample.class, idSample);
		session.delete(sample);
		session.getTransaction().commit();
		session.close();
	}
	
	
}
