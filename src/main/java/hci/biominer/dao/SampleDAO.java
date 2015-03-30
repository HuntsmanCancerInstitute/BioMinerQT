package hci.biominer.dao;

import java.util.List;

import hci.biominer.model.AnalysisType;
import hci.biominer.model.DataTrack;
import hci.biominer.model.Sample;
import hci.biominer.model.Project;
import hci.biominer.model.Analysis;
import hci.biominer.model.SampleType;

import org.hibernate.Hibernate;
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
		Hibernate.initialize(sample.isAnalysisSet());
		session.close();
		return sample;
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSampleByProject(Project project) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample where project = :project");
		query.setParameter("project", project);
		List<Sample> samples = query.list();
		for (Sample s: samples) {
			Hibernate.initialize(s.isAnalysisSet());
		}
		session.close();
		return samples;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSampleByAnalysis(Analysis analysis) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample where analysis = :analysis");
		query.setParameter("analysis", analysis);
		List<Sample> samples = query.list();
		for (Sample s: samples) {
			Hibernate.initialize(s.isAnalysisSet());
		}
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
	
	@SuppressWarnings("unchecked")
	public boolean isSampleConditionUsed(String cond) {
		Session session = getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from Sample as s left join s.sampleCondition as sc where sc.cond = :cond");
		query.setParameter("cond", cond);
		List<SampleType> stList = query.list();
		session.close();
		if (stList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSamplePrepUsed(String description) {
		Session session = getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("select s from Sample as s left join s.samplePrep as sp where sp.description = :description");
		query.setParameter("description", description);
		List<AnalysisType> spList = query.list();
		session.close();
		if (spList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSampleSource(String source) {
		Session session = getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from Sample as s left join s.sampleSource as ss where ss.source = :source");
		query.setParameter("source", source);
		List<AnalysisType> ssList = query.list();
		session.close();
		if (ssList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
}
