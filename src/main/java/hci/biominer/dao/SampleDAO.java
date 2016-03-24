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
	public boolean isSampleConditionUsed(String cond, Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample as s left join s.sampleCondition as sc where sc.cond = :cond and sc.organismBuild.idOrganismBuild = :idOrganismBuild");
		query.setParameter("cond", cond);
		query.setParameter("idOrganismBuild", idOrganismBuild);
		List<Sample> sampleList = query.list();
		session.close();
		if (sampleList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSamplePrepUsed(String description, Long idSamplePrep) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample s left join s.samplePrep as sp where sp.description = :description and sp.idSamplePrep = :idSamplePrep");
		query.setParameter("description", description);
		query.setParameter("idSamplePrep", idSamplePrep);
		List<Sample> sampleList = query.list();
		session.close();
		if (sampleList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSampleNameUsed(String name, Long idProject) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample where name = :name and idProject = :idProject");
		query.setParameter("name", name);
		query.setParameter("idProject",idProject);
		List<Sample> sampleList = query.list();
		session.close();
		if (sampleList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSampleSourceUsed(String source, Long idOrganismBuild) {
		Session session = getCurrentSession();
		Query query = session.createQuery("from Sample as s left join s.sampleSource as ss where ss.source = :source and ss.organismBuild.idOrganismBuild = :idOrganismBuild");
		query.setParameter("source", source);
		query.setParameter("idOrganismBuild",idOrganismBuild);
		List<Sample> sampleList = query.list();
		session.close();
		if (sampleList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isSampleTypeUsed(String type) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select s from Sample as s left join s.sampleType as st where st.type = :type");
		query.setParameter("type", type);
		List<Sample> sampleList = query.list();
		session.close();
		if (sampleList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Sample> getSamplesByCondition(Long idProject, String condition) {
		Session session = getCurrentSession();
		Query query = session.createQuery("select s from Sample as s left join s.project as p left join s.sampleCondition as sc where p.idProject = :idProject and sc.cond = :condition");
		query.setParameter("idProject", idProject);
		query.setParameter("condition",condition);
		List<Sample> sampleList = query.list();
		session.close();
		return sampleList;
	}
	
	
	
	
}
