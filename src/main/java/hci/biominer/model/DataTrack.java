package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name="DataTrack")
public class DataTrack {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idDataTrack")
	Long idDataTrack;
	
	@ManyToOne
	@JoinColumn(name="idProject")
	@JsonIgnore
	Project project;
	
	@ManyToOne
	@JoinColumn(name="idAnalysis")
	@JsonIgnore
	Analysis analysis;

	@Column(name="name")
	String name;
	
	@Column(name="url")
	String url;
	
	public DataTrack() {
		
	}
	
	public DataTrack(String name, String url, Project project) {
		this.name = name;
		this.url = url;
		this.project = project;
	}

	public Long getIdDataTrack() {
		return idDataTrack;
	}

	public void setIdDataTrack(Long idDataTrack) {
		this.idDataTrack = idDataTrack;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	
	
}
