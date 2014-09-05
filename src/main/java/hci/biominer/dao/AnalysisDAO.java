package hci.biominer.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hibernate.SessionFactory;

import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.DataTrack;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.Sample;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.util.Enumerated.ProjectVisibilityEnum;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisDAO {
	@Autowired
	private SessionFactory sessionFactory;
	
	private Session getCurrentSession() {
		return sessionFactory.openSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis>  getAllAnalysis() {
		Session session = this.getCurrentSession();
		List<Analysis> analyses = session.createQuery("from Analysis").list();
		
		
		for (Analysis a: analyses) {
			this.initializeAnalysis(a);
		}
		
		session.close();
		return analyses;
	}
  
  @SuppressWarnings("unchecked")
  public List<Analysis> getAnalysesByVisibility(User user) {
    List<Long> labList = new ArrayList<Long>();
    List<Long> instituteList = new ArrayList<Long>();
    HashSet<Long> instituteSet = new HashSet<Long>();
    
    
    for (Lab l: user.getLabs()) {
      labList.add(l.getIdLab());
    }
    
    for (Institute i: user.getInstitutes()) {
      instituteList.add(i.getIdInstitute());
    }
    instituteList.addAll(instituteSet);
    
    Session session = this.getCurrentSession();
    Query query = session.createQuery("select distinct a from Project as p "
    	+ "join p.analyses as a "
        + "left join p.labs as l "
        + "left join p.institutes as i " 
        + "where (l.idLab in (:userLabs) and p.visibility = :vis1) or "
        + "(i.idInstitute in (:userInstitute) and p.visibility = :vis2) or "
        + "(p.visibility = :vis3)");
    query.setParameterList("userLabs", labList);
    query.setParameterList("userInstitute", instituteList);
    query.setParameter("vis1",ProjectVisibilityEnum.LAB);
    query.setParameter("vis2",ProjectVisibilityEnum.INSTITUTE);
    query.setParameter("vis3", ProjectVisibilityEnum.PUBLIC);
    List<Analysis> analyses = query.list();
    for (Analysis a: analyses) {
		this.initializeAnalysis(a);
	}
    session.close();
    return analyses;
  }
  
  @SuppressWarnings("unchecked")
  public List<Analysis> getPublicAnalyses() {
    Session session = this.getCurrentSession();
    Query query = session.createQuery("select distinct a from Project as p join p.analyses as a where p.visibility = :visibility");
    query.setParameter("visibility", ProjectVisibilityEnum.PUBLIC);
    List<Analysis> analyses = query.list();
    for (Analysis a: analyses) {
		this.initializeAnalysis(a);
	}
    session.close();
    return analyses;
  }
	
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getAnalysesByProject(Project project) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select a from Analysis a where a.project = :project");
		query.setParameter("project",project);
		List<Analysis> analyses = query.list();
		
		for (Analysis a: analyses) {
			this.initializeAnalysis(a);
		}
		
		session.close();
		return analyses;
	}
	
	public List<Analysis> getAnalysesToPreload(OrganismBuild ob, AnalysisType at) {
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select distinct a from Analysis a "
				+ " left join a.project as p "
				+ " where "
				+ " a.analysisType = :analysisType and "
				+ " p.organismBuild = :organismBuild and "
				+ " a.file IS NOT NULL");
		query.setParameter("analysisType",at);
		query.setParameter("organismBuild",ob);
		List<Analysis> analyses = query.list();
		
		for (Analysis a: analyses) {
			this.initializeAnalysis(a);
		}
		
		session.close();
		return analyses;
	}
	
	public Long addAnalysis(Analysis analysis) {
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.save(analysis);
		session.getTransaction().commit();
		session.close();
		return analysis.getIdAnalysis();
	}
	
	public void updateAnalysis(Analysis analysis, Long idAnalysis) {
	    Analysis analysisToUpdate = this.getAnalysisById(idAnalysis);
	    Session session = getCurrentSession();
	    session.beginTransaction();
	    analysisToUpdate.setName(analysis.getName());
	    analysisToUpdate.setDescription(analysis.getDescription());
	    analysisToUpdate.setDate(analysis.getDate());
	    analysisToUpdate.setProject(analysis.getProject());
	    analysisToUpdate.setFile(analysis.getFile());
	    analysisToUpdate.setSamples(analysis.getSamples());
	    analysisToUpdate.setDataTracks(analysis.getDataTracks());
	    session.update(analysisToUpdate);
	    session.getTransaction().commit();
	    session.close();
	}
	
	public Analysis getAnalysisById(Long idAnalysis) {
		Session session = getCurrentSession();
		Analysis analysis = (Analysis)session.get(Analysis.class, idAnalysis);
		
		Hibernate.initialize(analysis.getFile());
		Hibernate.initialize(analysis.getDataTracks());
		Hibernate.initialize(analysis.getSamples());
		
		session.close();
		return analysis;
	}
	
	public void deleteAnalysis(Long idAnalysis) {
		Analysis analysis = this.getAnalysisById(idAnalysis);
		Session session = this.getCurrentSession();
		session.beginTransaction();
		session.delete(analysis);
		session.getTransaction().commit();
		session.close();
	}
	
	public List<Analysis> getAnalysesByQuery(List<Long> labs, List<Long> projects, List<Long> analyses, 
			List<Long> sources, Long analysisType, Long idGenomeBuild, User user) {
		
	    
	    //create parameter hashmap
	    HashMap<String,Object> parameterMap = new HashMap<String,Object>();
	    HashMap<String,Collection> parameterListMap = new HashMap<String,Collection>();
	    
	    //Create basic query
	    String selectString = "select distinct a from Analysis a ";
	    StringBuilder joinString = new StringBuilder();
	    StringBuilder whereString = new StringBuilder();
	    
	    joinString.append(" left join a.project as p ");
	    joinString.append(" left join p.labs as l ");
	    joinString.append(" left join p.institutes as i ");
	    
	    //Add public visibility permissions
	    
	    whereString.append(" where ");
	    whereString.append(" ( p.visibility = :pubVisibility ");
	    
	    parameterMap.put("pubVisibility", ProjectVisibilityEnum.PUBLIC);
	    
	    //If user logged in, add additional visibility permissions
	    if (user != null) {
	    	List<Long> labList = new ArrayList<Long>();
		    List<Long> instituteList = new ArrayList<Long>();
		    HashSet<Long> instituteSet = new HashSet<Long>();
		   
		    for (Lab l: user.getLabs()) {
		      labList.add(l.getIdLab());
		    }
		    
		    for (Institute i: user.getInstitutes()) {
		      instituteList.add(i.getIdInstitute());
		    }
		    instituteList.addAll(instituteSet);
	    	
	    	
	    	whereString.append(" or (l.idLab in (:userLabs) and p.visibility = :labVisibility) ");
		    whereString.append(" or (i.idInstitute in (:userInstitute) and p.visibility = :instVisibility) ");
		    
		    parameterListMap.put("userLabs", labList);
		    parameterListMap.put("userInstitute",instituteList);
		    parameterMap.put("labVisibility", ProjectVisibilityEnum.LAB);
		    parameterMap.put("instVisibility", ProjectVisibilityEnum.INSTITUTE);
		    
	    }
	    
	    //Separate out visibilty part of the 'where' clause
	    whereString.append(" ) ");
	    
	    //Make sure there is a parseable file!
	    whereString.append(" and (a.file IS NOT NULL) ");
	    
	    //Add query specific permissions
	    joinString.append(" left join p.organismBuild as ob ");
	    whereString.append(" and (ob.idOrganismBuild = :idGenomeBuild) ");
	    parameterMap.put("idGenomeBuild", idGenomeBuild);
	    
	    joinString.append(" left join a.analysisType as at ");
    	whereString.append(" and (at.idAnalysisType = (:selectAnalysisType)) ");
    	parameterMap.put("selectAnalysisType", analysisType);
    	
	    
	    if (labs.size() > 0) {
	    	whereString.append(" and (l.idLab in (:selectLabs)) ");
	    	parameterListMap.put("selectLabs", labs);
	    }
	    
	    if (projects.size() > 0) {
	    	whereString.append(" and (p.idProject in (:selectProjects)) ");
	    	parameterListMap.put("selectProjects", projects);
	    }
	    
	    if (analyses.size() > 0) {
	    	whereString.append(" and (a.idAnalysis in (:selectAnalyses)) ");
	    	parameterListMap.put("selectAnalyses", analyses);
	    }
	    
	
	    if (sources.size() > 0) {
	    	joinString.append(" left join a.samples as smp ");
	    	joinString.append(" left join smp.sampleSource as ss ");
	    	whereString.append(" and (ss.idSampleSource in (:selectSampleSource)) ");
	    	parameterListMap.put("selectSampleSource", sources);
	    }
	    
	      		  
	    //Create session and query.
	    Session session = this.getCurrentSession();
	    
	    Query query = session.createQuery(selectString + joinString.toString() + whereString.toString() );
	    
	    for (String key: parameterListMap.keySet()) {
	    	query.setParameterList(key, parameterListMap.get(key));
	    }
	    
	    for (String key: parameterMap.keySet()) {
	    	query.setParameter(key, parameterMap.get(key));
	    }
	    
	    //Fetch matching results and initialize fields
	    List<Analysis> analysesResult = query.list();
	    for (Analysis a: analysesResult) {
			this.initializeAnalysis(a);
		}
	    session.close();
	    return analysesResult;
	
	}
	
	private void initializeAnalysis(Analysis a) {
		Hibernate.initialize(a.getFile());
	    Hibernate.initialize(a.getDataTracks());
	    Hibernate.initialize(a.getSamples());
	      
	    for (DataTrack dt: a.getDataTracks()) {
	      Hibernate.initialize(dt.isAnalysisSet());
	    }
	    
	    for (Sample s: a.getSamples()) {
			Hibernate.initialize(s.isAnalysisSet());
		}
	}
	
	
}
