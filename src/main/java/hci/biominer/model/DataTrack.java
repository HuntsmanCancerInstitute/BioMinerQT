package hci.biominer.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
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
	
	@ManyToMany
	@JoinTable(name="AnalysisDataTrack",
				joinColumns={@JoinColumn(name="idDataTrack")},
				inverseJoinColumns={@JoinColumn(name="idAnalysis")})
	@JsonIgnore
	List<Analysis> analyses;

	@Column(name="name")
	String name;
	
	@Column(name="path")
	String path;
	
	public DataTrack() {
		
	}
	
	public DataTrack(String name, String path, Project project) {
		this.name = name;
		this.path = path;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}
	
	public boolean isAnalysisSet() {
		if (this.analyses == null || this.analyses.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	
}
