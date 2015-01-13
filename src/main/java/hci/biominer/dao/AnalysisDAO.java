package hci.biominer.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hibernate.SessionFactory;

import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.DashboardModel;
import hci.biominer.model.DataTrack;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.Sample;
import hci.biominer.model.SampleSource;
import hci.biominer.model.access.Institute;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;
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
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getAnalysesByQuery(List<Long> labs, List<Long> projects, List<Long> analyses, 
			List<Long> sources, Long analysisType, Long idOrganismBuild, User user) {
		
		//Basics
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
	    aqb.setSelect(" select distinct a ");
	    
	    //Add manditiory restrictions
	    addOrganismRestriction(aqb,idOrganismBuild);
	    addAnalysisTypeRestriction(aqb,analysisType);
	    
	    //Add optional restrictoins
	    if (labs.size() > 0) {
	    	addLabRestrictions(aqb,labs);
	    }
	    
	    if (projects.size() > 0) {
	    	addProjectRestrictions(aqb,projects);
	    }
	    
	    if (analyses.size() > 0) {
	    	addAnalysisRestrictions(aqb,analyses);
	    }
	    
	
	    if (sources.size() > 0) {
	    	addSampleSourceRestrictions(aqb,sources);
	    }
	    
	      		  
	    //Create session and query.
	    Session session = this.getCurrentSession();
	    
	    Query query = aqb.getQuery(session);
	    
	    //Fetch matching results and initialize fields
	    List<Analysis> analysesResult = query.list();
	    for (Analysis a: analysesResult) {
			this.initializeAnalysis(a);
		}
	    session.close();
	    return analysesResult;
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getAnalysesByQuery(List<Long> labs, List<Long> projects, 
			List<Long> sources, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		//Basics
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
		aqb.setSelect(" select distinct a ");

		//Add manditiory restrictions
		if (idOrganismBuild != null) {
			addOrganismRestriction(aqb,idOrganismBuild);
		}

		if (analysisTypes.size() > 0) {
			addAnalysisTypeRestrictions(aqb,analysisTypes);
		}

		//Add optional restrictoins
		if (labs.size() > 0) {
			addLabRestrictions(aqb,labs);
		}

		if (projects.size() > 0) {
			addProjectRestrictions(aqb,projects);
		}


		if (sources.size() > 0) {
			addSampleSourceRestrictions(aqb,sources);
		}


		//Create session and query.
		Session session = this.getCurrentSession();

		Query query = aqb.getQuery(session);

		//Fetch matching results and initialize fields
		List<Analysis> analysisResult = query.list();
		for (Analysis a: analysisResult) {
			this.initializeAnalysis(a);
		}
		session.close();
		return analysisResult;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Project> getProjectsByQuery(List<Long> labs, List<Long> analyses, 
			List<Long> sources, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		
		//Basics
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
		aqb.setSelect(" select distinct p ");

		//Add manditiory restrictions
		if (idOrganismBuild != null) {
			addOrganismRestriction(aqb,idOrganismBuild);
		}
		
		if (analysisTypes.size() > 0) {
			addAnalysisTypeRestrictions(aqb,analysisTypes);
		}
		
		//Add optional restrictoins
		if (labs.size() > 0) {
			addLabRestrictions(aqb,labs);
		}

		if (analyses.size() > 0) {
			addAnalysisRestrictions(aqb,analyses);
		}


		if (sources.size() > 0) {
			addSampleSourceRestrictions(aqb,sources);
		}


		//Create session and query.
		Session session = this.getCurrentSession();

		Query query = aqb.getQuery(session);

		//Fetch matching results and initialize fields
		List<Project> projectResult = query.list();
		for (Project p: projectResult) {
			this.initializeProject(p);
		}
		session.close();
		return projectResult;
	}
	
	@SuppressWarnings("unchecked")
	public List<AnalysisType> getAnalysisTypesByQuery(List<Long> labs, List<Long> projects, List<Long> analyses, 
			List<Long> sources, Long idOrganismBuild, User user) {
		
		//Basics
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
		aqb.setSelect(" select distinct at ");
		aqb.addToJoin(" left join a.analysisType as at ");

		//Add restrictions
		if (idOrganismBuild != null) {
			addOrganismRestriction(aqb,idOrganismBuild);
		}
		
		if (projects.size() > 0) {
			addProjectRestrictions(aqb,projects);
		}
		
		if (labs.size() > 0) {
			addLabRestrictions(aqb,labs);
		}

		if (analyses.size() > 0) {
			addAnalysisRestrictions(aqb,analyses);
		}


		if (sources.size() > 0) {
			addSampleSourceRestrictions(aqb,sources);
		}


		//Create session and query.
		Session session = this.getCurrentSession();

		Query query = aqb.getQuery(session);

		//Fetch matching results and initialize fields
		List<AnalysisType> atResult = query.list();
		
		session.close();
		return atResult;
	}
	
	@SuppressWarnings("unchecked")
	public List<Lab> getLabByQuery(User user, List<Long> analysisTypes, List<Long> projects, List<Long> analyses, List<Long> sources,
			Long idOrganismBuild) {
		//Create query object
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);


		//Create basic query
		aqb.setSelect(" select distinct l ");

		if (idOrganismBuild != null) {
			addOrganismRestriction(aqb,idOrganismBuild);
		}

		if (analyses.size() > 0) {
			addAnalysisRestrictions(aqb, analyses);
		}

		if (analysisTypes.size() > 0) {
			addAnalysisTypeRestrictions(aqb, analysisTypes);
		}

		if (sources.size() > 0) {
			addSampleSourceRestrictions(aqb, sources);
		}

		if (projects.size() > 0) {
			addProjectRestrictions(aqb,projects);
		}

		/**********************************************
		 * Run query object and fetch results
		 **********************************************/

		//Create session and query.
		Session session = this.getCurrentSession();

		Query query = aqb.getQuery(session);

		//Fetch matching results and initialize fields
		List<Lab> labList = query.list();

		session.close();
		return labList;
	}
	
	@SuppressWarnings("unchecked")
	public List<OrganismBuild> getOrgansimBuildByQuery(User user, List<Long> analysisTypes, List<Long> labs, List<Long> projects,
			List<Long> analyses, List<Long> sources) {
	
		//Create query object
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
	    
	    
	    /**********************************************
	     * Create query basics
	     **********************************************/
	    //Create basic query
	    aqb.setSelect("select distinct ob");
	    
	    
	    /**********************************************
	     * Add restrictions based on dropdowns
	     **********************************************/
	    
	    if (labs.size() > 0) {
	    	addLabRestrictions(aqb,labs);
	    }
	    
	    if (analyses.size() > 0) {
	    	addAnalysisRestrictions(aqb, analyses);
	    }
	    
	    if (analysisTypes.size() > 0) {
	    	addAnalysisTypeRestrictions(aqb, analysisTypes);
	    }
	
	    if (sources.size() > 0) {
	    	addSampleSourceRestrictions(aqb, sources);
	    }
	    
	    if (projects.size() > 0) {
	    	addProjectRestrictions(aqb,projects);
	    }
	    
	    /**********************************************
	     * Run query object and fetch results
	     **********************************************/
	    aqb.addToWhere( " and (ob.genomeFile IS NOT null and ob.transcriptFile IS NOT null) " );
	      		  
	    //Create session and query.
	    Session session = this.getCurrentSession();
	    
	    Query query = aqb.getQuery(session);
	    
	    //Fetch matching results and initialize fields
	    List<OrganismBuild> obList = query.list();
	   
	    session.close();
	    return obList;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<DashboardModel> getDashboard(AnalysisTypeEnum type) {
		ArrayList<DashboardModel> dmList = new ArrayList<DashboardModel>();
		Session session = this.getCurrentSession();
		Query query = session.createQuery("select ob.name from OrganismBuild ob");
		List<String> obNames = query.list();
		
		for (String name: obNames) {
			Query q2 = session.createQuery("select a from Analysis as a "
					+ "left join a.project as p "
					+ "left join p.organismBuild as ob "
					+ "left join a.analysisType as at "
					+ "where ob.name = :name and at.type = :type");
			q2.setParameter("name", name);
			q2.setParameter("type",type);
			
			List<Analysis> qList = new ArrayList<Analysis>();
			qList = q2.list();
			
			int size = qList.size();
			
			if (size > 0) {
				DashboardModel dm = new DashboardModel(size,name);
				dmList.add(dm);
			}
		}
		session.close();
		
		return dmList;
	}
	
	@SuppressWarnings("unchecked")
	public List<SampleSource> getSampleSourceByQuery(List<Long> labs, List<Long> analyses, 
			List<Long> projects, List<Long> analysisTypes, Long idOrganismBuild, User user) {
		
		//Basics
		AnalysisQueryBuilder aqb = new AnalysisQueryBuilder(user);
		aqb.setSelect(" select distinct ss ");
		aqb.addToJoin(" left join a.samples as smp ");
    	aqb.addToJoin(" left join smp.sampleSource as ss ");
		

		//Add restrictions
		if (idOrganismBuild != null) {
			addOrganismRestriction(aqb,idOrganismBuild);
		}
		
		if (analysisTypes.size() > 0) {
			addAnalysisTypeRestrictions(aqb,analysisTypes);
		}
		
		if (labs.size() > 0) {
			addLabRestrictions(aqb,labs);
		}

		if (analyses.size() > 0) {
			addAnalysisRestrictions(aqb,analyses);
		}


		if (projects.size() > 0) {
			addProjectRestrictions(aqb,projects);
		}

		//Create session and query.
		Session session = this.getCurrentSession();

		Query query = aqb.getQuery(session);

		//Fetch matching results and initialize fields
		List<SampleSource> ssResult = query.list();
		
		session.close();
		return ssResult;
	}
	
	private void addOrganismRestriction(AnalysisQueryBuilder aqb, Long idOrganismBuild) {
    	aqb.addToWhere(" and ( ob.idOrganismBuild = :idOrganismBuild ) ");
    	aqb.addParameter("idOrganismBuild", idOrganismBuild);
	}
	
	private void addLabRestrictions(AnalysisQueryBuilder aqb, List<Long> labs) {
		aqb.addToWhere(" and (l.idLab in (:selectLabs)) ");
    	aqb.addListParameter("selectLabs", labs);
	}
	
	private void  addAnalysisRestrictions(AnalysisQueryBuilder aqb, List<Long> analyses) {
		aqb.addToWhere(" and (a.idAnalysis in (:selectAnalyses)) ");
    	aqb.addListParameter("selectAnalyses", analyses);
	}
	
	private void addProjectRestrictions(AnalysisQueryBuilder aqb, List<Long> projects) {
		aqb.addToWhere(" and (p.idProject in (:selectProjects)) ");
    	aqb.addListParameter("selectProjects", projects);
	}
	
	private void addSampleSourceRestrictions(AnalysisQueryBuilder aqb, List<Long> sources) {
		aqb.addToJoin(" left join a.samples as smp ");
    	aqb.addToJoin(" left join smp.sampleSource as ss ");
    	aqb.addToWhere(" and (ss.idSampleSource in (:selectSampleSource)) ");
    	aqb.addListParameter("selectSampleSource", sources);
	}
	
	private void addAnalysisTypeRestrictions(AnalysisQueryBuilder aqb, List<Long> analysisTypes) {
		aqb.addToJoin(" left join a.analysisType as at ");
    	aqb.addToWhere(" and (at.idAnalysisType in (:selectAnalysisTypes)) ");
    	aqb.addListParameter("selectAnalysisTypes", analysisTypes);
	}
	
	private void addAnalysisTypeRestriction(AnalysisQueryBuilder aqb, Long idAnalysisType ) {
		aqb.addToJoin(" left join a.analysisType as at ");
    	aqb.addToWhere(" and (at.idAnalysisType = :selectAnalysisType) ");
    	aqb.addParameter("selectAnalysisType", idAnalysisType);
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
	
	public Project initializeProject(Project p) {
		 
		Hibernate.initialize(p.getSamples());
		Hibernate.initialize(p.getDataTracks());
		Hibernate.initialize(p.getFiles());
		Hibernate.initialize(p.getAnalyses());
		Hibernate.initialize(p.getLabs());
		Hibernate.initialize(p.getInstitutes());

		for (Analysis a: p.getAnalyses()) {
			Hibernate.initialize(a.getSamples());
			Hibernate.initialize(a.getFile());
			Hibernate.initialize(a.getDataTracks());
		}

		for (Sample s: p.getSamples()) {
			Hibernate.initialize(s.isAnalysisSet());
		}

		for (DataTrack d: p.getDataTracks()) {
			Hibernate.initialize(d.isAnalysisSet());
		}
		
		return p;
	}
	
	
	class AnalysisQueryBuilder {
		private String selectString;
		private StringBuilder joinString;
		private StringBuilder whereString;
		private HashMap<String,Object> parameterMap;
		private HashMap<String,Collection> parameterListMap;
		
		public AnalysisQueryBuilder(User user) {
			parameterMap = new HashMap<String,Object>();
			parameterListMap = new HashMap<String,Collection>();
			
			selectString = "";
			
			joinString = new StringBuilder(" from Analysis a ");
			joinString.append(" left join a.project as p ");
		    joinString.append(" left join p.labs as l ");
		    joinString.append(" left join p.institutes as i ");
		    joinString.append(" left join p.organismBuild as ob ");
		    
			whereString = new StringBuilder("");
			
			//public visibility
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
			
		    //Only return analyses that can be converted into interval trees
			whereString.append(" and (ob.genomeFile IS NOT NULL) ");
		    whereString.append(" and (a.file IS NOT NULL) ");
			
			
		}
		
		public Query getQuery(Session session) {
			Query query = session.createQuery(selectString + joinString.toString() + whereString.toString() );
		    
		    for (String key: parameterListMap.keySet()) {
		    	query.setParameterList(key, parameterListMap.get(key));
		    }
		    
		    for (String key: parameterMap.keySet()) {
		    	query.setParameter(key, parameterMap.get(key));
		    }
			
		    return query;
		}
		
		
		public void addListParameter(String parameter, Collection collection) {
			parameterListMap.put(parameter, collection);
		}
		
		public void addParameter(String parameter, Object object) {
			parameterMap.put(parameter, object);
		}
		
		public void setSelect(String selectStatement) {
			selectString = selectStatement;
		}
		
		public void addToJoin(String joinStatement) {
			joinString.append(" " + joinStatement + " ");
		}
		
		public void addToWhere(String whereStatement) {
			whereString.append(" " + whereStatement + " ");
		}
		
	}
	
	
}
